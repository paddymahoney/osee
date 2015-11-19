/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.goal;

import java.util.List;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.agile.AgileUtilClient;
import org.eclipse.osee.ats.agile.BacklogXViewerFactory;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.workflow.IAtsGoal;
import org.eclipse.osee.ats.artifact.GoalManager;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public class GoalMemberProvider extends AbstractMemberProvider {

   private final IAtsGoal goal;

   public GoalMemberProvider(IAtsGoal goal) {
      this.goal = goal;
   }

   @Override
   public String getCollectorName() {
      if (isBacklog()) {
         return "Backlog";
      }
      return "Goal";
   }

   @Override
   public String getMembersName() {
      if (isBacklog()) {
         return "Items";
      }
      return "Members";
   }

   @Override
   public KeyedImage getImageKey() {
      if (isBacklog()) {
         return AtsImage.AGILE_BACKLOG;
      }
      return AtsImage.GOAL;
   }

   @Override
   public List<Artifact> getMembers() {
      return getArtifact().getMembers();
   }

   @Override
   public GoalArtifact getArtifact() {
      return ((GoalArtifact) goal.getStoreObject());
   }

   @Override
   public String getGuid() {
      return getArtifact().getGuid();
   }

   @Override
   public void addMember(Artifact artifact) {
      getArtifact().addMember(artifact);
   }

   @Override
   public IXViewerFactory getXViewerFactory(Artifact awa) {
      if (isBacklog()) {
         return new BacklogXViewerFactory((GoalArtifact) awa);
      }
      return new GoalXViewerFactory((GoalArtifact) awa);
   }

   @Override
   public IArtifactType getArtifactType() {
      return AtsArtifactTypes.Goal;
   }

   @Override
   public String getColumnName() {
      if (isBacklog()) {
         return "ats.column.backlogOrder";
      }
      return "ats.column.goalOrder";
   }

   @Override
   public IRelationTypeSide getMemberRelationTypeSide() {
      return AtsRelationTypes.Goal_Member;
   }

   @Override
   public void promptChangeOrder(Artifact goalArt, List<Artifact> selectedAtsArtifacts) {
      new GoalManager().promptChangeMemberOrder((GoalArtifact) goalArt, selectedAtsArtifacts);
   }

   @Override
   public Result isAddValid(List<Artifact> artifacts) {
      return Result.TrueResult;
   }

   private boolean isBacklog() {
      return AgileUtilClient.isBacklog(getArtifact());
   }

   @Override
   public void deCacheArtifact() {
      AtsClientService.get().getGoalMembersCache().decache(getArtifact());
   }

}