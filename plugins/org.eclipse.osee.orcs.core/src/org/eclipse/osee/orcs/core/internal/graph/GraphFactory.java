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
package org.eclipse.osee.orcs.core.internal.graph;

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface GraphFactory {

   GraphData createGraph(IOseeBranch branch, int transactionId) throws OseeCoreException;

   GraphData createGraph(IOseeBranch branch) throws OseeCoreException;

   GraphData createGraphSetToHeadTx(IOseeBranch branch) throws OseeCoreException;

}