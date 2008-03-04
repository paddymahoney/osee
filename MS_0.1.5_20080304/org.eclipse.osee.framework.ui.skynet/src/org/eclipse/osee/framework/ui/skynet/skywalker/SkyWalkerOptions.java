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
package org.eclipse.osee.framework.ui.skynet.skywalker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.eclipse.mylyn.zest.layouts.LayoutStyles;
import org.eclipse.mylyn.zest.layouts.algorithms.AbstractLayoutAlgorithm;
import org.eclipse.mylyn.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.mylyn.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.mylyn.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.mylyn.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.mylyn.zest.layouts.algorithms.VerticalLayoutAlgorithm;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactSubtypeDescriptor;
import org.eclipse.osee.framework.skynet.core.attribute.ConfigurationPersistenceManager;
import org.eclipse.osee.framework.skynet.core.attribute.DynamicAttributeDescriptor;
import org.eclipse.osee.framework.skynet.core.relation.IRelationLinkDescriptor;
import org.eclipse.osee.framework.skynet.core.relation.RelationLinkGroup;
import org.eclipse.osee.framework.skynet.core.relation.RelationPersistenceManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.skywalker.ISkyWalkerOptionsChangeListener.ModType;
import org.eclipse.osee.framework.ui.skynet.skywalker.RelTypeContentProvider.RelationLinkDescriptorSide;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Donald G. Dunne
 */
public class SkyWalkerOptions {

   private Artifact artifact;
   private int levels = 1;
   private static Map<AbstractLayoutAlgorithm, String> layouts;
   private AbstractLayoutAlgorithm layout;
   protected AbstractLayoutAlgorithm defaultLayout;
   private Map<ArtifactSubtypeDescriptor, Boolean> artTypes;
   private Map<DynamicAttributeDescriptor, Boolean> showAttributes;
   // IRelationLinkDescriptor and RelationLinkDescriptorSide
   private Map<Object, Boolean> relTypes;
   private boolean filterEnabled = true;
   private Set<ISkyWalkerOptionsChangeListener> listeners = new HashSet<ISkyWalkerOptionsChangeListener>();
   public static String RADIAL_DOWN_LAYOUT = "Radial - Down";
   public static String SPRING_LAYOUT = "Spring";
   public static enum LinkName {
      None, Link_Name, Full_Link_Name, Phrasing_A_to_B, Phrasing_B_to_A, Other_Side_Name
   };
   private LinkName linkName = LinkName.Link_Name;

   /**
    * @param parent
    * @param style
    */
   public SkyWalkerOptions() {
      loadLayouts();
      layout = defaultLayout;
   }

   public void addSkyWalkerOptionsChangeListener(ISkyWalkerOptionsChangeListener skyWalkerOptionsChangeListener) {
      listeners.add(skyWalkerOptionsChangeListener);
   }

   public String getExtendedName(Artifact artifact) throws SQLException {
      if (getSelectedShowAttributeTypes().size() == 0)
         return "";
      else {
         StringBuffer sb = new StringBuffer();
         for (DynamicAttributeDescriptor desc : getSelectedShowAttributeTypes()) {
            if (artifact.isAttributeTypeValid(desc.getName()) && artifact.getAttributeManager(desc).getAttributes().size() > 0) sb.append("\n" + artifact.getAttributeManager(
                  desc).getAttributesStr());
         }
         return sb.toString();
      }
   }

