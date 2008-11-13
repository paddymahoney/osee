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
package org.eclipse.osee.framework.skynet.core.revision;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.exception.BranchMergeException;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.db.connection.exception.TransactionDoesNotExist;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoad;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.conflict.ArtifactConflictBuilder;
import org.eclipse.osee.framework.skynet.core.conflict.AttributeConflict;
import org.eclipse.osee.framework.skynet.core.conflict.AttributeConflictBuilder;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictBuilder;
import org.eclipse.osee.framework.skynet.core.status.EmptyMonitor;
import org.eclipse.osee.framework.skynet.core.status.IStatusMonitor;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;

/**
 * @author Theron Virgin
 * @author Jeff C. Phillips
 */
public class ConflictManagerInternal {
   private static final String REVISED_ARTIFACT_CONFLICTS =
         "SELECT art1.art_type_id, arv1.art_id, txs1.mod_type AS source_mod_type, txs1.gamma_id AS source_gamma, txs2.mod_type AS dest_mod_type, txs2.gamma_id AS dest_gamma FROM osee_txs txs1, osee_txs txs2, osee_tx_details txd1, osee_tx_details txd2, osee_artifact_version arv1, osee_artifact_version arv2, osee_artifact art1 WHERE txd1.tx_type = 0 AND txd1.branch_id = ? AND txd1.transaction_id = txs1.transaction_id AND txs1.tx_current in (1,2) AND txs1.gamma_id = arv1.gamma_id and arv1.art_id = art1.art_id AND arv1.art_id = arv2.art_id AND arv2.gamma_id = txs2.gamma_id AND txs2.transaction_id = txd2.transaction_id AND txd2.branch_id = ? AND ((txs2.tx_current = 1 AND txs2.gamma_id not in (SELECT txs.gamma_id FROM osee_txs txs WHERE txs.transaction_id = ?)) OR txs2.tx_current = 2)";

   private static final String REVISED_ATTRIBUTE_CONFLICTS =
         "SELECT atr1.art_id, txs1.mod_type, atr1.attr_type_id, atr1.attr_id, atr1.gamma_id AS source_gamma, atr1.value AS source_value, atr2.gamma_id AS dest_gamma, atr2.value as dest_value, txs2.mod_type AS dest_mod_type FROM osee_txs txs1, osee_txs txs2, osee_tx_details txd1, osee_tx_details txd2, osee_attribute atr1, osee_attribute atr2 WHERE txd1.tx_type = 0 AND txd1.branch_id = ? AND txd1.transaction_id = txs1.transaction_id AND txs1.tx_current in (1,2) AND txs1.gamma_id = atr1.gamma_id AND atr1.attr_id = atr2.attr_id AND atr2.gamma_id = txs2.gamma_id AND txs2.transaction_id = txd2.transaction_id AND txd2.branch_id = ? AND ((txs2.tx_current = 1 AND txs2.gamma_id not in (SELECT txs.gamma_id FROM osee_txs txs WHERE txs.transaction_id = ? )) OR txs2.tx_current = 2)";

   private static final String HISTORICAL_ATTRIBUTE_CONFLICTS =
         "SELECT atr.attr_id, atr.art_id, source_gamma_id, dest_gamma_id, attr_type_id, mer.merge_branch_id, mer.dest_branch_id, value as source_value, status FROM osee_conflict con, osee_merge mer, osee_attribute atr Where mer.commit_transaction_id = ? AND mer.merge_branch_id = con.merge_branch_id And con.source_gamma_id = atr.gamma_id AND con.status in (" + Conflict.Status.COMMITTED.getValue() + ", " + Conflict.Status.INFORMATIONAL.getValue() + " ) order by attr_id";

   private static final String CONFLICT_CLEANUP =
         "DELETE FROM osee_conflict WHERE merge_branch_id = ? AND conflict_id NOT IN ";

   private static final String GET_DESTINATION_BRANCHES =
         "SELECT dest_branch_id FROM osee_merge WHERE source_branch_id = ?";

   private static final String GET_MERGE_DATA =
         "SELECT commit_transaction_id, merge_branch_id FROM osee_merge WHERE source_branch_id = ? AND dest_branch_id = ?";

   private static final String GET_COMMIT_TRANSACTION_COMMENT =
         "SELECT transaction_id FROM osee_tx_details WHERE osee_comment = ? AND branch_id = ?";

