/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.dsl;

/**
 * Initialization support for running Xtext languages without equinox extension registry
 * 
 * @author Donald G. Dunne
 */
public class AtsDslStandaloneSetup extends AtsDslStandaloneSetupGenerated {

   public static void doSetup() {
      new AtsDslStandaloneSetup().createInjectorAndDoEMFRegistration();
   }
}
