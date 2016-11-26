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
package samples.gxy.touch.gestures;

import javax.swing.JPanel;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.common.SampleData;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.touch.GXYTouchSample;

/**
 * This sample demonstrates a few gesture recognizers for touch events. Whenever a gesture is
 * recognized, a corresponding geometric shape is drawn onto the screen.
 *
 * The following gestures are implemented:
 * - left to right flick (quick two-finger movement)
 * - right to left flick
 * - fragment of an ellipse contour 
 * - "Z" gesture (movement following 3 successive line segments)
 * - "X" gesture (two distinct line segments that cross each other)
 */
public class MainPanel extends GXYTouchSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-10.00, 34.00, 30.00, 30.00);
  }

  @Override
  protected JPanel createSettingsPanel() {
    GestureRecognizerController gestureController = new GestureRecognizerController();
    gestureController.setNextGXYController(getTouchToolBar().getEditController());
    getTouchToolBar().addGXYController(gestureController);
    getView().setGXYController(getTouchToolBar().getWrappedController(gestureController));
    return new GestureRecognizerPanel(gestureController);
  }

  @Override
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startTouchSample(MainPanel.class, "Touch gestures");
  }

}
