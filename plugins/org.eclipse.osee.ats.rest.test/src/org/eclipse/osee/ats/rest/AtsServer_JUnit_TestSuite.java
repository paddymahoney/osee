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
package org.eclipse.osee.ats.rest;

import org.eclipse.osee.ats.rest.internal.notify.AtsImpl_Notify_Suite;
import org.eclipse.osee.ats.rest.util.AtsImpl_Util_Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Donald G. Dunne
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AtsImpl_Notify_Suite.class, AtsImpl_Util_Suite.class})
public class AtsServer_JUnit_TestSuite {
   // Test Suite
}
