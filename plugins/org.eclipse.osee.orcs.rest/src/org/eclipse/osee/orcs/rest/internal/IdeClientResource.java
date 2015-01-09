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
package org.eclipse.osee.orcs.rest.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.framework.core.data.OseeCodeVersion;
import org.eclipse.osee.orcs.rest.model.IdeVersion;

/**
 * @author Roberto E. Escobar
 */
@Path("ide")
public class IdeClientResource {

   @GET
   @Path("versions")
   @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public IdeVersion getSupportedVersions() {
      IdeVersion versions = new IdeVersion();
      versions.addVersion(OseeCodeVersion.getVersion());
      return versions;
   }

}