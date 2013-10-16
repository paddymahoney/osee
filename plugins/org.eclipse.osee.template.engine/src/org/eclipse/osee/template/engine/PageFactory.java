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

package org.eclipse.osee.template.engine;

import org.eclipse.osee.framework.core.model.ResourceToken;
import org.eclipse.osee.framework.core.services.IResourceRegistry;

/**
 * Factory containing convenience methods for both creating HtmlPageCreator objects and directly realizing a page in a
 * single call
 * 
 * @author Ryan D. Brooks
 */
public final class PageFactory {

   public static HtmlPageCreator newHtmlPageCreator(IResourceRegistry registry, String... keyValues) {
      HtmlPageCreator page = new HtmlPageCreator(registry);
      page.addKeyValuePairs(keyValues);
      return page;
   }

   public static HtmlPageCreator newHtmlPageCreator(IResourceRegistry registry, ResourceToken valuesResource, String... keyValues) throws Exception {
      HtmlPageCreator page = newHtmlPageCreator(registry, keyValues);
      page.readKeyValuePairs(valuesResource);
      return page;
   }

   public static HtmlPageCreator newHtmlPageCreator(IResourceRegistry registry, Iterable<String> keyValues) {
      HtmlPageCreator page = new HtmlPageCreator(registry);
      page.addKeyValuePairs(keyValues);
      return page;
   }

   public static HtmlPageCreator newHtmlPageCreator(IResourceRegistry registry, ResourceToken valuesResource, Iterable<String> keyValues) throws Exception {
      HtmlPageCreator page = newHtmlPageCreator(registry, keyValues);
      page.readKeyValuePairs(valuesResource);
      return page;
   }

   public static String realizePage(IResourceRegistry registry, ResourceToken templateResource, String... keyValues) throws Exception {
      HtmlPageCreator page = newHtmlPageCreator(registry, keyValues);
      return page.realizePage(templateResource);
   }

   public static String realizePage(IResourceRegistry registry, ResourceToken templateResource, ResourceToken valuesResource, String... keyValues) throws Exception {
      HtmlPageCreator page = newHtmlPageCreator(registry, valuesResource, keyValues);
      return page.realizePage(templateResource);
   }

   public static String realizePage(IResourceRegistry registry, ResourceToken templateResource, Iterable<String> keyValues) throws Exception {
      HtmlPageCreator page = newHtmlPageCreator(registry, keyValues);
      return page.realizePage(templateResource);
   }

   public static String realizePage(IResourceRegistry registry, ResourceToken templateResource, ResourceToken valuesResource, Iterable<String> keyValues) throws Exception {
      HtmlPageCreator page = newHtmlPageCreator(registry, valuesResource, keyValues);
      return page.realizePage(templateResource);
   }
}