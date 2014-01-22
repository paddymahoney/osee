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
package org.eclipse.osee.account.admin.internal;

import org.eclipse.osee.account.admin.Account;
import org.eclipse.osee.account.admin.AccountAdmin;
import org.eclipse.osee.account.admin.AccountField;
import org.eclipse.osee.account.admin.AccountPreferences;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSetTransform.Function;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.eclipse.osee.framework.jdk.core.util.Conditions;

/**
 * @author Roberto E. Escobar
 */
public class AccountFieldResolver {

   private final AccountValidator validator;
   private final AccountAdmin accountAdmin;

   public AccountFieldResolver(AccountValidator validator, AccountAdmin accountAdmin) {
      super();
      this.validator = validator;
      this.accountAdmin = accountAdmin;
   }

   private long parseLocalId(String uniqueFieldValue) {
      return Long.valueOf(uniqueFieldValue);
   }

   public ResultSet<Account> resolveAccount(String uniqueFieldValue) {
      Conditions.checkNotNullOrEmpty(uniqueFieldValue, "account unique field value");
      ResultSet<Account> toReturn;
      AccountField type = validator.guessFormatType(uniqueFieldValue);
      switch (type) {
         case EMAIL:
            toReturn = accountAdmin.getAccountByEmail(uniqueFieldValue);
            break;
         case LOCAL_ID:
            long id = parseLocalId(uniqueFieldValue);
            toReturn = accountAdmin.getAccountById(id);
            break;
         case UUID:
            toReturn = accountAdmin.getAccountByUuid(uniqueFieldValue);
            break;
         case DISPLAY_NAME:
            toReturn = accountAdmin.getAccountByName(uniqueFieldValue);
            break;
         case USERNAME:
            toReturn = accountAdmin.getAccountByUserName(uniqueFieldValue);
            break;
         default:
            toReturn = ResultSets.emptyResultSet();
            break;
      }
      return toReturn;
   }

   public ResultSet<AccountPreferences> resolveAccountPreferences(String uniqueField) {
      ResultSet<Account> result = resolveAccount(uniqueField);
      return ResultSets.transform(result, new Function<String, Account, AccountPreferences>() {
         @Override
         public AccountPreferences apply(Account source) {
            return source.getPreferences();
         }
      });
   }
}