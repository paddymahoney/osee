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
package org.eclipse.osee.framework.database.internal.core;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.apache.commons.dbcp.PoolingDriver;
import org.eclipse.osee.framework.core.data.IDatabaseInfo;
import org.eclipse.osee.framework.core.data.LazyObject;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.database.core.IConnectionFactory;

/**
 * @author Roberto E. Escobar
 */
public class PoolFactory {

   private final PoolingDriverRef poolingDriver = new PoolingDriverRef();
   private final PoolConfigurationProvider poolConfig = new PoolConfigurationProvider();

   private final Map<String, IConnectionFactory> factories;

   public PoolFactory(Map<String, IConnectionFactory> factories) {
      this.factories = factories;
   }

   public void disposePools(Iterable<String> keys) throws OseeCoreException {
      PoolingDriver driver = poolingDriver.get();
      for (String key : keys) {
         try {
            driver.closePool(key);
         } catch (SQLException ex) {
            // Do Nothing
         }
      }
      poolingDriver.set(null);
   }

   public Callable<DataSource> createDataSourceFetcher(IDatabaseInfo dbInfo) {
      return new PooledDataSourceFetcher(factories, poolingDriver, poolConfig, dbInfo);
   }

   private static final class PoolingDriverRef extends LazyObject<PoolingDriver> {

      @Override
      protected PoolingDriver instance() throws OseeCoreException {
         try {
            Class.forName(PoolConfiguration.CONNECTION_POOL_DRIVER);
         } catch (Exception ex) {
            throw new OseeDataStoreException(ex, "Error loading connection pool driver [%s]",
               PoolConfiguration.CONNECTION_POOL_DRIVER);
         }
         PoolingDriver driver;
         try {
            driver = (PoolingDriver) DriverManager.getDriver(PoolConfiguration.CONNECTION_POOL_ID);
         } catch (SQLException ex) {
            throw new OseeDataStoreException(ex, "Error finding connection pool driver with id [%s]",
               PoolConfiguration.CONNECTION_POOL_ID);
         }
         return driver;
      }

   }

   public static class PoolConfiguration {
      private static final String CONNECTION_POOL_DRIVER = "org.apache.commons.dbcp.PoolingDriver";
      private static final String CONNECTION_POOL_ID = "jdbc:apache:commons:dbcp:";

      private final Properties config;

      public PoolConfiguration(Properties config) {
         this.config = config;
      }

      public String getConnectionPoolId() {
         return CONNECTION_POOL_ID;
      }

      public String getConnectionPoolDriver() {
         return CONNECTION_POOL_DRIVER;
      }

      public Properties getProperties() {
         return config;
      }
   }

}
