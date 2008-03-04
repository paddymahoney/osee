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
package org.eclipse.osee.define.blam.operation;

import java.util.logging.Logger;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;

/**
 * @author Jeff C. Phillips
 */
public class CheckValidAttributeTypes extends CheckValidType {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(CheckValidAttributeTypes.class);
   private static final String CHECK_SQL =
         "SELECT t2.*, t4.*, t10.guid, t10.human_readable_id, t10.art_type_id " + "FROM osee_define_attribute t2, " + "osee_define_txs t3, " + "osee_define_tx_details t4, " + "osee_define_artifact t10 " + "WHERE t2.gamma_id = t3.gamma_id " + "and t10.art_id = t2.art_id " + "AND t3.transaction_id = t4.transaction_id " + "AND NOT EXISTS " + "(SELECT 'x' " + "FROM osee_define_attribute_type t5, " + "osee_define_txs t6, " + "osee_define_tx_details t7 " + "WHERE t5.gamma_id = t6.gamma_id " + "AND t6.transaction_id = t7.transaction_id " + "AND t4.branch_id = t7.branch_id " + "AND t2.attr_type_id = t5.attr_type_id) order by attr_id";

   /**
    * @param sql
    * @param headers
    * @param colNames
    * @param logger
    */
   public CheckValidAttributeTypes() {
      super(CHECK_SQL, new String[] {"art_id", "attr_id", "attr_type_id", "gamma_id", "transaction_id", "branch_id"},
            new String[] {"Art ID", "Attr ID", "Art Type ID", "Gamma ID", "Transaction ID", "Branch ID"}, logger);
   }
}
