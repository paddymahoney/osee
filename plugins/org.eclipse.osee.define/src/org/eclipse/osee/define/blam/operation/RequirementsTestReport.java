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
package org.eclipse.osee.define.blam.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage.Manipulations;

public class RequirementsTestReport extends AbstractBlam {
   private static final String MISSING = "?";
   private static final String EMPTY = "&nbsp;";
   private static final String SPACE = " ";
   private String[] previousCells = {MISSING, MISSING, MISSING, MISSING};
   private final String[] columnHeaders = {"Requirement", "Test Procedure(s)", "Test Status", "Result File(s)"};
   private Collection<Artifact> inputArtifacts;
   private Collection<Artifact> requirementsBulkLoad;
   private StringBuilder report;

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      init(variableMap);

      for (Artifact input : inputArtifacts) {
         processArtifacts(input);
      }

      report();
   }

   private void processArtifacts(Artifact node) throws OseeCoreException, IOException {
      Collection<Artifact> children = node.getChildren();

      if (isRequirement(node)) {
         processRequirement(node);
      } else {
         reportLine(getReqCellOutput(node), "N/A (" + node.getArtifactTypeName() + ")", EMPTY, EMPTY);
      }
      for (Artifact child : children) {
         processArtifacts(child);
      }
   }

   private void processRequirement(Artifact req) throws OseeCoreException {
      Collection<Artifact> testProcs = req.getRelatedArtifacts(CoreRelationTypes.Verification__Verifier);
      if (testProcs.isEmpty()) {
         reportLine(getReqCellOutput(req), MISSING, MISSING, MISSING);
      } else {
         for (Artifact testProc : testProcs) {
            processTestProcedure(req, testProc);
         }
      }
   }

   private void processTestProcedure(Artifact req, Artifact testProc) throws OseeCoreException {
      String testStatus;
      if (testProc.isAttributeTypeValid(CoreAttributeTypes.TestProcedureStatus)) {
         testStatus = testProc.getSoleAttributeValue(CoreAttributeTypes.TestProcedureStatus, MISSING);
      } else {
         testStatus = "N/A (" + testProc.getArtifactTypeName() + ")";
      }
      Collection<Artifact> resultFiles = testProc.getRelatedArtifacts(CoreRelationTypes.Test_Unit_Result__Test_Result);

      if (resultFiles.isEmpty()) {
         reportLine(getReqCellOutput(req), testProc.getName(), testStatus, MISSING);
      } else {
         for (Artifact resultFile : resultFiles) {
            reportLine(getReqCellOutput(req), testProc.getName(), testStatus, resultFile.getName());
         }
      }
   }

   private String getReqCellOutput(Artifact req) throws OseeCoreException {
      String paragraphNumber = req.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, "");
      String reqName = req.getName();
      String returnValue = paragraphNumber + SPACE + reqName;
      return returnValue;
   }

   private boolean isRequirement(Artifact src) {
      ArtifactType temp = src.getArtifactType();
      if (temp.inheritsFrom(CoreArtifactTypes.Requirement)) {
         return true;
      }

      return false;
   }

   private void reportLine(String... cells) {
      String[] outputCells = new String[4];
      for (int i = 0; i < cells.length; i++) {
         if (previousCells[i].equals(cells[i])) {
            if (i == 0 || outputCells[i - 1].equals(" ")) {
               outputCells[i] = " ";
            } else {
               outputCells[i] = cells[i];
            }
         } else {
            outputCells[i] = cells[i];
         }
      }
      previousCells = cells.clone();
      report.append(AHTML.addRowMultiColumnTable(outputCells));
   }

   private void report() {
      report.append(AHTML.endMultiColumnTable());
      XResultData rd = new XResultData();
      rd.addRaw(report.toString());
      XResultDataUI.report(rd, "Requirements Test Report", Manipulations.RAW_HTML);
   }

   private void init(VariableMap variableMap) throws OseeCoreException {
      inputArtifacts = variableMap.getArtifacts("artifacts");
      initReport();
      load();
   }

   private void initReport() {
      report = new StringBuilder(AHTML.beginMultiColumnTable(100, 1));
      report.append(AHTML.addHeaderRowMultiColumnTable(columnHeaders));
   }

   private void load() throws OseeCoreException {
      requirementsBulkLoad = new ArrayList<>();
      for (Artifact input : inputArtifacts) {
         requirementsBulkLoad.addAll(input.getDescendants());
      }
      Collection<Artifact> temp =
         RelationManager.getRelatedArtifacts(requirementsBulkLoad, 1, CoreRelationTypes.Verification__Verifier);
      Collection<Artifact> temp2 =
         RelationManager.getRelatedArtifacts(temp, 1, CoreRelationTypes.Test_Unit_Result__Test_Result);
      logf("Bulk loaded %d test results", temp2.size());
   }

   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XListDropViewer\" displayName=\"artifacts\" /></xWidgets>";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Define");
   }

   @Override
   public String getName() {
      return "Requirements Test Report";
   }
}
