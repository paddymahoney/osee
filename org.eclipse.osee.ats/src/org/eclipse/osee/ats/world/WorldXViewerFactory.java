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
package org.eclipse.osee.ats.world;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeColumn;
import org.eclipse.osee.ats.util.xviewer.column.XViewerSmaCreatedDateColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.SkynetXViewerFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column.XViewerArtifactNameColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column.XViewerHridColumn;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class WorldXViewerFactory extends SkynetXViewerFactory {

   public static String COLUMN_NAMESPACE = "ats.column";
   public static final XViewerColumn Type_Col =
         new XViewerColumn("ats.column.type", "Type", 150, SWT.LEFT, true, SortDataType.String, false, null);
   public static final XViewerColumn State_Col =
         new XViewerColumn("ats.column.state", "State", 75, SWT.LEFT, true, SortDataType.String, false, null);
   public static final XViewerColumn Priority_Col =
         new XViewerAtsAttributeColumn(ATSAttributes.PRIORITY_TYPE_ATTRIBUTE, 20, SWT.CENTER, true,
               SortDataType.String, false, null);
   public static final XViewerColumn Change_Type_Col =
         new XViewerAtsAttributeColumn(ATSAttributes.CHANGE_TYPE_ATTRIBUTE, 22, SWT.CENTER, true, SortDataType.String,
               false, null);
   public static final XViewerColumn Assignees_Col =
         new XViewerColumn(WorldXViewerFactory.COLUMN_NAMESPACE + ".assignees", "Assignees", 100, SWT.LEFT, true,
               SortDataType.String, false, null);
   public static final XViewerColumn Title_Col = new XViewerArtifactNameColumn("Title", null);
   public static final XViewerColumn Actionable_Items_Col =
         new XViewerAtsAttributeColumn(ATSAttributes.ACTIONABLE_ITEM_GUID_ATTRIBUTE, 80, SWT.LEFT, true,
               SortDataType.String, false, "Actionable Items that are impacted by this change.");
   public static final XViewerColumn User_Community_Col =
         new XViewerAtsAttributeColumn(ATSAttributes.USER_COMMUNITY_ATTRIBUTE, 60, SWT.LEFT, true, SortDataType.String,
               false, null);
   public static final XViewerColumn ID_Col = new XViewerHridColumn("HRID", null);
   public static final XViewerColumn Created_Date_Col = new XViewerSmaCreatedDateColumn(null);
   public static final XViewerColumn Version_Target_Col =
         new XViewerColumn(WorldXViewerFactory.COLUMN_NAMESPACE + ".versionTarget", "Version Target", 40, SWT.LEFT,
               true, SortDataType.String, false, "Date this workflow transitioned to the Completed state.");
   public static final XViewerColumn Team_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".team", "Team", 50, SWT.LEFT, true, SortDataType.String, false,
               "Team that has been assigned to work this Action.");
   public static final XViewerColumn Notes_Col =
         new XViewerAtsAttributeColumn(ATSAttributes.SMA_NOTE_ATTRIBUTE, 80, SWT.LEFT, true, SortDataType.String,
               false, null);
   public static final XViewerColumn Deadline_Col =
         new XViewerAtsAttributeColumn(ATSAttributes.DEADLINE_ATTRIBUTE, 80, SWT.LEFT, true, SortDataType.String, true,
               null);

   // Aren't shown by default
   public static final XViewerColumn Annual_Cost_Avoidance_Col =
         new XViewerColumn(
               COLUMN_NAMESPACE + ".annualCostAvoidance",
               "Annual Cost Avoidance",
               50,
               SWT.LEFT,
               false,
               SortDataType.Float,
               false,
               "Hours that would be saved for the first year if this change were completed.\n\n" + "(Weekly Benefit Hours * 52 weeks) - Remaining Hours\n\n" + "If number is high, benefit is great given hours remaining.");
   public static final XViewerColumn Description_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".description", "Description", 150, SWT.LEFT, false, SortDataType.String,
               true, null);
   public static XViewerAtsAttributeColumn Legacy_PCR_Col =
         new XViewerAtsAttributeColumn(COLUMN_NAMESPACE + ".legacyPcrId", ATSAttributes.LEGACY_PCR_ID_ATTRIBUTE, 40,
               SWT.LEFT, false, SortDataType.String, false, null);
   public static final XViewerColumn Decision_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".decision", "Decision", 150, SWT.LEFT, false, SortDataType.String,
               false, null);
   public static final XViewerColumn Resolution_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".resolution", "Resolution", 150, SWT.LEFT, false, SortDataType.String,
               false, null);
   public static XViewerAtsAttributeColumn Estimated_Release_Date_Col =
         new XViewerAtsAttributeColumn(COLUMN_NAMESPACE + ".estimatedReleaseDate",
               ATSAttributes.ESTIMATED_RELEASE_DATE_ATTRIBUTE, 80, SWT.LEFT, false, SortDataType.Date, false,
               "Date the changes will be made available to the users.");
   public static final XViewerColumn Release_Date_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".releaseDate", ATSAttributes.RELEASE_DATE_ATTRIBUTE.getDisplayName(),
               80, SWT.LEFT, false, SortDataType.Date, false, "Date the changes were made available to the users.");
   public static final XViewerColumn Work_Package_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".workPackage", "Work Package", 80, SWT.LEFT, false, SortDataType.String,
               true, null);
   public static final XViewerColumn Category_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".category", "Category", 80, SWT.LEFT, false, SortDataType.String, true,
               "Open field for user to be able to enter text to use for categorizing/sorting.");
   public static final XViewerColumn Category2_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".category2", "Category2", 80, SWT.LEFT, false, SortDataType.String,
               true, "Open field for user to be able to enter text to use for categorizing/sorting.");
   public static final XViewerColumn Category3_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".category3", "Category3", 80, SWT.LEFT, false, SortDataType.String,
               true, "Open field for user to be able to enter text to use for categorizing/sorting.");
   public static XViewerAtsAttributeColumn Related_To_State_Col =
         new XViewerAtsAttributeColumn(COLUMN_NAMESPACE + ".relatedToState", ATSAttributes.RELATED_TO_STATE_ATTRIBUTE,
               80, SWT.LEFT, false, SortDataType.String, true,
               "State of the parent State Machine that this object is related to.");
   public static final XViewerColumn Estimated_Hours_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".estimatedHours", "Estimated Hours", 40, SWT.CENTER, false,
               SortDataType.Float, true, "Hours estimated to implement the changes associated with this Action.");
   public static final XViewerColumn Weekly_Benefit_Hrs_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".weeklyBenefitHrs", "Weekly Benefit Hrs", 40, SWT.CENTER, false,
               SortDataType.Float, false,
               "Estimated number of hours that will be saved over a single year if this change is completed.");
   public static final XViewerColumn Remaining_Hours_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".remainingHours", "Remaining Hours", 40, SWT.CENTER, false,
               SortDataType.Float, false,
               "Hours that remain to complete the changes.\n\nEstimated Hours - (Estimated Hours * Percent Complete).");

   public static final XViewerColumn Percent_Complete_State_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".statePercent", "State Percent Complete", 40, SWT.CENTER, false,
               SortDataType.Percent, false,
               "Percent Complete for the changes to the current state.\n\nAmount entered from user.");
   public static final XViewerColumn Percent_Complete_State_Task_Col =
         new XViewerColumn(
               COLUMN_NAMESPACE + ".stateTaskPercent",
               "State Task Percent Complete",
               40,
               SWT.CENTER,
               false,
               SortDataType.Percent,
               false,
               "Percent Complete for the tasks related to the current state.\n\nCalculation: total percent of all tasks related to state / number of tasks related to state");
   public static final XViewerColumn Percent_Complete_State_Review_Col =
         new XViewerColumn(
               COLUMN_NAMESPACE + ".stateReviewPercent",
               "State Review Percent Complete",
               40,
               SWT.CENTER,
               false,
               SortDataType.Percent,
               false,
               "Percent Complete for the reviews related to the current state.\n\nCalculation: total percent of all reviews related to state / number of reviews related to state");
   public static final XViewerColumn Percent_Complete_Total_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".totalPercentComplete", "Total Percent Complete", 40, SWT.CENTER, false,
               SortDataType.Percent, false, "Percent Complete for the reviews related to the current state.");

   public static final XViewerColumn Hours_Spent_State_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".stateHoursSpent", "State Hours Spent", 40, SWT.CENTER, false,
               SortDataType.Float, false, "Hours spent in performing the changes to the current state.");
   public static final XViewerColumn Hours_Spent_State_Task_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".stateTaskHoursSpent", "State Task Hours Spent", 40, SWT.CENTER, false,
               SortDataType.Float, false,
               "Hours spent in performing the changes for the tasks related to the current state.");
   public static final XViewerColumn Hours_Spent_State_Review_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".stateReviewHoursSpent", "State Review Hours Spent", 40, SWT.CENTER,
               false, SortDataType.Float, false,
               "Hours spent in performing the changes for the reveiws related to the current state.");
   public static final XViewerColumn Hours_Spent_Total_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".stateTotalHoursSpent", "State Total Hours Spent", 40, SWT.CENTER,
               false, SortDataType.Percent, false, "Hours spent for all work related to the current state.");

   public static final XViewerColumn Total_Hours_Spent_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".totalHoursSpent", "Total Hours Spent", 40, SWT.CENTER, false,
               SortDataType.Percent, false, "Hours spent for all work related to all states.");

   public static final XViewerColumn Originator_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".originator", "Originator", 80, SWT.LEFT, false, SortDataType.String,
               false, null);
   public static final XViewerColumn Implementor_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".implementer", "Implementer", 80, SWT.LEFT, false, SortDataType.String,
               false, "User assigned to the Implementation of the changes.");
   public static final XViewerColumn Review_Author_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".reviewAuthor", "Review Author", 100, SWT.LEFT, false,
               SortDataType.String, false, "Review Author(s)");
   public static final XViewerColumn Review_Moderator_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".reviewModerator", "Review Moderator", 100, SWT.LEFT, false,
               SortDataType.String, false, "Review Moderator(s)");
   public static final XViewerColumn Review_Reviewer_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".reviewReviewer", "Review Reviewer", 100, SWT.LEFT, false,
               SortDataType.String, false, "Review Reviewer(s)");
   public static final XViewerColumn Review_Decider_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".reviewDecider", "Review Decider", 100, SWT.LEFT, false,
               SortDataType.String, false, "Review Decider");
   public static final XViewerColumn Completed_Date_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".completedDate", "Completed Date", 80, SWT.CENTER, false,
               SortDataType.Date, false, null);
   public static final XViewerColumn Cancelled_Date_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".cancelledDate", "Cancelled Date", 80, SWT.CENTER, false,
               SortDataType.Date, false, null);
   public static final XViewerColumn Man_Days_Needed_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".manDaysNeeded", "Man Days Needed", 40, SWT.CENTER, false,
               SortDataType.Float, false, null);
   public static final XViewerColumn Percent_Rework_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".percentRework", "Percent Rework", 40, SWT.CENTER, false,
               SortDataType.Integer, false, null);
   public static final XViewerColumn Branch_Status_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".branchStatus", "Branch Status", 40, SWT.CENTER, false,
               SortDataType.String, false, null);
   public static final XViewerColumn Number_of_Tasks_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".numberOfTasks", "Number of Tasks", 40, SWT.CENTER, false,
               SortDataType.String, false, null);
   public static final XViewerColumn Last_Modified_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".lastModified", "Last Modified", 40, SWT.CENTER, false,
               SortDataType.Date, false, "Retrieves timestamp of last database update of this artifact.");
   public static final XViewerColumn Last_Statused_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".lastStatused", "Last Statused", 40, SWT.CENTER, false,
               SortDataType.Date, false, "Retrieves timestamp of status (percent completed or hours spent).");
   public static final XViewerColumn Validation_Required_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".validationRequired", "Validation Required", 80, SWT.LEFT, false,
               SortDataType.String, false,
               "If set, Originator will be asked to perform a review to\nensure changes are as expected.");
   public static final List<XViewerColumn> columns =
         Arrays.asList(Type_Col, State_Col, Priority_Col, Change_Type_Col, Assignees_Col, Title_Col,
               Actionable_Items_Col, User_Community_Col, ID_Col, Created_Date_Col, Version_Target_Col, Team_Col,
               Notes_Col, Deadline_Col, Annual_Cost_Avoidance_Col, Description_Col, Legacy_PCR_Col, Decision_Col,
               Resolution_Col, Estimated_Release_Date_Col, Release_Date_Col, Work_Package_Col, Category_Col,
               Category2_Col, Category3_Col, Related_To_State_Col, Estimated_Hours_Col, Weekly_Benefit_Hrs_Col,
               Remaining_Hours_Col, Percent_Complete_State_Col, Percent_Complete_State_Task_Col,
               Percent_Complete_State_Review_Col, Percent_Complete_Total_Col, Hours_Spent_State_Col,
               Hours_Spent_State_Task_Col, Hours_Spent_State_Review_Col, Hours_Spent_Total_Col, Total_Hours_Spent_Col,
               Originator_Col, Implementor_Col, Review_Author_Col, Review_Moderator_Col, Review_Reviewer_Col,
               Review_Decider_Col, Completed_Date_Col, Cancelled_Date_Col, Man_Days_Needed_Col, Percent_Rework_Col,
               Branch_Status_Col, Number_of_Tasks_Col, Last_Modified_Col, Last_Statused_Col, Validation_Required_Col);
   public static Map<String, XViewerColumn> idToColumn = null;

   private static String NAMESPACE = "org.eclipse.osee.ats.WorldXViewer";

   public WorldXViewerFactory() {
      super(NAMESPACE);
      if (idToColumn == null) {
         idToColumn = new HashMap<String, XViewerColumn>();
         for (XViewerColumn xCol : columns) {
            idToColumn.put(xCol.getId(), xCol);
         }
      }
   }

   public XViewerSorter createNewXSorter(XViewer xViewer) {
      return new WorldXViewerSorter(xViewer);
   }

   public CustomizeData getDefaultTableCustomizeData() {
      CustomizeData custData = new CustomizeData();
      custData.getColumnData().setColumns(columns);
      return custData;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.IXViewerFactory#getDefaultXViewerColumn()
    */
   public XViewerColumn getDefaultXViewerColumn(String id) {
      return idToColumn.get(id);
   }

}
