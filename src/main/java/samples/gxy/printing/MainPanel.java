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
package samples.gxy.printing;

import java.io.IOException;

import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.SampleData;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample demonstrates how to print a component containing a view,
 * using TLcdGXYViewComponentPrintable.
 */
public class MainPanel extends samples.gxy.decoder.MainPanel {

  @Override
  protected void createGUI() {
    super.createGUI();

    getToolBars()[0].addSpace();
    getToolBars()[0].addAction(new PrintAction(this, getView()));
    getToolBars()[0].addAction(new PrintPreviewAction(this, getView()));
  }

  protected void addData() throws IOException {
    ILcdGXYLayer countries = GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView()).getLayer();
    countries.setLabeled(true);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Printing");
  }

}
