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
package org.eclipse.osee.ats.core.internal.column.ev;

import static org.mockito.Mockito.when;
import junit.framework.Assert;
import org.eclipse.osee.ats.core.column.IAtsColumnUtility;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link WorkPackageProgramUtility}
 * 
 * @author Donald G. Dunne
 */
public class WorkPackageProgramUtilityTest extends AbstractWorkPackageUtilityTest {

   WorkPackageProgramUtility util;

   @Override
   @Before
   public void setup() throws OseeCoreException {
      super.setup();
      util = new WorkPackageProgramUtility(earnedValueServiceProvider);
   }

   @Test
   public void testGetColumnText() throws OseeCoreException {
      when(workPkg.getWorkPackageProgram()).thenReturn("");
      Assert.assertEquals("", util.getColumnText(workItem));

      when(workPkg.getWorkPackageProgram()).thenReturn("USG");
      Assert.assertEquals("USG", util.getColumnText(workItem));
   }

   @Override
   public IAtsColumnUtility getUtil() {
      return util;
   }

}