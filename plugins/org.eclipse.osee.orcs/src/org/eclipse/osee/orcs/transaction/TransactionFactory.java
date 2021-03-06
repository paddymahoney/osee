/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.transaction;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.model.change.CompareResults;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.data.TransactionReadable;

/**
 * @author Roberto E. Escobar
 */
public interface TransactionFactory {

   TransactionBuilder createTransaction(BranchId branch, ArtifactId userArtifact, String comment) throws OseeCoreException;

   Callable<Integer> purgeTransaction(Collection<? extends TransactionId> transactions);

   int[] purgeUnusedBackingDataAndTransactions();

   Callable<Void> setTransactionComment(TransactionId transaction, String comment);

   CompareResults compareTxs(TransactionId txId1, TransactionId txId2);

   ResultSet<TransactionReadable> getAllTxs();

   TransactionReadable getTx(TransactionId txId);

   boolean setTxComment(TransactionId txId, String comment);

   boolean replaceWithBaselineTxVersion(String userId, BranchId branchId, TransactionId txId, int artId, String comment);

   boolean purgeTxs(String txIds);
}