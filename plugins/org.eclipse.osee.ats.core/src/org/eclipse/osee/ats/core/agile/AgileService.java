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
package org.eclipse.osee.ats.core.agile;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.agile.AgileWriterResult;
import org.eclipse.osee.ats.api.agile.IAgileBacklog;
import org.eclipse.osee.ats.api.agile.IAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.IAgileItem;
import org.eclipse.osee.ats.api.agile.IAgileService;
import org.eclipse.osee.ats.api.agile.IAgileSprint;
import org.eclipse.osee.ats.api.agile.IAgileSprintHtmlOperation;
import org.eclipse.osee.ats.api.agile.IAgileTeam;
import org.eclipse.osee.ats.api.agile.JaxAgileBacklog;
import org.eclipse.osee.ats.api.agile.JaxAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.JaxAgileItem;
import org.eclipse.osee.ats.api.agile.JaxAgileTeam;
import org.eclipse.osee.ats.api.agile.JaxNewAgileTeam;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.core.agile.operations.SprintBurndownOperations;
import org.eclipse.osee.ats.core.agile.operations.SprintBurnupOperations;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.util.result.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G. Dunne
 */
public class AgileService implements IAgileService {

   private final Log logger;
   private final IAtsServices services;

   public AgileService(Log logger, IAtsServices services) {
      this.logger = logger;
      this.services = services;
   }

   /********************************
    ** Agile Team
    ***********************************/
   @Override
   public IAgileTeam getAgileTeam(ArtifactId artifact) {
      return AgileFactory.getAgileTeam(logger, services, artifact);
   }

   @Override
   public IAgileTeam getAgileTeam(long uuid) {
      IAgileTeam team = null;
      ArtifactId teamArt = services.getArtifact(uuid);
      if (teamArt != null) {
         team = getAgileTeam(teamArt);
      }
      return team;
   }

   @Override
   public IAgileTeam getAgileTeamById(long teamUuid) {
      IAgileTeam team = null;
      ArtifactId artifact = services.getArtifact(teamUuid);
      if (artifact != null) {
         team = getAgileTeam(artifact);
      }
      return team;
   }

   @Override
   public IAgileTeam createAgileTeam(JaxNewAgileTeam newTeam) {
      return AgileFactory.createAgileTeam(logger, services, newTeam);
   }

   @Override
   public IAgileTeam updateAgileTeam(JaxAgileTeam team) {
      return AgileFactory.updateAgileTeam(logger, services, team);
   }

   @Override
   public void deleteAgileTeam(long uuid) {
      ArtifactToken team = services.getArtifact(uuid);
      if (!services.getStoreService().isOfType(team, AtsArtifactTypes.AgileTeam)) {
         throw new OseeArgumentException("UUID %d is not a valid Agile Team", uuid);
      }
      IAtsChangeSet changes = services.createChangeSet("Delete Agile Team");
      deleteRecurse(services.getRelationResolver().getChildren(team), changes);
      changes.deleteArtifact(team);
      changes.execute();
   }

   private void deleteRecurse(Collection<ArtifactToken> resultSet, IAtsChangeSet changes) {
      Iterator<ArtifactToken> iterator = resultSet.iterator();
      while (iterator.hasNext()) {
         ArtifactId art = iterator.next();
         deleteRecurse(services.getRelationResolver().getChildren(art), changes);
         changes.deleteArtifact(art);
      }
   }

   @Override
   public Collection<IAgileTeam> getTeams() {
      List<IAgileTeam> teams = new ArrayList<>();
      for (ArtifactId teamArt : services.getQueryService().createQuery(AtsArtifactTypes.AgileTeam).getArtifacts()) {
         teams.add(getAgileTeam(teamArt));
      }
      return teams;
   }

