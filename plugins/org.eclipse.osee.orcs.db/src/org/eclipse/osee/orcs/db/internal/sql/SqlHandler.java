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
package org.eclipse.osee.orcs.db.internal.sql;

import org.eclipse.osee.framework.jdk.core.type.HasPriority;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.db.internal.IdentityLocator;

/**
 * @author Roberto E. Escobar
 */
public abstract class SqlHandler<T extends Criteria> implements HasPriority {

   private Log logger;
   private IdentityLocator idService;
   private int level;

   public void setIdentityService(IdentityLocator idService) {
      this.idService = idService;
   }

   public IdentityLocator getIdentityService() {
      return idService;
   }

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public Log getLogger() {
      return logger;
   }

   public int getLevel() {
      return level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   @Override
   public abstract int getPriority();

   public void setData(T criteria) {
      // do nothing
   }

   public abstract void addTables(AbstractSqlWriter writer) throws OseeCoreException;

   public abstract boolean addPredicates(AbstractSqlWriter writer) throws OseeCoreException;

   public void addWithTables(AbstractSqlWriter writer) throws OseeCoreException {
      // Do Nothing
   }

   public void addSelect(AbstractSqlWriter sqlWriter) throws OseeCoreException {
      // Do Nothing
   }

}
