/*
 * Created on May 27, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.plugin.core.util;

import java.util.HashMap;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @author b1529404
 */
public class ExportClassLoader extends ClassLoader {

   private final PackageAdmin packageAdmin;
   private final HashMap<String, Bundle> cache = new HashMap<String, Bundle>(1024);

   public ExportClassLoader(PackageAdmin packageAdmin) {
      super(ExportClassLoader.class.getClassLoader());
      this.packageAdmin = packageAdmin;
   }

   /* (non-Javadoc)
    * @see java.lang.ClassLoader#findClass(java.lang.String)
    */
   @Override
   protected Class<?> findClass(String name) throws ClassNotFoundException {
      try {
         final String pkg = name.substring(0, name.lastIndexOf('.'));
         Bundle cachedBundle = cache.get(pkg);
         if (cachedBundle != null) {
            return cachedBundle.loadClass(name);
         }
         ExportedPackage[] list = packageAdmin.getExportedPackages(pkg);
         if (list != null) {
            for (ExportedPackage ep : list) {
               final Bundle bundle = ep.getExportingBundle();
               final int state = bundle.getState();
               if (state == Bundle.INSTALLED) {
                  packageAdmin.resolveBundles(new Bundle[] {bundle});
               }
               if (state == Bundle.RESOLVED || state == Bundle.STARTING || state == Bundle.ACTIVE || state == Bundle.STOPPING) {
                  cache.put(pkg, bundle);
                  return bundle.loadClass(name);
               }
            }
         }
         throw new ClassNotFoundException("could not locate a class for " + name);
      } catch (Exception e) {
         throw new ClassNotFoundException("could not locate a class for " + name, e);
      }

   }

}
