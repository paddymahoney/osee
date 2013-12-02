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
package org.eclipse.osee.ats.impl.internal;

import org.eclipse.osee.ats.api.IAtsObject;
import org.eclipse.osee.ats.api.notify.IAtsNotificationService;
import org.eclipse.osee.ats.api.review.IAtsReviewService;
import org.eclipse.osee.ats.api.team.IAtsWorkItemFactory;
import org.eclipse.osee.ats.api.user.IAtsUserService;
import org.eclipse.osee.ats.api.util.IAtsStoreFactory;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionAdmin;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workdef.IAttributeResolver;
import org.eclipse.osee.ats.api.workflow.IAtsBranchService;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.core.config.IAtsConfig;
import org.eclipse.osee.ats.core.workdef.AtsWorkDefinitionAdminImpl;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.ats.impl.internal.util.AtsArtifactConfigCache;
import org.eclipse.osee.ats.impl.internal.util.AtsAttributeResolverServiceImpl;
import org.eclipse.osee.ats.impl.internal.util.AtsBranchServiceImpl;
import org.eclipse.osee.ats.impl.internal.util.AtsNotificationServiceImpl;
import org.eclipse.osee.ats.impl.internal.util.AtsReviewServiceImpl;
import org.eclipse.osee.ats.impl.internal.util.AtsStoreFactoryImpl;
import org.eclipse.osee.ats.impl.internal.util.AtsUtilServer;
import org.eclipse.osee.ats.impl.internal.util.AtsWorkDefinitionCacheProvider;
import org.eclipse.osee.ats.impl.internal.util.TeamWorkflowProvider;
import org.eclipse.osee.ats.impl.internal.workitem.AtsWorkItemServiceImpl;
import org.eclipse.osee.ats.impl.internal.workitem.WorkItemFactory;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author Donald G Dunne
 */
public class AtsServerImpl implements IAtsServer {

   public static String PLUGIN_ID = "org.eclipse.osee.ats.rest";
   private static OrcsApi orcsApi;
   private static IAtsWorkItemFactory workItemFactory;
   private static AtsServerImpl instance;
   private static IAtsWorkDefinitionService workDefService;
   private static IAtsUserService userService;
   private static IAtsNotificationService notifyService;
   private IAtsWorkItemService workItemService;
   private IAtsBranchService branchService;
   private IAtsReviewService reviewService;
   private IAtsWorkDefinitionAdmin workDefAdmin;
   private AtsWorkDefinitionCacheProvider workDefCacheProvider;
   private TeamWorkflowProvider teamWorkflowProvider;
   private AtsAttributeResolverServiceImpl attributeResolverService;
   private IAtsConfig config;
   private static Boolean started = null;

   public static AtsServerImpl get() {
      checkStarted();
      return instance;
   }

   public void setOrcsApi(OrcsApi orcsApi) {
      AtsServerImpl.orcsApi = orcsApi;
   }

   public static void setWorkDefService(IAtsWorkDefinitionService workDefService) {
      AtsServerImpl.workDefService = workDefService;
   }

   public static void setUserService(IAtsUserService userService) {
      AtsServerImpl.userService = userService;
   }

   public void start() throws OseeCoreException {
      Conditions.checkNotNull(orcsApi, "OrcsApi");
      Conditions.checkNotNull(workDefService, "IAtsWorkDefinitionService");
      Conditions.checkNotNull(userService, "IAtsUserService");
      instance = this;
      workItemFactory = new WorkItemFactory();
      notifyService = new AtsNotificationServiceImpl();

      workItemService = new AtsWorkItemServiceImpl(workItemFactory, this);
      branchService = new AtsBranchServiceImpl(orcsApi);
      reviewService = new AtsReviewServiceImpl(this, workItemService);
      workDefCacheProvider = new AtsWorkDefinitionCacheProvider(workDefService);

      teamWorkflowProvider = new TeamWorkflowProvider();
      attributeResolverService = new AtsAttributeResolverServiceImpl();
      attributeResolverService.setOrcsApi(orcsApi);
      workDefAdmin =
         new AtsWorkDefinitionAdminImpl(workDefCacheProvider, workItemService, workDefService, teamWorkflowProvider,
            attributeResolverService);

      config = new AtsArtifactConfigCache(orcsApi);
      System.out.println("ATS - AtsServerImpl started");
      started = true;
   }

   private static void checkStarted() throws OseeStateException {
      if (started == null) {
         throw new OseeStateException("AtsServer did not start");
      }
   }

   @Override
   public OrcsApi getOrcsApi() throws OseeCoreException {
      checkStarted();
      return orcsApi;
   }

   @Override
   public IAtsWorkItemFactory getWorkItemFactory() throws OseeCoreException {
      checkStarted();
      return workItemFactory;
   }

   @Override
   public IAtsWorkDefinitionService getWorkDefService() throws OseeCoreException {
      checkStarted();
      return workDefService;
   }

   @Override
   public IAtsUserService getUserService() throws OseeCoreException {
      checkStarted();
      return userService;
   }

   @Override
   public ArtifactReadable getArtifact(IAtsObject atsObject) throws OseeCoreException {
      checkStarted();
      ArtifactReadable result = null;
      if (atsObject.getStoreObject() != null) {
         result = (ArtifactReadable) atsObject.getStoreObject();
      } else {
         result =
            orcsApi.getQueryFactory(null).fromBranch(AtsUtilServer.getAtsBranch()).andGuid(atsObject.getGuid()).getResults().getExactlyOne();
      }
      return result;
   }

   @Override
   public IAtsNotificationService getNotifyService() throws OseeCoreException {
      checkStarted();
      return notifyService;
   }

   @Override
   public IAtsWorkItemService getWorkItemService() throws OseeStateException {
      checkStarted();
      return workItemService;
   }

   @Override
   public IAtsBranchService getBranchService() {
      return branchService;
   }

   @Override
   public IAtsReviewService getReviewService() {
      return reviewService;
   }

   @Override
   public ArtifactReadable getArtifactByGuid(String guid) throws OseeCoreException {
      checkStarted();
      return orcsApi.getQueryFactory(null).fromBranch(AtsUtilServer.getAtsBranch()).andGuid(guid).getResults().getExactlyOne();
   }

   @SuppressWarnings("unchecked")
   public ArtifactReadable getCurrentUser() throws OseeCoreException {
      QueryFactory query = orcsApi.getQueryFactory(null);
      return query.fromBranch(CoreBranches.COMMON).andIds(SystemUser.OseeSystem).getResults().getExactlyOne();
   }

   @Override
   public IAtsWorkDefinitionAdmin getWorkDefAdmin() {
      return workDefAdmin;
   }

   @Override
   public IAttributeResolver getAttributeResolver() {
      return attributeResolverService;
   }

   @Override
   public IAtsConfig getAtsConfig() throws OseeStateException {
      checkStarted();
      return config;
   }

   @Override
   public IAtsStoreFactory getStoreFactory() {
      return new AtsStoreFactoryImpl();
   }

}