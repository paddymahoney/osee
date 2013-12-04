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

package org.eclipse.osee.disposition.model;

import java.util.Date;
import org.eclipse.osee.framework.jdk.core.type.Identifiable;
import org.json.JSONArray;

/**
 * @author Angel Avila
 */

public interface DispoItem extends Identifiable<String> {

   String getAssignee();

   Date getCreationDate();

   Date getLastUpdate();

   String getStatus();

   JSONArray getDiscrepanciesList();

   JSONArray getAnnotationsList();
}