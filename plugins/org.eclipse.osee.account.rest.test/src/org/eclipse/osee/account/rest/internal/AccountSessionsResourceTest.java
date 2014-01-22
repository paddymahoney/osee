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
package org.eclipse.osee.account.rest.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.account.rest.model.AccountAccessData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Test Case for {@link AccountSessionsResource}
 * 
 * @author Roberto E. Escobar
 */
public class AccountSessionsResourceTest {

   private static final String ACCOUNT_ID = "hello@hello.com";

   //@formatter:off
   @Mock private AccountOps accountOps;
   //@formatter:on

   private AccountSessionsResource resource;

   @Before
   public void setUp() {
      initMocks(this);

      resource = new AccountSessionsResource(accountOps, ACCOUNT_ID);
   }

   @Test
   public void testGetAccountSessions() {
      List<AccountAccessData> accesses = new ArrayList<AccountAccessData>();
      when(accountOps.getAccountAccessById(ACCOUNT_ID)).thenReturn(accesses);

      List<AccountAccessData> actual = resource.getAccountSessions();

      assertEquals(accesses, actual);
      verify(accountOps).getAccountAccessById(ACCOUNT_ID);
   }
}