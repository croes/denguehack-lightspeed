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
package samples.lucy.printing.lightspeed;

import static com.luciad.lucy.addons.print.TLcyPrintAddOn.*;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;

import com.luciad.gui.ALcdAction;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.TLcyProperties;

/**
 * Action which prints the Lightspeed map component using the current print settings,
 * but forces the quality to 300dpi and uses 2 pages
 */
public class QuickPrintAction extends ALcdAction {
  private final CustomPrintAddOn fPrintAddOn;
  private final ILcyLspMapComponent fMapComponent;

  public QuickPrintAction(CustomPrintAddOn aPrintAddOn, ILcyLspMapComponent aMapComponent) {
    fPrintAddOn = aPrintAddOn;
    fMapComponent = aMapComponent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TLcyProperties copy = new TLcyProperties();
    copy.putAll(fPrintAddOn.getProperties(fMapComponent));

    // Let the user select a printer (optional step).
    // Both the PrintService and the PrinterJob store printing information, so we pass along both.
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    boolean proceed = printerJob.printDialog();
    if (!proceed) {
      return;
    }
    copy.put(PRINT_SERVICE_KEY, printerJob.getPrintService());
    copy.put(PRINTER_JOB_KEY, printerJob);

    // Customize the quality and page count.
    copy.putInt(RASTERIZATION_QUALITY_KEY, 300);
    copy.putString(PRINTING_AREA_PREFIX + PRINTING_AREA_CHOICE_VALUE_VIEW_EXTENTS + "." + SCALE_OR_PAGES_SUFFIX, SCALE_OR_PAGES_VALUE_PAGES);
    copy.putInt(PRINTING_AREA_PREFIX + PRINTING_AREA_CHOICE_VALUE_VIEW_EXTENTS + "." + ROW_COUNT_SUFFIX, 1);
    copy.putInt(PRINTING_AREA_PREFIX + PRINTING_AREA_CHOICE_VALUE_VIEW_EXTENTS + "." + COL_COUNT_SUFFIX, 2);

    fPrintAddOn.performQuickPrint(copy, fMapComponent);
  }
}
