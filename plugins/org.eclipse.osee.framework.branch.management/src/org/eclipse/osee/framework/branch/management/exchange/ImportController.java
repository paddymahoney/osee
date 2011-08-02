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
package org.eclipse.osee.framework.branch.management.exchange;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osee.framework.branch.management.ImportOptions;
import org.eclipse.osee.framework.branch.management.exchange.handler.BaseDbSaxHandler;
import org.eclipse.osee.framework.branch.management.exchange.handler.BranchDataSaxHandler;
import org.eclipse.osee.framework.branch.management.exchange.handler.DbTableSaxHandler;
import org.eclipse.osee.framework.branch.management.exchange.handler.ExportItem;
import org.eclipse.osee.framework.branch.management.exchange.handler.IExportItem;
import org.eclipse.osee.framework.branch.management.exchange.handler.ManifestSaxHandler;
import org.eclipse.osee.framework.branch.management.exchange.handler.MetaData;
import org.eclipse.osee.framework.branch.management.exchange.handler.MetaDataSaxHandler;
import org.eclipse.osee.framework.branch.management.exchange.transform.ExchangeDataProcessor;
import org.eclipse.osee.framework.branch.management.exchange.transform.ExchangeTransformProvider;
import org.eclipse.osee.framework.branch.management.exchange.transform.ExchangeTransformer;
import org.eclipse.osee.framework.branch.management.exchange.transform.IExchangeTransformProvider;
import org.eclipse.osee.framework.branch.management.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.DbTransaction;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public final class ImportController {
   private static final String SAVE_POINT_PREFIX = "save.point.";

   private static final String INSERT_INTO_IMPORT_SOURCES =
      "INSERT INTO osee_import_source (import_id, db_source_guid, source_export_date, date_imported) VALUES (?, ?, ?, ?)";

   private static final String INSERT_INTO_IMPORT_SAVE_POINT =
      "INSERT INTO osee_import_save_point (import_id, save_point_name, status, state_error) VALUES (?, ?, ?, ?)";

   private static final String QUERY_SAVE_POINTS_FROM_IMPORT_MAP =
      "SELECT save_point_name from osee_import_save_point oisp, osee_import_source ois WHERE ois.import_id = oisp.import_id AND oisp.status = 1 AND ois.db_source_guid = ? AND ois.source_export_date = ?";

   private final OseeServices oseeServices;
   private final IOseeExchangeDataProvider exportDataProvider;
   private final PropertyStore options;
   private final int[] branchesToImport = null;
   private final Map<String, SavePoint> savePoints;
   private final ExchangeDataProcessor exchangeDataProcessor;
   private final OperationLogger logger;

   private ExchangeTransformer exchangeTransformer;
   private TranslationManager translator;
   private ManifestSaxHandler manifestHandler;
   private MetaDataSaxHandler metadataHandler;
   private String currentSavePoint;

   ImportController(OseeServices oseeServices, IOseeExchangeDataProvider exportDataProvider, PropertyStore options, List<Integer> branchIds, OperationLogger logger) throws OseeArgumentException {
      this.oseeServices = oseeServices;
      this.exportDataProvider = exportDataProvider;
      this.options = options;
      this.savePoints = new LinkedHashMap<String, SavePoint>();
      this.exchangeDataProcessor = new ExchangeDataProcessor(exportDataProvider);
      this.logger = logger;

      if (!branchIds.isEmpty()) {
         throw new OseeArgumentException("selective branch import is not supported.");
      }
   }

   private void checkPreconditions() throws OseeCoreException {
      if (oseeServices.getDatabaseService().isProduction()) {
         throw new OseeStateException("DO NOT IMPORT ON PRODUCTION");
      }
   }

   private void setup() throws Exception {
      currentSavePoint = "sourceSetup";

      IExchangeTransformProvider transformProvider = new ExchangeTransformProvider(oseeServices.getCachingService());
      exchangeTransformer = new ExchangeTransformer(transformProvider, exchangeDataProcessor);

      exchangeTransformer.applyTransforms(logger);

      currentSavePoint = "manifest";
      manifestHandler = new ManifestSaxHandler();
      exchangeDataProcessor.parse(ExportItem.EXPORT_MANIFEST, manifestHandler);

      currentSavePoint = "setup";
      translator = new TranslationManager(oseeServices.getDatabaseService());
      translator.configure(options);

      // Process database meta data
      currentSavePoint = manifestHandler.getMetadataFile();
      metadataHandler = new MetaDataSaxHandler(oseeServices.getDatabaseService());
      exchangeDataProcessor.parse(ExportItem.EXPORT_DB_SCHEMA, metadataHandler);
      metadataHandler.checkAndLoadTargetDbMetadata();

      // Load Import Indexes
      currentSavePoint = "load.translator";
      translator.loadTranslators(manifestHandler.getSourceDatabaseId());

      loadImportTrace(manifestHandler.getSourceDatabaseId(), manifestHandler.getSourceExportDate());
   }

   private void cleanup() throws OseeCoreException {
      try {
         CommitImportSavePointsTx saveImportState = new CommitImportSavePointsTx();
         saveImportState.execute();
      } catch (OseeCoreException ex) {
         OseeLog.log(this.getClass(), Level.WARNING,
            "Error during save point save - you will not be able to reimport from last source again.");
         throw ex;
      } finally {
         exchangeDataProcessor.cleanUp();
         translator = null;
         manifestHandler = null;
         metadataHandler = null;
         exchangeTransformer = null;
         savePoints.clear();
      }
   }

   public void execute() throws OseeCoreException {
      checkPreconditions();
      savePoints.clear();
      try {
         currentSavePoint = "start";
         addSavePoint(currentSavePoint);

         setup();

         ImportBranchesTx importBranchesTx = new ImportBranchesTx();
         importBranchesTx.execute();

         currentSavePoint = "init.relational.objects";
         processImportFiles(manifestHandler.getImportFiles());

         importBranchesTx.updateBranchParentTransactionId();

         exchangeTransformer.applyFinalTransforms(logger);

         currentSavePoint = "stop";
         addSavePoint(currentSavePoint);
      } catch (Throwable ex) {
         reportError(currentSavePoint, ex);
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      } finally {
         cleanup();
      }
   }

   private void initializeHandler(OseeConnection connection, BaseDbSaxHandler handler, MetaData metadata) {
      handler.setConnection(connection);
      handler.setMetaData(metadata);
      handler.setOptions(options);
      handler.setTranslator(translator);
   }

   private void process(BaseDbSaxHandler handler, OseeConnection connection, IExportItem exportItem) throws OseeCoreException {
      MetaData metadata = checkMetadata(exportItem);
      initializeHandler(connection, handler, metadata);
      boolean cleanDataTable = options.getBoolean(ImportOptions.CLEAN_BEFORE_IMPORT.name());
      cleanDataTable &= !doesSavePointExist(currentSavePoint);
      OseeLog.logf(this.getClass(), Level.INFO, "Importing: [%s] %s Meta: %s", exportItem.getSource(),
         cleanDataTable ? "clean before import" : "", metadata.getColumnNames());
      if (cleanDataTable) {
         handler.clearDataTable();
      }
      try {
         exchangeDataProcessor.parse(exportItem, handler);
      } catch (Exception ex) {
         if (ex instanceof OseeCoreException) {
            throw (OseeCoreException) ex;
         }
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   private MetaData checkMetadata(IExportItem importFile) throws OseeArgumentException {
      MetaData metadata = metadataHandler.getMetadata(importFile.getSource());
      if (metadata == null) {
         throw new OseeArgumentException("Invalid metadata for [%s]", importFile.getSource());
      }
      return metadata;
   }

   private void processImportFiles(Collection<IExportItem> importItems) throws Exception {
      final DbTableSaxHandler handler =
         DbTableSaxHandler.createWithLimitedCache(oseeServices, exportDataProvider, 50000);
      handler.setSelectedBranchIds(branchesToImport);

      for (final IExportItem item : importItems) {
         OseeLog.logf(Activator.class, Level.INFO, "starting import for [%s]", item);
         currentSavePoint = item.getSource();
         handler.setExportItem(item);
         if (!doesSavePointExist(currentSavePoint)) {
            process(handler, ConnectionHandler.getConnection(), item);
            handler.store();
            handler.reset();
            addSavePoint(currentSavePoint);
         } else {
            OseeLog.logf(Activator.class, Level.INFO,
               "Save point found for: [%s] - skipping", item.getSource());
         }
      }
   }

   private void loadImportTrace(String sourceDatabaseId, Date sourceExportDate) throws OseeCoreException {
      IOseeStatement chStmt = oseeServices.getDatabaseService().getStatement();
      try {
         currentSavePoint = "load.save.points";
         chStmt.runPreparedQuery(QUERY_SAVE_POINTS_FROM_IMPORT_MAP, sourceDatabaseId,
            new Timestamp(sourceExportDate.getTime()));
         while (chStmt.next()) {
            String key = chStmt.getString("save_point_name");
            savePoints.put(key, new SavePoint(key));
         }
         addSavePoint(currentSavePoint);
      } finally {
         chStmt.close();
      }
   }

   private String asSavePointName(String sourceName) {
      return SAVE_POINT_PREFIX + sourceName;
   }

   private boolean doesSavePointExist(String sourceName) {
      return savePoints.containsKey(asSavePointName(sourceName));
   }

   private void addSavePoint(String sourceName) {
      String key = asSavePointName(sourceName);
      SavePoint point = savePoints.get(key);
      if (point == null) {
         point = new SavePoint(key);
         savePoints.put(key, point);
      }
   }

   private void reportError(String sourceName, Throwable ex) {
      String key = asSavePointName(sourceName);
      SavePoint point = savePoints.get(key);
      if (point == null) {
         point = new SavePoint(key);
         savePoints.put(key, point);
      }
      point.addError(ex);
   }

   private final class ImportBranchesTx extends DbTransaction {

      private final BranchDataSaxHandler branchHandler;
      private int[] branchesStored;

      public ImportBranchesTx() {
         super();
         branchHandler = BranchDataSaxHandler.createWithCacheAll(oseeServices.getDatabaseService());
         branchesStored = new int[0];
      }

      public void updateBranchParentTransactionId() throws OseeCoreException {
         currentSavePoint = "update branch parent transaction id";
         if (!doesSavePointExist(currentSavePoint)) {
            if (branchesStored.length > 0) {
               branchHandler.updateParentTransactionId(branchesStored);
            }
         } else {
            OseeLog.logf(this.getClass(), Level.INFO,
               "Save point found for: [%s] - skipping", currentSavePoint);
         }
      }

      @Override
      protected void handleTxWork(OseeConnection connection) throws OseeCoreException {
         // Import Branches
         currentSavePoint = manifestHandler.getBranchFile().getSource();
         process(branchHandler, connection, manifestHandler.getBranchFile());

         if (!doesSavePointExist(currentSavePoint)) {
            branchesStored = branchHandler.store(true, branchesToImport);
            addSavePoint(currentSavePoint);
         } else {
            // This step has already been performed - only get branches needed for remaining operations
            OseeLog.logf(this.getClass(), Level.INFO,
               "Save point found for: [%s] - skipping", currentSavePoint);
            branchesStored = branchHandler.store(false, branchesToImport);
         }
      }
   }

   private final class CommitImportSavePointsTx extends DbTransaction {

      public CommitImportSavePointsTx() {
         super();
      }

      @SuppressWarnings("unchecked")
      @Override
      protected void handleTxWork(OseeConnection connection) throws OseeCoreException {
         if (manifestHandler != null && translator != null) {
            int importIdIndex = oseeServices.getDatabaseService().getSequence().getNextImportId();
            String sourceDatabaseId = manifestHandler.getSourceDatabaseId();
            Timestamp importDate = new Timestamp(new Date().getTime());
            Timestamp exportDate = new Timestamp(manifestHandler.getSourceExportDate().getTime());
            oseeServices.getDatabaseService().runPreparedUpdate(connection, INSERT_INTO_IMPORT_SOURCES, importIdIndex,
               sourceDatabaseId, exportDate, importDate);

            translator.store(connection, importIdIndex);

            List<Object[]> data = new ArrayList<Object[]>();
            for (SavePoint savePoint : savePoints.values()) {
               int status = 1;
               String comment = "";
               if (savePoint.hasErrors()) {
                  status = -1;
                  StringBuilder builder = new StringBuilder();
                  for (Throwable ex : savePoint.getErrors()) {
                     builder.append(Lib.exceptionToString(ex).replaceAll("\n", " "));
                  }
                  if (builder.length() < 4000) {
                     comment = builder.toString();
                  } else {
                     comment = builder.substring(0, 3999);
                  }
               }
               data.add(new Object[] {importIdIndex, savePoint.getName(), status, comment});
            }
            oseeServices.getDatabaseService().runBatchUpdate(connection, INSERT_INTO_IMPORT_SAVE_POINT, data);
         } else {
            throw new OseeStateException("Import didn't make it past initialization");
         }
      }
   }

   private final class SavePoint {
      private final String savePointName;
      private List<Throwable> errors;

      public SavePoint(String name) {
         this.savePointName = name;
         this.errors = null;
      }

      public String getName() {
         return savePointName;
      }

      public void addError(Throwable ex) {
         if (errors == null) {
            errors = new ArrayList<Throwable>();
         }
         if (!errors.contains(ex)) {
            errors.add(ex);
         }
      }

      public List<Throwable> getErrors() {
         if (errors == null) {
            return Collections.emptyList();
         } else {
            return this.errors;
         }
      }

      public boolean hasErrors() {
         return errors != null;
      }
   }
}
