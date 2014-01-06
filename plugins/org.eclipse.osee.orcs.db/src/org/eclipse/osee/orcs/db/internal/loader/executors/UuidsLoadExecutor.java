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
package org.eclipse.osee.orcs.db.internal.loader.executors;

import java.util.Collection;
import org.eclipse.osee.executor.admin.HasCancellation;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.ArtifactJoinQuery;
import org.eclipse.osee.framework.database.core.CharJoinQuery;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.JoinUtility;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.LoadDataHandler;
import org.eclipse.osee.orcs.core.ds.Options;
import org.eclipse.osee.orcs.core.ds.OptionsUtil;
import org.eclipse.osee.orcs.db.internal.BranchIdProvider;
import org.eclipse.osee.orcs.db.internal.loader.LoadSqlContext;
import org.eclipse.osee.orcs.db.internal.loader.LoadUtil;
import org.eclipse.osee.orcs.db.internal.loader.SqlObjectLoader;
import org.eclipse.osee.orcs.db.internal.loader.criteria.CriteriaOrcsLoad;

/**
 * @author Roberto E. Escobar
 */
public class UuidsLoadExecutor extends AbstractLoadExecutor {

   private static final String GUIDS_TO_IDS =
      "SELECT art.art_id FROM osee_join_char_id jid, osee_artifact art WHERE jid.query_id = ? AND jid.id = art.guid";

   private final BranchIdProvider branchIdProvider;
   private final OrcsSession session;
   private final IOseeBranch branch;
   private final Collection<String> artifactIds;

   public UuidsLoadExecutor(SqlObjectLoader loader, IOseeDatabaseService dbService, BranchIdProvider branchIdProvider, OrcsSession session, IOseeBranch branch, Collection<String> artifactIds) {
      super(loader, dbService);
      this.branchIdProvider = branchIdProvider;
      this.session = session;
      this.branch = branch;
      this.artifactIds = artifactIds;
   }

   @Override
   public void load(HasCancellation cancellation, LoadDataHandler handler, CriteriaOrcsLoad criteria, Options options) throws OseeCoreException {
      checkCancelled(cancellation);
      if (!artifactIds.isEmpty()) {
         ArtifactJoinQuery join = createIdJoin(getDatabaseService(), options);
         LoadSqlContext loadContext = new LoadSqlContext(session, options, branch);
         int fetchSize = LoadUtil.computeFetchSize(artifactIds.size());
         getLoader().loadArtifacts(cancellation, handler, join, criteria, loadContext, fetchSize);
      }
   }

   private ArtifactJoinQuery createIdJoin(IOseeDatabaseService dbService, Options options) throws OseeCoreException {

      ArtifactJoinQuery toReturn = JoinUtility.createArtifactJoinQuery(dbService);

      CharJoinQuery guidJoin = JoinUtility.createCharJoinQuery(dbService, session.getGuid());
      try {
         for (String id : artifactIds) {
            guidJoin.add(id);
         }
         guidJoin.store();

         long branchId = branchIdProvider.getBranchId(branch);
         Integer transactionId = OptionsUtil.getFromTransaction(options);

         IOseeStatement chStmt = null;
         try {
            chStmt = dbService.getStatement();
            chStmt.runPreparedQuery(artifactIds.size(), GUIDS_TO_IDS, guidJoin.getQueryId());
            while (chStmt.next()) {
               Integer artId = chStmt.getInt("art_id");
               toReturn.add(artId, branchId, transactionId);
            }
         } finally {
            Lib.close(chStmt);
         }
      } finally {
         guidJoin.delete();
      }
      return toReturn;
   }
}