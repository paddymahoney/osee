/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.jdk.core.type.BaseId;
import org.eclipse.osee.framework.jdk.core.type.BaseIdentity;
import org.eclipse.osee.framework.jdk.core.type.FullyNamedIdentity;
import org.eclipse.osee.framework.jdk.core.type.NamedId;
import org.eclipse.osee.framework.jdk.core.type.NamedIdentity;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Lib;

public final class TokenFactory {

   public static final Pattern nameIdPattern = Pattern.compile("\\[(.*)\\]-\\[(.*)\\]");

   private TokenFactory() {
      // Utility Class
   }

   public static IArtifactType createArtifactType(long guid, String name) {
      return new ArtifactTypeToken(guid, name);
   }

   /**
    * @param token as [name]-[uuid]
    */
   public static IArtifactType createArtifactTypeFromToken(String token) {
      Matcher matcher = nameIdPattern.matcher(token);
      if (matcher.find()) {
         Long uuid = Long.valueOf(matcher.group(2));
         String name = matcher.group(1);
         return new ArtifactTypeToken(uuid, name);
      }
      return null;
   }

   public static IAttributeType createAttributeType(long guid, String name) {
      return new AttributeTypeToken(guid, name);
   }

   public static IAttributeType createAttributeType(long guid, String name, String description) {
      return new AttributeTypeToken(guid, name, description);
   }

   public static IRelationType createRelationType(long guid, String name) {
      return new RelationTypeToken(guid, name);
   }

   public static IRelationTypeSide createRelationTypeSide(RelationSide relationSide, long guid, String name) {
      return new RelationTypeSideToken(guid, name, relationSide);
   }

   public static IAccessContextId createAccessContextId(String guid, String name) {
      return new AccessContextIdToken(guid, name);
   }

   public static ArtifactId createArtifactId(Long uuid) {
      return new ArtifactToken(uuid, null, null, null);
   }

   public static IArtifactToken createArtifactToken(long uuid, String name, IArtifactType artifactType) {
      Conditions.checkExpressionFailOnTrue(uuid <= 0, "Artifact Token Uuid must be > 0 for token [%s] type [%s]", name,
         artifactType);
      return new ArtifactToken(uuid, null, name, artifactType);
   }

   public static IArtifactToken createArtifactToken(long uuid, String guid, String name, IArtifactType artifactType) {
      Conditions.checkExpressionFailOnTrue(uuid <= 0, "Artifact Token Uuid must be > 0 for token [%s] type [%s]", name,
         artifactType);
      return new ArtifactToken(uuid, guid, name, artifactType);
   }

   public static IUserToken createUserToken(long uuid, String guid, String name, String email, String userId, boolean active, boolean admin, boolean creationRequired) {
      Conditions.checkExpressionFailOnTrue(uuid <= 0, "User Token Uuid must be > 0 for userId [%s]", userId);
      return new UserToken(uuid, guid, name, userId, active, admin, email, creationRequired);
   }

   public static IOseeBranch createBranch(String name) {
      return createBranch(Lib.generateUuid(), name);
   }

   public static IOseeBranch createBranch(Long branchId, String name) {
      return new BranchToken(branchId, name);
   }

   public static IOseeBranch createBranch(long branchId, String name) {
      return new BranchToken(branchId, name);
   }

   public static BranchId createBranch(Long branchId) {
      return new BranchToken(branchId, null);
   }

   public static BranchId createBranch() {
      return new BranchToken(Lib.generateUuid(), null);
   }

   public static IRelationSorterId createSorterId(String guid, String name) {
      return new SorterIdToken(guid, name);
   }

   public static TupleTypeId createTupleType(Long tupleTypeId) {
      return new TupleTypeImpl(tupleTypeId);
   }

   public static <E1, E2> Tuple2Type<E1, E2> createTuple2Type(TupleFamilyId family, Long tupleTypeId) {
      return new Tuple2TypeImpl<E1, E2>(family, tupleTypeId);
   }

   public static <E1, E2, E3> Tuple3Type<E1, E2, E3> createTuple3Type(TupleFamilyId family, Long tupleTypeId) {
      return new Tuple3TypeImpl<E1, E2, E3>(family, tupleTypeId);
   }

   public static <E1, E2, E3, E4> Tuple4Type<E1, E2, E3, E4> createTuple4Type(TupleFamilyId family, Long tupleTypeId) {
      return new Tuple4TypeImpl<E1, E2, E3, E4>(family, tupleTypeId);
   }

   public static TupleFamilyId createTupleFamilyType(Long tupleFamilyTypeId) {
      return new TupleFailyTypeImpl(tupleFamilyTypeId);
   }

   private final static class SorterIdToken extends NamedIdentity<String> implements IRelationSorterId {

      public SorterIdToken(String guid, String name) {
         super(guid, name);
      }

      @Override
      public String toString() {
         return String.format("[%s:%s]", getName(), getGuid());
      }
   }

   private final static class ArtifactTypeToken extends NamedId implements IArtifactType {
      public ArtifactTypeToken(Long id, String name) {
         super(id, name);
      }

      @Override
      public Long getGuid() {
         return getId();
      }
   }

   public static ITransaction createTransaction(int txId) {
      return new TransactionToken(txId);
   }

