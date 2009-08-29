/*
 * Created on Aug 12, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.relation.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.relation.RelationType;

/**
 * @author Andrew M. Finkbeiner
 *
 */
class UserDefinedOrder implements RelationOrder{

   @Override
   public void sort(Artifact artifact, RelationType type, RelationSide side, List<Artifact> relatives) throws OseeCoreException {
      if(relatives != null && relatives.size() > 1){
         Attribute<String> attribute = artifact.getSoleAttribute("Relation Order");
         if(attribute != null){
            RelationOrderXmlProcessor relationOrderXmlProcessor= new RelationOrderXmlProcessor(attribute.getValue());
            String relationOrderGuid = relationOrderXmlProcessor.findRelationOrderGuid(type.getTypeName(), side);
            if(relationOrderGuid != null){
               List<String> list = relationOrderXmlProcessor.findOrderList(type.getTypeName(), side, relationOrderGuid);
               if(list != null){
                  orderRelatives(relatives, list);
               } else {
                  throw new OseeCoreException(String.format("Unable to find an order list for UserDefinedOrder artifact[%s] RelationType[%s] RelationSide[%s]", artifact.getGuid(), type.getTypeName(), side.name()));
               }
            }
         }   
      }
   }

   private void orderRelatives(List<Artifact> relatives, List<String> list) {
      Collections.sort(relatives, new UserDefinedOrderComparator(list));
   }

   @Override
   public RelationOrderId getOrderId() {
      return RelationOrderBaseTypes.USER_DEFINED;
   }

   @Override
   public void setOrder(Artifact artifact, RelationType type, RelationSide side, List<Artifact> relatives) throws OseeCoreException {
      if(relatives.size() > 0){
         String value = artifact.getOrInitializeSoleAttributeValue("Relation Order");
         RelationOrderXmlProcessor relationOrderXmlProcessor= new RelationOrderXmlProcessor(value);
         relationOrderXmlProcessor.putOrderList(type.getTypeName(), getOrderId(), side, toGuidList(relatives));
         artifact.setSoleAttributeFromString("Relation Order", relationOrderXmlProcessor.getAsXmlString());
      }
   }

   private List<String> toGuidList(List<Artifact> relatives){
      List<String> guids = new ArrayList<String>(relatives.size());
      for(Artifact art:relatives){
         guids.add(art.getGuid());
      }
      return guids;
   }
}
