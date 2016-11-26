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
package samples.lightspeed.oculus.common;

import java.awt.Component;

import javax.swing.JToolBar;

import com.luciad.oculus.TLspOculusDeviceBuilder;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspStereoscopicDevice;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;

/**
 * Base class for Oculus Rift samples. An Oculus view is created with a default terrain layer.
 */
public class OculusSample extends LightspeedSample {

  private ILspView fOculusView;

  protected OculusSample() {
    fOculusView = createOculusView();
  }

  @Override
  protected ILspAWTView createView() {
    return TLspViewBuilder.newBuilder().viewType(ILspView.ViewType.VIEW_3D).buildAWTView();
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    return new JToolBar[]{new ToolBar(aView, this, false, false) {
      @Override
      protected ILcdFilter<ILspLayer> createStickyLabelsLayerFilter() {
        return OculusSample.this.createStickyLabelsLayerFilter();
      }
    }};
  }

  public ILspView getOculusView() {
    return fOculusView;
  }

  private ILspView createOculusView() {
    TLspStereoscopicDevice oculusDevice = TLspOculusDeviceBuilder.newBuilder()
                                                                 .build();
    return TLspViewBuilder.newBuilder()
                          .buildStereoscopicView(oculusDevice);
  }
}
