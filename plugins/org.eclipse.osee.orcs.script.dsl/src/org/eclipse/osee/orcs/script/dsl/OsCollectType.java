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
package org.eclipse.osee.orcs.script.dsl;

/**
 * @author Roberto E. Escobar
 */
public enum OsCollectType {
   BRANCHES("branches"),
   TXS("txs"),
   ARTIFACTS("artifacts"),
   ATTRIBUTES("attributes"),
   RELATIONS("relations");

   private String literal;

   private OsCollectType(String literal) {
      this.literal = literal;
   }

   public String getLiteral() {
      return literal;
   }

   public static OsCollectType fromString(String value) {
      OsCollectType toReturn = null;
      for (OsCollectType type : OsCollectType.values()) {
         if (type.getLiteral().equals(value)) {
            toReturn = type;
            break;
         }
      }
      return toReturn;
   }
}