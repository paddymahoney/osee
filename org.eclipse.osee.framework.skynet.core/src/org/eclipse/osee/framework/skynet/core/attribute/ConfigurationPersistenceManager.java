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

package org.eclipse.osee.framework.skynet.core.attribute;

import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ARTIFACT_TYPE_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTRIBUTE_BASE_TYPE_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTRIBUTE_PROVIDER_TYPE_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTR_BASE_TYPE_ID_SEQ;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTR_PROVIDER_TYPE_ID_SEQ;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTR_TYPE_ID_SEQ;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.DbUtil;
import org.eclipse.osee.framework.db.connection.core.query.Query;
import org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.PersistenceManager;
import org.eclipse.osee.framework.skynet.core.PersistenceManagerInit;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeValidityCache;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.factory.ArtifactFactoryCache;
import org.eclipse.osee.framework.skynet.core.artifact.factory.IArtifactFactory;
import org.eclipse.osee.framework.skynet.core.attribute.providers.AbstractAttributeDataProvider;
import org.eclipse.osee.framework.skynet.core.attribute.providers.IAttributeDataProvider;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.InputStreamImageDescriptor;

/**
 * @author Jeff C. Phillips
 */
public class ConfigurationPersistenceManager implements PersistenceManager {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(ConfigurationPersistenceManager.class);
   private static final String SELECT_ATTRIBUTE_BASE_TYPE =
         "SELECT attr_base_type_id FROM " + ATTRIBUTE_BASE_TYPE_TABLE + " WHERE attribute_class = ?";
   private static final String SELECT_ATTRIBUTE_PROVIDER_TYPE =
         "SELECT attr_provider_type_id FROM " + ATTRIBUTE_PROVIDER_TYPE_TABLE + " WHERE attribute_provider_class = ?";

   private static final String INSERT_ARTIFACT_TYPE =
         "INSERT INTO osee_define_artifact_type (art_type_id, factory_id, namespace, name, factory_key, image) VALUES (?,?,?,?,?,?)";
   private static final String INSERT_VALID_ATTRIBUTE =
         "INSERT INTO osee_define_valid_attributes (art_type_id, attr_type_id) VALUES (?, ?)";
   private static final String INSERT_ATTRIBUTE_TYPE =
         "INSERT INTO osee_define_attribute_type (attr_type_id, attr_base_type_id, attr_provider_type_id, file_type_extension, namespace, name, default_value, validity_xml, min_occurence, max_occurence, tip_text) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
   private static final String INSERT_BASE_ATTRIBUTE_TYPE =
         "INSERT INTO osee_define_attr_base_type (attr_base_type_id, attribute_class) VALUES (?, ?)";
   private static final String INSERT_ATTRIBUTE_PROVIDER_TYPE =
         "INSERT INTO osee_define_attr_provider_type (attr_provider_type_id, attribute_provider_class) VALUES (?, ?)";

   private final ArtifactSubtypeDescriptorCache cacheArtifactSubtypeDescriptors;
   private final DynamicAttributeDescriptorCache cacheDynamicAttributeDescriptors;
   private final AttributeTypeValidityCache cacheAttributeTypeValidity;
   private final ArtifactTypeValidityCache artifactTypeValidityCache;

   private ArtifactFactoryCache artifactFactoryCache;
   private SkynetTransaction transaction;
   private HashMap<String, Pair<String, String>> imageMap;
   private static Pair<String, String> defaultIconLocation =
         new Pair<String, String>("org.eclipse.osee.framework.skynet.core", "images/laser_16_16.gif");

   private static final ConfigurationPersistenceManager instance = new ConfigurationPersistenceManager();

   private ConfigurationPersistenceManager() {
      super();
      cacheArtifactSubtypeDescriptors = new ArtifactSubtypeDescriptorCache();
      cacheDynamicAttributeDescriptors = new DynamicAttributeDescriptorCache();
      cacheAttributeTypeValidity = new AttributeTypeValidityCache();
      artifactTypeValidityCache = new ArtifactTypeValidityCache();
   }

