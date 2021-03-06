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
package org.eclipse.osee.framework.skynet.core.relation.sorters;

import static org.eclipse.osee.framework.core.enums.RelationSorter.LEXICOGRAPHICAL_ASC;
import static org.eclipse.osee.framework.core.enums.RelationSorter.LEXICOGRAPHICAL_DESC;
import static org.eclipse.osee.framework.core.enums.RelationSorter.UNORDERED;
import static org.eclipse.osee.framework.core.enums.RelationSorter.USER_DEFINED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.enums.RelationSorter;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.skynet.core.mocks.DataFactory;
import org.eclipse.osee.framework.skynet.core.relation.order.IRelationSorter;
import org.eclipse.osee.framework.skynet.core.relation.sorters.LexicographicalRelationSorter.SortMode;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Roberto E. Escobar
 */
@RunWith(Parameterized.class)
public class RelationSorterTest {

   private final String message;
   private final IRelationSorter sorter;
   private final RelationSorter expectedOrderId;
   private final List<IArtifact> expectedOrder;
   private final List<String> currentItems;
   private final List<IArtifact> itemsToOrder;

   public RelationSorterTest(String message, IRelationSorter sorter, RelationSorter expectedOrderId, List<String> currentItems, List<IArtifact> itemsToOrder, List<IArtifact> expectedOrder) {
      this.sorter = sorter;
      this.message = message;
      this.expectedOrderId = expectedOrderId;
      this.currentItems = currentItems;
      this.itemsToOrder = itemsToOrder;
      this.expectedOrder = expectedOrder;
   }

   @Test
   public void testSorterId() {
      Assert.assertNotNull(message, sorter.getSorterId());
      Assert.assertEquals(message, expectedOrderId, sorter.getSorterId());
   }

   @Test
   public void testSort() {
      List<IArtifact> actualToOrder = new ArrayList<>();
      actualToOrder.addAll(itemsToOrder);
      sorter.sort(actualToOrder, currentItems);

      Assert.assertEquals(message, expectedOrder.size(), actualToOrder.size());
      for (int index = 0; index < expectedOrder.size(); index++) {
         Assert.assertEquals(message + " - index:" + index, expectedOrder.get(index).getName(),
            actualToOrder.get(index).getName());
      }
   }

   @Parameters
   public static Collection<Object[]> data() {
      Collection<Object[]> data = new ArrayList<>();
      data.add(createUnorderedSortTest("4", "2", "1", "5"));
      data.add(createUnorderedSortTest("$", "a", "!", "2"));
      data.add(createLexicographicalTest(SortMode.ASCENDING, "1", "2", "3", "4"));
      data.add(createLexicographicalTest(SortMode.ASCENDING, "a", "b", "c", "d"));
      data.add(createLexicographicalTest(SortMode.ASCENDING, "!", "1", "a", "b"));

      data.add(createLexicographicalTest(SortMode.DESCENDING, "4", "3", "2", "1"));
      data.add(createLexicographicalTest(SortMode.DESCENDING, "d", "c", "b", "a"));
      data.add(createLexicographicalTest(SortMode.DESCENDING, "b", "a", "1", "!"));

      data.add(getTestUserDefined("1", "2", "3", "4"));
      return data;
   }

   private static Object[] createUnorderedSortTest(String... names) {
      IArtifact art1 = DataFactory.createArtifact(names[0], GUID.create());
      IArtifact art2 = DataFactory.createArtifact(names[1], GUID.create());
      IArtifact art3 = DataFactory.createArtifact(names[2], GUID.create());
      IArtifact art4 = DataFactory.createArtifact(names[3], GUID.create());

      List<IArtifact> artifacts = Arrays.asList(art1, art2, art3, art4);
      return new Object[] {"Unordered Test", new UnorderedRelationSorter(), UNORDERED, null, artifacts, artifacts};
   }

   private static Object[] createLexicographicalTest(SortMode mode, String... names) {
      IArtifact art1 = DataFactory.createArtifact(names[0], GUID.create());
      IArtifact art2 = DataFactory.createArtifact(names[1], GUID.create());
      IArtifact art3 = DataFactory.createArtifact(names[2], GUID.create());
      IArtifact art4 = DataFactory.createArtifact(names[3], GUID.create());

      RelationSorter orderId = mode == SortMode.ASCENDING ? LEXICOGRAPHICAL_ASC : LEXICOGRAPHICAL_DESC;

      List<IArtifact> itemsToOrder = Arrays.asList(art3, art1, art4, art2);
      List<IArtifact> expectedOrder = Arrays.asList(art1, art2, art3, art4);
      return new Object[] {
         "Lex Test " + mode.name(),
         new LexicographicalRelationSorter(mode),
         orderId,
         null,
         itemsToOrder,
         expectedOrder};
   }

   private static Object[] getTestUserDefined(String... names) {
      IArtifact art1 = DataFactory.createArtifact(names[0], GUID.create());
      IArtifact art2 = DataFactory.createArtifact(names[1], GUID.create());
      IArtifact art3 = DataFactory.createArtifact(names[2], GUID.create());
      IArtifact art4 = DataFactory.createArtifact(names[3], GUID.create());

      List<IArtifact> itemsToOrder = Arrays.asList(art2, art1, art3, art4);
      List<IArtifact> expectedOrder = Arrays.asList(art1, art2, art3, art4);
      List<String> relatives = Artifacts.toGuids(Arrays.asList(art1, art2, art3, art4));
      return new Object[] {
         "UserDefined",
         new UserDefinedRelationSorter(),
         USER_DEFINED,
         relatives,
         itemsToOrder,
         expectedOrder};
   }

}
