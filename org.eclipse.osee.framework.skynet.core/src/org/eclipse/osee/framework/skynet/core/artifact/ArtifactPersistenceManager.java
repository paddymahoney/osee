/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ARTIFACT_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ARTIFACT_VERSION_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTRIBUTE_VERSION_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.RELATION_LINK_VERSION_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.TRANSACTIONS_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.TRANSACTION_DETAIL_TABLE;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.DbUtil;
import org.eclipse.osee.framework.db.connection.core.schema.LocalAliasTable;
import org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase;
import org.eclipse.osee.framework.db.connection.core.transaction.AbstractDbTxTemplate;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.messaging.event.skynet.event.NetworkArtifactDeletedEvent;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.skynet.core.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.access.PermissionEnum;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactModifiedEvent.ModType;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeValueCriteria;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.RelatedToSearch;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeToTransactionOperation;
import org.eclipse.osee.framework.skynet.core.change.ModificationType;
import org.eclipse.osee.framework.skynet.core.change.TxChange;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.skynet.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.skynet.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.tagging.SystemTagDescriptor;
import org.eclipse.osee.framework.skynet.core.tagging.TagManager;
import org.eclipse.osee.framework.skynet.core.transaction.AbstractSkynetTxTemplate;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransactionBuilder;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransactionManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionDetailsType;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.data.ArtifactTransactionData;
import org.eclipse.osee.framework.skynet.core.utility.RemoteArtifactEventFactory;
import org.eclipse.osee.framework.ui.plugin.util.Result;

/**
 * @author Ryan D. Brooks
 * @author Robert A. Fisher
 */
public class ArtifactPersistenceManager {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(ArtifactPersistenceManager.class);

   private static final String INSERT_ARTIFACT =
         "INSERT INTO " + ARTIFACT_TABLE + " (art_id, art_type_id, guid, human_readable_id) VALUES (?, ?, ?, ?)";

   private static final String REMOVE_EMPTY_TRANSACTION_DETAILS =
         "DELETE FROM " + TRANSACTION_DETAIL_TABLE + " WHERE " + TRANSACTION_DETAIL_TABLE.column("branch_id") + " = ?" + " AND " + TRANSACTION_DETAIL_TABLE.column("transaction_id") + " NOT IN " + "(SELECT " + TRANSACTIONS_TABLE.column("transaction_id") + " FROM " + TRANSACTIONS_TABLE + ")";

   private static final LocalAliasTable ATTRIBUTE_ALIAS_1 = new LocalAliasTable(ATTRIBUTE_VERSION_TABLE, "t1");
   private static final LocalAliasTable ATTRIBUTE_ALIAS_2 = new LocalAliasTable(ATTRIBUTE_VERSION_TABLE, "t2");

   private static final String PURGE_ARTIFACT = "DELETE FROM " + ARTIFACT_TABLE + " WHERE art_id = ?";
   private static final String PURGE_ARTIFACT_GAMMAS =
         "DELETE" + " FROM " + TRANSACTIONS_TABLE + " WHERE gamma_id IN" + "(SELECT gamma_id" + " FROM " + ATTRIBUTE_VERSION_TABLE + " WHERE art_id = ? UNION " + "(SELECT gamma_id" + " FROM " + RELATION_LINK_VERSION_TABLE + " where a_art_id = ? " + " UNION SELECT gamma_id " + " FROM " + RELATION_LINK_VERSION_TABLE + " WHERE b_art_id = ?))";

   private static final String PURGE_BASELINE_ATTRIBUTE_TRANS =
         "DELETE from " + TRANSACTIONS_TABLE + " T2 WHERE EXISTS (SELECT 'x' from " + TRANSACTION_DETAIL_TABLE + " T1, " + ATTRIBUTE_VERSION_TABLE + " T3 WHERE T1.transaction_id = T2.transaction_id and T3.gamma_id = T2.gamma_id and T1.tx_type = " + TransactionDetailsType.Baselined.getId() + " and T1.branch_id = ? and T3.art_id = ?)";
   private static final String PURGE_BASELINE_RELATION_TRANS =
         "DELETE from " + TRANSACTIONS_TABLE + " T2 WHERE EXISTS (SELECT 'x' from " + TRANSACTION_DETAIL_TABLE + " T1, " + RELATION_LINK_VERSION_TABLE + " T3 WHERE T1.transaction_id = T2.transaction_id and T3.gamma_id = T2.gamma_id and T1.tx_type = " + TransactionDetailsType.Baselined.getId() + " and T1.branch_id = ? and (T3.a_art_id = ? or T3.b_art_id = ?))";
   private static final String PURGE_BASELINE_ARTIFACT_TRANS =
         "DELETE from " + TRANSACTIONS_TABLE + " T2 WHERE EXISTS (SELECT 'x' from " + TRANSACTION_DETAIL_TABLE + " T1, " + ARTIFACT_VERSION_TABLE + " T3 WHERE T1.transaction_id = T2.transaction_id and T3.gamma_id = T2.gamma_id and T1.tx_type = " + TransactionDetailsType.Baselined.getId() + " and T1.branch_id = ? and T3.art_id = ?)";

