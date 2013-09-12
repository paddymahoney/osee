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

import org.eclipse.osee.framework.core.data.Identifiable;
import org.eclipse.osee.orcs.data.HasBranch;
import org.eclipse.osee.orcs.data.HasLocalId;

/**
 * @author Roberto E. Escobar
 * @author Megumi Telles
 */
public interface GraphNode extends Identifiable, HasLocalId, HasBranch {

   void setGraph(GraphData graph);

   GraphData getGraph();

   String getExceptionString();
}