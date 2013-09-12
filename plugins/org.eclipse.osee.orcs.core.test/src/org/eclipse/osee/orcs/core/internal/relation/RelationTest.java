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
package org.eclipse.osee.orcs.core.internal.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.orcs.core.ds.OrcsData;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.internal.util.ValueProvider;
import org.eclipse.osee.orcs.data.RelationTypes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link Relation}
 * 
 * @author Roberto E. Escobar
 */
public class RelationTest {

   private static final long TYPE_UUID = 123124514L;

   // @formatter:off
   @Mock private ValueProvider<Branch, OrcsData> branchProvider; 
   @Mock private RelationTypes relationTypes;
   @Mock RelationData data;
   // @formatter:on

   private Relation relation;

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);

      relation = new Relation(relationTypes, data, branchProvider);

      when(data.getTypeUuid()).thenReturn(TYPE_UUID);
   }

   @Test
   public void testDelete() {
      assertFalse(relation.isDirty());
      relation.delete();

      verify(data).setModType(ModificationType.DELETED);
      assertTrue(relation.isDirty());
   }

   @Test
   public void testGetSetRationale() {
      assertFalse(relation.isDirty());
      Mockito.when(data.getRationale()).thenReturn("rationale");

      String rationale = relation.getRationale();

      verify(data).getRationale();
      assertEquals("rationale", rationale);
      assertFalse(relation.isDirty());

      relation.setRationale("new rationale");
      verify(data).setRationale("new rationale");
      verify(data).setModType(ModificationType.MODIFIED);
      assertTrue(relation.isDirty());
   }

   @Test
   public void testGetBranch() throws OseeCoreException {
      relation.getBranch();
      verify(branchProvider).get();
   }

   @Test
   public void testGetRelationType() throws OseeCoreException {
      relation.getRelationType();

      verify(relationTypes).getByUuid(TYPE_UUID);
   }

   @Test
   public void testGetModificationType() {
      relation.getModificationType();
      verify(data).getModType();
   }

   @Test
   public void testGetOrcsData() {
      assertEquals(data, relation.getOrcsData());
   }

   @Test
   public void testSetOrcsData() {
      relation.setOrcsData(data);

      verify(branchProvider).setOrcsData(data);
   }

   @Test
   public void testDirty() {
      relation.setDirty();
      assertTrue(relation.isDirty());

      relation.clearDirty();
      assertFalse(relation.isDirty());
   }

   @Test
   public void testGetLocalIdForSide() {
      when(data.getArtIdOn(RelationSide.SIDE_A)).thenReturn(45);
      when(data.getArtIdOn(RelationSide.SIDE_B)).thenReturn(33);

      assertEquals(45, relation.getLocalIdForSide(RelationSide.SIDE_A));
      assertEquals(33, relation.getLocalIdForSide(RelationSide.SIDE_B));
   }

   @Test
   public void testIsDeleteD() {
      when(data.getModType()).thenReturn(ModificationType.DELETED);
      assertTrue(relation.isDeleted());

      when(data.getModType()).thenReturn(ModificationType.ARTIFACT_DELETED);
      assertTrue(relation.isDeleted());

      when(data.getModType()).thenReturn(ModificationType.MODIFIED);
      assertFalse(relation.isDeleted());
   }

   @Test
   public void testIsOfType() throws OseeCoreException {
      IRelationType type1 = mock(IRelationType.class);
      IRelationType type2 = mock(IRelationType.class);

      when(relationTypes.getByUuid(TYPE_UUID)).thenReturn(type1);

      assertTrue(relation.isOfType(type1));
      assertFalse(relation.isOfType(type2));
   }

}