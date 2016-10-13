/*
 * generated by Xtext
 */
package org.eclipse.osee.framework.core.dsl.ui.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;

/**
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on how to customize content assistant
 */
public class OseeDslProposalProvider extends AbstractOseeDslProposalProvider {

   @Override
   public void completeAddEnum_EntryGuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeAddEnum_EntryGuid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void completeXArtifactType_TypeGuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXArtifactType_TypeGuid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void completeXAttributeType_TypeGuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXAttributeType_TypeGuid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void completeXAttributeTypeRef_BranchUuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXAttributeTypeRef_BranchUuid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void completeXOseeEnumEntry_EntryGuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXOseeEnumEntry_EntryGuid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void completeXOseeEnumType_TypeGuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXOseeEnumType_TypeGuid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void completeXRelationType_TypeGuid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXRelationType_TypeGuid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   @Override
   public void complete_AddEnum(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.complete_AddEnum(model, ruleCall, context, acceptor);
   }

   @Override
   public void completeAccessContext_Guid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeAccessContext_Guid(model, assignment, context, acceptor);
      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   }

   //   @Override
   //   public void completeXArtifactRef_Guid(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
   //      super.completeXArtifactRef_Guid(model, assignment, context, acceptor);
   //      completeGuidGeneration((RuleCall) assignment.getTerminal(), context, acceptor);
   //   }

   @Override
   public void completeXArtifactType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXArtifactType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   @Override
   public void completeXAttributeType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXAttributeType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   @Override
   public void completeXOseeEnumType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXOseeEnumType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   @Override
   public void completeXRelationType_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      super.completeXRelationType_Id(model, assignment, context, acceptor);
      completeRemoteTypeIdGeneration(context, acceptor);
   }

   private void completeGuidGeneration(RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      if (acceptor.canAcceptMoreProposals()) {
         String generatedGUID = String.format("\"%s\"", GUID.create());
         String displayProposalAs = generatedGUID + "- GUID";
         ICompletionProposal proposal = createCompletionProposal(generatedGUID, displayProposalAs, null, context);
         acceptor.accept(proposal);
      }
   }

   private void completeRemoteTypeIdGeneration(ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
      if (acceptor.canAcceptMoreProposals()) {
         String generatedRemoteId = "0x0000000000000000";
         String displayProposalAs = generatedRemoteId + "- RemoteTypeId";
         ICompletionProposal proposal = createCompletionProposal(generatedRemoteId, displayProposalAs, null, context);
         acceptor.accept(proposal);
      }
   }
}
