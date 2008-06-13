/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osee.framework.jdk.core.util.OseeUser;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.dbinit.SkynetDbInit;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.user.UserEnum;
import org.eclipse.osee.framework.skynet.core.user.UserNotInDatabase;
import org.eclipse.osee.framework.ui.plugin.event.AuthenticationEvent;
import org.eclipse.osee.framework.ui.plugin.security.AuthenticationDialog;
import org.eclipse.osee.framework.ui.plugin.security.OseeAuthentication;
import org.eclipse.osee.framework.ui.plugin.security.UserCredentials.UserCredentialEnum;
import org.eclipse.swt.widgets.Display;

/**
 * <b>Skynet Authentication</b><br/> Provides mapping of the current Authenticated User Id to its User Artifact in the
 * Skynet Database.
 * 
 * @author Roberto E. Escobar
 */
public class SkynetAuthentication {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(SkynetAuthentication.class);
   private int noOneArtifactId;
   private boolean createUserWhenNotInDatabase = true;

   public static enum UserStatusEnum {
      Active, InActive, Both
   }
   private boolean firstTimeThrough;
   private Map<String, User> userIdToUserCache;
   private Map<String, User> nameToUserCache;
   private Map<Integer, User> artIdToUserCache;
   private ArrayList<User> activeUserCache;
   private Map<OseeUser, User> enumeratedUserCache;
   private String[] sortedActiveUserNameCache;
   private User currentUser;
   private boolean duringUserCreation;

   private static final SkynetAuthentication instance = new SkynetAuthentication();

   private SkynetAuthentication() {
      firstTimeThrough = true;
   }

   private boolean isInLoadUsersCache = false;

   private synchronized void loadUsersCache() throws OseeCoreException, SQLException {
      if (activeUserCache == null) {
         isInLoadUsersCache = true;
         enumeratedUserCache = new HashMap<OseeUser, User>(30);
         nameToUserCache = new HashMap<String, User>(800);
         userIdToUserCache = new HashMap<String, User>(800);
         artIdToUserCache = new HashMap<Integer, User>(800);
         activeUserCache = new ArrayList<User>(700);
         Collection<Artifact> dbUsers =
               ArtifactQuery.getArtifactsFromType(User.ARTIFACT_NAME, BranchPersistenceManager.getCommonBranch());
         for (Artifact a : dbUsers) {
            User user = (User) a;
            cacheUser(user, null);
         }
         isInLoadUsersCache = false;
      }
   }

   private void cacheUser(User user, OseeUser userEnum) throws OseeCoreException, SQLException {
      // If cacheUser is called outside of the main loadUserCache, then load cache first
      if (!isInLoadUsersCache) loadUsersCache();
      if (user.isActive()) activeUserCache.add(user);
      nameToUserCache.put(user.getDescriptiveName(), user);
      userIdToUserCache.put(user.getUserId(), user);
      if (user.isInDb()) artIdToUserCache.put(user.getArtId(), user);
      if (userEnum != null) enumeratedUserCache.put(userEnum, user);
   }

   public boolean isAuthenticated() {
      return OseeAuthentication.getInstance().isAuthenticated();
   }

   private void forceAuthenticationRoutine() {
      OseeAuthentication oseeAuthentication = OseeAuthentication.getInstance();
      if (!oseeAuthentication.isAuthenticated()) {
         if (oseeAuthentication.isLoginAllowed()) {
            AuthenticationDialog.openDialog();
         } else {
            oseeAuthentication.authenticate("", "", "", false);
         }
         notifyListeners();
      }
   }

   private static void notifyListeners() {
      Display.getDefault().asyncExec(new Runnable() {
         public void run() {
            SkynetEventManager.getInstance().kick(new AuthenticationEvent(this));
         }
      });
   }

   /**
    * Returns the currently authenticated user
    * 
    * @return
    */
   public static User getUser() {
      return instance.getAuthenticatedUser();
   }

   private synchronized User getAuthenticatedUser() {
      try {
         if (SkynetDbInit.isPreArtifactCreation()) {
            return BootStrapUser.getInstance();
         } else {
            if (firstTimeThrough) {
               forceAuthenticationRoutine();
            }

            OseeAuthentication oseeAuthentication = OseeAuthentication.getInstance();
            if (!oseeAuthentication.isAuthenticated()) {
               currentUser = getUser(UserEnum.Guest);
            } else {
               String userId = oseeAuthentication.getCredentials().getField(UserCredentialEnum.Id);
               if (currentUser == null || !currentUser.getUserId().equals(userId)) {
                  try {
                     currentUser = getUserByIdWithError(userId);
                  } catch (UserNotInDatabase ex) {
                     if (createUserWhenNotInDatabase) {
                        currentUser =
                              createUser(oseeAuthentication.getCredentials().getField(UserCredentialEnum.Name),
                                    "spawnedBySkynet", userId, true);
                        persistUser(currentUser); // this is done outside of the crateUser call to avoid recursion
                     } else
                        currentUser = getUser(UserEnum.Guest);
                  }
               }
            }
            firstTimeThrough = false; // firstTimeThrough must be set false after last use of its value
         }
      } catch (OseeCoreException ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      } catch (SQLException ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }

      return currentUser;
   }

