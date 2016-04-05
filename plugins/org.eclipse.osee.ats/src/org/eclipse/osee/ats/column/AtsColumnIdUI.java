/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.column.AtsColumnIdValueColumn;
import org.eclipse.osee.ats.api.config.ColumnAlign;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class AtsColumnIdUI extends XViewerAtsColumn implements IXViewerValueColumn {

   private final AtsColumnIdValueColumn columnIdColumn;
   private final IAtsServices services;

   public AtsColumnIdUI(AtsColumnIdValueColumn columnIdColumn, IAtsServices services) {
      super(columnIdColumn.getId(), columnIdColumn.getName(), columnIdColumn.getWidth(),
         getSwtAlign(columnIdColumn.getAlign()), columnIdColumn.isVisible(),
         SortDataType.valueOf(columnIdColumn.getSortDataType()), columnIdColumn.isColumnMultiEdit(),
         columnIdColumn.getDescription());
      this.columnIdColumn = columnIdColumn;
      this.services = services;
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public AtsColumnIdUI copy() {
      AtsColumnIdUI newXCol = new AtsColumnIdUI(columnIdColumn, services);
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      String result = "";
      if (element instanceof IAtsObject) {
         result = services.getColumnService().getColumnText(columnIdColumn.getColumnId(), (IAtsObject) element);
      }
      return result;
   }

   public static int getSwtAlign(ColumnAlign align) {
      if (align == ColumnAlign.Left) {
         return SWT.LEFT;
      }
      if (align == ColumnAlign.Center) {
         return SWT.CENTER;
      }
      if (align == ColumnAlign.Right) {
         return SWT.RIGHT;
      }
      return SWT.LEFT;
   }

}