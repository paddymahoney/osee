/*
 * Created on Jul 9, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.artifact.ActionableItemArtifact;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeStateException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.StaticIdManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

/**
 * @author Donald G. Dunne
 */
public class AtsFolderUtil {

   public enum AtsFolder {
      Ats_Heading("Action Tracking System", "ats.HeadingFolder", FOLDER_ARTIFACT),
      Teams("Teams", "osee.ats.TopTeamDefinition", TeamDefinitionArtifact.ARTIFACT_NAME),
      ActionableItem("Actionable Items", "osee.ats.TopActionableItem", ActionableItemArtifact.ARTIFACT_NAME),
      WorkFlow("Work Flows", "Work Flows", FOLDER_ARTIFACT),
      WorkRules("Work Rules", "Work Rules", FOLDER_ARTIFACT),
      WorkWidgets("Work Widgets", "Work Widgets", FOLDER_ARTIFACT),
      WorkPages("Work Pages", "Work Pages", FOLDER_ARTIFACT);
      protected final String displayName;
      protected final String staticId;
      protected final String artifactTypeName;

      private AtsFolder(String displayName, String staticId, String artifactTypeName) {
         this.displayName = displayName;
         this.staticId = staticId;
         this.artifactTypeName = artifactTypeName;
      }

      /**
       * @return the displayName
       */
      public String getDisplayName() {
         return displayName;
      }

      /**
       * @return the staticId
       */
      public String getStaticId() {
         return staticId;
      }
   }

   public static Map<AtsFolder, Artifact> folderMap = new HashMap<AtsFolder, Artifact>();
   public static String FOLDER_ARTIFACT = "Folder";

   public static Artifact getFolder(AtsFolder atsFolder) throws OseeCoreException {
      if (!folderMap.containsKey(atsFolder)) {
         Artifact artifact =
               StaticIdManager.getSingletonArtifact(atsFolder.artifactTypeName, atsFolder.staticId,
                     AtsUtil.getAtsBranch());
         if (artifact == null) {
            throw new OseeStateException(String.format("Can't retrieve Ats folder [%s]", atsFolder.displayName));
         }
         folderMap.put(atsFolder, artifact);
      }
      return folderMap.get(atsFolder);
   }

   public static void createAtsFolders() throws OseeCoreException {
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch());

      Artifact headingArt =
            Artifacts.getOrCreateArtifact(AtsUtil.getAtsBranch(), AtsFolderUtil.FOLDER_ARTIFACT,
                  AtsFolder.Ats_Heading.displayName);
      if (!headingArt.hasParent()) {
         Artifact rootArt = ArtifactQuery.getDefaultHierarchyRootArtifact(AtsUtil.getAtsBranch());
         rootArt.addChild(headingArt);
         StaticIdManager.setSingletonAttributeValue(headingArt, AtsFolder.Ats_Heading.staticId);
         headingArt.persistAttributesAndRelations(transaction);
      }

      for (AtsFolder atsFolder : AtsFolder.values()) {
         if (atsFolder == AtsFolder.Ats_Heading) continue;
         Artifact art =
               Artifacts.getOrCreateArtifact(AtsUtil.getAtsBranch(), atsFolder.artifactTypeName,
                     atsFolder.displayName);
         StaticIdManager.setSingletonAttributeValue(art, atsFolder.staticId);
         headingArt.addChild(art);
         art.persistAttributesAndRelations(transaction);
      }

      transaction.execute();
   }

}
