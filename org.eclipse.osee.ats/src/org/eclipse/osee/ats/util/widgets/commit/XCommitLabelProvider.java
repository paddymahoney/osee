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
package org.eclipse.osee.ats.util.widgets.commit;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.artifact.VersionArtifact;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class XCommitLabelProvider extends XViewerLabelProvider {
   Font font = null;

   private final CommitXManager commitXManager;
   public static enum CommitStatus {
      Working_Branch_Not_Created("Working Branch Not Created"),
      Branch_Not_Configured("Branch Not Configured"),
      Branch_Commit_Disabled("Branch Commit Disabled"),
      Commit_Needed("Start Commit"),
      Merge_In_Progress("Merge in Progress"),
      Commit_Needed_After_Merge("Finish Commit"),
      Committed("Committed"),
      Committed_With_Merge("Committed With Merge");

      private final String displayName;

      private CommitStatus(String displayName) {
         this.displayName = displayName;
      }

      /**
       * @return the displayName
       */
      public String getDisplayName() {
         return displayName;
      }
   };

   public XCommitLabelProvider(CommitXManager commitXManager) {
      super(commitXManager);
      this.commitXManager = commitXManager;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      ICommitConfigArtifact configArt = (ICommitConfigArtifact) element;
      Branch branch = configArt.getParentBranch();
      if (xCol.equals(CommitXManagerFactory.Action_Col)) {
         return SkynetGuiPlugin.getInstance().getImage("nav_forward.gif");
      }
      if (branch == null) return null;
      if (xCol.equals(CommitXManagerFactory.Status_Col)) {
         try {
            CommitStatus commitStatus = getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt);
            if (commitStatus == CommitStatus.Branch_Not_Configured ||
            //
            commitStatus == CommitStatus.Branch_Commit_Disabled ||
            //
            commitStatus == CommitStatus.Commit_Needed ||
            //
            commitStatus == CommitStatus.Working_Branch_Not_Created) {
               return SkynetGuiPlugin.getInstance().getImage("red_light.gif");
            }

            if (commitStatus == CommitStatus.Merge_In_Progress ||
            //
            commitStatus == CommitStatus.Commit_Needed_After_Merge) {
               return SkynetGuiPlugin.getInstance().getImage("yellow_light.gif");
            }

            if (commitStatus == CommitStatus.Committed ||
            //
            commitStatus == CommitStatus.Committed_With_Merge) {
               return SkynetGuiPlugin.getInstance().getImage("green_light.gif");
            }
            return null;
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      } else if (xCol.equals(CommitXManagerFactory.Merge_Col)) {
         try {
            CommitStatus commitStatus = getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt);
            if (commitStatus == CommitStatus.Merge_In_Progress || commitStatus == CommitStatus.Commit_Needed_After_Merge || commitStatus == CommitStatus.Committed_With_Merge) {
               return SkynetGuiPlugin.getInstance().getImage("branch_merge.gif");
            }
            return null;
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }
      return null;
   }

   public static CommitStatus getCommitStatus(TeamWorkFlowArtifact teamArt, ICommitConfigArtifact configArt) throws OseeCoreException {
      Branch branch = configArt.getParentBranch();
      if (branch == null) return CommitStatus.Branch_Not_Configured;

      Set<Branch> branches = BranchManager.getAssociatedArtifactBranches(teamArt, false);
      if (branches.contains(branch)) {
         return CommitStatus.Committed;
      }
      Collection<TransactionId> transactions = TransactionIdManager.getCommittedArtifactTransactionIds(teamArt);
      for (TransactionId transId : transactions) {
         if (transId.getBranchId() == branch.getBranchId()) {
            if (teamArt.getSmaMgr().getBranchMgr().isMergeBranchExists(branch)) {
               return CommitStatus.Committed_With_Merge;
            } else {
               return CommitStatus.Committed;
            }
         }
      }

      Result result = teamArt.getSmaMgr().getBranchMgr().isCommitBranchAllowed(configArt);
      if (result.isFalse()) {
         return CommitStatus.Branch_Commit_Disabled;
      }
      if (teamArt.getSmaMgr().getBranchMgr().getWorkingBranch(true) == null) {
         return CommitStatus.Working_Branch_Not_Created;
      }
      if (teamArt.getSmaMgr().getBranchMgr().isMergeBranchExists(branch)) {
         if (teamArt.getSmaMgr().getBranchMgr().isMergeCompleted(branch)) {
            return CommitStatus.Commit_Needed_After_Merge;
         }
         return CommitStatus.Merge_In_Progress;
      }
      return CommitStatus.Commit_Needed;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      ICommitConfigArtifact configArt = (ICommitConfigArtifact) element;
      Branch branch = configArt.getParentBranch();

      if (xCol.equals(CommitXManagerFactory.Status_Col)) {
         return getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt).getDisplayName();
      } else if (xCol.equals(CommitXManagerFactory.Merge_Col)) {
         return "";
      } else if (xCol.equals(CommitXManagerFactory.Version_Col)) {
         return ((Artifact) element).getDescriptiveName();
      } else if (xCol.equals(CommitXManagerFactory.Configuring_Object_Col)) {
         return ((Artifact) element).getArtifactTypeName();
      } else if (xCol.equals(CommitXManagerFactory.Dest_Branch_Col)) {
         if (element instanceof VersionArtifact) {
            return (branch == null ? "Parent Branch Not Configured for Version [" + (element) + "]" : branch.getBranchShortName());
         } else if (element instanceof TeamDefinitionArtifact) {
            return (branch == null ? "Parent Branch Not Configured for Team Definition [" + (element) + "]" : branch.getBranchShortName());
         }
      } else if (xCol.equals(CommitXManagerFactory.Action_Col)) {
         CommitStatus commitStatus = getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt);
         if (commitStatus == CommitStatus.Branch_Not_Configured)
            return "Configure Branch";
         else if (commitStatus == CommitStatus.Branch_Commit_Disabled)
            return "Enable Branch Commit";
         else if (commitStatus == CommitStatus.Commit_Needed)
            return "Start Commit";
         else if (commitStatus == CommitStatus.Merge_In_Progress)
            return "Merge Conflicts";
         else if (commitStatus == CommitStatus.Commit_Needed_After_Merge)
            return "Finish Commit";
         else if (commitStatus == CommitStatus.Committed)
            return "Show Change Report";
         else if (commitStatus == CommitStatus.Committed_With_Merge)
            return "Show Change/Merge Report";
         else if (commitStatus == CommitStatus.Working_Branch_Not_Created) return "Working Branch Not Created";
         return "Error: Need to handle this";
      }
      return "unhandled column";
   }

   public void dispose() {
      if (font != null) font.dispose();
      font = null;
   }

   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   public void addListener(ILabelProviderListener listener) {
   }

   public void removeListener(ILabelProviderListener listener) {
   }

   public CommitXManager getTreeViewer() {
      return commitXManager;
   }

}
