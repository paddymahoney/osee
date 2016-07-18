/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.change;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeItemUtil;
import org.eclipse.osee.framework.core.model.change.ChangeVersion;
import org.eclipse.osee.framework.jdk.core.type.DoubleKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConstants;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.db.internal.callable.AbstractDatastoreCallable;
import org.eclipse.osee.orcs.db.internal.sql.join.ExportImportJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 * @author Ryan Schmitt
 * @author Jeff C. Phillips
 */
public class LoadDeltasBetweenBranches extends AbstractDatastoreCallable<List<ChangeItem>> {
   // @formatter:off
   private static final String SELECT_ALL_SOURCE_ADDRESSING =
      "with\n"+
        "txsOuter as (select transaction_id, gamma_id, mod_type, app_id from osee_txs txs where \n" +
        "branch_id = ? and txs.tx_current <> ? and transaction_id <> ? AND \n" +
        "NOT EXISTS (SELECT 1 FROM osee_txs txs1 WHERE txs1.branch_id = ? AND txs1.transaction_id = ? \n" +
        "AND txs1.gamma_id = txs.gamma_id and txs1.mod_type = txs.mod_type and txs1.app_id = txs.app_id)) \n"+
      "SELECT 1 as table_type, attr_type_id as item_type_id, attr_id as item_id, art_id as item_first, 0 as item_second, value as item_value, item.gamma_id, mod_type, app_id \n" +
      "FROM osee_attribute item, txsOuter where txsOuter.gamma_id = item.gamma_id\n"+
      "UNION ALL\n"+
      "SELECT 2 as table_type, art_type_id as item_type_id, art_id as item_id, 0 as item_first, 0 as item_second, 'na' as item_value, item.gamma_id, mod_type, app_id \n" +
      "FROM osee_artifact item, txsOuter where txsOuter.gamma_id = item.gamma_id\n"+
      "UNION ALL\n"+
      "SELECT 3 as table_type, rel_link_type_id as item_type_id, rel_link_id as item_id,  a_art_id as item_first, b_art_id as item_second, rationale as item_value, item.gamma_id, mod_type, app_id \n" +
      "FROM osee_relation_link item, txsOuter where txsOuter.gamma_id = item.gamma_id";
     // @formatter:on
   private static final String SELECT_BASE_TX = "select baseline_transaction_id from osee_branch where branch_id = ?";
   private final BranchId sourceBranch, destinationBranch;
   private final BranchId mergeBranch;
   private final TransactionId mergeTxId;
   private final TransactionId destinationHeadTxId;
   private final SqlJoinFactory joinFactory;

   public LoadDeltasBetweenBranches(Log logger, OrcsSession session, JdbcClient jdbcClient, SqlJoinFactory joinFactory, BranchId sourceBranch, BranchId destinationBranch, TransactionId destinationHeadTxId, BranchId mergeBranch, TransactionId mergeTxId) {
      super(logger, session, jdbcClient);
      this.joinFactory = joinFactory;
      this.sourceBranch = sourceBranch;
      this.destinationBranch = destinationBranch;
      this.destinationHeadTxId = destinationHeadTxId;
      this.mergeBranch = mergeBranch;
      this.mergeTxId = mergeTxId;
   }

   private boolean hasMergeBranch() {
      return mergeBranch.isValid();
   }

   @Override
   public List<ChangeItem> call() throws Exception {
      Conditions.checkExpressionFailOnTrue(sourceBranch.equals(destinationBranch),
         "Unable to compute deltas between transactions on the same branch [%s]", sourceBranch);

      TransactionId sourceBaselineTxId = getJdbcClient().fetch(TransactionId.SENTINEL, SELECT_BASE_TX, sourceBranch);
      DoubleKeyHashMap<Integer, Integer, ChangeItem> newChangeData = this.loadSourceBranchChanges(sourceBaselineTxId);
      return loadItemsbyId(newChangeData, sourceBaselineTxId);
   }

   private List<ChangeItem> loadItemsbyId(DoubleKeyHashMap<Integer, Integer, ChangeItem> changeData, TransactionId sourceBaselineTxId) {
      ExportImportJoinQuery idJoin = joinFactory.createExportImportJoinQuery();
      try {
         for (Integer i : changeData.getKeySetOne()) {
            for (ChangeItem item : changeData.allValues(i)) {
               idJoin.add(Long.valueOf(i), Long.valueOf(item.getItemId()));
            }
         }
         idJoin.store();

         if (hasMergeBranch()) {
            loadCurrentVersionData(idJoin, changeData, mergeBranch, mergeTxId, true);
         }
         loadCurrentVersionData(idJoin, changeData, destinationBranch, destinationHeadTxId, false);

         loadNonCurrentSourceVersionData(idJoin, changeData, sourceBaselineTxId);

      } finally {
         idJoin.delete();
      }
      List<ChangeItem> list = new LinkedList<ChangeItem>(changeData.allValues());
      return list;
   }

