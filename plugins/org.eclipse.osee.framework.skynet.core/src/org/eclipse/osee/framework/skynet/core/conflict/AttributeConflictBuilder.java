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

package org.eclipse.osee.framework.skynet.core.conflict;

import java.util.Set;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.ConflictStatus;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.relation.order.RelationOrderData;
import org.eclipse.osee.framework.skynet.core.relation.order.RelationOrderMergeUtility;
import org.eclipse.osee.framework.skynet.core.relation.order.RelationOrderParser;

/**
 * @author Theron Virgin
 */
public class AttributeConflictBuilder extends ConflictBuilder {

   private final String sourceValue;
   private final int attrId;
   private final long attrTypeId;

   public AttributeConflictBuilder(int sourceGamma, int destGamma, int artId, TransactionToken toTransactionId, IOseeBranch sourceBranch, IOseeBranch destBranch, String sourceValue, int attrId, long attrTypeId) {
      super(sourceGamma, destGamma, artId, toTransactionId, sourceBranch, destBranch);
      this.sourceValue = sourceValue;
      this.attrId = attrId;
      this.attrTypeId = attrTypeId;
   }

   @Override
   public Conflict getConflict(BranchId mergeBranch, Set<Integer> artIdSet) throws OseeCoreException {
      for (Integer integer : artIdSet) {
         if (integer.intValue() == artId) {
            return null;
         }
      }
      AttributeConflict attributeConflict = new AttributeConflict(sourceGamma, destGamma, artId, toTransactionId, null,
         sourceValue, attrId, attrTypeId, mergeBranch, sourceBranch, destBranch);
      if (attributeConflict.getChangeItem().toString().equals("Word Ole Data")) {
         return null;
      } else if (attributeConflict.getAttributeType().equals(CoreAttributeTypes.RelationOrder)) {
         Artifact left = attributeConflict.getSourceArtifact();
         Artifact right = attributeConflict.getDestArtifact();
         RelationOrderData mergedOrder = RelationOrderMergeUtility.mergeRelationOrder(left, right);
         if (mergedOrder != null) {
            RelationOrderParser parser = new RelationOrderParser();
            String attributeValue = parser.toXml(mergedOrder);
            attributeConflict.setStringAttributeValue(attributeValue);
            attributeConflict.setStatus(ConflictStatus.RESOLVED);
         }
      }
      return attributeConflict;
   }

}
