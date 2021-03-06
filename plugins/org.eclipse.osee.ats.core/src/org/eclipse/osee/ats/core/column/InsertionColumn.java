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
import org.eclipse.osee.ats.api.insertion.IAtsInsertion;
import org.eclipse.osee.ats.api.insertion.IAtsInsertionActivity;
import org.eclipse.osee.ats.core.config.WorkPackageUtility;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class InsertionColumn extends AbstractServicesColumn {

   private Map<Object, ArtifactToken> idToInsertion;

   public InsertionColumn(IAtsServices services) {
      super(services);
   }

   /**
    * Set optional map to use as a cache of work item id (Long) or work package guid (String) to Insertion artifact.
    */
   public void setIdToInsertionCache(Map<Object, ArtifactToken> idToInsertion) {
      this.idToInsertion = idToInsertion;
   }

   @Override
   public String getText(IAtsObject atsObject) {
      String format = "%s";
      if (services.getStoreService().isDeleted(atsObject)) {
         format = "<Deleted> %s";
      }
      return String.format(format, getInsertionStr(atsObject, services, CountryColumn.getUtil(), idToInsertion));
   }

   public static String getInsertionStr(IAtsObject atsObject, IAtsServices services) {
      return getInsertionStr(atsObject, services, CountryColumn.getUtil());
   }

   public static String getInsertionStr(IAtsObject atsObject, IAtsServices services, WorkPackageUtility util) {
      return getInsertionStr(atsObject, services, util, null);
   }

   public static String getInsertionStr(IAtsObject atsObject, IAtsServices services, WorkPackageUtility util, Map<Object, ArtifactToken> idToInsertion) {
      String result = "";
      if (atsObject instanceof IAtsWorkItem) {
         if (idToInsertion != null) {
            ArtifactToken insertionArt = idToInsertion.get(atsObject.getId());
            if (insertionArt != null) {
               result = insertionArt.getName();
            }
         }
         if (Strings.isInValid(result)) {
            IAtsWorkItem workItem = (IAtsWorkItem) atsObject;
            Pair<IAtsInsertion, Boolean> insertion = util.getInsertion(services, workItem);
            if (insertion.getFirst() != null) {
               result = String.format("%s%s", insertion.getFirst().getName(), insertion.getSecond() ? " (I)" : "");
            }
         }
      } else if (atsObject instanceof IAtsWorkPackage) {
         if (idToInsertion != null) {
            ArtifactToken insertionArt = idToInsertion.get(((IAtsWorkPackage) atsObject).getGuid());
            if (insertionArt != null) {
               result = insertionArt.getName();
            }
         }
         if (Strings.isInValid(result)) {
            IAtsWorkPackage workPackage = (IAtsWorkPackage) atsObject;
            IAtsInsertionActivity insertionActivity = services.getProgramService().getInsertionActivity(workPackage);
            if (insertionActivity != null) {
               IAtsInsertion insertion = services.getProgramService().getInsertion(insertionActivity);
               result = insertion.getName();
            }
         }
      }
      return result;
   }

}
