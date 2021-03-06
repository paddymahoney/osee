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
package org.eclipse.osee.ats.client.demo.internal;

import org.eclipse.osee.framework.ui.plugin.OseeUiActivator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends OseeUiActivator {
   private static Activator plugin;
   public static final String PLUGIN_ID = "org.eclipse.osee.ats.client.demo";

   public Activator() {
      super(PLUGIN_ID);
      plugin = this;
   }

   public static Activator getInstance() {
      return plugin;
   }
}