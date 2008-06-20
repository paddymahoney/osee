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
package org.eclipse.osee.framework.ui.skynet.artifact.snapshot;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;

/**
 * @author Roberto E. Escobar
 */
class ArtifactSnapshotPersistOperation implements Runnable {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(ArtifactSnapshotPersistOperation.class);

   private final ArtifactSnapshot snapshot;
   private final RemoteSnapshotManager manager;

   protected ArtifactSnapshotPersistOperation(final RemoteSnapshotManager manager, final ArtifactSnapshot snapshot) {
      this.snapshot = snapshot;
      this.manager = manager;
   }

   public void run() {
      long start = System.currentTimeMillis();
      try {
         manager.save(snapshot);
      } catch (Exception ex) {
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
      logger.log(Level.INFO, String.format("Artifact Snapshot Commit to DB Time: [%s] ms.",
            System.currentTimeMillis() - start));
   }
}
