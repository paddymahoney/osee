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
package org.eclipse.osee.framework.database.init.internal;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.database.init.IDbInitializationTask;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.ConnectionHandler;

/**
 * @author Roberto E. Escobar
 */
public class PostDbUserCleanUp implements IDbInitializationTask {
   private static final String UPDATE_BOOTSTRAP_USER_ID = "UPDATE osee_tx_details SET author = ? where author <= 0";

   @Override
   public void run() throws OseeCoreException {
      OseeLog.log(DatabaseInitActivator.class, Level.INFO, "Post Initialization User Clean-up");

      int authorArtId = 0;
      boolean isUserAuthenticationAllowed = false;
      try {
         // Check that this is a normal initialization
         Artifact root = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(COMMON);
         if (root != null) {
            isUserAuthenticationAllowed = true;
         }
      } catch (OseeCoreException ex) {
         // Do Nothing -- failure expected during base initialization
      }

      if (isUserAuthenticationAllowed) {
         // This is a regular initialization - users have been created.

         // Release bootstrap session session
         ClientSessionManager.releaseSession();

         User user = null;
         if (OseeProperties.isInTest()) {
            user = UserManager.getUser(SystemUser.OseeSystem);
         } else {
            user = UserManager.getUser();
         }
         authorArtId = user.getArtId();
      } else {
         // This is an initialization for base import -- users are not available
         OseeLog.log(DatabaseInitActivator.class, Level.INFO,
            "Post Initialization User Clean-up - Base Initialization - unable to set tx author id");
      }

      if (authorArtId > 0) {
         // Set author to current authenticated user art id
         ConnectionHandler.runPreparedUpdate(UPDATE_BOOTSTRAP_USER_ID, authorArtId);
      }

      DatabaseInitActivator.getInstance().getCachingService().clearAll();
      DatabaseInitActivator.getInstance().getCachingService().reloadTypes();
   }
}
