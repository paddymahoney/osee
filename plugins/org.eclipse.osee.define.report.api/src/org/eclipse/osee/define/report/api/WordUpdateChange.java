/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.define.report.api;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author David W. Miller
 */
@XmlRootElement
public class WordUpdateChange {
   Integer tx;
   Long branchUuid;
   List<WordArtifactChange> changedArts = new LinkedList<WordArtifactChange>();

   public Integer getTx() {
      return tx;
   }

   public void setTx(Integer tx) {
      this.tx = tx;
   }

   public Long getBranchUuid() {
      return branchUuid;
   }

   public void setBranchUuid(Long branchUuid) {
      this.branchUuid = branchUuid;
   }

   public List<WordArtifactChange> getChangedArts() {
      return changedArts;
   }

   public void setChangedArts(List<WordArtifactChange> changedArts) {
      this.changedArts = changedArts;
   }

   public void addChangedArt(WordArtifactChange change) {
      this.changedArts.add(change);
   }

   public WordArtifactChange getWordArtifactChange(long artId) {
      WordArtifactChange toReturn = null;
      if (changedArts != null && !changedArts.isEmpty()) {
         for (WordArtifactChange change : changedArts) {
            if (change.artId == artId) {
               toReturn = change;
               break;
            }
         }
      }
      return toReturn;
   }

   public boolean hasSafetyRelatedArtifactChange() {
      boolean hasRelatedChange = false;
      for (WordArtifactChange change : changedArts) {
         if (change.isSafetyRelated()) {
            hasRelatedChange = true;
            break;
         }
         if (change.isCreated()) {
            hasRelatedChange = true;
            break;
         }
      }
      return hasRelatedChange;
   }
}