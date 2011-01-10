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
package org.eclipse.osee.framework.ui.skynet.commandHandlers.branch.commit;

import java.util.Collections;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.branch.BranchSelectionDialog;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.Handlers;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.CheckBoxDialog;

/**
 * @author Jeff C. Phillips
 */
public class CommitIntoHandler extends CommitHandler {

   private static boolean USE_PARENT_BRANCH = false;

   public CommitIntoHandler() {
      super(USE_PARENT_BRANCH);
   }

   @Override
   public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
      IStructuredSelection selection =
         (IStructuredSelection) AWorkbench.getActivePage().getActivePart().getSite().getSelectionProvider().getSelection();
      Branch sourceBranch = Handlers.getBranchesFromStructuredSelection(selection).iterator().next();

      List<Branch> branches =
         BranchManager.getBranches(BranchArchivedState.UNARCHIVED, BranchType.WORKING, BranchType.BASELINE);
      Collections.sort(branches);

      branches.remove(sourceBranch);
      BranchSelectionDialog branchSelection =
         new BranchSelectionDialog(String.format("Source Branch [%s]\n\nSelect Destination Branch", sourceBranch),
            branches);
      int result = branchSelection.open();
      if (result == Window.OK) {
         CheckBoxDialog dialog =
            new CheckBoxDialog("Commit Into", String.format(
               "Commit from\n\nSource Branch: [%s]\n\ninto\n\nDestination Branch: [%s]", sourceBranch,
               branchSelection.getSelected()), "Archive Source Branch");
         if (dialog.open() == 0) {
            Jobs.startJob(new CommitJob(sourceBranch, branchSelection.getSelected(), dialog.isChecked()));
         }
      }

      return null;
   }

   @Override
   public boolean isEnabledWithException(IStructuredSelection structuredSelection) throws OseeCoreException {
      List<Branch> branches = Handlers.getBranchesFromStructuredSelection(structuredSelection);
      return branches.size() == 1 && AccessControlManager.isOseeAdmin();
   }

}
