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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author Donald G. Dunne
 */
public class XCombo extends XWidget {

   private Combo dataCombo;
   private Composite parent;
   protected String data = "";
   protected String[] inDataStrings; // Strings sent in for display
   //
   private final Map<String, Integer> displayDataStrings = new HashMap<String, Integer>();
   protected Map<String, String> dataStringToXmlString;
   private String displayArray[];
   private boolean isDefaultSelectionAllowed;

   public XCombo(String displayLabel, String xmlRoot, String xmlSubRoot) {
      super(displayLabel, xmlRoot, xmlSubRoot);
      isDefaultSelectionAllowed = true;
   }

   public XCombo(String displayLabel, String xmlRoot) {
      this(displayLabel, xmlRoot, "");
   }

   public XCombo(String displayLabel) {
      this(displayLabel, "", "");
   }

   public void setDefaultSelectionAllowed(boolean isAllowed) {
      this.isDefaultSelectionAllowed = isAllowed;
   }

   public boolean isDefaultSelectionAllowed() {
      return isDefaultSelectionAllowed;
   }

   @Override
   public Control getControl() {
      return dataCombo;
   }

   @Override
   public void setEditable(boolean editable) {
      super.setEditable(editable);
      if (getControl() != null && !getControl().isDisposed()) {
         getControl().setEnabled(editable);
      }
   }

   public void setEnabled(boolean enabled) {
      dataCombo.setEnabled(enabled);
   }

   public void createWidgets(Composite composite, int horizontalSpan, String inDataStrings[]) {
      this.inDataStrings = inDataStrings;
      createWidgets(composite, horizontalSpan);
   }

   /**
    * Create Data Widgets. Widgets Created: Data: "--select--" horizonatalSpan takes up 2 columns; horizontalSpan must
    * be >=2 the string "--select--" will be added to the sent in dataStrings array
    */
   @Override
   protected void createControls(Composite parent, int horizontalSpan) {

      GridData gd;
      this.parent = parent;

      if (inDataStrings == null) {
         inDataStrings = new String[] {"DATA NOT FOUND"};
      }
      setDisplayDataStrings();

      if (horizontalSpan < 2) {
         horizontalSpan = 2;
      }

      // Create Data Widgets
      if (isDisplayLabel() && !getLabel().equals("")) {
         labelWidget = new Label(parent, SWT.NONE);
         labelWidget.setText(getLabel() + ":");
         if (getToolTip() != null) {
            labelWidget.setToolTipText(getToolTip());
         }
      }

      dataCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.V_SCROLL);
      dataCombo.setItems(displayArray);
      dataCombo.setVisibleItemCount(Math.min(displayArray.length, 45));

      gd = new GridData();
      if (fillHorizontally) {
         gd.grabExcessHorizontalSpace = true;
      }
      if (fillVertically) {
         gd.grabExcessVerticalSpace = true;
      }
      gd.horizontalSpan = horizontalSpan - 1;
      dataCombo.setLayoutData(gd);
      ModifyListener dataComboListener = new ModifyListener() {

         @Override
         public void modifyText(ModifyEvent e) {
            data = dataCombo.getText();
            if (data.compareTo("--select--") == 0) {
               data = "";
            }
            validate();
            notifyXModifiedListeners();
         }
      };
      dataCombo.addModifyListener(dataComboListener);