   @Override
   public AttributeTypeId getAgileTeamPointsAttributeType(IAgileTeam team) {
      AttributeTypeId type = AtsAttributeTypes.Points;
      String attrTypeName =
         services.getAttributeResolver().getSoleAttributeValue(team, AtsAttributeTypes.PointsAttributeType, null);
      if (Strings.isValid(attrTypeName)) {
         type = getTypeFromName(attrTypeName);
      }
      return type;
   }

   private AttributeTypeId getTypeFromName(String attrTypeName) {
      AttributeTypeId type = null;
      for (AttributeTypeToken attrType : services.getStoreService().getAttributeTypes()) {
         if (attrType.getName().equals(attrTypeName)) {
            type = attrType;
            break;
         }
      }
      if (type == null) {
         throw new OseeCoreException("Invalid attribute type name provided: %s", attrTypeName);
      }
      return type;
   }

   @Override
   public IAgileTeam getAgileTeam(IAtsTeamDefinition teamDef) {
      IAgileTeam aTeam = null;
      ArtifactId aTeamArt =
         services.getRelationResolver().getRelatedOrNull(teamDef, AtsRelationTypes.AgileTeamToAtsTeam_AgileTeam);
      if (aTeamArt != null) {
         aTeam = services.getAgileService().getAgileTeam(aTeamArt);
      }
      return aTeam;
   }

   @Override
   public IAgileTeam getAgileTeam(IAgileSprint sprint) {
      IAgileTeam aTeam = null;
      ArtifactId aTeamArt =
         services.getRelationResolver().getRelatedOrNull(sprint, AtsRelationTypes.AgileTeamToSprint_AgileTeam);
      if (aTeamArt != null) {
         aTeam = services.getAgileService().getAgileTeam(aTeamArt);
      }
      return aTeam;
   }

   @Override
   public IAgileTeam getAgileTeamByName(String agileTeamName) {
      IAgileTeam aTeam = null;
      ArtifactId aTeamArt = services.getArtifactByName(AtsArtifactTypes.AgileTeam, agileTeamName);
      if (aTeamArt != null) {
         aTeam = services.getConfigItemFactory().getAgileTeam(aTeamArt);
      }
      return aTeam;
   }

   /********************************
    ** Agile Feature Group
    ***********************************/
   @Override
   public IAgileFeatureGroup getAgileFeatureGroup(ArtifactId artifact) {
      return AgileFactory.getAgileFeatureGroup(logger, services, artifact);
   }

   @Override
   public void deleteAgileFeatureGroup(long uuid) {
      ArtifactId featureGroup = services.getArtifact(uuid);
      if (!services.getStoreService().isOfType(featureGroup, AtsArtifactTypes.AgileFeatureGroup)) {
         throw new OseeArgumentException("UUID %d is not a valid Agile Feature Group", uuid);
      }
      IAtsChangeSet changes = services.createChangeSet("Delete Agile Feature Group");
      changes.deleteArtifact(featureGroup);
      changes.execute();
   }

   @Override
   public IAgileFeatureGroup createAgileFeatureGroup(long teamUuid, String name, String guid, Long uuid) {
      return AgileFactory.createAgileFeatureGroup(logger, services, teamUuid, name, guid, uuid);
   }

   @Override
   public IAgileFeatureGroup createAgileFeatureGroup(JaxAgileFeatureGroup newFeatureGroup) {
      return AgileFactory.createAgileFeatureGroup(logger, services, newFeatureGroup);
   }

   @Override
   public Collection<IAgileFeatureGroup> getAgileFeatureGroups(IAgileTeam team) {
      List<IAgileFeatureGroup> groups = new LinkedList<>();
      ArtifactId artifact = team.getStoreObject();
      for (ArtifactId groupArt : services.getRelationResolver().getRelated(artifact,
         AtsRelationTypes.AgileTeamToFeatureGroup_FeatureGroup)) {
         groups.add(services.getConfigItemFactory().getAgileFeatureGroup(groupArt));
      }
      return groups;
   }

