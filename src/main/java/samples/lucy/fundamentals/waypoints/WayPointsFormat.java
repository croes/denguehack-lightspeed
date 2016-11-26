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
package samples.lucy.fundamentals.waypoints;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.datatransfer.ALcyLayerSelectionTransferHandler;
import com.luciad.lucy.format.lightspeed.ALcyLspStyleFormat;
import com.luciad.lucy.gui.formatbar.ALcyFormatBarFactory;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyler;

import samples.gxy.fundamentals.step3.WayPointDataTypes;

/**
 * {@code ALcyLspFormat} responsible for the visualization of the waypoint data
 * on a Lightspeed view
 */
final class WayPointsFormat extends ALcyLspStyleFormat {

  public WayPointsFormat(ILcyLucyEnv aLucyEnv,
                         String aLongPrefix,
                         String aShortPrefix,
                         ALcyProperties aPreferences) {
    super(aLucyEnv, aLongPrefix, aShortPrefix, aPreferences, new WayPointsModelFilter());
  }

  @Override
  protected ILspLayerFactory createLayerFactoryImpl() {
    return new ALspSingleLayerFactory() {
      @Override
      public ILspLayer createLayer(ILcdModel aModel) {
        TLspCustomizableStyler bodyStyler = new TLspCustomizableStyler(
            TLspIconStyle.newBuilder().build()
        );
        TLspCustomizableStyler labelStyler = new TLspCustomizableStyler(
            TLspTextStyle.newBuilder().build(),
            TLspDataObjectLabelTextProviderStyle.newBuilder().expressions(WayPointDataTypes.NAME).build()
        );
        ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder()
                                                                   .model(aModel)
                                                                   .bodyStyler(TLspPaintState.REGULAR, bodyStyler)
                                                                   .labelStyler(TLspPaintState.REGULAR, labelStyler)
                                                                   .editableSupported(true)
                                                                   .bodyEditable(true)
                                                                   .build();
        layer.setVisible(TLspPaintRepresentationState.REGULAR_LABEL, false);
        layer.setVisible(TLspPaintRepresentationState.SELECTED_LABEL, true);
        return layer;
      }

      @Override
      public boolean canCreateLayers(ILcdModel aModel) {
        // The check on the model is done by the TLcyLspSafeGuardFormatWrapper
        return true;
      }
    };
  }

  @Override
  protected ALcyFormatBarFactory createFormatBarFactory() {
    return new WayPointsFormatBarFactory(getLucyEnv(), getProperties(), getShortPrefix());
  }

  @Override
  protected ALcyLayerSelectionTransferHandler[] createLayerSelectionTransferHandlers() {
    return new ALcyLayerSelectionTransferHandler[]{
        new WayPointLayerSelectionTransferHandler()
    };
  }
}
