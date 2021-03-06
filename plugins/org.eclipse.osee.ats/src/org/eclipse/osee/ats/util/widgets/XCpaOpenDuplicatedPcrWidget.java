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
package org.eclipse.osee.ats.util.widgets;

import org.eclipse.osee.ats.api.data.AtsAttributeTypes;

public class XCpaOpenDuplicatedPcrWidget extends XCpaOpenPcrWidget {

   public static final String WIDGET_ID = XCpaOpenDuplicatedPcrWidget.class.getSimpleName();

   public XCpaOpenDuplicatedPcrWidget() {
      super(AtsAttributeTypes.DuplicatedPcrId);
   }

}
