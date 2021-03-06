/*******************************************************************************
 * Copyright (c) 2017 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.widgets;

import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Donald G. Dunne
 */
public abstract class XButtonWithLabelDam extends XButton implements IArtifactWidget {

   private Artifact artifact;
   protected Label resultsLabelWidget;

   public XButtonWithLabelDam(String displayLabel, String toolTip, Image image) {
      super(displayLabel);
      setImage(image);
      setToolTip(toolTip);
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      numColumns = 3;
      super.createControls(parent, horizontalSpan);
      resultsLabelWidget = new Label(bComp, SWT.NONE);
      resultsLabelWidget.setText(getResultsText());
   }

   protected abstract String getResultsText();

   @Override
   public void setArtifact(Artifact artifact) throws OseeCoreException {
      this.artifact = artifact;
   }

   @Override
   public Artifact getArtifact() throws OseeCoreException {
      return artifact;
   }

   @Override
   public Result isDirty() {
      return Result.FalseResult;
   }

   @Override
   public void revert() {
      // do nothing
   }

   @Override
   public void saveToArtifact() {
      // do nothing
   }

}
