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
package org.eclipse.osee.ats.api.workdef;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.enums.DeletionFlag;

/**
 * @author Donald G. Dunne
 */
public interface IRelationResolver {

   Collection<ArtifactToken> getRelated(ArtifactId artifact, RelationTypeSide relationType);

   Collection<ArtifactToken> getRelated(ArtifactId artifact, RelationTypeSide relationType, IArtifactType artifactType);

   <T extends IAtsObject> Collection<T> getRelated(IAtsObject atsObject, RelationTypeSide relationType, Class<T> clazz);

   <T extends IAtsObject> Collection<T> getRelated(IAtsObject atsObject, RelationTypeSide relationType, DeletionFlag flag, Class<T> clazz);

   boolean areRelated(ArtifactId artifact1, RelationTypeSide relationType, ArtifactId artifact2);

   boolean areRelated(IAtsObject atsObject1, RelationTypeSide relationType, IAtsObject atsObject2);

   ArtifactToken getRelatedOrNull(ArtifactId artifact, RelationTypeSide relationType);

   ArtifactToken getRelatedOrNull(IAtsObject atsObject, RelationTypeSide relationType);

   <T> T getRelatedOrNull(IAtsObject atsObject, RelationTypeSide relationType, Class<T> clazz);

   int getRelatedCount(IAtsWorkItem workItem, RelationTypeSide relationType);

   Collection<ArtifactToken> getRelatedArtifacts(IAtsWorkItem workItem, RelationTypeSide relationTypeSide);

   Collection<ArtifactToken> getRelated(IAtsObject atsObject, RelationTypeSide relationTypeSide);

   Collection<ArtifactToken> getRelatedArtifacts(ArtifactId artifact, RelationTypeSide relationTypeSide);

   Collection<ArtifactToken> getChildren(ArtifactId artifact, IArtifactType artifactType);

   Collection<ArtifactToken> getChildren(ArtifactId artifact);

   ArtifactToken getParent(ArtifactId artifact);

   int getRelatedCount(ArtifactToken artifact, RelationTypeSide relationTypeSide);

   Collection<Long> getRelatedIds(ArtifactId artifact, RelationTypeSide relationTypeSide);

}
