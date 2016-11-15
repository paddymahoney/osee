package org.eclipse.osee.ote.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osee.ote.core.OTETestCaseHandler.TestCaseAnnotated;
import org.eclipse.osee.ote.core.environment.TestEnvironmentInterface;
import org.eclipse.osee.ote.core.environment.interfaces.ITestEnvironmentAccessor;
import org.eclipse.osee.ote.core.model.IModelManager;
import org.eclipse.osee.ote.message.interfaces.IMessageManager;
import org.eclipse.osee.ote.message.interfaces.IMessageRequestor;

public class OTERuntimeAnnotations {
   
   private final Map<Class<? extends Annotation>, AnnotationHandler<?>> annotationHandlers = new HashMap<Class<? extends Annotation>, AnnotationHandler<?>>();
   private IMessageRequestor req;
   private List<TestCaseAnnotated> testCases;

   public OTERuntimeAnnotations(){
      testCases = new ArrayList<>();
      IMessageManager messageManager = ServiceUtility.getService(IMessageManager.class);
      if(messageManager != null){
         req = messageManager.createRequestor("Annotated_" + this.getClass().getName());
         annotationHandlers.put( MessageWriter.class, new MessageWriterHandler(req));
         annotationHandlers.put( MessageReader.class, new MessageReaderHandler(req));
      }
      IModelManager modelManager = ServiceUtility.getService(IModelManager.class);
      if(modelManager != null){
         annotationHandlers.put( OTEModel.class, new OTEModelHandler(modelManager));
      }
      annotationHandlers.put( Service.class, new ServiceHandler());
      if(this instanceof TestScript){
         TestEnvironmentInterface accessor = ServiceUtility.getService(TestEnvironmentInterface.class, 10000);
         annotationHandlers.put( OTETestCase.class, new OTETestCaseHandler((TestScript)this, testCases, (ITestEnvironmentAccessor)accessor));
      }
      try {
         initAnnotations(this);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
      annotationHandlers.clear();
   }
   
   private void initAnnotations(Object testClass) throws Exception {
      Class<?> clazz = testClass.getClass();
      while (clazz != Object.class) {
         scan(testClass, clazz);
         clazz = clazz.getSuperclass();
      }
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   private void scan(Object object, Class<?> clazz) throws Exception {
      
      //class level annotations
      for (Annotation annotation : clazz.getAnnotations()) {
         AnnotationHandler handler = annotationHandlers.get(annotation.annotationType());
         if (handler != null) {
            handler.process(annotation, object);
         }
      }
      
      //method level annotations
      Method[] methods = clazz.getMethods();
      for (Method method : methods) {
         for (Annotation annotation : method.getAnnotations()) {
            AnnotationHandler handler = annotationHandlers.get(annotation.annotationType());
            if (handler != null) {
               handler.process(annotation, object, method);
            }
         }
      }
      
      //field level annotations
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
         for (Annotation annotation : field.getAnnotations()) {
            AnnotationHandler handler = annotationHandlers.get(annotation.annotationType());
            if (handler != null) {
               handler.process(annotation, object, field);
            }
         }
      }
      
   }

   protected void dispose(){
      if(req != null){
         req.dispose();
      }
   }

   public List<TestCaseAnnotated> getAnnotatedTestCases() {
      Collections.sort(testCases, new Comparator<TestCaseAnnotated>() {
         @Override
         public int compare(TestCaseAnnotated o1, TestCaseAnnotated o2) {
            return o1.getOrder() - o2.getOrder();
         }
      });
      return testCases;
   }
   
}