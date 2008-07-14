/*
 * Created on Jul 14, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.xviewer.column;

import java.sql.SQLException;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerValueColumn;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class XViewerSmaStateColumn extends XViewerValueColumn {

   public XViewerSmaStateColumn(XViewer viewer, int columnNum) {
      super(viewer, "State", "", 75, 75, SWT.LEFT);
      setOrderNum(columnNum);
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column) throws OseeCoreException, SQLException {
      if (element instanceof StateMachineArtifact) {
         return ((StateMachineArtifact) element).getSmaMgr().getStateMgr().getCurrentStateName();
      }
      return "";
   }

}
