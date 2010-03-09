package org.eclipse.osee.define.blam.operation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.importing.ReqNumbering;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

public class RequirementReorderOperation extends AbstractBlam {
   private SkynetTransaction transaction;

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Define");
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      List<Artifact> artifacts = variableMap.getArtifacts("artifacts");
      Branch branch = artifacts.get(0).getBranch();
      transaction = new SkynetTransaction(branch, "Fix Requirement Ordering BLAM");
      for (Artifact input : artifacts) {
         reorderChildren(input);
      }
      transaction.execute();
   }

   public void reorderChildren(Artifact parent) throws OseeCoreException {
      List<Artifact> oldChildren = parent.getChildren();
      List<Artifact> children = parent.getChildren();
      Collections.sort(children, new ParagraphComparator());
      if (!oldChildren.equals(children)) {
         parent.setRelationOrder(CoreRelationTypes.Default_Hierarchical__Child, children);
         parent.persist(transaction);
      }

      for (Artifact child : children) {
         reorderChildren(child);
      }
   }

   private class ParagraphComparator implements Comparator<Artifact> {
      @Override
      public int compare(Artifact o1, Artifact o2) {
         try {
            ReqNumbering n1 = new ReqNumbering((String) o1.getSoleAttributeValue(CoreAttributeTypes.PARAGRAPH_NUMBER));
            ReqNumbering n2 = new ReqNumbering((String) o2.getSoleAttributeValue(CoreAttributeTypes.PARAGRAPH_NUMBER));
            return n1.compareTo(n2);
         } catch (OseeCoreException e) {
            return 0;
         }
      }
   }
   
   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XListDropViewer\" displayName=\"artifacts\" /></xWidgets>";
   }

   @Override
   public String getName() {
      return "Fix Requirement Ordering";
   }

}
