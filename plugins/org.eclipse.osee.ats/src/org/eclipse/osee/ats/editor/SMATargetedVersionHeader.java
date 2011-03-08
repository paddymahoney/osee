/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.editor;

import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.column.TargetedVersionColumn;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.VersionLockedType;
import org.eclipse.osee.ats.util.VersionReleaseType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author Donald G. Dunne
 */
public class SMATargetedVersionHeader extends Composite {

   private final static String TARGET_VERSION = "Target Version:";
   Label valueLabel;
   Label origLabel;
   Hyperlink link;

   public SMATargetedVersionHeader(Composite parent, int style, final AbstractWorkflowArtifact sma, final SMAEditor editor) {
      super(parent, style);
      setLayoutData(new GridData());
      setLayout(ALayout.getZeroMarginLayout(2, false));
      editor.getToolkit().adapt(this);

      try {
         if (!sma.isCancelled() && !sma.isCompleted()) {
            link = editor.getToolkit().createHyperlink(this, TARGET_VERSION, SWT.NONE);
            link.addHyperlinkListener(new IHyperlinkListener() {

               @Override
               public void linkEntered(HyperlinkEvent e) {
                  // do nothing
               }

               @Override
               public void linkExited(HyperlinkEvent e) {
                  // do nothing
               }

               @Override
               public void linkActivated(HyperlinkEvent e) {
                  try {
                     if (TargetedVersionColumn.promptChangeVersion(sma,
                        AtsUtil.isAtsAdmin() ? VersionReleaseType.Both : VersionReleaseType.UnReleased,
                        VersionLockedType.UnLocked)) {
                        updateLabel(sma);
                        editor.onDirtied();
                     }
                  } catch (Exception ex) {
                     OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
                  }
               }
            });
         } else {
            origLabel = editor.getToolkit().createLabel(this, TARGET_VERSION);
            origLabel.setLayoutData(new GridData());
         }

         valueLabel = editor.getToolkit().createLabel(this, "Not Set");
         valueLabel.setLayoutData(new GridData());
         updateLabel(sma);

      } catch (OseeCoreException ex) {
         Label errorLabel = editor.getToolkit().createLabel(this, "Error: " + ex.getLocalizedMessage());
         errorLabel.setForeground(Displays.getSystemColor(SWT.COLOR_RED));
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }

   }

   private void updateLabel(AbstractWorkflowArtifact sma) throws OseeCoreException {
      String value = "Not Set";
      if (sma.getTargetedVersion() != null) {
         value = sma.getTargetedVersion().getName();
      }
      valueLabel.setText(value);
      valueLabel.getParent().layout();
   }

   @Override
   public void setBackground(Color color) {
      super.setBackground(color);
      if (Widgets.isAccessible(valueLabel)) {
         valueLabel.setBackground(color);
      }
      if (Widgets.isAccessible(origLabel)) {
         origLabel.setBackground(color);
      }
      if (Widgets.isAccessible(link)) {
         link.setBackground(color);
      }
   }

}
