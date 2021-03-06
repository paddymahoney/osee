/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.internal.column.ev;

import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.ev.IAtsEarnedValueServiceProvider;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.framework.core.data.ArtifactToken;

/**
 * @author Donald G. Dunne
 */
public class WorkPackageNameColumn extends AbstractRelatedWorkPackageColumn {

   public WorkPackageNameColumn(IAtsEarnedValueServiceProvider earnedValueServiceProvider, IAtsServices services) {
      super(earnedValueServiceProvider, services);
   }

   @Override
   protected String getColumnValue(IAtsWorkPackage workPkg) {
      return workPkg.getName();
   }

   @Override
   protected String getColumnValue(ArtifactToken wpArt) {
      return wpArt.getName();
   }

}
