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
package org.eclipse.osee.framework.jdk.core.type;

/**
 * This class implements a boolean that can be passed around and modified through a group of methods.
 * 
 * @author Chris Austin
 */
public class MutableBoolean {
   private boolean value;

   public MutableBoolean(boolean value) {
      this.value = value;
   }

   public boolean getValue() {
      return value;
   }

   public void setValue(boolean value) {
      this.value = value;
   }

   @Override
   public String toString() {
      return Boolean.toString(value);
   }
}
