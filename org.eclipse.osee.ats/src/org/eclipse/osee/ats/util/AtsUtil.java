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

package org.eclipse.osee.ats.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.actions.NewAction;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.ats.artifact.ActionArtifact;
import org.eclipse.osee.ats.artifact.GoalArtifact;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkflowLabelProvider;
import org.eclipse.osee.ats.artifact.VersionArtifact;
import org.eclipse.osee.ats.config.AtsBulkLoadCache;
import org.eclipse.osee.ats.config.AtsCacheManager;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.task.TaskEditor;
import org.eclipse.osee.ats.task.TaskEditorSimpleProvider;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.ats.world.WorldEditorSimpleProvider;
import org.eclipse.osee.ats.world.WorldEditorUISearchItemProvider;
import org.eclipse.osee.ats.world.search.GroupWorldSearchItem;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.jdk.core.util.AFile;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.OseeGroup;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.UniversalGroup;
import org.eclipse.osee.framework.skynet.core.artifact.search.Active;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;
import org.eclipse.osee.framework.skynet.core.utility.OseeData;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.ats.AtsOpenOption;
import org.eclipse.osee.framework.ui.skynet.ats.IAtsLib;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.osee.framework.ui.skynet.widgets.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * @author Donald G. Dunne
 */
public class AtsUtil implements IAtsLib {

   private static int atsDevNum;
   private static boolean emailEnabled = true;
   public static Color ACTIVE_COLOR = new Color(null, 206, 212, 241);
   private static OseeGroup atsAdminGroup = null;
   private static boolean goalEnabled = false;

   public AtsUtil() {
      super();
   }

   public static boolean isProductionDb() throws OseeCoreException {
      return ClientSessionManager.isProductionDataStore();
   }

   public boolean isAdmin() {
      return AtsUtil.isAtsAdmin();
   }

   public static boolean isAtsAdmin() {
      try {
         return getAtsAdminGroup().isCurrentUserMember();
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         return false;
      }
   }

   /**
    * @return the enableGoal
    */
   public static boolean isGoalEnabled() {
      try {
         if (ArtifactTypeManager.getType(GoalArtifact.ARTIFACT_NAME) != null) {
            return goalEnabled;
         }
      } catch (OseeTypeDoesNotExist ex) {
         // do nothing
      } catch (OseeDataStoreException ex) {
         // do nothing
      }
      return false;
   }

   public static OseeGroup getAtsAdminGroup() {
      if (atsAdminGroup == null) {
         atsAdminGroup = new OseeGroup("AtsAdmin");
      }
      return atsAdminGroup;
   }

   public static Branch getAtsBranch() throws OseeCoreException {
      return BranchManager.getCommonBranch();
   }

   public static boolean isEmailEnabled() {
      return emailEnabled;
   }

   public static void setEmailEnabled(boolean enabled) {
      if (!DbUtil.isDbInit()) System.out.println("Email " + (enabled ? "Enabled" : "Disabled"));
      emailEnabled = enabled;
   }

