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

import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.util.AttributeDoesNotExist;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Donald G. Dunne
 */
public class XCheckBoxDam extends XCheckBox implements IArtifactWidget {

   private Artifact artifact;
   private String attributeTypeName;

   public XCheckBoxDam(String displayLabel) {
      super(displayLabel);
   }

   public void setArtifact(Artifact artifact, String attrName) throws Exception {
      this.artifact = artifact;
      this.attributeTypeName = attrName;
      try {
         Boolean value = artifact.getSoleAttributeValue(attributeTypeName);
         super.set(value);
      } catch (AttributeDoesNotExist ex) {
         super.set(false);
      }
   }

   @Override
   public void saveToArtifact() throws Exception {
      try {
         artifact.setSoleXAttributeValue(attributeTypeName, checkButton.getSelection());
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget#isDirty()
    */
   @Override
   public Result isDirty() throws Exception {
      try {
         Boolean enteredValue = checkButton.getSelection();
         Boolean storedValue = artifact.getSoleAttributeValue(attributeTypeName);
         if (enteredValue.booleanValue() != storedValue.booleanValue()) {
            return new Result(true, attributeTypeName + " is dirty");
         }
      } catch (AttributeDoesNotExist ex) {
         if (checkButton.getSelection()) return new Result(true, attributeTypeName + " is dirty");
      }
      return Result.FalseResult;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget#revert()
    */
   @Override
   public void revert() throws Exception {
      setArtifact(artifact, attributeTypeName);
   }

}
