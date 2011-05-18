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

package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.utility.CsvArtifact;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Ryan D. Brooks
 */
public class NativeArtifactTest {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws Exception {
      Collection<Artifact> arts =
         ArtifactQuery.getArtifactListFromName(NativeArtifactTest.class.getSimpleName(), DemoSawBuilds.SAW_Bld_2,
            EXCLUDE_DELETED);
      new PurgeArtifacts(arts).execute();
   }

   @org.junit.Test
   public void testNativeArtifact() throws Exception {
      CsvArtifact csvArtifact = CsvArtifact.getCsvArtifact(getClass().getSimpleName(), DemoSawBuilds.SAW_Bld_2, true);
      assertNotNull(csvArtifact);
      Artifact artifact = csvArtifact.getArtifact();
      assertTrue(artifact.isAttributeTypeValid(CoreAttributeTypes.NativeContent));
      assertTrue(artifact.isAttributeTypeValid(CoreAttributeTypes.Extension));
   }

   @org.junit.Test
   public void testGetFileExtension() throws Exception {
      Artifact nativeArtifact = getNativeArtifact();
      assertTrue(nativeArtifact.getSoleAttributeValue(CoreAttributeTypes.Extension, "").equals("csv"));
   }

   @org.junit.Test
   public void testSetAndGetValueAsString() throws Exception {
      Artifact nativeArtifact = getNativeArtifact();
      nativeArtifact.setSoleAttributeFromString(CoreAttributeTypes.NativeContent, "hello world");
      nativeArtifact.persist();
      String content = nativeArtifact.getSoleAttributeValueAsString(CoreAttributeTypes.NativeContent, "");
      assertEquals("hello world", content);
   }

   @org.junit.Test
   public void testSetAndGetNativeContent() throws Exception {
      File file = OseeData.getFile(GUID.create() + ".txt");
      Lib.writeStringToFile("hello world", file);
      Artifact nativeArtifact = getNativeArtifact();
      nativeArtifact.setSoleAttributeFromStream(CoreAttributeTypes.NativeContent, new FileInputStream(file));
      nativeArtifact.persist();
      InputStream inputStream = null;
      try {
         inputStream = nativeArtifact.getSoleAttributeValue(CoreAttributeTypes.NativeContent, null);
         String content = Lib.inputStreamToString(inputStream);
         assertEquals("hello world", content);
      } finally {
         Lib.close(inputStream);
      }
   }

   private Artifact getNativeArtifact() throws Exception {
      return CsvArtifact.getCsvArtifact(getClass().getSimpleName(), DemoSawBuilds.SAW_Bld_2, false).getArtifact();
   }

}
