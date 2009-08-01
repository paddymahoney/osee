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
package org.eclipse.osee.framework.skynet.core.relation;

import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;

public enum CoreRelationEnumeration implements IRelationEnumeration {

   Users_Artifact(true, "Users"), Users_User(false, "Users"),

   // Define relations
   Supercedes_Supercedes(true, "Supercedes"),
   Supercedes_Superceded(false, "Supercedes"),
   AddressesIssues_AddressesIssues(true, "Addresses Issues"),
   AddressesIssues_IssuedArtifact(false, "Addresses Issues"),
   SupportingInfo_SupportedBy(true, "Supporting Info"),
   SupportingInfo_SupportingInfo(false, "Supporting Info"),

   DEFAULT_STYLESHEET__RENDERER(true, "Default Stylesheet"),
   DEFAULT_STYLESHEET__STYLESHEET(false, "Default Stylesheet"),
   REQUIREMENT_TRACE__HIGHER_LEVEL(true, "Requirement Trace"),
   REQUIREMENT_TRACE__LOWER_LEVEL(false, "Requirement Trace"),
   DEFAULT_HIERARCHICAL__CHILD(false, "Default Hierarchical"),
   DEFAULT_HIERARCHICAL__PARENT(true, "Default Hierarchical"),

   Dependency__Dependency(false, "Dependency"),
   Dependency__Artifact(true, "Dependency"),

   TeamMember_Team(true, "TeamMember"),
   TeamMember_Member(false, "TeamMember"),

   WorkItem__Parent(true, "Work Item"),
   WorkItem__Child(false, "Work Item"),

   Design__Design(false, "Design"),
   Design__Requirement(true, "Design"),

   UNIVERSAL_GROUPING__MEMBERS(false, "Universal Grouping"),
   UNIVERSAL_GROUPING__GROUP(true, "Universal Grouping"),

   ALLOCATION__REQUIREMENT(true, "Allocation"),
   ALLOCATION__COMPONENT(false, "Allocation"),
   Validation__Requirement(true, "Validation"),
   Validation__Validator(false, "Validation"),

   Verification__Requirement(true, "Verification"),
   Verification__Verifier(false, "Verification"),

   Uses__Requirement(true, "Uses"),
   Uses__TestUnit(false, "Uses"),

   CodeRequirement_Requirement(false, "Code-Requirement"),
   CodeRequirement_CodeUnit(true, "Code-Requirement"),

   TestConfigurationRelation_TestScript(true, "Test Configuration Relation"),
   TestConfigurationRelation_TestConfiguration(false, "Test Configuration Relation"),
   RunByRelation_User(true, "Run By Relation"),
   RunByRelation_TestRun(false, "Run By Relation"),
   TestRunConfigRelation_TestConfiguration(true, "Test Run Config Relation"),
   TestRunConfigRelation_TestRun(false, "Test Run Config Relation");

   private RelationSide relationSide;
   private String typeName;

   private CoreRelationEnumeration(boolean sideA, String typeName) {
      this.relationSide = sideA ? RelationSide.SIDE_A : RelationSide.SIDE_B;
      this.typeName = typeName;
   }

   /**
    * @return Returns the sideName.
    */
   @Deprecated
   public boolean isSideA() {
      return relationSide.isSideA();
   }

   public String getSideName() throws OseeTypeDoesNotExist, OseeDataStoreException {
      return getRelationType().getSideName(relationSide);
   }

   /**
    * @return Returns the typeName.
    */
   public String getTypeName() {
      return typeName;
   }

   public RelationType getRelationType() throws OseeTypeDoesNotExist, OseeDataStoreException {
      return RelationTypeManager.getType(typeName);
   }

   public boolean isThisType(RelationLink link) {
      return link.getRelationType().getTypeName().equals(typeName);
   }

   @Override
   public RelationSide getSide() {
      return relationSide;
   }
}
