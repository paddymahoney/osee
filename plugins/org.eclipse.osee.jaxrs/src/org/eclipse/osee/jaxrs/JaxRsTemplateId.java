/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.jaxrs;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Roberto E. Escobar
 */
@XmlRootElement
public class JaxRsTemplateId implements Comparable<JaxRsTemplateId> {

   private Long uuid;
   private String name;

   public Long getUuid() {
      return uuid;
   }

   public String getName() {
      return name;
   }

   public void setUuid(Long uuid) {
      this.uuid = uuid;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public int compareTo(JaxRsTemplateId other) {
      int result = 0;
      if (name != null) {
         result = name.compareTo(other.name);
      } else if (other != null && other.name != null) {
         result = 1;
      }
      return result;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (name == null ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      JaxRsTemplateId other = (JaxRsTemplateId) obj;
      if (name == null) {
         if (other.name != null) {
            return false;
         }
      } else if (!name.equals(other.name)) {
         return false;
      }
      return true;
   }
}