/*******************************************************************************
 * Copyright (c) 2017 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.workdef;

/**
 * @author Donald G. Dunne
 */
public class WorkDefData {

   private Long id;
   private String name;
   private String dsl;

   public WorkDefData() {
      // For Jax-RS instantiation
   }

   public WorkDefData(Long id, String name, String dsl) {
      this.id = id;
      this.name = name;
      this.dsl = dsl;
   }

   public String getDsl() {
      return dsl;
   }

   public void setDsl(String dsl) {
      this.dsl = dsl;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}