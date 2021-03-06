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
package org.eclipse.osee.framework.ui.skynet.blam;

/**
 * @author Ryan D. Brooks
 */
public class BlamParameter {
   @SuppressWarnings("unused")
   private final String name;
   @SuppressWarnings("unused")
   private final Class<?> clazz;
   @SuppressWarnings("unused")
   private final String binding;

   public BlamParameter(String name, Class<?> clazz, String binding) {
      super();
      this.name = name;
      this.clazz = clazz;
      this.binding = binding;
   }

}
