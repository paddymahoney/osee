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
package org.eclipse.osee.ats.core.config;

import java.util.Collection;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G Dunne
 */
public class TeamDefinitionUtility implements ITeamDefinitionUtility {

   @Override
   public Collection<IAtsTeamDefinition> getImpactedTeamDefs(Collection<IAtsActionableItem> aias) throws OseeCoreException {
      return TeamDefinitions.getImpactedTeamDefs(aias);
   }

}
