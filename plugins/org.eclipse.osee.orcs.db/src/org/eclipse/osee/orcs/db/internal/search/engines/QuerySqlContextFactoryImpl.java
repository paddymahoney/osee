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
package org.eclipse.osee.orcs.db.internal.search.engines;

import java.util.List;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.QueryData;
import org.eclipse.osee.orcs.db.internal.search.QuerySqlContext;
import org.eclipse.osee.orcs.db.internal.search.QuerySqlContext.ObjectQueryType;
import org.eclipse.osee.orcs.db.internal.search.QuerySqlContextFactory;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.QueryType;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandlerFactory;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Roberto E. Escobar
 */
public class QuerySqlContextFactoryImpl implements QuerySqlContextFactory {

   private final Log logger;
   private final SqlHandlerFactory handlerFactory;
   private final JdbcClient jdbcClient;
   private final SqlJoinFactory joinFactory;
   private final TableEnum table;
   private final String idColumn;
   private final ObjectQueryType type;

   public QuerySqlContextFactoryImpl(Log logger, SqlJoinFactory joinFactory, JdbcClient jdbcClient, SqlHandlerFactory handlerFactory, TableEnum table, String idColumn, ObjectQueryType type) {
      this.logger = logger;
      this.joinFactory = joinFactory;
      this.jdbcClient = jdbcClient;
      this.handlerFactory = handlerFactory;
      this.table = table;
      this.idColumn = idColumn;
      this.type = type;
   }

   @Override
   public QuerySqlContext createQueryContext(OrcsSession session, QueryData queryData, QueryType queryType) {
      QuerySqlContext context = new QuerySqlContext(session, queryData.getOptions(), type);
      AbstractSqlWriter writer =
         new QuerySqlWriter(logger, joinFactory, jdbcClient, context, queryType, table, idColumn);
      List<SqlHandler<?>> handlers = handlerFactory.createHandlers(queryData.getCriteriaSets());
      writer.build(handlers);
      return context;
   }

}
