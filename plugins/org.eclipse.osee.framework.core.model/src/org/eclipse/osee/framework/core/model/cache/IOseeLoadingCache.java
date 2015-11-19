/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.model.cache;

/**
 * @author Roberto E. Escobar
 */
public interface IOseeLoadingCache<KEY, TYPE> extends IOseeCache<KEY, TYPE> {

   long getLastLoaded();

   boolean isLoaded();

   void invalidate();

   boolean reloadCache();

}