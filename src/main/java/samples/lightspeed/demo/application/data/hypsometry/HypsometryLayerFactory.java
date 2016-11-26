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
package samples.lightspeed.demo.application.data.hypsometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricShadingLayerBuilder;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricShadingStyle;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;

import samples.lightspeed.demo.application.gui.menu.HypsometryPanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for hypsometric terrain shading.
 */
public class HypsometryLayerFactory extends AbstractLayerFactory {

  private static final String HYPSOMETRY_LAYERS_KEY = "hypsometry.layers";

  private static void checkIfHypsometryAvailable() {
    // Checking whether the TEA module is available
    TLspHypsometricShadingLayerBuilder.newBuilder();
  }

  public HypsometryLayerFactory() {
    checkIfHypsometryAvailable();
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer());
  }

  public ILspLayer createLayer() {
    // Create a hypsometric layer
    ILspEditableStyledLayer layer = TLspHypsometricShadingLayerBuilder.newBuilder().
        styler(
            TLspPaintRepresentationState.REGULAR_BODY,
            new TLspEditableStyler(
                Arrays.asList(
                    TLspHypsometricShadingStyle.newBuilder().
                        shader(HypsometryPanelFactory.HYPSOMETRY_SHADERS[0]).
                                                   colorModel(HypsometryPanelFactory.HYPSOMETRY_COLORMODELS[0]).
                                                   build(),
                    TLspRasterStyle.newBuilder().levelSwitchFactor(0.1).build()
                )
            )
        ).elevationFromView().
                                                                          build();

    // Store the layer in a globally accessible list. This list is used to link
    // the GUI components in the demo to the hypsometric layers.
    Framework app = Framework.getInstance();
    List<ILspEditableStyledLayer> layers = (List<ILspEditableStyledLayer>) app.getSharedValue(HYPSOMETRY_LAYERS_KEY);
    if (layers == null) {
      layers = new ArrayList<ILspEditableStyledLayer>();
      app.storeSharedValue(HYPSOMETRY_LAYERS_KEY, layers);
    }
    layers.add(layer);

    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    // The model is ignored, so this layer factory can create a layer for any model.
    return true;
  }
}
