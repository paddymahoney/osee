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
package org.eclipse.osee.disposition.rest.internal.importer;

import org.xml.sax.Attributes;

/**
 * @author Andrew M. Finkbeiner
 */
public class ScriptVersion extends ElementHandlers {

   public ScriptVersion() {
      super("ScriptVersion");
   }

   @Override
   public Object createStartElementFoundObject(String uri, String localName, String name, Attributes attributes) {
      return new ScriptVersionData(attributes.getValue("lastAuthor"), attributes.getValue("lastModified"),
         attributes.getValue("modifiedFlag"), attributes.getValue("repositoryType"), attributes.getValue("revision"),
         attributes.getValue("url"));
   }
}
