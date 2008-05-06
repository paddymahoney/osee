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
package org.eclipse.osee.framework.ui.skynet.widgets;

import java.sql.SQLException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class XFloatDam extends XFloat implements IDamWidget {

   private Artifact artifact;
   private String attrName;

   /**
    * @param displayLabel
    */
   public XFloatDam(String displayLabel) {
      super(displayLabel);
   }

   /**
    * @param displayLabel
    * @param xmlRoot
    */
   public XFloatDam(String displayLabel, String xmlRoot) {
      super(displayLabel, xmlRoot);
   }

   private Attribute<Double> getAttribute() throws Exception {
      return artifact.getAttributeManager(attrName).getSoleAttribute();
   }

   public void setArtifact(Artifact artifact, String attrName) throws SQLException {
      this.artifact = artifact;
      this.attrName = attrName;

      super.set(getUdatStringValue());
   }

   @Override
   public void set(String text) {
      super.set(text);
      try {
         getAttribute().setValue(new Double(text));
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   XModifiedListener modifyListener = new XModifiedListener() {
      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener#widgetModified(org.eclipse.osee.framework.ui.skynet.widgets.XWidget)
       */
      public void widgetModified(XWidget widget) {
         try {
            save();
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         }
      }
   };

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.XText#createWidgets(org.eclipse.swt.widgets.Composite, int, boolean)
    */
   @Override
   public void createWidgets(Composite parent, int horizontalSpan, boolean fillText) {
      super.createWidgets(parent, horizontalSpan, fillText);
      super.addXModifiedListener(modifyListener);
   }

   public String getUdatStringValue() throws SQLException {
      String toReturn = null;
      try {
         toReturn = getAttribute().getValue().toString();
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
      return toReturn != null ? toReturn : "";
   }

   @Override
   public boolean isDirty() throws SQLException {
      return (!getUdatStringValue().equals(get()));
   }

   @Override
   public void save() throws Exception {
      if (isDirty()) {
         getAttribute().setValue(new Double(get()));
      }
   }

}
