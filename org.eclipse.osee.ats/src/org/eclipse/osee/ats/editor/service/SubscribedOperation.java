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
package org.eclipse.osee.ats.editor.service;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.ISubscribableArtifact;
import org.eclipse.osee.ats.editor.SMAManager;
import org.eclipse.osee.ats.util.Subscribe;
import org.eclipse.osee.ats.workflow.AtsWorkPage;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class SubscribedOperation extends WorkPageService {

   private Action action;
   private static ImageDescriptor subscribeEmailDescriptor;

   public SubscribedOperation(SMAManager smaMgr) {
      super(smaMgr);
   }

   @Override
   public Action createToolbarService() {
      action = new Action(getName(), Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            (new Subscribe(smaMgr.getSma())).toggleSubscribe();
         }
      };
      action.setToolTipText(getName());
      if (subscribeEmailDescriptor == null) {
         subscribeEmailDescriptor = AtsPlugin.getInstance().getImageDescriptor("subscribedEmail.gif");
      }
      action.setImageDescriptor(subscribeEmailDescriptor);
      return action;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.service.WorkPageService#isShowSidebarService(org.eclipse.osee.ats.workflow.AtsWorkPage)
    */
   @Override
   public boolean isShowSidebarService(AtsWorkPage page) throws OseeCoreException {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.ats.editor.operation.WorkPageService#refresh()
    */
   @Override
   public void refresh() {
      if (action != null) action.setToolTipText(((ISubscribableArtifact) smaMgr.getSma()).amISubscribed() ? "Remove Subscribed" : "Add Subscribed");
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.editor.service.WorkPageService#getName()
    */
   @Override
   public String getName() {
      return "Subscribe to Email Notifications";
   }
}
