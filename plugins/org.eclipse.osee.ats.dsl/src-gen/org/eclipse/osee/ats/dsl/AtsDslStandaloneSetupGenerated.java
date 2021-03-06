/*
 * generated by Xtext
 */
package org.eclipse.osee.ats.dsl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.ISetup;

/**
 * Generated from StandaloneSetup.xpt!
 */
@SuppressWarnings("all")
public class AtsDslStandaloneSetupGenerated implements ISetup {

   @Override
   public Injector createInjectorAndDoEMFRegistration() {
      org.eclipse.xtext.common.TerminalsStandaloneSetup.doSetup();

      Injector injector = createInjector();
      register(injector);
      return injector;
   }

   public Injector createInjector() {
      return Guice.createInjector(new org.eclipse.osee.ats.dsl.AtsDslRuntimeModule());
   }

   public void register(Injector injector) {
      if (!EPackage.Registry.INSTANCE.containsKey("http://www.eclipse.org/osee/ats/dsl/AtsDsl")) {
         EPackage.Registry.INSTANCE.put("http://www.eclipse.org/osee/ats/dsl/AtsDsl",
            org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage.eINSTANCE);
      }

      org.eclipse.xtext.resource.IResourceFactory resourceFactory =
         injector.getInstance(org.eclipse.xtext.resource.IResourceFactory.class);
      org.eclipse.xtext.resource.IResourceServiceProvider serviceProvider =
         injector.getInstance(org.eclipse.xtext.resource.IResourceServiceProvider.class);
      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ats", resourceFactory);
      org.eclipse.xtext.resource.IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put("ats",
         serviceProvider);

   }
}