   @Override
   public IAgileBacklog getBacklogForTeam(long teamUuid) {
      IAgileBacklog backlog = null;
      ArtifactId teamArt = services.getArtifact(teamUuid);
      ArtifactId backlogArt =
         services.getRelationResolver().getRelatedOrNull(teamArt, AtsRelationTypes.AgileTeamToBacklog_Backlog);
      if (backlogArt != null) {
         backlog = getAgileBacklog(backlogArt);
      }
      return backlog;
   }

   /********************************
    ** Agile Sprint
    ***********************************/
   @Override
   public IAgileSprint getAgileSprint(ArtifactId artifact) {
      return AgileFactory.getAgileSprint(logger, services, artifact);
   }

   @Override
   public IAgileSprint getAgileSprint(long id) {
      return getAgileSprint(ArtifactId.valueOf(id));
   }

   @Override
   public IAgileSprint createAgileSprint(long teamUuid, String name, String guid, Long uuid) {
      return AgileFactory.createAgileSprint(logger, services, teamUuid, name, guid, uuid);
   }

   @Override
   public Collection<IAgileSprint> getSprintsForTeam(long teamUuid) {
      List<IAgileSprint> sprints = new ArrayList<>();
      ArtifactId team = services.getArtifact(teamUuid);
      for (ArtifactId sprintArt : services.getRelationResolver().getRelated(team,
         AtsRelationTypes.AgileTeamToSprint_Sprint)) {
         sprints.add(getAgileSprint(sprintArt));
      }
      return sprints;
   }

   @Override
   public Collection<IAgileSprint> getAgileSprints(IAgileTeam team) {
      List<IAgileSprint> sprints = new LinkedList<>();
      ArtifactId artifact = team.getStoreObject();
      for (ArtifactToken sprintArt : services.getRelationResolver().getRelated(artifact,
         AtsRelationTypes.AgileTeamToSprint_Sprint)) {
         sprints.add(services.getWorkItemFactory().getAgileSprint(sprintArt));
      }
      return sprints;
   }

   /********************************
    ** Agile Backlog
    ***********************************/
   @Override
   public IAgileBacklog getAgileBacklog(ArtifactId artifact) {
      return AgileFactory.getAgileBacklog(logger, services, artifact);
   }

   @Override
   public IAgileBacklog getAgileBacklog(IAgileTeam team) {
      ArtifactId teamFolder = AgileFolders.getTeamFolder(services, team.getId());
      if (teamFolder == null) {
         return null;
      }
      ArtifactToken backlogArt =
         services.getRelationResolver().getRelatedOrNull(teamFolder, AtsRelationTypes.AgileTeamToBacklog_Backlog);
      if (backlogArt == null) {
         return null;
      }
      return new AgileBacklog(logger, services, backlogArt);
   }

   @Override
   public IAgileBacklog getAgileBacklog(long uuid) {
      IAgileBacklog backlog = null;
      ArtifactId teamArt = services.getArtifact(uuid);
      if (teamArt != null) {
         backlog = getAgileBacklog(teamArt);
      }
      return backlog;
   }

   @Override
   public IAgileBacklog createAgileBacklog(long teamUuid, String name, String guid, Long uuid) {
      return AgileFactory.createAgileBacklog(logger, services, teamUuid, name, guid, uuid);
   }

   @Override
   public IAgileBacklog updateAgileBacklog(JaxAgileBacklog updatedBacklog) {
      AgileBacklogWriter writer = new AgileBacklogWriter(services, this, updatedBacklog);
      return writer.write();
   }

   @Override
   public AgileWriterResult updateAgileItem(JaxAgileItem newItem) {
      AgileItemWriter writer = new AgileItemWriter(services, this, newItem);
      return writer.write();
   }

