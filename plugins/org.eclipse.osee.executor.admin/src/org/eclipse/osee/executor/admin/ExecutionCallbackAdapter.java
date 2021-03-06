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
package org.eclipse.osee.executor.admin;

/**
 * @author Roberto E. Escobar
 */
public class ExecutionCallbackAdapter<T> implements ExecutionCallback<T> {

   @Override
   public void onCancelled() {
      // Sub-class
   }

   @Override
   public void onSuccess(T result) {
      // Sub-class
   }

   @Override
   public void onFailure(Throwable throwable) {
      // Sub-class
   }

}