   private DoubleKeyHashMap<Integer, Integer, ChangeItem> loadSourceBranchChanges(TransactionId sourceBaselineTxId) throws OseeCoreException {
      DoubleKeyHashMap<Integer, Integer, ChangeItem> hashChangeData =
         new DoubleKeyHashMap<Integer, Integer, ChangeItem>();

      Consumer<JdbcStatement> consumer = stmt -> {
         checkForCancelled();
         Long gammaId = stmt.getLong("gamma_id");
         ModificationType modType = ModificationType.getMod(stmt.getInt("mod_type"));
         ApplicabilityId appId = ApplicabilityId.valueOf(stmt.getLong("app_id"));
         int tableType = stmt.getInt("table_type");
         int itemId = stmt.getInt("item_id");
         Long itemTypeId = stmt.getLong("item_type_id");
         switch (tableType) {
            case 1:
               int artId = stmt.getInt("item_first");
               String value = stmt.getString("item_value");
               hashChangeData.put(1, itemId,
                  ChangeItemUtil.newAttributeChange(itemId, itemTypeId, artId, gammaId, modType, value, appId));
               break;

            case 2: {
               hashChangeData.put(2, itemId,
                  ChangeItemUtil.newArtifactChange(itemId, itemTypeId, gammaId, modType, appId));
               break;
            }
            case 3: {
               int aArtId = stmt.getInt("item_first");
               int bArtId = stmt.getInt("item_second");
               String rationale = stmt.getString("item_value");
               hashChangeData.put(3, itemId, ChangeItemUtil.newRelationChange(itemId, itemTypeId, gammaId, modType,
                  aArtId, bArtId, rationale, appId));
               break;
            }
         }
      };
      getJdbcClient().runQuery(consumer, JdbcConstants.JDBC__MAX_FETCH_SIZE, SELECT_ALL_SOURCE_ADDRESSING, sourceBranch,
         TxChange.NOT_CURRENT.getValue(), sourceBaselineTxId, sourceBranch, sourceBaselineTxId);

      return hashChangeData;
   }

   private void loadCurrentVersionData(ExportImportJoinQuery idJoin, DoubleKeyHashMap<Integer, Integer, ChangeItem> changesByItemId, BranchId txBranchId, TransactionId txId, boolean isMergeBranch) throws OseeCoreException {
      Consumer<JdbcStatement> consumer = stmt -> {
         checkForCancelled();

         Integer itemId = stmt.getInt("item_id");
         Integer tableType = stmt.getInt("table_type");
         ApplicabilityId appId = ApplicabilityId.valueOf(stmt.getLong("app_id"));
         Long gammaId = stmt.getLong("gamma_id");
         ChangeItem change = changesByItemId.get(tableType, itemId);

         if (isMergeBranch) {
            change.getNetChange().setGammaId(gammaId);
            change.getNetChange().setModType(ModificationType.MERGED);
            change.getNetChange().setApplicabilityId(appId);
         } else {
            change.getDestinationVersion().setModType(ModificationType.getMod(stmt.getInt("mod_type")));
            change.getDestinationVersion().setGammaId(gammaId);
            change.getDestinationVersion().setApplicabilityId(appId);
         }
      };

      String query =
         "select txs.gamma_id, txs.mod_type, txs.app_id, item.art_id as item_id, 2 as table_type from osee_join_export_import idj," + //
            " osee_artifact item, osee_txs txs where idj.query_id = ? and idj.id2 = item.art_id and idj.id1 = 2" + //
            " and item.gamma_id = txs.gamma_id and txs.tx_current <> ? and txs.branch_id = ? and txs.transaction_id <= ?" + //
            " union all select txs.gamma_id, txs.mod_type, txs.app_id, item.attr_id as item_id, 1 as table_type from osee_join_export_import idj," + //
            " osee_attribute item, osee_txs txs where idj.query_id = ? and idj.id2 = item.attr_id and idj.id1 = 1" + //
            " and item.gamma_id = txs.gamma_id and txs.tx_current <> ? and txs.branch_id = ? and txs.transaction_id <= ?" + //
            " union all select txs.gamma_id, txs.mod_type, txs.app_id, item.rel_link_id as item_id, 3 as table_type from osee_join_export_import idj," + //
            " osee_relation_link item, osee_txs txs where idj.query_id = ? and idj.id2 = item.rel_link_id and idj.id1 = 3" + //
            " and item.gamma_id = txs.gamma_id and txs.tx_current <> ? and txs.branch_id = ? and txs.transaction_id <= ?";

      getJdbcClient().runQuery(consumer, JdbcConstants.JDBC__MAX_FETCH_SIZE, query, idJoin.getQueryId(),
         TxChange.NOT_CURRENT.getValue(), txBranchId, txId, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(),
         txBranchId, txId, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(), txBranchId, txId);

   }

