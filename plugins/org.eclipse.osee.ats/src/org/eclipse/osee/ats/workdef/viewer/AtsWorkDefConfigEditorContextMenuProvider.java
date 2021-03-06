/*******************************************************************************
 * Copyright (c) 2004, 2005 Donald G. Dunne and others.
�* All rights reserved. This program and the accompanying materials
�* are made available under the terms of the Eclipse Public License v1.0
�* which accompanies this distribution, and is available at
�* http://www.eclipse.org/legal/epl-v10.html
�*
�* Contributors:
�*����Donald G. Dunne - initial API and implementation
�*******************************************************************************/
package org.eclipse.osee.ats.workdef.viewer;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;

/**
 * Provides context menu actions for the ShapesEditor.
 * 
 * @author Donald G. Dunne
 */
class AtsWorkDefConfigEditorContextMenuProvider extends ContextMenuProvider {

   /** The editor's action registry. */
   //   private final ActionRegistry actionRegistry;

   /**
    * Instantiate a new menu context provider for the specified EditPartViewer and ActionRegistry.
    * 
    * @param viewer the editor's graphical viewer
    * @param registry the editor's action registry
    * @throws OseeArgumentException if registry is <tt>null</tt>.
    */
   public AtsWorkDefConfigEditorContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
      super(viewer);
      if (registry == null) {
         throw new IllegalArgumentException();
      }
   }

   /**
    * Called when the context menu is about to show. Actions, whose state is enabled, will appear in the context menu.
    * 
    * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
    */
   @Override
   public void buildContextMenu(IMenuManager menu) {
      // Add standard action groups to the menu
      GEFActionConstants.addStandardActionGroups(menu);

      // Add actions to the menu
      //      menu.appendToGroup(GEFActionConstants.GROUP_EDIT, getAction(EditAction.ID));
      //      menu.appendToGroup(GEFActionConstants.GROUP_UNDO, // target group id
      //         getAction(ActionFactory.UNDO.getId())); // action to add
      //      menu.appendToGroup(GEFActionConstants.GROUP_UNDO, getAction(ActionFactory.REDO.getId()));
      //      menu.appendToGroup(GEFActionConstants.GROUP_EDIT, getAction(ActionFactory.DELETE.getId()));
   }

   //   private IAction getAction(String actionId) {
   //      return actionRegistry.getAction(actionId);
   //   }

}
