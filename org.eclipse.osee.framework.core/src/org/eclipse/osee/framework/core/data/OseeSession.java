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
package org.eclipse.osee.framework.core.data;

import java.io.InputStream;
import java.sql.Timestamp;
import org.eclipse.osee.framework.db.connection.exception.OseeWrappedException;

/**
 * @author Roberto E. Escobar
 */
public class OseeSession extends OseeClientInfo {

   private static final long serialVersionUID = 8144856081780854567L;
   private static final String SESSION_ID = "sessionId";
   private static final String USER_ID = "userId";
   private static final String CREATED_ON = "createdOn";
   private static final String LAST_INTERACTION_DATE = "lastInteractionDate";
   private static final String LAST_INTERACTION = "lastInteraction";

   private OseeSession() {
      super();
   }

   public OseeSession(String sessionId, String userId, Timestamp createdOn, String machineName, String machineIp, int port, String clientVersion, Timestamp lastInteractionDate, String lastInteractionName) {
      super(clientVersion, machineName, machineIp, port);
      this.properties.put(SESSION_ID, sessionId);
      this.properties.put(USER_ID, userId);
      this.properties.put(CREATED_ON, Long.toString(createdOn.getTime()));
      this.properties.put(LAST_INTERACTION_DATE, Long.toString(lastInteractionDate.getTime()));
      this.properties.put(LAST_INTERACTION, lastInteractionName);
   }

   /**
    * @return the userId
    */
   public String getUserId() {
      return getString(USER_ID);
   }

   /**
    * @return the session id
    */
   public String getSessionId() {
      return getString(SESSION_ID);
   }

   /**
    * @return the session creation date
    */
   public Timestamp getCreation() {
      return new Timestamp(Long.valueOf(getString(CREATED_ON)));
   }

   /**
    * @return the last task performed/requested
    */
   public String getLastInteraction() {
      return getString(LAST_INTERACTION);
   }

   /**
    * @return the last communication timestamp
    */
   public Timestamp getLastInteractionDate() {
      return new Timestamp(Long.valueOf(getString(LAST_INTERACTION_DATE)));
   }

   /**
    * Set the last interaction name
    * 
    * @param lastInteractionName
    */
   public void setLastInteraction(String lastInteractionName) {
      this.properties.put(LAST_INTERACTION, lastInteractionName);
   }

   /**
    * Set the last interaction date
    * 
    * @param timestamp
    */
   public void setLastInteractionDate(Timestamp timestamp) {
      this.properties.put(LAST_INTERACTION_DATE, Long.toString(timestamp.getTime()));
   }

   /**
    * Get a the session id and version in a single string
    * 
    * @param the session id and version
    */
   public String getSessionIdAndVersion() {
      return String.format("%s - %s", getSessionId(), getVersion());
   }

   /**
    * Create new instance from XML input
    * 
    * @param OseeSession the new instance
    * @throws OseeWrappedException
    */
   public static OseeSession fromXml(InputStream inputStream) throws OseeWrappedException {
      OseeSession session = new OseeSession();
      session.loadfromXml(inputStream);
      return session;
   }

}
