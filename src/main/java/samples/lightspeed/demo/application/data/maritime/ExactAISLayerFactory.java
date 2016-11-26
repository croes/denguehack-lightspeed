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
package samples.lightspeed.demo.application.data.maritime;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.plots.TLspPlotLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for the Exact AIS theme.
 */
public class ExactAISLayerFactory extends AbstractLayerFactory {
  private ExactAISStyler fStyler;
  private String fIconDir;

  public ExactAISLayerFactory() {
  }

  public ExactAISStyler getStyler() {
    return fStyler;
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);

    fIconDir = aProperties.getProperty("navstatus.icondir");
    if (fIconDir != null) {
      fIconDir = Framework.getInstance().getDataPath(fIconDir);
    }
    fStyler = new ExactAISStyler(fIconDir);
  }

  public String getIconDir() {
    return fIconDir;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof ExactAISModelDescriptor;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return createLayers(aModel, 0);
  }

  public Collection<ILspLayer> createLayers(ILcdModel aModel, double aMinScale) {
    ExactAISModelDescriptor modelDescriptor = (ExactAISModelDescriptor) aModel.getModelDescriptor();

    final ILspEditableStyledLayer layer = TLspPlotLayerBuilder.newBuilder()
                                                              .model(aModel)
                                                              .label("ExactAIS")
                                                              .bodyStyler(TLspPaintState.REGULAR, fStyler)
                                                              .bodyStyler(TLspPaintState.SELECTED, fStyler)
                                                              .bodyScaleRange(new TLcdInterval(aMinScale, Double.POSITIVE_INFINITY))
                                                              .labelScaleRange(new TLcdInterval(0.005, Double.MAX_VALUE))
                                                              .mandatoryOrientation(true)
                                                              .mandatoryAttributes(fStyler.getAttributes())
                                                              .labelStyler(TLspPaintState.REGULAR,
                                                                           TLspLabelStyler.newBuilder()
                                                                                          .locations(-20, TLspLabelLocationProvider.Location.SOUTH)
                                                                                          .styles(
                                                                                              TLspTextStyle.newBuilder()
                                                                                                           .font("Dialog-BOLD-11")
                                                                                                           .textColor(Color.white)
                                                                                                           .haloColor(Color.decode("0x024E68"))
                                                                                                           .build(),
                                                                                              new ShipNameTextProvider(modelDescriptor)
                                                                                          )
                                                                                          .build()
                                                              )
                                                              .build();
    layer.setVisible(TLspPaintRepresentationState.REGULAR_LABEL, true);
    layer.setVisible(TLspPaintRepresentationState.SELECTED_LABEL, true);

    return Collections.<ILspLayer>singletonList(
        layer
    );
  }

  private static class ShipNameTextProvider extends ALspLabelTextProviderStyle {
    private final ExactAISModelDescriptor fModelDescriptor;

    public ShipNameTextProvider(ExactAISModelDescriptor aModelDescriptor) {
      fModelDescriptor = aModelDescriptor;
    }

    @Override
    public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
      AISPlot plot = (AISPlot) aDomainObject;
      ExactAISModelDescriptor.ShipDescriptor shipDescriptor = fModelDescriptor.getShipDescriptor(plot.getID());
      return new String[]{shipDescriptor == null ? "Unknown" : shipDescriptor.getVesselName()};
    }
  }
}