      refresh();
      dataCombo.setEnabled(isEditable());
   }

   public int getDisplayPosition(String str) {
      for (int i = 0; i < displayArray.length; i++) {
         if (str.equals(displayArray[i])) {
            return i;
         }
      }
      return 0;
   }

   public int getDisplayPosition() {
      for (int i = 0; i < displayArray.length; i++) {
         if (data.equals(displayArray[i])) {
            return i;
         }
      }
      return 0;
   }

   public void setDataStrings(String[] inDataStrings) {
      this.inDataStrings = inDataStrings;
      setDisplayDataStrings();
      if (dataCombo != null && !dataCombo.isDisposed()) {
         dataCombo.setItems(displayArray);
         if (displayArray.length < 15) {
            dataCombo.setVisibleItemCount(displayArray.length);
         }
      }
      updateComboWidget();
   }

   /**
    * Given the inDataStrings, create the mapping of all data strings including "--select--" and map them to their index
    * in the combo list.
    */
   private void setDisplayDataStrings() {
      displayDataStrings.clear();

      if (isDefaultSelectionAllowed()) {
         displayDataStrings.put("--select--", 0);
         displayArray = new String[inDataStrings.length + 1];
         displayArray[0] = "--select--";
         for (int i = 0; i < inDataStrings.length; i++) {
            displayDataStrings.put(inDataStrings[i], (i + 1));
            displayArray[i + 1] = inDataStrings[i];
         }
      } else {
         displayArray = new String[inDataStrings.length];
         for (int i = 0; i < inDataStrings.length; i++) {
            displayDataStrings.put(inDataStrings[i], i);
            displayArray[i] = inDataStrings[i];
         }
      }
   }

   @Override
   public void setFocus() {
      if (dataCombo != null) {
         dataCombo.setFocus();
      }
   }

   public void setDataStringToXmlTranslations(Map<String, String> dataStringToXmlString) {
      this.dataStringToXmlString = dataStringToXmlString;
   }

   @SuppressWarnings("unchecked")
   @Override
   public void setFromXml(String xml) throws IllegalStateException {
      Matcher m;
      if (getXmlSubRoot().equals("")) {
         m =
            Pattern.compile("<" + getXmlRoot() + ">(.*?)</" + getXmlRoot() + ">", Pattern.MULTILINE | Pattern.DOTALL).matcher(
               xml);
      } else {
         m =
            Pattern.compile(
               "<" + getXmlRoot() + "><" + getXmlSubRoot() + ">(.*?)</" + getXmlSubRoot() + "></" + getXmlRoot() + ">",
               Pattern.MULTILINE | Pattern.DOTALL).matcher(xml);
      }
      while (m.find()) {
         String str = m.group(1);
         String transStr = null;
         // If translation given, translate back to display string
         // ie. bems number => full name
         if (dataStringToXmlString != null) {
            if (dataStringToXmlString.containsValue(str)) {
               for (Iterator<?> iter = dataStringToXmlString.entrySet().iterator(); iter.hasNext();) {
                  Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                  if (str.equals(entry.getValue())) {
                     transStr = entry.getKey();
                     break;
                  }
               }
            }
         }
         if (transStr != null) {
            set(transStr);
         } else {
            set(str);
         }
         break;
      }
      refresh();
   }

   @Override
   public void refresh() {
      updateComboWidget();
   }

   public void addModifyListener(ModifyListener modifyListener) {
      if (dataCombo != null) {
         dataCombo.addModifyListener(modifyListener);
      }
   }

   public Combo getComboBox() {
      return dataCombo;
   }

   /**
    * @return selected display value (eg. Donald Dunne)
    */
   public String get() {
      return data;
   }

   /**
    * @return returns translated xml value (eg. 727536)
    */
   public String getXml() {
      String s = "";
      if (dataStringToXmlString == null) {
         s = data;
      } else {
         s = dataStringToXmlString.get(data);
         if (s == null) {
            s = data;
         }
      }
      return s;
   }

   @Override
   public String getReportData() {
      return data;
   }

   @Override
   public String getXmlData() {
      return getReportData();
   }

   @Override
   public void setXmlData(String str) {
      // do nothing
   }

   private void updateComboWidget() {
      if (dataCombo != null && !dataCombo.isDisposed()) {
         if (displayDataStrings.containsKey(data)) {
            if (data.compareTo("") == 0) {
               dataCombo.select(0);
            } else {
               Integer pos = displayDataStrings.get(data);
               dataCombo.select(pos.intValue());
            }
         } else if (data.compareTo("") != 0) {
            // if not found, add it and select it
            displayDataStrings.put(data, displayDataStrings.size());
            dataCombo.add(data);
            dataCombo.select(displayDataStrings.size() - 1);
         } else {
            dataCombo.select(0);
         }
         if (displayDataStrings.size() < 15) {
            dataCombo.setVisibleItemCount(displayDataStrings.size());
         } else {
            dataCombo.setVisibleItemCount(15);
         }
         dataCombo.getParent().layout(true);
      }
      validate();
   }

   public void set(String data) {
      this.data = data;
      updateComboWidget();
   }

   public void set(int pos) {
      if (displayArray.length > pos) {
         this.data = displayArray[pos];
         updateComboWidget();
      }
   }

   public void remove(String data) {
      displayDataStrings.remove(data);
      if (dataCombo.indexOf(data) >= 0) {
         dataCombo.remove(data);
      }
   }

   @Override
   public IStatus isValid() {
      if (isRequiredEntry() && data.equals("")) {
         return new Status(IStatus.ERROR, SkynetGuiPlugin.PLUGIN_ID, getLabel() + " must be selected.");
      }
      return Status.OK_STATUS;
   }

   @Override
   protected String toXml() {
      return toXml(getXmlRoot());
   }

   @Override
   protected String toXml(String xmlRoot) {
      String s;
      String dataStr = getXml();
      if (!Strings.isValid(getXmlSubRoot())) {
         s = "<" + xmlRoot + ">" + dataStr + "</" + xmlRoot + ">\n";
      } else {
         s = "<" + xmlRoot + "><" + getXmlSubRoot() + ">" + dataStr + "</" + getXmlSubRoot() + "></" + xmlRoot + ">\n";
      }
      return s;
   }

   @Override
   public String toHTML(String labelFont) {
      return AHTML.getLabelStr(labelFont, getLabel() + ": ") + data;
   }

   public static void copy(XCombo from, XCombo to) throws IllegalStateException {
      to.set(from.get());
   }

   @Override
   public void dispose() {
      if (labelWidget != null) {
         labelWidget.dispose();
      }
      if (dataCombo != null) {
         dataCombo.dispose();
      }
      if (labelWidget != null) {
         labelWidget.dispose();
      }
      if (parent != null && !parent.isDisposed()) {
         parent.layout();
      }
      super.dispose();
   }

   public String[] getDisplayArray() {
      return displayArray;
   }

   public String[] getInDataStrings() {
      return inDataStrings;
   }

   @Override
   public Object getData() {
      return dataCombo.getText();
   }
}