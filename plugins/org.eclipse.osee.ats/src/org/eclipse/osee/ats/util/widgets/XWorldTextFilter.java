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
package org.eclipse.osee.ats.util.widgets;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerTextFilter;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;

/**
 * @author Megumi Telles
 */
public class XWorldTextFilter extends XViewerTextFilter {

   public XWorldTextFilter(XViewer xViewer) {
      super(xViewer);
   }

   @Override
   public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (parentElement instanceof Artifact) {
         Artifact parent = (Artifact) parentElement;
         if (element instanceof Artifact) {
            Artifact elem = (Artifact) element;
            for (RelationLink relation : parent.getRelationsAll(DeletionFlag.EXCLUDE_DELETED)) {
               // BArtifact of parentElement
               if (relation.getBArtifactId() == elem.getArtId()) {
                  return true;
               }
            }
         }
      }
      return super.select(viewer, parentElement, element);
   }
}
