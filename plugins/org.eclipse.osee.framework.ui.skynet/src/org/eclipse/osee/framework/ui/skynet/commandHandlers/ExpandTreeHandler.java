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

package org.eclipse.osee.framework.ui.skynet.commandHandlers;

import java.util.Iterator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.framework.ui.plugin.util.CommandHandler;

/**
 * @author Theron Virgin
 */
public class ExpandTreeHandler extends CommandHandler {
   private TreeViewer treeViewer;

   @Override
   protected boolean isEnabledWithException(IStructuredSelection structuredSelection) {
      ISelectionProvider selectionProvider = getSelectionProvider();
      treeViewer = selectionProvider instanceof TreeViewer ? (TreeViewer) selectionProvider : null;

      return treeViewer != null && structuredSelection != null;
   }

   @Override
   protected Object executeWithException(ExecutionEvent event, IStructuredSelection selection) {
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         treeViewer.expandToLevel(iter.next(), AbstractTreeViewer.ALL_LEVELS);
      }
      return null;
   }
}
