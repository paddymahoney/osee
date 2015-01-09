/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.cpa;

import org.eclipse.osee.framework.jdk.core.type.UuidNamedIdentityJaxRs;

/**
 * @author Donald G. Dunne
 */
public class CpaBuild extends UuidNamedIdentityJaxRs<Long> {

   public CpaBuild(Long uuid, String name) {
      super(uuid, name);
   }
}