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

package org.eclipse.osee.framework.skynet.core.change;

import java.util.logging.Level;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;

/**
 * @author Jeff C. Phillips
 */
public class ArtifactChanged extends Change {

   public ArtifactChanged(Branch branch, ArtifactType artType, int sourceGamma, int artId, TransactionId toTransactionId, TransactionId fromTransactionId, ModificationType modType, ChangeType changeType, boolean isHistorical) throws OseeDataStoreException, OseeTypeDoesNotExist {
      super(branch, artType, sourceGamma, artId, toTransactionId, fromTransactionId, modType, changeType, isHistorical);
   }

   @Override
   public String getName() throws IllegalArgumentException, ArtifactDoesNotExist, MultipleArtifactsExist {
      return getArtifactName();
   }

   @Override
   public String getItemTypeName() throws OseeCoreException {
      return getArtifactType().getName();
   }

   @Override
   public String getIsValue() {
      return "";
   }

   @SuppressWarnings("unchecked")
   @Override
   public Object getAdapter(Class adapter) {
      if (adapter == null)
         throw new IllegalArgumentException("adapter can not be null");

      try {
         if (adapter.isInstance(getArtifact())) {
            return getArtifact();
         } else if (adapter.isInstance(getToTransactionId()) && isHistorical()) {
            return getToTransactionId();
         } else if (adapter.isInstance(this)) {
            return this;
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   @Override
   public String getItemKind() {
      return "Artifact";
   }

   @Override
   public String getWasValue() {
      return null;
   }

   @Override
   public int getItemTypeId() {
      return getArtifactType().getArtTypeId();
   }

   @Override
   public int getItemId() {
      return getArtId();
   }
}
