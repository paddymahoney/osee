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
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.util;

import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.core.users.AtsCoreUsers;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class AtsUsersTest {

   @Test
   public void testGetUser() throws OseeCoreException {
      Assert.assertEquals(AtsCoreUsers.SYSTEM_USER,
         AtsClientService.get().getUserService().getUserById(SystemUser.OseeSystem.getUserId()));
      Assert.assertEquals(AtsCoreUsers.ANONYMOUS_USER,
         AtsClientService.get().getUserService().getUserById(SystemUser.Anonymous.getUserId()));
      Assert.assertEquals(AtsCoreUsers.UNASSIGNED_USER,
         AtsClientService.get().getUserService().getUserById(SystemUser.UnAssigned.getUserId()));
   }

   @Test
   public void testGetUserException() throws OseeCoreException {
      Assert.assertNull(AtsClientService.get().getUserService().getUserById("2345"));
   }

   @Test
   public void testGetUserWithNull() throws OseeCoreException {
      Assert.assertNull(AtsClientService.get().getUserService().getUserById(null));
   }

   @Test
   public void testGetCurrentUser() throws OseeCoreException {
      User currentUser = UserManager.getUser();

      Assert.assertEquals(currentUser, AtsClientService.get().getUserServiceClient().getCurrentOseeUser());

      IAtsUser atsUser = AtsClientService.get().getUserService().getUserById(currentUser.getUserId());

      Assert.assertEquals(currentUser.getUserId(), atsUser.getUserId());
      Assert.assertEquals(currentUser.getEmail(), atsUser.getEmail());
      Assert.assertEquals(currentUser.isActive(), atsUser.isActive());
      Assert.assertEquals(currentUser.getName(), atsUser.getName());
      Assert.assertEquals(atsUser, AtsClientService.get().getUserService().getCurrentUser());
   }

}
