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
package samples.gxy.touch.basic;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.common.SampleData;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.toolbar.TouchToolBar;
import samples.gxy.touch.GXYTouchSample;

/**
 * This sample demonstrates how to use touch input to navigate, select, and measure distances in a
 * GXY view. The touch controllers are created in the TouchToolBar class.
 */
public class MainPanel extends GXYTouchSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-115, 22.50, 40.00, 30.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    TouchToolBar toolBar = getTouchToolBar();
    getView().setGXYController(toolBar.getWrappedController(toolBar.getNavigateController()));
  }

  @Override
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());

    // Add some layers to show touch based selection.
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startTouchSample(MainPanel.class, "Touch controllers");
  }
}