   private static final String GET_GAMMAS_REVERT =
         "SELECT txs1.gamma_id, txd1.tx_type, txs1.transaction_id  FROM osee_define_tx_details txd1, osee_define_txs  txs1, osee_define_attribute atr1 where  txd1.transaction_id = txs1.transaction_id and txs1.gamma_id = atr1.gamma_id and txd1.branch_id = ? and atr1.art_id = ? UNION ALL SELECT txs2.gamma_id, txd2.tx_type, txs2.transaction_id FROM osee_define_tx_details txd2, osee_define_txs  txs2, osee_define_rel_link rel2 where txd2.transaction_id = txs2.transaction_id and txs2.gamma_id = rel2.gamma_id and txd2.branch_id = ? and (rel2.a_art_id = ?  or  rel2.b_art_id = ?) UNION ALL SELECT txs3.gamma_id, txd3.tx_type, txs3.transaction_id   FROM osee_define_tx_details txd3, osee_define_txs txs3, osee_define_artifact_version art3 where  txd3.transaction_id = txs3.transaction_id and txs3.gamma_id = art3.gamma_id and txd3.branch_id = ? and art3.art_id = ?";

   private static final String DELETE_ATTRIBUTE_GAMMAS_REVERT =
         "DELETE FROM osee_define_attribute  atr1 WHERE atr1.gamma_id  in (SELECT txh1.gamma_id FROM osee_transaction_holder txh1 where txh1.query_id = ?)";
   private static final String DELETE_RELATION_GAMMAS_REVERT =
         "DELETE FROM osee_define_rel_link  rel1 WHERE rel1.gamma_id in (SELECT txh1.gamma_id FROM osee_transaction_holder txh1 where txh1.query_id = ?)";
   private static final String DELETE_ARTIFACT_GAMMAS_REVERT =
         "DELETE FROM osee_define_artifact_version art1 WHERE art1.gamma_id in (SELECT txh1.gamma_id FROM osee_transaction_holder txh1 where txh1.query_id = ?)";

   private static final String DELETE_TXS_GAMMAS_REVERT =
         "DELETE from osee_define_txs txs1 WHERE (txs1.transaction_id , txs1.gamma_id ) in (SELECT txh1.transaction_id , txh1.gamma_id FROM osee_transaction_holder  txh1 WHERE query_id = ? )";

   private static final String SET_TX_CURRENT_REVERT =
         "UPDATE osee_define_txs txs1 SET tx_current = 1 WHERE (txs1.transaction_id , txs1.gamma_id ) in (SELECT txh1.transaction_id , txh1.gamma_id FROM osee_transaction_holder  txh1 WHERE query_id = ? )";

   private static final String PURGE_ATTRIBUTE = "DELETE FROM " + ATTRIBUTE_VERSION_TABLE + " WHERE attr_id = ?";
   private static final String PURGE_ATTRIBUTE_GAMMAS =
         "DELETE" + " FROM " + TRANSACTIONS_TABLE + " WHERE gamma_id IN" + "(SELECT gamma_id" + " FROM " + ATTRIBUTE_VERSION_TABLE + " WHERE attr_id = ?)";

   private static final String SELECT_ATTRIBUTES_FOR_ARTIFACT =
         "SELECT " + ATTRIBUTE_ALIAS_1.columns("attr_id", "attr_type_id", "gamma_id", "value", "uri") + " FROM " + ATTRIBUTE_ALIAS_1 + "," + TRANSACTIONS_TABLE + " WHERE " + ATTRIBUTE_ALIAS_1.column("art_id") + "=?" + " AND " + ATTRIBUTE_ALIAS_1.column("modification_id") + "<> ?" + " AND " + ATTRIBUTE_ALIAS_1.column("gamma_id") + "=" + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + "(SELECT MAX(" + TRANSACTION_DETAIL_TABLE.column("transaction_id") + ")" + " FROM " + ATTRIBUTE_ALIAS_2 + "," + TRANSACTIONS_TABLE + "," + TRANSACTION_DETAIL_TABLE + " WHERE " + ATTRIBUTE_ALIAS_2.column("attr_id") + "=" + ATTRIBUTE_ALIAS_1.column("attr_id") + " AND " + ATTRIBUTE_ALIAS_2.column("gamma_id") + "=" + TRANSACTIONS_TABLE.column("gamma_id") + " AND " + TRANSACTIONS_TABLE.column("transaction_id") + "=" + TRANSACTION_DETAIL_TABLE.column("transaction_id") + " AND " + TRANSACTION_DETAIL_TABLE.column("transaction_id") + " <= ?" + " AND " + TRANSACTION_DETAIL_TABLE.column("branch_id") + "=?)";

   private static final String UPDATE_ARTIFACT_TYPE = "UPDATE osee_define_artifact SET art_type_id = ? WHERE art_id =?";

