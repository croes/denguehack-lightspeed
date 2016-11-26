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
package samples.lightspeed.style.fillstyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * A sample to demonstrate the capabilities of TLspFillStyle, i.e. texture filling, solid color filling or
 * the use of a custom stipple pattern.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addData() throws IOException {
    super.addData();

    Collection<ILspLayer> layers = new ArrayList<>();

    LayerFactory layerFactory = new LayerFactory();
    layers.add(LspDataUtil.instance().model(createExtrudedSolidModel()).layer(layerFactory).addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(createExtrudedStipplePatternModel()).layer(layerFactory).addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(createSolidModel()).layer(layerFactory).addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(createStipplePatternModel()).layer(layerFactory).addToView(getView()).getLayer());
    layers.add(LspDataUtil.instance().model(createTextureModel()).layer(layerFactory).addToView(getView()).getLayer());

    FitUtil.fitOnLayers(this, layers);
  }

  private ILcdModel createTextureModel() {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(),
                                                new TLcdModelDescriptor("", "TextureShapes", "Textured shapes"));
    TLcdLonLatBounds bounds = new TLcdLonLatBounds(new TLcdLonLatPoint(-122.426044, 37.800256), 0.02083, 0.00587);
    model.addElement(bounds, ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createStipplePatternModel() {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(),
                                                new TLcdModelDescriptor("", "StippleShapes", "Stipple pattern shapes"));
    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon();
    polygon.insert2DPoint(0, -122.436669, 37.802664);
    polygon.insert2DPoint(1, -122.434781, 37.793406);
    polygon.insert2DPoint(2, -122.446343, 37.791931);
    polygon.insert2DPoint(3, -122.448514, 37.805003);
    polygon.insert2DPoint(4, -122.442248, 37.805885);
    model.addElement(polygon, ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createSolidModel() {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(),
                                                new TLcdModelDescriptor("", "SolidShapes", "Solid fill shapes"));
    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon();
    polygon.insert2DPoint(0, -122.423469, 37.795729);
    polygon.insert2DPoint(0, -122.407054, 37.797848);
    polygon.insert2DPoint(0, -122.404865, 37.786521);
    polygon.insert2DPoint(0, -122.419287, 37.775179);
    model.addElement(polygon, ILcdFireEventMode.NO_EVENT);
    return model;

  }

  private ILcdModel createExtrudedStipplePatternModel() {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(),
                                                new TLcdModelDescriptor("", "StippleShapes", "Extruded stipple shapes"));
    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon();
    polygon.insert2DPoint(0, -122.426439, 37.769646);
    polygon.insert2DPoint(1, -122.436636, 37.769028);
    polygon.insert2DPoint(2, -122.439529, 37.783365);
    polygon.insert2DPoint(3, -122.424711, 37.78539);
    polygon.insert2DPoint(4, -122.422163, 37.772892);
    TLcdExtrudedShape extrudedShape = new TLcdExtrudedShape(polygon, 200, 500);
    model.addElement(extrudedShape, ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createExtrudedSolidModel() {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(),
                                                new TLcdModelDescriptor("", "SolidShapes", "Extruded solid shapes"));
    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon();
    polygon.insert2DPoint(0, -122.405286, 37.796967);
    polygon.insert2DPoint(1, -122.398301, 37.791721);
    polygon.insert2DPoint(2, -122.403414, 37.787672);
    TLcdExtrudedShape extrudedShape = new TLcdExtrudedShape(polygon, 300, 1000);
    model.addElement(extrudedShape, ILcdFireEventMode.NO_EVENT);

    return model;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Fill style");
  }

}
