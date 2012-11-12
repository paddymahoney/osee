package org.eclipse.osee.orcs.rest.client;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.Operator;
import org.eclipse.osee.framework.core.enums.QueryOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 */
public interface QueryBuilder {

   public static IAttributeType ANY_ATTRIBUTE_TYPE = TokenFactory.createAttributeType(Long.MIN_VALUE,
      "Any Attribute Type");

   QueryBuilder includeCache();

   QueryBuilder includeCache(boolean enabled);

   boolean isCacheIncluded();

   QueryBuilder includeDeleted();

   QueryBuilder includeDeleted(boolean enabled);

   boolean areDeletedIncluded();

   QueryBuilder includeTypeInheritance();

   QueryBuilder includeTypeInheritance(boolean enabled);

   boolean isTypeInheritanceIncluded();

   QueryBuilder fromTransaction(int transactionId);

   int getFromTransaction();

   QueryBuilder headTransaction();

   boolean isHeadTransaction();

   QueryBuilder excludeCache();

   QueryBuilder excludeDeleted();

   QueryBuilder excludeTypeInheritance();

   /**
    * Resets query builder to default settings. This also clear all criteria added to original query.
    */
   QueryBuilder resetToDefaults();

   /**
    * Search criteria that finds a given artifact id
    */
   QueryBuilder andLocalId(int... artifactId) throws OseeCoreException;

   /**
    * Search criteria that finds a given artifact ids
    */
   QueryBuilder andLocalIds(Collection<Integer> artifactIds) throws OseeCoreException;

   /**
    * Search criteria that finds a given artifact with guids or hrids
    */
   QueryBuilder andGuidsOrHrids(String... ids) throws OseeCoreException;

   /**
    * Search criteria that finds a given artifact with guids or hrids
    */
   QueryBuilder andGuidsOrHrids(Collection<String> ids) throws OseeCoreException;

   /**
    * Artifacts matching token id(s)
    */
   QueryBuilder andIds(IArtifactToken... artifactToken) throws OseeCoreException;

   /**
    * Artifacts matching token id(s)
    */
   QueryBuilder andIds(Collection<? extends IArtifactToken> artifactTokens) throws OseeCoreException;

   /**
    * Search criteria that finds a given artifact type
    */
   QueryBuilder andIsOfType(IArtifactType... artifactType) throws OseeCoreException;

   /**
    * Search criteria that finds a given artifact types
    */
   QueryBuilder andIsOfType(Collection<? extends IArtifactType> artifactType) throws OseeCoreException;

   /**
    * Search criteria that checks for the existence of an attribute type(s).
    */
   QueryBuilder andExists(IAttributeType... attributeType) throws OseeCoreException;

   /**
    * Search criteria that checks for the existence of an attribute types.
    */
   QueryBuilder andExists(Collection<? extends IAttributeType> attributeTypes) throws OseeCoreException;

   /**
    * Search criteria that follows the relation link ending on the given side
    * 
    * @param relationType the type to start following the link from
    */
   QueryBuilder andExists(IRelationType relationType) throws OseeCoreException;

   /**
    * Artifact name equals value
    */
   QueryBuilder andNameEquals(String artifactName) throws OseeCoreException;

   /**
    * Search criteria that finds an attribute of the given type with its current value relative to the given value based
    * on the operator provided.
    */
   QueryBuilder and(IAttributeType attributeType, Operator operator, String value) throws OseeCoreException;

   /**
    * Search criteria that finds an attribute of the given type with its current value exactly equal (or not equal) to
    * any one of the given literal values. If the list only contains one value, then the search is conducted exactly as
    * if the single value constructor was called. This search does not support the (* wildcard) for multiple values.
    */
   QueryBuilder and(IAttributeType attributeType, Operator operator, Collection<String> values) throws OseeCoreException;

   /**
    * Search criteria that finds an attribute of the given type with its current value relative to the given value based
    * on the operator provided.
    */
   QueryBuilder and(IAttributeType attributeType, String value, QueryOption... options) throws OseeCoreException;

   /**
    * Search criteria that finds an attribute of the given type with its current value exactly equal (or not equal) to
    * any one of the given literal values. If the list only contains one value, then the search is conducted exactly as
    * if the single value constructor was called. This search does not support the (* wildcard) for multiple values.
    */
   QueryBuilder and(Collection<? extends IAttributeType> attributeTypes, String value, QueryOption... options) throws OseeCoreException;

   /**
    * Search for related artifacts
    * 
    * @param relationTypeSide the type-side to search on
    */
   QueryBuilder andRelatedTo(IRelationTypeSide relationTypeSide, IArtifactToken... artifacts) throws OseeCoreException;

   /**
    * Search for related artifacts
    * 
    * @param relationTypeSide the type-side to search on
    */
   QueryBuilder andRelatedTo(IRelationTypeSide relationTypeSide, Collection<? extends IArtifactToken> artifacts) throws OseeCoreException;

   /**
    * Search for related artifacts
    * 
    * @param relationTypeSide the type-side to search on
    */
   QueryBuilder andRelatedToLocalIds(IRelationTypeSide relationTypeSide, int... artifactIds) throws OseeCoreException;

   /**
    * Search for related artifacts
    * 
    * @param relationTypeSide the type-side to search on
    */
   QueryBuilder andRelatedToLocalIds(IRelationTypeSide relationTypeSide, Collection<Integer> artifactIds) throws OseeCoreException;

   /**
    * Executes query
    * 
    * @return artifact search results
    */
   public ResultSet<Integer> getResults() throws OseeCoreException;

   /**
    * Executes query
    * 
    * @return artifact search results with match locations
    */
   //   public ResultSet<Match<Artifact, Attribute<?>>> getMatches() throws OseeCoreException;

   /**
    * Count search results
    */
   public int getCount() throws OseeCoreException;

}