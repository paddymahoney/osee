/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.messaging.internal;

import static org.eclipse.osee.framework.messaging.data.DefaultNodeInfos.asURI;
import java.lang.reflect.Field;
import java.net.URI;
import org.eclipse.osee.framework.jdk.core.util.annotation.AbstractFieldAnnotationHandler;
import org.eclipse.osee.framework.messaging.ConnectionNode;
import org.eclipse.osee.framework.messaging.MessageService;
import org.eclipse.osee.framework.messaging.NodeInfo;
import org.eclipse.osee.framework.messaging.rules.MessageConnection;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public class MessageConnectionFieldAnnotationHandler extends AbstractFieldAnnotationHandler<MessageConnection> {

   private final HasMessageService hasMessageService;

   public MessageConnectionFieldAnnotationHandler(HasMessageService hasMessageService) {
      super();
      this.hasMessageService = hasMessageService;
   }

   @Override
   public void handleAnnotation(MessageConnection annotation, Object object, Field field) throws Exception {
      MessageService messageService = hasMessageService.getMessageService();
      Assert.assertNotNull(messageService);
      URI brokerUri = asURI(annotation.brokerUri());
      NodeInfo nodeInfo = new NodeInfo(annotation.name(), brokerUri);
      ConnectionNode connection = messageService.get(nodeInfo);
      Assert.assertNotNull(connection);
      injectToFields(annotation, object, field, connection);
   }

}
