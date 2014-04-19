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
package org.eclipse.osee.disposition.rest.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.eclipse.osee.disposition.model.Discrepancy;
import org.eclipse.osee.disposition.model.DispoItem;
import org.eclipse.osee.disposition.model.DispoStrings;
import org.eclipse.osee.disposition.rest.internal.LocationRangesCompressor;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Angel
 */
public final class DispoHtmlOutputStream implements StreamingOutput {
   private final Iterable<DispoItem> dispoItems;

   public DispoHtmlOutputStream(Iterable<DispoItem> dispoItems) {
      this.dispoItems = dispoItems;
   }

   @Override
   public void write(OutputStream output) throws IOException {
      Writer writer = new OutputStreamWriter(output);
      try {

         for (DispoItem item : dispoItems) {
            writer.append("<tr id=\"");
            writer.append(item.getGuid());
            if (item.getStatus().equals(DispoStrings.Item_Pass)) {
               writer.append("\" class=\"itemRow\">");
            } else {
               writer.append("\" class=\"itemRow\" ondblclick=\"showAnnotations(this)\">");
            }
            addItemData(writer, item);
            writer.append("</tr>");

            writer.append("\n");
         }

      } catch (Exception ex) {
         throw new WebApplicationException(ex);
      } finally {
         writer.close();
      }
   }

   private void addItemData(Appendable appendable, DispoItem item) throws IOException, JSONException {
      addData(appendable, item.getName());
      addData(appendable, item.getStatus(), true);
      addData(appendable, String.valueOf(item.getDiscrepanciesList().length())); // change to total points
      addData(appendable, String.valueOf(item.getDiscrepanciesList().length()));
      addData(appendable, String.valueOf(getFailureLocations(item.getDiscrepanciesList())));
      addData(appendable, item.getAssignee());
      addData(appendable, item.getVersion()); // Change to Version
   }

   private void addData(Appendable appendable, String data) throws IOException {
      appendable.append("<td class=\"itemData\">");
      appendable.append(data);
      appendable.append("</td>");
   }

   private void addData(Appendable appendable, String data, boolean isStatus) throws IOException {
      if (data.equals(DispoStrings.Item_Complete)) {
         appendable.append("<td class=\"itemData\">");
      } else if (data.equals(DispoStrings.Item_Pass)) {
         appendable.append("<td class=\"itemDataPass\">");
      } else {
         appendable.append("<td class=\"itemDataFail\">");
      }
      appendable.append(data);
      appendable.append("</td>");
   }

   private String getFailureLocations(JSONObject discrepanciesList) throws JSONException {
      List<Integer> locations = new ArrayList<Integer>();
      @SuppressWarnings("unchecked")
      Iterator<String> iterator = discrepanciesList.keys();
      while (iterator.hasNext()) {
         String key = iterator.next();
         JSONObject discrepancyAsJson = discrepanciesList.getJSONObject(key);
         Discrepancy discrepany = DispoUtil.jsonObjToDiscrepancy(discrepancyAsJson);

         locations.add(discrepany.getLocation());
      }

      return LocationRangesCompressor.compress(locations);
   }

}