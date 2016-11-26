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
package samples.gxy.fundamentals.step2;

import java.awt.Color;

import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYDataObjectPolylineLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.TLcdStrokeLineStyle;
import com.luciad.view.map.TLcdGeodeticPen;


/**
 * Factory to create layers for models that contain flight plan data.
 */
public class FlightPlanLayerFactory implements ILcdGXYLayerFactory {

  private boolean isFlightPlanModel(ILcdModel aModel) {
    ILcdModelDescriptor md = aModel.getModelDescriptor();
    return md instanceof ILcdDataModelDescriptor &&
           ((ILcdDataModelDescriptor) md).getDataModel().equals(
               FlightPlanDataTypes.getDataModel());
  }

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!isFlightPlanModel(aModel)) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);

    // Sets a geodetic pen on the layer.

    layer.setGXYPen(new TLcdGeodeticPen());

    // Creates a painter to display the flight plans.

    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setLineStyle(TLcdStrokeLineStyle.newBuilder().
        color(Color.ORANGE).selectionColor(Color.YELLOW).build());
    layer.setGXYPainterProvider(painter);

    // Creates a label painter that displays the first feature of each model element.

    TLcdGXYDataObjectPolylineLabelPainter labelPainter = new TLcdGXYDataObjectPolylineLabelPainter();
    labelPainter.setExpressions(FlightPlanDataTypes.NAME);
    labelPainter.setForeground(Color.WHITE);
    labelPainter.setHaloEnabled(true);
    labelPainter.setHaloColor(Color.BLACK);

    layer.setGXYLabelPainterProvider(labelPainter);
    layer.setLabeled(true);

    return layer;
  }
}
