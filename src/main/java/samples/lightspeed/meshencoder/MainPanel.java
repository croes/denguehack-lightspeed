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
package samples.lightspeed.meshencoder;

import java.io.IOException;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.ILcdAction;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.shape.shape3D.TLcdLonLatHeight3DArcBand;
import com.luciad.shape.shape3D.TLcdLonLatHeightDome;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.shape.shape3D.TLcdLonLatHeightSphere;
import com.luciad.shape.shape3D.TLcdLonLatHeightVariableGeoBuffer;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;

/**
 * This samples demonstrates how to encode a 3D shape in LuciadLightspeed into a discretized mesh, and save
 * it as a JSON file, in the following format:
 *
 * <pre class="code">
 *   {
 *     "location": [6, 50, 100],
 *     "positions": [0, 0, 0,   //Vertex 0
 *                   10, 0, 0,  //Vertex 1
 *                   0, 10, 0], //Vertex 2
 *     "indices": [0, 1, 2]     //Triangle 0 - 1 - 2
 *   }
 * </pre>
 *
 * This format can easily be re-used in LuciadRIA.
 */
public class MainPanel extends LightspeedSample {

  private ILspInteractivePaintableLayer fShapeLayer;

  @Override
  protected void addData() throws IOException {
    super.addData();

    if (fShapeLayer == null) {
      fShapeLayer = createShapeLayer();
    }
    getView().addLayer(fShapeLayer);

    FitUtil.fitOnLayers(this, fShapeLayer);
  }

  /**
   * Adds open and save buttons to the toolbar.
   */
  @Override
  protected void createGUI() {
    super.createGUI();

    ToolBar toolBar = getToolBars()[0];

    if (fShapeLayer == null) {
      fShapeLayer = createShapeLayer();
    }
    // Saves a 3D shape to a file
    ILcdAction saveAction = new SaveMeshAction(fShapeLayer);
    toolBar.addAction(saveAction, ToolBar.FILE_GROUP);
  }

  private ILspInteractivePaintableLayer createShapeLayer() {
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference();
    ILcdEllipsoid ellipsoid = modelReference.getGeodeticDatum().getEllipsoid();
    TLcdVectorModel vectorModel = new TLcdVectorModel(modelReference, new TLcdModelDescriptor("3D Shapes", "3D Shapes", "3D Shapes"));
    //Add Sphere to model
    TLcdLonLatPoint circleCenter = new TLcdLonLatPoint(-122.4194, 37.7749);
    TLcdLonLatCircle circle = new TLcdLonLatCircle(circleCenter, 200, ellipsoid);
    vectorModel.addElement(new TLcdLonLatHeightSphere(circle, 100), ILcdModel.NO_EVENT);

    //Add Dome to model
    TLcdLonLatPoint domeCenter = new TLcdLonLatPoint(-122.42, 37.7789);
    TLcdLonLatCircle dome = new TLcdLonLatCircle(domeCenter, 200, ellipsoid);
    vectorModel.addElement(new TLcdLonLatHeightDome(dome, 100), ILcdModel.NO_EVENT);

    //Add extruded shape to model
    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon();
    polygon.insert2DPoint(0, -122.425, 37.7749);
    polygon.insert2DPoint(1, -122.4257, 37.7749);
    polygon.insert2DPoint(2, -122.4257, 37.773);
    polygon.insert2DPoint(3, -122.4256, 37.771);
    polygon.insert2DPoint(4, -122.4232, 37.773);
    polygon.insert2DPoint(5, -122.422, 37.774);
    vectorModel.addElement(new TLcdExtrudedShape(polygon, 30, 150), ILcdModel.NO_EVENT);

    //Add 3D arcband to model
    TLcdLonLatHeight3DArcBand arcBand = new TLcdLonLatHeight3DArcBand(new TLcdLonLatHeightPoint(-122.4194, 37.768, 100), 50, 200, 60, 120, -30, 90, 0, 0, ellipsoid);
    vectorModel.addElement(arcBand, ILcdModel.NO_EVENT);

    //Add variable width buffer to model
    TLcdLonLatHeightPolyline geoBufferPolyline = new TLcdLonLatHeightPolyline();
    geoBufferPolyline.insert3DPoint(0, -122.4134, 37.777, 50);
    geoBufferPolyline.insert3DPoint(1, -122.4137, 37.774, 150);
    geoBufferPolyline.insert3DPoint(2, -122.4137, 37.771, 200);
    geoBufferPolyline.insert3DPoint(3, -122.4136, 37.768, 300);
    double[] widths = {50, 100, 150, 200};
    double[] heights = {50, 100, 150, 200};
    TLcdLonLatHeightVariableGeoBuffer geoBuffer = new TLcdLonLatHeightVariableGeoBuffer(geoBufferPolyline, widths, heights, ellipsoid);
    vectorModel.addElement(geoBuffer, ILcdModel.NO_EVENT);

    return TLspShapeLayerBuilder.newBuilder().model(vectorModel).bodyEditable(false).build();
  }

  @Override
  protected ILspAWTView createView() {
    return createView(ILspView.ViewType.VIEW_3D);
  }

  public static void main(String[] args) {
    LightspeedSample.startSample(MainPanel.class, "Mesh encoding");
  }

}
