/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.search;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 */
public interface BranchQueryBuilder<T> {

   T includeDeleted();

   T excludeDeleted();

   T includeDeleted(boolean enabled);

   boolean areDeletedIncluded();

   T includeArchived();

   T includeArchived(boolean enabled);

   T excludeArchived();

   boolean areArchivedIncluded();

   T andIds(Collection<? extends BranchId> ids) throws OseeCoreException;

   T andId(BranchId branchId);

   T andIsOfType(BranchType... branchType) throws OseeCoreException;

   T andStateIs(BranchState... branchState) throws OseeCoreException;

   T andNameEquals(String value) throws OseeCoreException;

   T andNamePattern(String pattern) throws OseeCoreException;

   T andIsChildOf(BranchId branch) throws OseeCoreException;

   T andIsAncestorOf(BranchId branch) throws OseeCoreException;

   T andIsMergeFor(BranchId source, BranchId destination);

   T andAssociatedArtId(ArtifactId artId);

}
