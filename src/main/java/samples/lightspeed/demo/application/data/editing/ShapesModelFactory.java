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
package samples.lightspeed.demo.application.data.editing;

import java.util.Properties;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatArcBand;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape3D.TLcdExtrudedShape;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Creates models with shapes.
 */
public class ShapesModelFactory extends AbstractModelFactory {

  private double fOffsetX;
  private double fOffsetY;

  public ShapesModelFactory(String aType) {
    super(aType);
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fOffsetX = Double.parseDouble(aProperties.getProperty("xOffset", "0.0"));
    fOffsetY = Double.parseDouble(aProperties.getProperty("yOffset", "0.0"));
  }

  @Override
  public ILcdModel createModel(String aSource) {
    // Create the model
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference());
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        getClass().getName(), // source name
        getType(),            // type
        "Shapes"              // display name (user)
    ));

    // Fill the model
    addShapes(model, datum.getEllipsoid());
    addExtrudedShapes(model, datum.getEllipsoid());

    return model;
  }

  private void addShapes(TLcdVectorModel aModel, ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] polygonPoints = new ILcd2DEditablePoint[]{
        new TLcdLonLatPoint(fOffsetX + 0.014869132062202084, fOffsetY - 1.0739613671262305E-4),
        new TLcdLonLatPoint(fOffsetX + 0.009295149473544484, fOffsetY + 0.002174861055159738),
        new TLcdLonLatPoint(fOffsetX + 0.006422744943762382, fOffsetY - 1.5583250461048692E-4),
        new TLcdLonLatPoint(fOffsetX + 0.002361001092012316, fOffsetY - 9.757442625826229E-4),
        new TLcdLonLatPoint(fOffsetX + 4.381462208726816E-4, fOffsetY - 0.0024451419145279374),
        new TLcdLonLatPoint(fOffsetX + 0.0021053805792092817, fOffsetY - 0.005774764036416968),
        new TLcdLonLatPoint(fOffsetX + 0.0064939658690548185, fOffsetY - 0.008660576189384983),
        new TLcdLonLatPoint(fOffsetX + 0.012130781660587786, fOffsetY - 0.008782627904579954),
        new TLcdLonLatPoint(fOffsetX + 0.012572657400127696, fOffsetY - 0.005012795511113666)};
    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon(new TLcd2DEditablePointList(polygonPoints, false));
    aModel.addElement(polygon, ILcdModel.NO_EVENT);

    TLcdLonLatPoint ellipseCenter = new TLcdLonLatPoint(fOffsetX - 0.008911061769254047,
                                                        fOffsetY + 0.014186857564013167);
    TLcdLonLatEllipse ellipse = new TLcdLonLatEllipse(ellipseCenter, 500, 300, 30, aEllipsoid);
    aModel.addElement(ellipse, ILcdModel.NO_EVENT);

    TLcdLonLatPoint arcBandCenter = new TLcdLonLatPoint(fOffsetX - 0.03348393382593429,
                                                        fOffsetY + 0.00405993984939812);
    TLcdLonLatArcBand arcBand = new TLcdLonLatArcBand(arcBandCenter, 800, 1200, 20, 200, aEllipsoid);
    aModel.addElement(arcBand, ILcdModel.NO_EVENT);
  }

  private void addExtrudedShapes(TLcdVectorModel aModel, ILcdEllipsoid aEllipsoid) {
    TLcdLonLatPoint polygon2Center = new TLcdLonLatPoint(fOffsetX + 0.0147584, fOffsetY + 0.019594);
    ILcd2DEditablePoint[] polygonPoints2 = new ILcd2DEditablePoint[]{
        new TLcdLonLatPoint(fOffsetX - 0.008589495325651342, fOffsetY + 0.004497708087676244),
        new TLcdLonLatPoint(fOffsetX - 0.01157179480142645, fOffsetY + 6.610379170979286E-5),
        new TLcdLonLatPoint(fOffsetX - 0.008886998309776573, fOffsetY - 0.003318671637323689),
        new TLcdLonLatPoint(fOffsetX - 0.005470471790246734, fOffsetY - 5.085214064948218E-4),
        new TLcdLonLatPoint(fOffsetX - 0.002506400817239296, fOffsetY + 1.6838266862606588E-4),
        new TLcdLonLatPoint(fOffsetX - 0.0014405972179076798, fOffsetY - 0.005110558837600365),
        new TLcdLonLatPoint(fOffsetX + 0.004274696265866851, fOffsetY - 0.00672080381941953),
        new TLcdLonLatPoint(fOffsetX + 0.005942127509499073, fOffsetY + 0.0034689756244361547),
        new TLcdLonLatPoint(fOffsetX + 0.0024585284493809922, fOffsetY + 0.010083379076029075),
        new TLcdLonLatPoint(fOffsetX - 0.002445983413281283, fOffsetY + 0.00453584628782977)};
    TLcdLonLatPolygon polygon2 = new TLcdLonLatPolygon(new TLcd2DEditablePointList(polygonPoints2, false));
    polygon2.move2D(polygon2Center);
    aModel.addElement(new TLcdExtrudedShape(polygon2, 989.2093409784138, 1501.7914562951773), ILcdModel.NO_EVENT);

    TLcdLonLatPoint arcBandCenter = new TLcdLonLatPoint(fOffsetX + 0.028236389450853494, fOffsetY - 0.007161839835440276);
    TLcdLonLatArcBand arcBand = new TLcdLonLatArcBand(arcBandCenter, 900, 1400, -80, 220, aEllipsoid);
    aModel.addElement(new TLcdExtrudedShape(arcBand, 1307.1313374652527, 1818.4471482951194), ILcdModel.NO_EVENT);
  }
}
