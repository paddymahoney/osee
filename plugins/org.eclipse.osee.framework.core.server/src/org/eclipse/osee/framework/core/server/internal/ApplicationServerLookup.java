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
package org.eclipse.osee.framework.core.server.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.OseeServerInfo;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.server.IApplicationServerLookup;
import org.eclipse.osee.framework.core.server.ServerThreads;
import org.eclipse.osee.framework.core.util.HttpProcessor;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class ApplicationServerLookup implements IApplicationServerLookup {

   private static ThreadFactory threadFactory = null;

   @Override
   public Collection<OseeServerInfo> getAvailableServers() throws OseeCoreException {
      return getHealthyServers(ApplicationServerDataStore.getAllApplicationServerInfos());
   }

   private Collection<OseeServerInfo> getHealthyServers(Collection<OseeServerInfo> infos) {
      List<OseeServerInfo> healthyServers = new ArrayList<OseeServerInfo>();
      List<OseeServerInfo> unHealthyServers = new ArrayList<OseeServerInfo>();
      for (OseeServerInfo info : infos) {
         if (isServerAlive(info)) {
            if (info.isAcceptingRequests()) {
               healthyServers.add(info);
            }
         } else {
            unHealthyServers.add(info);
         }
      }
      cleanUpServers(unHealthyServers);
      return healthyServers;
   }

   @Override
   public OseeServerInfo getServerInfoBy(String version) throws OseeCoreException {
      Collection<OseeServerInfo> healthyServers =
         getHealthyServers(ApplicationServerDataStore.getApplicationServerInfos(version));
      return getBestAvailable(healthyServers);
   }

   private static void cleanUpServers(final Collection<OseeServerInfo> unHealthyServers) {
      if (!unHealthyServers.isEmpty()) {
         if (threadFactory == null) {
            threadFactory = ServerThreads.createNewThreadFactory("Server Status Thread Factory");
         }

         Thread thread = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
               try {
                  ApplicationServerDataStore.removeByServerId(unHealthyServers);
               } catch (OseeCoreException ex) {
                  OseeLog.logf(ServerActivator.class, Level.SEVERE,
                     ex, "Error removing unhealthy server entries: [%s]", unHealthyServers);
               }
            }
         });
         thread.start();
      }
   }

   private OseeServerInfo getBestAvailable(Collection<OseeServerInfo> infos) throws OseeCoreException {
      OseeServerInfo result = null;
      if (infos.size() == 1) {
         result = infos.iterator().next();
      } else {
         int minSessions = Integer.MAX_VALUE;
         for (OseeServerInfo info : infos) {
            try {
               int numberOfSessions = ApplicationServerDataStore.getNumberOfSessions(info.getServerId());
               if (minSessions > numberOfSessions) {
                  result = info;
                  minSessions = numberOfSessions;
               }
            } catch (OseeDataStoreException ex) {
               OseeLog.log(ServerActivator.class, Level.SEVERE, ex);
            }
         }
      }
      return result;
   }

   private boolean isServerAlive(OseeServerInfo info) {
      return HttpProcessor.isAlive(info.getServerAddress(), info.getPort());
   }

}
