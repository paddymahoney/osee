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
package org.eclipse.osee.framework.ui.skynet.change;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class OpenChangeReportByTransactionIdAction extends Action {
   private static final String NAME = "Open Change Report by Transaction Id";

   public OpenChangeReportByTransactionIdAction() {
      super(NAME, IAction.AS_PUSH_BUTTON);
      setId("open.by.transaction.id");
      setToolTipText(NAME);
      setImageDescriptor(ImageManager.getImageDescriptor(FrameworkImage.OPEN));
   }

   @Override
   public void run() {
      EntryDialog dialog = new EntryDialog(NAME, "Enter Transaction Id");
      if (dialog.open() == 0) {
         String entry = dialog.getEntry();
         if (Strings.isNumeric(entry)) {
            ChangeUiUtil.open(TransactionManager.getTransaction(Long.valueOf(entry)));
         } else {
            AWorkbench.popup("Entry must be numeric.");
         }
      }
   }
}