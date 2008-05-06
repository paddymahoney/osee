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
package org.eclipse.osee.framework.skynet.core.attribute;

import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeValueSearch;
import org.eclipse.osee.framework.skynet.core.attribute.providers.ICharacterAttributeDataProvider;

/**
 * @author Robert A. Fisher
 */
public class AttributeValueSearchAttribute extends StringAttribute implements ISearchAttribute<AttributeValueSearch> {

   public AttributeValueSearchAttribute(DynamicAttributeDescriptor attributeType, ICharacterAttributeDataProvider dataProvider) {
      super(attributeType, dataProvider);
   }

   public void setSearchPrimitive(AttributeValueSearch value) {
      setValue(value.getStorageString());
   }

   public AttributeValueSearch getSearchPrimitive() {
      return AttributeValueSearch.getPrimitive(getValue());
   }
}
