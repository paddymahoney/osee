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
package org.eclipse.osee.orcs.core.internal.types.impl;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumType;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.data.EnumType;

/**
 * @author Roberto E. Escobar
 */
public class AttributeTypesImpl implements AttributeTypes {

   public static interface AttributeTypeIndexProvider {
      AttributeTypeIndex getAttributeTypeIndex() throws OseeCoreException;
   }

   public static interface EnumTypeIndexProvider {
      EnumTypeIndex getEnumTypeIndex() throws OseeCoreException;
   }

   private static final String ATTRIBUTE_OCCURRENCE_UNLIMITED = "unlimited";
   private static final String BASE_TYPE_NAMESPACE = "org.eclipse.osee.framework.skynet.core.";

   private final AttributeTypeIndexProvider provider;
   private final EnumTypeIndexProvider enumTypeIndexProvider;

   public AttributeTypesImpl(AttributeTypeIndexProvider provider, EnumTypeIndexProvider enumTypeIndexProvider) {
      this.provider = provider;
      this.enumTypeIndexProvider = enumTypeIndexProvider;
   }

   private XAttributeType getType(AttributeTypeId attributeType) throws OseeCoreException {
      return provider.getAttributeTypeIndex().getDslTypeByToken(attributeType);
   }

   @Override
   public Collection<AttributeTypeToken> getAll() throws OseeCoreException {
      return provider.getAttributeTypeIndex().getAllTokens();
   }

   @Override
   public AttributeTypeToken get(Id id) {
      return provider.getAttributeTypeIndex().get(id);
   }

   @Override
   public AttributeTypeToken get(Long id) {
      return provider.getAttributeTypeIndex().get(id);
   }

   private String getQualifiedTypeName(String id) {
      String value = !Strings.isValid(id) ? Strings.emptyString() : id;
      if (!value.contains(".")) {
         value = BASE_TYPE_NAMESPACE + id;
      }
      return value;
   }

   @Override
   public String getBaseAttributeTypeId(AttributeTypeId attributeType) throws OseeCoreException {
      return getQualifiedTypeName(getType(attributeType).getBaseAttributeType());
   }

   @Override
   public String getAttributeProviderId(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      return getQualifiedTypeName(type.getDataProvider());
   }

   @Override
   public String getDefaultValue(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      return type.getDefaultValue();
   }

   @Override
   public int getMaxOccurrences(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String maxValue = type.getMax();
      int max = Integer.MAX_VALUE;
      if (!ATTRIBUTE_OCCURRENCE_UNLIMITED.equals(maxValue)) {
         if (Strings.isValid(maxValue)) {
            max = Integer.parseInt(maxValue);
         }
      }
      return max;
   }

   @Override
   public int getMinOccurrences(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String minValue = type.getMin();
      int min = 0;
      if (Strings.isValid(minValue)) {
         min = Integer.parseInt(minValue);
      }
      return min;
   }

   @Override
   public String getFileTypeExtension(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String value = type.getFileExtension();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public String getTaggerId(AttributeTypeId attributeType) {
      XAttributeType type = getType(attributeType);
      String value = type.getTaggerId();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public boolean isTaggable(AttributeTypeId attributeType) {
      boolean toReturn = false;
      String taggerId = getTaggerId(attributeType);
      if (taggerId != null) {
         toReturn = Strings.isValid(taggerId.trim());
      }
      return toReturn;
   }

   @Override
   public boolean isEnumerated(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      XOseeEnumType enumType = type.getEnumType();
      return enumType != null;
   }

   @Override
   public EnumType getEnumType(AttributeTypeId attrType) throws OseeCoreException {
      EnumType toReturn = null;
      XAttributeType type = getType(attrType);
      XOseeEnumType enumType = type.getEnumType();
      if (enumType != null) {
         toReturn = enumTypeIndexProvider.getEnumTypeIndex().getTokenByDslType(enumType);
      }
      return toReturn;
   }

   @Override
   public String getDescription(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String value = type.getDescription();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public boolean isEmpty() throws OseeCoreException {
      return provider.getAttributeTypeIndex().isEmpty();
   }

   @Override
   public int size() throws OseeCoreException {
      return provider.getAttributeTypeIndex().size();
   }

   @Override
   public Collection<AttributeTypeId> getAllTaggable() throws OseeCoreException {
      return provider.getAttributeTypeIndex().getAllTaggable();
   }

   @Override
   public boolean exists(Id id) throws OseeCoreException {
      return provider.getAttributeTypeIndex().exists(id);
   }

   @Override
   public String getMediaType(AttributeTypeId attrType) throws OseeCoreException {
      XAttributeType type = getType(attrType);
      String value = type.getMediaType();
      return Strings.isValid(value) ? value : Strings.emptyString();
   }

   @Override
   public boolean hasMediaType(AttributeTypeId attrType) throws OseeCoreException {
      boolean toReturn = false;
      String mediaType = getMediaType(attrType);
      if (mediaType != null) {
         toReturn = Strings.isValid(mediaType.trim());
      }
      return toReturn;
   }

   @Override
   public boolean isBooleanType(AttributeTypeId attrType) throws OseeCoreException {
      String baseType = getBaseAttributeTypeId(attrType);
      return baseType != null && baseType.toLowerCase().contains("boolean");
   }

   @Override
   public boolean isIntegerType(AttributeTypeId attrType) {
      String baseType = getBaseAttributeTypeId(attrType);
      return baseType != null && baseType.toLowerCase().contains("integer");
   }

   @Override
   public boolean isDateType(AttributeTypeId attributeType) {
      String baseType = getBaseAttributeTypeId(attributeType);
      return baseType != null && baseType.toLowerCase().contains("date");
   }

   @Override
   public boolean isFloatingType(AttributeTypeId attrType) {
      String baseType = getBaseAttributeTypeId(attrType);
      return baseType != null && baseType.toLowerCase().contains("floatingpoint");
   }

   @Override
   public AttributeTypeId getByName(String attrTypeName) {
      AttributeTypeId attrType = null;
      for (AttributeTypeToken type : getAll()) {
         if (type.getName().equals(attrTypeName)) {
            attrType = type;
            break;
         }
      }
      return attrType;
   }
}
