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
package org.eclipse.osee.orcs.search;

import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.orcs.data.BranchReadable;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 */
public interface BranchQuery extends BranchQueryBuilder<BranchQuery>, Query {

   ResultSet<BranchReadable> getResults() throws OseeCoreException;

   ResultSet<IOseeBranch> getResultsAsId() throws OseeCoreException;

   @Override
   int getCount() throws OseeCoreException;

   @Override
   CancellableCallable<Integer> createCount() throws OseeCoreException;

   CancellableCallable<ResultSet<BranchReadable>> createSearch() throws OseeCoreException;

   CancellableCallable<ResultSet<IOseeBranch>> createSearchResultsAsIds() throws OseeCoreException;

}
