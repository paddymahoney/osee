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
package org.eclipse.osee.ats.util;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.IFavoriteableArtifact;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class FavoritesManager {

   private final Collection<StateMachineArtifact> smas;

   public FavoritesManager(StateMachineArtifact sma) {
      this(Arrays.asList(sma));
   }

   public FavoritesManager(Collection<StateMachineArtifact> smas) {
      super();
      this.smas = smas;
   }

   public void toggleFavorite() {
      toggleFavorite(true);
   }

   public void toggleFavorite(boolean prompt) {
      try {
         if (((IFavoriteableArtifact) smas.iterator().next()).amIFavorite()) {
            boolean result = true;
            if (prompt) result =
                  MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "Remove Favorite", "Are You sure you wish to remove this as Favorite?");
            if (result) {
               SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch());
               for (StateMachineArtifact sma : smas) {
                  ((IFavoriteableArtifact) sma).removeFavorite(UserManager.getUser(), transaction);
               }
               transaction.execute();
            }
         } else {
            boolean result = true;
            if (prompt) result =
                  MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "Favorite", "Are you sure you wish add this as a Favorite?");
            if (result) {
               SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch());
               for (StateMachineArtifact sma : smas) {
                  ((IFavoriteableArtifact) sma).addFavorite(UserManager.getUser(), transaction);
               }
               transaction.execute();
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }
}
