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

package org.eclipse.osee.ats.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.client.util.CopyActionDetails;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * @author Donald G. Dunne
 */
public class CopyActionDetailsAction extends Action {

   private final AbstractWorkflowArtifact awa;

   public CopyActionDetailsAction(AbstractWorkflowArtifact awa) {
      super();
      this.awa = awa;
      setText("Copy " + awa.getArtifactTypeName() + " details to clipboard");
      setToolTipText(getText());
   }

   private void performCopy() {
      Clipboard clipboard = new Clipboard(null);
      try {
         String detailsStr = new CopyActionDetails(awa).getDetailsString();
         clipboard.setContents(new Object[] {detailsStr}, new Transfer[] {TextTransfer.getInstance()});
      } finally {
         clipboard.dispose();
      }
   }

   @Override
   public void run() {
      performCopy();
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(AtsImage.COPY_TO_CLIPBOARD);
   }

}
