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
package org.eclipse.osee.ats.client.demo.populate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.eclipse.core.runtime.Assert;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.agile.AgileEndpointApi;
import org.eclipse.osee.ats.api.agile.AgileWriterResult;
import org.eclipse.osee.ats.api.agile.IAgileSprint;
import org.eclipse.osee.ats.api.agile.JaxAgileItem;
import org.eclipse.osee.ats.api.agile.JaxNewAgileBacklog;
import org.eclipse.osee.ats.api.agile.JaxNewAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.JaxNewAgileSprint;
import org.eclipse.osee.ats.api.agile.JaxNewAgileTeam;
import org.eclipse.osee.ats.api.config.JaxAtsObject;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.ats.api.workflow.transition.IAtsTransitionManager;
import org.eclipse.osee.ats.api.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.client.demo.SprintItemData;
import org.eclipse.osee.ats.client.demo.internal.AtsClientService;
import org.eclipse.osee.ats.core.client.config.AtsBulkLoad;
import org.eclipse.osee.ats.core.workflow.state.TeamState;
import org.eclipse.osee.ats.core.workflow.transition.TransitionFactory;
import org.eclipse.osee.ats.core.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.demo.api.DemoArtifactToken;
import org.eclipse.osee.ats.demo.api.DemoArtifactTypes;
import org.eclipse.osee.ats.demo.api.DemoWorkflowTitles;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.RelationSorter;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.support.test.util.TestUtil;

/**
 * @author Donald G. Dunne
 */
public class Pdd93CreateDemoAgile {

   private static void validateArtifactCache() {
      final Collection<Artifact> list = ArtifactCache.getDirtyArtifacts();
      if (!list.isEmpty()) {
         for (Artifact artifact : list) {
            System.err.println(String.format("Artifact [%s] is dirty [%s]", artifact.toStringWithId(),
               Artifacts.getDirtyReport(artifact)));
         }
         throw new OseeStateException("[%d] Dirty Artifacts found after populate (see console for details)",
            list.size());
      }

   }

   public void run() throws Exception {
      validateArtifactCache();

      AtsBulkLoad.reloadConfig(true);
      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();

      createSampleAgileTeam();

      TestUtil.severeLoggingEnd(monitorLog);
   }

   private void createSampleAgileTeam() {
      AgileEndpointApi agile = AtsClientService.getAgile();

      // Create Team
      JaxNewAgileTeam newTeam = getJaxAgileTeam();
      Response response = agile.createTeam(newTeam);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Config Agile Team with points attr type");
      Artifact sawAgileTeam = AtsClientService.get().getArtifact(DemoArtifactToken.SAW_Agile_Team);
      changes.setSoleAttributeValue(sawAgileTeam, AtsAttributeTypes.PointsAttributeType,
         AtsAttributeTypes.Points.getName());
      changes.execute();

      // Assigne ATS Team to Agile Team
      Artifact sawCodeArt = AtsClientService.get().getArtifact(DemoArtifactToken.SAW_Code);
      Conditions.assertNotNull(sawCodeArt, "sawCodeArt");
      Artifact agileTeam = AtsClientService.get().getArtifact(newTeam.getUuid());
      agileTeam.addRelation(AtsRelationTypes.AgileTeamToAtsTeam_AtsTeam, sawCodeArt);
      agileTeam.persist("Assigne ATS Team to Agile Team");

      // Create Backlog
      JaxNewAgileBacklog backlog = getBacklog();
      response = agile.createBacklog(DemoArtifactToken.SAW_Agile_Team.getId(), backlog);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      // Add items to backlog
      Collection<IAtsWorkItem> items =
         AtsClientService.get().getQueryService().createQuery(WorkItemType.TeamWorkflow).isOfType(
            DemoArtifactTypes.DemoCodeTeamWorkflow, DemoArtifactTypes.DemoReqTeamWorkflow,
            DemoArtifactTypes.DemoTestTeamWorkflow).getItems();
      Assert.isTrue(items.size() > 0);

      JaxAgileItem item = new JaxAgileItem();
      item.setBacklogUuid(backlog.getUuid());
      item.setSetBacklog(true);
      for (IAtsWorkItem workItem : items) {
         item.getUuids().add(workItem.getId());
      }
      AgileWriterResult result = agile.updateItems(item);
      Conditions.assertFalse(result.getResults().isErrors(), result.getResults().toString());

      // Set backlog as user_defined member order
      Artifact backlogArt = AtsClientService.get().getArtifact(backlog.getUuid());
      RelationManager.setRelationOrder(backlogArt, AtsRelationTypes.Goal_Member, RelationSide.SIDE_B,
         RelationSorter.USER_DEFINED, backlogArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member));

