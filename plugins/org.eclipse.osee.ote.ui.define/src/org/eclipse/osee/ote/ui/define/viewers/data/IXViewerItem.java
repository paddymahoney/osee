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
package org.eclipse.osee.ote.ui.define.viewers.data;

import org.eclipse.swt.graphics.Image;

/**
 * @author Roberto E. Escobar
 */
public interface IXViewerItem {

   public abstract Image getImage();

   public abstract String getLabel(int index);

}
