/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.util;

import org.eclipse.osee.ats.AtsImage;

/**
 * @author Donald G. Dunne
 */
public class AtsImageTest extends AbstractImageManagerTest {

   public AtsImageTest() {
      super("AtsImage", AtsImage.values());
   }

}
