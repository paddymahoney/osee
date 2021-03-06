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
package org.eclipse.osee.orcs.db.internal.search.engines;

import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.OptionsUtil;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.ObjectType;
import org.eclipse.osee.orcs.db.internal.sql.QueryType;
import org.eclipse.osee.orcs.db.internal.sql.SqlContext;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactQuerySqlWriter extends AbstractSqlWriter {

   private final BranchId branch;

   public ArtifactQuerySqlWriter(Log logger, SqlJoinFactory joinFactory, JdbcClient jdbcClient, SqlContext context, QueryType queryType, BranchId branch) {
      super(logger, joinFactory, jdbcClient, context, queryType);
      this.branch = branch;
   }

   private void writeSelectHelper() throws OseeCoreException {
      String txAlias = getLastAlias(TableEnum.TXS_TABLE, ObjectType.ARTIFACT);
      String artAlias = getLastAlias(TableEnum.ARTIFACT_TABLE);

      write("SELECT%s ", getSqlHint());
      if (OptionsUtil.isHistorical(getOptions())) {
         write("%s.transaction_id, %s.art_id, %s.branch_id", txAlias, artAlias, txAlias);
      } else {
         write("%s.art_id, %s.branch_id", artAlias, txAlias);
      }
   }

   @Override
   public void writeSelect(Iterable<SqlHandler<?>> handlers) throws OseeCoreException {
      if (isCountQueryType()) {
         if (OptionsUtil.isHistorical(getOptions())) {
            write("SELECT count(xTable.art_id) FROM (\n ");
            writeSelectHelper();
         } else {
            String artAlias = getLastAlias(TableEnum.ARTIFACT_TABLE);
            write("SELECT%s count(%s.art_id)", getSqlHint(), artAlias);
         }
      } else {
         writeSelectHelper();
      }
   }

   @Override
   public void writeGroupAndOrder() throws OseeCoreException {
      if (!isCountQueryType()) {
         String txAlias = getLastAlias(TableEnum.TXS_TABLE, ObjectType.ARTIFACT);
         String artAlias = getLastAlias(TableEnum.ARTIFACT_TABLE);
         write("\n ORDER BY %s.art_id, %s.branch_id", artAlias, txAlias);
      } else {
         if (OptionsUtil.isHistorical(getOptions())) {
            write("\n) xTable");
         }
      }
   }

   @Override
   public String getTxBranchFilter(String txsAlias) {
      boolean allowDeleted = //
         OptionsUtil.areDeletedArtifactsIncluded(getOptions()) || //
            OptionsUtil.areDeletedAttributesIncluded(getOptions()) || //
            OptionsUtil.areDeletedRelationsIncluded(getOptions());

      StringBuilder sb = new StringBuilder();
      writeTxFilter(txsAlias, sb, allowDeleted);
      if (branch.isValid()) {
         sb.append(" AND ");
         sb.append(txsAlias);
         sb.append(".branch_id = ?");
         addParameter(branch);
      } else {
         throw new OseeArgumentException("getTxBranchFilter: branch uuid must be > 0");
      }
      return sb.toString();
   }

   @Override
   public String getTxBranchFilter(String txsAlias, boolean allowDeleted) {
      StringBuilder sb = new StringBuilder();
      writeTxFilter(txsAlias, sb, allowDeleted);
      if (branch.isValid()) {
         sb.append(" AND ");
         sb.append(txsAlias);
         sb.append(".branch_id = ?");
         addParameter(branch);
      }
      return sb.toString();
   }

   private void writeTxFilter(String txsAlias, StringBuilder sb, boolean allowDeleted) {
      if (OptionsUtil.isHistorical(getOptions())) {
         if (allowDeleted) {
            removeDanglingSeparator(AND_WITH_NEWLINES);
            removeDanglingSeparator(" AND ");
         } else {
            sb.append(txsAlias);
            sb.append(".mod_type <> ");
            sb.append(String.valueOf(ModificationType.DELETED.getValue()));
         }
      } else {
         sb.append(txsAlias);
         sb.append(".tx_current");
         if (allowDeleted) {
            sb.append(" <> ");
            sb.append(String.valueOf(TxChange.NOT_CURRENT.getValue()));
         } else {
            sb.append(" = ");
            sb.append(String.valueOf(TxChange.CURRENT.getValue()));
         }
      }
   }

   @Override
   public String getWithClauseTxBranchFilter(String txsAlias, boolean deletedPredicate) throws OseeCoreException {
      StringBuilder sb = new StringBuilder();

      if (deletedPredicate) {
         boolean allowDeleted = //
            OptionsUtil.areDeletedArtifactsIncluded(getOptions()) || //
               OptionsUtil.areDeletedAttributesIncluded(getOptions()) || //
               OptionsUtil.areDeletedRelationsIncluded(getOptions());
         writeTxFilter(txsAlias, sb, allowDeleted);
      } else {
         if (OptionsUtil.isHistorical(getOptions())) {
            sb.append(txsAlias);
            sb.append(".transaction_id <= ?");
            addParameter(OptionsUtil.getFromTransaction(getOptions()));
         }
      }
      if (branch.isValid()) {
         if (sb.length() > 0) {
            sb.append(" AND ");
         }
         sb.append(txsAlias);
         sb.append(".branch_id = ?");
         addParameter(branch);
      }
      return sb.toString();
   }
}
