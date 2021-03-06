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
package org.eclipse.osee.ats.version;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.core.client.task.TaskArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.ChangeTypeUtil;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.internal.AtsClientService;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.results.ResultsEditor;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage.Manipulations;

/**
 * @author Donald G. Dunne
 */
public class VersionReportJob extends Job {

   protected final String title;
   protected final IAtsVersion verArt;

   public VersionReportJob(String title, IAtsVersion verArt) {
      super("Creating Release Report");
      this.title = title;
      this.verArt = verArt;
   }

   @Override
   public IStatus run(IProgressMonitor monitor) {
      try {
         final String html = getReleaseReportHtml(title + " - " + DateUtil.getMMDDYYHHMM(), verArt, monitor);
         ResultsEditor.open(new XResultPage(title, html, Manipulations.HTML_MANIPULATIONS));
      } catch (Exception ex) {
         return new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, ex.toString(), ex);
      }
      monitor.done();
      return Status.OK_STATUS;
   }

   public static String getReleaseReportHtml(String title, IAtsVersion verArt, IProgressMonitor monitor) throws OseeCoreException {
      if (verArt == null) {
         AWorkbench.popup("ERROR", "Must select product, config and version.");
         return null;
      }
      StringBuilder sb = new StringBuilder();
      sb.append(AHTML.heading(3, title + getReleasedString(verArt), verArt.getName()));
      Collection<TeamWorkFlowArtifact> targetedForTeamWorkflows =
         Collections.castAll(AtsClientService.get().getVersionService().getTargetedForTeamWorkflows(verArt));
      sb.append(getTeamWorkflowReport(targetedForTeamWorkflows, null, monitor));
      return sb.toString();
   }

   public static String getFullReleaseReport(IAtsTeamDefinition teamDef, IProgressMonitor monitor) throws OseeCoreException {
      // Sort by release date and estimated release date
      Map<String, IAtsVersion> dateToVerArt = new HashMap<>();
      for (IAtsVersion verArt : teamDef.getVersions()) {
         Date estRelDate = verArt.getEstimatedReleaseDate();
         Date relDate = verArt.getReleaseDate();
         if (relDate != null) {
            dateToVerArt.put(relDate.getTime() + verArt.getName(), verArt);
         } else if (estRelDate != null) {
            dateToVerArt.put(estRelDate.getTime() + verArt.getName(), verArt);
         } else {
            dateToVerArt.put("Un-Released - No Estimated Release " + verArt.getName(), verArt);
         }
      }
      String[] dateSort = dateToVerArt.keySet().toArray(new String[dateToVerArt.size()]);
      Arrays.sort(dateSort);
      // Create hyperlinks reverse sorted
      StringBuffer sb = new StringBuffer();
      sb.append(AHTML.heading(2, teamDef + " Releases"));
      sb.append(AHTML.bold("Report generated by OSEE ATS on " + DateUtil.getDateNow()) + AHTML.newline(2));
      for (int x = dateSort.length - 1; x >= 0; x--) {
         IAtsVersion verArt = dateToVerArt.get(dateSort[x]);
         Date estRelDate = verArt.getEstimatedReleaseDate();
         if (verArt.isNextVersion() || verArt.isReleased()) {
            sb.append(AHTML.getHyperlink("#" + verArt.getName(),
               verArt.getName() + VersionReportJob.getReleasedString(verArt)));
            sb.append(AHTML.newline());
         } else if (estRelDate != null) {
            sb.append(verArt.getName());
            sb.append(" - Un-Released - Estimated Release Date: ");
            sb.append(getDateString(estRelDate));
            sb.append(AHTML.newline());
         } else {
            sb.append(verArt.getName());
            sb.append(" - Un-Released - No Estimated Release Date");
            sb.append(AHTML.newline());
         }
      }
      sb.append(AHTML.addSpace(5));
      int x = 1;
      for (IAtsVersion verArt : teamDef.getVersions()) {
         if (monitor != null) {
            String str = "Processing version " + x++ + "/" + teamDef.getVersions().size();
            monitor.subTask(str);
         }
         if (verArt.isReleased() || verArt.isNextVersion()) {
            String html = VersionReportJob.getReleaseReportHtml(verArt.getName(), verArt, null);
            sb.append(html);
         }
      }
      return sb.toString();
   }

   private static String getDateString(Date date) {
      if (date != null) {
         return DateUtil.getMMDDYY(date);
      }
      return null;
   }

   public static String getReleasedString(IAtsVersion verArt) {
      String released = "";
      if (verArt.isReleased() && verArt.getReleaseDate() != null) {
         released = " - " + "Released: " + getDateString(verArt.getReleaseDate());
      }
      if (verArt.isNextVersion() && verArt.getEstimatedReleaseDate() != null) {
         released = " - " + "Next Release - Estimated Release Date: " + getDateString(verArt.getEstimatedReleaseDate());
      }
      return released;
   }

   public static String getTeamWorkflowReport(Collection<TeamWorkFlowArtifact> teamArts, Integer backgroundColor, IProgressMonitor monitor) throws OseeCoreException {
      StringBuilder sb = new StringBuilder();
      sb.append(AHTML.beginMultiColumnTable(100, 1, backgroundColor));
      sb.append(AHTML.addHeaderRowMultiColumnTable(new String[] {"Type", "Team", "Priority", "Change", "Title", "ID"}));
      int x = 1;
      Set<IAtsTeamDefinition> teamDefs = new HashSet<>();
      for (TeamWorkFlowArtifact team : teamArts) {
         teamDefs.add(team.getTeamDefinition());
      }
      for (IAtsTeamDefinition teamDef : teamDefs) {
         for (TeamWorkFlowArtifact team : teamArts) {
            if (team.getTeamDefinition().equals(teamDef)) {
               String str = "Processing team " + x++ + "/" + teamArts.size();
               if (monitor != null) {
                  monitor.subTask(str);
               }
               sb.append(AHTML.addRowMultiColumnTable(
                  new String[] {
                     "Action",
                     team.getTeamName(),
                     team.getSoleAttributeValue(AtsAttributeTypes.PriorityType, ""),
                     ChangeTypeUtil.getChangeTypeStr(team),
                     team.getName(),
                     team.getAtsId()},
                  null, x % 2 == 0 ? null : "#cccccc"));

               for (TaskArtifact taskArt : team.getTaskArtifacts()) {
                  sb.append(AHTML.addRowMultiColumnTable(
                     new String[] {"Task", "", "", "", taskArt.getName(), taskArt.getAtsId()}, null,
                     x % 2 == 0 ? null : "#cccccc"));
               }
            }
         }
      }
      sb.append(AHTML.endMultiColumnTable());
      return sb.toString();
   }

}
