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
package org.eclipse.osee.framework.skynet.core.event.listener;

import org.eclipse.osee.framework.skynet.core.event.model.RemoteEventServiceEventType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;

/**
 * @author Donald G. Dunne
 */
public interface IRemoteEventManagerEventListener extends IEventListener {
   public void handleRemoteEventManagerEvent(Sender sender, RemoteEventServiceEventType remoteEventServiceEventType);

}