   private static ConflictManagerInternal instance = new ConflictManagerInternal();

   private static final boolean DEBUG =
         "TRUE".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.osee.framework.skynet.core/debug/Merge"));

   private ConflictManagerInternal() {
      super();
   }

   public static ConflictManagerInternal getInstance() {
      return instance;
   }

   public List<Conflict> getConflictsPerBranch(TransactionId commitTransaction, IStatusMonitor monitor) throws OseeCoreException {
      long time = System.currentTimeMillis();
      long totalTime = time;
      if (monitor == null) monitor = new EmptyMonitor();
      monitor.startJob(String.format("Loading Merge Manager for Transaction %d",
            commitTransaction.getTransactionNumber()), 100);
      monitor.setSubtaskName("Finding Database stored conflicts");
      if (DEBUG) {
         System.out.println(String.format("\nDiscovering Conflicts based on Transaction ID: %d",
               commitTransaction.getTransactionNumber()));
         totalTime = System.currentTimeMillis();
      }
      ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      if (DEBUG) {
         System.out.println("Running Query to find conflicts stored in the DataBase");
         time = System.currentTimeMillis();
      }
      try {
         chStmt.runPreparedQuery(HISTORICAL_ATTRIBUTE_CONFLICTS, commitTransaction.getTransactionNumber());
         if (DEBUG) {
            System.out.println(String.format("          Query finished in %s", Lib.getElapseString(time)));
         }
         while (chStmt.next()) {
            AttributeConflict attributeConflict =
                  new AttributeConflict(chStmt.getInt("source_gamma_id"), chStmt.getInt("dest_gamma_id"),
                        chStmt.getInt("art_id"), commitTransaction, chStmt.getString("source_value"),
                        chStmt.getInt("attr_id"), chStmt.getInt("attr_type_id"),
                        BranchManager.getBranch(chStmt.getInt("merge_branch_id")),
                        BranchManager.getBranch(chStmt.getInt("dest_branch_id")));
            conflicts.add(attributeConflict);

            attributeConflict.setStatus(Conflict.Status.getStatus(chStmt.getInt("status")));
         }
         monitor.done();
      } finally {
         chStmt.close();
      }
      if (DEBUG) {
         debugDump(conflicts, totalTime);
      }
      return conflicts;
   }

   public List<Conflict> getConflictsPerBranch(Branch sourceBranch, Branch destinationBranch, TransactionId baselineTransaction, IStatusMonitor monitor) throws OseeCoreException {
      ArrayList<ConflictBuilder> conflictBuilders = new ArrayList<ConflictBuilder>();
      ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
      Set<Integer> artIdSet = new HashSet<Integer>();
      Set<Integer> artIdSetDontShow = new HashSet<Integer>();
      Set<Integer> artIdSetDontAdd = new HashSet<Integer>();

      //Check to see if the branch has already been committed than use the transaction version
      if (monitor == null) monitor = new EmptyMonitor();
      int commitTransactionId = getCommitTransaction(sourceBranch, destinationBranch);
      if (commitTransactionId != 0) {
         try {
            return getConflictsPerBranch(TransactionIdManager.getTransactionId(commitTransactionId), monitor);
         } catch (TransactionDoesNotExist ex) {
         }
      }

      monitor.startJob(String.format("Loading Merge Manager for Branch %d into Branch %d", sourceBranch.getBranchId(),
            destinationBranch.getBranchId()), 100);
      monitor.setSubtaskName("Finding Database stored conflicts");
      long totalTime = 0;
      if (DEBUG) {
         System.out.println(String.format("\nDiscovering Conflicts based on Source Branch: %d Destination Branch: %d",
               sourceBranch.getBranchId(), destinationBranch.getBranchId()));
         totalTime = System.currentTimeMillis();
      }

      if ((sourceBranch == null) || (destinationBranch == null)) {
         throw new IllegalArgumentException(String.format("Source Branch = %s Destination Branch = %s",
               sourceBranch == null ? "NULL" : sourceBranch.getBranchId(),
               destinationBranch == null ? "NULL" : destinationBranch.getBranchId()));
      }
      int transactionId = findCommonTransaction(sourceBranch, destinationBranch);
      loadArtifactVersionConflictsNew(sourceBranch, destinationBranch, baselineTransaction, conflictBuilders, artIdSet,
            artIdSetDontShow, artIdSetDontAdd, monitor, transactionId);
      loadAttributeConflictsNew(sourceBranch, destinationBranch, baselineTransaction, conflictBuilders, artIdSet,
            monitor, transactionId);
      for (Integer integer : artIdSetDontAdd) {
         artIdSet.remove(integer);
      }
      if (artIdSet.isEmpty()) return conflicts;

      monitor.setSubtaskName("Creating and/or maintaining the Merge Branch");
      Branch mergeBranch =
            BranchManager.getOrCreateMergeBranch(sourceBranch, destinationBranch, new ArrayList<Integer>(artIdSet));

      if (mergeBranch == null) {
         throw new BranchMergeException("Could not create the Merge Branch.");
      }
      monitor.updateWork(15);

      preloadConflictArtifacts(sourceBranch, destinationBranch, mergeBranch, artIdSet, monitor);

      //Don't create the conflicts for attributes on an artifact that is deleted etc.
      for (ConflictBuilder conflictBuilder : conflictBuilders) {
         Conflict conflict = conflictBuilder.getConflict(mergeBranch, artIdSetDontShow);
         if (conflict != null) {
            conflicts.add(conflict);
            conflict.computeStatus();
         }
      }
      if (DEBUG) {
         debugDump(conflicts, totalTime);
      }
      cleanUpConflictDB(conflicts, mergeBranch.getBranchId(), monitor);
      return conflicts;
   }

