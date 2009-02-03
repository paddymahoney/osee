/*******************************************************************************
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

package org.eclipse.osee.framework.ui.skynet.widgets.xHistory;

import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osee.framework.db.connection.exception.OseeArgumentException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.event.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.plugin.util.Jobs;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.util.SkynetViews;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * Displays persisted changes made to an artifact.
 * 
 * @author Jeff C. Phillips
 */
public class HistoryView extends ViewPart implements IActionable, IBranchEventListener {

   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.widgets.xHistory.HistoryView";
   private static String HELP_CONTEXT_ID = "HistoryView";
   private XHistoryWidget xHistoryWidget;
   private Artifact artifact;

   public HistoryView() {
   }

   public static void open(Artifact artifact) throws OseeArgumentException {
      if (artifact == null) throw new OseeArgumentException("Artifact can't be null");
      HistoryView.openViewUpon(artifact, true);
   }

   private static void openViewUpon(final Artifact artifact, final Boolean loadHistory) {
      Job job = new Job("Open History: " + artifact.getDescriptiveName()) {

         @Override
         protected IStatus run(final IProgressMonitor monitor) {
            Displays.ensureInDisplayThread(new Runnable() {
               public void run() {
                  try {
                     IWorkbenchPage page = AWorkbench.getActivePage();
                     HistoryView historyView =
                           (HistoryView) page.showView(VIEW_ID, artifact.getGuid(), IWorkbenchPage.VIEW_VISIBLE);

                     historyView.explore(artifact, loadHistory);
                  } catch (Exception ex) {
                     OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
                  }
               }
            });
            monitor.done();
            return Status.OK_STATUS;
         }
      };

      Jobs.startJob(job);
   }

   @Override
   public void dispose() {
      super.dispose();
   }

   @Override
   public void setFocus() {
   }

   /*
    * @see IWorkbenchPart#createPartControl(Composite)
    */
   @Override
   public void createPartControl(Composite parent) {
      /*
       * Create a grid layout object so the text and treeviewer are layed out the way I want.
       */
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      parent.setLayout(layout);
      parent.setLayoutData(new GridData(GridData.FILL_BOTH));

      xHistoryWidget = new XHistoryWidget();
      xHistoryWidget.setDisplayLabel(false);
      xHistoryWidget.createWidgets(parent, 1);

      MenuManager menuManager = new MenuManager();
      menuManager.setRemoveAllWhenShown(true);
      menuManager.addMenuListener(new IMenuListener() {
         public void menuAboutToShow(IMenuManager manager) {
            MenuManager menuManager = (MenuManager) manager;
            menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
         }
      });

      menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      xHistoryWidget.getXViewer().getTree().setMenu(
            menuManager.createContextMenu(xHistoryWidget.getXViewer().getTree()));
      getSite().registerContextMenu(VIEW_ID, menuManager, xHistoryWidget.getXViewer());

      getSite().setSelectionProvider(xHistoryWidget.getXViewer());
      SkynetGuiPlugin.getInstance().setHelp(parent, HELP_CONTEXT_ID);
   }

   private void explore(final Artifact artifact, boolean loadHistory) {
      if (xHistoryWidget != null) {
         this.artifact = artifact;

         setPartName("History: " + artifact.getDescriptiveName());
         xHistoryWidget.setInputData(artifact, loadHistory);
      }
   }

   public String getActionDescription() {
      return "";
   }

   private static final String INPUT = "input";
   private static final String ART_GUID = "artifactGuid";
   private static final String BRANCH_ID = "branchId";

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
    */
   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      memento = memento.createChild(INPUT);
      if (artifact != null) {
         memento.putString(ART_GUID, artifact.getGuid());
         memento.putInteger(BRANCH_ID, artifact.getBranch().getBranchId());
         SkynetViews.addDatabaseSourceId(memento);
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      try {
         if (memento != null) {
            memento = memento.getChild(INPUT);
            if (memento != null) {
               if (SkynetViews.isSourceValid(memento)) {
                  String guid = memento.getString(ART_GUID);
                  Integer branchId = memento.getInteger(BRANCH_ID);
                  Artifact artifact = ArtifactQuery.getArtifactFromId(guid, BranchManager.getBranch(branchId));
                  openViewUpon(artifact, false);
               } else {
                  closeView();
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.WARNING, "History View error on init", ex);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.eventx.IBranchEventListener#handleBranchEvent(org.eclipse.osee.framework.ui.plugin.event.Sender, org.eclipse.osee.framework.skynet.core.artifact.BranchModType, int)
    */
   @Override
   public void handleBranchEvent(Sender sender, BranchEventType branchModType, final int branchId) {
      if (branchModType == BranchEventType.Deleted) {
         Displays.ensureInDisplayThread(new Runnable() {
            public void run() {
               closeView();
            }
         });
         return;
      } else if (branchModType == BranchEventType.Committed) {
         Displays.ensureInDisplayThread(new Runnable() {
            public void run() {
               try {
                  explore(artifact, true);
               } catch (Exception ex) {
                  OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         });
         // refresh view with new branch and transaction id
      } else if (branchModType == BranchEventType.DefaultBranchChanged) {
         Displays.ensureInDisplayThread(new Runnable() {
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
               if (xHistoryWidget == null || xHistoryWidget.getXViewer().getTree() == null || xHistoryWidget.getXViewer().getTree().isDisposed()) return;
               xHistoryWidget.getXViewer().getTree().setEnabled(artifact.getBranch().getBranchId() == branchId);
            }
         });
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.eventx.IBranchEventListener#handleLocalBranchToArtifactCacheUpdateEvent(org.eclipse.osee.framework.ui.plugin.event.Sender)
    */
   @Override
   public void handleLocalBranchToArtifactCacheUpdateEvent(Sender sender) {
   }

   private void closeView() {
      SkynetViews.closeView(VIEW_ID, getViewSite().getSecondaryId());
   }
}