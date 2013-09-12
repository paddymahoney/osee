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
package org.eclipse.osee.orcs.core.internal.relation;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.internal.graph.GraphData;

/**
 * @author Andrew M. Finkbeiner
 * @author Roberto E. Escobar
 * @author Megumi Telles
 */
public interface RelationManager {

   int getMaximumRelationAllowed(OrcsSession session, IRelationType type, RelationNode node, RelationSide side) throws OseeCoreException;

   Collection<? extends IRelationType> getValidRelationTypes(OrcsSession session, RelationNode node) throws OseeCoreException;

   ///////////////////////////////////////

   void accept(OrcsSession session, GraphData graph, RelationNode node, RelationVisitor visitor) throws OseeCoreException;

   ///////////////////////////////////////

   boolean hasDirtyRelations(OrcsSession session, GraphData graph, RelationNode node) throws OseeCoreException;

   Collection<? extends IRelationType> getExistingRelationTypes(OrcsSession session, GraphData graph, RelationNode node) throws OseeCoreException;

   int getRelatedCount(OrcsSession session, GraphData graph, IRelationType type, RelationNode node, RelationSide side) throws OseeCoreException;

   int getRelatedCount(OrcsSession session, GraphData graph, IRelationType type, RelationNode node, RelationSide side, DeletionFlag includeDeleted) throws OseeCoreException;

   boolean areRelated(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode) throws OseeCoreException;

   String getRationale(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode) throws OseeCoreException;

   ///////////////////////////////////////

   <T extends RelationNode> T getParent(OrcsSession session, GraphData graph, RelationNode child) throws OseeCoreException;

   <T extends RelationNode> ResultSet<T> getChildren(OrcsSession session, GraphData graph, RelationNode parent) throws OseeCoreException;

   <T extends RelationNode> ResultSet<T> getRelated(OrcsSession session, GraphData graph, IRelationType type, RelationNode node, RelationSide side) throws OseeCoreException;

   ///////////////////////////////////////

   void addChild(OrcsSession session, GraphData graph, RelationNode parent, RelationNode child) throws OseeCoreException;

   void addChildren(OrcsSession session, GraphData graph, RelationNode parent, List<? extends RelationNode> children) throws OseeCoreException;

   void relate(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode) throws OseeCoreException;

   void relate(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode, String rationale) throws OseeCoreException;

   void relate(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode, IRelationSorterId sortType) throws OseeCoreException;

   void relate(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode, String rationale, IRelationSorterId sortType) throws OseeCoreException;

   void setRationale(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode, String rationale) throws OseeCoreException;

   void unrelate(OrcsSession session, GraphData graph, RelationNode aNode, IRelationType type, RelationNode bNode) throws OseeCoreException;

   void unrelateFromAll(OrcsSession session, GraphData graph, RelationNode node) throws OseeCoreException;

   void unrelateFromAll(OrcsSession session, GraphData graph, IRelationType type, RelationNode node, RelationSide side) throws OseeCoreException;

   void cloneRelations(OrcsSession session, RelationNode source, RelationNode destination) throws OseeCoreException;

   ///////////////////////////////////////
}