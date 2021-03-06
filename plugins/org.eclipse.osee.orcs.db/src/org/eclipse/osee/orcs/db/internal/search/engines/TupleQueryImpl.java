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
package org.eclipse.osee.orcs.db.internal.search.engines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.Tuple2Type;
import org.eclipse.osee.framework.core.data.Tuple3Type;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.jdk.core.type.TriConsumer;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.orcs.db.internal.sql.join.IdJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;
import org.eclipse.osee.orcs.search.TupleQuery;

/**
 * @author Angel Avila
 */
public class TupleQueryImpl implements TupleQuery {

   private final JdbcClient jdbcClient;
   private final SqlJoinFactory sqlJoinFactory;

   private static final String SELECT_E2_FROM_E1 =
      "select e2, value from osee_txs txs, osee_tuple2 app, osee_key_value where app.tuple_type = ? and e1 = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1 and e2 = key";

   private static final String SELECT_E2_BY_TUPLE_TYPE =
      "select distinct e2, value from osee_txs txs, osee_tuple2 app, osee_key_value where tuple_type = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1 and e2 = key";

   private static final String SELECT_E2_BY_TUPLE_TYPE_RAW =
      "select distinct e2 from osee_txs txs, osee_tuple2 app where tuple_type = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1 and e1 = ?";

   private static final String SELECT_KEY_VALUE_FROM_BRANCH_VIEW =
      "SELECT distinct e2, value from osee_tuple2 app, osee_txs txs1, osee_key_value where app.e1 = ? and tuple_type = ? and app.gamma_id = txs1.gamma_id and txs1.branch_id = ? AND txs1.tx_current = 1 and app.e2 = key";

   private static final String SELECT_TUPLE3_E1_BY_TUPLE_TYPE =
      "select distinct e1, value from osee_txs txs, osee_tuple3 app, osee_key_value where tuple_type = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1 and e1 = key";

   private static final String SELECT_TUPLE3_E3_BY_TUPLE_TYPE =
      "select distinct e3, value from osee_txs txs, osee_tuple3 app, osee_key_value where tuple_type = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1 and e3 = key";

   private static final String SELECT_TUPLE3_E3_FROM_E1 =
      "select e3, value from osee_txs txs, osee_tuple3 app, osee_key_value where app.tuple_type = ? and e1 = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1 and e3 = key";

   private static final String SELECT_TUPLE3_GAMMA_FROM_E1 =
      "select app.gamma_id from osee_txs txs, osee_tuple3 app where app.tuple_type = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1 and e1 = ?";

   private static final String SELECT_TUPLE3_E2_FROM_E3 =
      "select distinct e2 from osee_txs txs, osee_tuple3 app, osee_key_value where app.tuple_type = ? and e3 = ? and app.gamma_id = txs.gamma_id and branch_id = ? and tx_current = 1";

   private static final String SELECT_TUPLE3_COUNT_FROM_E3 =
      "select count(1) from osee_tuple3 where tuple_type=? and e3 = ?";

   private static final String SELECT_TUPLE2_COUNT_FROM_E1_E2 =
      "select count(1) from osee_tuple2 where tuple_type=? and e1 = ? and e2 = ?";

   private static final String SELECT_APPLIC_FOR_ART =
      "SELECT distinct e2, value FROM osee_artifact art, osee_txs txs1, osee_tuple2 app, osee_txs txs2, osee_key_value WHERE art_id = ? and art.gamma_id = txs1.gamma_id and txs1.branch_id = ? AND txs1.tx_current in (1,2) and tuple_type = 2 AND e2 = txs1.app_id AND app.gamma_id = txs2.gamma_id AND txs2.branch_id = txs1.branch_id AND txs2.tx_current = 1 AND e2 = key";

   private static final String SELECT_APPLIC_FOR_ARTS =
      "SELECT art_id, key, value FROM osee_join_id jid, osee_artifact art, osee_txs txs1, osee_key_value WHERE jid.query_id = ? and jid.id = art_id and art.gamma_id = txs1.gamma_id and txs1.branch_id = ? AND txs1.tx_current <> ? AND txs1.app_id = key";

   private static final String SELECT_TUPLE2_BY_TUPLE_TYPE =
      "select distinct e1, e2 from osee_txs txs, osee_tuple2 app where tuple_type = ? and txs.gamma_id = app.gamma_id and branch_id = ? and tx_current = 1";

