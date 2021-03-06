/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.column;

import java.util.Map;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.core.config.WorkPackageUtility;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class InsertionActivityColumn extends AbstractServicesColumn {

   private Map<Object, ArtifactToken> idToInsertionActivity;

   public InsertionActivityColumn(IAtsServices services) {
      super(services);
   }

   /**
    * Set optional map to use as a cache of work item id (Long) or work package guid (String) to Insertion Activity
    * artifact.
    */
   public void setIdToInsertionActivityCache(Map<Object, ArtifactToken> idToInsertionActivity) {
      this.idToInsertionActivity = idToInsertionActivity;
   }

   @Override
   public String getText(IAtsObject atsObject) {
      String format = "%s";
      if (services.getStoreService().isDeleted(atsObject)) {
         format = "<Deleted> %s";
      }
      return String.format(format,
         getInsertionActivityStr(atsObject, services, CountryColumn.getUtil(), idToInsertionActivity));
   }

   public static String getInsertionActivityStr(IAtsObject atsObject, IAtsServices services) {
      return getInsertionActivityStr(atsObject, services, CountryColumn.getUtil());
   }

   public static String getInsertionActivityStr(IAtsObject atsObject, IAtsServices services, WorkPackageUtility util) {
      return getInsertionActivityStr(atsObject, services, util, null);
   }

   public static String getInsertionActivityStr(IAtsObject atsObject, IAtsServices services, WorkPackageUtility utilMap, Map<Object, ArtifactToken> idToInsertionActivity) {
      String result = "";
      if (atsObject instanceof IAtsWorkItem) {
         if (idToInsertionActivity != null) {
            ArtifactToken insertionArt = idToInsertionActivity.get(atsObject.getId());
            if (insertionArt != null) {
               result = insertionArt.getName();
            }
         }
         if (Strings.isInValid(result)) {
            IAtsWorkItem workItem = (IAtsWorkItem) atsObject;
            Pair<IAtsInsertionActivity, Boolean> insertionActivity = utilMap.getInsertionActivity(services, workItem);
            if (insertionActivity.getFirst() != null) {
               result = String.format("%s%s", insertionActivity.getFirst().getName(),
                  insertionActivity.getSecond() ? " (I)" : "");
            }
         }
      } else if (atsObject instanceof IAtsWorkPackage) {
         if (idToInsertionActivity != null) {
            ArtifactToken insertionActivityArt = idToInsertionActivity.get(((IAtsWorkPackage) atsObject).getGuid());
            if (insertionActivityArt != null) {
               result = insertionActivityArt.getName();
            }
         }
         if (Strings.isInValid(result)) {
            IAtsWorkPackage workPackage = (IAtsWorkPackage) atsObject;
            IAtsInsertionActivity insertionActivity = services.getProgramService().getInsertionActivity(workPackage);
            if (insertionActivity != null) {
               result = insertionActivity.getName();
            }
         }
      }
      return result;
   }

}
