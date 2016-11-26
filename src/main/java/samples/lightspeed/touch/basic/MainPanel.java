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
package samples.lightspeed.touch.basic;


import java.io.IOException;

import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.lightspeed.ILspView;

import samples.common.SampleData;
import samples.gxy.common.touch.TouchUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates the use of the touch-based ILspControllers.
 * It features a single controller chain to navigate around the map, and select and edit objects.
 * The navigation is configured for one finger panning and two finger rotating and zooming.
 *
 * @see samples.lightspeed.common.touch.TouchControllerFactory#createTouchGeneralController
 */
public class MainPanel extends LightspeedSample {

  public MainPanel() {
    super(true); // we want a touch toolbar.
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Once everything is connected to the sample, adapt the look and feel.
    TouchUtil.setTouchLookAndFeel(this);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").editable(true).addToView(getView()).fit();
  }

  public static void main(final String[] aArgs) {
    //To ensure that all icons created in the sample are 32 by 32 (more touch-friendly than 16 by 16),
    //we call this method before creating any UI
    TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);
    startSample(MainPanel.class, "Touch basic");
  }

}
