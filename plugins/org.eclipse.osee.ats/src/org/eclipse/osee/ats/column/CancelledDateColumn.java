/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.swt.SWT;

public class CancelledDateColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static CancelledDateColumn instance = new CancelledDateColumn();

   public static CancelledDateColumn getInstance() {
      return instance;
   }

   private CancelledDateColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".cancelledDate", "Cancelled Date", 80, SWT.CENTER, false,
         SortDataType.Date, false, null);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public CancelledDateColumn copy() {
      CancelledDateColumn newXCol = new CancelledDateColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         return getDateStr(element);
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }

   public static Date getDate(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         getDate(ActionManager.getFirstTeam(object));
      } else if (object instanceof AbstractWorkflowArtifact) {
         AbstractWorkflowArtifact awa = (AbstractWorkflowArtifact) object;
         if (((AbstractWorkflowArtifact) object).isCancelled()) {
            Date date = ((AbstractWorkflowArtifact) object).internalGetCancelledDate();
            if (date == null) {
               OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP,
                  "Cancelled with no date => " + awa.getHumanReadableId());
            }
            return date;
         }
      }
      return null;
   }

   public static String getDateStr(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         Set<String> strs = new HashSet<String>();
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(object)) {
            String str = getDateStr(team);
            if (Strings.isValid(str)) {
               strs.add(str);
            }
         }
         return Collections.toString(";", strs);
      }
      return DateUtil.getMMDDYYHHMM(getDate(object));
   }
}
