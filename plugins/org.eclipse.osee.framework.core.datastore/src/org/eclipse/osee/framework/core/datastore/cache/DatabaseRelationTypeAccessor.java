/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.datastore.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.datastore.internal.Activator;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.model.type.RelationTypeFactory;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class DatabaseRelationTypeAccessor extends AbstractDatabaseAccessor<RelationType> {
   private static final String SELECT_LINK_TYPES = "SELECT * FROM osee_relation_link_type";
   private static final String INSERT_RELATION_TYPE =
      "INSERT INTO osee_relation_link_type (rel_link_type_id, rel_link_type_guid, type_name, a_name, b_name, a_art_type_id, b_art_type_id, multiplicity, default_order_type_guid) VALUES (?,?,?,?,?,?,?,?,?)";
   private static final String UPDATE_RELATION_TYPE =
      "update osee_relation_link_type SET type_name=?, a_name=?, b_name=?, a_art_type_id=?, b_art_type_id=?, multiplicity=?, default_order_type_guid=? where rel_link_type_id = ?";

   private final ArtifactTypeCache artifactTypeCache;
   private final RelationTypeFactory factory;

   public DatabaseRelationTypeAccessor(IOseeDatabaseService databaseService, ArtifactTypeCache artifactCache, RelationTypeFactory factory) {
      super(databaseService);
      this.artifactTypeCache = artifactCache;
      this.factory = factory;
   }

   @Override
   public void load(IOseeCache<RelationType> cache) throws OseeCoreException {
      artifactTypeCache.ensurePopulated();
      IOseeStatement chStmt = getDatabaseService().getStatement();

      try {
         chStmt.runPreparedQuery(SELECT_LINK_TYPES);

         while (chStmt.next()) {
            String typeName = chStmt.getString("type_name");
            int typeId = chStmt.getInt("rel_link_type_id");
            int aArtTypeId = chStmt.getInt("a_art_type_id");
            int bArtTypeId = chStmt.getInt("b_art_type_id");
            int multiplicityValue = chStmt.getInt("multiplicity");
            try {
               IArtifactType artifactTypeSideA = artifactTypeCache.getById(aArtTypeId);
               IArtifactType artifactTypeSideB = artifactTypeCache.getById(bArtTypeId);
               RelationTypeMultiplicity multiplicity =
                  RelationTypeMultiplicity.getRelationMultiplicity(multiplicityValue);
               String sideAName = chStmt.getString("a_name");
               String sideBName = chStmt.getString("b_name");
               String defaultOrderTypeGuid = chStmt.getString("default_order_type_guid");
               String guid = chStmt.getString("rel_link_type_guid");

               RelationType relationType =
                  factory.createOrUpdate(cache, typeId, StorageState.LOADED, guid, typeName, sideAName, sideBName,
                     artifactTypeSideA, artifactTypeSideB, multiplicity, defaultOrderTypeGuid);
               relationType.clearDirty();
            } catch (OseeCoreException ex) {
               String message =
                  String.format(
                     "Error loading relation type - name:[%s] id:[%s] aArtTypeId:[%s] bArtTypeid:[%s] multiplicity:[%s]",
                     typeName, typeId, aArtTypeId, bArtTypeId, multiplicityValue);
               OseeLog.log(Activator.class, Level.SEVERE, message, ex);
            }
         }
      } finally {
         chStmt.close();
      }
   }

   @Override
   public void store(Collection<RelationType> relationTypes) throws OseeCoreException {
      List<Object[]> insertData = new ArrayList<Object[]>();
      List<Object[]> updateData = new ArrayList<Object[]>();
      for (RelationType type : relationTypes) {
         if (type.isDirty()) {
            switch (type.getStorageState()) {
               case CREATED:
                  type.setId(getSequence().getNextRelationTypeId());
                  insertData.add(toInsertValues(type));
                  break;
               case MODIFIED:
                  updateData.add(toUpdateValues(type));
                  break;
               default:
                  break;
            }
         }
      }
      OseeConnection connection = getDatabaseService().getConnection();
      try {
         getDatabaseService().runBatchUpdate(connection, INSERT_RELATION_TYPE, insertData);
         getDatabaseService().runBatchUpdate(connection, UPDATE_RELATION_TYPE, updateData);
      } finally {
         connection.close();
      }
      for (RelationType type : relationTypes) {
         type.clearDirty();
      }
   }

   private Object[] toInsertValues(RelationType type) throws OseeCoreException {
      return new Object[] {
         type.getId(),
         type.getGuid(),
         type.getName(),
         type.getSideAName(),
         type.getSideBName(),
         artifactTypeCache.get(type.getArtifactTypeSideA()).getId(),
         artifactTypeCache.get(type.getArtifactTypeSideB()).getId(),
         type.getMultiplicity().getValue(),
         type.getDefaultOrderTypeGuid()};
   }

   private Object[] toUpdateValues(RelationType type) throws OseeCoreException {
      return new Object[] {
         type.getName(),
         type.getSideAName(),
         type.getSideBName(),
         artifactTypeCache.get(type.getArtifactTypeSideA()).getId(),
         artifactTypeCache.get(type.getArtifactTypeSideB()).getId(),
         type.getMultiplicity().getValue(),
         type.getDefaultOrderTypeGuid(),
         type.getId()};
   }
}
