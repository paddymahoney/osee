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

package org.eclipse.osee.framework.skynet.core.artifact;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeInvalidInheritanceException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.DbTransaction;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;

/**
 * Contains methods specific to artifact types. All artifact methods will eventually be moved from the
 * ConfigurationPersistenceManager to here.
 * 
 * @author Donald G. Dunne
 */
public class ArtifactTypeManager {

   private static final ArtifactTypeManager instance = new ArtifactTypeManager();

   private final IArtifactTypeDataAccess artifactTypeDataAccess;
   private final IOseeTypeDataAccessor dataAccessor;
   private final OseeTypeCache oseeTypeCache;
   private final IOseeTypeFactory artifactTypefactory;

   private ArtifactTypeManager() {
      oseeTypeCache = new OseeTypeCache();
      artifactTypefactory = new OseeTypeFactory(oseeTypeCache);
      dataAccessor = new OseeTypeDatabaseAccessor(oseeTypeCache, artifactTypefactory);
      artifactTypeDataAccess = new ArtifactTypeDataAccess(dataAccessor, oseeTypeCache);
   }

   private static void ensureArtifactTypePopulated() throws OseeCoreException {
      instance.dataAccessor.ensureArtifactTypePopulated();
   }

   public static Collection<ArtifactType> getArtifactTypesFromAttributeType(String requestedAttributeType, Branch branch) throws OseeCoreException {
      List<ArtifactType> artifactTypes = new ArrayList<ArtifactType>();
      AttributeType attributeType = AttributeTypeManager.getType(requestedAttributeType);
      for (ArtifactType artifactType : getAllTypes()) {
         if (artifactType.getAttributeTypes(branch).contains(attributeType)) {
            artifactTypes.add(artifactType);
         }
      }
      return artifactTypes;
   }

   public static Collection<ArtifactType> getValidArtifactTypes(Branch branch) throws OseeCoreException {
      // TODO Filter artifact types by branch
      return ArtifactTypeManager.getAllTypes();
   }

   public static void createInheritance(String superType, Collection<String> descendants) throws OseeCoreException {
      ensureArtifactTypePopulated();
      ArtifactType superArtifactType = getType(superType);
      List<Object[]> typeDescendants = new ArrayList<Object[]>();
      Set<ArtifactType> artTypeDescendats = new HashSet<ArtifactType>();
      for (String descendant : descendants) {
         ArtifactType artTypeDescendant = getType(descendant);
         if (superArtifactType.equals(artTypeDescendant)) {
            throw new OseeInvalidInheritanceException(String.format(
                  "Circular inheritance detected artifact type [%s] inherits from [%s]", artTypeDescendant,
                  superArtifactType));
         }
         if (!inheritanceExists(artTypeDescendant, superArtifactType)) {
            if (artTypeDescendats.add(artTypeDescendant)) {
               typeDescendants.add(new Object[] {artTypeDescendant.getArtTypeId(), superArtifactType.getArtTypeId()});
            }
         }
      }
      if (!typeDescendants.isEmpty()) {
         instance.dataAccessor.storeTypeInheritance(typeDescendants);
         for (ArtifactType artifactType : artTypeDescendats) {
            instance.oseeTypeCache.cacheArtifactTypeInheritance(artifactType, superArtifactType);
         }
      }
   }

   public static boolean inheritanceExists(ArtifactType artifactType, ArtifactType superArtifactType) throws OseeCoreException {
      ensureArtifactTypePopulated();
      boolean exists = false;
      if (artifactType.hasSuperArtifactTypes()) {
         exists = artifactType.getSuperArtifactTypes().contains(superArtifactType);
      }
      return exists;
   }

   public static ArtifactType createType(String guid, boolean isAbstract, String artifactTypeName) throws OseeCoreException {
      ArtifactType artifactType;
      if (!typeExists(artifactTypeName)) {
         artifactType = instance.dataAccessor.createArtifactType(guid, isAbstract, artifactTypeName);
         instance.dataAccessor.storeArtifactType(artifactType);
         instance.oseeTypeCache.cacheArtifactType(artifactType);
      } else {
         // TODO: Check if anything valuable is different and update it
         artifactType = getType(artifactTypeName);
      }
      return artifactType;
   }

   /**
    * @return Returns all of the descriptors.
    * @throws OseeTypeDoesNotExist
    * @throws OseeCoreException
    */
   public static Collection<ArtifactType> getAllTypes() throws OseeCoreException {
      ensureArtifactTypePopulated();
      return instance.oseeTypeCache.getAllArtifactTypes();
   }

