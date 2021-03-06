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
package org.eclipse.osee.ats.api.agile;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.osee.ats.api.config.JaxAtsObject;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class JaxAgileTeam extends JaxAtsObject {

   private List<Long> atsTeamUuids = new ArrayList<>();
   private long backlogUuid = 0;
   private String description = "";

   public List<Long> getAtsTeamUuids() {
      return atsTeamUuids;
   }

   public void setAtsTeamUuids(List<Long> atsTeamUuids) {
      this.atsTeamUuids = atsTeamUuids;
   }

   public long getBacklogUuid() {
      return backlogUuid;
   }

   public void setBacklogUuid(long backlogUuid) {
      this.backlogUuid = backlogUuid;
   }

   @Override
   public String getDescription() {
      return description;
   }

   @Override
   public void setDescription(String description) {
      this.description = description;
   }

}
