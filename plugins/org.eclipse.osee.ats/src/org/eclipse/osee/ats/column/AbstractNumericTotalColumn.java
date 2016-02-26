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
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IAltLeftClickProvider;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.util.LogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public abstract class AbstractNumericTotalColumn extends XViewerAtsColumn implements IXViewerValueColumn, IAltLeftClickProvider {

   private final String calulationStr;
   private final IAttributeType pointsAttrType;

   public AbstractNumericTotalColumn(String id, String name, String description, String calulationStr, IAttributeType pointsAttrType) {
      super(id, name, 40, SWT.CENTER, false, SortDataType.Float, false, description);
      this.calulationStr = calulationStr;
      this.pointsAttrType = pointsAttrType;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (element instanceof AbstractWorkflowArtifact) {
         try {
            Result result = isPointsNumericValid(element);
            if (result.isFalse()) {
               return result.getText();
            }
            return AtsUtilCore.doubleToI18nString(getRemainingPoints(element));
         } catch (OseeCoreException ex) {
            LogUtil.getCellExceptionString(ex);
         }
      }
      return "";
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         AbstractWorkflowArtifact aba = null;
         if (treeItem.getData() instanceof AbstractWorkflowArtifact) {
            aba = (AbstractWorkflowArtifact) treeItem.getData();
         } else if (Artifacts.isOfType(treeItem.getData(),
            AtsArtifactTypes.Action) && ActionManager.getTeams(treeItem.getData()).size() == 1) {
            aba = ActionManager.getFirstTeam(treeItem.getData());
         }
         if (aba != null) {
            AWorkbench.popup("Calculated Field", getDescription() + "\n\n" + calulationStr);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   public Result isPointsNumericValid(Object object) throws OseeCoreException {
      if (object instanceof AbstractWorkflowArtifact) {
         AbstractWorkflowArtifact aba = (AbstractWorkflowArtifact) object;
         if (!aba.isAttributeTypeValid(pointsAttrType)) {
            return Result.TrueResult;
         }
         try {
            Double value = aba.getSoleAttributeValue(pointsAttrType, null);
            if (aba.isCancelled()) {
               return Result.TrueResult;
            }
            if (value == null) {
               return new Result(pointsAttrType.getName() + " is not set.");
            }
            return Result.TrueResult;
         } catch (Exception ex) {
            return new Result(
               ex.getClass().getName() + ": " + ex.getLocalizedMessage() + "\n\n" + Lib.exceptionToString(ex));
         }
      } else if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(object)) {
            if (!isPointsNumericValid(team).isFalse()) {
               return Result.FalseResult;
            }
         }
      }
      return Result.FalseResult;
   }

   private double getRemainingPoints(Object object) throws OseeCoreException {
      if (object instanceof AbstractWorkflowArtifact) {
         return getRemainPointsFromArtifact((IAtsWorkItem) object);
      } else if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         double hours = 0;
         // Add up points for all children
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(object)) {
            hours += getRemainingPoints(team);
         }
         return hours;
      }
      return 0;
   }

   private double getRemainPointsFromArtifact(IAtsWorkItem workItem) throws OseeCoreException {
      if (workItem.getStateMgr().getStateType().isCompletedOrCancelled()) {
         return 0;
      }
      double est = ((Artifact) workItem.getStoreObject()).getSoleAttributeValue(pointsAttrType, 0.0);
      if (est > 0) {
         int percentComplete = getPercentComplete(workItem);
         est = est - est * percentComplete / 100.0;
      }
      return est;
   }

   abstract protected int getPercentComplete(IAtsWorkItem workItem);

}