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
package org.eclipse.osee.framework.core.dsl.integration.mocks;

import org.eclipse.osee.framework.core.model.access.AccessDetail;
import org.eclipse.osee.framework.core.model.access.AccessDetailCollector;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public final class CheckAccessDetailCollectorNotCalled implements AccessDetailCollector {

   @Override
   public void collect(AccessDetail<?> accessDetail) {
      Assert.fail("Method was unexpectedly called");
   }

};