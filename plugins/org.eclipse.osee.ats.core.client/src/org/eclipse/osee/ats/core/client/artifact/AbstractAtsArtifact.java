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
package org.eclipse.osee.ats.core.client.artifact;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractAtsArtifact extends Artifact implements IAtsObject {

   public AbstractAtsArtifact(String guid, BranchId branch, ArtifactTypeId artifactType) {
      super(guid, branch, artifactType);
   }

   public Artifact getParentAtsArtifact() throws OseeCoreException {
      return null;
   }

   @Override
   public ArtifactToken getStoreObject() {
      return this;
   }
}