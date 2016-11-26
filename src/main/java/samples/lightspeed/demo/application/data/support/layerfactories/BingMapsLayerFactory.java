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
package samples.lightspeed.demo.application.data.support.layerfactories;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import com.luciad.format.bingmaps.TLcdBingMapsModelDescriptor;
import com.luciad.format.bingmaps.copyright.lightspeed.TLspBingMapsCopyrightIcon;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.ELcdHorizontalAlignment;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.style.TLspRasterStyle;

import samples.decoder.bingmaps.ChangeListenerLabel;
import samples.decoder.bingmaps.DataSourceFactory;
import samples.decoder.bingmaps.LayerVisibilityLabel;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for Bing Maps.
 */
public class BingMapsLayerFactory extends AbstractLayerFactory {

  private boolean fInteractive;

  @Override
  public void configure(Properties aProperties) {
    fInteractive = Boolean.parseBoolean(aProperties.getProperty("interactive", "true"));
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdBingMapsModelDescriptor;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    // Improves legibility of labeled content.
    double quality = 0.6;
    if (DataSourceFactory.containsText((TLcdBingMapsModelDescriptor) aModel.getModelDescriptor())) {
      quality = 0.3;
    }
    TLspRasterStyle rasterStyle = TLspRasterStyle.newBuilder().levelSwitchFactor(quality).build();
    ILspEditableStyledLayer layer = TLspRasterLayerBuilder.newBuilder()
                                                          .model(aModel)
                                                          .layerType(fInteractive ? ILspLayer.LayerType.INTERACTIVE : ILspLayer.LayerType.BACKGROUND)
                                                          .styler(TLspPaintRepresentationState.REGULAR_BODY, rasterStyle)
                                                          .build();
    layer.setVisible(false);
    layer.addPropertyChangeListener(new CopyrightListener(layer));
    return Collections.<ILspLayer>singleton(layer);
  }

  /**
   * Enables the copyright when the layer is visible in a view.
   */
  private static class CopyrightListener implements PropertyChangeListener {
    private final ILspEditableStyledLayer fLayer;
    private final Set<ILspView> fViewsWithCopyRight = Collections.newSetFromMap(new TLcdWeakIdentityHashMap<ILspView, Boolean>());

    public CopyrightListener(ILspEditableStyledLayer aLayer) {
      fLayer = aLayer;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equalsIgnoreCase("visible")) {
        setCopyrightEnabled((Boolean) evt.getNewValue());
      }
    }

    private void setCopyrightEnabled(boolean aEnabled) {
      for (ILspView view : fLayer.getCurrentViews()) {
        if (!(view instanceof ILspAWTView)) {
          continue;
        }
        ILspAWTView awtView = (ILspAWTView) view;
        if (!fViewsWithCopyRight.contains(view)) {
          if (aEnabled) {
            initComponents(awtView);
          }
        }
      }
    }

    private void initComponents(ILspAWTView aView) {
      TLcdBingMapsModelDescriptor modelDescriptor = (TLcdBingMapsModelDescriptor) fLayer.getModel().getModelDescriptor();
      ILcdIcon logoIcon = modelDescriptor.getLogo();

      // Shows a Bing Maps logo, linked to the layer's visibility.
      if (logoIcon != null) {
        LayerVisibilityLabel logoLabel = new LayerVisibilityLabel(new TLcdSWIcon(logoIcon), aView, TLcdBingMapsModelDescriptor.class);
        aView.getOverlayComponent().add(logoLabel, TLcdOverlayLayout.Location.NORTH_WEST);
      }

      // Shows Bing Maps attribution strings, linked to what is shown in the map
      TLspBingMapsCopyrightIcon copyrightIcon = new TLspBingMapsCopyrightIcon(aView);
      copyrightIcon.setAlignment(ELcdHorizontalAlignment.RIGHT);
      ChangeListenerLabel copyrightLabel = new ChangeListenerLabel(new TLcdSWIcon(copyrightIcon), copyrightIcon);
      aView.getOverlayComponent().add(copyrightLabel, TLcdOverlayLayout.Location.NORTH_WEST);

      fViewsWithCopyRight.add(aView);
    }
  }
}
