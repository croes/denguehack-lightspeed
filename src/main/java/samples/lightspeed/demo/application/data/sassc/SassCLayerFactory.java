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
package samples.lightspeed.demo.application.data.sassc;

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.SOUTH;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.plots.TLspPlotLayerBuilder;
import com.luciad.view.lightspeed.layer.plots.TLspPlotPaintingHints;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.ASasRecord;

/**
 * Layer factory for EuroControl SASS-C models.
 */
public class SassCLayerFactory extends AbstractLayerFactory {

  private SassCStyler fStyler;

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof SassCModelDescriptor;
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fStyler = new SassCStyler(new SassCStyleConfig(aProperties));
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    fStyler.init(aModel);

    ILspEditableStyledLayer layer = TLspPlotLayerBuilder.newBuilder().
        model(aModel).
                                                            label("SASS-C").
                                                            bodyStyler(TLspPaintState.REGULAR, fStyler).
                                                            labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder()
                                                                                                               .locations(8, SOUTH)
                                                                                                               .styles(TLspTextStyle.newBuilder()
                                                                                                                                    .font("Dialog-BOLD-11")
                                                                                                                                    .textColor(Color.white)
                                                                                                                                    .haloColor(Color.decode("0x024E68"))
                                                                                                                                    .build(),
                                                                                                                       new RecordTextProvider()
                                                                                                               )
                                                                                                               .build()).
                                                            labelScaleRange(new TLcdInterval(0.1, Double.MAX_VALUE)).
                                                            paintingHints(TLspPlotPaintingHints.MAX_PERFORMANCE).
                                                            build();

    layer.setVisible(TLspPaintRepresentationState.REGULAR_LABEL, true);
    layer.setVisible(TLspPaintRepresentationState.SELECTED_LABEL, true);

    return Collections.<ILspLayer>singletonList(layer);
  }

  private static class RecordTextProvider extends ALspLabelTextProviderStyle {

    @Override
    public String[] getText(Object o, Object o1, TLspContext aContext) {
      ASasRecord record = (ASasRecord) o;
      return new String[]{((record.getCallsign() == null) ? "" : record.getCallsign()),
                          valueToString(record.getTimeOfDetection())};
    }

    private static final DecimalFormat sDecimalFormat2 = new DecimalFormat("00");

    private static String valueToString(long value) {
      int totalSeconds = (int) value / 1000;
      int hours = totalSeconds / 3600;
      totalSeconds -= hours * 3600;
      int minutes = totalSeconds / 60;
      totalSeconds -= minutes * 60;
      int seconds = totalSeconds;
      return sDecimalFormat2.format(hours) + ":" + sDecimalFormat2.format(minutes) + ":" + sDecimalFormat2.format(seconds);
    }
  }
}