   private void preloadConflictArtifacts(Branch sourceBranch, Branch destinationBranch, Branch mergeBranch, Collection<Integer> artIdSet, IStatusMonitor monitor) throws OseeCoreException {
      long time = 0;

      monitor.setSubtaskName("Preloading Artifacts Associated with the Conflicts");
      if (DEBUG) {
         System.out.println("Prelodaing Conflict Artifacts");
         time = System.currentTimeMillis();
      }
      if (artIdSet != null && !artIdSet.isEmpty()) {
         int queryId = ArtifactLoader.getNewQueryId();
         Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();

         List<Object[]> insertParameters = new LinkedList<Object[]>();
         for (int artId : artIdSet) {
            insertParameters.add(new Object[] {queryId, insertTime, artId, sourceBranch.getBranchId(),
                  SQL3DataType.INTEGER});
            insertParameters.add(new Object[] {queryId, insertTime, artId, destinationBranch.getBranchId(),
                  SQL3DataType.INTEGER});
            insertParameters.add(new Object[] {queryId, insertTime, artId, mergeBranch.getBranchId(),
                  SQL3DataType.INTEGER});
         }
         ArtifactLoader.loadArtifacts(queryId, ArtifactLoad.FULL, null, insertParameters, false, false, true);
      }
      if (DEBUG) {
         System.out.println(String.format("    Preloading took %s", Lib.getElapseString(time)));
      }
      monitor.updateWork(25);
   }

   /**
    * @param sourceBranch
    * @param destinationBranch
    * @param baselineTransaction
    * @param conflictBuilders
    * @param artIdSet
    */

   private void loadArtifactVersionConflictsNew(Branch sourceBranch, Branch destinationBranch, TransactionId baselineTransaction, ArrayList<ConflictBuilder> conflictBuilders, Set<Integer> artIdSet, Set<Integer> artIdSetDontShow, Set<Integer> artIdSetDontAdd, IStatusMonitor monitor, int transactionId) throws OseeDataStoreException {
      long time = 0;
      if (DEBUG) {
         System.out.println("Finding Artifact Version Conflicts");
         System.out.println("    Running the Artifact Conflict Query");
         time = System.currentTimeMillis();
      }
      monitor.setSubtaskName("Finding Artifact Version Conflicts");
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();

      try {
         chStmt.runPreparedQuery(REVISED_ARTIFACT_CONFLICTS, sourceBranch.getBranchId(),
               destinationBranch.getBranchId(), transactionId);

         if (DEBUG) {
            System.out.println(String.format("         Query completed in %s ", Lib.getElapseString(time)));
         }

         if (!chStmt.next()) {
            return;
         }
         ArtifactConflictBuilder artifactConflictBuilder;
         int artId = 0;

         do {
            int nextArtId = chStmt.getInt("art_id");
            int sourceGamma = chStmt.getInt("source_gamma");
            int destGamma = chStmt.getInt("dest_gamma");
            int sourceModType = chStmt.getInt("source_mod_type");
            int destModType = chStmt.getInt("dest_mod_type");
            int artTypeId = chStmt.getInt("art_type_id");

            if (artId != nextArtId) {
               artId = nextArtId;

               if ((destModType == ModificationType.DELETED.getValue() && sourceModType == ModificationType.CHANGE.getValue()) || (destModType == ModificationType.CHANGE.getValue() && sourceModType == ModificationType.DELETED.getValue())) {

                  artifactConflictBuilder =
                        new ArtifactConflictBuilder(sourceGamma, destGamma, artId, baselineTransaction, sourceBranch,
                              destinationBranch, sourceModType, destModType, artTypeId);

                  conflictBuilders.add(artifactConflictBuilder);
                  artIdSet.add(artId);
               } else if (destModType == ModificationType.DELETED.getValue() && sourceModType == ModificationType.DELETED.getValue()) {
                  artIdSetDontShow.add(artId);
                  artIdSetDontAdd.add(artId);
               }
            }
         } while (chStmt.next());
      } finally {
         chStmt.close();
      }
      monitor.updateWork(20);
      for (Integer integer : artIdSet) {
         artIdSetDontShow.add(integer);
      }
   }