   private void persistUser(User user) throws OseeCoreException, SQLException {
      duringUserCreation = true;
      try {
         user.persistAttributesAndRelations();
         cacheUser(user, null);
      } catch (SQLException ex) {
         duringUserCreation = false;
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
   }

   public static User createUser(OseeUser userEnum) throws OseeCoreException, SQLException {
      User user =
            instance.createUser(userEnum.getName(), userEnum.getEmail(), userEnum.getUserID(), userEnum.isActive());
      instance.persistUser(user);
      return user;
   }

   public static User getUser(OseeUser userEnum) throws OseeCoreException, SQLException {
      instance.loadUsersCache();
      User user = instance.enumeratedUserCache.get(userEnum);
      if (user == null) {
         user = getUserByIdWithError(userEnum.getUserID());
         if (user == null) throw new UserNotInDatabase("UserEnum \"" + userEnum + "\" not in database.");
         instance.enumeratedUserCache.put(userEnum, user);
      }
      return instance.enumeratedUserCache.get(userEnum);
   }

   private User createUser(String name, String email, String userID, boolean active) throws OseeCoreException, SQLException {
      duringUserCreation = true;
      User user = null;
      try {
         user =
               (User) ArtifactTypeManager.addArtifact(User.ARTIFACT_NAME, BranchPersistenceManager.getCommonBranch(),
                     name);
         user.setActive(active);
         user.setUserID(userID);
         user.setEmail(email);
         // this is here in case a user is created at an unexpected time
         if (!SkynetDbInit.isDbInit()) logger.log(Level.INFO, "Created user " + user, new Exception(
               "just wanted the stack trace"));
      } finally {
         duringUserCreation = false;
      }
      return user;
   }

   /**
    * @return shallow copy of ArrayList of all active users in the datastore sorted by user name
    */
   @SuppressWarnings("unchecked")
   public static ArrayList<User> getUsers() throws OseeCoreException, SQLException {
      instance.loadUsersCache();
      return (ArrayList<User>) instance.activeUserCache.clone();
   }

   public static User getUserByIdWithError(String userId) throws OseeCoreException, SQLException {
      instance.loadUsersCache();
      if (userId == null || userId.equals("")) {
         throw new IllegalArgumentException("UserId can't be null or \"\"");
      }
      User user = instance.userIdToUserCache.get(userId);
      if (user == null) throw new UserNotInDatabase("User requested by id \"" + userId + "\" was not found.");
      return user;
   }

   /**
    * Return sorted list of active User.getName() in database
    * 
    * @return String[]
    */
   public static String[] getUserNames() throws OseeCoreException, SQLException {
      instance.loadUsersCache();
      // Sort if null or new names added since last sort
      if (instance.sortedActiveUserNameCache == null || instance.sortedActiveUserNameCache.length != instance.userIdToUserCache.size()) {
         Collections.sort(instance.activeUserCache);
         int i = 0;
         instance.sortedActiveUserNameCache = new String[instance.activeUserCache.size()];
         for (User user : instance.activeUserCache) {
            instance.sortedActiveUserNameCache[i++] = user.getName();
         }
      }
      return instance.sortedActiveUserNameCache;
   }

   /**
    * @param name
    * @param create if true, will create a temp user artifact; should only be used for dev purposes
    * @return user
    */
   public static User getUserByName(String name, boolean create) throws OseeCoreException, SQLException {
      instance.loadUsersCache();
      User user = instance.nameToUserCache.get(name);
      if (create) {
         instance.persistUser(instance.createUser(name, "", name, true));
         user = instance.nameToUserCache.get(name);
         if (user == null) throw new UserNotInDatabase(
               "Error creating and caching user \"" + name + "\" was not found.");
      }
      if (user == null) throw new UserNotInDatabase("User requested by name \"" + name + "\" was not found.");
      return user;
   }

   public static User getUserByArtId(int authorId) throws OseeCoreException, SQLException {
      instance.loadUsersCache();
      User user = instance.artIdToUserCache.put(authorId, null);
      if (user == null) throw new UserNotInDatabase("User requested by artId \"" + authorId + "\" was not found.");
      return user;
   }

   /**
    * @return whether the Authentication manager is in the middle of creating a user
    */
   public static boolean duringUserCreation() {
      return instance.duringUserCreation;
   }

   public static int getNoOneArtifactId() {
      if (instance.noOneArtifactId == 0) {
         try {
            instance.noOneArtifactId = getUser(UserEnum.NoOne).getArtId();
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.toString(), ex);
            instance.noOneArtifactId = -1;
         }
      }
      return instance.noOneArtifactId;
   }
}