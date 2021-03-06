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
package org.eclipse.osee.framework.core.data;

import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.NamedId;
import org.eclipse.osee.framework.jdk.core.type.NamedIdBase;
import org.eclipse.osee.framework.jdk.core.util.GUID;

/**
 * @author Ryan D. Brooks
 * @author Donald G. Dunne
 */
public interface ArtifactToken extends ArtifactId, HasArtifactType, HasBranch, NamedId {
   default ArtifactTypeId getArtifactTypeId() {
      return null;
   }

   public static ArtifactToken valueOf(Id id, BranchId branch) {
      return valueOf(id.getId(), GUID.create(), null, branch, null);
   }

   public static ArtifactToken valueOf(ArtifactId id, BranchId branch) {
      return valueOf(id.getId(), GUID.create(), null, branch, null);
   }

   public static ArtifactToken valueOf(long id, BranchId branch) {
      return valueOf(id, GUID.create(), null, branch, null);
   }

   public static ArtifactToken valueOf(long id, String name, BranchId branch) {
      return valueOf(id, GUID.create(), name, branch, null);
   }

   public static ArtifactToken valueOf(long id, String name, BranchId branch, IArtifactType artifactType) {
      return valueOf(id, GUID.create(), name, branch, artifactType);
   }

   public static ArtifactToken valueOf(long id, String guid, String name, BranchId branch, IArtifactType artifactType) {
      final class ArtifactTokenImpl extends NamedIdBase implements ArtifactToken {
         private final BranchId branch;
         private final IArtifactType artifactType;
         private final String guid;

         public ArtifactTokenImpl(Long id, String guid, String name, BranchId branch, IArtifactType artifactType) {
            super(id, name);
            this.branch = branch;
            this.artifactType = artifactType;
            this.guid = guid;
         }

         @Override
         public IArtifactType getArtifactType() {
            return artifactType;
         }

         @Override
         public BranchId getBranch() {
            return branch;
         }

         @Override
         public String getGuid() {
            return guid;
         }

         @Override
         public Long getUuid() {
            return getId();
         }

         @Override
         public boolean equals(Object obj) {
            boolean equal = super.equals(obj);
            if (equal && obj instanceof HasBranch) {
               return isOnSameBranch((HasBranch) obj);
            }
            return equal;
         }
      }
      return new ArtifactTokenImpl(id, guid, name, branch, artifactType);
   }

}