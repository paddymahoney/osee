/*******************************************************************************
 * Copyright (c) 2010 Boeing.
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
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.osee.ats.api.review.IAtsPeerReviewRoleManager;
import org.eclipse.osee.ats.api.review.Role;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.util.AtsObjects;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;

/**
 * @author Donald G. Dunne
 */
public class ReviewAuthorColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ReviewAuthorColumn instance = new ReviewAuthorColumn();

   public static ReviewAuthorColumn getInstance() {
      return instance;
   }

   private ReviewAuthorColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".reviewAuthor", "Review Author", 100, XViewerAlign.Left, false,
         SortDataType.String, false, "Review Author(s)");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ReviewAuthorColumn copy() {
      ReviewAuthorColumn newXCol = new ReviewAuthorColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof PeerToPeerReviewArtifact) {
            IAtsPeerReviewRoleManager roleMgr = ((PeerToPeerReviewArtifact) element).getRoleManager();
            return AtsObjects.toString("; ", roleMgr.getRoleUsers(Role.Author));
         }
      } catch (OseeCoreException ex) {
         LogUtil.getCellExceptionString(ex);
      }
      return "";
   }
}
