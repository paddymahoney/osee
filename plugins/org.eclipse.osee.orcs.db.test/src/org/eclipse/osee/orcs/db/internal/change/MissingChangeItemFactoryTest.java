/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.change;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.executor.admin.HasCancellation;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.RelationId;
import org.eclipse.osee.framework.core.data.RelationTypeId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.DemoBranches;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeItemUtil;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.DataLoader;
import org.eclipse.osee.orcs.core.ds.DataLoaderFactory;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.LoadDataHandler;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.ds.VersionData;
import org.eclipse.osee.orcs.search.ApplicabilityQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author John Misinco
 */
@RunWith(value = Parameterized.class)
public class MissingChangeItemFactoryTest {

   // @formatter:off
   @Mock private DataLoaderFactory dataLoaderFactory;
   @Mock private DataLoader sourceDataLoader;
   @Mock private DataLoader destDataLoader;
   @Mock private OrcsSession session;
   @Mock private HasCancellation cancellation;
   @Mock private ApplicabilityQuery applicQuery;
   // @formatter:on

   private MissingChangeItemFactory changeItemFactory;
   private final List<ChangeItem> changes;
   private final List<ChangeItem> expectedMissingChanges;
   private final List<AttributeData> attributeData;
   private final List<ArtifactData> artifactData;
   private final List<ArtifactData> destArtifactData;
   private final List<RelationData> relationData;
   private final HashMap<Long, ApplicabilityToken> applicMap;
   private final BranchId sourceBranch = DemoBranches.SAW_Bld_2;
   private final BranchId destBranch = DemoBranches.SAW_Bld_1;
   private final TransactionToken sourceTx = TransactionToken.valueOf(11, sourceBranch);
   private final TransactionToken destTx = TransactionToken.valueOf(12, destBranch);

   @Parameters
   public static Collection<Object[]> data() {
      List<Object[]> params = new LinkedList<>();
      params.add(testCase_missingAttribute());
      params.add(testCase_missingRelation());
      params.add(testCase_deletedAttribute());
      params.add(testCase_artifactDeletedAttribute());
      return params;
   }

   public MissingChangeItemFactoryTest(List<ChangeItem> changes, List<ChangeItem> expectedMissingChanges, List<AttributeData> attributeData, List<ArtifactData> artifactData, List<RelationData> relationData, List<ArtifactData> destArtifactData, HashMap<Long, ApplicabilityToken> applicMap) {
      this.changes = changes;
      this.expectedMissingChanges = expectedMissingChanges;
      this.attributeData = attributeData;
      this.artifactData = artifactData;
      this.relationData = relationData;
      this.destArtifactData = destArtifactData;
      this.applicMap = applicMap;
   }

   @SuppressWarnings("unchecked")
   @Before
   public void init() throws OseeCoreException {
      MockitoAnnotations.initMocks(this);

      String sessionGuid = GUID.create();
      when(session.getGuid()).thenReturn(sessionGuid);
      when(dataLoaderFactory.newDataLoaderFromIds(any(OrcsSession.class), eq(sourceBranch),
         any(Collection.class))).thenReturn(sourceDataLoader);
      when(dataLoaderFactory.newDataLoaderFromIds(any(OrcsSession.class), eq(destBranch),
         any(Collection.class))).thenReturn(destDataLoader);
      when(applicQuery.getApplicabilityTokens(any(BranchId.class), any(BranchId.class))).thenReturn(applicMap);
      changeItemFactory = new MissingChangeItemFactoryImpl(dataLoaderFactory);
   }

