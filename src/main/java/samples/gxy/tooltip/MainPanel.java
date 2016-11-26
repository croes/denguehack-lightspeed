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
package samples.gxy.tooltip;

import java.io.IOException;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample contains a custom implementation of ILcdGXYController, which
 * displays tooltips for objects under the cursor. The text of the tooltips is
 * obtained via the ILcdDataObject interface. The applyOnInteract2DBounds()
 * method of ILcd2DBoundsIndexedModel is used to locate the object under the
 * mouse cursor.
 */
public class MainPanel extends GXYSample {

  @Override
  protected void createGUI() {
    super.createGUI();

    // Create the tooltip controller and add mouse wheel zoom support
    ToolTipController tooltipController = new ToolTipController();
    getToolBars()[0].addGXYController(tooltipController);
    getView().setGXYController(getToolBars()[0].getGXYController(tooltipController));
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView()).fit();
    GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Tooltips");
  }
}
