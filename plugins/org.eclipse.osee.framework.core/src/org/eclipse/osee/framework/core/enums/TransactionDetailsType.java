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
 * @author Jeff C. Phillips
 */
public enum TransactionDetailsType {
   INVALID(-1),
   NonBaselined(0),
   Baselined(1),
   reverted(2);

   private int id;

   private TransactionDetailsType(int id) {
      this.id = id;
   }

   public int getId() {
      return id;
   }

   public boolean isBaseline() {
      return this == TransactionDetailsType.Baselined;
   }

   public static TransactionDetailsType toEnum(int value) {
      for (TransactionDetailsType txType : values()) {
         if (txType.getId() == value) {
            return txType;
         }
      }
      return null;
   }

}