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
package samples.lucy.lightspeed.style.loading;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import com.luciad.gui.ALcdAction;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.stylerepository.lightspeed.TLcyLspStyleRepositoryAddOn;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyLayerStyle;
import com.luciad.lucy.map.TLcyCompositeLayerStyleCodec;
import com.luciad.lucy.map.TLcyCompositeLayerStyleProvider;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Action which loads a predefined style file and tries to apply it on all layers + adjusts the
 * application-wide selection style
 *
 */
public class LoadStyleAction extends ALcdAction {

  private final ILcyGenericMapComponent<? extends ILspView, ? extends ILspLayer> fMapComponent;
  private final String fStyleFile;
  private final ILcyLucyEnv fLucyEnv;

  private Color fSelectionLineColor;
  private Color fSelectionFillColor;
  private Color fSelectionTextColor;

  public LoadStyleAction(ILcyGenericMapComponent<? extends ILspView, ? extends ILspLayer> aMapComponent,
                         String aStyleFile,
                         Color aSelectionLineColor,
                         Color aSelectionFillColor,
                         Color aSelectionTextColor,
                         ILcyLucyEnv aLucyEnv) {
    fMapComponent = aMapComponent;
    fStyleFile = aStyleFile;
    fSelectionLineColor = aSelectionLineColor;
    fSelectionFillColor = aSelectionFillColor;
    fSelectionTextColor = aSelectionTextColor;
    fLucyEnv = aLucyEnv;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //apply the style on the selected layers when possible
    Enumeration layers = fMapComponent.getMainView().layers();
    try {
      while (layers.hasMoreElements()) {
        ILspLayer layer = (ILspLayer) layers.nextElement();
        loadStyle(layer);
      }
    } catch (IOException aException) {
      throw new RuntimeException(aException);
    }

    updateSelectionStyleColors();
  }

  private void loadStyle(ILspLayer aLayer) throws IOException {
    TLcyCompositeLayerStyleCodec layerStyleCodec = new TLcyCompositeLayerStyleCodec(fLucyEnv);
    TLcyCompositeLayerStyleProvider layerStyleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);
    if (layerStyleProvider.canGetStyle(aLayer)) {
      ILcyLayerStyle style = layerStyleProvider.getStyle(aLayer);
      if (layerStyleCodec.canDecode(aLayer, style)) {
        InputStream inputStream = null;
        try {
          TLcdInputStreamFactory inputStreamFactory = new TLcdInputStreamFactory();
          inputStream = inputStreamFactory.createInputStream(fStyleFile);
          layerStyleCodec.decode(aLayer, style, inputStream);
        } finally {
          if (inputStream != null) {
            inputStream.close();
          }
        }
      }
    }
  }

  private void updateSelectionStyleColors() {
    TLcyLspStyleRepositoryAddOn styleRepositoryAddOn = fLucyEnv.retrieveAddOnByClass(TLcyLspStyleRepositoryAddOn.class);
    if (styleRepositoryAddOn != null) {
      ALcyProperties preferences = styleRepositoryAddOn.getPreferences();
      preferences.putColor("TLcyLspStyleRepositoryAddOn.selection.fillColor", fSelectionFillColor);
      preferences.putColor("TLcyLspStyleRepositoryAddOn.selection.lineColor", fSelectionLineColor);
      preferences.putColor("TLcyLspStyleRepositoryAddOn.selection.textColor", fSelectionTextColor);
    }
  }
}

