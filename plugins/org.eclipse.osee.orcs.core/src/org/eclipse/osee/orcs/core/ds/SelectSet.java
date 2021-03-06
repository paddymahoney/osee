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
package org.eclipse.osee.orcs.core.ds;

/**
 * @author Roberto E. Escobar
 */
public class SelectSet implements Cloneable {

   private long limit = -1;
   private DynamicData data;

   public long getLimit() {
      return limit;
   }

   public void setLimit(long limit) {
      this.limit = limit;
   }

   public DynamicData getData() {
      return data;
   }

   public void reset() {
      limit = -1;
      data = null;
   }

   public void setData(DynamicData data) {
      this.data = data;
   }

   @Override
   public SelectSet clone() {
      SelectSet clone = new SelectSet();
      clone.limit = limit;
      clone.data = data;
      return clone;
   }

}