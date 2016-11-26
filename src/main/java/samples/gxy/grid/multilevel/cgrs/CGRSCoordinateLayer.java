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
package samples.gxy.grid.multilevel.cgrs;

import java.awt.Color;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.gxy.grid.multilevel.AreaFillStyle;
import samples.gxy.grid.multilevel.MultilevelGridElementPainter;
import samples.gxy.grid.multilevel.StatusedCoordinateLayer;
import samples.gxy.grid.multilevel.StatusedEditableMultilevelGridCoordinate;

public class CGRSCoordinateLayer extends TLcdGXYLayer implements StatusedCoordinateLayer {

  private final CGRSGridLayer fGridLayer;

  public CGRSCoordinateLayer(CGRSGridLayer aGridLayer) {
    super(createModel(aGridLayer));
    fGridLayer = aGridLayer;

    setGXYPen(new TLcdGeodeticPen(false)); // We need to turn of the straight line mode for the geodetic pen.

    MultilevelGridElementPainter area_painter = new MultilevelGridElementPainter(aGridLayer.getCGRSGrid());
    area_painter.setMode(MultilevelGridElementPainter.FILLED);
    area_painter.setFillStyle(new AreaFillStyle());
    area_painter.setLineStyle(new TLcdG2DLineStyle(Color.blue, Color.blue));
    setGXYPainterProvider(area_painter);
    setSelectable(true);
  }

  @Override
  public StatusedEditableMultilevelGridCoordinate createStatusedEditableMultilevelGridCoordinate() {
    return new StatusedCGRSCoordinate();
  }

  private static ILcdModel createModel(CGRSGridLayer aGridLayer) {
    CGRSGrid grid = aGridLayer.getCGRSGrid();
    ILcdGeodeticDatum datum = ((ILcdGeoReference) aGridLayer.getModel().getModelReference()).getGeodeticDatum();
    CGRSCoordinateModel area_model = new CGRSCoordinateModel(grid, datum);
    area_model.setModelDescriptor(new TLcdModelDescriptor("", "CGRS areas", "CGRS areas"));
    return area_model;
  }

  @Override
  public CGRSGridLayer getGridLayer() {
    return fGridLayer;
  }
}
