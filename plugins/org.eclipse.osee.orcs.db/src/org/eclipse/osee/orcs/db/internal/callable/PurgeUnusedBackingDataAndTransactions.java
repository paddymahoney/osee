/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.callable;

import static org.eclipse.osee.jdbc.JdbcConstants.JDBC__MAX_FETCH_SIZE;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConnection;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.jdbc.OseePreparedStatement;

/**
 * Purge artifact, attribute, and relation versions that are not addressed or nonexistent and purge empty transactions.
 * Additionally purge search tags referencing non-existent gammas.
 * 
 * @author Ryan D. Brooks
 */
public class PurgeUnusedBackingDataAndTransactions {

   private static final String OBSOLETE_TAGS =
      "select gamma_id from osee_search_tags tag where not exists (select 1 from osee_attribute att where tag.gamma_id = att.gamma_id)";

   private static final String NOT_ADDRESSESED_GAMMAS =
      "select gamma_id from %s t1 where not exists (select 1 from osee_txs txs1 where t1.gamma_id = txs1.gamma_id union all select 1 from osee_txs_archived txs2 where t1.gamma_id = txs2.gamma_id)";

   private static final String EMPTY_TRANSACTIONS =
      "select branch_id, transaction_id from osee_tx_details txd where not exists (select 1 from osee_txs txs1 where txs1.branch_id = txd.branch_id and txs1.transaction_id = txd.transaction_id union all select 1 from osee_txs_archived txs2 where txs2.branch_id = txd.branch_id and txs2.transaction_id = txd.transaction_id)";

   private static final String NONEXISTENT_GAMMAS = "SELECT branch_id, gamma_id FROM %s txs WHERE " + //
   "NOT EXISTS (SELECT 1 FROM osee_attribute att WHERE txs.gamma_id = att.gamma_id union all " + //
   "SELECT 1 FROM osee_artifact art WHERE txs.gamma_id = art.gamma_id union all " + //
   "SELECT 1 FROM osee_relation_link rel WHERE txs.gamma_id = rel.gamma_id)";

   private static final String DELETE_GAMMAS = "DELETE FROM %s WHERE gamma_id = ?";
   private static final String DELETE_GAMMAS_BY_BRANCH = "DELETE FROM %s WHERE branch_id = ? and gamma_id = ?";

   private static final String DELETE_EMPTY_TRANSACTIONS =
      "DELETE FROM osee_tx_details WHERE branch_id = ? and transaction_id = ?";

   private static final String GET_INVALID_ART_REFERENCES =
      "select item.gamma_id from %s item where not exists (select 1 from osee_artifact art where art.art_id = item.%s)";

   private static final String GET_INVALID_ART_REFERENCES_ACL =
      "select item.art_id from osee_artifact_acl item where not exists (select 1 from osee_artifact art where art.art_id = item.art_id)";

   private static final String DELETE_ACL = "DELETE FROM osee_artifact_acl WHERE art_id = ?";

   private final JdbcClient jdbcClient;

   public PurgeUnusedBackingDataAndTransactions(JdbcClient jdbcClient) {
      this.jdbcClient = jdbcClient;
   }

   private int purgeNotAddressedGammas(JdbcConnection connection, String tableName) throws OseeCoreException {
      String selectSql = String.format(NOT_ADDRESSESED_GAMMAS, tableName);
      return purgeGammas(connection, selectSql, tableName);
   }

   private int purgeAddressedButNonexistentGammas(JdbcConnection connection, String tableName) throws OseeCoreException {
      JdbcStatement chStmt = jdbcClient.getStatement(connection);
      OseePreparedStatement nonexistentGammas;

      try {
         String sql = String.format(NONEXISTENT_GAMMAS, tableName);
         chStmt.runPreparedQuery(JDBC__MAX_FETCH_SIZE, sql);
         nonexistentGammas =
            jdbcClient.getBatchStatement(connection, String.format(DELETE_GAMMAS_BY_BRANCH, tableName));

         while (chStmt.next()) {
            nonexistentGammas.addToBatch(chStmt.getLong("branch_id"), chStmt.getLong("gamma_id"));
         }
      } finally {
         chStmt.close();
      }

      return nonexistentGammas.execute();
   }

   private int purgeEmptyTransactions(JdbcConnection connection) throws OseeCoreException {
      JdbcStatement chStmt = jdbcClient.getStatement(connection);
      OseePreparedStatement emptyTransactions;

      try {
         chStmt.runPreparedQuery(JDBC__MAX_FETCH_SIZE, EMPTY_TRANSACTIONS);
         emptyTransactions = jdbcClient.getBatchStatement(connection, DELETE_EMPTY_TRANSACTIONS);

         while (chStmt.next()) {
            int txId = chStmt.getInt("transaction_id");
            emptyTransactions.addToBatch(chStmt.getLong("branch_id"), txId);
         }
      } finally {
         chStmt.close();
      }

      return emptyTransactions.execute();
   }

   public int deleteObsoleteTags(JdbcConnection connection) {
      return purgeGammas(connection, OBSOLETE_TAGS, "osee_search_tags");
   }

   private int purgeInvalidArtfactReferences(JdbcConnection connection, String table, String artColumn) {
      String selectSql = String.format(GET_INVALID_ART_REFERENCES, table, artColumn);
      return purgeGammas(connection, selectSql, table);
   }

   private int purgeInvalidArtfactReferencesAcl(JdbcConnection connection) {
      return purgeData(connection, GET_INVALID_ART_REFERENCES_ACL, DELETE_ACL, "art_id");
   }

   private int purgeGammas(JdbcConnection connection, String selectSql, String table) {
      return purgeData(connection, selectSql, String.format(DELETE_GAMMAS, table), "gamma_id");
   }

   private int purgeData(JdbcConnection connection, String selectSql, String purgeSQL, String purgeColumn) {
      JdbcStatement chStmt = jdbcClient.getStatement(connection);
      OseePreparedStatement dropGammas;

      try {
         chStmt.runPreparedQuery(JDBC__MAX_FETCH_SIZE, selectSql);
         dropGammas = jdbcClient.getBatchStatement(connection, purgeSQL);

         while (chStmt.next()) {
            dropGammas.addToBatch(chStmt.getLong(purgeColumn));
         }
      } finally {
         chStmt.close();
      }
      return dropGammas.execute();
   }

   public int[] purge(JdbcConnection connection) throws OseeCoreException {
      int[] counts = new int[11];
      int i = 0;

      counts[i++] = purgeNotAddressedGammas(connection, "osee_artifact");
      counts[i++] = purgeNotAddressedGammas(connection, "osee_attribute");
      counts[i++] = purgeNotAddressedGammas(connection, "osee_relation_link");
      counts[i++] = purgeInvalidArtfactReferences(connection, "osee_relation_link", "a_art_id");
      counts[i++] = purgeInvalidArtfactReferences(connection, "osee_relation_link", "b_art_id");
      counts[i++] = purgeInvalidArtfactReferences(connection, "osee_attribute", "art_id");
      counts[i++] = purgeInvalidArtfactReferencesAcl(connection);
      counts[i++] = deleteObsoleteTags(connection);
      counts[i++] = purgeAddressedButNonexistentGammas(connection, "osee_txs");
      counts[i++] = purgeAddressedButNonexistentGammas(connection, "osee_txs_archived");
      counts[i++] = purgeEmptyTransactions(connection);

      return counts;
   }
}