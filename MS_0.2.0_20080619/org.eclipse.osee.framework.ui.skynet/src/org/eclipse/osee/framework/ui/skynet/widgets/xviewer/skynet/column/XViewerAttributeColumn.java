/*
 * Created on Jul 14, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.widgets.xviewer.skynet.column;

import java.sql.SQLException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerValueColumn;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class XViewerAttributeColumn extends XViewerValueColumn {

   private final String attributeTypeName;

   public XViewerAttributeColumn(XViewer viewer, String name, String attributeTypeName, int width, int defaultWidth, int align, boolean show, SortDataType sortDataType, int orderNum) {
      super(viewer, name, width, defaultWidth, align, show, sortDataType, orderNum);
      this.attributeTypeName = attributeTypeName;
   }

   public XViewerAttributeColumn(String name, String attributeTypeName, int width, int defaultWidth, int align, boolean show, SortDataType sortDataType, int orderNum) {
      this(null, name, attributeTypeName, width, defaultWidth, align, show, sortDataType, orderNum);
   }

   public XViewerAttributeColumn(XViewer viewer, String name, String attributeTypeName, int width, int defaultWidth, int align) {
      this(viewer, name, attributeTypeName, width, defaultWidth, align, true, SortDataType.String, SWT.LEFT);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerValueColumn#getColumnText(java.lang.Object, org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn)
    */
   @Override
   public String getColumnText(Object element, XViewerColumn column) throws OseeCoreException, SQLException {
      if (element instanceof Artifact) {
         return ((Artifact) element).getAttributesToString(attributeTypeName);
      }
      return super.getColumnText(element, column);
   }
}
