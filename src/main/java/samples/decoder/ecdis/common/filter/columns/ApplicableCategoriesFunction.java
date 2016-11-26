/*
 *
 * Copyright (c) 1999-2016 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package samples.decoder.ecdis.common.filter.columns;

import static com.luciad.format.s52.TLcdS52DisplaySettings.DISPLAY_CATEGORY_PROPERTY;

import java.util.EnumSet;

import com.luciad.format.s52.ELcdS52DisplayCategory;
import samples.decoder.ecdis.common.ObjectClass;
import samples.decoder.ecdis.common.S57ObjectClassLookup;

/**
 * Function that determines the applicable {@link ELcdS52DisplayCategory categories} for a given object class.
 */
public class ApplicableCategoriesFunction implements IFunction<ObjectClass, String> {

  private static final S57ObjectClassLookup LOOKUP = S57ObjectClassLookup.getLookup();
  private static final EnumSet<ELcdS52DisplayCategory> DISPLAY_CATEGORIES =
      EnumSet.of(ELcdS52DisplayCategory.DISPLAY_BASE, ELcdS52DisplayCategory.STANDARD, ELcdS52DisplayCategory.OTHER);

  @Override
  public String apply(ObjectClass input) {
    StringBuilder stringBuilder = new StringBuilder();
    EnumSet<ELcdS52DisplayCategory> displayBase = DISPLAY_CATEGORIES;
    for (ELcdS52DisplayCategory category : displayBase) {
      if (LOOKUP.categoryContains(category, input.getCode())) {
        if (stringBuilder.length() != 0) {
          stringBuilder.append("+");
        }

        String displayName = DISPLAY_CATEGORY_PROPERTY.getType().getDisplayName(category);
        stringBuilder.append(displayName);
      }
    }

    return stringBuilder.toString();
  }
}
