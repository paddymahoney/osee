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
package org.eclipse.osee.framework.skynet.core.commit;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;

public class ChangeCache {
   private final IChangeDataAccessor changeDataAccessor;

   public ChangeCache(IChangeDataAccessor changeDataAccessor) {
      this.changeDataAccessor = changeDataAccessor;
   }

   public List<OseeChange> getRawChangeData(IProgressMonitor monitor, ChangeLocator locator) throws Exception {
      List<OseeChange> changeData = new ArrayList<OseeChange>();
      changeDataAccessor.loadChangeData(monitor, locator, changeData);
      return changeData;
   }
}
