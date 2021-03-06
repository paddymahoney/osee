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
package org.eclipse.osee.framework.core.dsl.integration.mocks;

import org.eclipse.osee.framework.core.dsl.integration.ArtifactDataProvider.ArtifactProxy;
import org.eclipse.osee.framework.core.dsl.integration.RestrictionHandler;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ObjectRestriction;
import org.eclipse.osee.framework.core.model.access.AccessDetailCollector;
import org.eclipse.osee.framework.core.model.access.Scope;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public class MockRestrictionHandler implements RestrictionHandler<Object> {

   private final ObjectRestriction expectedObjectRestriction;
   private final ArtifactProxy expectedArtifactProxy;
   private final AccessDetailCollector expectedCollector;
   private boolean wasProcessCalled;

   public MockRestrictionHandler(ObjectRestriction expectedObjectRestriction, ArtifactProxy expectedArtifactProxy, AccessDetailCollector expectedCollector) {
      super();
      this.expectedObjectRestriction = expectedObjectRestriction;
      this.expectedArtifactProxy = expectedArtifactProxy;
      this.expectedCollector = expectedCollector;
      reset();
   }

   @Override
   public Object asCastedObject(ObjectRestriction objectRestriction) {
      return null;
   }

   public void reset() {
      wasProcessCalled = false;
   }

   public boolean wasProcessCalled() {
      return wasProcessCalled;
   }

   @Override
   public void process(ObjectRestriction objectRestriction, ArtifactProxy artifactProxy, AccessDetailCollector collector, Scope scopeLevel) {
      wasProcessCalled = true;
      Assert.assertEquals(expectedObjectRestriction, objectRestriction);
      Assert.assertEquals(expectedArtifactProxy, artifactProxy);
      Assert.assertEquals(expectedCollector, collector);
   }
}