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
package samples.decoder.asdi;

import java.awt.Color;

import com.luciad.format.asdi.TLcdASDIFlightPlanHistoryModelDescriptor;
import com.luciad.format.asdi.TLcdASDITrajectoryModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.gxy.common.layers.LayerFactoryWrapper;

/**
 * Layer factory for the trajectory or flight plan history models produced by the ASDI model decoder.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class ASDILayerFactory extends LayerFactoryWrapper {

  public static final Color TRAJECTORY_COLOR = new Color(115, 115, 115);
  public static final Color HISTORY_COLOR = new Color(83, 102, 83);

  public ASDILayerFactory() {
    super(new SingleLayerFactory());
  }

  private static class SingleLayerFactory implements ILcdGXYLayerFactory {
    public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
      if (aModel.getModelDescriptor() instanceof TLcdASDITrajectoryModelDescriptor) {
        return createTrajectoryLayer(aModel);
      } else if (aModel.getModelDescriptor() instanceof TLcdASDIFlightPlanHistoryModelDescriptor) {
        return createFlightPlanHistoryLayer(aModel);
      }
      return null;
    }

    private ILcdGXYLayer createTrajectoryLayer(ILcdModel aModel) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      layer.setGXYPen(new TLcdGeodeticPen(true));
      TLcdGXYPointListPainter painter = new TLcdGXYPointListPainter();
      TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
      line_style.setLineWidth(1);
      line_style.setColor(TRAJECTORY_COLOR);
      line_style.setSelectionLineWidth(1);
      line_style.setSelectionColor(new Color(66, 66, 102));
      painter.setLineStyle(line_style);
      layer.setGXYPainterProvider(painter);
      return layer;
    }

    private ILcdGXYLayer createFlightPlanHistoryLayer(ILcdModel aModel) {
      TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
      layer.setVisible(false);
      layer.setGXYPen(new TLcdGeodeticPen(true));
      TLcdGXYPointListPainter painter = new TLcdGXYPointListPainter();
      painter.setLineStyle(new TLcdGXYPainterColorStyle(HISTORY_COLOR, Color.black));
      layer.setGXYPainterProvider(painter);
      return layer;
    }

  }
}
