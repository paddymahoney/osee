/*******************************************************************************
 * Copyright (c) 2017 Boeing.
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
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.agile.IAgileTeam;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinitionService;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class TeamDefinitionService implements IAtsTeamDefinitionService {

   protected final IAtsServices services;

   public TeamDefinitionService(IAtsServices services) {
      this.services = services;
   }

   @Override
   public IAtsTeamDefinition getTeamDefinition(IAtsWorkItem workItem) throws OseeCoreException {
      IAtsTeamDefinition teamDef = null;
      String teamDefGuid =
         services.getAttributeResolver().getSoleAttributeValue(workItem, AtsAttributeTypes.TeamDefinition, "");
      if (Strings.isValid(teamDefGuid)) {
         teamDef = services.getConfigItem(teamDefGuid);
      }
      return teamDef;
   }

   @Override
   public Collection<IAtsVersion> getVersions(IAtsTeamDefinition teamDef) {
      List<IAtsVersion> versions = new ArrayList<>();
      for (ArtifactId verArt : services.getRelationResolver().getRelated(teamDef,
         AtsRelationTypes.TeamDefinitionToVersion_Version)) {
         versions.add(services.getConfigItemFactory().getVersion(verArt));
      }
      return versions;
   }

   @Override
   public IAtsTeamDefinition getTeamDefHoldingVersions(IAtsTeamDefinition teamDef) {
      return teamDef.getTeamDefinitionHoldingVersions();
   }

   @Override
   public IAtsTeamDefinition getTeamDefHoldingVersions(IAtsProgram program) {
      return services.getProgramService().getTeamDefHoldingVersions(program);
   }

   @Override
   public IAtsTeamDefinition getTeamDefinition(String name) {
      IAtsTeamDefinition teamDef = null;
      ArtifactId teamDefArt = services.getArtifactByName(AtsArtifactTypes.TeamDefinition, name);
      if (teamDefArt != null) {
         teamDef = services.getConfigItemFactory().getTeamDef(teamDefArt);
      }
      return teamDef;
   }

   @Override
   public Collection<IAtsTeamDefinition> getTeamDefinitions(IAgileTeam agileTeam) {
      List<IAtsTeamDefinition> teamDefs = new LinkedList<>();
      for (ArtifactId atsTeamArt : services.getRelationResolver().getRelated(agileTeam,
         AtsRelationTypes.AgileTeamToAtsTeam_AtsTeam)) {
         teamDefs.add(services.getConfigItemFactory().getTeamDef(atsTeamArt));
      }
      return teamDefs;
   }

}
