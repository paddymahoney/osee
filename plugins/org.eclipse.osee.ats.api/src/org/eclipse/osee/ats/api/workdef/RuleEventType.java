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
package org.eclipse.osee.ats.api.workdef;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Mark Joy
 */
@XmlRootElement
public enum RuleEventType {

   None,
   CreateBranch,
   CommitBranch,
   CreateWorkflow,
   CompleteWorkflow,
   TransitionTo,
   Manual
}
