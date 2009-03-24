package org.eclipse.nebula.widgets.xviewer;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

   // The plug-in ID
   public static final String PLUGIN_ID = "org.eclipse.nebula.widgets.xviewer";

   // The shared instance
   private static Activator plugin;
   protected static final String imagePath = "images/";
   private ImageRegistry imageRegistry;

   /**
    * The constructor
    */
   public Activator() {
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
    */
   @Override
   public void start(BundleContext context) throws Exception {
      super.start(context);
      plugin = this;
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
    */
   @Override
   public void stop(BundleContext context) throws Exception {
      plugin = null;
      if (imageRegistry != null) {
         imageRegistry.dispose();
      }
      super.stop(context);
   }

   /**
    * Returns the shared instance
    * 
    * @return the shared instance
    */
   public static Activator getDefault() {
      return plugin;
   }

   public static Activator getInstance() {
      return plugin;
   }

}
