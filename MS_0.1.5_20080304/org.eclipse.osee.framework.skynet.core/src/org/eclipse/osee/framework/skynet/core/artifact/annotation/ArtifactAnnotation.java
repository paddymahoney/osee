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
package org.eclipse.osee.framework.skynet.core.artifact.annotation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public class ArtifactAnnotation {

   private Type type;
   private String namespace;
   private String content;
   public enum Type {
      None(null, null),
      Info("info2.gif", null),
      Warning("warn.gif", "alert_8_8.gif"),
      Error("errorRound.gif", "error.gif"),
      Hidden(null, null);
      private String imageName;
      private String imageOverlayName;
      private static Type[] orderedTypes = new Type[] {Error, Warning, Info, Hidden, None};

      private Type(String imageName, String imageOverlayName) {
         this.imageName = imageName;
         this.imageOverlayName = imageOverlayName;
      }

      public static Type[] getOrderedTypes() {
         return orderedTypes;
      }

      /**
       * @return the largeImage
       */
      public Image getImage() {
         if (imageName == null) return null;
         return SkynetActivator.getInstance().getImage(imageName);
      }

      /**
       * @return the imageOverlayDescriptor
       */
      public ImageDescriptor getImageOverlayDescriptor() {
         if (imageOverlayName == null) return null;
         return SkynetActivator.getInstance().getImageDescriptor(imageOverlayName);
      }
   };

   public ArtifactAnnotation(Type type, String namespace, String message) {
      this.type = type;
      this.content = message;
      this.namespace = namespace;
   }

   public ArtifactAnnotation(String xml) {
      fromXml(xml);
   }

   public static ArtifactAnnotation getError(String namespace, String message) {
      return new ArtifactAnnotation(Type.Error, namespace, message);
   }

   public static ArtifactAnnotation getInfo(String namespace, String message) {
      return new ArtifactAnnotation(Type.Info, namespace, message);
   }

   public static ArtifactAnnotation getHidden(String namespace, String message) {
      return new ArtifactAnnotation(Type.Hidden, namespace, message);
   }

   public static ArtifactAnnotation getWarning(String namespace, String message) {
      return new ArtifactAnnotation(Type.Warning, namespace, message);
   }

   private static String TYPE_TAG = "type";
   private static String NAMESPACE_TAG = "namespace";
   private static String CONTENT_TAG = "content";

   public String toXml() {
      return AXml.addTagData(TYPE_TAG, type.name()) + AXml.addTagData(NAMESPACE_TAG, namespace) + AXml.addTagData(
            CONTENT_TAG, content);
   }

   public void fromXml(String xml) {
      String typeStr = AXml.getTagData(xml, TYPE_TAG);
      if (typeStr == null || typeStr.equals(""))
         type = Type.None;
      else
         type = Type.valueOf(typeStr);
      namespace = AXml.getTagData(xml, NAMESPACE_TAG);
      content = AXml.getTagData(xml, CONTENT_TAG);
   }

   /**
    * @return the type
    */
   public Type getType() {
      return type;
   }

   /**
    * @return the content
    */
   public String getContent() {
      return content;
   }

   /**
    * @return the namespace
    */
   public String getNamespace() {
      return namespace;
   }
}
