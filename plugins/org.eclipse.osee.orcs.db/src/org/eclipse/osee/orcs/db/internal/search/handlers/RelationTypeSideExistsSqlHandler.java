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
package org.eclipse.osee.orcs.db.internal.search.handlers;

import java.util.List;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.criteria.CriteriaRelationTypeSideExists;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.ObjectType;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;

/**
 * @author Roberto E. Escobar
 */
public class RelationTypeSideExistsSqlHandler extends AbstractRelationSqlHandler<CriteriaRelationTypeSideExists> {

   private String relAlias;
   private String txsAlias;

   @Override
   public void addTables(AbstractSqlWriter writer) {
      super.addTables(writer);
      relAlias = writer.addTable(TableEnum.RELATION_TABLE);
      txsAlias = writer.addTable(TableEnum.TXS_TABLE, ObjectType.RELATION);
   }

   @Override
   public boolean addPredicates(AbstractSqlWriter writer) throws OseeCoreException {
      super.addPredicates(writer);

      RelationTypeSide type = criteria.getType();
      writer.write(relAlias);
      writer.write(".rel_link_type_id = ?");
      writer.addParameter(type.getGuid());

      List<String> aliases = writer.getAliases(TableEnum.ARTIFACT_TABLE);
      String side = type.getSide().isSideA() ? "a" : "b";
      if (!aliases.isEmpty()) {
         writer.writeAndLn();
         int aSize = aliases.size();
         for (int index = 0; index < aSize; index++) {
            String artAlias = aliases.get(index);

            writer.write(relAlias);
            writer.write(".");
            writer.write(side);
            writer.write("_art_id = ");
            writer.write(artAlias);
            writer.write(".art_id");

            if (index + 1 < aSize) {
               writer.writeAndLn();
            }
         }
      }
      writer.writeAndLn();
      writer.write(relAlias);
      writer.write(".gamma_id = ");
      writer.write(txsAlias);
      writer.write(".gamma_id");
      writer.writeAndLn();
      writer.write(writer.getTxBranchFilter(txsAlias));
      return true;
   }

}
