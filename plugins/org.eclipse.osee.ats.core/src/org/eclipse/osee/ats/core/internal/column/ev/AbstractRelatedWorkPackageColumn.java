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

import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ev.IAtsEarnedValueServiceProvider;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.ats.core.column.IAtsColumnUtility;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * Provides for display of ActivityId and ActivityName from related Work Package
 * 
 * @author Donald G. Dunne
 */
public abstract class AbstractRelatedWorkPackageColumn implements IAtsColumnUtility {

   private final IAtsEarnedValueServiceProvider earnedValueServiceProvider;

   protected static final String CELL_ERROR_PREFIX = "!Error";

   public AbstractRelatedWorkPackageColumn(IAtsEarnedValueServiceProvider earnedValueServiceProvider) {
      this.earnedValueServiceProvider = earnedValueServiceProvider;
   }

   @Override
   public String getColumnText(Object object) {
      String result = "";
      try {
         if (object instanceof IAtsWorkItem) {
            IAtsWorkPackage workPkg =
               earnedValueServiceProvider.getEarnedValueService().getWorkPackage((IAtsWorkItem) object);
            if (workPkg != null) {
               result = getColumnValue(workPkg);
            }
         }
      } catch (OseeCoreException ex) {
         result = CELL_ERROR_PREFIX + " - " + ex.getLocalizedMessage();
      }
      return result;
   }

   protected abstract String getColumnValue(IAtsWorkPackage workPkg) throws OseeCoreException;

}