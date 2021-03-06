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
package org.eclipse.osee.orcs.core;

import java.util.List;
import org.eclipse.osee.orcs.core.internal.attribute.Attribute;

/**
 * @author Roberto E. Escobar
 */
public interface AttributeClassProvider {

   List<Class<? extends Attribute<?>>> getClasses();
}