   public static boolean typeExists(String name) throws OseeCoreException {
      ensureArtifactTypePopulated();
      return instance.oseeTypeCache.getArtifactTypeByName(name) != null;
   }

   /**
    * @return Returns the artifact type matching the name
    * @param name artifact type name to match
    * @throws OseeDataStoreException
    * @throws OseeTypeDoesNotExist
    */
   public static ArtifactType getType(String name) throws OseeCoreException {
      ensureArtifactTypePopulated();
      ArtifactType artifactType = instance.oseeTypeCache.getArtifactTypeByName(name);
      if (artifactType == null) {
         throw new OseeTypeDoesNotExist("Artifact type [" + name + "] is not available.");
      }
      return artifactType;
   }

   /**
    * Get Artifact Types by type names.
    * 
    * @return Returns the types with a particular name
    * @param artifactTypeNames names to get
    * @throws OseeDataStoreException
    * @throws OseeTypeDoesNotExist if any name in the artifactTypeNames does not match
    */
   public static List<ArtifactType> getTypes(Iterable<String> artifactTypeNames) throws OseeCoreException {
      List<ArtifactType> artifactTypes = new ArrayList<ArtifactType>();
      for (String artifactTypeName : artifactTypeNames) {
         artifactTypes.add(getType(artifactTypeName));
      }
      return artifactTypes;
   }

   /**
    * @return Returns the descriptor with a particular name, null if it does not exist.
    * @throws OseeTypeDoesNotExist
    */
   public static ArtifactType getType(int artTypeId) throws OseeCoreException {
      ensureArtifactTypePopulated();

      ArtifactType artifactType = instance.oseeTypeCache.getArtifactTypeById(artTypeId);

      if (artifactType == null) {
         throw new OseeTypeDoesNotExist("Atrifact type: " + artTypeId + " is not available.");
      }
      return artifactType;
   }

   /**
    * Get a new instance of type artifactTypeName
    * 
    * @param artifactTypeName
    * @param branch
    * @throws OseeCoreException
    */
   public static Artifact addArtifact(String artifactTypeName, Branch branch) throws OseeCoreException {
      return getType(artifactTypeName).makeNewArtifact(branch);
   }

   /**
    * Get a new instance of type artifactTypeName and set it's name.
    * 
    * @param artifactTypeName
    * @param branch
    * @param name
    */
   public static Artifact addArtifact(String artifactTypeName, Branch branch, String name) throws OseeCoreException {
      Artifact artifact = addArtifact(artifactTypeName, branch);
      artifact.setDescriptiveName(name);
      return artifact;
   }

   /**
    * Get a new instance of the type of artifact. This is just a convenience method that calls makeNewArtifact on the
    * known factory with this descriptor for the descriptor parameter, and the supplied branch.
    * 
    * @param branch branch on which artifact will be created
    * @return Return artifact reference
    * @throws OseeCoreException
    * @see ArtifactFactory#makeNewArtifact(Branch, ArtifactType, String, String, ArtifactProcessor)
    */
   public static Artifact addArtifact(String artifactTypeName, Branch branch, String guid, String humandReadableId) throws OseeCoreException {
      return getType(artifactTypeName).makeNewArtifact(branch, guid, humandReadableId);
   }

   private static final String DELETE_VALID_REL = "delete from osee_valid_relations where art_type_id = ?";
   private static final String DELETE_VALID_ATTRIBUTE = "delete from osee_valid_attributes where art_type_id = ?";
   private static final String COUNT_ARTIFACT_OCCURRENCE = "select count(1) FROM osee_artifact where art_type_id = ?";
   private static final String DELETE_ARIFACT_TYPE = "delete from osee_artifact_type where art_type_id = ?";

   public static void purgeArtifactType(final ArtifactType artifactType) throws OseeCoreException {
      int artifactCount =
            ConnectionHandler.runPreparedQueryFetchInt(0, COUNT_ARTIFACT_OCCURRENCE, artifactType.getArtTypeId());

      if (artifactCount != 0) {
         throw new OseeArgumentException(
               "Can not delete artifact type " + artifactType.getName() + " because there are " + artifactCount + " existing artifacts of this type.");
      }
      new DbTransaction() {

         @Override
         protected void handleTxWork(OseeConnection connection) throws OseeCoreException {
            int artTypeId = artifactType.getArtTypeId();
            ConnectionHandler.runPreparedUpdate(connection, DELETE_VALID_REL, artTypeId);
            ConnectionHandler.runPreparedUpdate(connection, DELETE_VALID_ATTRIBUTE, artTypeId);
            ConnectionHandler.runPreparedUpdate(connection, DELETE_ARIFACT_TYPE, artTypeId);
         }

      }.execute();
   }

