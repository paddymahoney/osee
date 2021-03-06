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
package org.eclipse.osee.framework.skynet.core.httpRequests;

import static org.eclipse.osee.framework.core.enums.SystemUser.OseeSystem;
import java.net.URI;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.jaxrs.client.JaxRsExceptions;
import org.eclipse.osee.orcs.rest.model.BranchEndpoint;
import org.eclipse.osee.orcs.rest.model.NewBranch;

/**
 * @author Andrew M. Finkbeiner
 * @author Ryan D. Brooks
 */
public final class CreateBranchHttpRequestOperation extends AbstractOperation {
   private final BranchType branchType;
   private final TransactionToken parentTransaction;
   private final String branchName;
   private final ArtifactId associatedArtifact;
   private final String creationComment;
   private final int mergeAddressingQueryId;
   private final BranchId destinationBranch;
   private IOseeBranch newBranch;
   private boolean txCopyBranchType;
   private final BranchId branch;

   public CreateBranchHttpRequestOperation(BranchType branchType, TransactionToken parentTransaction, IOseeBranch branch, ArtifactId associatedArtifact, String creationComment) {
      this(branchType, parentTransaction, branch, associatedArtifact, creationComment, -1, BranchId.SENTINEL);
   }

   public CreateBranchHttpRequestOperation(BranchType branchType, TransactionToken parentTransaction, IOseeBranch branch, ArtifactId associatedArtifact, String creationComment, int mergeAddressingQueryId, BranchId destinationBranch) {
      super("Create branch " + branch.getName(), Activator.PLUGIN_ID);
      this.branchType = branchType;
      this.parentTransaction = parentTransaction;
      this.branchName = branch.getName();
      this.branch = branch;
      this.associatedArtifact = associatedArtifact;
      this.creationComment = creationComment;
      this.mergeAddressingQueryId = mergeAddressingQueryId;
      this.destinationBranch = destinationBranch;
      this.txCopyBranchType = false;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws OseeCoreException {
      BranchEndpoint proxy = ServiceUtil.getOseeClient().getBranchEndpoint();

      NewBranch data = new NewBranch();
      data.setAssociatedArtifact(
         associatedArtifact != null && associatedArtifact.isValid() ? associatedArtifact : OseeSystem);
      data.setAuthor(UserManager.getUser());
      data.setBranchName(branchName);
      data.setBranchType(branchType);
      data.setCreationComment(creationComment);
      data.setMergeAddressingQueryId(mergeAddressingQueryId);
      data.setMergeDestinationBranchId(destinationBranch);
      data.setParentBranchId(parentTransaction.getBranch());
      data.setSourceTransactionId(parentTransaction);
      data.setTxCopyBranchType(isTxCopyBranchType());

      try {
         Response response = branch.isValid() ? proxy.createBranchWithId(branch, data) : proxy.createBranch(data);
         if (Status.CREATED.getStatusCode() == response.getStatus()) {
            newBranch = getBranchUuid(response); // can't use TokenFactory here because some places assume branch will be cached such as getBranchesByName
            OseeEventManager.kickBranchEvent(getClass(), new BranchEvent(BranchEventType.Added, newBranch));
         }
      } catch (Exception ex) {
         throw JaxRsExceptions.asOseeException(ex);
      }
   }

   private Branch getBranchUuid(Response response) {
      Long toReturn = -1L;
      if (response.hasEntity()) {
         org.eclipse.osee.orcs.rest.model.Branch branch =
            response.readEntity(org.eclipse.osee.orcs.rest.model.Branch.class);
         toReturn = branch.getBranchUuid();
      } else {
         URI location = response.getLocation();
         if (location != null) {
            String path = location.toASCIIString();
            int index = path.lastIndexOf("branches/");
            if (index > 0 && index < path.length()) {
               String value = path.substring(index);
               if (Strings.isNumeric(value)) {
                  toReturn = Long.parseLong(value);
               }
            }
         }
      }
      // can't use TokenFactory here because some places assume branch will be cached such as getBranchesByName
      return BranchManager.getBranch(toReturn);
   }

   public IOseeBranch getNewBranch() {
      return newBranch;
   }

   public boolean isTxCopyBranchType() {
      return txCopyBranchType;
   }

   public void setTxCopyBranchType(boolean value) {
      txCopyBranchType = value;
   }
}