   public static Composite createCommonPageComposite(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(1, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      layout.verticalSpacing = 0;
      composite.setLayout(layout);

      return composite;
   }

   /**
    * The development of ATS requires quite a few Actions to be created. To facilitate this, getTTNum will retrieve a
    * persistent number from the filesystem so each action has a different name. By entering "tt" in the title, new
    * action wizard will be prepopulated with selections and the action name will be created as "tt <number in
    * atsNumFilename>".
    * 
    * @return number
    * @throws IOException
    */
   public static int getAtsDeveloperIncrementingNum() {
      try {
         File numFile = OseeData.getFile("atsDevNum.txt");
         if (numFile.exists() && atsDevNum == 0) {
            try {
               atsDevNum = new Integer(AFile.readFile(numFile).replaceAll("\\s", ""));
            } catch (NumberFormatException ex) {
            } catch (NullPointerException ex) {
            }
         }
         atsDevNum++;
         Lib.writeStringToFile(String.valueOf(atsDevNum), numFile);
         return atsDevNum;
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return 99;
   }

   public static ToolBar createCommonToolBar(Composite parent) {
      return createCommonToolBar(parent, null);
   }

   public static ToolBar createCommonToolBar(Composite parent, XFormToolkit toolkit) {
      ToolBar toolBar = ALayout.createCommonToolBar(parent);
      if (toolkit != null) toolkit.adapt(toolBar.getParent());
      if (toolkit != null) toolkit.adapt(toolBar);
      return toolBar;
   }

   /**
    * @param <A>
    * @param artifacts to iterate through
    * @param active state to validate against; Both will return all artifacts matching type
    * @param clazz type of artifacts to consider
    * @return set of Artifacts of type clazz that match the given active state of the "Active" attribute value. If no
    *         attribute exists, Active == true; If does exist then attribute value "yes" == true, "no" == false.
    */
   @SuppressWarnings("unchecked")
   public static <A extends Artifact> Set<A> getActiveSet(Collection<A> artifacts, Active active, Class<? extends Artifact> clazz) throws OseeCoreException {
      Set<A> results = new HashSet<A>();
      for (Artifact art : artifacts) {
         if ((art.getClass().equals(clazz)) && art.isAttributeTypeValid(ATSAttributes.ACTIVE_ATTRIBUTE.getStoreName())) {
            if (active == Active.Both)
               results.add((A) art);
            else {
               // Ats config Artifact is Active unless otherwise specified
               boolean attributeActive =
                     ((A) art).getSoleAttributeValue(ATSAttributes.ACTIVE_ATTRIBUTE.getStoreName(), false);
               if (active == Active.Active && attributeActive) {
                  results.add((A) art);
               } else if (active == Active.InActive && !attributeActive) {
                  results.add((A) art);
               }
            }
         }
      }
      return results;
   }

   public static String doubleToStrString(double d) {
      return doubleToStrString(d, false);
   }

   public static String doubleToStrString(double d, boolean blankIfZero) {
      if (blankIfZero && d == 0)
         return "";
      else
         return String.format("%4.2f", d);
   }

   public static void editActionableItems(ActionArtifact actionArt) throws OseeCoreException {
      Result result = actionArt.editActionableItems();
      if (result.isFalse() && result.getText().equals("")) return;
      if (result.isFalse()) result.popup();
   }

   public static void editActionableItems(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      Result result = teamArt.editActionableItems();
      if (result.isFalse() && result.getText().equals("")) return;
      if (result.isFalse() && !result.getText().equals("")) result.popup();
   }

   public static void open(String guid, OseeAts.OpenView view) {
      (new AtsUtil()).openArtifact(guid, view);
   }

   public void openArtifact(String guidOrHrid, Integer branchId, OseeAts.OpenView view) {
      try {
         Branch branch = BranchManager.getBranch(branchId);
         Artifact artifact = ArtifactQuery.getArtifactFromId(guidOrHrid, branch);
         openAtsAction(artifact, AtsOpenOption.OpenOneOrPopupSelect);
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   /**
    * Only to be used by browser. Use open (artifact) instead.
    * 
    * @param guid
    */
   public void openArtifact(String guid, OseeAts.OpenView view) {
      AtsBulkLoadCache.run(false);
      Artifact artifact = null;
      try {
         artifact = ArtifactQuery.getArtifactFromId(guid, getAtsBranch());
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         return;
      }

      if (view == OseeAts.OpenView.ActionEditor) {
         if ((artifact instanceof StateMachineArtifact) || (artifact instanceof ActionArtifact))
            openATSAction(artifact, AtsOpenOption.OpenOneOrPopupSelect);
         else
            ArtifactEditor.editArtifact(artifact);
      } else if (view == OseeAts.OpenView.ArtifactEditor) {
         ArtifactEditor.editArtifact(artifact);
      } else if (view == OseeAts.OpenView.ArtifactHyperViewer) {
         AWorkbench.popup("ERROR", "Unimplemented");
      }
   }

   public static void createAtsAction(String initialDescription, String actionableItem) {
      (new AtsUtil()).createATSAction(initialDescription, actionableItem);
   }

   public void createATSAction(String initialDescription, String actionableItemName) {
      // Ensure actionable item is configured for ATS before continuing
      try {
         AtsCacheManager.getSoleArtifactByName(actionableItemName, VersionArtifact.class);
      } catch (ArtifactDoesNotExist ex) {
         AWorkbench.popup(
               "Configuration Error",
               "Actionable Item \"" + actionableItemName + "\" is not configured for ATS tracking.\n\nAction can not be created.");
         return;
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         return;
      }

      NewAction newAction = new NewAction(actionableItemName);
      newAction.setInitialDescription(initialDescription);
      newAction.run();
   }

   public static void openAtsAction(final Artifact art, final AtsOpenOption option) {
      (new AtsUtil()).openATSAction(art, option);
   }

   public void openATSAction(final Artifact art, final AtsOpenOption option) {
      try {
         if (art instanceof ActionArtifact) {
            final ActionArtifact actionArt = (ActionArtifact) art;
            Collection<TeamWorkFlowArtifact> teams = actionArt.getTeamWorkFlowArtifacts();
            if (option == AtsOpenOption.OpenAll)
               for (TeamWorkFlowArtifact team : teams)
                  SMAEditor.editArtifact(team);
            else if (option == AtsOpenOption.AtsWorld)
               WorldEditor.open(new WorldEditorSimpleProvider("Action " + actionArt.getHumanReadableId(),
                     Arrays.asList(actionArt)));
            else if (option == AtsOpenOption.OpenOneOrPopupSelect) {
               if (teams.size() == 1)
                  SMAEditor.editArtifact(teams.iterator().next());
               else {
                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        try {
                           TeamWorkFlowArtifact teamArt = promptSelectTeamWorkflow(actionArt);
                           if (teamArt != null)
                              SMAEditor.editArtifact((Artifact) teamArt);
                           else
                              return;
                        } catch (OseeCoreException ex) {
                           OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
                        }
                     }
                  });
               }
            }
         } else
            SMAEditor.editArtifact(art);
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public static TeamWorkFlowArtifact promptSelectTeamWorkflow(ActionArtifact actArt) throws OseeCoreException {
      ListDialog ld = new ListDialog(Display.getCurrent().getActiveShell());
      ld.setContentProvider(new ArrayContentProvider());
      ld.setLabelProvider(new TeamWorkflowLabelProvider());
      ld.setTitle("Select Team Workflow");
      ld.setMessage("Select Team Workflow");
      ld.setInput(actArt.getTeamWorkFlowArtifacts());
      if (ld.open() == 0) {
         if (ld.getResult().length == 0)
            AWorkbench.popup("Error", "No Workflow Selected");
         else
            return (TeamWorkFlowArtifact) ld.getResult()[0];
      }
      return null;
   }

   @Override
   public void openInAtsWorldEditor(String name, Collection<Artifact> artifacts) throws OseeCoreException {
      Set<Artifact> otherArts = new HashSet<Artifact>();
      for (Artifact art : artifacts) {
         if (art.getArtifactTypeName().equals(UniversalGroup.ARTIFACT_TYPE_NAME)) {
            WorldEditor.open(new WorldEditorUISearchItemProvider(new GroupWorldSearchItem(art), null,
                  TableLoadOption.None));
         } else
            otherArts.add(art);
      }
      if (otherArts.size() > 0) WorldEditor.open(new WorldEditorSimpleProvider(name, otherArts));
   }

   @Override
   public void openInAtsTaskEditor(String name, Collection<Artifact> artifacts) throws OseeCoreException {
      TaskEditor.open(new TaskEditorSimpleProvider(name, artifacts));
   }

}
