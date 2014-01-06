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
package org.eclipse.osee.ats.impl;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.notify.IAtsNotificationService;
import org.eclipse.osee.ats.api.notify.IAtsNotificationServiceProvider;
import org.eclipse.osee.ats.api.review.IAtsReviewServiceProvider;
import org.eclipse.osee.ats.api.team.IAtsConfigItemFactory;
import org.eclipse.osee.ats.api.team.IAtsConfigItemFactoryProvider;
import org.eclipse.osee.ats.api.team.IAtsWorkItemFactory;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.ats.api.util.IAtsStoreFactory;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionAdmin;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionServiceProvider;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workflow.IAtsBranchServiceProvider;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemServiceProvider;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogFactory;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateFactory;
import org.eclipse.osee.ats.core.config.IAtsConfigProvider;
import org.eclipse.osee.ats.impl.action.IWorkItemPage;
import org.eclipse.osee.ats.impl.internal.workitem.IArtifactProvider;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Donald G Dunne
 */
public interface IAtsServer extends IAtsConfigItemFactoryProvider, IAtsConfigProvider, IAtsNotificationServiceProvider, IAtsReviewServiceProvider, IAtsBranchServiceProvider, IAtsWorkItemServiceProvider, IAtsWorkDefinitionServiceProvider, IArtifactProvider {

   OrcsApi getOrcsApi() throws OseeCoreException;

   IAtsWorkItemFactory getWorkItemFactory() throws OseeCoreException;

   IAtsUserService getUserService() throws OseeCoreException;

   @Override
   ArtifactReadable getArtifact(IAtsObject atsObject) throws OseeCoreException;

   @Override
   IAtsNotificationService getNotifyService() throws OseeCoreException;

   IAtsWorkDefinitionAdmin getWorkDefAdmin();

   ArtifactReadable getArtifactByGuid(String guid);

   IAttributeResolver getAttributeResolver();

   IAtsStoreFactory getStoreFactory();

   @Override
   IAtsConfigItemFactory getConfigItemFactory();

   IAtsStateFactory getStateFactory();

   IAtsLogFactory getLogFactory();

   IWorkItemPage getWorkItemPage();

}