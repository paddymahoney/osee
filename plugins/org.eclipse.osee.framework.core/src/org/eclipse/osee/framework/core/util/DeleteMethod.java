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
package org.eclipse.osee.framework.core.util;

import org.apache.commons.httpclient.methods.PostMethod;

public class DeleteMethod extends PostMethod {

   public DeleteMethod(String uri) {
      super(uri);
   }

   @Override
   public String getName() {
      return "DELETE";
   }

}
