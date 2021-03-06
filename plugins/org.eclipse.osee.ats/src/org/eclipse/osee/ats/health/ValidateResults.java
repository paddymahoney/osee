/*
 * Created on Nov 12, 2013
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.health;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.CountingMap;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.type.MutableInteger;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class ValidateResults {

   private final CountingMap<String> testNameToTimeSpentMap = new CountingMap<>();
   private final HashCollection<String, String> testNameToResultsMap = new HashCollection<>(50);
   private final HashCollection<String, String> testNameToGuidMap = new HashCollection<>(50);

   public void logTestTimeSpent(Date date, String testName) {
      Date now = new Date();
      int spent = new Long(now.getTime() - date.getTime()).intValue();
      testNameToTimeSpentMap.put(testName, spent);
   }

   public void log(Artifact artifact, String testName, String message) {
      if (artifact != null) {
         testNameToGuidMap.put(testName, artifact.getGuid());
      }
      log(testName, message);
   }

   public void log(String testName, String message) {
      testNameToResultsMap.put(testName, message);
   }

   public void addResultsMapToResultData(XResultData xResultData) {
      String[] keys = testNameToResultsMap.keySet().toArray(new String[testNameToResultsMap.keySet().size()]);
      Arrays.sort(keys);
      for (String testName : keys) {
         xResultData.log(testName);
         for (String result : testNameToResultsMap.getValues(testName)) {
            xResultData.log(result);
         }
         // uniqueize guids
         Set<String> guidStrs = new HashSet<>();
         Collection<String> values = testNameToGuidMap.getValues(testName);
         if (values != null) {
            guidStrs.addAll(values);
         }
         xResultData.log(testName + "GUIDS: " + Collections.toString(",", guidStrs) + "\n");
      }
   }

   public void addTestTimeMapToResultData(XResultData xResultData) {
      xResultData.log("\n\nTime Spent in Tests");
      long totalTime = 0;
      for (Entry<String, MutableInteger> entry : testNameToTimeSpentMap.getCounts()) {
         xResultData.log(entry.getKey() + " - " + entry.getValue() + " ms");
         totalTime += entry.getValue().getValue();
      }
      xResultData.log("TOTAL - " + totalTime + " ms");

      xResultData.log("\n");
   }

   public HashCollection<String, String> getTestNameToResultsMap() {
      return testNameToResultsMap;
   }

}
