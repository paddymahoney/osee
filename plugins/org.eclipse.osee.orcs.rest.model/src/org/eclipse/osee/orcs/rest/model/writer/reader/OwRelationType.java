/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.model.writer.reader;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class OwRelationType extends OwNamedBase {

   private boolean sideA;
   private String sideName;

   public boolean isSideA() {
      return sideA;
   }

   public void setSideA(boolean sideA) {
      this.sideA = sideA;
   }

   public String getSideName() {
      return sideName;
   }

   public void setSideName(String sideName) {
      this.sideName = sideName;
   }

   @Override
   public String toString() {
      return "OwRelationType [sideA=" + sideA + ", sideName=" + sideName + ", uuid=" + uuid + ", data=" + data + "]";
   }

}