   /**
    * @param sourceBranch
    * @param destinationBranch
    * @param baselineTransaction
    * @param conflicts
    */
   private void loadAttributeConflictsNew(Branch sourceBranch, Branch destinationBranch, TransactionId baselineTransaction, ArrayList<ConflictBuilder> conflictBuilders, Set<Integer> artIdSet, IStatusMonitor monitor, int transactionId) throws OseeDataStoreException {
      long time = 0;
      if (DEBUG) {
         System.out.println("Finding Attribute Version Conflicts");
         System.out.println("    Running the Attribute Conflict Query");
         time = System.currentTimeMillis();
      }
      monitor.setSubtaskName("Finding the Attribute Conflicts");
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      AttributeConflictBuilder attributeConflictBuilder;
      try {
         chStmt.runPreparedQuery(REVISED_ATTRIBUTE_CONFLICTS, sourceBranch.getBranchId(),
               destinationBranch.getBranchId(), transactionId);

         if (DEBUG) {
            System.out.println(String.format("         Query completed in %s ", Lib.getElapseString(time)));
         }
         int attrId = 0;

         if (chStmt.next()) {

            do {
               int nextAttrId = chStmt.getInt("attr_id");
               int artId = chStmt.getInt("art_id");
               int sourceGamma = chStmt.getInt("source_gamma");
               int destGamma = chStmt.getInt("dest_gamma");
               int attrTypeId = chStmt.getInt("attr_type_id");
               String sourceValue =
                     chStmt.getString("source_value") != null ? chStmt.getString("source_value") : chStmt.getString("dest_value");

               if (attrId != nextAttrId) {
                  attrId = nextAttrId;
                  attributeConflictBuilder =
                        new AttributeConflictBuilder(sourceGamma, destGamma, artId, baselineTransaction, sourceBranch,
                              destinationBranch, sourceValue, attrId, attrTypeId);

                  conflictBuilders.add(attributeConflictBuilder);
                  artIdSet.add(artId);
               }
            } while (chStmt.next());
         }
      } finally {
         chStmt.close();
      }
      monitor.updateWork(30);
   }

   private void debugDump(Collection<Conflict> conflicts, long time) throws OseeCoreException {
      int displayCount = 1;
      System.out.println(String.format("Found %d conflicts in %s", conflicts.size(), Lib.getElapseString(time)));
      for (Conflict conflict : conflicts) {
         System.out.println(String.format(
               "    %d. ArtId = %d, ChangeItem = %s, SourceGamma = %d, DestGamma = %d, Status = %s", displayCount++,
               conflict.getArtId(), conflict.getChangeItem(), conflict.getSourceGamma(), conflict.getDestGamma(),
               conflict.getStatus()));
      }
   }

   private void cleanUpConflictDB(Collection<Conflict> conflicts, int branchId, IStatusMonitor monitor) throws OseeCoreException {
      int count = 0;
      long time = System.currentTimeMillis();
      monitor.setSubtaskName("Cleaning up old conflict data");
      if (conflicts != null && conflicts.size() != 0 && branchId != 0) {
         count = ConnectionHandler.runPreparedUpdate(CONFLICT_CLEANUP + createData(conflicts), branchId);
      }
      if (DEBUG) {
         System.out.println(String.format("       Cleaned up %d conflicts that are no longer conflicting in %s ",
               count, Lib.getElapseString(time)));
      }
      monitor.updateWork(10);
   }

