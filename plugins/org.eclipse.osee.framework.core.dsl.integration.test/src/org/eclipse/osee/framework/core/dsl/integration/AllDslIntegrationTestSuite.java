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
package org.eclipse.osee.framework.core.dsl.integration;

import org.eclipse.osee.framework.core.dsl.integration.internal.InternalTestSuite;
import org.eclipse.osee.framework.core.dsl.integration.util.DslUtilTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Roberto E. Escobar
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ //
   InternalTestSuite.class, //
   DslUtilTestSuite.class, //
   OseeDslAccessModelTest.class, //
})
public class AllDslIntegrationTestSuite {
   // Test Suite
}
