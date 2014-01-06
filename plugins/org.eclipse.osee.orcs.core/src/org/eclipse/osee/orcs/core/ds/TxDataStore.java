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
package org.eclipse.osee.orcs.core.ds;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.ITransaction;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public interface TxDataStore {

   Callable<TransactionResult> commitTransaction(OrcsSession session, TransactionData transaction);

   Callable<Integer> purgeTransactions(OrcsSession session, Collection<? extends ITransaction> transactions);

   Callable<String> createUnsubscribeTx(ArtifactReadable userArtifact, ArtifactReadable groupArtifact);

}