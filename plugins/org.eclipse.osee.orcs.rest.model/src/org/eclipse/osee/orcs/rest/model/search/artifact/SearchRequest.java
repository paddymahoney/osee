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
package org.eclipse.osee.orcs.rest.model.search.artifact;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.osee.framework.core.data.BranchId;

/**
 * @author John R. Misinco
 * @author Roberto E. Escobar
 */
@XmlRootElement(name = "SearchRequest")
public class SearchRequest implements SearchParameters {

   private BranchId branch;
   private RequestType type;
   private int fromTx;
   private boolean includeDeleted;

   @XmlTransient
   private List<Predicate> predicates;

   public SearchRequest() {
      super();
   }

   public SearchRequest(BranchId branch, List<Predicate> predicates, RequestType type, int fromTx, boolean includeDeleted) {
      this.branch = branch;
      this.predicates = predicates;
      this.type = type;
      this.fromTx = fromTx;
      this.includeDeleted = includeDeleted;
   }

   @Override
   public BranchId getBranch() {
      return branch;
   }

   @Override
   @XmlElementWrapper(name = "predicates")
   @XmlElement(name = "predicate")
   public List<Predicate> getPredicates() {
      return predicates;
   }

   @Override
   public RequestType getRequestType() {
      return type;
   }

   public void setBranch(BranchId branch) {
      this.branch = branch;
   }

   public void setPredicates(List<Predicate> predicates) {
      this.predicates = predicates;
   }

   public void setRequestType(RequestType type) {
      this.type = type;
   }

   @Override
   public boolean isIncludeDeleted() {
      return includeDeleted;
   }

   @Override
   public int getFromTx() {
      return fromTx;
   }

   public void setFromTx(int fromTx) {
      this.fromTx = fromTx;
   }

   public void setIncludeDeleted(boolean includeDeleted) {
      this.includeDeleted = includeDeleted;
   }

}
