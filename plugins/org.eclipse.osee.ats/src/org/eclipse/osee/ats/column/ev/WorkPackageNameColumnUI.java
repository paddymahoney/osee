/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column.ev;

import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.osee.ats.core.column.AtsColumnId;

/**
 * @author Donald G. Dunne
 */
public class WorkPackageNameColumnUI extends AbstractWorkPackageRelatedColumnUI {

   private static WorkPackageNameColumnUI instance = new WorkPackageNameColumnUI();

   public static WorkPackageNameColumnUI getInstance() {
      return instance;
   }

   private WorkPackageNameColumnUI() {
      super(AtsColumnId.WorkPackageName, AtsColumnId.WorkPackageName.getId(), "Work Package Name", 80, XViewerAlign.Left, false,
         SortDataType.String, true,
         "Provides Work Package Name from the selected Work Package related to the selected workflow.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public WorkPackageNameColumnUI copy() {
      WorkPackageNameColumnUI newXCol = new WorkPackageNameColumnUI();
      super.copy(this, newXCol);
      return newXCol;
   }

}