   public static final String INSERT_TRANSACTION_HOLDER =
         "INSERT INTO osee_transaction_holder (query_id, gamma_id, transaction_id) VALUES (?, ?, ?)";

   private static final String DELETE_FROM_HOLDER = "DELETE FROM osee_transaction_holder WHERE query_id = ?";

   private ExtensionDefinedObjects<IAttributeSaveListener> attributeSaveListeners;

   private IProgressMonitor monitor;
   public static final String ROOT_ARTIFACT_TYPE_NAME = "Root Artifact";
   public static final String DEFAULT_HIERARCHY_ROOT_NAME = "Default Hierarchy Root";

   private static final ArtifactPersistenceManager instance = new ArtifactPersistenceManager();

   public static boolean initFinished = false;

   private ArtifactPersistenceManager() {
      this.attributeSaveListeners =
            new ExtensionDefinedObjects<IAttributeSaveListener>(
                  "org.eclipse.osee.framework.skynet.core.AttributeSaveListener", "AttributeSaveListener", "classname");
   }

   public static ArtifactPersistenceManager getInstance() {
      return instance;
   }

   /**
    * Sets the monitor to report work to. Passing a <code>null</null> will cause the manager
    * to stop reporting work.
    * 
    * @param monitor The monitor to report work to, or <code>null</code> to stop reporting.
    */
   public void setProgressMonitor(IProgressMonitor monitor) {
      this.monitor = monitor;
   }

   protected void workedOneUnit() {
      if (monitor != null) monitor.worked(1);
   }

   protected void workingOn(String name) {
      if (monitor != null) {
         monitor.subTask(name);
      }
   }

   public static void saveTrace(Artifact artifact, SkynetTransactionBuilder builder) throws Exception {
      if (!AccessControlManager.getInstance().checkObjectPermission(artifact.getBranch(), PermissionEnum.WRITE)) throw new IllegalArgumentException(
            "No write permissions for the branch that this artifact belongs to:" + artifact.getBranch());
      if (artifact.isHistorical()) {
         throw new IllegalArgumentException(
               "The artifact " + artifact.getGuid() + " must be at the head of the branch to be edited.");
      }
      if (artifact.isDirty() && !artifact.isInTransaction()) {
         builder.addArtifact(artifact);
      }
   }