   @Override
   public Collection<IAgileFeatureGroup> getAgileFeatureGroups(List<Long> uuids) {
      List<IAgileFeatureGroup> features = new LinkedList<>();
      for (ArtifactId featureArt : services.getArtifacts(uuids)) {
         features.add(services.getConfigItemFactory().getAgileFeatureGroup(featureArt));
      }
      return features;
   }

   @Override
   public Collection<IAgileItem> getItems(IAgileBacklog backlog) {
      return getItems(backlog, AtsRelationTypes.Goal_Member);
   }

   private Collection<IAgileItem> getItems(IAtsObject backlogOrSprint, RelationTypeSide relationType) {
      List<IAgileItem> items = new LinkedList<>();
      ArtifactId backlogArt = backlogOrSprint.getStoreObject();
      for (ArtifactToken art : services.getRelationResolver().getRelated(backlogArt, relationType)) {
         if (services.getStoreService().isOfType(art, AtsArtifactTypes.AbstractWorkflowArtifact)) {
            items.add(services.getWorkItemFactory().getAgileItem(art));
         } else {
            throw new OseeStateException("Inavlid artifact [%s] in [%s].  Only workflows are allowed, not [%s]",
               art.toStringWithId(), backlogOrSprint, art.getArtifactType().getName());
         }
      }
      return items;
   }

   @Override
   public Collection<IAgileItem> getItems(IAgileSprint sprint) {
      return getItems(sprint, AtsRelationTypes.AgileSprintToItem_AtsItem);
   }

   @Override
   public Collection<IAgileFeatureGroup> getFeatureGroups(IAgileItem aItem) {
      List<IAgileFeatureGroup> groups = new LinkedList<>();
      ArtifactId itemArt = services.getArtifact(aItem);
      for (ArtifactId featureGroup : services.getRelationResolver().getRelated(itemArt,
         AtsRelationTypes.AgileFeatureToItem_FeatureGroup)) {
         groups.add(services.getAgileService().getAgileFeatureGroup(featureGroup));
      }
      return groups;
   }

   @Override
   public IAgileSprint getSprint(IAgileItem item) {
      IAgileSprint sprint = null;
      ArtifactToken itemArt = services.getArtifact(item);
      ArtifactToken sprintArt =
         services.getRelationResolver().getRelatedOrNull(itemArt, AtsRelationTypes.AgileSprintToItem_Sprint);
      if (sprintArt != null) {
         sprint = services.getWorkItemFactory().getAgileSprint(sprintArt);
      }
      return sprint;
   }

   @Override
   public void deleteSprint(long sprintUuid) {
      ArtifactId sprint = services.getArtifact(sprintUuid);
      if (sprint != null) {
         IAtsChangeSet changes = services.createChangeSet("Delete Agile Sprint");
         changes.deleteArtifact(sprint);
         changes.execute();
      }
   }

   @Override
   public IAgileTeam getAgileTeam(IAgileItem item) {
      ArtifactId itemArt = services.getArtifact(item);
      ArtifactId backlogArt = services.getRelationResolver().getRelatedOrNull(itemArt, AtsRelationTypes.Goal_Member);
      if (backlogArt != null) {
         ArtifactId teamArt =
            services.getRelationResolver().getRelatedOrNull(backlogArt, AtsRelationTypes.AgileTeamToBacklog_AgileTeam);
         if (teamArt != null) {
            return services.getConfigItemFactory().getAgileTeam(teamArt);
         }
      }
      ArtifactId sprintArt =
         services.getRelationResolver().getRelatedOrNull(itemArt, AtsRelationTypes.AgileSprintToItem_Sprint);
      if (sprintArt != null) {
         ArtifactId teamArt =
            services.getRelationResolver().getRelatedOrNull(sprintArt, AtsRelationTypes.AgileTeamToSprint_AgileTeam);
         if (teamArt != null) {
            return services.getConfigItemFactory().getAgileTeam(teamArt);
         }
      }
      return null;
   }

