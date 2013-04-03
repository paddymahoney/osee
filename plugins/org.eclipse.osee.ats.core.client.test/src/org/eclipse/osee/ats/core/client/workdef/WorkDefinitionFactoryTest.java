/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.workdef;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.review.IAtsPeerToPeerReview;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinitionService;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionService;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinitionStore;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.IAtsWorkItemService;
import org.eclipse.osee.ats.core.client.workflow.ITeamWorkflowProvider;
import org.eclipse.osee.ats.core.workdef.WorkDefinitionMatch;
import org.eclipse.osee.framework.core.util.XResultData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test case for {@link WorkDefinitionFactory}
 * 
 * @author Donald G. Dunne
 */
public class WorkDefinitionFactoryTest {

   // @formatter:off
   @Mock IAtsTeamDefinition topTeamDef;
   @Mock IAtsTeamDefinition projTeamDef;
   @Mock IAtsTeamDefinition featureTeamDef;
   @Mock IAtsWorkItemService workItemService;
   @Mock IAtsWorkDefinitionService workDefinitionService;
   @Mock IAtsWorkDefinitionStore workDefinitionStore;
   @Mock IAtsTeamDefinitionService teamDefService;
   @Mock IAtsActionableItem actionableItem;
   @Mock IAtsPeerToPeerReview peerReview;
   @Mock XResultData resultData;
   @Mock IAtsTeamWorkflow teamWf;
   @Mock IAtsWorkDefinition defaultPeerToPeerWorkDef;
   @Mock IAtsWorkDefinition myPeerToPeerWorkDef;
   private static String MyPeerToPeerWorkDefId = "myPeerToPeerWorkDef";
   private WorkDefinitionFactory factory;

   // @formatter:on

   @Before
   public void setup() throws Exception {
      MockitoAnnotations.initMocks(this);
      when(topTeamDef.getParentTeamDef()).thenReturn(null);
      when(projTeamDef.getParentTeamDef()).thenReturn(topTeamDef);
      when(featureTeamDef.getParentTeamDef()).thenReturn(projTeamDef);
      // always return default when requested
      when(
         workDefinitionService.getWorkDef(eq(WorkDefinitionFactory.PeerToPeerDefaultWorkflowDefinitionId),
            any(XResultData.class))).thenReturn(defaultPeerToPeerWorkDef);
      // always return myPeerToPeerWorkDef when requested
      when(workDefinitionService.getWorkDef(eq(MyPeerToPeerWorkDefId), any(XResultData.class))).thenReturn(
         myPeerToPeerWorkDef);

      factory =
         new WorkDefinitionFactory(teamDefService, workItemService, workDefinitionService,
            new ArrayList<ITeamWorkflowProvider>());
   }

   @Test
   public void testGetDefaultPeerToPeerWorkflowDefinitionMatch() {
      assertEquals(defaultPeerToPeerWorkDef, factory.getDefaultPeerToPeerWorkflowDefinitionMatch().getWorkDefinition());
   }

   /**
    * When peerReview has no WorkDefinition attribute set, then default peer review WorkDefinition is returned
    */
   @Test
   public void testGetWorkDefinitionForPeerToPeerReview_deafault() throws Exception {
      when(workItemService.getAttributeValues(peerReview, AtsAttributeTypes.WorkflowDefinition)).thenReturn(
         Collections.emptyList());

      WorkDefinitionMatch match = factory.getWorkDefinitionForPeerToPeerReview(peerReview);

      assertEquals(defaultPeerToPeerWorkDef, match.getWorkDefinition());
   }

   /**
    * Test that peer review WorkDefinition id comes from teamDefinition hierarchy
    */
   @Test
   public void testGetWorkDefinitionForPeerToPeerReviewNotYetCreated() throws Exception {
      when(teamDefService.getTeamDefinition(teamWf)).thenReturn(topTeamDef);
      when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());

      WorkDefinitionMatch match = factory.getWorkDefinitionForPeerToPeerReviewNotYetCreated(teamWf);

