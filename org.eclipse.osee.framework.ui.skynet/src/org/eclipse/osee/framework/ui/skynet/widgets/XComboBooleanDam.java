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
import org.eclipse.osee.framework.skynet.core.util.AttributeDoesNotExist;
import org.eclipse.osee.framework.skynet.core.util.MultipleAttributesExist;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.swt.widgets.Composite;

public class XComboBooleanDam extends XCombo implements IDamWidget {

   private Artifact artifact;
   private String attrName;

   public XComboBooleanDam(String displayLabel) {
      super(displayLabel);
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

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.ui.skynet.widgets.XCombo#createWidgets(org.eclipse.swt.widgets.Composite, int,
    *      java.lang.String[])
    */
   @Override
   public void createWidgets(Composite composite, int horizontalSpan, String[] inDataStrings) {
      super.createWidgets(composite, horizontalSpan, inDataStrings);
      super.addXModifiedListener(modifyListener);
   }

   public void createWidgets(Composite composite, int horizontalSpan) {
      super.createWidgets(composite, horizontalSpan);
      super.addXModifiedListener(modifyListener);
   }

   public void setArtifact(Artifact artifact, String attrName) throws IllegalStateException, SQLException, MultipleAttributesExist, AttributeDoesNotExist {
      this.artifact = artifact;
      this.attrName = attrName;
      Boolean result = artifact.getSoleAttributeValue(attrName, null);
      if (result == null)
         super.set("");
      else
         super.set(result ? "yes" : "no");
   }

   private Attribute<Boolean> getAttribute() throws Exception {
      return artifact.getSoleAttributeValue(attrName);
   }

   @Override
   public void set(String text) throws IllegalStateException, SQLException {
      try {
         if (text == null || text.equals("")) {
            super.set("");
            artifact.getAttributeManager(attrName).getSoleAttribute().delete();
         } else {
            super.set(text);
            getAttribute().setValue(new Boolean(text.equals("yes")));
         }
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   @Override
   public void save() throws Exception {
      if (isDirty()) {
         if (get() == null || get().equals("")) {
            artifact.getAttributeManager(attrName).getSoleAttribute().delete();
         } else {
            getAttribute().setValue(get().equals("yes"));
         }
      }
   }

   @Override
   public boolean isDirty() {
      try {
         Boolean result = artifact.getSoleAttributeValue(attrName, null);
         if (result == null && (get() != null || !get().equals("")))
            return true;
         else if ((get() != null || !get().equals("")) && result != null)
            return true;
         else
            return artifact.getSoleAttributeValue(attrName, false) != (get().equals("yes"));
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, false);
      }
      return false;
   }
}
