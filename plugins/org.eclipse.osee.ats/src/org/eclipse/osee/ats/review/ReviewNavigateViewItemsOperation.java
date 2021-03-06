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
package org.eclipse.osee.ats.review;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.operation.AbstractOperation;

/**
 * @author Donald G. Dunne
 */
public class ReviewNavigateViewItemsOperation extends AbstractOperation {

   public ReviewNavigateViewItemsOperation() {
      super("Loading Review Navigate View Items", Activator.PLUGIN_ID);
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      ReviewNavigateViewItems.getInstance().getSearchNavigateItems();
   }
}
