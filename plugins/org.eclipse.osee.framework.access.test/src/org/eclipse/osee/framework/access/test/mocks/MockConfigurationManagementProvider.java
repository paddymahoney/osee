/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.access.test.mocks;

import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.services.ConfigurationManagement;
import org.eclipse.osee.framework.core.services.ConfigurationManagementProvider;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public class MockConfigurationManagementProvider implements ConfigurationManagementProvider {
   private final IBasicArtifact<?> expectedUser;
   private final Object expectedObject;
   private final ConfigurationManagement cmToReturn;
   private boolean wasGetCMCalled;

   public MockConfigurationManagementProvider(IBasicArtifact<?> expectedUser, Object expectedObject, ConfigurationManagement cmToReturn) {
      super();
      this.expectedUser = expectedUser;
      this.expectedObject = expectedObject;
      this.cmToReturn = cmToReturn;
   }

   @Override
   public ConfigurationManagement getCmService(IBasicArtifact<?> user, Object object) {
      wasGetCMCalled = true;
      Assert.assertEquals(expectedUser, user);
      Assert.assertEquals(expectedObject, object);
      return cmToReturn;
   }

   public boolean wasGetCMCalled() {
      return wasGetCMCalled;
   }
}