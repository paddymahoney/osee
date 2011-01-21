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
package org.eclipse.osee.ats.navigate;

import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.workdef.AtsWorkDefinitionProviders;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;

public class ConvertAIsAndTeamDefinitions extends XNavigateItemAction {

   public ConvertAIsAndTeamDefinitions(XNavigateItem parent) {
      super(parent, "Convert AIs and Team Definition to AtsDsl", AtsImage.WORK_DEFINITION);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws Exception {
      XResultData resultData = new XResultData();
      AtsWorkDefinitionProviders.convertAndOpenAIandTeamAtsDsl(resultData);
      resultData.report(getName());
   }

}
