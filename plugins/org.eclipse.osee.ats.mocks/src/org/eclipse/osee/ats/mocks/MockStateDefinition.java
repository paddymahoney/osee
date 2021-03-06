/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.mocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsPeerReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.StateColor;
import org.eclipse.osee.ats.api.workdef.StateType;

/**
 * @author Donald G. Dunne
 */
public class MockStateDefinition implements IAtsStateDefinition {

   private StateType StateType;
   private int ordinal = 0;
   private final List<IAtsLayoutItem> stateItems = new ArrayList<>(5);
   private final List<IAtsStateDefinition> toStates = new ArrayList<>(5);
   private IAtsStateDefinition defaultToState;
   private final List<IAtsStateDefinition> overrideAttributeValidationStates = new ArrayList<>(5);
   private final List<IAtsDecisionReviewDefinition> decisionReviews = new ArrayList<>();
   private final List<IAtsPeerReviewDefinition> peerReviews = new ArrayList<>();
   private IAtsWorkDefinition workDefinition;
   private int stateWeight = 0;
   private Integer recommendedPercentComplete = null;
   private StateColor color = null;
   private String name;
   private String description;

   public MockStateDefinition(String name) {
      this.name = name;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public List<IAtsLayoutItem> getLayoutItems() {
      return stateItems;
   }

   @Override
   public StateType getStateType() {
      return StateType;
   }

   public void setStateType(StateType StateType) {
      this.StateType = StateType;
   }

   @Override
   public int getOrdinal() {
      return ordinal;
   }

   public void setOrdinal(int ordinal) {
      this.ordinal = ordinal;
   }

   @Override
   public List<IAtsStateDefinition> getToStates() {
      return toStates;
   }

   @Override
   public IAtsWorkDefinition getWorkDefinition() {
      return workDefinition;
   }

   @Override
   public void setWorkDefinition(IAtsWorkDefinition workDefinition) {
      this.workDefinition = workDefinition;
   }

   @Override
   public String toString() {
      return String.format("[%s][%s]", getName(), getStateType());
   }

   /**
    * Returns fully qualified name of <work definition name>.<this state name
    */

   @Override
   public String getFullName() {
      if (workDefinition != null) {
         return workDefinition.getName() + "." + getName();
      }
      return getName();
   }

   @Override
   public List<IAtsStateDefinition> getOverrideAttributeValidationStates() {
      return overrideAttributeValidationStates;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (getFullName() == null ? 0 : getFullName().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      IAtsStateDefinition other = (IAtsStateDefinition) obj;
      if (getFullName() == null) {
         if (other.getFullName() != null) {
            return false;
         } else {
            return false;
         }
      } else if (!getFullName().equals(other.getFullName())) {
         return false;
      }
      return true;
   }

   @Override
   public IAtsStateDefinition getDefaultToState() {
      return defaultToState;
   }

   public void setDefaultToState(IAtsStateDefinition defaultToState) {
      this.defaultToState = defaultToState;
   }

   @Override
   public List<IAtsDecisionReviewDefinition> getDecisionReviews() {
      return decisionReviews;
   }

   @Override
   public List<IAtsPeerReviewDefinition> getPeerReviews() {
      return peerReviews;
   }

   @Override
   public int getStateWeight() {
      return stateWeight;
   }

   /**
    * Set how much (of 100%) this state's percent complete will contribute to the full percent complete of work
    * definitions.
    *
    * @param percentWeight int value where all stateWeights in workdefinition == 100
    */

   public void setStateWeight(int percentWeight) {
      this.stateWeight = percentWeight;
   }

   public void setRecommendedPercentComplete(int recommendedPercentComplete) {
      this.recommendedPercentComplete = recommendedPercentComplete;
   }

   @Override
   public Integer getRecommendedPercentComplete() {
      return recommendedPercentComplete;
   }

   public void setColor(StateColor stateColor) {
      this.color = stateColor;
   }

   @Override
   public StateColor getColor() {
      return color;
   }

   @Override
   public List<String> getRules() {
      return Collections.emptyList();
   }

   @Override
   public boolean hasRule(String rule) {
      return false;
   }

   @Override
   public String getDescription() {
      return description;
   }

   public void setName(String name) {
      this.name = name;
   }

}
