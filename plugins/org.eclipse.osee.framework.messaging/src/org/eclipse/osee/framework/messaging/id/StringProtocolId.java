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
package org.eclipse.osee.framework.messaging.id;

/**
 * @author Andrew M. Finkbeiner
 */
public class StringProtocolId extends StringId implements ProtocolId {

   private static final long serialVersionUID = 2526404617710228921L;

   public StringProtocolId(Namespace namespace, Name name) {
      super(namespace, name);
   }
}
