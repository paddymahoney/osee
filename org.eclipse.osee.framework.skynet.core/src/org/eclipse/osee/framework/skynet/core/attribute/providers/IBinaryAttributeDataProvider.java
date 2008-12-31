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
package org.eclipse.osee.framework.skynet.core.attribute.providers;

import java.nio.ByteBuffer;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public interface IBinaryAttributeDataProvider extends IAttributeDataProvider {

   public ByteBuffer getValueAsBytes() throws OseeCoreException;

   public boolean setValue(ByteBuffer data) throws OseeCoreException;

}
