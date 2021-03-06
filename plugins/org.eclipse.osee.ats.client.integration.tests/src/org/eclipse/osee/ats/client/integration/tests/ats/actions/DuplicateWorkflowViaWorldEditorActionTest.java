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

import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.actions.DuplicateWorkflowViaWorldEditorAction;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.actions.SelectedAtsArtifactsAdapter;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class DuplicateWorkflowViaWorldEditorActionTest extends AbstractAtsActionRunTest {

   @Override
   public Action createAction() {
      return new DuplicateWorkflowViaWorldEditorAction(AtsTestUtil.getSelectedAtsArtifactsForTeamWf());
   }

   @Test(expected = OseeArgumentException.class)
   public void testException() throws OseeCoreException {
      DuplicateWorkflowViaWorldEditorAction action =
         new DuplicateWorkflowViaWorldEditorAction(new SelectedAtsArtifactsAdapter());
      action.runWithException();
   }
}
