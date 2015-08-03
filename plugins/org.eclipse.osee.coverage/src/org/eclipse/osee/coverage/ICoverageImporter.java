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
package org.eclipse.osee.coverage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.coverage.model.CoverageImport;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface ICoverageImporter {

   public String getName();

   public CoverageImport run(IProgressMonitor progressMonitor) throws OseeCoreException;
}