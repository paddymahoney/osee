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
package org.eclipse.osee.ats.navigate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.search.WorldSearchItem;
import org.eclipse.osee.ats.world.search.WorldUISearchItem;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Megumi Telles
 */
public class AtsNavigateQuickSearch extends WorldUISearchItem {
   private String searchStr;
   private boolean includeCompleteCancelled = false;

   /**
    * @param name
    */
   public AtsNavigateQuickSearch(String name) {
      super(name);
   }

   public AtsNavigateQuickSearch(String name, String searchStr) {
      super(name);
      this.searchStr = searchStr;
   }

   public AtsNavigateQuickSearch(String name, String searchStr, boolean includeCompleteCancelled) {
      super(name);
      this.searchStr = searchStr;
      this.includeCompleteCancelled = includeCompleteCancelled;
   }

   /**
    * @param atsNavigateQuickSearch
    */
   public AtsNavigateQuickSearch(AtsNavigateQuickSearch atsNavigateQuickSearch) {
      super(atsNavigateQuickSearch);
      this.searchStr = atsNavigateQuickSearch.getSearchStr();
      this.includeCompleteCancelled = atsNavigateQuickSearch.includeCompleteCancelled;
   }

   /**
    * @return
    */
   private String getSearchStr() {
      return this.searchStr;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.search.WorldUISearchItem#performSearch(org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType)
    */
   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {
      try {
         return getExpectedArtifacts(ArtifactQuery.getArtifactsFromAttributeWithKeywords(AtsUtil.getAtsBranch(),
               searchStr, false, false, false));
      } catch (Exception ex) {
         OseeLog.log("AtsNavigateQuickSearch.performSearch", Level.SEVERE, ex.getMessage(), ex);
      }
      return null;
   }

   private Collection<Artifact> getExpectedArtifacts(List<Artifact> arts) throws OseeCoreException {
      List<Artifact> allArtifacts = new ArrayList<Artifact>();
      for (Artifact art : arts) {
         // only ATS Artifacts
         if (art instanceof StateMachineArtifact) {
            StateMachineArtifact sma = (StateMachineArtifact) art;
            // default excludes canceled/completed
            if (this.includeCompleteCancelled == false) {
               if (!sma.getSmaMgr().isCancelledOrCompleted()) {
                  allArtifacts.add(art);
               }
            } else
               allArtifacts.add(art);
         }
      }
      return allArtifacts;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.search.WorldSearchItem#copy()
    */
   @Override
   public WorldSearchItem copy() {
      return new AtsNavigateQuickSearch(this);
   }

}