   private static final class TransactionToken extends BaseIdentity<Integer> implements ITransaction {
      public TransactionToken(Integer txId) {
         super(txId);
      }
   }

   private static final class BranchToken extends NamedId implements IOseeBranch {
      public BranchToken(Long branchId, String name) {
         super(branchId, name);
      }
   }

   private final static class AttributeTypeToken extends FullyNamedIdentity<Long> implements IAttributeType {
      public AttributeTypeToken(Long guid, String name) {
         super(guid, name);
      }

      public AttributeTypeToken(Long guid, String name, String description) {
         super(guid, name, description);
      }
   }

   private final static class RelationTypeToken extends FullyNamedIdentity<Long> implements IRelationType {
      public RelationTypeToken(Long guid, String name) {
         super(guid, name);
      }
   }

   private static class ArtifactToken extends NamedIdentity<String> implements IArtifactToken {
      private final IArtifactType artifactType;
      private final long uuid;

      public ArtifactToken(long uuid, String guid, String name, IArtifactType artifactType) {
         super(guid, name);
         this.uuid = uuid;
         this.artifactType = artifactType;
      }

      @Override
      public IArtifactType getArtifactType() {
         return artifactType;
      }

      @Override
      public Long getUuid() {
         return uuid;
      }
   }

   private static class UserToken extends NamedIdentity<String> implements IUserToken {

      private final String userId;
      private final boolean active;
      private final boolean admin;
      private final String email;
      private final boolean creationRequired;
      private final long uuid;

      public UserToken(long uuid, String guid, String name, String userId, boolean active, boolean admin, String email, boolean creationRequired) {
         super(guid, name);
         this.uuid = uuid;
         this.userId = userId;
         this.active = active;
         this.admin = admin;
         this.email = email;
         this.creationRequired = creationRequired;
      }

      @Override
      public IArtifactType getArtifactType() {
         return CoreArtifactTypes.User;
      }

      @Override
      public String getUserId() {
         return userId;
      }

      @Override
      public boolean isActive() {
         return active;
      }

      @Override
      public boolean isAdmin() {
         return admin;
      }

      @Override
      public String getEmail() {
         return email;
      }

      @Override
      public boolean isCreationRequired() {
         return creationRequired;
      }

      @Override
      public String toString() {
         return String.format("UserToken [userId=[%s], active=[%s], admin=[%s], email=[%s], creationRequired=[%s]",
            userId, active, admin, email, creationRequired);
      }

      @Override
      public Long getUuid() {
         return uuid;
      }
   }

   private final static class AccessContextIdToken extends NamedIdentity<String> implements IAccessContextId {
      public AccessContextIdToken(String guid, String name) {
         super(guid, name);
      }

      @Override
      public String toString() {
         return String.format("%s - %s", getName(), getGuid());
      }
   }

   private final static class RelationTypeSideToken extends FullyNamedIdentity<Long> implements IRelationTypeSide {

      private final RelationSide relationSide;
      private RelationTypeSideToken opposite;

      private RelationTypeSideToken(Long guid, String name, RelationSide relationSide) {
         super(guid, name);
         this.relationSide = relationSide;
      }

      @Override
      public RelationSide getSide() {
         return relationSide;
      }

      @Override
      public boolean isOfType(IRelationType type) {
         return type.getGuid() == getGuid();
      }

      @Override
      public int hashCode() {
         // Do not add relation side to hash code because it will violate the hash code contract
         return super.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof IRelationTypeSide) {
            IRelationTypeSide otherSide = (IRelationTypeSide) obj;
            if (relationSide != otherSide.getSide()) {
               return false;
            }
         }
         if (obj instanceof IRelationType) {
            return super.equals(obj);
         }
         return false;
      }

      @Override
      public synchronized IRelationTypeSide getOpposite() {
         if (opposite == null) {
            opposite = new RelationTypeSideToken(getGuid(), getName(), getSide().oppositeSide());
         }
         return opposite;
      }

      @Override
      public String toString() {
         return String.format("RelationTypeSide - uuid=[%s] type=[%s] side=[%s]", getGuid(), getName(), getSide());
      }
   }

   private static final class Tuple2TypeImpl<E1, E2> extends TupleTypeImpl implements Tuple2Type<E1, E2> {
      public Tuple2TypeImpl(TupleFamilyId family, Long tupleTypeId) {
         super(family, tupleTypeId);
      }
   }

   private static final class Tuple3TypeImpl<E1, E2, E3> extends TupleTypeImpl implements Tuple3Type<E1, E2, E3> {
      public Tuple3TypeImpl(TupleFamilyId family, Long tupleTypeId) {
         super(family, tupleTypeId);
      }
   }

   private static final class Tuple4TypeImpl<E1, E2, E3, E4> extends TupleTypeImpl implements Tuple4Type<E1, E2, E3, E4> {
      public Tuple4TypeImpl(TupleFamilyId family, Long tupleTypeId) {
         super(family, tupleTypeId);
      }
   }

   private static final class TupleFailyTypeImpl extends BaseId implements TupleFamilyId {
      public TupleFailyTypeImpl(Long tupleFamilyTypeId) {
         super(tupleFamilyTypeId);
      }
   }

   public static String createArtifactTypeTokenString(IArtifactType artifactType) {
      return String.format("[%s]-[%d]", artifactType.getName(), artifactType.getGuid());
   }
}
