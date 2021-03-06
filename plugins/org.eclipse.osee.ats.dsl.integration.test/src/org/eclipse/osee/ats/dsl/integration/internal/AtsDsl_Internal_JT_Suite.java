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
package org.eclipse.osee.ats.dsl.integration.internal;

import org.eclipse.osee.ats.dsl.integration.internal.model.AtsDsl_Internal_Model_JT_Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Donald G. Dunne
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   AtsDsl_Internal_Model_JT_Suite.class,
   ConvertWorkDefinitionToAtsDslTest.class,
   ConvertAtsDslToRuleDefinitionTest.class})
public class AtsDsl_Internal_JT_Suite {
   // Test Suite
}
