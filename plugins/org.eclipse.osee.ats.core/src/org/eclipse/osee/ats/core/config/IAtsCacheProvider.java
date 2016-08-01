/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.config;

import org.eclipse.osee.ats.api.config.IAtsCache;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsCacheProvider {

   IAtsCache getCache() throws OseeStateException;

}