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
package org.eclipse.osee.framework.jdk.core.type;

import java.io.InputStream;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Ryan D. Brooks
 */
public interface IResourceRegistry {

   ResourceToken registerResource(Long universalId, ResourceToken token);

   ResourceToken getResourceToken(Long universalId) throws IllegalArgumentException;

   InputStream getResource(Long universalId) throws Exception;

   void registerAll(Iterable<ResourceToken> tokens);

}