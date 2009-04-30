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
package org.eclipse.osee.ats.test.config;

import junit.framework.TestCase;
import org.eclipse.osee.ats.workflow.item.AtsWorkDefinitions;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayout;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinitionFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkWidgetDefinition;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkItemDefinitionTest extends TestCase {

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception {
      super.setUp();
   }

   public void testWorkItemDefinitions() throws Exception {
      for (WorkItemDefinition workItemDefinition : WorkItemDefinitionFactory.getWorkItemDefinitions()) {
         System.out.println("Testing " + workItemDefinition);
         // set up dynamic layout for work widget definition
         if (workItemDefinition instanceof WorkWidgetDefinition) {
            WorkWidgetDefinition workWidgetDefinition = (WorkWidgetDefinition) workItemDefinition;
            DynamicXWidgetLayoutData dynamicXWidgetLayoutData = workWidgetDefinition.get();
            if (dynamicXWidgetLayoutData.getDynamicXWidgetLayout() == null) {
               dynamicXWidgetLayoutData.setDynamicXWidgetLayout(new DynamicXWidgetLayout());
               workWidgetDefinition.set(dynamicXWidgetLayoutData);
            }
         }
         Result result = AtsWorkDefinitions.validateWorkItemDefinition(workItemDefinition);
         if (result.isFalse()) {
            fail(result.getText());
         }
      }
   }

}
