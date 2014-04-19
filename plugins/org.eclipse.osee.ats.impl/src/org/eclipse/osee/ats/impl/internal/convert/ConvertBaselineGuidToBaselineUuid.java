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
package org.eclipse.osee.ats.impl.internal.convert;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.util.IAtsDatabaseConversion;
import org.eclipse.osee.ats.impl.internal.util.AtsUtilServer;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.transaction.TransactionFactory;

/**
 * See description below
 * 
 * @author Donald G Dunne
 */
public class ConvertBaselineGuidToBaselineUuid implements IAtsDatabaseConversion {

   private final OrcsApi orcsApi;
   // Leave this attribute definition and conversion for other OSEE sites to convert
   public static final IAttributeType BaselineBranchGuid = TokenFactory.createAttributeType(0x10000000000000A9L,
      "ats.Baseline Branch Guid");
   private final IOseeDatabaseService dbService;

   public ConvertBaselineGuidToBaselineUuid(IOseeDatabaseService dbService, OrcsApi orcsApi) {
      this.dbService = dbService;
      this.orcsApi = orcsApi;
   }

   @Override
   public void run(XResultData data, boolean reportOnly) {
      if (reportOnly) {
         data.log("REPORT ONLY - Changes not persisted\n");
      }
      if (!orcsApi.getOrcsTypes(null).getAttributeTypes().exists(AtsAttributeTypes.BaselineBranchUuid)) {
         data.logError("ats.BaselineBranchUuid is not configured for this database");
         return;
      }
      TransactionFactory txFactory = orcsApi.getTransactionFactory(null);
      TransactionBuilder tx =
         txFactory.createTransaction(COMMON, AtsUtilServer.getArtifactByGuid(orcsApi, SystemUser.OseeSystem.getGuid()),
            getName());
      int numChanges = 0;
      for (ArtifactReadable art : orcsApi.getQueryFactory(null).fromBranch(AtsUtilServer.getAtsBranch()).andTypeEquals(
         AtsArtifactTypes.Version, AtsArtifactTypes.TeamDefinition).andExists(BaselineBranchGuid).getResults()) {
         List<String> attributeValues = art.getAttributeValues(BaselineBranchGuid);
         for (String guid : attributeValues) {
            BranchReadable branch = null;
            try {
               branch =
                  orcsApi.getQueryFactory(null).branchQuery().andUuids(getBranchIdLegacy(guid)).getResults().getExactlyOne();
            } catch (Exception ex) {
               // do nothing
            }
            if (branch == null) {
               data.logErrorWithFormat("Branch with guid %s can't be found", guid);
            } else {
               String uuid = art.getSoleAttributeAsString(AtsAttributeTypes.BaselineBranchUuid, null);
               if (uuid == null || !Long.valueOf(uuid).equals(branch.getLocalId())) {
                  if (uuid == null) {
                     data.logWithFormat("Adding uuid attribute of value %d to artifact type [%s] name [%s] id [%s]\n",
                        branch.getLocalId(), art.getArtifactType(), art.getName(), art.getGuid());
                  } else if (!Long.valueOf(uuid).equals(branch.getLocalId())) {
                     data.logWithFormat(
                        "Updating uuid attribute of value %d to artifact type [%s] name [%s] id [%s]\n",
                        branch.getLocalId(), art.getArtifactType(), art.getName(), art.getGuid());
                  }
                  numChanges++;
                  if (!reportOnly) {
                     tx.setSoleAttributeValue(art, AtsAttributeTypes.BaselineBranchUuid,
                        String.valueOf(branch.getLocalId()));
                  }
               }
            }
         }
      }
      if (!reportOnly) {
         data.log("\n" + numChanges + " Changes Persisted");
         tx.commit();
      } else {
         data.log("\n" + numChanges + " Need to be Changed");
      }
   }

   private final String SELECT_BRANCH_ID_BY_GUID = "select branch_id from osee_branch where branch_guid = ?";

   /**
    * Temporary method till all code uses branch uuid. Remove after 0.17.0
    */
   public long getBranchIdLegacy(String branchGuid) {
      Long longId = dbService.runPreparedQueryFetchObject(0L, SELECT_BRANCH_ID_BY_GUID, branchGuid);
      Conditions.checkExpressionFailOnTrue(longId <= 0, "Error getting branch_id for branch: [%s]", branchGuid);
      return longId;
   }

   @Override
   public String getDescription() {
      StringBuffer data = new StringBuffer();
      data.append("ConvertBaselineGuidToBaselineUuid (required conversion)\n\n");
      data.append("Necessary for upgrading from OSEE 0.16.2 to 0.17.0");
      data.append("- Verify that ats.BaselineBranchUuid is a valid attribute type\n");
      data.append("- Verify Add uuid attribute for every ats.BaselineBranchGuid attribute on Version artifacts\n");
      data.append("- Verify Add uuid attribute for every ats.BaselineBranchGuid attribute on Team Definition artifacts\n\n");
      data.append("NOTE: This operation can be run multiple times\n");
      data.append("Manual Cleanup (optional): Use Purge Attribute Type BLAM to remove the ats.BaselineBranchGuid attributes.");
      return data.toString();
   }

   @Override
   public String getName() {
      return "ConvertBaselineGuidToBaselineUuid";
   }
}