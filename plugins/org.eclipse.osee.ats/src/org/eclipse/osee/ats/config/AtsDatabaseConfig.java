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
package org.eclipse.osee.ats.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.util.AtsActivity;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionAdmin;
import org.eclipse.osee.ats.core.client.util.AtsGroup;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.ats.workdef.AtsWorkDefinitionSheetProviders;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.database.init.IDbInitializationTask;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.ActivityLogJaxRsService;

/**
 * @author Donald G. Dunne
 */
public class AtsDatabaseConfig implements IDbInitializationTask {

   @Override
   public void run() throws OseeCoreException {
      createAtsFolders();

      // load top team into cache
      Artifact topTeamDefArt =
         ArtifactQuery.getArtifactFromToken(AtsArtifactToken.TopTeamDefinition, AtsClientService.get().getAtsBranch());
      IAtsTeamDefinition teamDef = AtsClientService.get().getConfigItem(topTeamDefArt);
      IAtsChangeSet changes = AtsClientService.get().createChangeSet("Set Top Team Work Definition");
      changes.setSoleAttributeValue(teamDef, AtsAttributeTypes.WorkflowDefinition,
         IAtsWorkDefinitionAdmin.TeamWorkflowDefaultDefinitionId);
      changes.execute();

      // load top ai into cache
      Artifact topAiArt =
         ArtifactQuery.getArtifactFromToken(AtsArtifactToken.TopActionableItem, AtsClientService.get().getAtsBranch());
      IAtsActionableItem aia = AtsClientService.get().getConfigItem(topAiArt);
      changes.setSoleAttributeValue(aia, AtsAttributeTypes.Actionable, false);
      changes.execute();

      AtsWorkDefinitionSheetProviders.initializeDatabase(new XResultData(false), "ats");

      AtsGroup.AtsAdmin.getArtifact().persist(getClass().getSimpleName());
      AtsGroup.AtsTempAdmin.getArtifact().persist(getClass().getSimpleName());

      ActivityLogJaxRsService.createActivityType(AtsActivity.ATSNAVIGATEITEM);

      createUserCreationDisabledConfig();

      createSafetyConfig();
   }

   private void createUserCreationDisabledConfig() {
      AtsClientService.get().setConfigValue(AtsUtilCore.USER_CREATION_DISABLED,
         Collections.toString(";", Arrays.asList(TokenFactory.createArtifactTypeTokenString(AtsArtifactTypes.Action),
            TokenFactory.createArtifactTypeTokenString(AtsArtifactTypes.TeamWorkflow))));
   }

   private void createSafetyConfig() {
      List<String> versions = new ArrayList<>();
      AtsConfigOperation operation = new AtsConfigOperation("Configure Safety For ATS",
         AtsArtifactToken.SafetyTeamDefinition, versions, AtsArtifactToken.SafetyActionableItem);
      Operations.executeWorkAndCheckStatus(operation);
   }

   public static void createAtsFolders() throws OseeCoreException {
      BranchId atsBranch = AtsClientService.get().getAtsBranch();
      SkynetTransaction transaction = TransactionManager.createTransaction(atsBranch, "Create ATS Folders");

      Artifact headingArt = OseeSystemArtifacts.getOrCreateArtifact(AtsArtifactToken.HeadingFolder, atsBranch);
      if (!headingArt.hasParent()) {
         Artifact rootArt = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(atsBranch);
         rootArt.addChild(headingArt);
         headingArt.persist(transaction);
      }
      for (ArtifactToken token : Arrays.asList(AtsArtifactToken.TopActionableItem, AtsArtifactToken.TopTeamDefinition,
         AtsArtifactToken.WorkDefinitionsFolder)) {
         Artifact art = OseeSystemArtifacts.getOrCreateArtifact(token, atsBranch);
         headingArt.addChild(art);
         art.persist(transaction);
      }

      transaction.execute();

      Response response = AtsClientService.getConfigEndpoint().createUpdateConfig();
      try {
         String message = Lib.inputStreamToString((InputStream) response.getEntity());
         if (message.toLowerCase().contains("error")) {
            throw new OseeStateException("Error found in ATS configuration [%s]", message);
         }
      } catch (IOException ex) {
         throw new OseeWrappedException(ex);
      }
   }

   public static void organizePrograms(IArtifactType programType, ArtifactToken programFolderToken) {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsClientService.get().getAtsBranch(), "Organize Programs");
      Artifact programFolder =
         OseeSystemArtifacts.getOrCreateArtifact(programFolderToken, AtsClientService.get().getAtsBranch());
      programFolder.persist(transaction);
      for (Artifact programArt : ArtifactQuery.getArtifactListFromType(programType,
         AtsClientService.get().getAtsBranch())) {
         if (!programFolder.getChildren().contains(programArt)) {
            programFolder.addChild(programArt);
         }
      }
      transaction.execute();
   }
}