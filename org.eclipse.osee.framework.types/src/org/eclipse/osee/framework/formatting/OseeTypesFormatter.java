/*
 * generated by Xtext
 */
package org.eclipse.osee.framework.formatting;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;

/**
 * This class contains custom formatting description.
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#formatting
 * on how and when to use it
 * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an example
 */
public class OseeTypesFormatter extends AbstractDeclarativeFormatter {
   private final List<String> KEYWORDS =
         Arrays.asList(new String[] {"attribute", "sideAName", "sideAArtifactType", "sideBName", "sideBArtifactType",
               "defaultOrderType", "multiplicity", "dataProvider", "min", "max", "taggerId", "enumType",
               "defaultValue", "entry", "guid", "add", "remove", "inheritsAll"});

   private boolean isKeywordEntry(String current) {
      return KEYWORDS.contains(current);
   }

   @Override
   protected void configureFormatting(FormattingConfig c) {
      org.eclipse.osee.framework.services.OseeTypesGrammarAccess f =
            (org.eclipse.osee.framework.services.OseeTypesGrammarAccess) getGrammarAccess();

      c.setIndentationSpace("   ");

      Iterable<Keyword> keywords = GrammarUtil.containedKeywords(f.getGrammar());
      Stack<Keyword> openBraceStack = new Stack<Keyword>();
      for (Keyword currentKeyword : keywords) {
         String current = currentKeyword.getValue();
         if ("{".equals(current)) {
            openBraceStack.add(currentKeyword);
            c.setLinewrap().after(currentKeyword);
         } else if ("}".equals(current)) {
            c.setLinewrap(2).after(currentKeyword);
            c.setLinewrap().before(currentKeyword);
            if (!openBraceStack.isEmpty()) {
               c.setIndentation(openBraceStack.pop(), currentKeyword);
            }
         } else if (isKeywordEntry(current)) {
            c.setLinewrap().before(currentKeyword);
         }
      }
   }
}
