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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.widgets.xHistory.HistoryView;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class ResourceHistoryAction extends AbstractAtsAction {

   private final AbstractWorkflowArtifact sma;

   public ResourceHistoryAction(AbstractWorkflowArtifact sma) {
      super("Resource History");
      this.sma = sma;
      setToolTipText(getText());
   }

   @Override
   public void runWithException() throws OseeCoreException {
      HistoryView.open(sma);
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.EDIT_BLUE);
   }

}
