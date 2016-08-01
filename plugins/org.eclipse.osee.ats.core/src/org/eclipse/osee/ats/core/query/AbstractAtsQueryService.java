/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.query;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.query.IAtsQueryService;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.jdbc.JdbcService;
import org.eclipse.osee.jdbc.JdbcStatement;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractAtsQueryService implements IAtsQueryService {

   protected final JdbcService jdbcService;
   private final IAtsServices services;

   public AbstractAtsQueryService(JdbcService jdbcService, IAtsServices services) {
      this.jdbcService = jdbcService;
      this.services = services;
   }

   /**
    * Run query that returns art_ids of IAtsWorkItems to return
    */
   @Override
   public Collection<IAtsWorkItem> runQuery(String query, Object... data) {
      JdbcStatement chStmt = jdbcService.getClient().getStatement();
      List<Integer> ids = new LinkedList<Integer>();
      try {
         chStmt.runPreparedQuery(query, data);
         while (chStmt.next()) {
            ids.add(chStmt.getInt("art_id"));
         }
      } finally {
         chStmt.close();
      }
      List<IAtsWorkItem> workItems = new LinkedList<>();
      for (ArtifactId art : services.getQueryService().getArtifacts(ids, AtsUtilCore.getAtsBranch())) {
         if (services.getStoreService().isOfType(art, AtsArtifactTypes.AbstractWorkflowArtifact)) {
            IAtsWorkItem workItem = services.getWorkItemFactory().getWorkItem(art);
            if (workItem != null) {
               workItems.add(workItem);
            }
         }
      }
      return workItems;
   }

}