   TupleQueryImpl(JdbcClient jdbcClient, SqlJoinFactory sqlJoinFactory) {
      this.jdbcClient = jdbcClient;
      this.sqlJoinFactory = sqlJoinFactory;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <E1, E2> Iterable<E2> getTuple2(Tuple2Type<E1, E2> tupleType, BranchId branchId, E1 e1) {
      Map<Long, String> consumer = new TreeMap<>();
      getTuple2NamedId(tupleType, branchId, e1, (e2, value) -> consumer.put(e2, value));
      return (Iterable<E2>) consumer.values();
   }

   @Override
   public <E1, E2> Iterable<Long> getTuple2Raw(Tuple2Type<E1, E2> tupleType, BranchId branchId, E1 e1) {
      List<Long> consumer = new ArrayList<Long>();
      runQuery("e2", consumer, SELECT_E2_BY_TUPLE_TYPE_RAW, tupleType, branchId, e1);
      return consumer;
   }

   @Override
   public <E1, E2> void getTuple2NamedId(Tuple2Type<E1, E2> tupleType, BranchId branchId, E1 e1, BiConsumer<Long, String> consumer) {
      runQuery(consumer, SELECT_E2_FROM_E1, "e2", tupleType, e1, branchId);
   }

   @Override
   public <E1, E2> void getTuple2KeyValuePair(Tuple2Type<E1, E2> tupleType, E1 e1, BranchId branch, BiConsumer<Long, String> consumer) {
      runQuery(consumer, SELECT_KEY_VALUE_FROM_BRANCH_VIEW, "e2", e1, tupleType, branch);
   }

   @Override
   public <E1, E2> void getTuple2UniqueE2Pair(Tuple2Type<E1, E2> tupleType, BranchId branchId, BiConsumer<Long, String> consumer) {
      runQuery(consumer, SELECT_E2_BY_TUPLE_TYPE, "e2", tupleType, branchId);
   }

   @Override
   public <E1, E2> void getTupleType2ForArtifactId(ArtifactId artId, BranchId branchId, BiConsumer<Long, String> consumer) {
      runQuery(consumer, SELECT_APPLIC_FOR_ART, "e2", artId, branchId);
   }

   @Override
   public <E1, E2> boolean doesTuple2Exist(Tuple2Type<E1, E2> tupleType, E1 e1, E2 e2) {
      return jdbcClient.fetch(0, SELECT_TUPLE2_COUNT_FROM_E1_E2, tupleType, e1, e2) > 0;
   }

   @Override
   public <E1, E2> void getTuple2ForArtifactIds(Tuple2Type<E1, E2> tupleType, Collection<? extends ArtifactId> artIds, BranchId branchId, TriConsumer<ArtifactId, Long, String> consumer) {
      IdJoinQuery idJoin = sqlJoinFactory.createIdJoinQuery();
      for (ArtifactId artId : artIds) {
         idJoin.add(artId);
      }
      idJoin.store();

      jdbcClient.runQuery(
         stmt -> consumer.accept(ArtifactId.valueOf(stmt.getLong("art_id")), stmt.getLong("key"),
            stmt.getString("value")),
         SELECT_APPLIC_FOR_ARTS, idJoin.getQueryId(), branchId, TxChange.NOT_CURRENT.getValue());
      idJoin.delete();
   }

   //////  Tuple3 //////
   @Override
   public <E1, E2, E3> void getTuple3UniqueE1Pair(Tuple3Type<E1, E2, E3> tupleType, BranchId branchId, BiConsumer<Long, String> consumer) {
      runQuery(consumer, SELECT_TUPLE3_E1_BY_TUPLE_TYPE, "e1", tupleType, branchId);
   }

   @Override
   public <E1, E2, E3> void getTuple3UniqueE3Pair(Tuple3Type<E1, E2, E3> tupleType, BranchId branchId, BiConsumer<Long, String> consumer) {
      runQuery(consumer, SELECT_TUPLE3_E3_BY_TUPLE_TYPE, "e3", tupleType, branchId);
   }

   @Override
   public <E1, E2, E3> void getTuple3NamedId(Tuple3Type<E1, E2, E3> tupleType, BranchId branchId, Long e1, BiConsumer<Long, String> consumer) {
      runQuery(consumer, SELECT_TUPLE3_E3_FROM_E1, "e3", tupleType, e1, branchId);
   }

   @Override
   public <E1, E2, E3> void getTuple3GammaFromE1(Tuple3Type<E1, E2, E3> tupleType, BranchId branchId, Long e1, List<Long> consumer) {
      runQuery("gamma_id", consumer, SELECT_TUPLE3_GAMMA_FROM_E1, tupleType, e1, branchId);
   }

   @Override
   public <E1, E2, E3> void getTuple3E2FromE3(Tuple3Type<E1, E2, E3> tupleType, BranchId branchId, Long e3, List<Long> consumer) {
      runQuery("e2", consumer, SELECT_TUPLE3_E2_FROM_E3, tupleType, e3, branchId);
   }

   @Override
   public <E1, E2> void getTuple2E1E2Pair(Tuple2Type<E1, E2> tupleType, BranchId branchId, BiConsumer<Long, Long> consumer) {
      runQuery(consumer, SELECT_TUPLE2_BY_TUPLE_TYPE, "e1", "e2", tupleType, branchId);
   }

   @Override
   public <E1, E2, E3> boolean doesTuple3E3Exist(Tuple3Type<E1, E2, E3> tupleType, Long e3) {
      return jdbcClient.fetch(0, SELECT_TUPLE3_COUNT_FROM_E3, tupleType, e3) > 0;
   }

   private void runQuery(BiConsumer<Long, String> consumer, String query, String column, Object... data) {
      jdbcClient.runQuery(stmt -> consumer.accept(stmt.getLong(column), stmt.getString("value")), query, data);
   }

   private void runQuery(BiConsumer<Long, Long> consumer, String query, String column1, String column2, Object... data) {
      jdbcClient.runQuery(stmt -> consumer.accept(stmt.getLong(column1), stmt.getLong(column2)), query, data);
   }

   private void runQuery(String column, List<Long> consumer, String query, Object... data) {
      jdbcClient.runQuery(stmt -> consumer.add(stmt.getLong(column)), query, data);
   }

}