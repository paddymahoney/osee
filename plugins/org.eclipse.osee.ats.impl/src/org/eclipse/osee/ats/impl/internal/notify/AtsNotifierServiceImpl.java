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
package org.eclipse.osee.ats.impl.internal.notify;

import java.util.Collection;
import org.eclipse.osee.ats.api.notify.AtsNotificationEvent;
import org.eclipse.osee.mail.api.MailService;

/**
 * @author Donald G. Dunne
 */
public class AtsNotifierServiceImpl implements IAtsNotifierServer {

   private MailService mailService;

   public void setMailService(MailService mailService) {
      this.mailService = mailService;
   }

   /**
    * Send notifications
    */
   @Override
   public void sendNotifications(String fromUserEmail, String testingUserEmail, String subject, String body, Collection<? extends AtsNotificationEvent> notificationEvents) {
      SendNotificationEvents job =
         new SendNotificationEvents(mailService, fromUserEmail, testingUserEmail, subject, body, notificationEvents);
      job.run();
   }

}