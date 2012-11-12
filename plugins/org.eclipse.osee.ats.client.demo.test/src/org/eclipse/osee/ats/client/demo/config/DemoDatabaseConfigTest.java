/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.demo.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.client.demo.DemoCISBuilds;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * Test unit for {@link DemoDatabaseConfig}
 * 
 * @author Donald G. Dunne
 */
public class DemoDatabaseConfigTest {

   @BeforeClass
   public static void validateDbInit() throws OseeCoreException {
      DemoDbUtil.checkDbInitAndPopulateSuccess();
   }

   @org.junit.Test
   public void test_SAW_Bld_1__creation() throws OseeCoreException {
      Branch branch = BranchManager.getBranchByGuid(DemoSawBuilds.SAW_Bld_1.getGuid());
      Assert.assertNotNull(branch);
      Artifact programRoot = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(branch);
      Assert.assertNotNull(programRoot);

      Assert.assertEquals(8, programRoot.getChildren().size());
      List<String> topFolders = new ArrayList<String>();
      topFolders.addAll(Arrays.asList("Hardware Requirements", "Integration Tests", "SAW Product Decomposition",
         "Software Requirements", "Subsystem Requirements", "System Requirements", "Validation Tests",
         "Verification Tests"));

      for (Artifact child : programRoot.getChildren()) {
         if (child.getName().equals("Integration Tests")) {
            Assert.assertEquals(3, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Hardware Requirements")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("SAW Product Decomposition")) {
            Assert.assertEquals(14, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Software Requirements")) {
            Assert.assertEquals(1, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Subsystem Requirements")) {
            Assert.assertEquals(1, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("System Requirements")) {
            Assert.assertEquals(6, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Validation Tests")) {
            Assert.assertEquals(3, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Verification Tests")) {
            Assert.assertEquals(3, child.getChildren().size());
            topFolders.remove(child.getName());
         }
      }

      Assert.assertEquals(String.format("All top folders not created; missing [%s]", topFolders), 0, topFolders.size());
   }

   @org.junit.Test
   public void test_SAW_Bld_1__SoftwareRequirementImport() throws OseeCoreException {
      Branch branch = BranchManager.getBranchByGuid(DemoSawBuilds.SAW_Bld_1.getGuid());
      Assert.assertNotNull(branch);

      Collection<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SystemRequirement, branch);
      Assert.assertEquals(67, artifacts.size());

      artifacts = ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SubsystemRequirement, branch);
      Assert.assertEquals(59, artifacts.size());

      artifacts = ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SoftwareRequirement, branch);
      Assert.assertEquals(24, artifacts.size());

   }

   @org.junit.Test
   public void test_CIS_Bld_1__creation() throws OseeCoreException {
      Branch branch = BranchManager.getBranchByGuid(DemoCISBuilds.CIS_Bld_1.getGuid());
      Assert.assertNotNull(branch);
      Artifact programRoot = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(branch);
      Assert.assertNotNull(programRoot);

      Assert.assertEquals(8, programRoot.getChildren().size());
      List<String> topFolders = new ArrayList<String>();
      topFolders.addAll(Arrays.asList("Hardware Requirements", "Integration Tests", "SAW Product Decomposition",
         "Software Requirements", "Subsystem Requirements", "System Requirements", "Validation Tests",
         "Verification Tests"));

      for (Artifact child : programRoot.getChildren()) {
         if (child.getName().equals("Integration Tests")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Hardware Requirements")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("SAW Product Decomposition")) {
            Assert.assertEquals(14, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Software Requirements")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Subsystem Requirements")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("System Requirements")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Validation Tests")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         } else if (child.getName().equals("Verification Tests")) {
            Assert.assertEquals(0, child.getChildren().size());
            topFolders.remove(child.getName());
         }
      }

      Assert.assertEquals(String.format("All top folders not created; missing [%s]", topFolders), 0, topFolders.size());
   }

   @org.junit.Test
   public void test_CIS_Bld_1__SoftwareRequirementImport() throws OseeCoreException {
      Branch branch = BranchManager.getBranchByGuid(DemoCISBuilds.CIS_Bld_1.getGuid());
      Assert.assertNotNull(branch);

      Collection<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SystemRequirement, branch);
      Assert.assertEquals(0, artifacts.size());

      artifacts = ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SubsystemRequirement, branch);
      Assert.assertEquals(0, artifacts.size());

      artifacts = ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.SoftwareRequirement, branch);
      Assert.assertEquals(0, artifacts.size());

   }

}