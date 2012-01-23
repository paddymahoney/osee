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
package org.eclipse.osee.framework.skynet.core.event.systems;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.eclipse.osee.framework.messaging.event.res.IOseeCoreModelEventService;
import org.eclipse.osee.framework.skynet.core.event.EventSystemPreferences;
import org.eclipse.osee.framework.skynet.core.event.OseeEventThreadFactory;
import org.eclipse.osee.framework.skynet.core.event.listener.IEventListener;
import org.eclipse.osee.framework.skynet.core.event.systems.InternalEventManager.ConnectionStatus;

/**
 * @author Roberto E. Escobar
 */
public class EventManagerFactory {

   public InternalEventManager createNewEventManager(IOseeCoreModelEventService coreModelEventService, EventSystemPreferences preferences, Collection<IEventListener> listeners, Collection<IEventListener> priorityListeners, ConnectionStatus connectionStatus) {
      ExecutorService executorService = createExecutor("Osee Client Events");
      InternalEventManager eventManager =
         new InternalEventManager(coreModelEventService, listeners, priorityListeners, executorService, preferences,
            connectionStatus);
      return eventManager;
   }

   private ExecutorService createExecutor(String threadPrefix) {
      int numberOfProcessors = Runtime.getRuntime().availableProcessors();
      if (numberOfProcessors > 4) {
         numberOfProcessors = 4;
      }
      ThreadFactory threadFactory = new OseeEventThreadFactory(threadPrefix);
      return Executors.newFixedThreadPool(numberOfProcessors, threadFactory);
   }

}
