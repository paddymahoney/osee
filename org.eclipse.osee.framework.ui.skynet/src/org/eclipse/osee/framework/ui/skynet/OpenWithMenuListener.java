/**
 * 
 */
package org.eclipse.osee.framework.ui.skynet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.listener.IRebuildMenuListener;
import org.eclipse.osee.framework.ui.skynet.render.IRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * @author Ryan D. Brooks
 * @author Jeff C. Phillips
 */

public class OpenWithMenuListener implements MenuListener {
   private Menu parentMenu;
   private Viewer viewer;
   private IRebuildMenuListener rebuildMenuListener;
   private ICommandService commandService;

   public OpenWithMenuListener(Menu parentMenu, final Viewer viewer, IRebuildMenuListener rebuildMenuListener) {
      super();
      this.parentMenu = parentMenu;
      this.viewer = viewer;
      this.rebuildMenuListener = rebuildMenuListener;
      this.commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MenuListener#menuHidden(org.eclipse.swt.events.MenuEvent)
    */
   @Override
   public void menuHidden(MenuEvent e) {
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.MenuListener#menuShown(org.eclipse.swt.events.MenuEvent)
    */
   @Override
   public void menuShown(MenuEvent e) {
      try {
         rebuildMenuListener.rebuildMenu();

         IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
         Iterator<?> iterator = selection.iterator();
         ArrayList<Artifact> artifacts = new ArrayList<Artifact>(selection.size());

         boolean validForPreview = true;
         //load artifacts in the list
         Artifact artifact = null;
         while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof IAdaptable) {
               artifact = (Artifact) ((IAdaptable) object).getAdapter(Artifact.class);
            } else if (object instanceof Match) {
               artifact = (Artifact) ((Match) object).getElement();
            }

            validForPreview &= !artifact.isOfType("Native");
            artifacts.add(artifact);
         }

         if (validForPreview) {
            if (loadMenuItems(PresentationType.PREVIEW, artifacts)) {
               new MenuItem(parentMenu, SWT.SEPARATOR);
            }
         }
         loadMenuItems(PresentationType.SPECIALIZED_EDIT, artifacts);

      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }
   }

   private boolean loadMenuItems(PresentationType presentationType, List<Artifact> artifacts) throws OseeCoreException, NotDefinedException {
      List<IRenderer> commonRenders = RendererManager.getCommonRenderers(artifacts, presentationType);
      Artifact artifact = artifacts.iterator().next();
      boolean hasMenus = false;
      for (IRenderer renderer : commonRenders) {
         for (String commandId : renderer.getCommandId(presentationType)) {
            Command command = commandService.getCommand(commandId);
            if (command != null && command.isEnabled()) {
               hasMenus = true;
               MenuItem menuItem = new MenuItem(parentMenu, SWT.PUSH);
               menuItem.setText(command.getName());
               menuItem.setImage(renderer.getImage(artifact));
               menuItem.addSelectionListener(new OpenWithSelectionListener(command));
            }
         }
      }
      return hasMenus;
   }
}
