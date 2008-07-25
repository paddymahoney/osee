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
package org.eclipse.osee.ats.util.widgets.task;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerSorter;

/**
 * @author Donald G. Dunne
 */
public class TaskXViewerFactory extends XViewerFactory {

   public static final List<XViewerColumn> TaskViewerVisibleColumns =
         Arrays.asList(WorldXViewerFactory.Title_Col, WorldXViewerFactory.State_Col, WorldXViewerFactory.Assignees_Col,
               WorldXViewerFactory.Percent_Complete_Total_Col, WorldXViewerFactory.Total_Hours_Spent_Col,
               WorldXViewerFactory.Resolution_Col, WorldXViewerFactory.Estimated_Hours_Col,
               WorldXViewerFactory.Remaining_Hours_Col, WorldXViewerFactory.Related_To_State_Col,
               WorldXViewerFactory.Notes_Col);
   public static List<Integer> widths = Arrays.asList(450, 60, 150, 40, 40, 100, 50, 50, 50, 80, 80);

   public TaskXViewerFactory() {
      super("org.eclipse.osee.ats.TaskXViewer");
      int widthIndex = 0;
      // Create new column from world columns but set show and width for task
      for (XViewerColumn taskCol : TaskViewerVisibleColumns) {
         XViewerColumn newCol = taskCol.copy();
         newCol.setShow(true);
         newCol.setWidth(widths.get(widthIndex++));
         registerColumn(newCol);
      }
      // Add remaining columns from world columns
      for (XViewerColumn worldCol : WorldXViewerFactory.WorldViewColumns) {
         if (!TaskViewerVisibleColumns.contains(worldCol)) {
            XViewerColumn newCol = worldCol.copy();
            newCol.setShow(false);
            newCol.setWidth(0);
            registerColumn(newCol);
         }
      }
   }

   public XViewerSorter createNewXSorter(XViewer xViewer) {
      return new TaskXViewerSorter(xViewer);
   }

}
