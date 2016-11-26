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
package samples.lucy.printing;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.Arrays;

import com.luciad.gui.TLcdCompositePageable;
import com.luciad.gui.TLcdWatermarkPrintable;
import com.luciad.lucy.addons.print.TLcyPrintAddOn;
import com.luciad.lucy.addons.print.TLcyPrintContext;

/**
 * <p>Custom extension of the {@code TLcyPrintAddOn}. This add-on adds the following behavior:</p>
 *
 * <ul>
 *   <li>Adds a watermark to all GXY prints</li>
 *   <li>Adds a title page to all GXY prints</li>
 * </ul>
 */
public class CustomPrintAddOn extends TLcyPrintAddOn {

  @Override
  public Printable createPageDecorator(TLcyPrintContext aPrintContext) {
    return new TLcdWatermarkPrintable();
  }

  @Override
  public Pageable createPrintPageable(Pageable aPageable, TLcyPrintContext aPrintContext) {

    // Reuse the original page format for the title page, but make sure it's portrait.
    PageFormat originalPageFormat = aPageable.getPageFormat(0);
    PageFormat titlePageFormat = (PageFormat) originalPageFormat.clone();
    titlePageFormat.setOrientation(PageFormat.PORTRAIT);

    // Create and append a title page.
    // Note that the title page will not be visible in the print preview.
    TitlePageable titlePageable = new TitlePageable(titlePageFormat);
    return new TLcdCompositePageable(Arrays.asList(titlePageable, aPageable));
  }
}