   private String createData(Collection<Conflict> conflicts) throws OseeCoreException {
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      boolean first = true;
      for (Conflict conflict : conflicts) {
         if (!first) {
            builder.append(" , ");
         } else {
            first = false;
         }
         builder.append(conflict.getObjectId());
      }
      builder.append(")");
      return builder.toString();
   }

   public Collection<Integer> getDestinationBranchesMerged(int sourceBranchId) throws OseeCoreException {
      List<Integer> destinationBranches = new LinkedList<Integer>();
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      try {
         chStmt.runPreparedQuery(GET_DESTINATION_BRANCHES, sourceBranchId);
         while (chStmt.next()) {
            destinationBranches.add(chStmt.getInt("dest_branch_id"));
         }
      } finally {
         chStmt.close();
      }
      Collections.sort(destinationBranches);
      return destinationBranches;
   }

   public int getCommitTransaction(Branch sourceBranch, Branch destBranch) throws OseeCoreException {
      int transactionId = 0;
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      if (sourceBranch != null && destBranch != null) {
         try {
            chStmt.runPreparedQuery(GET_MERGE_DATA, sourceBranch.getBranchId(), destBranch.getBranchId());
            if (chStmt.next()) {
               transactionId = chStmt.getInt("commit_transaction_id");
            }
         } finally {
            chStmt.close();
         }
         if (transactionId == 0) {
            try {
               chStmt.runPreparedQuery(GET_COMMIT_TRANSACTION_COMMENT,
                     BranchManager.COMMIT_COMMENT + sourceBranch.getBranchName(), destBranch.getBranchId());
               if (chStmt.next()) {
                  transactionId = chStmt.getInt("transaction_id");
               }
            } finally {
               chStmt.close();
            }
         }
      }
      return transactionId;
   }

   public int getMergeBranchId(int sourceBranchId, int destBranchId) throws OseeCoreException {
      int mergeBranchId = 0;
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      try {
         chStmt.runPreparedQuery(GET_MERGE_DATA, sourceBranchId, destBranchId);
         if (chStmt.next()) {
            mergeBranchId = chStmt.getInt("merge_branch_id");
         }
      } finally {
         chStmt.close();
      }
      return mergeBranchId;
   }

   /**
    * The purpose of this function is find the transaction (Branch Create) that holds the last common values for two
    * branches that share a common history. If two branches share the same history than the point at which they diverged
    * should provide the reference for detecting conflicts based on the gamma at that point.
    */
   public int findCommonTransaction(Branch sourceBranch, Branch destBranch) throws OseeCoreException {
      List<Branch> sourceBranches = getBranchHierarchy(sourceBranch);
      List<Branch> destBranches = getBranchHierarchy(destBranch);
      Branch commonBranch = null;
      for (Branch branch : sourceBranches) {
         if (destBranches.contains(branch)) {
            commonBranch = branch;
            break;
         }
      }
      if (commonBranch == null) {
         throw new OseeCoreException(String.format("Can not find a common ancestor for Branch %s and Branch %s",
               sourceBranch.getBranchShortName(), destBranch.getBranchShortName()));
      }
      int sourceTransaction = 0;
      int destTransaction = 0;
      if (commonBranch.equals(destBranch)) {
         destTransaction = Integer.MAX_VALUE;
      } else {
         for (Branch branch : destBranches) {
            if (branch.getParentBranch().equals(commonBranch)) {
               destTransaction = TransactionIdManager.getStartEndPoint(branch).getKey().getTransactionNumber();
               break;
            }

         }
      }
      for (Branch branch : sourceBranches) {
         if (branch.getParentBranch().equals(commonBranch)) {
            sourceTransaction = TransactionIdManager.getStartEndPoint(branch).getKey().getTransactionNumber();
            break;
         }
      }
      return sourceTransaction <= destTransaction ? sourceTransaction : destTransaction;
   }

   private List<Branch> getBranchHierarchy(Branch branch) {
      List<Branch> ancestors = new LinkedList<Branch>();
      ancestors.add(branch);
      try {
         while (branch.getParentBranch() != BranchManager.getSystemRootBranch()) {
            ancestors.add(branch.getParentBranch());
            branch = branch.getParentBranch();
         }
      } catch (OseeCoreException ex) {
      }
      return ancestors;
   }

}
