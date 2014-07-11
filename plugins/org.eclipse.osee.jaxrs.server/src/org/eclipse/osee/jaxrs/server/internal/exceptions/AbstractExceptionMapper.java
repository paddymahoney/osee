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
package org.eclipse.osee.jaxrs.server.internal.exceptions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.osee.jaxrs.ErrorResponse;
import org.eclipse.osee.jaxrs.OseeWebApplicationException;
import org.eclipse.osee.jaxrs.server.internal.JaxRsUtils;
import org.eclipse.osee.logger.Log;

/**
 * @author Roberto E. Escobar
 */
@Provider
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

   private final Log logger;

   public AbstractExceptionMapper(Log logger) {
      super();
      this.logger = logger;
   }

   @Context
   private HttpHeaders headers;

   @Context
   private UriInfo uriInfo;

   public Log getLogger() {
      return logger;
   }

   public HttpHeaders getHeaders() {
      return headers;
   }

   public UriInfo getUriInfo() {
      return uriInfo;
   }

   @Override
   public final Response toResponse(T ex) {
      OseeWebApplicationException exception = asWebAppException(ex);
      return writeResponse(exception);
   }

   protected abstract OseeWebApplicationException asWebAppException(T ex);

   protected Response writeResponse(OseeWebApplicationException exception) {
      final ErrorResponse errorResponse = exception.getErrorResponse();
      Response toReturn = exception.getResponse();
      List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
      if (JaxRsUtils.isHtmlSupported(acceptableMediaTypes)) {
         StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
               Writer writer = new OutputStreamWriter(output, JaxRsUtils.UTF_8_ENCODING);
               try {
                  ErrorResponseMessageBodyWriter.writeErrorHtml(errorResponse, writer);
               } finally {
                  writer.flush();
               }
            }
         };
         toReturn = Response.fromResponse(toReturn).entity(output).type(MediaType.TEXT_HTML_TYPE).build();
      }
      return toReturn;
   }
}