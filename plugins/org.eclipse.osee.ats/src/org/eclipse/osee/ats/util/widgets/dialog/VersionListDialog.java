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
package org.eclipse.osee.ats.util.widgets.dialog;

import java.util.Collection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.ats.artifact.VersionCommitConfigArtifact;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactNameReverseSorter;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class VersionListDialog extends org.eclipse.ui.dialogs.ListDialog {

   public VersionListDialog(String title, String message, Collection<Artifact> verArts) {
      super(Displays.getActiveShell());
      this.setTitle(title);
      this.setMessage(message);
      this.setContentProvider(new ArrayContentProvider() {
         @SuppressWarnings({"rawtypes", "unchecked"})
         @Override
         public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Collection) {
               Collection list = (Collection) inputElement;
               return list.toArray(new VersionCommitConfigArtifact[list.size()]);
            }
            return super.getElements(inputElement);
         }
      });
      setLabelProvider(new VersionArtifactLabelProvider());
      setInput(verArts);
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control c = super.createDialogArea(container);
      getTableViewer().setSorter(new ArtifactNameReverseSorter());
      return c;
   }

}