   private void notifyOnAttributeSave(Artifact artifact) {
      try {
         List<IAttributeSaveListener> listeners = attributeSaveListeners.getObjects();
         for (IAttributeSaveListener listener : listeners) {
            listener.notifyOnAttributeSave(artifact);
         }
      } catch (Exception ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
   }

   public void doSave(Artifact artifact, SkynetTransaction transaction, boolean persistAttributes) throws Exception {
      workingOn(artifact.getDescriptiveName());
      ModificationType modType;

      if (artifact.isInDb()) {
         modType = ModificationType.CHANGE;
      } else {
         modType = ModificationType.NEW;
      }

      int artGamma = SkynetDatabase.getNextGammaId();

      if (!artifact.isInDb()) {
         addArtifactData(artifact, transaction);
      }

      artifact.setGammaId(artGamma);
      processTransactionForArtifact(artifact, modType, transaction, artGamma);

      if (persistAttributes) {
         notifyOnAttributeSave(artifact);

         // Add Attributes to Transaction
         AttributeToTransactionOperation operation = new AttributeToTransactionOperation(artifact, transaction);
         operation.execute();

         if (modType != ModificationType.NEW) {
            transaction.addRemoteEvent(RemoteArtifactEventFactory.makeEvent(artifact,
                  transaction.getTransactionNumber()));
         }
         artifact.setInTransaction(true);
      }
      workedOneUnit();
   }

   private void processTransactionForArtifact(Artifact artifact, ModificationType modType, SkynetTransaction transaction, int artGamma) throws SQLException {
      transaction.addTransactionDataItem(new ArtifactTransactionData(artifact, artGamma,
            transaction.getTransactionNumber(), modType, transaction.getBranch()));
   }

   private void addArtifactData(Artifact artifact, SkynetTransaction transaction) throws SQLException {
      transaction.addToBatch(INSERT_ARTIFACT, SQL3DataType.INTEGER, artifact.getArtId(), SQL3DataType.INTEGER,
            artifact.getArtTypeId(), SQL3DataType.VARCHAR, artifact.getGuid(), SQL3DataType.VARCHAR,
            artifact.getHumanReadableId());
   }

   private static final String SELECT_ARTIFACT_START =
         "SELECT art1.*, txs1.* FROM osee_define_artifact art1, osee_define_artifact_version arv1, osee_define_txs txs1, osee_define_tx_details txd1 WHERE ";
   private static final String SELECT_ARTIFACT_END =
         " AND art1.art_id = arv1.art_id AND arv1.gamma_id = txs1.gamma_id AND txs1.transaction_id <= ? AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = ? order by txs1.transaction_id desc";
   private static final String SELECT_ARTIFACT_BY_GUID = SELECT_ARTIFACT_START + "art1.guid =?" + SELECT_ARTIFACT_END;
   private static final String SELECT_ARTIFACT_BY_ID = SELECT_ARTIFACT_START + "art1.art_id =?" + SELECT_ARTIFACT_END;

   /**
    * This method acquires <code>Artifact</code>'s directly from the database. This should only be called by
    * factories since all caching is performed by the factory.
    * 
    * @param guid The guid of the artifact.
    * @return The <code>Artifact</code> from the database that corresponds to the supplied guid.
    * @throws SQLException
    */
   public Artifact getArtifact(String guid, TransactionId transactionId) throws SQLException {
      return getArtifactInternal(transactionId, SELECT_ARTIFACT_BY_GUID, SQL3DataType.VARCHAR, guid, -1, true);
   }

   public Artifact getArtifactFromId(int artId, TransactionId transactionId) throws SQLException, IllegalArgumentException {
      return getArtifactInternal(transactionId, SELECT_ARTIFACT_BY_ID, SQL3DataType.INTEGER, null, artId, false);
   }

   private Artifact getArtifactInternal(TransactionId transactionId, String query, SQL3DataType sqlDataType, String guid, int artId, boolean useGuid) throws SQLException {
      // First try to acquire the artifact from cache
      Artifact artifact;
      Object data;
      String idString;
      if (useGuid) {
         artifact = ArtifactCache.getHistorical(guid, transactionId.getTransactionNumber());
         data = guid;
         idString = "guid \"" + guid + "\"";
      } else {
         artifact = ArtifactCache.getHistorical(artId, transactionId.getTransactionNumber());
         data = artId;
         idString = "id \"" + artId + "\"";
      }

      // If it wasn't found, then it must be acquired from the database
      if (artifact == null) {
         ConnectionHandlerStatement chStmt = null;
         try {
            chStmt =
                  ConnectionHandler.runPreparedQuery(1, query, sqlDataType, data, SQL3DataType.INTEGER,
                        transactionId.getTransactionNumber(), SQL3DataType.INTEGER,
                        transactionId.getBranch().getBranchId());

            ResultSet rSet = chStmt.getRset();
            if (!rSet.next()) {
               throw new IllegalStateException(
                     "The artifact with " + idString + " or does not exist for transaction \"" + transactionId + "\"");
            }

            ArtifactType artifactType = ArtifactTypeManager.getType(rSet.getInt("art_type_id"));
            ArtifactFactory factory = artifactType.getFactory();

            artifact =
                  factory.loadExisitingArtifact(rSet.getInt("art_id"), rSet.getInt("gamma_id"), rSet.getString("guid"),
                        rSet.getString("human_readable_id"), artifactType.getFactoryKey(), transactionId.getBranch(),
                        artifactType, rSet.getInt("transaction_id"), ModificationType.getMod(rSet.getInt("mod_type")),
                        false);

            setAttributesOnHistoricalArtifact(artifact);

            artifact.onInitializationComplete();
         } finally {
            DbUtil.close(chStmt);
         }
      }

      return artifact;
   }

   private static final String ARTIFACT_SELECT =
         "SELECT osee_define_artifact.art_id, txd1.branch_id FROM osee_define_artifact, osee_define_artifact_version arv1, osee_define_txs txs1, osee_define_tx_details txd1 WHERE " + ARTIFACT_TABLE.column("art_id") + "=arv1.art_id AND arv1.gamma_id=txs1.gamma_id AND txs1.tx_current=" + TxChange.CURRENT.getValue() + " AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id=? AND ";

   private static final String ARTIFACT_ID_SELECT =
         "SELECT " + ARTIFACT_TABLE.columns("art_id") + " FROM " + ARTIFACT_TABLE + " WHERE ";

   private static final String ARTIFACT_COUNT_SELECT = "SELECT COUNT(art_id) FROM " + ARTIFACT_TABLE + " WHERE ";

   public static CharSequence getSelectArtIdSql(ISearchPrimitive searchCriteria, List<Object> dataList, Branch branch) throws SQLException {
      return getSelectArtIdSql(searchCriteria, dataList, null, branch);
   }

   public static CharSequence getSelectArtIdSql(ISearchPrimitive searchCriteria, List<Object> dataList, String alias, Branch branch) throws SQLException {
      StringBuilder sql = new StringBuilder();

      sql.append("SELECT ");
      sql.append(searchCriteria.getArtIdColName());

      if (alias != null) {
         sql.append(" AS " + alias);
      }

      sql.append(" FROM ");
      sql.append(searchCriteria.getTableSql(dataList, branch));

      String criteriaSql = searchCriteria.getCriteriaSql(dataList, branch);
      if (criteriaSql.trim().length() != 0) {
         sql.append(" WHERE (");
         sql.append(criteriaSql);
         sql.append(")");
      }

      return sql;
   }

   public int getArtifactCount(ISearchPrimitive searchCriteria, Branch branch) throws SQLException {
      List<Object> dataList = new LinkedList<Object>();
      return getArtifactCount(getSql(searchCriteria, ARTIFACT_COUNT_SELECT, dataList, branch), dataList);
   }

   public int getArtifactCount(List<ISearchPrimitive> searchCriteria, boolean all, Branch branch) throws SQLException {
      List<Object> dataList = new LinkedList<Object>();
      return getArtifactCount(getSql(searchCriteria, all, ARTIFACT_COUNT_SELECT, dataList, branch), dataList);
   }

   private int getArtifactCount(String sql, List<Object> dataList) throws SQLException {
      int toReturn = -1;
      ConnectionHandlerStatement chStmt = null;
      try {
         chStmt = ConnectionHandler.runPreparedQuery(sql, dataList.toArray());
         if (chStmt.next()) {
            toReturn = chStmt.getRset().getInt(1);
         }
      } finally {
         DbUtil.close(chStmt);
      }
      return toReturn;
   }

   public static String getIdSql(List<ISearchPrimitive> searchCriteria, boolean all, List<Object> dataList, Branch branch) throws SQLException {
      return getSql(searchCriteria, all, ARTIFACT_ID_SELECT, dataList, branch);
   }

   private static String getSql(ISearchPrimitive searchCriteria, String header, List<Object> dataList, Branch branch) throws SQLException {
      StringBuilder sql = new StringBuilder(header);

      sql.append(ARTIFACT_TABLE.column("art_id") + " in (");
      sql.append(getSelectArtIdSql(searchCriteria, dataList, branch));
      sql.append(")");

      return sql.toString();
   }

   private static String getSql(List<ISearchPrimitive> searchCriteria, boolean all, String header, List<Object> dataList, Branch branch) throws SQLException {
      StringBuilder sql = new StringBuilder(header);

      if (all) {
         ISearchPrimitive primitive = null;
         Iterator<ISearchPrimitive> iter = searchCriteria.iterator();

         while (iter.hasNext()) {
            primitive = iter.next();
            sql.append(ARTIFACT_TABLE.column("art_id") + " in (");
            sql.append(getSelectArtIdSql(primitive, dataList, branch));

            if (iter.hasNext()) {
               sql.append(") AND ");
            }
         }
         sql.append(")");
      } else {
         ISearchPrimitive primitive = null;
         Iterator<ISearchPrimitive> iter = searchCriteria.iterator();

         sql.append(ARTIFACT_TABLE.column("art_id") + " IN(SELECT art_id FROM " + ARTIFACT_TABLE + ", (");

         while (iter.hasNext()) {
            primitive = iter.next();
            sql.append(getSelectArtIdSql(primitive, dataList, "desired_art_id", branch));
            if (iter.hasNext()) sql.append(" UNION ALL ");
         }
         sql.append(") ORD_ARTS");
         sql.append(" WHERE art_id = ORD_ARTS.desired_art_id");

         sql.append(")");
      }

      return sql.toString();
   }

   @Deprecated
   public Collection<Artifact> getArtifacts(ISearchPrimitive searchCriteria, Branch branch) throws SQLException {
      LinkedList<Object> queryParameters = new LinkedList<Object>();
      queryParameters.add(SQL3DataType.INTEGER);
      queryParameters.add(branch.getBranchId());
      return ArtifactLoader.getArtifacts(getSql(searchCriteria, ARTIFACT_SELECT, queryParameters, branch),
            queryParameters.toArray(), 100, ArtifactLoad.FULL, false, null);
   }

   @Deprecated
   public Collection<Artifact> getArtifacts(List<ISearchPrimitive> searchCriteria, boolean all, Branch branch, ISearchConfirmer confirmer) throws SQLException {
      LinkedList<Object> queryParameters = new LinkedList<Object>();
      queryParameters.add(SQL3DataType.INTEGER);
      queryParameters.add(branch.getBranchId());
      return ArtifactLoader.getArtifacts(getSql(searchCriteria, all, ARTIFACT_SELECT, queryParameters, branch),
            queryParameters.toArray(), 100, ArtifactLoad.FULL, false, confirmer);
   }

   @Deprecated
   public Collection<Artifact> getArtifacts(List<ISearchPrimitive> searchCriteria, boolean all, Branch branch) throws SQLException {
      return getArtifacts(searchCriteria, all, branch, null);
   }

   /**
    * Acquires the user defined attributes for an artifact. If none are in the table, then it returns the 'default set'
    * of attributes for the artifact.
    * 
    * @param artifact The artifact to acquire the attributes for.
    * @param branch The tag to get the data for.
    * @throws SQLException
    * @throws OseeCoreException
    */
   public static void setAttributesOnHistoricalArtifact(Artifact artifact) throws SQLException {
      ConnectionHandlerStatement chStmt = null;
      try {
         // Acquire previously stored attributes
         chStmt =
               ConnectionHandler.runPreparedQuery(SELECT_ATTRIBUTES_FOR_ARTIFACT, SQL3DataType.INTEGER,
                     artifact.getArtId(), SQL3DataType.INTEGER, ModificationType.DELETED.getValue(),
                     SQL3DataType.INTEGER, artifact.getTransactionNumber(), SQL3DataType.INTEGER,
                     artifact.getBranch().getBranchId());

         ResultSet rSet = chStmt.getRset();
         while (rSet.next()) {
            AttributeToTransactionOperation.initializeAttribute(artifact, rSet.getInt("attr_type_id"),
                  rSet.getString("value"), rSet.getString("uri"), rSet.getInt("attr_id"), rSet.getInt("gamma_id"));
         }
      } finally {
         DbUtil.close(chStmt);
      }

      try {
         AttributeToTransactionOperation.meetMinimumAttributeCounts(artifact);
      } catch (OseeDataStoreException ex) {
         throw new SQLException(ex);
      }
   }

   /**
    * This method returns 1000 of the ids, removing them from the collection
    * 
    * @param artifacts
    */
   public String getArtIdList(final ArrayList<Artifact> artifacts) {
      StringBuilder artIdList = new StringBuilder();
      int count = -1;
      while (!artifacts.isEmpty() && count++ < 999) {
         if (count > 0) artIdList.append(",");
         artIdList.append(artifacts.remove(0).getArtId());
      }

      return artIdList.toString();
   }

   /**
    * @param artifacts The artifacts to delete.
    * @throws SQLException
    */
   public static void deleteArtifact(final Artifact... artifacts) throws Exception {
      if (artifacts.length == 0) return;

      // Confirm artifacts are fit to delete
      for (IArtifactCheck check : ArtifactChecks.getArtifactChecks()) {
         Result result = check.isDeleteable(Arrays.asList(artifacts));
         if (result.isFalse()) throw new IllegalStateException(result.getText());
      }

      final Branch branch = artifacts[0].getBranch();
      if (SkynetTransactionManager.getInstance().isInBatch(branch)) {
         for (Artifact artifact : artifacts) {
            deleteTrace(artifact, SkynetTransactionManager.getInstance().getTransactionBuilder(branch));
         }
      } else {
         AbstractSkynetTxTemplate deleteTx = new AbstractSkynetTxTemplate(branch) {
            @Override
            protected void handleTxWork() throws Exception {
               for (Artifact artifact : artifacts) {
                  if (!artifact.isDeleted()) {
                     deleteTrace(artifact, getTxBuilder());
                  }
               }
            }
         };
         deleteTx.execute();
      }
   }

   /**
    * @param artifact
    * @param builder
    * @throws Exception
    */
   public static void deleteTrace(Artifact artifact, SkynetTransactionBuilder builder) throws Exception {
      if (!artifact.isDeleted()) {
         // This must be done first since the the actual deletion of an artifact clears out the link manager
         for (Artifact childArtifact : artifact.getChildren()) {
            deleteTrace(childArtifact, builder);
         }

         builder.deleteArtifact(artifact);

         artifact.setDeleted();
      }
   }

   /**
    * @param artifact
    * @param transaction
    * @throws Exception
    */
   public synchronized void doDelete(Artifact artifact, SkynetTransaction transaction, SkynetTransactionBuilder builder) throws Exception {
      if (!artifact.isInDb()) return;

      processTransactionForArtifact(artifact, ModificationType.DELETED, transaction, SkynetDatabase.getNextGammaId());

      transaction.addRemoteEvent(new NetworkArtifactDeletedEvent(artifact.getBranch().getBranchId(),
            transaction.getTransactionNumber(), artifact.getArtId(), artifact.getArtTypeId(),
            artifact.getFactory().getClass().getCanonicalName(), SkynetAuthentication.getUser().getArtId()));
      transaction.addLocalEvent(new TransactionArtifactModifiedEvent(artifact, ModType.Deleted, this));

      RelationManager.deleteRelationsAll(artifact);
      artifact.deleteAttributes();
      artifact.persistRelations();
      TagManager.getInstance().clearTags(artifact, SystemTagDescriptor.AUTO_INDEXED.getDescriptor());
   }

   public void purgeArtifactFromBranch(Artifact artifact) throws Exception {
      if (artifact == null) throw new IllegalArgumentException("Artifact = null in purgeArtifactFromBranch");
      purgeArtifactFromBranch(artifact.getBranch().getBranchId(), artifact.getArtId());
   }

   /**
    * Removes an artifact, it's attributes and any relations that have become invalid from the removal of this artifact
    * from the database. It also removes all history associated with this artifact (i.e. all transactions and gamma ids
    * will also be removed from the database only for the branch it is on).
    * 
    * @param artifact
    * @throws SQLException
    */
   public void purgeArtifactFromBranch(int branchId, int artId) throws Exception {
      revertArtifact(branchId, artId);

      //Remove from Baseline
      ConnectionHandler.runPreparedUpdate(PURGE_BASELINE_ATTRIBUTE_TRANS, SQL3DataType.INTEGER, branchId,
            SQL3DataType.INTEGER, artId);
      ConnectionHandler.runPreparedUpdate(PURGE_BASELINE_RELATION_TRANS, SQL3DataType.INTEGER, branchId,
            SQL3DataType.INTEGER, artId, SQL3DataType.INTEGER, artId);
      ConnectionHandler.runPreparedUpdate(PURGE_BASELINE_ARTIFACT_TRANS, SQL3DataType.INTEGER, branchId,
            SQL3DataType.INTEGER, artId);
   }

   /**
    * this method does not update the in memory model or send events. It also does not purge any child artifacts. The
    * more full featured version of this method takes an artifact as an argument rather than and artifact id.
    * 
    * @param artifactId
    * @throws SQLException
    */
   public static void purgeArtifact(int artifactId) throws SQLException {
      ConnectionHandler.runPreparedUpdate(PURGE_ARTIFACT_GAMMAS, SQL3DataType.INTEGER, artifactId,
            SQL3DataType.INTEGER, artifactId, SQL3DataType.INTEGER, artifactId);
      ConnectionHandler.runPreparedUpdate(PURGE_ARTIFACT, SQL3DataType.INTEGER, artifactId);
      // System.out.println("Purge empty transactions");
      // ConnectionHandler.runPreparedUpdate(PURGE_EMPTY_TRANSACTIONS);
   }

   /**
    * Removes an artifact, it's attributes and any relations that have become invalid from the removal of this artifact
    * from the database. It also removes all history associated with this artifact (i.e. all transactions and gamma ids
    * will also be removed from the database).
    * 
    * @param artifact
    * @throws SQLException
    */
   public static void purgeArtifact(final Artifact artifact) throws SQLException {
      purgeArtifact(artifact.getArtId());

      System.out.println("number of children:" + artifact.getChildren().size());
      for (Artifact child : artifact.getChildren()) {
         purgeArtifact(child);
      }

      RelationManager.purgeRelationsFor(artifact);
      SkynetEventManager.getInstance().kick(new TransactionArtifactModifiedEvent(artifact, ModType.Purged, instance));
   }

   public void revertArtifact(Artifact artifact) throws Exception {
      if (artifact == null) return;
      revertArtifact(artifact.getBranch().getBranchId(), artifact.getArtId());
   }

   public void revertArtifact(int branchId, int artId) throws Exception {
      new RevertDbTx(branchId, artId).execute();
   }

   private final class RevertDbTx extends AbstractDbTxTemplate {
      int branchId;
      int artId;

      public RevertDbTx(int branchId, int artId) {
         this.branchId = branchId;
         this.artId = artId;
      }

      @Override
      protected void handleTxWork() throws Exception {
         class GammaTransaction {
            int gammaId;
            int transactionId;

            public GammaTransaction(int gammaId, int transactionId) {
               this.gammaId = gammaId;
               this.transactionId = transactionId;
            }
         }
         ;
         Collection<GammaTransaction> gammaIdsModifications = new LinkedList<GammaTransaction>();
         Collection<GammaTransaction> gammaIdsBaseline = new LinkedList<GammaTransaction>();

         //Get attribute Gammas
         ConnectionHandlerStatement connectionHandlerStatement = null;
         ResultSet resultSet = null;
         try {
            connectionHandlerStatement =
                  ConnectionHandler.runPreparedQuery(GET_GAMMAS_REVERT, SQL3DataType.INTEGER, branchId,
                        SQL3DataType.INTEGER, artId, SQL3DataType.INTEGER, branchId, SQL3DataType.INTEGER, artId,
                        SQL3DataType.INTEGER, artId, SQL3DataType.INTEGER, branchId, SQL3DataType.INTEGER, artId);
            resultSet = connectionHandlerStatement.getRset();
            while (resultSet.next()) {
               if (resultSet.getInt("tx_type") == TransactionDetailsType.NonBaselined.getId()) {
                  gammaIdsModifications.add(new GammaTransaction(resultSet.getInt("gamma_id"),
                        resultSet.getInt("transaction_id")));
               } else {
                  gammaIdsBaseline.add(new GammaTransaction(resultSet.getInt("gamma_id"),
                        resultSet.getInt("transaction_id")));
               }
            }
         } finally {
            DbUtil.close(connectionHandlerStatement);
            connectionHandlerStatement = null;
            resultSet = null;
         }

         if (!gammaIdsModifications.isEmpty()) {
            List<Object[]> datas = new LinkedList<Object[]>();
            int queryId = ArtifactLoader.getNewQueryId();
            for (GammaTransaction gammaTransaction : gammaIdsModifications) {
               datas.add(new Object[] {SQL3DataType.INTEGER, queryId, SQL3DataType.INTEGER, gammaTransaction.gammaId,
                     SQL3DataType.INTEGER, gammaTransaction.transactionId});
            }
            selectTempTransactions(datas);

            try {
               ConnectionHandler.runPreparedUpdate(DELETE_ATTRIBUTE_GAMMAS_REVERT, SQL3DataType.INTEGER, queryId);
               ConnectionHandler.runPreparedUpdate(DELETE_RELATION_GAMMAS_REVERT, SQL3DataType.INTEGER, queryId);
               ConnectionHandler.runPreparedUpdate(DELETE_ARTIFACT_GAMMAS_REVERT, SQL3DataType.INTEGER, queryId);

               ConnectionHandler.runPreparedUpdate(DELETE_TXS_GAMMAS_REVERT, SQL3DataType.INTEGER, queryId);
               clearTempTransactions(queryId);

               if (!gammaIdsBaseline.isEmpty()) {
                  datas = new LinkedList<Object[]>();
                  queryId = ArtifactLoader.getNewQueryId();
                  for (GammaTransaction gammaTransaction : gammaIdsBaseline) {
                     datas.add(new Object[] {SQL3DataType.INTEGER, queryId, SQL3DataType.INTEGER,
                           gammaTransaction.gammaId, SQL3DataType.INTEGER, gammaTransaction.transactionId});
                  }
                  selectTempTransactions(datas);
                  ConnectionHandler.runPreparedUpdate(SET_TX_CURRENT_REVERT, SQL3DataType.INTEGER, queryId);
               }
            } finally {
               clearTempTransactions(queryId);
            }
         }

         ConnectionHandler.runPreparedUpdate(REMOVE_EMPTY_TRANSACTION_DETAILS, SQL3DataType.INTEGER, branchId);

      }

      @Override
      protected void handleTxFinally() throws Exception {
         super.handleTxFinally();
      }

   }

   public static void selectTempTransactions(Collection<Object[]> insertParameters) throws SQLException {
      ConnectionHandler.runPreparedUpdateBatch(INSERT_TRANSACTION_HOLDER, insertParameters);
   }

   public static void clearTempTransactions(int queryId) throws SQLException {
      ConnectionHandler.runPreparedUpdateReturnCount(DELETE_FROM_HOLDER, SQL3DataType.INTEGER, queryId);
   }

   public static Artifact getDefaultHierarchyRootArtifact(Branch branch, boolean createIfNecessary) throws SQLException, MultipleArtifactsExist, ArtifactDoesNotExist {
      try {
         Artifact root = ArtifactCache.getByTextId(DEFAULT_HIERARCHY_ROOT_NAME, branch);
         if (root == null) {
            root =
                  ArtifactQuery.getArtifactFromTypeAndName(ROOT_ARTIFACT_TYPE_NAME, DEFAULT_HIERARCHY_ROOT_NAME, branch);
            ArtifactCache.putByTextId(DEFAULT_HIERARCHY_ROOT_NAME, root);
         }
         return root;
      } catch (ArtifactDoesNotExist ex) {
         if (createIfNecessary) {
            logger.log(Level.INFO, "Created " + DEFAULT_HIERARCHY_ROOT_NAME + " becuase no root was found.");
            Artifact root =
                  ArtifactTypeManager.addArtifact(ROOT_ARTIFACT_TYPE_NAME, branch, DEFAULT_HIERARCHY_ROOT_NAME);
            root.persistAttributes();
            ArtifactCache.putByTextId(DEFAULT_HIERARCHY_ROOT_NAME, root);
            return root;
         }
         throw ex;
      }
   }

   public static Artifact getDefaultHierarchyRootArtifact(Branch branch) throws SQLException, MultipleArtifactsExist, ArtifactDoesNotExist {
      return getDefaultHierarchyRootArtifact(branch, false);
   }

   public void bulkLoadArtifacts(Collection<? extends Artifact> arts, Branch branch) throws SQLException, IllegalArgumentException {
      if (arts.size() == 0) return;

      List<ISearchPrimitive> bulkLoad = new LinkedList<ISearchPrimitive>();
      for (Artifact art : arts) {
         bulkLoad.add(new RelatedToSearch(art.getArtId(), true));
         bulkLoad.add(new RelatedToSearch(art.getArtId(), false));
      }
      getArtifacts(bulkLoad, false, branch);
   }

   public Collection<Artifact> getArtifactsFromAttribute(String attributeName, String attributeValue, Branch branch) throws SQLException {
      return ArtifactQuery.getArtifactsFromCriteria(branch, new AttributeValueCriteria(attributeName, attributeValue));
   }

   /**
    * Changes the artifact type
    * 
    * @param artifact
    * @param artifactType
    * @throws SQLException
    */
   public static void changeArtifactSubStype(Artifact artifact, ArtifactType artifactType) throws SQLException {
      ConnectionHandler.runPreparedUpdate(UPDATE_ARTIFACT_TYPE, SQL3DataType.INTEGER, artifactType.getArtTypeId(),
            SQL3DataType.INTEGER, artifact.getArtId());
   }

   /**
    * Purge attribute from the database.
    * 
    * @param attribute
    */
   public static void purgeAttribute(Attribute<?> attribute, int attributeId) throws SQLException {
      ConnectionHandler.runPreparedUpdate(PURGE_ATTRIBUTE_GAMMAS, SQL3DataType.INTEGER, attributeId);
      ConnectionHandler.runPreparedUpdate(PURGE_ATTRIBUTE, SQL3DataType.INTEGER, attributeId);
   }
}