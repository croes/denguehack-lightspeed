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
package samples.lightspeed.fundamentals.step2;

import java.awt.Color;

import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;

import samples.gxy.fundamentals.step2.FlightPlanDataTypes;


/**
 * Factory to create layers for models that contain flight plan data.
 */
public class FlightPlanLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    ILcdModelDescriptor md = aModel.getModelDescriptor();
    return md instanceof ILcdDataModelDescriptor &&
           ((ILcdDataModelDescriptor) md).getDataModel().equals(
               FlightPlanDataTypes.getDataModel());
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (!canCreateLayers(aModel)) {
      return null;
    }

    // Create a TLspLayer with the given model.
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel);
    layerBuilder.bodyStyler(TLspPaintState.REGULAR,
                            // Use yellow lines, drape them onto the terrain
                            TLspLineStyle.newBuilder()
                                         .color(Color.YELLOW)
                                         .width(2)
                                         .elevationMode(ElevationMode.ABOVE_ELLIPSOID)
                                         .build()
    );
    layerBuilder.labelStyles(TLspPaintState.REGULAR,
                             // Use yellow text with a black outline
                             TLspTextStyle.newBuilder()
                                          .textColor(Color.YELLOW)
                                          .haloColor(Color.BLACK)
                                          .build(),
                             // Use the flight plan name as the label content
                             TLspDataObjectLabelTextProviderStyle.newBuilder()
                                                                 .expressions(FlightPlanDataTypes.NAME)
                                                                 .build()
    );
    return layerBuilder.build();
  }
}
