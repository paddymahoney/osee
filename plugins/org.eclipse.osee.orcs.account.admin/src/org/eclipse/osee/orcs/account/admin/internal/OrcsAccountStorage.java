/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.account.admin.internal;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.osee.account.admin.Account;
import org.eclipse.osee.account.admin.AccountPreferences;
import org.eclipse.osee.account.admin.AccountSession;
import org.eclipse.osee.account.admin.CreateAccountRequest;
import org.eclipse.osee.account.admin.ds.AccountStorage;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.Operator;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.jdk.core.type.Identifiable;
import org.eclipse.osee.framework.jdk.core.type.Identity;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.data.ArtifactId;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.utility.OrcsUtil;

/**
 * @author Roberto E. Escobar
 */
public class OrcsAccountStorage extends AbstractOrcsStorage implements AccountStorage {

   private IOseeDatabaseService dbService;
   private AccountSessionStorage sessionStore;

   public void setDatabaseService(IOseeDatabaseService dbService) {
      this.dbService = dbService;
   }

   @Override
   public void start() {
      super.start();
      sessionStore = new AccountSessionDatabaseStore(getLogger(), dbService, getFactory());
   }

   @Override
   public void stop() {
      super.stop();
      sessionStore = null;
   }

   @Override
   public boolean userNameExists(String username) {
      int count = newQuery().andIsOfType(CoreArtifactTypes.User).and(CoreAttributeTypes.UserId, username).getCount();
      return count > 0;
   }

   @Override
   public boolean emailExists(String email) {
      int count = newQuery().andIsOfType(CoreArtifactTypes.User).and(CoreAttributeTypes.Email, email).getCount();
      return count > 0;
   }

   @Override
   public boolean displayNameExists(String displayName) {
      int count = newQuery().andIsOfType(CoreArtifactTypes.User).andNameEquals(displayName).getCount();
      return count > 0;
   }

   @Override
   public ResultSet<Account> getAllAccounts() {
      ResultSet<ArtifactReadable> results = newQuery().andIsOfType(CoreArtifactTypes.User).getResults();
      return getFactory().newAccountResultSet(results);
   }

   @Override
   public ResultSet<Account> getAccountByUserName(String username) {
      ResultSet<ArtifactReadable> results =
         newQuery().andIsOfType(CoreArtifactTypes.User).and(CoreAttributeTypes.UserId, username).getResults();
      return getFactory().newAccountResultSet(results);
   }

   @Override
   public ResultSet<Account> getAccountByUuid(String accountUuid) {
      ResultSet<ArtifactReadable> results =
         newQuery().andIsOfType(CoreArtifactTypes.User).andGuid(accountUuid).getResults();
      return getFactory().newAccountResultSet(results);
   }

   @Override
   public ResultSet<Account> getAccountByLocalId(long accountId) {
      int id = Long.valueOf(accountId).intValue();
      ResultSet<ArtifactReadable> results = newQuery().andIsOfType(CoreArtifactTypes.User).andLocalId(id).getResults();
      return getFactory().newAccountResultSet(results);
   }

   @Override
   public ResultSet<Account> getAccountByEmail(String email) {
      ResultSet<ArtifactReadable> results =
         newQuery().andIsOfType(CoreArtifactTypes.User).and(CoreAttributeTypes.Email, Operator.EQUAL, email).getResults();
      return getFactory().newAccountResultSet(results);
   }

   @Override
   public ResultSet<Account> getAccountByName(String name) {
      ResultSet<ArtifactReadable> results =
         newQuery().andIsOfType(CoreArtifactTypes.User).andNameEquals(name).getResults();
      return getFactory().newAccountResultSet(results);
   }

   @Override
   public ResultSet<AccountPreferences> getAccountPreferencesById(long accountId) {
      int id = Long.valueOf(accountId).intValue();
      ResultSet<ArtifactReadable> results = newQuery().andIsOfType(CoreArtifactTypes.User).andLocalId(id).getResults();
      return getFactory().newAccountPreferencesResultSet(results);
   }

