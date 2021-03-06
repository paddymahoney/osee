/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.activity.internal.jaxrs;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import org.eclipse.osee.activity.ActivityConstants;
import org.eclipse.osee.activity.api.Activity;
import org.eclipse.osee.activity.api.ActivityLog;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;

/**
 * @author Ryan D. Brooks
 */
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
@Provider
public class ActivityLogRequestFilter implements ContainerRequestFilter {

   private Log logger;
   private ActivityLog activityLog;

   public void setActivityLogger(ActivityLog activityLog) {
      this.activityLog = activityLog;
   }

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   /**
    * Called before a resource method is executed
    */
   @Override
   public void filter(ContainerRequestContext context) {
      if (activityLog.isEnabled()) {
         try {
            String message = String.format("%s %s", context.getMethod(), context.getUriInfo().getPath());
            Long serverId = getServerId(context);
            Long clientId = ActivityConstants.DEFAULT_CLIENT_ID;
            if (Strings.isNumeric(context.getHeaders().getFirst("osee.client.id"))) {
               clientId = Long.valueOf(context.getHeaders().getFirst("osee.client.id"));
            }

            Long accountId = ActivityConstants.DEFAULT_ACCOUNT_ID;
            if (Strings.isNumeric(context.getHeaders().getFirst("osee.account.id"))) {
               accountId = Long.valueOf(context.getHeaders().getFirst("osee.account.id"));
            }
            Long entryId =
               activityLog.createActivityThread(Activity.JAXRS_METHOD_CALL, accountId, serverId, clientId, message);

            context.setProperty(ActivityConstants.HTTP_HEADER__ACTIVITY_ENTRY_ID, entryId);
         } catch (Throwable th) {
            logger.error(th, "Error during ActivityContainerRequestFilter");
         }
      }
   }

   private Long getServerId(ContainerRequestContext context) {
      Long toReturn = ActivityConstants.DEFAULT_SERVER_ID;
      return toReturn;
   }

}
