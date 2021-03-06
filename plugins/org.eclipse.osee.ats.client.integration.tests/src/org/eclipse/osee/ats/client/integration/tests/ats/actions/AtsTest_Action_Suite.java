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
package org.eclipse.osee.ats.client.integration.tests.ats.actions;

import org.eclipse.osee.ats.client.integration.tests.util.DemoTestUtil;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   AccessControlActionTest.class,
   AddNoteActionTest.class,
   CopyActionDetailsActionTest.class,
   DeletePurgeAtsArtifactsActionTest.class,
   DirtyReportActionTest1.class,
   DirtyReportActionTest2.class,
   DuplicateWorkflowActionTest.class,
   DuplicateWorkflowViaWorldEditorActionTest.class,
   FavoriteActionTest.class,
   ImportTasksViaSimpleListTest.class,
   ImportTasksViaSpreadsheetTest.class,
   MyFavoritesActionTest.class,
   MyWorldActionTest.class,
   NewGoalTest.class,
   OpenChangeReportByIdActionTest.class,
   OpenInArtifactEditorActionTest.class,
   OpenInAtsWorkflowEditorActionTest.class,
   OpenInAtsWorldActionTest1.class,
   OpenInAtsWorldActionTest2.class,
   OpenInAtsWorldActionTest3.class,
   OpenInSkyWalkerActionTest.class,
   OpenNewAtsTaskEditorActionTest.class,
   OpenNewAtsTaskEditorSelectedTest.class,
   OpenNewAtsWorldEditorActionTest.class,
   OpenNewAtsWorldEditorSelectedActionTest.class,
   OpenParentActionTest.class,
   OpenWorkflowByIdActionTest.class,
   OpenWorldByIdActionTest.class,
   RefreshDirtyActionTest.class,
   ReloadActionTest.class,
   ResourceHistoryActionTest.class,
   ShowBranchChangeDataActionTest.class,
   ShowChangeReportActionTest.class,
   ShowMergeManagerActionTest.class,
   ShowWorkDefinitionActionTest.class,
   SubscribedActionTest.class,
   TaskAddActionTest.class,})
/**
 * @author Donald G. Dunne
 */
public class AtsTest_Action_Suite {
   @BeforeClass
   public static void setUp() throws Exception {
      OseeProperties.setIsInTest(true);
      System.out.println("\n\nBegin " + AtsTest_Action_Suite.class.getSimpleName());
      DemoTestUtil.setUpTest();
   }

   @AfterClass
   public static void tearDown() throws Exception {
      System.out.println("End " + AtsTest_Action_Suite.class.getSimpleName());
   }
}
