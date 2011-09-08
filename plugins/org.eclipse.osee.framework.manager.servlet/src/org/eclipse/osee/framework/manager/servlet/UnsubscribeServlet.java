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
package org.eclipse.osee.framework.manager.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.osee.framework.core.operation.LogProgressMonitor;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.server.UnsecuredOseeHttpServlet;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.manager.servlet.internal.Activator;
import org.osgi.framework.BundleContext;

/**
 * @author Roberto E. Escobar
 */
public class UnsubscribeServlet extends UnsecuredOseeHttpServlet {

   private static final long serialVersionUID = -263648072167664572L;
   private final IOseeDatabaseService databaseService;
   private final IOseeCachingService cacheService;
   private final BundleContext bundleContext;
   private final IdentityService identityService;

   public UnsubscribeServlet(BundleContext bundleContext, IOseeDatabaseService databaseService, IOseeCachingService cacheService, IdentityService identityService) {
      this.databaseService = databaseService;
      this.cacheService = cacheService;
      this.bundleContext = bundleContext;
      this.identityService = identityService;
   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      try {
         String requestUri = request.getRequestURL().toString();
         requestUri = requestUri.replace(request.getPathInfo(), "");
         UnsubscribeRequest data = UnsubscribeRequest.createFromURI(request);

         String page = createConfirmationPage(requestUri, data);
         response.setStatus(HttpServletResponse.SC_OK);
         response.setContentType("text/html");
         response.setContentLength(page.length());
         response.getWriter().append(page);
      } catch (Exception ex) {
         handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during unsubscribe page creation",
            ex);
      }
   }

   private void handleError(HttpServletResponse response, int status, String message, Throwable ex) throws IOException {
      response.setStatus(status);
      response.setContentType("text/plain");
      OseeLog.log(Activator.class, Level.SEVERE, message, ex);
      response.getWriter().write(ex.toString());
   }

   private String createConfirmationPage(String uri, UnsubscribeRequest data) throws IOException {
      URL url = bundleContext.getBundle().getResource("templates/unsubscribeTemplate.html");
      InputStream inputStream = null;
      try {
         inputStream = url.openStream();
         String template = Lib.inputStreamToString(inputStream);
         return String.format(template, uri, data.getGroupId(), data.getUserId());
      } finally {
         Lib.close(inputStream);
      }
   }

   @Override
   protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
      try {
         UnsubscribeRequest data = UnsubscribeRequest.createFromXML(request);
         UnsubscribeTransaction del = new UnsubscribeTransaction(databaseService, cacheService, data, identityService);
         Operations.executeWorkAndCheckStatus(del, new LogProgressMonitor());
         response.setStatus(HttpServletResponse.SC_OK);
         response.setContentType("text/plain");
         String message = del.getCompletionMessage();
         response.setContentLength(message.length());
         response.getWriter().append(message);
      } catch (Exception ex) {
         handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error unsubscribing", ex);
      }
   }
}