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
package org.eclipse.osee.ats.api.cpa;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class DecisionUpdate {

   private List<String> uuids;
   private List<String> assignees;
   private String rationale;
   private String applicability;
   private String duplicatedPcrId;

   public List<String> getAssignees() {
      return assignees;
   }

   public void setAssignees(List<String> assignees) {
      this.assignees = assignees;
   }

   public String getRationale() {
      return rationale;
   }

   public void setRationale(String rationale) {
      this.rationale = rationale;
   }

   @Override
   public String toString() {
      return "DecisionUpdate [ids=" + uuids + ", assignees=" + assignees + ", rationale=" + rationale + ", applicability=" + applicability + "]";
   }

   public List<String> getUuids() {
      return uuids;
   }

   public void setUuids(List<String> uuids) {
      this.uuids = uuids;
   }

   public String getApplicability() {
      return applicability;
   }

   public void setApplicability(String applicability) {
      this.applicability = applicability;
   }

   public String getDuplicatedPcrId() {
      return duplicatedPcrId;
   }

   public void setDuplicatedPcrId(String duplicatedPcrId) {
      this.duplicatedPcrId = duplicatedPcrId;
   }

}
