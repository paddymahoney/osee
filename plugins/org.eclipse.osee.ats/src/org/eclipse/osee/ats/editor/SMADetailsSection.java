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
package org.eclipse.osee.ats.editor;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.framework.core.data.AccessContextId;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.services.CmAccessControl;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Donald G. Dunne
 */
public class SMADetailsSection extends SectionPart {

   private FormText formText;
   private final SMAEditor editor;
   private boolean sectionCreated = false;

   public SMADetailsSection(SMAEditor editor, Composite parent, FormToolkit toolkit, int style) {
      super(parent, toolkit, style | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
      this.editor = editor;
   }

   @Override
   public void initialize(IManagedForm form) {
      super.initialize(form);
      Section section = getSection();
      section.setText("Details");
      section.setLayout(new GridLayout());
      section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      // Only load when users selects section
      section.addListener(SWT.Activate, new Listener() {

         @Override
         public void handleEvent(Event e) {
            createSection();
         }
      });
   }

   private synchronized void createSection() {
      if (!sectionCreated) {
         final FormToolkit toolkit = getManagedForm().getToolkit();
         Composite composite = toolkit.createComposite(getSection(), toolkit.getBorderStyle() | SWT.WRAP);
         composite.setLayout(new GridLayout());
         composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

         formText = toolkit.createFormText(composite, false);
         GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
         gd.widthHint = 200;
         formText.setLayoutData(gd);

         getSection().setClient(composite);
         toolkit.paintBordersFor(composite);
         sectionCreated = true;
      }

      if (Widgets.isAccessible(formText)) {
         AbstractWorkflowArtifact workflow = editor.getSma();

         try {
            Map<String, String> smaDetails = Artifacts.getDetailsKeyValues(workflow);
            addSMADetails(workflow, smaDetails);

            String formattedDetails = Artifacts.getDetailsFormText(smaDetails);
            formText.setText(formattedDetails, true, true);
         } catch (Exception ex) {
            formText.setText(Lib.exceptionToString(ex), false, false);
         }
         getManagedForm().reflow(true);
      }
   }

   private void addSMADetails(AbstractWorkflowArtifact workflow, Map<String, String> details) throws OseeCoreException {
      details.put("Workflow Definition", workflow.getWorkDefinition().getName());
      if (workflow.getParentActionArtifact() != null) {
         details.put("Action Id", workflow.getParentActionArtifact().getHumanReadableId());
      }
      if (!(workflow instanceof TeamWorkFlowArtifact) && workflow.getParentTeamWorkflow() != null) {
         details.put("Parent Team Workflow Id", workflow.getParentTeamWorkflow().getHumanReadableId());
      }
      if (workflow.isOfType(AtsArtifactTypes.TeamWorkflow)) {
         details.put("Working Branch Access Context Id", getAccessContextId((TeamWorkFlowArtifact) workflow));
      }
   }

   private String getAccessContextId(TeamWorkFlowArtifact workflow) {
      String message;
      CmAccessControl accessControl = workflow.getAccessControl();
      if (accessControl == null) {
         message = "AtsCmAccessControlService not found.";
      } else {
         Branch workingBranch = null;
         try {
            workingBranch = workflow.getWorkingBranch();
         } catch (Exception ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
         if (workingBranch == null) {
            message = "No working branch";
         } else {
            try {
               Collection<? extends AccessContextId> ids =
                  accessControl.getContextId(UserManager.getUser(), workingBranch);
               message = ids.toString();
            } catch (Exception ex) {
               OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
               message = String.format("Error getting context id [%s]", ex.getMessage());
            }
         }
      }
      return message;
   }

   @Override
   public void dispose() {
      if (Widgets.isAccessible(formText)) {
         formText.dispose();
      }
      super.dispose();
   }

}
