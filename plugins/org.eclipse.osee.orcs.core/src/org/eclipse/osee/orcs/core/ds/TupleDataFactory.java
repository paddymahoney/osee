/*******************************************************************************
 * Copyright (c) 2016 Boeing.
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

/**
 * @author Angel Avila
 */
public interface TupleDataFactory {

   TupleData createTuple2Data(Long tupleTypeId, BranchId branch, Long e1, Long e2);

   TupleData createTuple3Data(Long tupleTypeId, BranchId branch, Long e1, Long e2, Long e3);

   TupleData createTuple4Data(Long tupleTypeId, BranchId branch, Long e1, Long e2, Long e3, Long e4);

}