      // Create Sprints
      JaxNewAgileSprint sprint1 = newSprint(DemoArtifactToken.SAW_Sprint_1);
      response = agile.createSprint(sprint1.getTeamUuid(), sprint1);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      JaxNewAgileSprint sprint2 = newSprint(DemoArtifactToken.SAW_Sprint_2);
      response = agile.createSprint(sprint2.getTeamUuid(), sprint2);
      Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());

      // Add items to Sprint
      JaxAgileItem completedItems = new JaxAgileItem();
      completedItems.setSprintUuid(DemoArtifactToken.SAW_Sprint_1.getId());
      completedItems.setSetSprint(true);

      JaxAgileItem inworkItems = new JaxAgileItem();
      inworkItems.setSprintUuid(DemoArtifactToken.SAW_Sprint_2.getId());
      inworkItems.setSetSprint(true);

      for (IAtsWorkItem workItem : items) {
         if (workItem.getStateMgr().getStateType().isCompleted()) {
            completedItems.getUuids().add(workItem.getId());
         } else {
            inworkItems.getUuids().add(workItem.getId());
         }
      }
      result = agile.updateItems(inworkItems);
      Conditions.assertFalse(result.getResults().isErrors(), result.getResults().toString());
      result = agile.updateItems(completedItems);
      Conditions.assertFalse(result.getResults().isErrors(), result.getResults().toString());

      Artifact sprint1Art = AtsClientService.get().getArtifact(sprint1.getUuid());
      RelationManager.setRelationOrder(sprint1Art, AtsRelationTypes.AgileSprintToItem_AtsItem, RelationSide.SIDE_B,
         RelationSorter.USER_DEFINED, sprint1Art.getRelatedArtifacts(AtsRelationTypes.AgileSprintToItem_AtsItem));
      sprint1Art.persist("Set sort order for Sprint 1");

      Artifact spring2Art = AtsClientService.get().getArtifact(sprint2.getUuid());
      RelationManager.setRelationOrder(spring2Art, AtsRelationTypes.AgileSprintToItem_AtsItem, RelationSide.SIDE_B,
         RelationSorter.USER_DEFINED, spring2Art.getRelatedArtifacts(AtsRelationTypes.AgileSprintToItem_AtsItem));
      sprint2Art.persist("Set sort order for Sprint 2");

      // Transition First Sprint to completed
      IAtsWorkItem sprint = AtsClientService.get().getQueryService().createQuery(WorkItemType.WorkItem).andUuids(
         DemoArtifactToken.SAW_Sprint_1.getId()).getItems().iterator().next();
      changes.reset("Transition Agile Sprint");
      TransitionHelper helper =
         new TransitionHelper("Transition Agile Stprint", Arrays.asList(sprint), TeamState.Completed.getName(), null,
            null, changes, AtsClientService.get().getServices(), TransitionOption.OverrideAssigneeCheck);
      IAtsTransitionManager transitionMgr = TransitionFactory.getTransitionManager(helper);
      TransitionResults results = transitionMgr.handleAll();
      if (results.isEmpty()) {
         changes.execute();
      } else {
         throw new OseeStateException("Can't transition sprint to completed [%s]", results.toString());
      }

      // Create Feature Groups
      for (String name : Arrays.asList("Communications", "UI", "Documentation", "Framework")) {
         JaxNewAgileFeatureGroup group = newFeatureGroup(name);
         response = agile.createFeatureGroup(DemoArtifactToken.SAW_Agile_Team.getId(), group);
         Assert.isTrue(Response.Status.CREATED.getStatusCode() == response.getStatus());
      }

      setupSprint2ForBurndown(DemoArtifactToken.SAW_Sprint_2.getId());
   }

   private void setupSprint2ForBurndown(long secondSprintUuid) {

      // Transition First Sprint to completed
      IAtsWorkItem sprint = AtsClientService.get().getQueryService().createQuery(WorkItemType.WorkItem).andUuids(
         secondSprintUuid).getItems().iterator().next();
      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Setup Sprint 2 for Burndown");

      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_YEAR, -5);
      Date holiday1 = null, holiday2 = null;
      // backup start date till hit weekday
      while (!DateUtil.isWeekDay(cal)) {
         cal.add(Calendar.DAY_OF_YEAR, -1);
      }
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.StartDate, cal.getTime());
      int x = 1;
      int holidayDayNum = 5;
      // count up 20 weekdays and set 2 weekday holidays
      for (x = 1; x <= 21; x++) {
         cal.add(Calendar.DAY_OF_YEAR, 1);
         while (!DateUtil.isWeekDay(cal)) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
         }
         if (x == holidayDayNum) {
            holiday1 = cal.getTime();
         } else if (x == holidayDayNum + 1) {
            holiday2 = cal.getTime();
         }
      }
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.EndDate, cal.getTime());
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.UnPlannedPoints, 45);
      changes.setSoleAttributeValue(sprint, AtsAttributeTypes.PlannedPoints, 200);
      changes.addAttribute(sprint, AtsAttributeTypes.Holiday, holiday1);
      changes.addAttribute(sprint, AtsAttributeTypes.Holiday, holiday2);
      // set sprint data on sprint items
      Artifact agileTeamArt =
         ((Artifact) sprint.getStoreObject()).getRelatedArtifact(AtsRelationTypes.AgileTeamToSprint_AgileTeam);
      changes.execute();

      setSprintItemData(agileTeamArt.getUuid(), (IAgileSprint) sprint);
   }

   private void setSprintItemData(Long teamUuid, IAgileSprint sprint) {
      List<SprintItemData> datas = new LinkedList<>();
      datas.add(
         new SprintItemData("Sprint Order", "Title", "Points", "Unplanned Work", "Feature Group", "CreatedDate"));
      datas.add(
         new SprintItemData("1", "Button W doesn't work on Situation Page", "8", " ", "Communications", "10/03/2016"));
      datas.add(new SprintItemData("2", "Can't load Diagram Tree", "4", "Unplanned Work", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("3", "Can't see the Graph View", "8", "Unplanned Work", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("4", "Problem in Diagram Tree", "40", " ", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("5", "Problem with the Graph View", "8", " ", "Communications", "10/03/2016"));
      datas.add(new SprintItemData("6", DemoWorkflowTitles.SAW_COMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "2",
         "Unplanned Work", "Framework", "10/03/2016"));
      datas.add(new SprintItemData("7", DemoWorkflowTitles.SAW_COMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "8", " ",
         "Framework", "10/03/2016"));
      datas.add(new SprintItemData("8", DemoWorkflowTitles.SAW_COMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "16", " ", "UI",
         "10/03/2016"));
      datas.add(new SprintItemData("9", DemoWorkflowTitles.SAW_NO_BRANCH_REQT_CHANGES_FOR_DIAGRAM_VIEW, "32", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("10", DemoWorkflowTitles.SAW_NO_BRANCH_REQT_CHANGES_FOR_DIAGRAM_VIEW, "40", " ",
         "Documentation", "10/03/2016"));
      datas.add(new SprintItemData("11", DemoWorkflowTitles.SAW_NO_BRANCH_REQT_CHANGES_FOR_DIAGRAM_VIEW, "8", " ",
         "Documentation", "10/03/2016"));
      datas.add(new SprintItemData("12", DemoWorkflowTitles.SAW_UNCOMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "1", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("13", DemoWorkflowTitles.SAW_UNCOMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "6", " ",
         "Documentation", "10/03/2016"));
      datas.add(new SprintItemData("14", DemoWorkflowTitles.SAW_UNCOMMITTED_REQT_CHANGES_FOR_DIAGRAM_VIEW, "32", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("15", DemoArtifactToken.SAW_UnCommitedConflicted_Req_TeamWf.getName(), "1", " ",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("16", "Workaround for Graph View for SAW_Bld_2", "1", "Unplanned Work",
         "Communications", "10/03/2016"));
      datas.add(new SprintItemData("17", "Workaround for Graph View for SAW_Bld_3", "2", "Unplanned Work",
         "Communications", "10/03/2016"));

      int x = 1;
      for (JaxAtsObject jaxWorkItem : AtsClientService.getAgile().getSprintItemsAsJax(teamUuid,
         sprint.getId()).getAtsObjects()) {
         SprintItemData data = getSprintData(datas, x++, jaxWorkItem);
         String featureGroupName = data.getFeature();
         if (Strings.isValid(featureGroupName)) {
            AtsClientService.getAgile().addFeatureGroup(jaxWorkItem.getUuid(), featureGroupName);
         }
         String unPlannedStr = data.getUnPlanned();
         boolean unPlanned = false;
         if (Strings.isValid(unPlannedStr)) {
            if (unPlannedStr.toLowerCase().contains("un")) {
               unPlanned = true;
            }
         }
         AtsClientService.getAgile().setUnPlanned(jaxWorkItem.getUuid(), unPlanned);
         String points = data.getPoints();
         if (Strings.isValid(points)) {
            AtsClientService.getAgile().setPoints(jaxWorkItem.getUuid(), points);
         }
      }
   }

   private SprintItemData getSprintData(List<SprintItemData> datas, int i, JaxAtsObject workItem) {
      for (SprintItemData data : datas) {
         if (data.getOrder().equals(String.valueOf(i)) && data.getTitle().equals(workItem.getName())) {
            return data;
         }
      }
      return null;
   }

   private JaxNewAgileBacklog getBacklog() {
      JaxNewAgileBacklog backlog = new JaxNewAgileBacklog();
      backlog.setName(DemoArtifactToken.SAW_Backlog.getName());
      backlog.setUuid(DemoArtifactToken.SAW_Backlog.getId());
      backlog.setTeamUuid(DemoArtifactToken.SAW_Agile_Team.getId());
      return backlog;
   }

   private JaxNewAgileFeatureGroup newFeatureGroup(String name) {
      JaxNewAgileFeatureGroup group = new JaxNewAgileFeatureGroup();
      group.setName(name);
      group.setTeamUuid(DemoArtifactToken.SAW_Agile_Team.getId());
      group.setUuid(Lib.generateArtifactIdAsInt());
      return group;
   }

   private JaxNewAgileSprint newSprint(ArtifactToken token) {
      JaxNewAgileSprint newSprint = new JaxNewAgileSprint();
      newSprint.setName(token.getName());
      newSprint.setUuid(token.getId());
      newSprint.setTeamUuid(DemoArtifactToken.SAW_Agile_Team.getId());
      return newSprint;
   }

   private JaxNewAgileTeam getJaxAgileTeam() {
      JaxNewAgileTeam newTeam = new JaxNewAgileTeam();
      newTeam.setName(DemoArtifactToken.SAW_Agile_Team.getName());
      newTeam.setUuid(DemoArtifactToken.SAW_Agile_Team.getId());
      return newTeam;
   }

}
