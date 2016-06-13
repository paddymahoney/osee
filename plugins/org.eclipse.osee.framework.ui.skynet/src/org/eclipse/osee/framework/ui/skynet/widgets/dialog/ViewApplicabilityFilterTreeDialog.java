/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.widgets.dialog;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.plugin.util.StringLabelProvider;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.util.StringNameSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.XCheckBox;
import org.eclipse.osee.orcs.rest.model.ApplicabilityId;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Allows the selection of active work packages with the option to toggle on inActive.
 *
 * @param allValidViewApplicabilities contains the valid list of active and inactive View Applicabilities
 * @author Donald G. Dunne
 */
public class ViewApplicabilityFilterTreeDialog extends FilteredTreeDialog {
   private ApplicabilityId selection;
   XCheckBox showAll = new XCheckBox("Show All View Applicabilities");

   public ViewApplicabilityFilterTreeDialog(String title, String message) {
      super(title, message, new ArrayTreeContentProvider(), new StringLabelProvider());
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control comp = super.createDialogArea(container);
      try {
         getTreeViewer().getViewer().setSorter(new StringNameSorter());
         getTreeViewer().getViewer().addPostSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
               IStructuredSelection sel = (IStructuredSelection) getTreeViewer().getViewer().getSelection();
               if (sel.isEmpty()) {
                  selection = null;
               } else {
                  selection = (ApplicabilityId) sel.getFirstElement();
               }
               updateStatusLabel();
            }
         });
         GridData gd = new GridData(GridData.FILL_BOTH);
         gd.heightHint = 500;
         getTreeViewer().getViewer().getTree().setLayoutData(gd);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      return comp;
   }

   @Override
   protected Result isComplete() {
      try {
         if (selection == null) {
            return new Result("A View Applicability must be selected.");
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return Result.TrueResult;
   }

   public ApplicabilityId getSelection() {
      return selection;
   }

}
