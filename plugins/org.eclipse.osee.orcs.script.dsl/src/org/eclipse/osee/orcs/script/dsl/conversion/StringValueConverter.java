/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.script.dsl.conversion;

import org.eclipse.osee.orcs.script.dsl.OrcsScriptUtil;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public class StringValueConverter extends AbstractValueConverter<String> {

   @Override
   public String toString(String value) {
      if (value == null) {
         throw new ValueConverterException("STRING-value may not be null.", null, null);
      }
      String converted = Strings.convertToJavaString(value, false);
      return OrcsScriptUtil.quote(converted);
   }

   @Override
   public String toValue(String rawValue, INode node) throws ValueConverterException {
      if (rawValue == null) {
         return null;
      }
      try {
         String value = OrcsScriptUtil.unquote(rawValue);
         return convertFromJavaString(value, false);
      } catch (IllegalArgumentException e) {
         throw new ValueConverterException(e.getMessage(), node, e);
      }
   }

   private String convertFromJavaString(String javaString, boolean useUnicode) {
      char[] in = javaString.toCharArray();
      int off = 0;
      int len = javaString.length();
      char[] convtBuf = new char[len];
      char aChar;
      char[] out = convtBuf;
      int outLen = 0;
      int end = off + len;

      while (off < end) {
         aChar = in[off++];
         if (aChar == '\\') {
            aChar = in[off++];
            if (useUnicode && aChar == 'u') {
               // Read the xxxx
               int value = 0;
               for (int i = 0; i < 4; i++) {
                  aChar = in[off++];
                  switch (aChar) {
                     case '0':
                     case '1':
                     case '2':
                     case '3':
                     case '4':
                     case '5':
                     case '6':
                     case '7':
                     case '8':
                     case '9':
                        value = (value << 4) + aChar - '0';
                        break;
                     case 'a':
                     case 'b':
                     case 'c':
                     case 'd':
                     case 'e':
                     case 'f':
                        value = (value << 4) + 10 + aChar - 'a';
                        break;
                     case 'A':
                     case 'B':
                     case 'C':
                     case 'D':
                     case 'E':
                     case 'F':
                        value = (value << 4) + 10 + aChar - 'A';
                        break;
                     default:
                        throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                  }
               }
               out[outLen++] = (char) value;
            } else {
               if (aChar == 't') {
                  aChar = '\t';
               } else if (aChar == 'r') {
                  aChar = '\r';
               } else if (aChar == 'n') {
                  aChar = '\n';
               } else if (aChar == 'f') {
                  aChar = '\f';
               } else if (aChar == 'b') {
                  aChar = '\b';
               } else if (aChar == '"') {
                  aChar = '\"';
               } else if (aChar == '\'') {
                  aChar = '\'';
               } else if (aChar == '\\') {
                  aChar = '\\';
               } else {
                  out[outLen++] = '\\';
               }
               out[outLen++] = aChar;
            }
         } else {
            out[outLen++] = aChar;
         }
      }
      return new String(out, 0, outLen);
   }
}