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
package org.eclipse.osee.framework.osee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.diff.metamodel.ComparisonSnapshot;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.osee.framework.OseeTypesStandaloneSetup;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.oseeTypes.OseeTypeModel;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import com.google.inject.Injector;

/**
 * @author Roberto E. Escobar
 */
public final class ModelUtil {

   private ModelUtil() {
   }

   private void loadDependencies(OseeTypeModel baseModel, List<OseeTypeModel> models) throws OseeCoreException, URISyntaxException {
      // This is commented out cause we're using a combined file.  Once combined files
      // are no longer generated, this should be uncommented.
      //      for (Import dependant : baseModel.getImports()) {
      //         OseeTypeModel childModel = OseeTypeModelUtil.loadModel(context, new URI(dependant.getImportURI()));
      //         loadDependencies(childModel, models);
      //         System.out.println("depends on: " + dependant.getImportURI());
      //      }
      //      System.out.println("Added on: " + baseModel.eResource().getURI());
      models.add(baseModel);

   }

   //   OseeTypeModel targetModel = null;
   //   try {
   //      targetModel = OseeTypeModelUtil.loadModel(context, resource);
   //   } catch (OseeCoreException ex) {
   //      throw new OseeWrappedException(String.format("Error loading: [%s]", resource), ex);
   //   }
   //   loadDependencies(targetModel, models);

   public static OseeTypeModel loadModel(String xTextData) throws OseeCoreException {
      try {
         return ModelUtil.loadModel(new ByteArrayInputStream(xTextData.getBytes("UTF-8")), false);
      } catch (UnsupportedEncodingException ex) {
         throw new OseeWrappedException(ex);
      }
   }

   public static OseeTypeModel loadModel(InputStream inputStream, boolean isZipped) throws OseeCoreException {
      Injector injector = new OseeTypesStandaloneSetup().createInjectorAndDoEMFRegistration();
      XtextResource resource = injector.getInstance(XtextResource.class);

      Map<String, Boolean> options = new HashMap<String, Boolean>();
      options.put(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
      if (isZipped) {
         options.put(XtextResource.OPTION_ZIP, Boolean.TRUE);
      }
      try {
         resource.setURI(URI.createURI("http://www.eclipse.org/osee/framework/OseeTypes"));
         resource.load(inputStream, options);
      } catch (IOException ex) {
         throw new OseeWrappedException(ex);
      }
      OseeTypeModel model = (OseeTypeModel) resource.getContents().get(0);
      for (Diagnostic diagnostic : resource.getErrors()) {
         throw new OseeStateException(diagnostic.toString());
      }
      return model;
   }

   public static OseeTypeModel loadModel(Object context, java.net.URI target) throws OseeCoreException {
      String uri = target.toASCIIString();
      Injector injector = new OseeTypesStandaloneSetup().createInjectorAndDoEMFRegistration();
      XtextResourceSet set = injector.getInstance(XtextResourceSet.class);

      set.setClasspathURIContext(context);
      set.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);

      Resource resource = set.getResource(URI.createURI(uri), true);
      OseeTypeModel model = (OseeTypeModel) resource.getContents().get(0);
      for (Diagnostic diagnostic : resource.getErrors()) {
         throw new OseeStateException(diagnostic.toString());
      }
      return model;
   }

   public static void saveModel(java.net.URI uri, OseeTypeModel model) throws IOException {
      OseeTypesStandaloneSetup.doSetup();

      ResourceSet resourceSet = new ResourceSetImpl();
      Resource resource = resourceSet.createResource(URI.createURI(uri.toASCIIString()));
      resource.getContents().add(model);

      Map<String, Boolean> options = new HashMap<String, Boolean>();
      options.put(XtextResource.OPTION_FORMAT, Boolean.TRUE);
      resource.save(options);
   }

   public static void saveModel(OseeTypeModel model, OutputStream outputStream, boolean isZipped) throws IOException {
      Injector injector = new OseeTypesStandaloneSetup().createInjectorAndDoEMFRegistration();
      XtextResource resource = injector.getInstance(XtextResource.class);

      Map<String, Boolean> options = new HashMap<String, Boolean>();
      options.put(XtextResource.OPTION_FORMAT, Boolean.TRUE);
      options.put(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
      if (isZipped) {
         options.put(XtextResource.OPTION_ZIP, Boolean.TRUE);
      }
      resource.getContents().add(model);
      resource.save(outputStream, options);
   }

   private static void storeModel(OutputStream outputStream, EObject object, Map<String, Boolean> options) throws OseeCoreException {
      Resource resource = new ResourceImpl();
      try {
         resource.save(outputStream, options);
      } catch (IOException ex) {
         throw new OseeWrappedException(ex);
      }
   }

   public static String modelToString(EObject object, Map<String, Boolean> options) throws OseeCoreException {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      storeModel(outputStream, object, options);
      try {
         return outputStream.toString("UTF-8");
      } catch (UnsupportedEncodingException ex) {
         throw new OseeWrappedException(ex);
      }
   }

   public static ComparisonSnapshot loadComparisonSnapshot(String compareData) {
      return null;
   }
}
