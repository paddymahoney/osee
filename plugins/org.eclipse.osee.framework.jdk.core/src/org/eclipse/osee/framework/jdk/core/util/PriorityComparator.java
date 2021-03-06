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
package org.eclipse.osee.framework.jdk.core.util;

import java.util.Comparator;
import org.eclipse.osee.framework.jdk.core.type.HasPriority;

/**
 * @author Roberto E. Escobar
 */
public class PriorityComparator implements Comparator<HasPriority> {

   @Override
   public int compare(HasPriority o1, HasPriority o2) {
      return o1.getPriority() - o2.getPriority();
   }
}
