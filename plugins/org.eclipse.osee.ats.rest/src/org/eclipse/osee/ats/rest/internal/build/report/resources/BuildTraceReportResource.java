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
package org.eclipse.osee.ats.rest.internal.build.report.resources;

import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.rest.internal.AtsApplication;
import org.eclipse.osee.ats.rest.internal.build.report.model.AtsWorkflowData;
import org.eclipse.osee.ats.rest.internal.build.report.parser.ArtIdParser;
import org.eclipse.osee.ats.rest.internal.build.report.parser.AtsAbstractSAXParser.AtsDataHandler;
import org.eclipse.osee.ats.rest.internal.build.report.parser.AtsWorkflowDataParser;
import org.eclipse.osee.ats.rest.internal.build.report.table.BuildTraceTable;
import org.eclipse.osee.ats.rest.internal.build.report.util.InputFilesUtil;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.data.GraphReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author John Misinco
 * @author Megumi Telles
 */
@Path("buildTraceReport")
public class BuildTraceReportResource {

   @Context
   UriInfo uriInfo;
   @Context
   Request request;

   @GET
   @Path("{programId}/{buildId}")
   @Produces(MediaType.TEXT_HTML)
   public StreamingOutput getBuildReport(@PathParam("programId") String programId, @PathParam("buildId") final String buildId, @DefaultValue("UNKNOWN") @QueryParam("program") final String programName, @DefaultValue("UNKNOWN") @QueryParam("build") final String buildName) throws OseeCoreException {
      OrcsApi orcsApi = AtsApplication.getOrcsApi();
      final GraphReadable graph = orcsApi.getGraph(null);
      final QueryFactory queryFactory = orcsApi.getQueryFactory(null);
      final IOseeBranch branch = getBaselineBranch(buildId, queryFactory);

      return new StreamingOutput() {

         @Override
         public void write(OutputStream output) throws WebApplicationException {

            final BuildTraceTable buildTraceTable = new BuildTraceTable(output);
            try {
               buildTraceTable.initializeTraceReportTable(programName, buildName);
               AtsDataHandler<AtsWorkflowData> handler = new AtsDataHandler<AtsWorkflowData>() {

                  @Override
                  public void handleData(AtsWorkflowData data) {
                     if (data.getWorkflowBuildId().equals(buildId)) {
                        String pcrId = data.getWorkflowPcrId();
                        try {
                           Collection<Integer> artIds = ArtIdParser.getArtIds(pcrId);
                           Map<ArtifactReadable, List<ArtifactReadable>> requirementsToTests =
                              new LinkedHashMap<ArtifactReadable, List<ArtifactReadable>>();

                           if (Conditions.hasValues(artIds)) {
                              List<ArtifactReadable> requirements =
                                 queryFactory.fromBranch(branch).andLocalIds(artIds).getResults().getList();

                              for (ArtifactReadable requirement : requirements) {
                                 List<ArtifactReadable> verifiers =
                                    graph.getRelatedArtifacts(CoreRelationTypes.Verification__Verifier, requirement).getList();
                                 requirementsToTests.put(requirement, verifiers);
                              }

                           }
                           buildTraceTable.addRpcrToTable(pcrId, requirementsToTests, uriInfo);

                        } catch (OseeCoreException ex) {
                           AtsApplication.getLogger().error(ex, "Error handling AtsWorkflowData");
                        }
                     }
                  }
               };

               AtsWorkflowDataParser parser = new AtsWorkflowDataParser(InputFilesUtil.getWorkflowFile(), handler);
               parser.parseDocument();
               buildTraceTable.close();
            } catch (OseeCoreException ex) {
               throw new WebApplicationException(ex);
            }
         }
      };

   }

   private IOseeBranch getBaselineBranch(String buildId, QueryFactory queryFactory) throws OseeCoreException {
      QueryBuilder builder = queryFactory.fromBranch(CoreBranches.COMMON);
      ArtifactReadable buildArt = builder.andGuidsOrHrids(buildId).getResults().getExactlyOne();
      List<AttributeReadable<String>> branchGuids = buildArt.getAttributes(AtsAttributeTypes.BaselineBranchGuid);
      Conditions.checkNotNullOrEmpty(branchGuids, "branchGuid");
      String baselineBranchGuid = branchGuids.iterator().next().getValue();
      IOseeBranch branch = TokenFactory.createBranch(baselineBranchGuid, "TraceReport Branch");
      return branch;
   }
}