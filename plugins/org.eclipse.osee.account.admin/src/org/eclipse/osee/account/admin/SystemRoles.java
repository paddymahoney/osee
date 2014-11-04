/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.account.admin;

/**
 * @author Roberto E. Escobar
 */
public enum SystemRoles {
   ANONYMOUS,
   ADMINISTRATOR,
   AUTHENTICATED;

   public static final String ROLES_ANONYMOUS = "anonymous";
   public static final String ROLES_ADMINISTRATOR = "administrator";
   public static final String ROLES_AUTHENTICATED = "authenticated";

}