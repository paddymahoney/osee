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
package org.eclipse.osee.disposition.rest.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.eclipse.osee.disposition.model.DispoItemData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Angel Avila
 */
public class DispoItemMessageWriter implements MessageBodyWriter<DispoItemData> {

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type == DispoItemData.class;
   }

   @Override
   public long getSize(DispoItemData dispoItem, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return -1;
   }

   @Override
   public void writeTo(DispoItemData dispoItem, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
      JSONObject jsonObject = new JSONObject(dispoItem);
      try {
         jsonObject.put("annotationsList", dispoItem.getAnnotationsList());
         jsonObject.put("discrepanciesList", dispoItem.getDiscrepanciesList());
      } catch (JSONException ex) {
         throw new IOException("Could not get Lists from Disposition Item", ex);
      }
      String jsonString = jsonObject.toString();
      entityStream.write(jsonString.getBytes(Charset.forName("UTF-8")));
   }
}