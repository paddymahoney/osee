/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.swt.SWT;

public class AnnualCostAvoidanceColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static AnnualCostAvoidanceColumn instance = new AnnualCostAvoidanceColumn();

   public static AnnualCostAvoidanceColumn getInstance() {
      return instance;
   }

   private AnnualCostAvoidanceColumn() {
      super(
         WorldXViewerFactory.COLUMN_NAMESPACE + ".annualCostAvoidance",
         "Annual Cost Avoidance",
         50,
         SWT.LEFT,
         false,
         SortDataType.Float,
         false,
         "Hours that would be saved for the first year if this change were completed.\n\n" + "(Weekly Benefit Hours * 52 weeks) - Remaining Hours\n\n" + "If number is high, benefit is great given hours remaining.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public AnnualCostAvoidanceColumn copy() {
      AnnualCostAvoidanceColumn newXCol = new AnnualCostAvoidanceColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         Result result = isWorldViewAnnualCostAvoidanceValid(element);
         if (result.isFalse()) {
            return result.getText();
         }
         return AtsUtil.doubleToI18nString(getWorldViewAnnualCostAvoidance(element), true);
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }

   public static double getWorldViewAnnualCostAvoidance(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         double hours = 0;
         // Add up hours for all children
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(object)) {
            if (!team.isCompleted() && !team.isCancelled()) {
               hours += getWorldViewAnnualCostAvoidance(team);
            }
         }
         return hours;
      } else if (object instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) object;
         double benefit = teamArt.getWorldViewWeeklyBenefit();
         double remainHrs = teamArt.getRemainHoursTotal();
         return benefit * 52 - remainHrs;
      }
      return 0;
   }

   public static Result isWorldViewAnnualCostAvoidanceValid(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(object)) {
            Result result = isWorldViewAnnualCostAvoidanceValid(team);
            if (result.isFalse()) {
               return result;
            }
         }
      }
      if (object instanceof AbstractWorkflowArtifact) {
         AbstractWorkflowArtifact artifact = (AbstractWorkflowArtifact) object;
         if (artifact.isAttributeTypeValid(AtsAttributeTypes.WeeklyBenefit)) {
            return Result.TrueResult;
         }
         Result result = RemainingHoursColumn.isRemainingHoursValid(artifact);
         if (result.isFalse()) {
            return result;
         }
         String value = null;
         try {
            value = artifact.getSoleAttributeValue(AtsAttributeTypes.WeeklyBenefit, "");
            if (!Strings.isValid(value)) {
               return new Result("Weekly Benefit Hours not set.");
            }
            double val = new Float(value).doubleValue();
            if (val == 0) {
               return new Result("Weekly Benefit Hours not set.");
            }
         } catch (NumberFormatException ex) {
            OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "HRID " + artifact.getHumanReadableId(), ex);
            return new Result("Weekly Benefit value is invalid double \"" + value + "\"");
         } catch (Exception ex) {
            OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "HRID " + artifact.getHumanReadableId(), ex);
            return new Result("Exception calculating cost avoidance.  See log for details.");
         }
         return Result.TrueResult;
      }
      return Result.FalseResult;
   }
}
