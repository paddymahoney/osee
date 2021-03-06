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
package org.eclipse.osee.ats.client.integration.tests.ats.world.search;

import java.util.Collection;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.util.DemoTestUtil;
import org.eclipse.osee.ats.world.search.MyFavoritesSearchItem;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.junit.Test;

/**
 * Test case for {@link MyFavoritesSearchItem}
 *
 * @author Donald G. Dunne
 */
public class MyFavoritesSearchItemTest {

   @Test
   public void search() {
      MyFavoritesSearchItem search =
         new MyFavoritesSearchItem("Search", AtsClientService.get().getUserService().getCurrentUser());
      Collection<Artifact> results = search.performSearchGetResults();
      DemoTestUtil.assertTypes(results, 3, IAtsTeamWorkflow.class);
   }

}
