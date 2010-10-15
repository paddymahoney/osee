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
package org.eclipse.osee.framework.ui.skynet.util.email;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OseeEmail;
import org.eclipse.osee.framework.ui.skynet.util.OseeEmail.BodyType;

/**
 * @author Donald G. Dunne
 */
public class EmailUtil {

   private static Pattern addressPattern = Pattern.compile(".+?@.+?\\.[a-z]+");

   public static boolean isEmailValid(String email) {
      return addressPattern.matcher(email).matches();
   }

   public static boolean isEmailValid(User user) throws OseeCoreException {
      return isEmailValid(user.getEmail());
   }

   public static Collection<User> getValidEmailUsers(Collection<User> users) {
      Set<User> validUsers = new HashSet<User>();
      for (User user : users) {
         try {
            if (isEmailValid(user)) {
               validUsers.add(user);
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }
      return validUsers;
   }

   public static Collection<User> getActiveEmailUsers(Collection<User> users) {
      Set<User> activeUsers = new HashSet<User>();
      for (User user : users) {
         try {
            if (user.isActive()) {
               activeUsers.add(user);
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }
      return activeUsers;
   }

   public static void emailHtml(Collection<String> emails, String subject, String htmlBody) throws OseeCoreException {
      OseeEmail emailMessage =
         new OseeEmail(emails, UserManager.getUser().getEmail(), UserManager.getUser().getEmail(), subject, htmlBody,
            BodyType.Html);
      emailMessage.send();
   }

}
