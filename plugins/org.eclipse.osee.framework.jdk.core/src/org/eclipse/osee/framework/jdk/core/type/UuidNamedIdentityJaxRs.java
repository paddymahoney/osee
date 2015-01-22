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
package org.eclipse.osee.framework.jdk.core.type;

/**
 * UuidNamedIdentity with zero parameter constructor needed by Jax-Rs serialization
 * 
 * @author Donald G. Dunne
 */
public class UuidNamedIdentityJaxRs<T> extends UuidBaseIdentityJaxRs<T> implements UuidIdentifiable<T>, Comparable<Named> {
   private String name;

   public UuidNamedIdentityJaxRs() {
      this(null, null);
   }

   public UuidNamedIdentityJaxRs(T uid, String name) {
      super(uid);
      this.name = name;
   }

   @Override
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public int compareTo(Named other) {
      if (other != null && other.getName() != null && getName() != null) {
         return getName().compareTo(other.getName());
      }
      return -1;
   }

   @Override
   public String toString() {
      return getName();
   }
}