   @Test
   public void testCreateMissingChanges() throws OseeCoreException {
      doAnswer(new Answer<Object>() {

         @Override
         public Object answer(InvocationOnMock invocation) throws Throwable {
            LoadDataHandler handler = (LoadDataHandler) invocation.getArguments()[1];
            if (artifactData != null) {
               for (ArtifactData data : artifactData) {
                  handler.onData(data);
               }
            }

            if (attributeData != null) {
               for (AttributeData data : attributeData) {
                  handler.onData(data);
               }
            }

            if (relationData != null) {
               for (RelationData data : relationData) {
                  handler.onData(data);
               }
            }

            return null;
         }
      }).when(sourceDataLoader).load(any(HasCancellation.class), any(LoadDataHandler.class));

      doAnswer(new Answer<Object>() {

         @Override
         public Object answer(InvocationOnMock invocation) throws Throwable {
            LoadDataHandler handler = (LoadDataHandler) invocation.getArguments()[1];

            if (destArtifactData != null) {
               for (ArtifactData data : destArtifactData) {
                  handler.onData(data);
               }
            }

            return null;
         }
      }).when(destDataLoader).load(any(HasCancellation.class), any(LoadDataHandler.class));

      Collection<ChangeItem> results =
         changeItemFactory.createMissingChanges(cancellation, session, changes, sourceTx, destTx, applicQuery);
      if (expectedMissingChanges == null) {
         Assert.assertTrue(results.isEmpty());
      } else {
         Assert.assertEquals(expectedMissingChanges.size(), results.size());
         for (ChangeItem change : results) {
            ChangeItem expected = getMatchingChangeItem(change);
            Assert.assertNotNull(expected);
            Assert.assertEquals(expected.getClass().getSimpleName(), change.getClass().getSimpleName());
            Assert.assertEquals(expected.getCurrentVersion().getGammaId(), change.getCurrentVersion().getGammaId());
            Assert.assertTrue(expected.getCurrentVersion().getModType() == change.getCurrentVersion().getModType());
         }
      }
   }

   /**
    * This tests when a modified attribute causes its artifact and non-modified attribute to be introduced
    */
   private static Object[] testCase_missingAttribute() {
      List<ChangeItem> changes = new LinkedList<>();
      final int ci1AttrId = 1;
      final int artId = 3;
      final long missingGamma = 9L;
      long artGamma = 7L;
      HashMap<Long, ApplicabilityToken> applicMap = new HashMap<>();
      applicMap.put(1L, ApplicabilityToken.BASE);

      ChangeItem ci1 = ChangeItemUtil.newAttributeChange(AttributeId.valueOf(ci1AttrId), AttributeTypeId.valueOf(2L),
         ArtifactId.valueOf(artId), GammaId.valueOf(4L), ModificationType.MODIFIED, Strings.EMPTY_STRING,
         ApplicabilityToken.BASE);
      changes.add(ci1);

      List<AttributeData> attrDatas = new LinkedList<>();
      AttributeData attrData1 = createAttributeData(artId, ci1AttrId, 1L, ModificationType.MODIFIED);
      AttributeData attrData2 = createAttributeData(artId, ci1AttrId + 1, missingGamma, ModificationType.INTRODUCED);
      attrDatas.add(attrData1);
      attrDatas.add(attrData2);

      List<ArtifactData> artData = new LinkedList<>();
      ArtifactData artData1 = createArtifactData(artId, artGamma, ModificationType.NEW);
      artData.add(artData1);

      List<ChangeItem> expected = new LinkedList<>();
      expected.add(createExpected(attrData2));
      expected.add(createExpected(artData1));

      return new Object[] {changes, expected, attrDatas, artData, null, null, applicMap};
   }

