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
package org.eclipse.osee.framework.skynet.core.utility;

import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TxChange;

/**
 * @author Ryan D. Brooks
 */
public final class Address implements Comparable<Address> {
   private final long branchUuid;
   private final int itemId;
   private final Long transactionId;
   private final long gammaId;
   private ModificationType modType;
   private ApplicabilityId appId;
   private final TxChange txCurrent;
   private final boolean isBaseline;
   private TxChange correctedTxCurrent;
   private boolean purge;

   public Address(boolean isBaseline, long branchUuid, int itemId, Long transactionId, long gammaId, ModificationType modType, ApplicabilityId appId, TxChange txCurrent) {
      super();
      this.branchUuid = branchUuid;
      this.itemId = itemId;
      this.transactionId = transactionId;
      this.gammaId = gammaId;
      this.modType = modType;
      this.appId = appId;
      this.txCurrent = txCurrent;
      this.isBaseline = isBaseline;
   }

   public boolean isBaselineTx() {
      return isBaseline;
   }

   public boolean isSimilar(Address other) {
      return other != null && other.itemId == itemId && other.branchUuid == branchUuid;
   }

   public boolean isSameTransaction(Address other) {
      return other != null && transactionId == other.transactionId;
   }

   public boolean hasSameGamma(Address other) {
      return other != null && gammaId == other.gammaId;
   }

   public boolean hasSameModType(Address other) {
      return modType == other.modType;
   }

   public boolean hasSameApplicability(Address other) {
      return appId.equals(other.getBranchId());
   }

   public void ensureCorrectCurrent() {
      TxChange correctCurrent = TxChange.getCurrent(modType);
      if (txCurrent != correctCurrent) {
         correctedTxCurrent = correctCurrent;
      }
   }

   public void ensureNotCurrent() {
      if (txCurrent != TxChange.NOT_CURRENT) {
         correctedTxCurrent = TxChange.NOT_CURRENT;
      }
   }

   public boolean hasIssue() {
      return purge || correctedTxCurrent != null;
   }

   public TxChange getCorrectedTxCurrent() {
      return correctedTxCurrent;
   }

   public void setCorrectedTxCurrent(TxChange correctedTxCurrent) {
      this.correctedTxCurrent = correctedTxCurrent;
   }

   public boolean isPurge() {
      return purge;
   }

   public void setPurge(boolean purge) {
      this.purge = purge;
   }

   public Long getBranchId() {
      return branchUuid;
   }

   public int getItemId() {
      return itemId;
   }

   public Long getTransactionId() {
      return transactionId;
   }

   public long getGammaId() {
      return gammaId;
   }

   public ModificationType getModType() {
      return modType;
   }

   public void setModType(ModificationType modType) {
      this.modType = modType;
   }

   public ApplicabilityId getApplicabilityId() {
      return appId;
   }

   public void setApplicabilityId(ApplicabilityId appId) {
      this.appId = appId;
   }

   public TxChange getTxCurrent() {
      return txCurrent;
   }

   public boolean isBaseline() {
      return isBaseline;
   }

   @Override
   public String toString() {
      return "Address [branchUuid=" + branchUuid + ", gammaId=" + gammaId + ", itemId=" + itemId + ", modType=" + modType + ", transactionId=" + transactionId + ", txCurrent=" + txCurrent + "]";
   }

   @Override
   public int compareTo(Address otherAddress) {
      if (transactionId != otherAddress.transactionId) {
         return (int) (transactionId.longValue() - otherAddress.transactionId.longValue());
      } else {
         return (int) (gammaId - otherAddress.gammaId);
      }
   }
}