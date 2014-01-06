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
package org.eclipse.osee.ats.impl.action;

import org.eclipse.osee.ats.impl.action.ActionUtility.ActionLoadLevel;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Donald G. Dunne
 */
public interface IWorkItemPage {

   String getHtml(ArtifactReadable action, String title, ActionLoadLevel actionLoadLevel) throws Exception;

   String getHtmlWithStates(ArtifactReadable action, String title, ActionLoadLevel actionLoadLevel) throws Exception;

}