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
package org.eclipse.osee.orcs.core.ds;

import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;

/**
 * @author Roberto E. Escobar
 */
public interface VersionData extends Cloneable {

   long getGammaId();

   void setGammaId(long gamma);

   TransactionId getTransactionId();

   void setTransactionId(TransactionId txId);

   TransactionId getStripeId();

   void setStripeId(TransactionId stripeId);

   BranchId getBranch();

   void setBranch(BranchId branch);

   boolean isInStorage();

   boolean isHistorical();

   void setHistorical(boolean historical);

   VersionData clone();

}