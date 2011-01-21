/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.ats.artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.config.AtsCacheManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactFactory;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemArtifact extends Artifact {

   public ActionableItemArtifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, IArtifactType artifactType) throws OseeCoreException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
   }

   public static List<ActionableItemArtifact> getActionableItems(Active active) throws OseeCoreException {
      return Collections.castAll(AtsCacheManager.getArtifactsByActive(AtsArtifactTypes.ActionableItem, active));
   }

   public static String getNotActionableItemError(Artifact aia) {
      return "Action can not be written against " + aia.getArtifactTypeName() + " \"" + aia + "\" (" + aia.getHumanReadableId() + ").\n\nChoose another item.";
   }

   public static List<ActionableItemArtifact> getTopLevelActionableItems(Active active) throws OseeCoreException {
      ActionableItemArtifact topAi = getTopActionableItem();
      if (topAi == null) {
         return java.util.Collections.emptyList();
      }
      return Collections.castAll(AtsUtil.getActive(
         Artifacts.getChildrenOfTypeSet(topAi, ActionableItemArtifact.class, false), active,
         ActionableItemArtifact.class));
   }

   public Collection<User> getLeads() throws OseeCoreException {
      return getRelatedArtifacts(AtsRelationTypes.TeamLead_Lead, User.class);
   }

   public static ActionableItemArtifact getTopActionableItem() throws OseeCoreException {
      return (ActionableItemArtifact) AtsArtifactToken.get(AtsArtifactToken.TopActionableItem);
   }

   public static List<ActionableItemArtifact> getActionableItems() throws OseeCoreException {
      return Collections.castAll(AtsCacheManager.getArtifactsByActive(AtsArtifactTypes.ActionableItem, Active.Both));
   }

   public boolean isActionable() throws OseeCoreException {
      return getSoleAttributeValue(AtsAttributeTypes.Actionable, false);
   }

   public static Set<ActionableItemArtifact> getActionableItems(Collection<String> actionableItemNames) throws OseeCoreException {
      Set<ActionableItemArtifact> aias = new HashSet<ActionableItemArtifact>();
      for (String actionableItemName : actionableItemNames) {
         for (Artifact artifact : AtsCacheManager.getArtifactsByName(AtsArtifactTypes.ActionableItem,
            actionableItemName)) {
            aias.add((ActionableItemArtifact) artifact);
         }
      }
      return aias;
   }

   public static Collection<TeamDefinitionArtifact> getImpactedTeamDefs(Collection<ActionableItemArtifact> aias) throws OseeCoreException {
      return TeamDefinitionArtifact.getImpactedTeamDefs(aias);
   }

   public Collection<TeamDefinitionArtifact> getImpactedTeamDefs() throws OseeCoreException {
      return TeamDefinitionArtifact.getImpactedTeamDefs(Arrays.asList(this));
   }

   public static Set<TeamDefinitionArtifact> getTeamsFromItemAndChildren(ActionableItemArtifact aia) throws OseeCoreException {
      return TeamDefinitionArtifact.getTeamsFromItemAndChildren(aia);
   }

   public static Set<ActionableItemArtifact> getActionableItemsFromItemAndChildren(ActionableItemArtifact aia) throws OseeCoreException {
      Set<ActionableItemArtifact> aias = new HashSet<ActionableItemArtifact>();
      getActionableItemsFromItemAndChildren(aia, aias);
      return aias;
   }

   public static void getActionableItemsFromItemAndChildren(ActionableItemArtifact aia, Set<ActionableItemArtifact> aiaTeams) throws OseeCoreException {
      for (Artifact art : aia.getChildren()) {
         if (art instanceof ActionableItemArtifact) {
            aiaTeams.add((ActionableItemArtifact) art);
            for (Artifact childArt : aia.getChildren()) {
               if (childArt instanceof ActionableItemArtifact) {
                  getActionableItemsFromItemAndChildren((ActionableItemArtifact) childArt, aiaTeams);
               }
            }
         }
      }
   }

}
