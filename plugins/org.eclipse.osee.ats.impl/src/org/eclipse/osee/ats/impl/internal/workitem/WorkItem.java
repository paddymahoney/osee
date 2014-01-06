/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.impl.internal.workitem;

import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.review.IAtsAbstractReview;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IWorkDefinitionMatch;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkData;
import org.eclipse.osee.ats.api.workflow.log.IAtsLog;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateManager;
import org.eclipse.osee.ats.core.model.impl.AtsObject;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Donald G Dunne
 */
public class WorkItem extends AtsObject implements IAtsWorkItem {

   protected final ArtifactReadable artifact;
   public static final int TRANSACTION_SENTINEL = -1;
   private IAtsStateManager stateMgr;
   private IAtsLog atsLog;
   private IAtsWorkData workData;
   private IWorkDefinitionMatch match;
   private final IAtsServer atsServer;
   private final Log logger;

   public WorkItem(Log logger, IAtsServer atsServer, ArtifactReadable artifact) {
      super(artifact.getName(), artifact.getGuid());
      this.logger = logger;
      this.atsServer = atsServer;
      this.artifact = artifact;
   }

   protected IAtsServer getAtsServer() {
      return atsServer;
   }

   @Override
   public String getDescription() {
      try {
         return artifact.getSoleAttributeAsString(AtsAttributeTypes.Description, "");
      } catch (OseeCoreException ex) {
         logger.error(ex, "Error getting description for artifact[%s]", artifact);
         return "exception: " + ex.getLocalizedMessage();
      }
   }

   @Override
   public IAtsWorkData getWorkData() {
      if (workData == null) {
         workData = new WorkData(atsServer.getUserService(), this, artifact);
      }
      return workData;
   }

   @Override
   public List<IAtsUser> getAssignees() throws OseeCoreException {
      return getStateMgr().getAssignees();
   }

   @Override
   public List<IAtsUser> getImplementers() throws OseeCoreException {
      throw new OseeStateException("Not implemented");
   }

   @Override
   public String getAtsId() {
      try {
         return artifact.getSoleAttributeAsString(AtsAttributeTypes.AtsId, getGuid());
      } catch (OseeCoreException ex) {
         return null;
      }
   }

   @Override
   public void setAtsId(String atsId, IAtsChangeSet changes) throws OseeCoreException {
      throw new OseeStateException("Not implemented");
   }

   @Override
   public IAtsTeamWorkflow getParentTeamWorkflow() throws OseeCoreException {
      ArtifactReadable teamArt = null;
      if (isTeamWorkflow()) {
         teamArt = artifact;
      } else if (isReview()) {
         ResultSet<ArtifactReadable> results = artifact.getRelated(AtsRelationTypes.TeamWorkflowToReview_Team);
         if (!results.isEmpty()) {
            teamArt = results.iterator().next();
         }
      } else if (isTask()) {
         ResultSet<ArtifactReadable> results = artifact.getRelated(AtsRelationTypes.TeamWfToTask_TeamWf);
         if (!results.isEmpty()) {
            teamArt = results.iterator().next();
         }
      }
      return atsServer.getWorkItemFactory().getTeamWf(teamArt);
   }

   private boolean isReview() {
      return this instanceof IAtsAbstractReview;
   }

   @Override
   public IAtsStateManager getStateMgr() {
      if (stateMgr == null) {
         try {
            stateMgr = getAtsServer().getStateFactory().getStateManager(this, true);
         } catch (OseeCoreException ex) {
            logger.error(ex, "Error getting stateManager for artifact[%s]", artifact);
         }
      }
      return stateMgr;
   }

   @Override
   public IAtsLog getLog() {
      if (atsLog == null) {
         try {
            atsLog = getAtsServer().getLogFactory().getLogLoaded(this, atsServer.getAttributeResolver());
         } catch (OseeCoreException ex) {
            logger.error(ex, "Error getting Log for artifact[%s]", artifact);
         }
      }
      return atsLog;
   }

   @Override
   public IAtsWorkDefinition getWorkDefinition() {
      if (match == null) {
         match = getWorkDefinitionMatch();
         if (match == null) {
            return null;
         }
         if (!match.isMatched()) {
            logger.error("Error getting work definition for artifact[%s] - using match [%s]", artifact, match);
            return null;
         }
      }
      return match.getWorkDefinition();
   }

   public IWorkDefinitionMatch getWorkDefinitionMatch() {
      if (match == null) {
         try {
            match = getAtsServer().getWorkDefAdmin().getWorkDefinition(this);
         } catch (Exception ex) {
            logger.error("Error getting work definition match for artifact[%s]", artifact);
         }
      }
      return match;
   }

   @Override
   public IAtsStateDefinition getStateDefinition() {
      if (getStateMgr().getCurrentStateName() == null) {
         return null;
      }
      return getWorkDefinition().getStateByName(getStateMgr().getCurrentStateName());
   }

   public IAtsStateDefinition getStateDefinitionByName(String name) {
      return getWorkDefinition().getStateByName(name);
   }

   @Override
   public boolean isTask() {
      return this instanceof IAtsTask;
   }

   @Override
   public boolean isTeamWorkflow() {
      return this instanceof IAtsTeamWorkflow;
   }

   @Override
   public Object getStoreObject() {
      return artifact;
   }

}