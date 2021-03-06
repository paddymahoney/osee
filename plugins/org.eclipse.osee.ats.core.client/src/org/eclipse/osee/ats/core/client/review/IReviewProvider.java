/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.review;

/**
 * @author Donald G. Dunne
 */
public interface IReviewProvider {

   /**
    * Notification that a review was created. This allows the extension to do necessary initial tasks after the review
    * workflow artifact is created. All changes made to review will be persisted after this call.
    */
   public void reviewCreated(AbstractReviewArtifact reviewArt);

}
