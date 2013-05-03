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
package org.eclipse.osee.cache.admin;

import java.util.Map;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author John Misinco
 * @author Roberto E. Escobar
 */
public interface CacheDataLoader<K, V> {

   Map<K, V> load(Iterable<? extends K> keys) throws OseeCoreException;

   V load(K key) throws OseeCoreException;

   V reload(K key, V oldValue) throws OseeCoreException;
}