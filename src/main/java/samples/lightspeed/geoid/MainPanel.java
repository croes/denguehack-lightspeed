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
package samples.lightspeed.geoid;

import java.io.IOException;

import javax.swing.JComponent;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.navigationcontrols.ALcdZoomNavigationControl;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.swing.navigationcontrols.TLspAltitudeExaggerationControl;

import samples.common.SampleData;
import samples.gxy.geoid.GeoidModelFactory;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

public class MainPanel extends LightspeedSample {

  @Override
  protected ILspAWTView createView() {
    return super.createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected void addData() {
    LspDataUtil.instance().model(SampleData.BLUE_MARBLE).layer().label("Background").addToView(getView());
    LspDataUtil.instance()
               .model(GeoidModelFactory.createGeoidModel(getStatusBar(), this))
               .layer(new GeoidLayerFactory())
               .label("Geoid").addToView(getView());
  }

  @Override
  protected void addAltitudeExaggerationControl(JComponent aOverlayPanel) {
    try {
      TLspAltitudeExaggerationControl altitudeExaggerationControl =
          new TLspAltitudeExaggerationControl("images/gui/navigationcontrols/small/"
                                              + ALcdZoomNavigationControl.ZOOM_COMPONENT_DIR,
                                              getView(),
                                              1,
                                              10000);

      getOverlayPanel().add(altitudeExaggerationControl, TLcdOverlayLayout.Location.WEST);
    } catch (IOException ignored) {
    }
  }

  public static void main(String[] aArgs) {
    startSample(MainPanel.class, "Geoid");
  }
}
