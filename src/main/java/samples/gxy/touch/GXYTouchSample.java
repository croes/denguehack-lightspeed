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
package samples.gxy.touch;

import java.awt.Component;

import com.luciad.gui.TLcdIconFactory;

import samples.common.SamplePanel;
import samples.gxy.common.GXYSample;
import samples.gxy.common.toolbar.TouchToolBar;
import samples.gxy.common.touch.TouchUtil;

/**
 * Base class for GXY samples with touch input.<br/>
 * It changes the look-and-feel and sets up a toolbar with touch-enabled controllers.
 */
public abstract class GXYTouchSample extends GXYSample {

  private TouchToolBar fToolBar;

  @Override
  protected void createGUI() {
    super.createGUI();
    // Once everything is connected to the sample, adapt the look and feel.
    TouchUtil.setTouchLookAndFeel(this);
  }

  @Override
  protected Component[] createToolBars() {
    // Create the tree layer control
    boolean touchSupported = TouchUtil.checkTouchDevice(this);
    fToolBar = createTouchToolBar(touchSupported);
    return new Component[]{fToolBar};
  }

  protected TouchToolBar createTouchToolBar(boolean aTouchSupported) {
    return new TouchToolBar(getView(), true, aTouchSupported, this, getOverlayPanel());
  }

  protected TouchToolBar getTouchToolBar() {
    return fToolBar;
  }

  public static void startTouchSample(Class<? extends SamplePanel> aClass, String aTitle) {
    //To ensure that all icons created in the sample are 32 by 32 (more touch-friendly than 16 by 16),
    //we call this method before creating any UI
    TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);
    startSample(aClass, aTitle);
  }

}

