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
package org.eclipse.osee.framework.skynet.core.attribute;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.attribute.providers.ICharacterAttributeDataProvider;

/**
 * @author Robert A. Fisher
 * @author Ryan D. Brooks
 */
public class DateAttribute extends CharacterBackedAttribute<Date> {
   public static final DateFormat MMDDYY = new SimpleDateFormat("MM/dd/yyyy");
   public static final DateFormat MMDDYYHHMM = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
   public static final DateFormat HHMM = new SimpleDateFormat("hh:mm");
   public static final DateFormat MMDDYYYYHHMMSSAMPM = new SimpleDateFormat("MMM dd,yyyy hh:mm:ss a");
   public static final DateFormat ALLDATETIME = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

   private static final DateFormat[] legacyDateFormats = new DateFormat[] {MMDDYYYYHHMMSSAMPM, ALLDATETIME, MMDDYYHHMM};

   private ICharacterAttributeDataProvider dataProvider;

   /**
    * Create a date attribute with a given type, initialized to the current date and time.
    * 
    * @param attributeType The type of the attribute
    */
   public DateAttribute(AttributeType attributeType, ICharacterAttributeDataProvider dataProvider) {
      super(attributeType);
      this.dataProvider = dataProvider;
      String defaultValue = attributeType.getDefaultValue();
      if (Strings.isValid(defaultValue) != true) {
         defaultValue = "";
      }
      dataProvider.setValue(defaultValue);
   }

   /**
    * Sets date
    * 
    * @param value value or null to clear
    */
   public void setValue(Date value) {
      String toSet = value != null ? Long.toString(value.getTime()) : "";
      dataProvider.setValue(toSet);
   }

   /**
    * Return current date or null if not set
    * 
    * @return date or null if not set
    */
   public Date getValue() {
      Date toReturn = null;
      String value = dataProvider.getValueAsString();
      if (Strings.isValid(value) != false) {
         //TODO Added for backward compatibility with inconsistent date formats;
         try {
            toReturn = new Date(Long.parseLong(value));
         } catch (Exception ex) {
            // We have a legacy date - need to figure out how to parse it
            toReturn = handleLegacyDates(value);
         }
      }
      return toReturn;
   }

   private Date handleLegacyDates(String rawValue) {
      Date toReturn = null;
      for (DateFormat format : legacyDateFormats) {
         try {
            toReturn = format.parse(rawValue);
            break;
         } catch (ParseException ex) {
         }
      }
      return toReturn;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.attribute.Attribute#getDisplayableString()
    */
   @Override
   public String getDisplayableString() {
      return getAsFormattedString(DateAttribute.MMDDYYHHMM);
   }

   /**
    * Return date in format given by pattern or "" if not set
    * 
    * @param pattern DateAttribute.MMDDYY, etc...
    * @return formated date
    */
   public String getAsFormattedString(DateFormat dateFormat) {
      Date date = getValue();
      return date != null ? dateFormat.format(getValue()) : "";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.attribute.Attribute#setFromString(java.lang.String)
    */
   @Override
   public void setFromString(String value) throws Exception {
      Date toSet = null;
      if (value == null || value.equals("")) {
         toSet = new Date(1);
      } else {
         toSet = new Date(Long.parseLong(value));
      }
      setValue(toSet);
   }

}
