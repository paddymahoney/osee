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
package org.eclipse.osee.ote.message.condition;

public class TransmissionCountCondition extends AbstractCondition {

   private final int max;

   public TransmissionCountCondition(int max) {
      this.max = max;
   }

   @Override
   public boolean check() {
      return getCheckCount() >= max;
   }

   public int getMaxTransmitCount() {
      return max;
   }

}