   /**
    * Given a set of artifact types, they will be converted to the new artifact type and the old artifact types will be
    * purged
    * 
    * @param purgeArtifactTypes types to be converted and purged
    * @param newArtifactType new type to convert any existing artifacts of the old type
    * @throws OseeCoreException
    */
   public static void purgeArtifactTypesWithCheck(Collection<ArtifactType> purgeArtifactTypes, ArtifactType newArtifactType) throws OseeCoreException {
      for (ArtifactType purgeArtifactType : purgeArtifactTypes) {
         // find all artifact of this type on all branches and make a unique list for type change (since it is not by branch)
         List<Artifact> artifacts = ArtifactQuery.getArtifactListFromType(purgeArtifactType, true);
         if (artifacts.size() > 0) {
            HashMap<Integer, Artifact> artifactMap = new HashMap<Integer, Artifact>();
            for (Artifact artifact : artifacts) {
               artifactMap.put(artifact.getArtId(), artifact);
            }
            if (newArtifactType == null) {
               HashSet<Branch> branches = new HashSet<Branch>();
               for (Artifact artifact : artifacts) {
                  branches.add(artifact.getBranch());
               }
               throw new OseeStateException(
                     "Found " + artifacts.size() + " artifact references of type " + purgeArtifactType + " on branches " + branches);
            } else {
               changeArtifactType(artifactMap.values(), newArtifactType);
            }
         }
         purgeArtifactType(purgeArtifactType);
      }
   }

   /**
    * Run code that will be run during purge with convert and report on what relations, attributes will be deleted as
    * part of the conversion.
    * 
    * @param purgeArtifactTypes
    * @param newArtifactType
    * @throws OseeCoreException
    */
   public static void purgeArtifactTypesWithConversionReportOnly(StringBuffer results, Collection<ArtifactType> purgeArtifactTypes, ArtifactType newArtifactType) throws OseeCoreException {
      try {
         for (ArtifactType purgeArtifactType : purgeArtifactTypes) {
            // find all artifact of this type on all branches and make a unique list for type change (since it is not by branch)
            List<Artifact> artifacts = ArtifactQuery.getArtifactListFromType(purgeArtifactType, true);
            if (artifacts.size() > 0) {
               HashMap<Integer, Artifact> artifactMap = new HashMap<Integer, Artifact>();
               for (Artifact artifact : artifacts) {
                  artifactMap.put(artifact.getArtId(), artifact);
               }
               ChangeArtifactType.changeArtifactTypeReportOnly(results, artifactMap.values(), newArtifactType);
            }
         }
      } catch (Exception ex) {
         throw new OseeCoreException(ex);
      }
   }

   /**
    * Changes the artifactType of the artifacts to the provided artifact artifactType
    * 
    * @param artifacts
    * @param artifactType
    */
   public static void changeArtifactType(Collection<Artifact> artifacts, ArtifactType artifactType) throws OseeCoreException {
      ChangeArtifactType.changeArtifactType(artifacts, artifactType);
   }

   public static void printInheritanceTree(Writer out) throws OseeCoreException {
      ArtifactType artifactType = ArtifactTypeManager.getType("Artifact");
      try {
         out.write("Inheritance:\n");
         printInheritanceHelper(artifactType, out);
      } catch (Exception e) {
         throw new OseeWrappedException(e);
      }
   }

   private static void printInheritanceHelper(ArtifactType artifactType, Writer out) throws Exception {
      Collection<ArtifactType> artifactTypes = artifactType.getDescendants(false);
      if (!artifactTypes.isEmpty()) {
         out.write(artifactType.getName());
         out.write("->");
         out.write(artifactTypes.toString());
         out.write("\n");
         for (ArtifactType child : artifactTypes) {
            printInheritanceHelper(child, out);
         }
      }
   }

   public static void setAttributeTypes(ArtifactType artifactType, Collection<AttributeType> attributeType, Branch branch) throws OseeCoreException {
      instance.artifactTypeDataAccess.setAttributeTypes(artifactType, attributeType, branch);
   }
}