   @Override
   public ResultSet<AccountPreferences> getAccountPreferencesByUuid(String uuid) {
      ResultSet<ArtifactReadable> results = newQuery().andIsOfType(CoreArtifactTypes.User).andGuid(uuid).getResults();
      return getFactory().newAccountPreferencesResultSet(results);
   }

   @Override
   public Identifiable<String> createAccount(CreateAccountRequest request) {
      TransactionBuilder tx = newTransaction("Create Account");
      ArtifactId artId = tx.createArtifact(CoreArtifactTypes.User, request.getDisplayName());
      tx.setSoleAttributeFromString(artId, CoreAttributeTypes.Email, request.getEmail());
      tx.setSoleAttributeFromString(artId, CoreAttributeTypes.UserId, request.getUserName());
      tx.setSoleAttributeValue(artId, CoreAttributeTypes.Active, request.isActive());

      Map<String, String> preferences = request.getPreferences();
      if (preferences != null && !preferences.isEmpty()) {
         String prefValue = asString(artId.getGuid(), preferences);
         tx.createAttribute(artId, CoreAttributeTypes.UserSettings, prefValue);
      }
      tx.commit();
      return artId;
   }

   @Override
   public void setActive(Identifiable<String> account, boolean active) {
      ArtifactId artId = OrcsUtil.newArtifactId(account.getGuid(), account.getName());

      TransactionBuilder tx = newTransaction("Update Account Active");
      tx.setSoleAttributeValue(artId, CoreAttributeTypes.Active, active);
      tx.commit();
   }

   private String asString(String uuid, Map<String, String> preferences) {
      PropertyStore settings = new PropertyStore(uuid);
      for (Entry<String, String> entry : preferences.entrySet()) {
         settings.put(entry.getKey(), entry.getValue());
      }

      StringWriter stringWriter = new StringWriter();
      try {
         settings.save(stringWriter);
         return stringWriter.toString();
      } catch (Exception ex) {
         throw new OseeCoreException(ex);
      }
   }

   @Override
   public void setAccountPreferences(Identity<String> account, Map<String, String> preferences) {
      String prefValue = asString(account.getGuid(), preferences);

      ArtifactId artId = OrcsUtil.newArtifactId(account.getGuid(), "N/A");
      TransactionBuilder tx = newTransaction("User - Save Settings");
      tx.setSoleAttributeFromString(artId, CoreAttributeTypes.UserSettings, prefValue);
      tx.commit();
   }

   @Override
   public void deleteAccount(Identifiable<String> account) {
      ArtifactId artId = OrcsUtil.newArtifactId(account.getGuid(), account.getName());

      TransactionBuilder tx = newTransaction("Delete User");
      tx.deleteArtifact(artId);
      tx.commit();
   }

   @Override
   public ResultSet<AccountSession> getAccountSessionById(long accountId) {
      try {
         return sessionStore.getAccountSessionByAccountId(accountId).call();
      } catch (Exception ex) {
         throw new OseeCoreException(ex);
      }
   }

   @Override
   public ResultSet<AccountSession> getAccountSessionBySessionToken(String sessionToken) {
      try {
         return sessionStore.getAccountSessionBySessionToken(sessionToken).call();
      } catch (Exception ex) {
         throw new OseeCoreException(ex);
      }
   }

   @Override
   public AccountSession createAccountSession(String sessionToken, Account account, String remoteAddress, String accessDetails) {
      AccountSession session =
         getFactory().newAccountSession(account.getId(), sessionToken, remoteAddress, accessDetails);
      try {
         sessionStore.createAccountSession(Collections.singleton(session)).call();
         return session;
      } catch (Exception ex) {
         throw new OseeCoreException(ex);
      }
   }

   @Override
   public void deleteAccountSessionBySessionToken(String sessionToken) {
      try {
         sessionStore.deleteAccountSessionBySessionToken(sessionToken).call();
      } catch (Exception ex) {
         throw new OseeCoreException(ex);
      }
   }

}