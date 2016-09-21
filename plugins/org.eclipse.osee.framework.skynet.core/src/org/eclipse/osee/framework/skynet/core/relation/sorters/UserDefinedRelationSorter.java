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

import static org.eclipse.osee.framework.core.enums.RelationSorter.USER_DEFINED;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.RelationSorter;
import org.eclipse.osee.framework.skynet.core.relation.order.IRelationSorter;

/**
 * @author Andrew M. Finkbeiner
 */
public class UserDefinedRelationSorter implements IRelationSorter {

   @Override
   public RelationSorter getSorterId() {
      return USER_DEFINED;
   }

   @Override
   public void sort(List<? extends ArtifactToken> relatives, List<String> relativeSequence) {
      if (relatives.size() > 1) {
         Collections.sort(relatives, new UserDefinedOrderComparator(relativeSequence));
      }
   }
}
