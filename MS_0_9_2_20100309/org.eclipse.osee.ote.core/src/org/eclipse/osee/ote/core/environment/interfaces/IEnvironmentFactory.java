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
package org.eclipse.osee.ote.core.environment.interfaces;

import org.eclipse.osee.ote.core.framework.ICommandContextFactory;
import org.eclipse.osee.ote.core.framework.IRunManager;
import org.eclipse.osee.ote.core.framework.command.ICommandManager;



public interface IEnvironmentFactory {

   ITimerControl getTimerControl();
   IScriptControl getScriptControl();
   IReportData getReportDataControl();
   ITestLogger getTestLogger();
   
   ICommandManager getCommandManager();
   IRunManager getRunManager();
   ICommandContextFactory getCommandContextFactory();
   IRuntimeLibraryManager getRuntimeManager();
   ITestStation getTestStation();
   /**
    * gets the class for RuntimeConfiguration. Return null if the class needs to be loaded using reflection
    * @return the class or null if the class must be dynamically loaded
    */
   Class<? extends RuntimeConfigurationInitilizer> getRuntimeClass();
   
   /**
    * dybamically loads runtime configuration class. This is only called if {@link #getRuntimeClass()} returns null;
    * @return
    */
   String getRuntimeLibraryConfiguration();
}
