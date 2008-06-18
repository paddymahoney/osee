/*
 * Created on Jun 18, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.jdk.core.util;

import java.io.File;

/**
 * @author b1529404
 */
public class FileChangeEvent {
   private final File file;
   private final FileChangeType changeType;

   /**
    * @param file
    * @param changeType
    */
   public FileChangeEvent(File file, FileChangeType changeType) {
      this.file = file;
      this.changeType = changeType;
   }

   /**
    * @return the file
    */
   public File getFile() {
      return file;
   }

   /**
    * @return the changeType
    */
   public FileChangeType getChangeType() {
      return changeType;
   }

}
