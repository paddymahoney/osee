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
package org.eclipse.osee.orcs.core.ds;

import org.eclipse.osee.executor.admin.HasCancellation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Andrew M. Finkbeiner
 */
public interface Loader {

   Loader setOptions(Options sourceOptions);

   void load(HasCancellation cancellation, LoadDataHandler handler) throws OseeCoreException;

   void load(LoadDataHandler handler) throws OseeCoreException;

}
