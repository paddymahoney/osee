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
package org.eclipse.osee.ats.core.internal.state;

import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.notify.AtsNotifyType;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.util.IExecuteListener;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateManager;
import org.eclipse.osee.ats.core.AtsCore;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G Dunne
 */
public class StateManagerStore {

   public void writeToStore(IAtsWorkItem workItem, IAttributeResolver attrResolver, IAtsChangeSet changes) throws OseeCoreException {
      StateManager stateMgr = (StateManager) workItem.getStateMgr();
      StateManagerWriter writer = new StateManagerWriter(workItem, stateMgr, attrResolver, changes);
      List<IAtsUser> assigneesAdded = stateMgr.getAssigneesAdded();
      writer.writeToStore();
      changes.addExecuteListener(getPostPersistExecutionListener(assigneesAdded));
   }

   public static void postPersistNotifyReset(IAtsWorkItem workItem, IAttributeResolver attrResolver, List<IAtsUser> assigneesAdded) throws OseeCoreException {
      AtsCore.getNotifyService().notify(workItem, assigneesAdded, AtsNotifyType.Assigned);
      load(workItem, workItem.getStateMgr(), attrResolver);
   }

   public static IExecuteListener getPostPersistExecutionListener(final List<IAtsUser> assigneesAdded) {
      return new IExecuteListener() {

         @Override
         public void changesStored(IAtsChangeSet changes) {
            for (Object obj : changes.getObjects()) {
               if (obj instanceof IAtsWorkItem) {
                  try {
                     postPersistNotifyReset((IAtsWorkItem) obj, AtsCore.getAttrResolver(), assigneesAdded);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(AtsCore.class, Level.SEVERE, ex);
                  }
               }
            }
         }
      };
   }

   public static void load(IAtsWorkItem workItem, IAtsStateManager stateMgr, IAttributeResolver attrResolver) throws OseeCoreException {
      StateManager stateManager = (StateManager) stateMgr;
      stateManager.clear();
      StateManagerReader reader = new StateManagerReader(workItem, stateManager, attrResolver);
      reader.load();
   }

}