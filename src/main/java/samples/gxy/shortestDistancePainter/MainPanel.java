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
package samples.gxy.shortestDistancePainter;

import java.io.IOException;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.decoder.MapSupport;

/**
 * This sample demonstrates how to create a composite ILcd2DEditableShape and a
 * suitable ILcdGXYPainter and ILcdGXYEditor for that ILcd2DEditableShape.
 * <p/>
 * The class ShortestDistanceShape is a composition of a TLcdLonLatPolyline and
 * a TLcdLonLatPoint around this polyline. The class ShortestDistanceShape knows
 * how to calculate the ILcdPoint on the geodesic TLcdLonLatPolyline that is the
 * closest to the TLcdLonLatPoint.
 * <p/>
 * The class ShortestDistancePainter implements ILcdGXYPainter and ILcdGXYEditor
 * and is used for painting and editing ShortestDistanceShape objects.
 * The ShortestDistancePainter delegates painting of the TLcdLonLatPoint to a
 * TLcdGXYIconPainter and the p
 * Existing painters for the composed shape object are used for painting,
 * TLcdGXYIconPainter for the TLcdLonLatPoint and TLcdGXYPointListPainter for
 * the TLcdLonLatPolyline. Painting the shortest distance path of the
 * TLcdLonLatPoint to the TLcdLonLatPolyline the utility ILcdGXYPen is used.
 * <p/>
 * The painter-editor allows to move the TLcdLonLatPoint or a point of the
 * TLcdLonLatPolyline of the ShortestDistanceShape object to any location while
 * constantly showing the shortest distance path of the TLcdLonLatPoint to
 * the TLcdLonLatPolyline.
 */
public class MainPanel extends GXYSample {

  @Override
  protected void addData() throws IOException {
    ILcdGXYLayer layer = createGeodeticModelLayer();
    GXYLayerUtil.addGXYLayer(getView(), layer, false, false);
    GXYLayerUtil.fitGXYLayer(getView(), layer);
  }

  /**
   * Creation of a geodetic model layer that contains a polygon.
   */
  private ILcdGXYLayer createGeodeticModelLayer() {

    // create 2D polygon
    ILcd2DEditablePoint[] points = {
        new TLcdLonLatPoint(15.0, 45.0),
        new TLcdLonLatPoint(9.0, 55.0),
        new TLcdLonLatPoint(0.0, 45.0),
        new TLcdLonLatPoint(9.0, 25.0),
    };
    TLcdLonLatPolyline polyline = new TLcdLonLatPolyline(new TLcd2DEditablePointList(points, false));

    // create geodetic model
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing the shortest distance data",   // source name (is used as tooltip text)
        "MyLonLatPolygon",      // data type
        "Geodetic"   // display name (user)
    ));

    ILcdGeodeticDatum datum = new TLcdGeodeticDatum();

    ShortestDistanceShape shortest_distance_shape = new ShortestDistanceShape(
        polyline, new TLcdLonLatPoint(3.0, 45.0), datum.getEllipsoid()
    );

    TLcdGeodeticReference modelReference = new TLcdGeodeticReference(datum);
    model.setModelReference(modelReference);
    model.addElement(shortest_distance_shape, ILcdFireEventMode.NO_EVENT);

    // create layer for geodetic model
    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setSelectable(true);
    layer.setEditable(true);

    // configure a suitable pen
    layer.setGXYPen(MapSupport.createPen(modelReference, false));

    // provide painter and editor for geodetic model layer
    ShortestDistancePainter painter = new ShortestDistancePainter();
    layer.setGXYPainterProvider(painter);
    layer.setGXYEditorProvider(painter);

    return layer;
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Painter, shape composition");
  }
}
