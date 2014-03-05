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
package org.eclipse.osee.account.admin.internal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.account.admin.AccountField;
import org.eclipse.osee.account.admin.ds.AccountStorage;
import org.eclipse.osee.account.admin.internal.validator.DisplayNameValidator;
import org.eclipse.osee.account.admin.internal.validator.EmailValidator;
import org.eclipse.osee.account.admin.internal.validator.FieldValidator;
import org.eclipse.osee.account.admin.internal.validator.LocalIdValidator;
import org.eclipse.osee.account.admin.internal.validator.UserNameValidator;
import org.eclipse.osee.account.admin.internal.validator.UuidValidator;

/**
 * @author Roberto E. Escobar
 */
public final class Validators {

   private Validators() {
      // 
   }

   public static Map<AccountField, FieldValidator> newValidators(AccountStorage storage) {
      Map<AccountField, FieldValidator> data = new HashMap<AccountField, FieldValidator>();
      addValidator(data, new UuidValidator());
      addValidator(data, new LocalIdValidator());
      addValidator(data, new DisplayNameValidator(storage));
      addValidator(data, new EmailValidator(storage));
      addValidator(data, new UserNameValidator(storage));
      return data;
   }

   private static void addValidator(Map<AccountField, FieldValidator> validators, FieldValidator toAdd) {
      validators.put(toAdd.getFieldType(), toAdd);
   }

   public static Comparator<FieldValidator> VALIDATOR_PRIORITY_ORDER_COMPARATOR = new Comparator<FieldValidator>() {

      @Override
      public int compare(FieldValidator o1, FieldValidator o2) {
         Integer priority1 = o1.getPriority();
         Integer priority2 = o2.getPriority();
         return priority1.compareTo(priority2);
      }
   };

}