   public static ConfigurationPersistenceManager getInstance() {
      PersistenceManagerInit.initManagerWeb(instance);
      return instance;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.skynet.core.PersistenceManager#setRelatedManagers()
    */
   public void onManagerWebInit() throws Exception {
      artifactFactoryCache = ArtifactFactoryCache.getInstance();
   }

   public void makePersistent(String attributeTypeName, String attributeProviderTypeName, String fileTypeExtension, String namespace, String name, String defaultValue, String validityXml, int minOccurrences, int maxOccurrences, String tipText) throws Exception {
      AttributeExtensionManager extensionManager = AttributeExtensionManager.getInstance();

      Class<? extends Attribute> baseAttributeClass = extensionManager.getAttributeClassFor(attributeTypeName);
      Class<? extends AbstractAttributeDataProvider> providerAttributeClass =
            extensionManager.getAttributeProviderClassFor(attributeProviderTypeName);

      if (!cacheDynamicAttributeDescriptors.descriptorExists(namespace, name)) {
         int attrTypeId = Query.getNextSeqVal(null, ATTR_TYPE_ID_SEQ);
         int attrBaseTypeId = getOrCreateAttributeBaseType(baseAttributeClass);
         int attrProviderTypeId = getOrCreateAttributeProviderType(providerAttributeClass);
         ConnectionHandler.runPreparedUpdate(INSERT_ATTRIBUTE_TYPE, SQL3DataType.INTEGER, attrTypeId,
               SQL3DataType.INTEGER, attrBaseTypeId, SQL3DataType.INTEGER, attrProviderTypeId, SQL3DataType.VARCHAR,
               fileTypeExtension, SQL3DataType.VARCHAR, namespace, SQL3DataType.VARCHAR, name, SQL3DataType.VARCHAR,
               defaultValue, SQL3DataType.VARCHAR, validityXml, SQL3DataType.INTEGER, minOccurrences,
               SQL3DataType.INTEGER, maxOccurrences, SQL3DataType.VARCHAR, tipText);

         DynamicAttributeDescriptor descriptor =
               new DynamicAttributeDescriptor(attrTypeId, baseAttributeClass, providerAttributeClass,
                     fileTypeExtension, namespace, name, defaultValue, validityXml, minOccurrences, maxOccurrences,
                     tipText);
         cacheDynamicAttributeDescriptors.cache(descriptor);
      }
   }

   private int getOrCreateAttributeProviderType(Class<? extends IAttributeDataProvider> baseClass) throws SQLException {
      int attrBaseTypeId = -1;
      String attributeClass = baseClass.getCanonicalName();
      if (attributeClass == null) {
         throw new IllegalArgumentException(
               "The baseClass argument must have a canonical name; it must be directly instantiable");
      }

      ConnectionHandlerStatement chStmt = null;
      try {
         chStmt =
               ConnectionHandler.runPreparedQuery(SELECT_ATTRIBUTE_PROVIDER_TYPE, SQL3DataType.VARCHAR, attributeClass);
         ResultSet rSet = chStmt.getRset();
         if (rSet.next()) {
            attrBaseTypeId = rSet.getInt("attr_provider_type_id");
         } else {
            attrBaseTypeId = Query.getNextSeqVal(null, ATTR_PROVIDER_TYPE_ID_SEQ);

            ConnectionHandler.runPreparedUpdate(INSERT_ATTRIBUTE_PROVIDER_TYPE, SQL3DataType.INTEGER, attrBaseTypeId,
                  SQL3DataType.VARCHAR, attributeClass);
         }
      } finally {
         DbUtil.close(chStmt);
      }
      return attrBaseTypeId;
   }

   private int getOrCreateAttributeBaseType(Class<? extends Attribute> baseClass) throws SQLException {
      int attrBaseTypeId = -1;
      String attributeClass = baseClass.getCanonicalName();
      if (attributeClass == null) {
         throw new IllegalArgumentException(
               "The baseClass argument must have a canonical name; it must be directly instantiable");
      }

      ConnectionHandlerStatement chStmt = null;
      try {
         chStmt = ConnectionHandler.runPreparedQuery(SELECT_ATTRIBUTE_BASE_TYPE, SQL3DataType.VARCHAR, attributeClass);
         ResultSet rSet = chStmt.getRset();
         if (rSet.next()) {
            attrBaseTypeId = rSet.getInt("attr_base_type_id");
         } else {
            attrBaseTypeId = Query.getNextSeqVal(null, ATTR_BASE_TYPE_ID_SEQ);

            ConnectionHandler.runPreparedUpdate(INSERT_BASE_ATTRIBUTE_TYPE, SQL3DataType.INTEGER, attrBaseTypeId,
                  SQL3DataType.VARCHAR, attributeClass);
         }
      } finally {
         DbUtil.close(chStmt);
      }

      return attrBaseTypeId;
   }

   public void makeSubtypePersistent(String factoryName, String namespace, String artifactTypeName, String factoryKey) throws SQLException {
      if (!cacheArtifactSubtypeDescriptors.typeExists(namespace, artifactTypeName)) {
         int artTypeId = Query.getNextSeqVal(null, SkynetDatabase.ART_TYPE_ID_SEQ);
         InputStreamImageDescriptor imageDescriptor = getDefaultImageDescriptor(artifactTypeName);
         IArtifactFactory factory = artifactFactoryCache.getFactoryFromName(factoryName);

         ConnectionHandler.runPreparedUpdate(INSERT_ARTIFACT_TYPE, SQL3DataType.INTEGER, artTypeId,
               SQL3DataType.INTEGER, factory.getFactoryId(), SQL3DataType.VARCHAR, namespace, SQL3DataType.VARCHAR,
               artifactTypeName, SQL3DataType.VARCHAR, factoryKey, SQL3DataType.BLOB, new ByteArrayInputStream(
                     imageDescriptor.getData()));

         new ArtifactSubtypeDescriptor(cacheArtifactSubtypeDescriptors, artTypeId, factoryKey, factory, namespace,
               artifactTypeName, imageDescriptor);
      } else {
         // Check if anything valuable is different
         ArtifactSubtypeDescriptor artifactType =
               cacheArtifactSubtypeDescriptors.getDescriptor(namespace, artifactTypeName);
         if (!artifactType.getFactoryKey().equals(factoryKey) || !artifactType.getFactory().getClass().getCanonicalName().equals(
               factoryName)) {
            // update factory information
         }
      }
   }

   private InputStreamImageDescriptor getDefaultImageDescriptor(String typeName) {
      if (imageMap == null) {
         imageMap = new HashMap<String, Pair<String, String>>();
         IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
         if (extensionRegistry != null) {
            IExtensionPoint point =
                  extensionRegistry.getExtensionPoint("org.eclipse.osee.framework.skynet.core.ArtifactTypeImage");
            if (point != null) {
               IExtension[] extensions = point.getExtensions();
               for (IExtension extension : extensions) {
                  IConfigurationElement[] elements = extension.getConfigurationElements();
                  String artifact = null;
                  String path = null;
                  String bundle = null;
                  for (IConfigurationElement el : elements) {
                     if (el.getName().equals("ArtifactImage")) {
                        artifact = el.getAttribute("ArtifactTypeName");
                        path = el.getAttribute("ImagePath");
                        bundle = el.getContributor().getName();
                        imageMap.put(artifact, new Pair<String, String>(bundle, path));
                     }
                  }
               }
            }
         }
      }
      InputStreamImageDescriptor imageDescriptor;
      try {
         Pair<String, String> imagelocation = imageMap.get(typeName);
         if (imagelocation == null) {
            logger.log(Level.WARNING, "No image was defined for art type [" + typeName + "]");
            imagelocation = defaultIconLocation;
         }
         URL url = getUrl(imagelocation);
         if (url == null) {
            logger.log(Level.WARNING,
                  "Unable to get url for type [" + typeName + "] bundle path " + imagelocation.getValue());
            url = getUrl(defaultIconLocation);
         }
         imageDescriptor = new InputStreamImageDescriptor(url.openStream());
      } catch (Exception ex) {
         logger.log(Level.SEVERE, "Icon for Artifact type " + typeName + " not found.", ex);
         imageDescriptor = new InputStreamImageDescriptor(new byte[0]);
      }

      return imageDescriptor;
   }

   private URL getUrl(Pair<String, String> location) {
      return Platform.getBundle(location.getKey()).getEntry(location.getValue());
   }

   public static void updateArtifactTypeImage(ArtifactSubtypeDescriptor descriptor, InputStreamImageDescriptor imageDescriptor) throws SQLException {
      // Update DB
      ConnectionHandler.runPreparedUpdate("UPDATE " + ARTIFACT_TYPE_TABLE + " SET image = ? where art_type_id = ?",
            SQL3DataType.BLOB, new ByteArrayInputStream(imageDescriptor.getData()), SQL3DataType.INTEGER,
            descriptor.getArtTypeId());

      // TODO Update descriptor's cached copy of image
      descriptor.setImageDescriptor(imageDescriptor);
   }

   /**
    * Persists that a particular user defined attribute is valid for some artifact type.
    * 
    * @param attributeType
    * @param artifactType
    * @throws Exception
    */
   public void persistAttributeValidity(ArtifactSubtypeDescriptor artifactType, DynamicAttributeDescriptor attributeType) throws Exception {
      if (!cacheAttributeTypeValidity.isValid(artifactType, attributeType)) {
         ConnectionHandler.runPreparedUpdate(INSERT_VALID_ATTRIBUTE, SQL3DataType.INTEGER, artifactType.getArtTypeId(),
               SQL3DataType.INTEGER, attributeType.getAttrTypeId());

         cacheAttributeTypeValidity.add(artifactType, attributeType);
      }
   }

   public Collection<ArtifactSubtypeDescriptor> getArtifactTypesFromAttributeType(DynamicAttributeDescriptor attributeType) throws SQLException {
      return cacheAttributeTypeValidity.getArtifactTypesFromAttributeType(attributeType);
   }

   public Collection<DynamicAttributeDescriptor> getAttributeTypesFromArtifactType(String artifactTypeName, Branch branch) throws SQLException {
      return cacheAttributeTypeValidity.getAttributeTypesFromArtifactType(
            getArtifactSubtypeDescriptor(artifactTypeName), branch);
   }

   public Collection<DynamicAttributeDescriptor> getAttributeTypesFromArtifactType(ArtifactSubtypeDescriptor descriptor, Branch branch) throws SQLException {
      return cacheAttributeTypeValidity.getAttributeTypesFromArtifactType(descriptor, branch);
   }

   public Collection<ArtifactSubtypeDescriptor> getValidArtifactTypes(Branch branch) throws SQLException {
      return artifactTypeValidityCache.getValidArtifactTypes(branch);
   }

   public ArtifactSubtypeDescriptor getArtifactSubtypeDescriptor(String name) throws SQLException {
      return cacheArtifactSubtypeDescriptors.getDescriptor(name);
   }

   public boolean artifactTypeExists(String namespace, String name) throws SQLException {
      return cacheArtifactSubtypeDescriptors.typeExists(namespace, name);
   }

   @Deprecated
   public Collection<ArtifactSubtypeDescriptor> getArtifactSubtypeDescriptors() throws SQLException {
      return cacheArtifactSubtypeDescriptors.getAllDescriptors();
   }

   public DynamicAttributeDescriptor getDynamicAttributeType(int attrTypeId) throws SQLException {
      return cacheDynamicAttributeDescriptors.getDescriptor(attrTypeId);
   }

   public DynamicAttributeDescriptor getDynamicAttributeType(String attributeName) throws SQLException {
      return cacheDynamicAttributeDescriptors.getDescriptor(attributeName);
   }

   public Collection<DynamicAttributeDescriptor> getDynamicAttributeDescriptors(Branch branch) throws SQLException {
      return cacheDynamicAttributeDescriptors.getAllDescriptors(branch);
   }

   public Set<String> getValidEnumerationAttributeValues(String attributeName, Branch branch) {
      Set<String> names = new HashSet<String>();
      try {
         DynamicAttributeDescriptor dad =
               ConfigurationPersistenceManager.getInstance().getDynamicAttributeType(attributeName);
         String str = dad.getValidityXml();
         Matcher m = Pattern.compile("<Enum>(.*?)</Enum>").matcher(str);
         while (m.find())
            names.add(m.group(1));
      } catch (Exception ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, "Error getting valid enumeration values", ex);
      }
      return names;
   }

   public void startBatch(Branch branch) throws SQLException {
      // NOTE: the transaction level must be started before getting a SkynetTransaction since it may
      // perform SQL
      ConnectionHandler.startTransactionLevel(this);
      transaction = new SkynetTransaction(branch);
   }

   /**
    * Executes the transaction and then resets the transaction.
    */
   public void executeBatch() throws SQLException {
      checkTransaction();

      try {
         transaction.execute();
         ConnectionHandler.setTransactionLevelAsSuccessful(this);
      } finally {
         ConnectionHandler.endTransactionLevel(this);
         transaction = null;
      }
   }

   private void checkTransaction() {
      if (transaction == null) throw new RuntimeException("Batch has not been started");
   }

   public ArtifactSubtypeDescriptor getArtifactSubtypeDescriptor(int artTypeId) throws SQLException {
      return cacheArtifactSubtypeDescriptors.getDescriptor(artTypeId);
   }

   public IArtifactFactory getFactoryFromId(int factoryId) {
      return artifactFactoryCache.getFactoryFromId(factoryId);
   }

   public IArtifactFactory getFactoryFromName(String factoryName) throws IllegalStateException {
      return artifactFactoryCache.getFactoryFromName(factoryName);
   }
}