   private void loadArtTypes() {
      if (artTypes == null) {
         artTypes = new HashMap<ArtifactSubtypeDescriptor, Boolean>();
         try {
            for (ArtifactSubtypeDescriptor descriptor : ConfigurationPersistenceManager.getInstance().getValidArtifactTypes(
                  artifact.getBranch())) {
               artTypes.put(descriptor, true);
            }
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, false);
         }
      }
   }

   private void loadAttributeTypes() {
      if (showAttributes == null) {
         showAttributes = new HashMap<DynamicAttributeDescriptor, Boolean>();
         try {
            for (DynamicAttributeDescriptor descriptor : ConfigurationPersistenceManager.getInstance().getDynamicAttributeDescriptors(
                  artifact.getBranch())) {
               showAttributes.put(descriptor, false);
            }
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, false);
         }
      }
   }

   private void loadRelTypes() {
      if (relTypes == null) {
         relTypes = new HashMap<Object, Boolean>();
         try {
            for (IRelationLinkDescriptor descriptor : RelationPersistenceManager.getInstance().getIRelationLinkDescriptors(
                  artifact.getBranch())) {
               relTypes.put(descriptor, true);
               relTypes.put(new RelTypeContentProvider.RelationLinkDescriptorSide(descriptor, true), true);
               relTypes.put(new RelTypeContentProvider.RelationLinkDescriptorSide(descriptor, false), true);
            }
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, false);
         }
      }
   }

   public String toXml() {
      StringBuffer sb = new StringBuffer();
      sb.append(AXml.addTagData("guid", artifact.getGuid()));
      sb.append(AXml.addTagData("branchId", artifact.getBranch().getBranchId() + ""));
      sb.append(AXml.addTagData("artTypes", org.eclipse.osee.framework.jdk.core.util.Collections.toString(",",
            getSelectedArtTypes())));
      sb.append(AXml.addTagData("relTypes", org.eclipse.osee.framework.jdk.core.util.Collections.toString(",",
            getSelectedRelTypes())));
      sb.append(AXml.addTagData("showAttributes", org.eclipse.osee.framework.jdk.core.util.Collections.toString(",",
            getSelectedShowAttributeTypes())));
      sb.append(AXml.addTagData("layout", getLayoutName(getLayout())));
      sb.append(AXml.addTagData("levels", getLevels() + ""));
      sb.append(AXml.addTagData("linkName", getLinkName() + ""));
      return sb.toString();
   }

   public void fromXml(String xml) {
      try {
         String guid = AXml.getTagData(xml, "guid");
         if (guid != null && !guid.equals("")) {
            String branchId = AXml.getTagData(xml, "branchId");
            Branch branch = BranchPersistenceManager.getInstance().getBranch(Integer.parseInt(branchId));
            Artifact art = ArtifactPersistenceManager.getInstance().getArtifact(guid, branch);
            if (art != null) {
               setArtifact(art);
            }
         }
      } catch (SQLException ex) {
         OSEELog.logWarning(SkynetGuiPlugin.class, "SkyWalker couldn't find stored artifact via guid", ex, false);
      }
      String artTypeStr = AXml.getTagData(xml, "artTypes");
      if (artTypeStr != null && !artTypeStr.equals("")) {
         for (Entry<ArtifactSubtypeDescriptor, Boolean> desc : artTypes.entrySet()) {
            desc.setValue(false);
         }
         for (String name : artTypeStr.split(",")) {
            for (Entry<ArtifactSubtypeDescriptor, Boolean> desc : artTypes.entrySet()) {
               if (desc.getKey().getName().equals(name)) {
                  desc.setValue(true);
                  break;
               }
            }
         }
      }
      String relTypeStr = AXml.getTagData(xml, "relTypes");
      if (relTypeStr != null && !relTypeStr.equals("")) {
         for (Entry<Object, Boolean> desc : relTypes.entrySet()) {
            desc.setValue(false);
         }
         for (String name : relTypeStr.split(",")) {
            for (Entry<Object, Boolean> desc : relTypes.entrySet()) {
               if (desc.getKey().toString().equals(name)) {
                  desc.setValue(true);
                  break;
               }
            }
         }
      }
      String showAttrString = AXml.getTagData(xml, "showAttributes");
      if (showAttrString != null && !showAttrString.equals("")) {
         for (Entry<DynamicAttributeDescriptor, Boolean> desc : showAttributes.entrySet()) {
            desc.setValue(false);
         }
         for (String name : showAttrString.split(",")) {
            for (Entry<DynamicAttributeDescriptor, Boolean> desc : showAttributes.entrySet()) {
               if (desc.getKey().getName().equals(name)) {
                  desc.setValue(true);
                  break;
               }
            }
         }
      }
      String layoutStr = AXml.getTagData(xml, "layout");
      if (layoutStr != null && !layoutStr.equals("")) {
         for (AbstractLayoutAlgorithm layout : getLayouts()) {
            if (getLayoutName(layout).equals(layoutStr)) {
               setLayout(layout);
               break;
            }
         }
      }
      String levelStr = AXml.getTagData(xml, "levels");
      if (levelStr != null && !levelStr.equals("")) {
         setLevels(Integer.parseInt(levelStr));
      }

      String linkNameStr = AXml.getTagData(xml, "linkName");
      if (linkNameStr != null && !linkNameStr.equals("")) {
         setLinkName(LinkName.valueOf(linkNameStr));
      }

      notifyListeners(ModType.ArtType, ModType.RelType, ModType.Level, ModType.Layout, ModType.Link_Name,
            ModType.Show_Attribute);
   }

   /**
    * @return the artifact
    */
   public Artifact getArtifact() {
      return artifact;
   }

   private Map<AbstractLayoutAlgorithm, String> loadLayouts() {
      if (layouts == null) {
         layouts = new HashMap<AbstractLayoutAlgorithm, String>();

         RadialLayoutAlgorithm radLayout = new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
         radLayout.setRangeToLayout((-90 * Math.PI) / 360, (90 * Math.PI) / 360);
         defaultLayout = radLayout;
         layouts.put(radLayout, "Radial - Right (default)");

         radLayout = new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
         radLayout.setRangeToLayout(0, (180 * Math.PI) / 360);
         layouts.put(radLayout, RADIAL_DOWN_LAYOUT);

         layouts.put(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), "Radial - Full");
         layouts.put(new SpringLayoutAlgorithm(), SPRING_LAYOUT);
         layouts.put(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), "Tree");
         layouts.put(new VerticalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), "Vertical");
         layouts.put(new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), "Grid");
      }
      return layouts;
   }

   /**
    * @return the defaultLayout
    */
   public AbstractLayoutAlgorithm getLayout() {
      return layout;
   }

   public AbstractLayoutAlgorithm getLayout(String layoutName) {
      for (Entry<AbstractLayoutAlgorithm, String> entry : layouts.entrySet()) {
         if (entry.getValue().equals(layoutName)) return entry.getKey();
      }
      return defaultLayout;
   }

   public Set<AbstractLayoutAlgorithm> getLayouts() {
      return layouts.keySet();
   }

   public String getLayoutName(AbstractLayoutAlgorithm layout) {
      return layouts.get(layout);
   }

   /**
    * @param artifact the artifact to set
    */
   public void setArtifact(Artifact artifact) {
      if (this.artifact == artifact) return;
      this.artifact = artifact;
      loadArtTypes();
      loadRelTypes();
      loadAttributeTypes();
      notifyListeners(ModType.Artifact);
   }

   public boolean isValidArtifactType(Artifact artifact) {
      if (!isFilterEnabled()) return true;
      return getSelectedArtTypes().contains(artifact.getDescriptor());
   }

   public boolean isValidRelationLinkGroup(RelationLinkGroup linkGroup) {
      if (!isFilterEnabled()) return true;
      return getSelectedRelTypes().contains(linkGroup.getDescriptor()) || getSelectedRelTypes().contains(
            new RelationLinkDescriptorSide(linkGroup.getDescriptor(), linkGroup.isSideA()));
   }

   /**
    * @return the levels
    */
   public int getLevels() {
      return levels;
   }

   /**
    * @param levels the levels to set
    */
   public void setLevels(int levels) {
      if (this.levels == levels) return;
      this.levels = levels;
      notifyListeners(ModType.Level);
   }

   /**
    * @return the filterByArtType
    */
   public boolean isFilterEnabled() {
      return filterEnabled;
   }

   /**
    * @param filterByArtType the filterByArtType to set
    */
   public void setFilterEnabled(boolean enable) {
      if (this.filterEnabled == enable) return;
      this.filterEnabled = enable;
      notifyListeners(ModType.FilterEnabled);
   }

   private void notifyListeners(ModType... modType) {
      for (ISkyWalkerOptionsChangeListener listener : listeners)
         listener.modified(modType);
   }

   public void setSelectedRelTypes(Object[] selected) {
      List<Object> selList = new ArrayList<Object>();
      for (Object obj : selected)
         selList.add(obj);
      for (Entry<Object, Boolean> entry : relTypes.entrySet()) {
         entry.setValue(selList.contains(entry.getKey()));
      }
      notifyListeners(ModType.RelType);
   }

   public void setSelectedShowAttributes(Object[] selected) {
      List<Object> selList = new ArrayList<Object>();
      for (Object obj : selected)
         selList.add(obj);
      for (Entry<DynamicAttributeDescriptor, Boolean> entry : showAttributes.entrySet()) {
         entry.setValue(selList.contains(entry.getKey()));
      }
      notifyListeners(ModType.Show_Attribute);
   }

   public void setSelectedArtTypes(Collection<ArtifactSubtypeDescriptor> selected) {
      for (Entry<ArtifactSubtypeDescriptor, Boolean> entry : artTypes.entrySet()) {
         entry.setValue(selected.contains(entry.getKey()));
      }
      notifyListeners(ModType.ArtType);
   }

   public Set<ArtifactSubtypeDescriptor> getSelectedArtTypes() {
      Set<ArtifactSubtypeDescriptor> selected = new HashSet<ArtifactSubtypeDescriptor>();
      if (artTypes == null) return selected;
      for (ArtifactSubtypeDescriptor desc : artTypes.keySet())
         if (artTypes.get(desc)) selected.add(desc);
      return selected;
   }

   public Set<Object> getSelectedRelTypes() {
      Set<Object> selected = new HashSet<Object>();
      if (relTypes == null) return selected;
      for (Object desc : relTypes.keySet())
         if (relTypes.get(desc)) selected.add(desc);
      return selected;
   }

   public Set<DynamicAttributeDescriptor> getSelectedShowAttributeTypes() {
      Set<DynamicAttributeDescriptor> selected = new HashSet<DynamicAttributeDescriptor>();
      if (showAttributes == null) return selected;
      for (DynamicAttributeDescriptor desc : showAttributes.keySet())
         if (showAttributes.get(desc)) selected.add(desc);
      return selected;
   }

   public Set<ArtifactSubtypeDescriptor> getAllArtTypes() {
      if (artTypes == null) return new HashSet<ArtifactSubtypeDescriptor>();
      return artTypes.keySet();
   }

   public Set<Object> getAllRelTypes() {
      if (relTypes == null) return new HashSet<Object>();
      return relTypes.keySet();
   }

   public Set<DynamicAttributeDescriptor> getAllShowAttributes() {
      if (showAttributes == null) return new HashSet<DynamicAttributeDescriptor>();
      return showAttributes.keySet();
   }

   public Set<IRelationLinkDescriptor> getAllIRelationLinkDescriptorTypes() {
      if (relTypes == null) return new HashSet<IRelationLinkDescriptor>();
      Set<IRelationLinkDescriptor> descs = new HashSet<IRelationLinkDescriptor>();
      for (Object obj : relTypes.keySet()) {
         if (obj instanceof IRelationLinkDescriptor) descs.add((IRelationLinkDescriptor) obj);
      }
      return descs;
   }

   /**
    * @param layout the layout to set
    */
   public void setLayout(AbstractLayoutAlgorithm layout) {
      if (this.layout == layout) return;
      this.layout = layout;
      notifyListeners(ModType.Layout);
   }

   /**
    * @return the linkName
    */
   public LinkName getLinkName() {
      return linkName;
   }

   /**
    * @param linkName the linkName to set
    */
   public void setLinkName(LinkName linkName) {
      if (this.linkName == linkName) return;
      this.linkName = linkName;
      notifyListeners(ModType.Link_Name);
   }

   /**
    * @return the defaultLayout
    */
   public AbstractLayoutAlgorithm getDefaultLayout() {
      return defaultLayout;
   }

   /**
    * @param defaultLayout the defaultLayout to set
    */
   public void setDefaultLayout(AbstractLayoutAlgorithm defaultLayout) {
      this.defaultLayout = defaultLayout;
   }

}