   @Override
   public ArtifactToken getRelatedBacklogArt(IAtsWorkItem workItem) {
      ArtifactToken relatedBacklogArt = null;
      for (ArtifactToken backlogArt : services.getRelationResolver().getRelated(workItem, AtsRelationTypes.Goal_Goal)) {
         if (isBacklog(backlogArt)) {
            relatedBacklogArt = backlogArt;
         }
      }
      return relatedBacklogArt;
   }

   @Override
   public boolean isBacklog(Object object) {
      boolean backlog = false;
      if (object instanceof IAtsWorkItem) {
         backlog = services.getRelationResolver().getRelatedCount((IAtsWorkItem) object,
            AtsRelationTypes.AgileTeamToBacklog_AgileTeam) == 1;
      } else if (object instanceof ArtifactToken) {
         backlog = services.getRelationResolver().getRelatedCount((ArtifactToken) object,
            AtsRelationTypes.AgileTeamToBacklog_AgileTeam) == 1;
      }
      return backlog;
   }

   @Override
   public boolean isSprint(ArtifactId artifact) {
      return services.getStoreService().isOfType(artifact, AtsArtifactTypes.AgileSprint);
   }

   @Override
   public Collection<ArtifactToken> getRelatedSprints(ArtifactId artifact) {
      Set<ArtifactToken> sprints = new HashSet<>();
      for (ArtifactToken sprintArt : services.getRelationResolver().getRelatedArtifacts(artifact,
         AtsRelationTypes.AgileSprintToItem_Sprint)) {
         sprints.add(sprintArt);
      }
      return sprints;
   }

   @Override
   public XResultData storeSprintReports(long teamId, long sprintId) {
      XResultData results = new XResultData();
      results.setTitle("Store Sprint Reports");
      IAtsChangeSet changes = services.createChangeSet("Store Agile Sprint Reports");
      IAgileSprint sprint = getAgileSprint(services.getArtifact(sprintId));
      createUpdateBurnChart(new SprintBurndownOperations(services), teamId, sprintId, services, changes, sprint);
      createUpdateBurnChart(new SprintBurnupOperations(services), teamId, sprintId, services, changes, sprint);
      for (IAgileSprintHtmlOperation operation : services.getAgileSprintHtmlReportOperations()) {
         createUpdateBurnChart(operation, teamId, sprintId, services, changes, sprint);
      }
      changes.executeIfNeeded();
      return results;
   }

   public static void createUpdateBurnChart(IAgileSprintHtmlOperation operation, long teamId, long sprintId, IAtsServices services, IAtsChangeSet changes, IAgileSprint sprint) {
      String html = operation.getReportHtml(teamId, sprintId);

      ArtifactId burndownArt =
         services.getRelationResolver().getChildNamedOrNull(sprint, operation.getReportType().name());
      if (burndownArt == null) {
         burndownArt = changes.createArtifact(CoreArtifactTypes.GeneralDocument, operation.getReportType().name());
         changes.setSoleAttributeValue(burndownArt, CoreAttributeTypes.Extension, "html");
         changes.addChild(sprint.getStoreObject(), burndownArt);
      }
      try {
         changes.setSoleAttributeValue(burndownArt, CoreAttributeTypes.NativeContent, Lib.stringToInputStream(html));
      } catch (UnsupportedEncodingException ex) {
         throw new OseeArgumentException(ex, "Error trying to store Agile " + operation.getReportType());
      }
   }

   @Override
   public Collection<IAtsTeamDefinition> getAtsTeams(IAgileTeam aTeam) {
      List<IAtsTeamDefinition> teamDefs = new LinkedList<>();
      for (ArtifactId teamArt : services.getRelationResolver().getRelated(aTeam.getStoreObject(),
         AtsRelationTypes.AgileTeamToAtsTeam_AtsTeam)) {
         teamDefs.add(services.getConfigItem(teamArt));
      }
      return teamDefs;
   }

}
