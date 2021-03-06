/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.widgets.xmerge;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;

/**
 * @author Karol M. Wilk
 */
public final class ConflictHandlingOperation extends AbstractOperation {

   public static enum ConflictOperationEnum {
      RESET,
      SET_DST_AND_RESOLVE,
      SET_SRC_AND_RESOLVE,
      MARK_RESOLVED,
      MARK_UNRESOLVED
   }

   private final Collection<Conflict> conflicts;
   private final ConflictOperationEnum operation;

   public ConflictHandlingOperation(ConflictOperationEnum operation, Collection<Conflict> conflicts) {
      super("Updating conflicts", Activator.PLUGIN_ID);
      this.conflicts = conflicts;
      this.operation = operation;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws OseeCoreException {
      Conditions.checkNotNullOrEmpty(conflicts, "conflicts");
      for (Conflict conflict : conflicts) {
         ConflictStatus status = conflict.getStatus();
         if (status.isResolvable()) {
            switch (operation) {
               case RESET:
                  if (status.isResolved() || status.isEdited()) {
                     // Status must be set before clearing the conflict
                     conflict.setStatus(ConflictStatus.EDITED);
                     MergeUtility.clearValue(conflict, null, false);
                  }
                  break;
               case SET_DST_AND_RESOLVE:
                  if (!status.isResolved()) {
                     MergeUtility.setToDest(conflict, null, false);
                     conflict.setStatus(ConflictStatus.RESOLVED);
                  }
                  break;
               case SET_SRC_AND_RESOLVE:
                  if (!status.isResolved()) {
                     MergeUtility.setToSource(conflict, null, false);
                     conflict.setStatus(ConflictStatus.RESOLVED);
                  }
                  break;
               case MARK_RESOLVED:
                  if (!status.isUntouched()) {
                     conflict.setStatus(ConflictStatus.RESOLVED);
                  }
                  break;
               case MARK_UNRESOLVED:
                  if (!status.isUntouched()) {
                     conflict.setStatus(ConflictStatus.EDITED);
                  }
                  break;
               default:
                  throw new OseeArgumentException("Invalid operation [%s]", operation);
            }
         }
      }
   }
}