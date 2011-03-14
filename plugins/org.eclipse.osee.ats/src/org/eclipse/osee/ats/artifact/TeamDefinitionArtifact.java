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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.config.AtsCacheManager;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.VersionLockedType;
import org.eclipse.osee.ats.util.VersionManager;
import org.eclipse.osee.ats.util.VersionReleaseType;
import org.eclipse.osee.ats.util.widgets.commit.ICommitConfigArtifact;
import org.eclipse.osee.ats.workdef.RuleDefinition;
import org.eclipse.osee.ats.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.workdef.WorkDefinitionFactory;
import org.eclipse.osee.ats.workdef.WorkDefinitionMatch;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkFlowDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkFlowDefinitionMatch;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinitionFactory;

/**
 * @author Donald G. Dunne
 */
public class TeamDefinitionArtifact extends Artifact implements ICommitConfigArtifact {

   public static enum TeamDefinitionOptions {
      TeamUsesVersions,
      RequireTargetedVersion
   };

   public TeamDefinitionArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
   }

   @Override
   public Result isCreateBranchAllowed() throws OseeCoreException {
      if (!getSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch, false)) {
         return new Result(false, "Branch creation disabled for Team Definition [" + this + "]");
      }
      if (getParentBranch() == null) {
         return new Result(false, "Parent Branch not configured for Team Definition [" + this + "]");
      }
      return Result.TrueResult;
   }

   @Override
   public Result isCommitBranchAllowed() throws OseeCoreException {
      if (!getSoleAttributeValue(AtsAttributeTypes.AllowCommitBranch, false)) {
         return new Result(false, "Team Definition [" + this + "] not configured to allow branch commit.");
      }
      if (getParentBranch() == null) {
         return new Result(false, "Parent Branch not configured for Team Definition [" + this + "]");
      }
      return Result.TrueResult;
   }

   public void initialize(String fullname, String description, Collection<User> leads, Collection<User> members, Collection<ActionableItemArtifact> actionableItems, TeamDefinitionOptions... teamDefinitionOptions) throws OseeCoreException {
      List<Object> teamDefOptions = Collections.getAggregate((Object[]) teamDefinitionOptions);

      setSoleAttributeValue(AtsAttributeTypes.Description, description);
      setSoleAttributeValue(AtsAttributeTypes.FullName, fullname);
      for (User user : leads) {
         addRelation(AtsRelationTypes.TeamLead_Lead, user);
         // All leads are members
         addRelation(AtsRelationTypes.TeamMember_Member, user);
      }
      for (User user : members) {
         addRelation(AtsRelationTypes.TeamMember_Member, user);
      }

      if (teamDefOptions.contains(TeamDefinitionOptions.TeamUsesVersions)) {
         setSoleAttributeValue(AtsAttributeTypes.TeamUsesVersions, true);
      }
      if (teamDefOptions.contains(TeamDefinitionOptions.RequireTargetedVersion)) {
         addRule(RuleDefinitionOption.RequireTargetedVersion);
      }

      // Relate to actionable items
      for (ActionableItemArtifact aia : actionableItems) {
         addRelation(AtsRelationTypes.TeamActionableItem_ActionableItem, aia);
      }
   }

   public static List<TeamDefinitionArtifact> getTopLevelTeamDefinitions(Active active) throws OseeCoreException {
      TeamDefinitionArtifact topTeamDef = getTopTeamDefinition();
      if (topTeamDef == null) {
         return java.util.Collections.emptyList();
      }
      return Collections.castAll(AtsUtil.getActive(
         Artifacts.getChildrenOfTypeSet(topTeamDef, TeamDefinitionArtifact.class, false), active,
         TeamDefinitionArtifact.class));
   }

   @Override
   public Branch getParentBranch() throws OseeCoreException {
      try {
         String guid = getSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, "");
         if (GUID.isValid(guid)) {
            return BranchManager.getBranchByGuid(guid);
         }
      } catch (BranchDoesNotExist ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return null;
   }

   /**
    * This method will walk up the TeamDefinition tree until a def is found that configured with versions. This allows
    * multiple TeamDefinitions to be versioned/released together by having the parent hold the versions. It is not
    * required that a product configured in ATS uses the versions option. If no parent with versions is found, null is
    * returned. If boolean "Team Uses Versions" is false, just return cause this team doesn't use versions
    * 
    * @return parent TeamDefinition that holds the version definitions
    */
   public TeamDefinitionArtifact getTeamDefinitionHoldingVersions() throws OseeCoreException {
      if (!isTeamUsesVersions()) {
         return null;
      }
      if (getVersionsArtifacts().size() > 0) {
         return this;
      }
      if (getParent() instanceof TeamDefinitionArtifact) {
         TeamDefinitionArtifact parentTda = (TeamDefinitionArtifact) getParent();
         if (parentTda != null) {
            return parentTda.getTeamDefinitionHoldingVersions();
         }
      }
      return null;
   }

   /**
    * This method will walk up the TeamDefinition tree until a def is found that configured with work flow.
    * 
    * @return parent TeamDefinition that holds the work flow id attribute
    */
   public TeamDefinitionArtifact getTeamDefinitionHoldingWorkFlow() throws OseeCoreException {
      for (Artifact artifact : getRelatedArtifacts(CoreRelationTypes.WorkItem__Child, Artifact.class)) {
         if (artifact.isOfType(CoreArtifactTypes.WorkFlowDefinition)) {
            return this;
         }
      }
      if (getParent() instanceof TeamDefinitionArtifact) {
         TeamDefinitionArtifact parentTda = (TeamDefinitionArtifact) getParent();
         if (parentTda != null) {
            return parentTda.getTeamDefinitionHoldingWorkFlow();
         }
      }
      return null;
   }

   public Artifact getNextReleaseVersion() throws OseeCoreException {
      for (Artifact verArt : getRelatedArtifacts(AtsRelationTypes.TeamDefinitionToVersion_Version)) {
         if (verArt.getSoleAttributeValue(AtsAttributeTypes.NextVersion, false)) {
            return verArt;
         }
      }
      return null;
   }

   public Collection<Artifact> getVersionsFromTeamDefHoldingVersions(VersionReleaseType releaseType, VersionLockedType lockedType) throws OseeCoreException {
      TeamDefinitionArtifact teamDef = getTeamDefinitionHoldingVersions();
      if (teamDef == null) {
         return new ArrayList<Artifact>();
      }
      return teamDef.getVersionsArtifacts(releaseType, lockedType);
   }

   public static List<TeamDefinitionArtifact> getTeamDefinitions(Active active) throws OseeCoreException {
      return Collections.castAll(AtsCacheManager.getArtifactsByActive(AtsArtifactTypes.TeamDefinition, active));
   }

   public static List<TeamDefinitionArtifact> getTeamTopLevelDefinitions(Active active) throws OseeCoreException {
      TeamDefinitionArtifact topTeamDef = getTopTeamDefinition();
      if (topTeamDef == null) {
         return java.util.Collections.emptyList();
      }
      return Collections.castAll(AtsUtil.getActive(
         Artifacts.getChildrenOfTypeSet(topTeamDef, TeamDefinitionArtifact.class, false), active,
         TeamDefinitionArtifact.class));
   }

   public static TeamDefinitionArtifact getTopTeamDefinition() {
      return (TeamDefinitionArtifact) AtsUtil.getFromToken(AtsArtifactToken.TopTeamDefinition);
   }

   public static Set<TeamDefinitionArtifact> getTeamReleaseableDefinitions(Active active) throws OseeCoreException {
      Set<TeamDefinitionArtifact> teamDefs = new HashSet<TeamDefinitionArtifact>();
      for (TeamDefinitionArtifact teamDef : getTeamDefinitions(active)) {
         if (teamDef.getVersionsArtifacts().size() > 0) {
            teamDefs.add(teamDef);
         }
      }
      return teamDefs;
   }

   public static Collection<TeamDefinitionArtifact> getImpactedTeamDefs(Collection<ActionableItemArtifact> aias) throws OseeCoreException {
      Set<TeamDefinitionArtifact> resultTeams = new HashSet<TeamDefinitionArtifact>();
      for (ActionableItemArtifact aia : aias) {
         resultTeams.addAll(getImpactedTeamDefInherited(aia));
      }
      return resultTeams;
   }

   private static List<TeamDefinitionArtifact> getImpactedTeamDefInherited(ActionableItemArtifact aia) throws OseeCoreException {
      if (aia.getRelatedArtifacts(AtsRelationTypes.TeamActionableItem_Team).size() > 0) {
         return aia.getRelatedArtifacts(AtsRelationTypes.TeamActionableItem_Team, TeamDefinitionArtifact.class);
      }
      Artifact parentArt = aia.getParent();
      if (parentArt instanceof ActionableItemArtifact) {
         return getImpactedTeamDefInherited((ActionableItemArtifact) parentArt);
      }
      return java.util.Collections.emptyList();
   }

   public static Set<TeamDefinitionArtifact> getTeamsFromItemAndChildren(ActionableItemArtifact aia) throws OseeCoreException {
      Set<TeamDefinitionArtifact> aiaTeams = new HashSet<TeamDefinitionArtifact>();
      getTeamFromItemAndChildren(aia, aiaTeams);
      return aiaTeams;
   }

   public static Set<TeamDefinitionArtifact> getTeamsFromItemAndChildren(TeamDefinitionArtifact teamDef) throws OseeCoreException {
      Set<TeamDefinitionArtifact> teamDefs = new HashSet<TeamDefinitionArtifact>();
      teamDefs.add(teamDef);
      for (Artifact art : teamDef.getChildren()) {
         if (art instanceof TeamDefinitionArtifact) {
            teamDefs.addAll(getTeamsFromItemAndChildren((TeamDefinitionArtifact) art));
         }
      }
      return teamDefs;
   }

   private static void getTeamFromItemAndChildren(ActionableItemArtifact aia, Set<TeamDefinitionArtifact> aiaTeams) throws OseeCoreException {
      if (aia.getRelatedArtifacts(AtsRelationTypes.TeamActionableItem_Team).size() > 0) {
         aiaTeams.addAll(aia.getRelatedArtifacts(AtsRelationTypes.TeamActionableItem_Team, TeamDefinitionArtifact.class));
      }
      for (Artifact childArt : aia.getChildren()) {
         if (childArt instanceof ActionableItemArtifact) {
            getTeamFromItemAndChildren((ActionableItemArtifact) childArt, aiaTeams);
         }
      }
   }

   public double getManDayHrsFromItemAndChildren() {
      return getHoursPerWorkDayFromItemAndChildren(this);
   }

   public WorkFlowDefinitionMatch getWorkFlowDefinition() throws OseeCoreException {
      Artifact teamDef = getTeamDefinitionHoldingWorkFlow();
      if (teamDef == null) {
         return new WorkFlowDefinitionMatch();
      }
      Artifact workFlowArt = getWorkflowArtifact(teamDef);
      if (workFlowArt == null) {
         return new WorkFlowDefinitionMatch();
      }
      WorkFlowDefinition workDef =
         (WorkFlowDefinition) WorkItemDefinitionFactory.getWorkItemDefinition(workFlowArt.getName());
      return new WorkFlowDefinitionMatch(workDef, String.format("from teamDef [%s] related work child [%s]", teamDef,
         workFlowArt.getName()));

   }

   public Artifact getWorkflowArtifact(Artifact teamDef) throws OseeCoreException {
      Artifact workFlowArt = null;
      for (Artifact artifact : teamDef.getRelatedArtifacts(CoreRelationTypes.WorkItem__Child, Artifact.class)) {
         if (artifact.isOfType(CoreArtifactTypes.WorkFlowDefinition)) {
            if (workFlowArt != null) {
               OseeLog.log(
                  AtsPlugin.class,
                  Level.SEVERE,
                  "Multiple workflows found where only one expected for Team Definition " + getHumanReadableId() + " - " + getName());
            }
            workFlowArt = artifact;
         }
      }
      return workFlowArt;
   }

   public WorkDefinitionMatch getWorkDefinition() throws OseeCoreException {
      Artifact teamDef = getTeamDefinitionHoldingWorkFlow();
      if (teamDef == null) {
         return null;
      }
      Artifact workFlowArt = getWorkflowArtifact(teamDef);
      if (workFlowArt == null) {
         return null;
      }
      WorkDefinitionMatch match = WorkDefinitionFactory.getWorkDefinition(workFlowArt.getName());
      if (match.isMatched()) {
         match.getTrace().add(
            String.format("from teamDef [%s] related work child [%s]", teamDef, workFlowArt.getName()));
      }
      return match;
   }

   /**
    * Return rules associated with team definition . Use StateMachineArtifact.getWorkRulesStartsWith to acquire these
    * and work page rules and workflow rules.
    */
   public Collection<RuleDefinition> getRulesStartsWith(String ruleName) throws OseeCoreException {
      Set<RuleDefinition> workRules = new HashSet<RuleDefinition>();
      if (!Strings.isValid(ruleName)) {
         return workRules;
      }
      // Get work rules from team definition
      for (RuleDefinition ruleDefinition : getWorkRules()) {
         if (!ruleDefinition.getName().equals("") && ruleDefinition.getName().startsWith(ruleName)) {
            workRules.add(ruleDefinition);
         }
      }

      return workRules;
   }

   public Collection<RuleDefinition> getWorkRules() throws OseeCoreException {
      Set<RuleDefinition> workRules = new HashSet<RuleDefinition>();
      // Get work rules from team definition
      for (Artifact art : getRelatedArtifacts(CoreRelationTypes.WorkItem__Child)) {
         if (art.isOfType(CoreArtifactTypes.WorkRuleDefinition)) {
            String id = art.getSoleAttributeValue(CoreAttributeTypes.WorkId, "");
            if (Strings.isValid(id)) {
               // Note: This may skip any complex rules (more than name), but don't think teamdefs have them
               RuleDefinition ruleDef = WorkDefinitionFactory.getRuleById(id.replaceFirst("^ats", ""));
               if (ruleDef == null) {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE, String.format("Null work rule for " + id));
               } else {
                  workRules.add(ruleDef);
               }
            }
         }
      }

      return workRules;
   }

   /**
    * If hours per work day attribute is set, use it, otherwise, walk up the Team Definition tree. Value used in
    * calculations.
    */
   public double getHoursPerWorkDayFromItemAndChildren(TeamDefinitionArtifact teamDef) {
      try {
         Double manDaysHrs = teamDef.getSoleAttributeValue(AtsAttributeTypes.HoursPerWorkDay, 0.0);
         if (manDaysHrs != null && manDaysHrs != 0) {
            return manDaysHrs;
         }
         if (teamDef.getParent() instanceof TeamDefinitionArtifact) {
            return teamDef.getHoursPerWorkDayFromItemAndChildren((TeamDefinitionArtifact) teamDef.getParent());
         }
         return AtsUtil.DEFAULT_HOURS_PER_WORK_DAY;
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return 0.0;
   }

   /**
    * Return ONLY leads configured for this TeamDefinitionArtifact. Depending on the use, like creating new actions, the
    * assignees (or Leads) are determined first from users configured as leads of individual actionable items and only
    * if that returns no leads, THEN default to using the leads configured for the TeamDefinition. In these cases, use
    * getLeads(Collection<ActionableItemArtifact>) instead.
    * 
    * @return users configured as leads for this TeamDefinitionArtifact
    */
   public Collection<User> getLeads() throws OseeCoreException {
      return getRelatedArtifacts(AtsRelationTypes.TeamLead_Lead, User.class);
   }

   public Collection<User> getPrivilegedMembers() throws OseeCoreException {
      return getRelatedArtifacts(AtsRelationTypes.PrivilegedMember_Member, User.class);
   }

   /**
    * Returns leads configured first by ActionableItems and only if this is an empty set, THEN defaults to those
    * configured by TeamDefinitions. Use getLeads() to only get the leads configured for this TeamDefinitionArtifact.
    * 
    * @return users configured as leads by ActionableItems, then by TeamDefinition
    */
   public Collection<User> getLeads(Collection<ActionableItemArtifact> actionableItems) throws OseeCoreException {
      Set<User> leads = new HashSet<User>();
      for (ActionableItemArtifact aia : actionableItems) {
         if (aia.getImpactedTeamDefs().contains(this)) {
            // If leads are specified for this aia, add them
            if (aia.getLeads().size() > 0) {
               leads.addAll(aia.getLeads());
            } else {
               for (TeamDefinitionArtifact teamDef : aia.getImpactedTeamDefs()) {
                  leads.addAll(teamDef.getLeads());
               }
            }
         }
      }
      if (leads.isEmpty()) {
         leads.addAll(getLeads());
      }
      return leads;
   }

   @SuppressWarnings("unchecked")
   public Collection<User> getMembersAndLeads() throws OseeCoreException {
      return Collections.setUnion(getMembers(), getLeads());
   }

   public Collection<User> getMembers() throws OseeCoreException {
      return getRelatedArtifacts(AtsRelationTypes.TeamMember_Member, User.class);
   }

   public Artifact getVersionArtifact(String name, boolean create) throws OseeCoreException {
      for (Artifact verArt : getVersionsArtifacts()) {
         if (verArt.getName().equals(name)) {
            return verArt;
         }
      }
      if (create) {
         return createVersion(name);
      }
      return null;
   }

   public Artifact createVersion(String name) throws OseeCoreException {
      Artifact versionArt = ArtifactTypeManager.addArtifact(AtsArtifactTypes.Version, AtsUtil.getAtsBranch(), name);
      addRelation(AtsRelationTypes.TeamDefinitionToVersion_Version, versionArt);
      return versionArt;
   }

   public Collection<Artifact> getVersionsArtifacts() throws OseeCoreException {
      return getRelatedArtifacts(AtsRelationTypes.TeamDefinitionToVersion_Version);
   }

   public Collection<Artifact> getVersionsArtifacts(VersionReleaseType releaseType, VersionLockedType lockType) throws OseeCoreException {
      return Collections.setIntersection(getVersionsArtifacts(releaseType), getVersionsArtifacts(lockType));
   }

   private Collection<Artifact> getVersionsArtifacts(VersionReleaseType releaseType) throws OseeCoreException {
      ArrayList<Artifact> versions = new ArrayList<Artifact>();
      for (Artifact version : getVersionsArtifacts()) {
         if (VersionManager.isReleased(version) && (releaseType == VersionReleaseType.Released || releaseType == VersionReleaseType.Both)) {
            versions.add(version);
         } else if ((!VersionManager.isReleased(version) && releaseType == VersionReleaseType.UnReleased) || releaseType == VersionReleaseType.Both) {
            versions.add(version);
         }
      }
      return versions;
   }

   private Collection<Artifact> getVersionsArtifacts(VersionLockedType lockType) throws OseeCoreException {
      ArrayList<Artifact> versions = new ArrayList<Artifact>();
      for (Artifact version : getVersionsArtifacts()) {
         if (VersionManager.isVersionLocked(version) && (lockType == VersionLockedType.Locked || lockType == VersionLockedType.Both)) {
            versions.add(version);
         } else if ((!VersionManager.isVersionLocked(version) && lockType == VersionLockedType.UnLocked) || lockType == VersionLockedType.Both) {
            versions.add(version);
         }
      }
      return versions;
   }

   public boolean isTeamUsesVersions() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.TeamUsesVersions, false);
   }

   public boolean isActionable() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.Actionable, false);
   }

   public void addRule(RuleDefinitionOption option) throws OseeCoreException {
      addRule(option.name());
   }

   public void addRule(String ruleId) throws OseeCoreException {
      if (!hasRule(ruleId)) {
         addAttribute(AtsAttributeTypes.RuleDefinition, ruleId);
      }
   }

   public boolean hasRule(RuleDefinitionOption option) throws OseeCoreException {
      return hasRule(option.name());
   }

   public boolean hasRule(String ruleId) throws OseeCoreException {
      return getAttributesToStringList(AtsAttributeTypes.RuleDefinition).contains(ruleId);
   }

   /**
    * Returns the branch associated with this team. If this team does not have a branch associated then the parent team
    * will be asked, this results in a recursive look at parent teams until a parent artifact has a related branch or
    * the parent of a team is not a team. <br/>
    * <br/>
    * If no branch is associated then null will be returned.
    */
   public Branch getTeamBranch() throws OseeCoreException {
      String guid = getSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, null);
      if (GUID.isValid(guid)) {
         return BranchManager.getBranchByGuid(guid);
      } else {
         Artifact parent = getParent();
         if (parent instanceof TeamDefinitionArtifact) {
            return ((TeamDefinitionArtifact) parent).getTeamBranch();
         }
      }
      return null;
   }

   public static Set<TeamDefinitionArtifact> getTeamDefinitions(Collection<String> teamDefNames) {
      Set<TeamDefinitionArtifact> teamDefs = new HashSet<TeamDefinitionArtifact>();
      for (String teamDefName : teamDefNames) {
         for (Artifact artifact : AtsCacheManager.getArtifactsByName(AtsArtifactTypes.TeamDefinition, teamDefName)) {
            teamDefs.add((TeamDefinitionArtifact) artifact);
         }
      }
      return teamDefs;
   }

   @Override
   public String getFullDisplayName() {
      return getName();
   }

}
