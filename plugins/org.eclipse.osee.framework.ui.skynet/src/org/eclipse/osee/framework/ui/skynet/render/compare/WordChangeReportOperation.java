package org.eclipse.osee.framework.ui.skynet.render.compare;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.preferences.MsWordPreferencePage;
import org.eclipse.osee.framework.ui.skynet.render.FileRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RenderingUtil;
import org.eclipse.osee.framework.ui.skynet.render.VbaWordDiffGenerator;
import org.eclipse.osee.framework.ui.skynet.render.WordImageChecker;
import org.eclipse.osee.framework.ui.skynet.util.WordUiUtil;

public final class WordChangeReportOperation extends AbstractOperation {
   private final Collection<Pair<Artifact, Artifact>> artifactsToCompare;

   private final String reportDirName;
   private final boolean isSuppressWord;
   private final FileRenderer renderer;
   private final IAttributeType attributeType;

   private IFolder renderingFolder;

   public WordChangeReportOperation(Collection<Pair<Artifact, Artifact>> artifactsToCompare, FileRenderer renderer, String reportDirName, boolean isSuppressWord) {
      super("Word Change Report", SkynetGuiPlugin.PLUGIN_ID);
      this.renderer = renderer;
      this.artifactsToCompare = artifactsToCompare;

      this.isSuppressWord = isSuppressWord;
      this.reportDirName = Strings.isValid(reportDirName) ? reportDirName : GUID.create();

      this.attributeType = CoreAttributeTypes.WORD_TEMPLATE_CONTENT;
   }

   private IFolder getRenderingFolder(Branch branch, PresentationType presentationType) throws OseeCoreException {
      if (renderingFolder == null) {
         renderingFolder = RenderingUtil.getRenderFolder(branch, presentationType);
      }
      return renderingFolder;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      ArrayList<String> fileNames = new ArrayList<String>(artifactsToCompare.size());

      IFolder changeReportFolder = OseeData.getFolder(".diff/" + reportDirName);
      String baseFileStr = "c:/UserData";
      String localFileName = null;

      VbaWordDiffGenerator generator = new VbaWordDiffGenerator();
      generator.initialize(false, false);

      Set<Artifact> artifacts = new HashSet<Artifact>();
      for (Pair<Artifact, Artifact> entry : artifactsToCompare) {
         try {
            //Remove tracked changes and display image diffs
            Pair<String, Boolean> originalValue = null;
            Pair<String, Boolean> newAnnotationValue = null;
            Pair<String, Boolean> oldAnnotationValue = null;

            //Check for tracked changes
            artifacts.clear();
            artifacts.addAll(RenderingUtil.checkForTrackedChangesOn(entry.getFirst()));
            artifacts.addAll(RenderingUtil.checkForTrackedChangesOn(entry.getSecond()));

            if (!artifacts.isEmpty()) {
               continue;
            }

            Artifact baseArtifact = entry.getFirst();
            Artifact newerArtifact = entry.getSecond();

            if (baseArtifact == null && newerArtifact == null) {
               throw new OseeArgumentException("baseVersion and newerVersion can't both be null.");
            }

            Attribute<String> baseContent = getWordContent(baseArtifact, attributeType);
            Attribute<String> newerContent = getWordContent(newerArtifact, attributeType);

            if (!UserManager.getUser().getBooleanSetting(MsWordPreferencePage.IDENTFY_IMAGE_CHANGES)) {
               originalValue = WordImageChecker.checkForImageDiffs(baseContent, newerContent);
            }
            Branch branch = baseArtifact != null ? baseArtifact.getBranch() : newerArtifact.getBranch();

            IFile baseFile = renderFile(renderer, baseArtifact, branch);
            IFile newerFile = renderFile(renderer, newerArtifact, branch);

            WordImageChecker.restoreOriginalValue(baseContent,
                  oldAnnotationValue != null ? oldAnnotationValue : originalValue);

            WordImageChecker.restoreOriginalValue(newerContent, newAnnotationValue);

            baseFileStr = changeReportFolder.getLocation().toOSString();
            localFileName = baseFileStr + "/" + GUID.create() + ".xml";
            fileNames.add(localFileName);

            monitor.setTaskName("Adding to Diff Script: " + (newerArtifact == null ? "Unnamed Artifact" : newerArtifact.getName()));
            monitor.worked(1);

            checkForCancelledStatus(monitor);

            generator.addComparison(baseFile, newerFile, localFileName, false);
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }

      monitor.setTaskName("Running Diff Script");
      if (!baseFileStr.equals("c:/UserData")) {
         generator.finish(baseFileStr + "/compareDocs.vbs", !isSuppressWord);
      }
      // Let the user know that these artifacts had tracked changes on and we are not handling them
      // Also, list these artifacts in an artifact explorer
      if (!artifacts.isEmpty() && RenderingUtil.arePopupsAllowed()) {
         WordUiUtil.displayWarningMessageDialog("Diff Artifacts Warning",
               "Detected tracked changes for some artifacts. Please refer to the results HTML report.");
         WordUiUtil.displayTrackedChangesOnArtifacts(artifacts);
      }
   }

   //   private IFile renderFile(IProgressMonitor monitor, FileRenderer renderer, Artifact artifact, Branch branch, PresentationType presentationType) throws OseeCoreException {
   //      IFile toReturn = null;
   //      if (artifact != null) {
   //         if (presentationType == PresentationType.MERGE || presentationType == PresentationType.MERGE_EDIT) {
   //            toReturn = renderer.renderForMerge(monitor, artifact, presentationType);
   //         } else {
   //            toReturn = renderer.renderForDiff(monitor, artifact);
   //         }
   //      } else {
   //         toReturn = renderer.renderForDiff(monitor, branch);
   //      }
   //      return toReturn;
   //   }

   private Attribute<String> getWordContent(Artifact artifact, IAttributeType attributeType) throws OseeCoreException {
      Attribute<String> toReturn = null;
      if (artifact != null) {
         toReturn = artifact.getSoleAttribute(attributeType);
      }
      return toReturn;
   }

   private IFile renderFile(FileRenderer renderer, Artifact artifact, Branch branch) throws OseeCoreException {
      PresentationType presentationType = PresentationType.DIFF;
      String fileName = RenderingUtil.getFilenameFromArtifact(renderer, null, presentationType);
      InputStream inputStream = renderer.getRenderInputStream(artifact, presentationType);
      IFolder renderingFolder = getRenderingFolder(branch, presentationType);
      return renderer.renderToFile(renderingFolder, fileName, branch, inputStream, presentationType);
   }




}