   /**
    * create a relation change item who's artifact and other attributes and relations need to be introduced
    */
   private static Object[] testCase_missingRelation() {
      List<ChangeItem> changes = new LinkedList<>();
      final int ci1AttrId = 22;
      final long missingGamma = 9L;
      long artGamma = 7L;

      RelationId relId = RelationId.valueOf(6L);
      int artA = 65;
      int artB = 2;
      GammaId srcGamma = GammaId.valueOf(7L);
      HashMap<Long, ApplicabilityToken> applicMap = new HashMap<>();
      applicMap.put(1L, ApplicabilityToken.BASE);
      ChangeItem ci1 = ChangeItemUtil.newRelationChange(relId, RelationTypeId.SENTINEL, srcGamma, ModificationType.NEW,
         ArtifactId.valueOf(artA), ArtifactId.valueOf(artB), "", ApplicabilityToken.BASE);
      changes.add(ci1);

      List<AttributeData> attrDatas = new LinkedList<>();
      AttributeData attrData1 = createAttributeData(artA, ci1AttrId, 1L, ModificationType.MODIFIED);
      AttributeData attrData2 = createAttributeData(artA, ci1AttrId + 1, missingGamma, ModificationType.INTRODUCED);
      attrDatas.add(attrData1);
      attrDatas.add(attrData2);

      List<ArtifactData> artData = new LinkedList<>();
      ArtifactData artData1 = createArtifactData(artA, artGamma, ModificationType.NEW);
      artData.add(artData1);

      List<RelationData> relDatas = new LinkedList<>();
      RelationData relData1 = createRelationData(11, artA, artB + 1, 88, ModificationType.NEW);
      relDatas.add(relData1);

      List<ArtifactData> destArtData = new LinkedList<>();
      ArtifactData destArt = createArtifactData(artB + 1, 99, ModificationType.NEW);
      destArtData.add(destArt);

      List<ChangeItem> expected = new LinkedList<>();
      expected.add(createExpected(attrData1));
      expected.add(createExpected(attrData2));
      expected.add(createExpected(artData1));
      expected.add(createExpected(relData1));

      return new Object[] {changes, expected, attrDatas, artData, relDatas, destArtData, applicMap};
   }

   /**
    * create a deleted attribute who's artifact needs to be introduced
    */
   private static Object[] testCase_deletedAttribute() {
      List<ChangeItem> changes = new LinkedList<>();
      final int artId = 3;
      HashMap<Long, ApplicabilityToken> applicMap = new HashMap<>();
      applicMap.put(1L, ApplicabilityToken.BASE);
      ChangeItem ci1 = ChangeItemUtil.newAttributeChange(AttributeId.valueOf(22), AttributeTypeId.valueOf(2L),
         ArtifactId.valueOf(artId), GammaId.valueOf(4L), ModificationType.DELETED, Strings.EMPTY_STRING,
         ApplicabilityToken.BASE);
      changes.add(ci1);

      List<AttributeData> attrDatas = new LinkedList<>();
      AttributeData attrData1 = createAttributeData(artId, 33, 1L, ModificationType.MODIFIED);
      AttributeData attrData2 = createAttributeData(artId, 44 + 1, 67L, ModificationType.NEW);
      attrDatas.add(attrData1);
      attrDatas.add(attrData2);

      List<ArtifactData> artData = new LinkedList<>();
      ArtifactData artData1 = createArtifactData(artId, 89L, ModificationType.NEW);
      artData.add(artData1);

      List<ChangeItem> expected = new LinkedList<>();
      expected.add(createExpected(attrData1));
      expected.add(createExpected(attrData2));
      expected.add(createExpected(artData1));

      return new Object[] {changes, expected, attrDatas, artData, null, null, applicMap};
   }

   /**
    * create an artifact_deleted attribute who's artifact needs to be introduced deleted
    */
   private static Object[] testCase_artifactDeletedAttribute() {
      List<ChangeItem> changes = new LinkedList<>();
      final ArtifactId artId = ArtifactId.valueOf(3);
      HashMap<Long, ApplicabilityToken> applicMap = new HashMap<>();
      applicMap.put(1L, ApplicabilityToken.BASE);
      ChangeItem ci1 = ChangeItemUtil.newAttributeChange(AttributeId.valueOf(22), AttributeTypeId.valueOf(2L), artId,
         GammaId.valueOf(4L), ModificationType.ARTIFACT_DELETED, Strings.EMPTY_STRING, ApplicabilityToken.BASE);
      changes.add(ci1);

      List<AttributeData> attrDatas = new LinkedList<>();
      AttributeData attrData1 =
         createAttributeData(artId.getId().intValue(), 33, 1L, ModificationType.ARTIFACT_DELETED);
      AttributeData attrData2 =
         createAttributeData(artId.getId().intValue(), 44 + 1, 67L, ModificationType.ARTIFACT_DELETED);
      attrDatas.add(attrData1);
      attrDatas.add(attrData2);

      List<ArtifactData> artData = new LinkedList<>();
      ArtifactData artData1 = createArtifactData(artId.getId().intValue(), 89L, ModificationType.DELETED);
      artData.add(artData1);

      List<ChangeItem> expected = new LinkedList<>();
      expected.add(createExpected(attrData1));
      expected.add(createExpected(attrData2));
      expected.add(createExpected(artData1));

      return new Object[] {changes, expected, attrDatas, artData, null, null, applicMap};
   }

