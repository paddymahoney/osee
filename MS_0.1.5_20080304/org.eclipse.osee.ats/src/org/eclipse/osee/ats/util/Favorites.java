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

import java.sql.SQLException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.IFavoriteableArtifact;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class Favorites {

   private final StateMachineArtifact sma;

   public Favorites(StateMachineArtifact sma) {
      super();
      this.sma = sma;
   }

   public void toggleFavorite() {
      try {
         if (((IFavoriteableArtifact) sma).amIFavorite()) {
            boolean result =
                  MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "Remove Favorite", "Are You sure you wish to remove this as Favorite?");
            if (result) ((IFavoriteableArtifact) sma).removeFavorite(SkynetAuthentication.getInstance().getAuthenticatedUser());
         } else {
            boolean result =
                  MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "Favorite", "Are you sure you wish add this as a Favorite?");
            if (result) ((IFavoriteableArtifact) sma).addFavorite(SkynetAuthentication.getInstance().getAuthenticatedUser());
         }
      } catch (SQLException ex) {
         OSEELog.logException(AtsPlugin.class, ex, true);
      }
   }

}
