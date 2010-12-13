/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.results;

import org.eclipse.osee.framework.core.operation.OperationReporter;
import org.eclipse.osee.framework.ui.skynet.results.table.ResultsEditorTableTab;
import org.eclipse.osee.framework.ui.skynet.results.table.ResultsXViewerRow;

public class ResultsReporter extends OperationReporter {
   private final ResultsEditorTableTab resultsTab;

   public ResultsReporter(ResultsEditorTableTab resultsTab) {
      this.resultsTab = resultsTab;
   }

   @Override
   public void report(String... row) {
      resultsTab.addRow(new ResultsXViewerRow(row));
   }
}