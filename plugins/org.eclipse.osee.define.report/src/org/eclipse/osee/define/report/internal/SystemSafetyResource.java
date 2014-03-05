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
package org.eclipse.osee.define.report.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.osee.app.OseeAppletPage;
import org.eclipse.osee.define.report.OseeDefineResourceTokens;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsApi;

/**
 * @author Ryan D. Brooks
 * @author David W. Miller
 */
@Path("define")
public final class SystemSafetyResource {
   private final OrcsApi orcsApi;
   private Log logger;

   public SystemSafetyResource(Log logger, OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   /**
    * Produce the System Safety Report
    * 
    * @param branchGuid The Branch to run the System Safety Report on.
    * @param codeRoot The root directory accessible on the server for the code traces.
    * @return Produces a streaming xml file containing the System Safety Report
    */
   @Path("safety")
   @GET
   @Produces(MediaType.APPLICATION_XML)
   public Response getSystemSafetyReport(@QueryParam("branch") String branchGuid, @QueryParam("code_root") String codeRoot) {
      StreamingOutput streamingOutput = new SafetyStreamingOutput(logger, orcsApi, branchGuid, codeRoot);
      ResponseBuilder builder = Response.ok(streamingOutput);
      builder.header("Content-Disposition", "attachment; filename=" + "Safety_Report.xml");
      return builder.build();
   }

   /**
    * Provides the user interface for the System Safety Report
    * 
    * @return Returns the html page for the System Safety Report
    */
   @Path("ui/safety")
   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getApplet() {
      OseeAppletPage pageUtil = new OseeAppletPage(orcsApi.getQueryFactory(null).branchQuery());
      return pageUtil.realizeApplet(orcsApi.getResourceRegistry(), OseeDefineResourceTokens.SystemSafetyReportHtml);
   }
}