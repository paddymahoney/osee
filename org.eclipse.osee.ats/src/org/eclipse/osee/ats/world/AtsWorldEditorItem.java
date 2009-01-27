/*
 * Created on Jan 26, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.world;

import java.util.Collections;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class AtsWorldEditorItem implements IAtsWorldEditorItem {

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.IAtsWorldEditorItem#getColumnImage(java.lang.Object, org.eclipse.nebula.widgets.xviewer.XViewerColumn, int)
    */
   @Override
   public Image getColumnImage(Object element, XViewerColumn col, int columnIndex) throws OseeCoreException {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.IAtsWorldEditorItem#getColumnText(java.lang.Object, org.eclipse.nebula.widgets.xviewer.XViewerColumn, int)
    */
   @Override
   public String getColumnText(Object element, XViewerColumn col, int columnIndex) throws OseeCoreException {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.IAtsWorldEditorItem#getForeground(java.lang.Object, org.eclipse.nebula.widgets.xviewer.XViewerColumn, int)
    */
   @Override
   public Color getForeground(Object element, XViewerColumn col, int columnIndex) throws OseeCoreException {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.IAtsWorldEditorItem#getWorldMenuActions(org.eclipse.osee.ats.world.WorldComposite)
    */
   @Override
   public List<? extends Action> getWorldMenuActions(IWorldEditorProvider worldEditorProvider, WorldComposite worldComposite) throws OseeCoreException {
      return Collections.emptyList();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.IAtsWorldEditorItem#getXViewerColumns()
    */
   @Override
   public List<XViewerColumn> getXViewerColumns() throws OseeCoreException {
      return Collections.emptyList();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.IAtsWorldEditorItem#isXColumnProvider(org.eclipse.nebula.widgets.xviewer.XViewerColumn)
    */
   @Override
   public boolean isXColumnProvider(XViewerColumn col) throws OseeCoreException {
      return false;
   }

}
