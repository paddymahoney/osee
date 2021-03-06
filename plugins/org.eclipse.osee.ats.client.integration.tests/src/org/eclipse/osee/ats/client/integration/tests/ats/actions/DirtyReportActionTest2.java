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
package org.eclipse.osee.ats.client.integration.tests.ats.actions;

import org.eclipse.osee.ats.actions.DirtyReportAction;
import org.eclipse.osee.ats.actions.IDirtyReportable;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class DirtyReportActionTest2 extends AbstractAtsActionTest {

   @Test(expected = OseeStateException.class)
   public void test() throws Exception {
      AtsTestUtil.cleanupAndReset(getClass().getSimpleName());
      DirtyReportAction action = createAction();
      action.runWithException();
   }

   @Override
   public DirtyReportAction createAction() {
      return new DirtyReportAction(new IDirtyReportable() {
         @Override
         public Result isDirtyResult() {
            return Result.TrueResult;
         }
      });
   }

}
