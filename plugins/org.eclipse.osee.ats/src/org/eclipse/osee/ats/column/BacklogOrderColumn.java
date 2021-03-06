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

import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.data.IArtifactType;

/**
 * @author Donald G. Dunne
 */
public class BacklogOrderColumn extends GoalOrderColumn {

   public static final String COLUMN_ID = WorldXViewerFactory.COLUMN_NAMESPACE + ".backlogOrder";

   static BacklogOrderColumn instance = new BacklogOrderColumn();

   public static BacklogOrderColumn getInstance() {
      return instance;
   }

   public BacklogOrderColumn() {
      super(true, COLUMN_ID, "Backlog Order");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public BacklogOrderColumn copy() {
      BacklogOrderColumn newXCol = new BacklogOrderColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public IArtifactType getArtifactType() {
      return AtsArtifactTypes.AgileBacklog;
   }
}
