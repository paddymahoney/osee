/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.config;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.team.IAtsConfigItemFactory;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IArtifactType;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractConfigItemFactory implements IAtsConfigItemFactory {

   public final List<IArtifactType> atsConfigArtifactTypes;

   public AbstractConfigItemFactory() {
      atsConfigArtifactTypes = Arrays.asList(AtsArtifactTypes.Version, AtsArtifactTypes.TeamDefinition,
         AtsArtifactTypes.ActionableItem, AtsArtifactTypes.Country, AtsArtifactTypes.Program,
         AtsArtifactTypes.Insertion, AtsArtifactTypes.InsertionActivity, AtsArtifactTypes.AgileTeam,
         AtsArtifactTypes.AgileFeatureGroup, AtsArtifactTypes.WorkPackage);
   }

   @Override
   public List<IArtifactType> getAtsConfigArtifactTypes() {
      return atsConfigArtifactTypes;
   }

   @Override
   public abstract boolean isAtsConfigArtifact(ArtifactId artifact);

}
