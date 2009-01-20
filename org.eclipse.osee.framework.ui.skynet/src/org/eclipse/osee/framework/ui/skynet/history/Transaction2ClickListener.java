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
package org.eclipse.osee.framework.ui.skynet.history;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.StaticIdManager;
import org.eclipse.osee.framework.skynet.core.revision.TransactionData;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.preferences.EditorsPreferencePage;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Ryan D. Brooks
 */
public class Transaction2ClickListener implements IDoubleClickListener {
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
    */
   public void doubleClick(DoubleClickEvent event) {
      Object selectedItem = ((IStructuredSelection) event.getSelection()).getFirstElement();
      if (selectedItem instanceof TransactionData) {
         openArtifact((TransactionData) selectedItem);
      } else {
         OSEELog.logSevere(SkynetGuiPlugin.class, "Selected item not of expected type", true);
      }
   }

   private void openArtifact(TransactionData transactionData) {
      try {
         Artifact artifact = transactionData.getArtifact();

         if (StaticIdManager.hasValue(UserManager.getUser(), EditorsPreferencePage.PreviewOnDoubleClickForWordArtifacts)) {
            RendererManager.previewInJob(artifact);
         } else {
            RendererManager.openInJob(artifact, PresentationType.GENERALIZED_EDIT);
         }
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }
}