   private void loadNonCurrentSourceVersionData(ExportImportJoinQuery idJoin, DoubleKeyHashMap<Integer, Integer, ChangeItem> changesByItemId, TransactionId sourceBaselineTxId) throws OseeCoreException {
      try (JdbcStatement chStmt = getJdbcClient().getStatement()) {
         String query =
            "select * from (select null as value, item.art_id as item_id, txs.gamma_id, txs.mod_type, txs.app_id, txs.transaction_id, idj.id2, 2 as table_type from osee_join_export_import idj, " + //
               "osee_artifact item, osee_txs txs where idj.query_id = ? and idj.id2 = item.art_id and idj.id1 = 2 " + //
               "and item.gamma_id = txs.gamma_id and txs.tx_current = ? and txs.branch_id = ? " + //
               "union all select item.value as value, item.attr_id as item_id, txs.gamma_id, txs.mod_type, txs.app_id, txs.transaction_id, idj.id2, 1 as table_type from osee_join_export_import idj, " + //
               "osee_attribute item, osee_txs txs where idj.query_id = ? and idj.id2 = item.attr_id and idj.id1 = 1 " + //
               "and item.gamma_id = txs.gamma_id and txs.tx_current = ? and txs.branch_id = ? " + //
               "union all select null as value, item.rel_link_id as item_id, txs.gamma_id, txs.mod_type, txs.app_id, txs.transaction_id, idj.id2, 3 as table_type from osee_join_export_import idj, " + //
               "osee_relation_link item, osee_txs txs where idj.query_id = ? and idj.id2 = item.rel_link_id and idj.id1 = 3 " + //
               "and item.gamma_id = txs.gamma_id and txs.tx_current = ? and txs.branch_id = ?) t order by t.id2, t.transaction_id asc";

         chStmt.runPreparedQuery(JdbcConstants.JDBC__MAX_FETCH_SIZE, query, idJoin.getQueryId(),
            TxChange.NOT_CURRENT.getValue(), sourceBranch, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(),
            sourceBranch, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(), sourceBranch);

         int previousItemId = -1;
         boolean isFirstSet = false;
         while (chStmt.next()) {
            checkForCancelled();
            Integer itemId = chStmt.getInt("item_id");
            Integer tableType = chStmt.getInt("table_type");
            Long transactionId = chStmt.getLong("transaction_id");
            ApplicabilityId appId = ApplicabilityId.valueOf(chStmt.getLong("app_id"));
            ModificationType modType = ModificationType.getMod(chStmt.getInt("mod_type"));
            Long gammaId = chStmt.getLong("gamma_id");

            String value = chStmt.getString("value");

            ChangeItem change = changesByItemId.get(tableType, itemId);
            if (previousItemId != itemId) {
               isFirstSet = false;
            }
            if (sourceBaselineTxId.equals(transactionId)) {
               setVersionData(change.getBaselineVersion(), gammaId, modType, value, appId);
            } else if (!isFirstSet) {
               setVersionData(change.getFirstNonCurrentChange(), gammaId, modType, value, appId);
               isFirstSet = true;
            }
            previousItemId = itemId;
         }
      }
   }

   private void setVersionData(ChangeVersion versionedChange, Long gammaId, ModificationType modType, String value, ApplicabilityId appId) {
      // Tolerates the case of having more than one version of an item on a
      // baseline transaction by picking the most recent one
      if (versionedChange.getGammaId() == null || versionedChange.getGammaId().compareTo(gammaId) < 0) {
         versionedChange.setValue(value);
         versionedChange.setModType(modType);
         versionedChange.setGammaId(gammaId);
         versionedChange.setApplicabilityId(appId);
      }
   }
}