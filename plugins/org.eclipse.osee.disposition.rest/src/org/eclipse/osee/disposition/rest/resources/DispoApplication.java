/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.rest.resources;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.eclipse.osee.disposition.rest.DispoApi;
import org.eclipse.osee.disposition.rest.messages.DispoAnnotationMessageReader;
import org.eclipse.osee.disposition.rest.messages.DispoAnnotationMessageWriter;
import org.eclipse.osee.disposition.rest.messages.DispoItemMessageReader;
import org.eclipse.osee.disposition.rest.messages.DispoItemMessageWriter;
import org.eclipse.osee.disposition.rest.messages.DispoSetMessageReader;
import org.eclipse.osee.disposition.rest.messages.DispoSetMessageWriter;
import org.eclipse.osee.disposition.rest.util.HtmlWriter;
import org.eclipse.osee.disposition.rest.util.TemplateRegistry;

/**
 * @author Angel Avila
 */
public final class DispoApplication extends Application {

   private DispoApi dispoApi;

   public void setDispoApi(DispoApi dispoApi) {
      this.dispoApi = dispoApi;
   }

   @Override
   public Set<Object> getSingletons() {
      Set<Object> singletons = new HashSet<Object>();
      singletons.add(new DispoSetMessageReader());
      singletons.add(new DispoSetMessageWriter());
      singletons.add(new DispoItemMessageReader());
      singletons.add(new DispoItemMessageWriter());
      singletons.add(new DispoAnnotationMessageReader());
      singletons.add(new DispoAnnotationMessageWriter());

      HtmlWriter writer = new HtmlWriter(TemplateRegistry.newRegistry());
      singletons.add(new DispoProgramResource(dispoApi, writer));
      return singletons;
   }
}