   private static ModificationType determineModType(OrcsData data) {
      if (data.getModType().matches(ModificationType.DELETED, ModificationType.ARTIFACT_DELETED)) {
         return data.getModType();
      } else {
         return ModificationType.INTRODUCED;
      }
   }

   private static ChangeItem createExpected(RelationData data) {
      return ChangeItemUtil.newRelationChange(RelationId.valueOf(Long.valueOf(data.getLocalId())),
         RelationTypeId.SENTINEL, GammaId.valueOf(data.getVersion().getGammaId()), determineModType(data),
         ArtifactId.valueOf(data.getArtIdA()), ArtifactId.valueOf(data.getArtIdB()), "", ApplicabilityToken.BASE);
   }

   private static ChangeItem createExpected(AttributeData data) {
      return ChangeItemUtil.newAttributeChange(data, AttributeTypeId.SENTINEL, ArtifactId.valueOf(data.getArtifactId()),
         GammaId.valueOf(data.getVersion().getGammaId()), determineModType(data), "", ApplicabilityToken.BASE);
   }

   private static ChangeItem createExpected(ArtifactData data) {
      return ChangeItemUtil.newArtifactChange(ArtifactId.valueOf(data.getLocalId()), ArtifactTypeId.SENTINEL,
         GammaId.valueOf(data.getVersion().getGammaId()), determineModType(data), ApplicabilityToken.BASE);
   }

   private ChangeItem getMatchingChangeItem(ChangeItem item) {
      for (ChangeItem change : expectedMissingChanges) {
         if (change.getItemId().equals(item.getItemId()) && //
            change.getArtId().equals(item.getArtId()) && //
            change.getClass().getSimpleName().equals(item.getClass().getSimpleName())) {
            return change;
         }
      }
      return null;
   }

   private static AttributeData createAttributeData(int artId, int attrId, long gamma, ModificationType modType) {
      AttributeData data = mock(AttributeData.class);
      VersionData version = mock(VersionData.class);
      DataProxy proxy = mock(DataProxy.class);
      when(data.getModType()).thenReturn(modType);
      when(data.getVersion()).thenReturn(version);
      when(data.getArtifactId()).thenReturn(artId);
      when(data.getLocalId()).thenReturn(attrId);
      when(data.getDataProxy()).thenReturn(proxy);
      when(data.getVersion().getGammaId()).thenReturn(gamma);
      when(data.getApplicabilityId()).thenReturn(ApplicabilityId.BASE);
      return data;
   }

   private static ArtifactData createArtifactData(int artId, long gamma, ModificationType modType) {
      ArtifactData data = mock(ArtifactData.class);
      VersionData version = mock(VersionData.class);
      when(data.getModType()).thenReturn(modType);
      when(data.getLocalId()).thenReturn(artId);
      when(version.getGammaId()).thenReturn(gamma);
      when(data.getVersion()).thenReturn(version);
      when(data.getApplicabilityId()).thenReturn(ApplicabilityId.BASE);
      return data;
   }

   private static RelationData createRelationData(int relId, int artA, int artB, long gamma, ModificationType modType) {
      RelationData data = mock(RelationData.class);
      VersionData version = mock(VersionData.class);
      when(data.getLocalId()).thenReturn(relId);
      when(data.getArtIdA()).thenReturn(artA);
      when(data.getArtIdB()).thenReturn(artB);
      when(version.getGammaId()).thenReturn(gamma);
      when(data.getVersion()).thenReturn(version);
      when(data.getModType()).thenReturn(modType);
      when(data.getApplicabilityId()).thenReturn(ApplicabilityId.BASE);
      return data;
   }
}
