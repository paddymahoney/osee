/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.cpa;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.util.XResultData;

/**
 * @author Donald G. Dunne
 */
public interface IAtsCpaService {

   String getId();

   List<CpaProgram> getPrograms();

   String getConfigJson() throws Exception;

   URI getLocation(URI uri, String uuid);

   CpaPcr getPcr(String pcrId);

   Map<String, CpaPcr> getPcrsByIds(Collection<String> issueIds);

   Collection<CpaBuild> getBuilds(String programUuid);

   /**
    * Duplicate originatingPcrId for programUuid,versionUuid and return duplicatedPcrId
    */
   String duplicate(IAtsTeamWorkflow cpaWf, String programUuid, String versionUuid, String originatingPcrId, XResultData rd);

}
