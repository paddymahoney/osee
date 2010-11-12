/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.util.widgets.role.UserRole.Role;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.swt.SWT;

public class ReviewAuthorColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ReviewAuthorColumn instance = new ReviewAuthorColumn();

   public static ReviewAuthorColumn getInstance() {
      return instance;
   }

   private ReviewAuthorColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".reviewAuthor", "Review Author", 100, SWT.LEFT, false,
         SortDataType.String, false, "Review Author(s)");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ReviewAuthorColumn copy() {
      ReviewAuthorColumn newXCol = new ReviewAuthorColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof PeerToPeerReviewArtifact) {
            return Artifacts.toString("; ",
               ((PeerToPeerReviewArtifact) element).getUserRoleManager().getRoleUsers(Role.Author));
         }
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }
}
