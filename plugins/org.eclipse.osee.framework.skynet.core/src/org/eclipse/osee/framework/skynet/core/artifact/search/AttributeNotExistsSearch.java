/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.artifact.search;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.jdk.core.util.Conditions;

/**
 * @author John Misinco
 */
public class AttributeNotExistsSearch implements ISearchPrimitive {
   private final List<IAttributeType> attributeTypes;

   public AttributeNotExistsSearch(List<IAttributeType> attributeTypes) {
      Conditions.checkNotNull(attributeTypes, "attributeTypes");
      this.attributeTypes = attributeTypes;
   }

   @Override
   public String toString() {
      return "Attribute Not Exists type: \"" + attributeTypes + "\"";
   }

   @Override
   public String getStorageString() {
      StringBuilder storageString = new StringBuilder();

      for (IAttributeType attrType : attributeTypes) {
         storageString.append(attrType.getGuid().toString());
         storageString.append(",");
      }
      storageString.deleteCharAt(storageString.length() - 1);
      return storageString.toString();
   }

   public static AttributeNotExistsSearch getPrimitive(String storageString) {
      ArrayList<IAttributeType> attributeTypes = new ArrayList<>();

      for (String attributeTypeId : storageString.split(",")) {
         attributeTypes.add(TokenFactory.createAttributeType(Long.valueOf(attributeTypeId), "SearchAttrType"));
      }
      return new AttributeNotExistsSearch(attributeTypes);
   }

   @Override
   public void addToQuery(QueryBuilderArtifact builder) {
      builder.andNotExists(attributeTypes);
   }

}