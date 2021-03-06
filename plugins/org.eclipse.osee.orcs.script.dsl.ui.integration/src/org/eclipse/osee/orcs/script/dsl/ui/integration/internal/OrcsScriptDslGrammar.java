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
package org.eclipse.osee.orcs.script.dsl.ui.integration.internal;

import org.eclipse.osee.framework.ui.skynet.DslGrammar;
import org.eclipse.osee.framework.ui.skynet.DslGrammarStorageAdapter;
import org.eclipse.osee.orcs.script.dsl.ui.OrcsScriptDslAccess;

/**
 * @author Roberto E. Escobar
 */
public class OrcsScriptDslGrammar implements DslGrammar {

   @Override
   public String getExtension() {
      return "orcs";
   }

   @Override
   public String getGrammarId() {
      return OrcsScriptDslAccess.getGrammarId();
   }

   @Override
   public <T> T getObject(Class<? extends T> clazz) {
      return OrcsScriptDslAccess.getInjector().getProvider(clazz).get();
   }

   @Override
   public DslGrammarStorageAdapter getStorageAdapter() {
      return null;
   }

}
