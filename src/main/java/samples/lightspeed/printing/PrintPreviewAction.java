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
package samples.lightspeed.printing;

import java.awt.Component;

import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.swing.TLspViewComponentPrintable;

import samples.common.action.APrintPreviewAction;

public class PrintPreviewAction extends APrintPreviewAction<ILspAWTView, TLspViewComponentPrintable> {

  public PrintPreviewAction(Component aParent, ILspAWTView aView) {
    super(aParent, aView);
  }

  @Override
  protected TLspViewComponentPrintable createViewComponentPrintable(final ILspAWTView aView) {
    return PrintAction.createLspPrintable(aView);
  }

  @Override
  protected void setDPI(TLspViewComponentPrintable aPrintable, int aDpi) {
    aPrintable.setDPI(aDpi);
  }

  @Override
  protected void setRowsAndColumns(TLspViewComponentPrintable aPrintable, int aRows, int aColumns) {
    aPrintable.usePageCount(aColumns, aRows);
  }

  @Override
  protected void setFeatureScale(TLspViewComponentPrintable aPrintable, double aFeatureScale) {
    aPrintable.setFeatureScale(aFeatureScale);
  }

  @Override
  protected void beforePrinting() {
    if (getView() instanceof ALspAWTView) {
      ((ALspAWTView) getView()).beginPrinting();
    }
  }

  @Override
  protected void afterPrinting() {
    if (getView() instanceof ALspAWTView) {
      ((ALspAWTView) getView()).endPrinting();
    }
  }
}
