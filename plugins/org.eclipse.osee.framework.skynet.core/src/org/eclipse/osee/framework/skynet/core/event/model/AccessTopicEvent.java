/*******************************************************************************
 * Copyright (c) 2016 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.event.model;

import org.eclipse.osee.framework.core.event.AbstractTopicEvent;
import org.eclipse.osee.framework.core.event.EventType;

/**
 * @author Donald G. Dunne
 */
public class AccessTopicEvent extends AbstractTopicEvent {

   public static final AccessTopicEvent ACCESS_ARTIFACT_MODIFIED =
      new AccessTopicEvent(EventType.LocalAndRemote, "framework/access/artifact/modified");
   public static final AccessTopicEvent ACCESS_ARTIFACT_LOCK_MODIFIED =
      new AccessTopicEvent(EventType.LocalAndRemote, "framework/access/artifact/lock/modified");
   public static final AccessTopicEvent ACCESS_BRANCH_MODIFIED =
      new AccessTopicEvent(EventType.LocalAndRemote, "framework/access/branch/modified");
   public static final AccessTopicEvent USER_AUTHENTICATED =
      new AccessTopicEvent(EventType.LocalOnly, "framework/access/user/authenticated");

   private AccessTopicEvent(EventType eventType, String topic) {
      super(eventType, topic);
   }

}
