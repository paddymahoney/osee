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
import org.eclipse.osee.ats.api.column.IAtsColumn;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link WorkPackageNameColumn}
 *
 * @author Donald G. Dunne
 */
public class WorkPackageNameUtilityTest extends AbstractWorkPackageUtilityTest {

   WorkPackageNameColumn util;

   @Override
   @Before
   public void setup() throws OseeCoreException {
      super.setup();
      util = new WorkPackageNameColumn(earnedValueServiceProvider, null);
   }

   @Test
   public void testGetColumnText() {
      when(workPkg.getName()).thenReturn("");
      Assert.assertEquals("", util.getColumnText(workItem));

      when(workPkg.getName()).thenReturn("WP CONTROL");
      Assert.assertEquals("WP CONTROL", util.getColumnText(workItem));
   }

   @Override
   public IAtsColumn getUtil() {
      return util;
   }

}
