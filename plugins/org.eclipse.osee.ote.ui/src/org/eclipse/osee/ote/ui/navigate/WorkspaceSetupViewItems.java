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
package org.eclipse.osee.ote.ui.navigate;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionPoints;
import org.eclipse.osee.framework.svn.CheckoutProjectSetJob;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.xnavigate.IXNavigateContainer;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.swt.KeyedImage;
import org.eclipse.osee.ote.ui.OteImage;
import org.eclipse.osee.ote.ui.internal.TestCoreGuiPlugin;
import org.osgi.framework.Bundle;

/**
 * @author Roberto E. Escobar
 */
public class WorkspaceSetupViewItems implements IXNavigateContainer {
   private static final String PARENT_FOLDER_NAME = "Workspace Operations";

   public WorkspaceSetupViewItems() {
      super();
   }

   @Override
   public List<XNavigateItem> getNavigateItems() {
      List<XNavigateItem> items = new ArrayList<XNavigateItem>();
      XNavigateItem parentFolder = new XNavigateItem(null, PARENT_FOLDER_NAME, PluginUiImage.FOLDER);
      workspaceSetupFactory(parentFolder);
      items.add(parentFolder);
      return items;
   }

   private void workspaceSetupFactory(XNavigateItem parent) {
      List<IConfigurationElement> configurationElements =
         ExtensionPoints.getExtensionElements("org.eclipse.osee.ote.ui.WorkspaceSetup", "WorkspaceConfig");
      for (IConfigurationElement configElement : configurationElements) {
         IExtension extension = (IExtension) configElement.getParent();
         String shortCutName =
            extension.getLabel() == null || extension.getLabel().length() == 0 ? extension.getSimpleIdentifier() : extension.getLabel();
         String resourceName = configElement.getAttribute("configFile");
         String bundleName = configElement.getContributor().getName();
         if (Strings.isValid(shortCutName) && Strings.isValid(bundleName) && Strings.isValid(resourceName)) {
            try {
               URL configPath = getWorkspaceConfig(bundleName, resourceName);
               if (configPath != null) {
                  new XNavigateItemRunnable(parent, shortCutName, OteImage.CHECKOUT, configPath);
               }
            } catch (Exception ex) {
               OseeLog.logf(TestCoreGuiPlugin.class, Level.WARNING, ex, "Unable to Load: [%s.%s - %s]", bundleName,
                  resourceName, OteImage.CHECKOUT);
            }
         }
      }
   }

   private URL getResource(String bundleName, String resourceName) throws IOException {
      Bundle bundle = Platform.getBundle(bundleName);
      URL url = bundle.getEntry(resourceName);
      url = FileLocator.resolve(url);
      return url;
   }

   private URL getWorkspaceConfig(String bundleName, String configName) throws IOException {
      return getResource(bundleName, configName);
   }

   private static final class XNavigateItemRunnable extends XNavigateItem {
      private final URL projectSetFile;
      private final String jobName;

      public XNavigateItemRunnable(XNavigateItem parent, String name, KeyedImage oseeImage, URL projectSetFile) {
         super(parent, name, oseeImage);
         this.jobName = String.format("Workspace Configuration: [%s]", name);
         this.projectSetFile = projectSetFile;
      }

      @Override
      public void run(TableLoadOption... tableLoadOptions) {
         Job job = new CheckoutProjectSetJob(jobName, getName(), projectSetFile);
         job.setUser(true);
         job.schedule();
      }
   }
}