      assertEquals(defaultPeerToPeerWorkDef, match.getWorkDefinition());
   }

   /**
    * When peerReview WorkDefinition attribute is set, then that WorkDefinition is returned instead of default
    */
   @Test
   public void testGetWorkDefinitionForPeerToPeerReviewIAtsTeamWorkflowIAtsPeerToPeerReview__fromReview() throws Exception {
      List<Object> attrValues = new ArrayList<Object>();
      attrValues.add(MyPeerToPeerWorkDefId);
      when(workItemService.getAttributeValues(peerReview, AtsAttributeTypes.WorkflowDefinition)).thenReturn(attrValues);

      WorkDefinitionMatch match = factory.getWorkDefinitionForPeerToPeerReview(peerReview);

      assertEquals(myPeerToPeerWorkDef, match.getWorkDefinition());
   }

   /**
    * Test that stand alone peer WorkDefinition comes from actionableItem's Team Definition hierarchy
    */
   @Test
   public void testGetWorkDefinitionForPeerToPeerReviewNotYetCreatedAndStandalone() throws Exception {
      when(teamDefService.getTeamDefinition(teamWf)).thenReturn(featureTeamDef);
      List<Object> attrValues = new ArrayList<Object>();
      attrValues.add(MyPeerToPeerWorkDefId);
      when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         attrValues);
      when(actionableItem.getTeamDefinitionInherited()).thenReturn(topTeamDef);
      when(topTeamDef.getRelatedPeerWorkDefinition()).thenReturn(MyPeerToPeerWorkDefId);

      WorkDefinitionMatch match =
         factory.getWorkDefinitionForPeerToPeerReviewNotYetCreatedAndStandalone(actionableItem);

      assertEquals(myPeerToPeerWorkDef, match.getWorkDefinition());
   }

   /**
    * When no team definition in team definition hierarchy has a Peer WorkDefinition defined, return No-Match<br>
    * When top team definition has a Peer WorkDefinition defined, return it's value.
    */
   @Test
   public void testGetPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse() throws Exception {
      // Setup all teamDefinitions to not have values defined
      when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      when(workItemService.getAttributeValues(projTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      when(workItemService.getAttributeValues(featureTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());

      // Test that no-match is returned
      WorkDefinitionMatch peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse =
         factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(topTeamDef);
      assertFalse(peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse.isMatched());
      assertFalse(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(projTeamDef).isMatched());
      assertFalse(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(featureTeamDef).isMatched());

      // Setup that top team definition has WorkDefinition defined
      List<Object> attrValues = new ArrayList<Object>();
      attrValues.add(MyPeerToPeerWorkDefId);
      when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         attrValues);
      when(workDefinitionService.getWorkDef(eq(MyPeerToPeerWorkDefId), any(XResultData.class))).thenReturn(
         myPeerToPeerWorkDef);
      when(topTeamDef.getRelatedPeerWorkDefinition()).thenReturn(MyPeerToPeerWorkDefId);

      // Test that match is returned
      peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse =
         factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(topTeamDef);
      assertTrue(peerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse.isMatched());
      assertTrue(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(projTeamDef).isMatched());
      assertTrue(factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(featureTeamDef).isMatched());
      assertEquals(myPeerToPeerWorkDef,
         factory.getPeerToPeerWorkDefinitionFromTeamDefinitionAttributeValueRecurse(featureTeamDef).getWorkDefinition());
   }

   @Test
   public void testGetWorkDefinitionIAtsWorkItem() throws Exception {
      when(workItemService.getParentTeamWorkflow(peerReview)).thenReturn(teamWf);
      when(teamDefService.getTeamDefinition(teamWf)).thenReturn(topTeamDef);
      when(workItemService.getAttributeValues(topTeamDef, AtsAttributeTypes.RelatedPeerWorkflowDefinition)).thenReturn(
         Collections.emptyList());
      when(
         workDefinitionService.getWorkDef(Matchers.eq(WorkDefinitionFactory.PeerToPeerDefaultWorkflowDefinitionId),
            (XResultData) Matchers.anyObject())).thenReturn(defaultPeerToPeerWorkDef);

      WorkDefinitionMatch match = factory.getWorkDefinition(peerReview);
      assertEquals(defaultPeerToPeerWorkDef, match.getWorkDefinition());
   }

}