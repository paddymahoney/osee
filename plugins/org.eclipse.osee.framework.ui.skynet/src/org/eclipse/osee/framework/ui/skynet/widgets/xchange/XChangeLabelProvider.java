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

package org.eclipse.osee.framework.ui.skynet.widgets.xchange;

import java.util.logging.Level;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.change.ErrorChange;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @author Jeff C. Phillips
 */
public class XChangeLabelProvider extends XViewerLabelProvider {

   Font font = null;
   private final ChangeXViewer changeXViewer;

   public XChangeLabelProvider(ChangeXViewer changeXViewer) {
      super(changeXViewer);
      this.changeXViewer = changeXViewer;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn cCol, int columnIndex) {
      try {
         if (element instanceof String && columnIndex == 0) {
            return String.valueOf(element);
         }

         if (!(element instanceof Change)) {
            return "";
         }
         Change change = (Change) element;
         if (cCol instanceof XViewerValueColumn) {
            return ((XViewerValueColumn) cCol).getColumnText(element, cCol, columnIndex);
         }
         if (cCol.equals(ChangeXViewerFactory.Name)) {
            return change.getName();
         }

         if (change instanceof ErrorChange) {
            return "";
         } else if (cCol.equals(ChangeXViewerFactory.Change_Type)) {
            ChangeItem changeItem = change.getChangeItem();
            if (changeItem != null && changeItem.getIgnoreType().isDeletedOnDestAndNotResurrected()) {
               return ModificationType.DELETED_ON_DESTINATION.getDisplayName();
            } else {
               return change.getModificationType().getDisplayName();
            }
         } else if (cCol.equals(ChangeXViewerFactory.Item_Kind)) {
            return change.getItemKind();
         } else if (cCol.equals(ChangeXViewerFactory.Item_Type)) {
            return change.getItemTypeName();
         } else if (cCol.equals(ChangeXViewerFactory.Is_Value)) {
            return change.getIsValue();
         } else if (cCol.equals(ChangeXViewerFactory.Was_Value)) {
            return change.getWasValue();
         } else if (cCol.equals(ChangeXViewerFactory.paraNumber)) {
            String paragraphNum = "";
            Artifact artifact = change.getChangeArtifact();
            if (artifact.isAttributeTypeValid(CoreAttributeTypes.ParagraphNumber)) {
               paragraphNum = artifact.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "");
            }
            return paragraphNum;
         }
      } catch (Exception ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
      return "unhandled column";
   }

   @Override
   public void dispose() {
      if (font != null) {
         font.dispose();
      }
      font = null;
   }

   @Override
   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   @Override
   public void addListener(ILabelProviderListener listener) {
      // do nothing
   }

   @Override
   public void removeListener(ILabelProviderListener listener) {
      // do nothing
   }

   public ChangeXViewer getTreeViewer() {
      return changeXViewer;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         if (!(element instanceof Change)) {
            return null;
         }
         Change change = (Change) element;
         if (xCol.equals(ChangeXViewerFactory.Name)) {
            if (change instanceof ErrorChange) {
               return ImageManager.getImage(FrameworkImage.ERROR);
            } else {
               return ArtifactImageManager.getChangeKindImage(change);
            }
         } else if (xCol.equals(ChangeXViewerFactory.Item_Type)) {
            return ArtifactImageManager.getChangeTypeImage(change);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }
}
