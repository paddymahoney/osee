/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.callable;

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;

public class ChangeBranchStateCallable extends AbstractDatastoreTxCallable<Void> {

   private static final String UPDATE_BRANCH_STATE = "UPDATE osee_branch SET branch_state = ? WHERE branch_id = ?";

   private final IOseeBranch branch;
   private final BranchState newState;

   public ChangeBranchStateCallable(Log logger, OrcsSession session, IOseeDatabaseService dbService, IOseeBranch branch, BranchState newState) {
      super(logger, session, dbService, String.format("Change BranchState of %s to %s", branch, newState));
      this.branch = branch;
      this.newState = newState;
   }

   @Override
   protected Void handleTxWork(OseeConnection connection) throws OseeCoreException {
      Object[] params = new Object[] {newState.getValue(), branch.getUuid()};
      getDatabaseService().runPreparedUpdate(connection, UPDATE_BRANCH_STATE, params);
      return null;
   }
}