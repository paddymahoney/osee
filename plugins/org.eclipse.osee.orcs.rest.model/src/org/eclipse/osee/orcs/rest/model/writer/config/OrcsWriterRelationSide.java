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
package org.eclipse.osee.orcs.rest.model.writer.config;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class OrcsWriterRelationSide {

   private Long relationTypeId;
   private boolean sideA;

   public Long getRelationTypeId() {
      return relationTypeId;
   }

   public void setRelationTypeId(Long relationTypeId) {
      this.relationTypeId = relationTypeId;
   }

   public boolean isSideA() {
      return sideA;
   }

   public void setSideA(boolean sideA) {
      this.sideA = sideA;
   }

}
