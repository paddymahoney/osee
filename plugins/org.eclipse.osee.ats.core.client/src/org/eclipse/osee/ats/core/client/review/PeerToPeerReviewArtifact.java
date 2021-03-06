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
package org.eclipse.osee.ats.core.client.review;

import org.eclipse.osee.ats.api.review.IAtsPeerReviewRoleManager;
import org.eclipse.osee.ats.api.review.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.skynet.core.artifact.IATSStateMachineArtifact;

/**
 * @author Donald G. Dunne
 */
public class PeerToPeerReviewArtifact extends AbstractReviewArtifact implements IATSStateMachineArtifact, IAtsPeerToPeerReview {

   public PeerToPeerReviewArtifact(String guid, BranchId branch, ArtifactTypeId artifactType) {
      super(guid, branch, artifactType);
   }

   @Override
   public IAtsPeerReviewRoleManager getRoleManager() {
      return AtsClientService.get().getReviewService().createPeerReviewRoleManager(this);
   }
}