/*
 * Created on Aug 12, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.serverCommit;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.core.operation.CompositeOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.commit.CommitDbOperation;
import org.eclipse.osee.framework.skynet.core.commit.CommitItem;
import org.eclipse.osee.framework.skynet.core.commit.ComputeNetChangeOperation;
import org.eclipse.osee.framework.skynet.core.commit.LoadChangeDataOperation;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Jeff C. Phillips
 */
public class CommitService implements ICommitService {

   @Override
   public void commitBranch(IProgressMonitor monitor, ConflictManagerExternal conflictManager, boolean archiveSourceBranch) throws OseeCoreException {
      Branch sourceBranch = conflictManager.getSourceBranch();
      Branch destinationBranch = conflictManager.getDestinationBranch();
      Branch mergeBranch = BranchManager.getMergeBranch(sourceBranch, destinationBranch);

      List<CommitItem> changes = new ArrayList<CommitItem>();

      List<IOperation> ops = new ArrayList<IOperation>();
      ops.add(new LoadChangeDataOperation(sourceBranch, destinationBranch, mergeBranch, changes));
      System.out.println("Commit change size: " + changes.size());
      ops.add(new ComputeNetChangeOperation(changes));
      System.out.println("Commit change size: " + changes.size());
      ops.add(new CommitDbOperation(sourceBranch, destinationBranch, mergeBranch, changes));

      String opName =
            String.format("Commit: [%s]->[%s]", sourceBranch.getShortName(), destinationBranch.getShortName());
      IOperation op = new CompositeOperation(opName, Activator.PLUGIN_ID, ops);
      Operations.executeWork(op, monitor, -1);
      try {
         Operations.checkForErrorStatus(op.getStatus());
      } catch (Exception ex) {
         if (ex instanceof OseeCoreException) {
            throw (OseeCoreException) ex;
         } else {
            throw new OseeWrappedException(ex);
         }
      }
      if (archiveSourceBranch) {
         sourceBranch.archive();
      }
   }
}
