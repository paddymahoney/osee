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
package org.eclipse.osee.disposition.rest.internal.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.eclipse.osee.disposition.model.DispoItem;
import org.eclipse.osee.disposition.model.DispoItemData;
import org.eclipse.osee.disposition.rest.internal.DispoDataFactory;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.logger.Log;

/**
 * @author Angel Avila
 */
public class TmoImporter extends AbstractDispoImporter {

   private final DispoDataFactory dataFactory;
   private final ExecutorAdmin executor;
   private final Log logger;

   TmoImporter(DispoDataFactory dataFactory, ExecutorAdmin executor, Log logger) {
      this.dataFactory = dataFactory;
      this.executor = executor;
      this.logger = logger;
   }

   @Override
   public List<DispoItem> importDirectory(Map<String, DispoItem> exisitingItems, File tmoDirectory) {
      List<DispoItem> toReturn = new LinkedList<DispoItem>();
      if (tmoDirectory.isDirectory()) {
         TmoFileFilter filter = new TmoFileFilter();
         File[] files = tmoDirectory.listFiles(filter);
         List<File> listOfFiles = Arrays.asList(files);
         int numThreads = 8;
         int partitionSize = listOfFiles.size() / numThreads;

         int remainder = listOfFiles.size() % numThreads;
         int startIndex = 0;
         int endIndex = 0;
         List<Future<List<DispoItem>>> futures = new LinkedList<Future<List<DispoItem>>>();
         for (int i = 0; i < numThreads; i++) {
            startIndex = endIndex;
            endIndex = startIndex + partitionSize;
            if (i == 0) {
               endIndex += remainder;
            }
            List<File> sublist = listOfFiles.subList(startIndex, endIndex);
            Worker worker = new Worker(sublist, dataFactory, exisitingItems, this);
            Future<List<DispoItem>> future;
            try {
               future = executor.schedule(worker);
               futures.add(future);
            } catch (Exception ex) {
               logger.error(ex, "Unable to schedule worker");
            }
         }
         for (Future<List<DispoItem>> future : futures) {
            try {
               toReturn.addAll(future.get());
            } catch (Exception ex) {
               logger.error(ex, "Unable to get future result");
            }
         }
      }
      return toReturn;
   }

   private static final class Worker implements Callable<List<DispoItem>> {

      private final List<File> sublist;
      private final DispoDataFactory dataFactory;
      Map<String, DispoItem> exisitingItems;
      private final AbstractDispoImporter importer;

      public Worker(List<File> sublist, DispoDataFactory dataFactory, Map<String, DispoItem> exisitingItems, AbstractDispoImporter importer) {
         super();
         this.sublist = sublist;
         this.dataFactory = dataFactory;
         this.exisitingItems = exisitingItems;
         this.importer = importer;
      }

      @Override
      public List<DispoItem> call() throws Exception {
         List<DispoItem> fromThread = new LinkedList<DispoItem>();
         for (File file : sublist) {
            InputStream inputStream = null;
            try {
               inputStream = new FileInputStream(file);

               String scriptName = file.getName().replaceAll("\\..*", "");

               DispoItemData itemToBuild = new DispoItemData();
               // We already have an item with this name so we now have to check the dates
               if (exisitingItems.containsKey(scriptName)) {
                  DispoItem oldItem = exisitingItems.get(scriptName);
                  Date lastUpdate = oldItem.getLastUpdate();
                  boolean wasSameFile =
                     DiscrepancyParser.buildItemFromFile(itemToBuild, file.getName(), inputStream, false, lastUpdate);
                  if (!wasSameFile) {
                     importer.mergeDispoItems(itemToBuild, oldItem);
                     fromThread.add(itemToBuild);
                  }
               } else {
                  DiscrepancyParser.buildItemFromFile(itemToBuild, file.getName(), inputStream, true, new Date());
                  dataFactory.initDispoItem(itemToBuild);
                  fromThread.add(itemToBuild);
               }
            } finally {
               Lib.close(inputStream);
            }
         }
         return fromThread;
      }

   };

   private static final class TmoFileFilter implements FilenameFilter {

      @Override
      public boolean accept(File dir, String name) {
         return name.endsWith(".tmo");
      }
   }

}