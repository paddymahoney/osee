/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.loader.criteria;

import org.eclipse.osee.orcs.core.ds.Criteria;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaArtifact extends Criteria {

   private int id;

   public CriteriaArtifact() {
      super();
      this.id = -1;
   }

   public int getQueryId() {
      return id;
   }

   protected void setQueryId(int id) {
      this.id = id;
   }

   @Override
   public String toString() {
      return "CriteriaArtifactQid [queryId=" + id + "]";
   }
}
