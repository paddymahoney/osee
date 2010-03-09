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
package org.eclipse.osee.ats.artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.artifact.ATSLog.LogType;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.model.ArtifactType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;

/**
 * @author Donald G. Dunne
 */
public class GoalArtifact extends StateMachineArtifact {

   public static enum GoalState {
      InWork, Completed, Cancelled
   };

   public GoalArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, ArtifactType artifactType) throws OseeDataStoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
      registerAtsWorldRelation(AtsRelationTypes.Goal_Member);
   }

   @Override
   public ActionArtifact getParentActionArtifact() throws OseeCoreException {
      return null;
   }

   @Override
   public StateMachineArtifact getParentSMA() throws OseeCoreException {
      List<Artifact> parents = getRelatedArtifacts(AtsRelationTypes.Goal_Goal);
      if (parents.size() == 0) {
         return null;
      }
      if (parents.size() == 1) {
         return (StateMachineArtifact) parents.iterator().next();
      }
      System.err.println("Two parent goals, what do here?");
      return (StateMachineArtifact) parents.iterator().next();
   }

   @Override
   public TeamWorkFlowArtifact getParentTeamWorkflow() throws OseeCoreException {
      return null;
   }

   @Override
   public Set<User> getPrivilegedUsers() throws OseeCoreException {
      return null;
   }

   @Override
   public boolean isTaskable() throws OseeCoreException {
      return false;
   }

   @Override
   public Date getWorldViewReleaseDate() throws OseeCoreException {
      return null;
   }

   @Override
   public VersionArtifact getWorldViewTargetedVersion() throws OseeCoreException {
      return null;
   }

   @Override
   public String getWorldViewParentID() throws OseeCoreException {
      return null;
   }

   @Override
   public String getHyperTargetVersion() {
      return null;
   }

   public static void getGoals(Artifact artifact, Set<Artifact> goals, boolean recurse) throws OseeCoreException {
      getGoals(Arrays.asList(artifact), goals, recurse);
   }

   public static void getGoals(Collection<Artifact> artifacts, Set<Artifact> goals, boolean recurse) throws OseeCoreException {
      for (Artifact art : artifacts) {
         if (art instanceof GoalArtifact) {
            goals.add(art);
         }
         goals.addAll(art.getRelatedArtifacts(AtsRelationTypes.Goal_Goal, GoalArtifact.class));
         if (recurse && art instanceof StateMachineArtifact && ((StateMachineArtifact) art).getParentSMA() != null) {
            getGoals(((StateMachineArtifact) art).getParentSMA(), goals, recurse);
         }
      }
   }

   public static GoalArtifact createGoal(String title) throws OseeCoreException {
      GoalArtifact goalArt =
            (GoalArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.Goal, AtsUtil.getAtsBranch());
      goalArt.setName(title);
      goalArt.getLog().addLog(LogType.Originated, "", "");

      // Initialize state machine
      goalArt.getStateMgr().initializeStateMachine(GoalState.InWork.name(),
            Collections.singleton(UserManager.getUser()));
      goalArt.getLog().addLog(LogType.StateEntered, GoalState.InWork.name(), "");
      return goalArt;
   }

   public List<Artifact> getMembers() throws OseeCoreException {
      return getRelatedArtifacts(AtsRelationTypes.Goal_Member, false);
   }

   public void addMember(Artifact artifact) throws OseeCoreException {
      if (!getMembers().contains(artifact)) {
         addRelation(AtsRelationTypes.Goal_Member, artifact);
      }
   }

}
