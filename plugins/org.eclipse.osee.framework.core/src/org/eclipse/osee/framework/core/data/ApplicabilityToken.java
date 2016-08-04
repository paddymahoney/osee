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
package org.eclipse.osee.framework.core.data;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.osee.framework.jdk.core.type.NamedId;
import org.eclipse.osee.framework.jdk.core.type.NamedIdSerializer;

/**
 * @author Angel Avila
 */
@JsonSerialize(using = NamedIdSerializer.class)
public class ApplicabilityToken extends NamedId implements ApplicabilityId {
   public static final ApplicabilityToken BASE = new ApplicabilityToken(ApplicabilityId.BASE.getId(), "Base");

   public ApplicabilityToken(long applId, String name) {

      super(applId, name);
   }

   public ApplicabilityToken(Long applId, String name) {
      super(applId, name);
   }

   @JsonCreator
   public static ApplicabilityToken c(@JsonProperty("id") long id, @JsonProperty("name") String name) {
      return new ApplicabilityToken(id, name);
   }
}