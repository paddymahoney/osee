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
package org.eclipse.osee.framework.lifecycle.internal;

/**
 * @author Roberto E. Escobar
 * @author Jeff C. Phillips
 */
public enum OperationPointId {
   NOOP_ID,
   PRE_CONDITION_ID,
   POST_CONDITION_ID,
   CHECK_CONDITION_ID;

   public static OperationPointId toEnum(String sourceId) {
      OperationPointId toReturn = OperationPointId.NOOP_ID;
      for (OperationPointId pointId : OperationPointId.values()) {
         if (pointId.name().equalsIgnoreCase(sourceId)) {
            toReturn = pointId;
            break;
         }
      }
      return toReturn;
   }
}