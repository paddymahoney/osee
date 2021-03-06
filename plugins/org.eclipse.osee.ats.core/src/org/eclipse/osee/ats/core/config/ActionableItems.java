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
package org.eclipse.osee.ats.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.query.IAtsQueryService;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;

/**
 * @author Donald G. Dunne
 */
public class ActionableItems {

   public static Set<IAtsActionableItem> getAIsFromItemAndChildren(IAtsActionableItem ai) throws OseeCoreException {
      Set<IAtsActionableItem> ais = new HashSet<>();
      ais.add(ai);
      for (IAtsActionableItem art : ai.getChildrenActionableItems()) {
         ais.addAll(getAIsFromItemAndChildren(art));
      }
      return ais;
   }

   public static Set<IAtsActionableItem> getActionableItemsFromItemAndChildren(IAtsActionableItem ai) throws OseeCoreException {
      Set<IAtsActionableItem> ais = new HashSet<>();
      getActionableItemsFromItemAndChildren(ai, ais);
      return ais;
   }

   public static void getActionableItemsFromItemAndChildren(IAtsActionableItem ai, Set<IAtsActionableItem> aiTeams) throws OseeCoreException {
      for (IAtsActionableItem art : ai.getChildrenActionableItems()) {
         aiTeams.add(art);
         for (IAtsActionableItem childArt : ai.getChildrenActionableItems()) {
            getActionableItemsFromItemAndChildren(childArt, aiTeams);
         }
      }
   }

   public static Set<IAtsActionableItem> getActionableItems(Collection<String> actionableItemNames, IAtsServices services) throws OseeCoreException {
      Set<IAtsActionableItem> ais = new HashSet<>();
      for (String actionableItemName : actionableItemNames) {
         for (IAtsActionableItem ai : services.getQueryService().createQuery(AtsArtifactTypes.ActionableItem).getItems(
            IAtsActionableItem.class)) {
            if (ai.getName().equals(actionableItemName)) {
               ais.add(ai);
            }
         }
      }
      return ais;
   }

   public static Collection<IAtsTeamDefinition> getImpactedTeamDefs(Collection<IAtsActionableItem> ais) throws OseeCoreException {
      return TeamDefinitions.getImpactedTeamDefs(ais);
   }

   public static List<IAtsActionableItem> getActionableItems(Active active, IAtsQueryService queryService) throws OseeCoreException {
      return Collections.castAll(
         getActive(queryService.createQuery(AtsArtifactTypes.ActionableItem).getItems(), active));
   }

   public static String getNotActionableItemError(IAtsConfigObject configObject) {
      return "Action can not be written against " + configObject.getName() + " \"" + configObject + "\" (" + configObject.getId() + ").\n\nChoose another item.";
   }

   public static IAtsActionableItem getTopActionableItem(IAtsServices services) throws OseeCoreException {
      ArtifactToken artifact = services.getArtifact(AtsArtifactToken.TopActionableItem.getId());
      return services.getConfigItem(artifact);
   }

   public static List<IAtsActionableItem> getActionableItemsAll(IAtsQueryService queryService) throws OseeCoreException {
      return getActionableItems(Active.Both, queryService);
   }

   public static List<IAtsActionableItem> getTopLevelActionableItems(Active active, IAtsServices services) throws OseeCoreException {
      IAtsActionableItem topAi = getTopActionableItem(services);
      if (topAi == null) {
         return java.util.Collections.emptyList();
      }
      return Collections.castAll(getActive(getChildren(topAi, false), active));
   }

   public static List<IAtsActionableItem> getActive(Collection<IAtsActionableItem> ais, Active active) {
      List<IAtsActionableItem> results = new ArrayList<>();
      for (IAtsActionableItem ai : ais) {
         if (active == Active.Both) {
            results.add(ai);
         } else {
            // assume active unless otherwise specified
            boolean attributeActive = ai.isActive();
            if (active == Active.Active && attributeActive) {
               results.add(ai);
            } else if (active == Active.InActive && !attributeActive) {
               results.add(ai);
            }
         }
      }
      return results;
   }

   public static Set<IAtsActionableItem> getChildren(IAtsActionableItem topActionableItem, boolean recurse) throws OseeCoreException {
      Set<IAtsActionableItem> children = new HashSet<>();
      for (IAtsActionableItem child : topActionableItem.getChildrenActionableItems()) {
         children.add(child);
         if (recurse) {
            children.addAll(getChildren(child, recurse));
         }
      }
      return children;
   }

   public static Collection<IAtsActionableItem> getUserEditableActionableItems(Collection<IAtsActionableItem> actionableItems) {
      List<IAtsActionableItem> ais = new LinkedList<>();
      for (IAtsActionableItem ai : actionableItems) {
         if (ai.isAllowUserActionCreation()) {
            ais.add(ai);
         }
      }
      return ais;
   }

}
