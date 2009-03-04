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
package org.eclipse.osee.framework.core.enums;

/**
 * @author Ryan D. Brooks
 */
public enum BranchType {
   WORKING(0), TOP_LEVEL(1), BASELINE(2), MERGE(3), SYSTEM_ROOT(4);
   private final int value;

   BranchType(int value) {
      this.value = value;
   }

   public final int getValue() {
      return value;
   }

   public static BranchType getBranchType(int value) {
      for (BranchType type : values())
         if (type.getValue() == value) return type;
      return null;
   }
}
