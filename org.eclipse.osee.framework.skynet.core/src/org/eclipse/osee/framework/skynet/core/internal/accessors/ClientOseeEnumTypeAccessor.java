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
package org.eclipse.osee.framework.skynet.core.internal.accessors;

import java.util.Collection;
import org.eclipse.osee.framework.core.cache.IOseeCache;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.OseeEnumType;
import org.eclipse.osee.framework.core.model.OseeEnumTypeFactory;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryServiceProvider;

/**
 * @author Roberto E. Escobar
 */
public class ClientOseeEnumTypeAccessor extends AbstractClientDataAccessor<OseeEnumType> {

   public ClientOseeEnumTypeAccessor(IOseeModelFactoryServiceProvider factoryProvider) {
      super(factoryProvider);
   }

   protected OseeEnumTypeFactory getFactory() throws OseeCoreException {
      return getOseeFactoryService().getOseeEnumTypeFactory();
   }

   @Override
   protected Collection<OseeEnumType> updateCache(IOseeCache<OseeEnumType> cache) throws OseeCoreException {
      //      OseeEnumTypeFactory factory = getFactory();
      //      for (OseeEnumType srcItem : items) {
      //         OseeEnumType updated =
      //               factory.createOrUpdate(cache, srcItem.getId(), srcItem.getModificationType(), srcItem.getGuid(),
      //                     srcItem.getName());
      //         OseeEnumEntry[] entries = srcItem.values();
      //         updated.setEntries(Arrays.asList(entries));
      //      }
      return null;
   }
}
