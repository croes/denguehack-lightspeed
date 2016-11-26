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
package samples.lightspeed.style.strokedline;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.view.lightspeed.style.ALspStyle;

public class ModelFactory {

  static ILcdModel createCartographyModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor("line", "ComplexStroke", "Cartography");
    TLcdGeodeticReference reference = new TLcdGeodeticReference();
    ILcdModel model = new TLcdVectorModel(reference, descriptor);

    List<ALspStyle> style1 = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.RAILROAD, 1, Color.black);
    TLcdLonLatPolyline line1 = StyledShapeFactory.createPolyline(style1);
    line1.insert2DPoint(0, -122.428905, 37.667606);
    line1.insert2DPoint(1, -122.425062, 37.670679);
    line1.insert2DPoint(2, -122.431684, 37.70335);
    line1.insert2DPoint(3, -122.417249, 37.705165);
    line1.insert2DPoint(4, -122.403339, 37.695521);
    line1.insert2DPoint(5, -122.401998, 37.688811);
    line1.insert2DPoint(6, -122.394123, 37.695152);
    model.addElement(line1, ILcdModel.NO_EVENT);

    List<ALspStyle> style2 = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.HIGHWAY, 1, Color.black);
    TLcdLonLatPolyline line2 = StyledShapeFactory.createPolyline(style2);
    line2.insert2DPoint(0, -122.426186, 37.701126);
    line2.insert2DPoint(1, -122.417986, 37.702223);
    line2.insert2DPoint(2, -122.417047, 37.697553);
    line2.insert2DPoint(3, -122.410464, 37.698399);
    line2.insert2DPoint(4, -122.407621, 37.684359);
    line2.insert2DPoint(5, -122.420736, 37.674101);
    model.addElement(line2, ILcdModel.NO_EVENT);

    List<ALspStyle> style3 = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.POWER_LINE, 1, Color.black);
    TLcdLonLatPolyline line3 = StyledShapeFactory.createPolyline(style3);
    line3.insert2DPoint(0, -122.424529, 37.692846);
    line3.insert2DPoint(1, -122.414669, 37.694118);
    line3.insert2DPoint(2, -122.412996, 37.685784);
    line3.insert2DPoint(3, -122.422809, 37.684592);
    model.addElement(line3, ILcdModel.NO_EVENT);

    List<ALspStyle> style4 = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.FENCE, 1, Color.black);
    TLcdLonLatPolyline line4 = StyledShapeFactory.createPolyline(style4);
    line4.insert2DPoint(0, -122.417164, 37.674279);
    line4.insert2DPoint(1, -122.410948, 37.679174);
    line4.insert2DPoint(2, -122.406279, 37.675471);
    line4.insert2DPoint(3, -122.412494, 37.670608);
    model.addElement(line4, ILcdModel.NO_EVENT);

    List<ALspStyle> style5 = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.PIPELINE, 1, Color.black);
    TLcdLonLatPolyline line5 = StyledShapeFactory.createPolyline(style5);
    int circlePoints = 32;
    for (int j = 0; j <= circlePoints; j++) {
      double angle = Math.toRadians(j * (360.0 / (double) circlePoints));
      double sinAngle = Math.sin(angle);
      double cosAngle = Math.cos(angle);
      line5.insert2DPoint(line5.getPointCount(), -122.3975 + 0.005 * cosAngle, 37.678 + 0.005 * sinAngle);
    }
    model.addElement(line5, ILcdModel.NO_EVENT);

    return model;
  }

  public static ILcdModel createMeteoModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor("line", "ComplexStroke", "Meteo");
    TLcdGeodeticReference reference = new TLcdGeodeticReference();
    ILcdModel model = new TLcdVectorModel(reference, descriptor);

    List<ALspStyle> warmFront = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.WARM_FRONT, 1, Color.red);
    List<ALspStyle> coldFront = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.COLD_FRONT, 1, Color.blue);
    List<ALspStyle> occludedFront = ComplexStrokeFactory.create(ComplexStrokeFactory.ComplexStrokeType.OCCLUDED_FRONT, 1, Color.magenta);

    TLcdLonLatPolyline line1 = StyledShapeFactory.createPolyline(warmFront);
    line1.insert2DPoint(0, -4.673789, 58.293103);
    line1.insert2DPoint(1, -3.700088, 56.662962);
    line1.insert2DPoint(2, -3.148032, 54.511178);
    line1.insert2DPoint(3, -3.048032, 52.228981);
    line1.insert2DPoint(4, -3.504471, 50.011991);
    line1.insert2DPoint(5, -4.347761, 48.121028);
    model.addElement(line1, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line2 = StyledShapeFactory.createPolyline(coldFront);
    line2.insert2DPoint(0, -4.673789, 58.293103);
    line2.insert2DPoint(1, -6.564751, 57.836663);
    line2.insert2DPoint(2, -9.172975, 57.510635);
    line2.insert2DPoint(3, -12.36805, 58.167075);
    line2.insert2DPoint(4, -15.10668, 59.532009);
    model.addElement(line2, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line3 = StyledShapeFactory.createPolyline(occludedFront);
    line3.insert2DPoint(0, 19.134031, 58.388174);
    line3.insert2DPoint(1, 20.637676, 57.707278);
    line3.insert2DPoint(2, 21.375313, 56.969640);
    line3.insert2DPoint(3, 21.999468, 56.061779);
    line3.insert2DPoint(4, 22.339916, 54.898582);
    line3.insert2DPoint(5, 22.368287, 53.480048);
    line3.insert2DPoint(6, 21.772503, 51.976403);
    line3.insert2DPoint(7, 20.439081, 50.501128);
    line3.insert2DPoint(8, 18.396393, 49.451413);
    line3.insert2DPoint(9, 16.183481, 48.827259);
    line3.insert2DPoint(10, 13.38816, 48.512262);
    model.addElement(line3, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line4 = StyledShapeFactory.createPolyline(occludedFront);
    line4.insert2DPoint(0, 15.327414, 40.298086);
    line4.insert2DPoint(1, 17.067356, 41.861997);
    line4.insert2DPoint(2, 18.933293, 42.448413);
    line4.insert2DPoint(3, 20.455234, 42.328869);
    line4.insert2DPoint(4, 21.587146, 41.800644);
    line4.insert2DPoint(5, 22.280205, 41.014902);
    line4.insert2DPoint(6, 22.926954, 39.189374);
    model.addElement(line4, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line5 = StyledShapeFactory.createPolyline(coldFront);
    line5.insert2DPoint(0, 22.926954, 39.189374);
    line5.insert2DPoint(1, 22.963353, 37.554087);
    line5.insert2DPoint(2, 22.402761, 36.136018);
    line5.insert2DPoint(3, 21.153038, 34.913156);
    line5.insert2DPoint(4, 19.061050, 34.036618);
    line5.insert2DPoint(5, 16.387620, 33.449768);
    line5.insert2DPoint(6, 13.388162, 33.384562);
    line5.insert2DPoint(7, 9.9322658, 33.514973);
    model.addElement(line5, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line6 = StyledShapeFactory.createPolyline(occludedFront);
    line6.insert2DPoint(0, -21.431629, 61.227355);
    line6.insert2DPoint(1, -17.606143, 56.597757);
    line6.insert2DPoint(2, -15.171891, 54.250355);
    line6.insert2DPoint(3, -11.520377, 51.446514);
    line6.insert2DPoint(4, -9.5081815, 48.838290);
    line6.insert2DPoint(5, -8.1540694, 45.838832);
    line6.insert2DPoint(6, -7.6028358, 42.643758);
    line6.insert2DPoint(7, -7.9340694, 38.535805);
    line6.insert2DPoint(8, -8.7165367, 34.688674);
    model.addElement(line6, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line7 = StyledShapeFactory.createPolyline(warmFront);
    line7.insert2DPoint(0, 3.346499, 50.207608);
    line7.insert2DPoint(1, 4.846228, 48.968701);
    line7.insert2DPoint(2, 5.889518, 47.403767);
    line7.insert2DPoint(3, 6.541574, 45.838832);
    line7.insert2DPoint(4, 6.932808, 43.882664);
    line7.insert2DPoint(5, 6.932808, 43.882664);
    model.addElement(line7, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line8 = StyledShapeFactory.createPolyline(occludedFront);
    line8.insert2DPoint(0, 3.346499, 50.207608);
    line8.insert2DPoint(1, 2.824855, 48.838290);
    line8.insert2DPoint(2, 1.846771, 47.729795);
    line8.insert2DPoint(3, 0.281836, 46.686505);
    model.addElement(line8, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line9 = StyledShapeFactory.createPolyline(coldFront);
    line9.insert2DPoint(0, 3.346499, 50.207608);
    line9.insert2DPoint(1, 3.998555, 47.142944);
    line9.insert2DPoint(2, 3.737733, 44.534720);
    line9.insert2DPoint(3, 3.020471, 42.252524);
    line9.insert2DPoint(4, 1.914225, 40.222827);
    line9.insert2DPoint(5, 0.294742, 38.622931);
    line9.insert2DPoint(6, -1.123905, 37.411870);
    line9.insert2DPoint(7, -2.059689, 35.673985);
    line9.insert2DPoint(8, -2.326387, 33.514973);
    line9.insert2DPoint(9, -1.869948, 30.841544);
    model.addElement(line9, ILcdModel.NO_EVENT);

    TLcdLonLatPolyline line10 = StyledShapeFactory.createPolyline(warmFront);
    line10.insert2DPoint(0, 1.914225, 40.222827);
    line10.insert2DPoint(1, 2.824855, 37.688132);
    line10.insert2DPoint(2, 3.802939, 35.992786);
    line10.insert2DPoint(3, 5.107051, 34.558263);
    model.addElement(line10, ILcdModel.NO_EVENT);

    return model;
  }

  static ILcdModel createAirRouteModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor("line", "ComplexStroke", "Routes");
    TLcdGeodeticReference reference = new TLcdGeodeticReference();
    ILcdModel model = new TLcdVectorModel(reference, descriptor);

    List<ALspStyle> destinationIconStyle = IconFactory.createDestinationIconStyle();

    // Generate points
    ILcdPoint[] points = new ILcdPoint[]{
        StyledShapeFactory.createPoint(-76.56, 37.95, destinationIconStyle),
        StyledShapeFactory.createPoint(-75.03, 41.94, destinationIconStyle),
        StyledShapeFactory.createPoint(-76.11, 36.33, destinationIconStyle),
        StyledShapeFactory.createPoint(-77.22, 40.39, destinationIconStyle),
        StyledShapeFactory.createPoint(-78.35, 38.58, destinationIconStyle),
        StyledShapeFactory.createPoint(-76.50, 41.44, destinationIconStyle),
        StyledShapeFactory.createPoint(-75.05, 39.63, destinationIconStyle)
    };

    Random random = new Random(-1568530430);
    for (int i = 0; i < points.length; i++) {
      model.addElement(points[i], ILcdModel.NO_EVENT);
      if (i > 0) {
        List<ALspStyle> routeStyle = ComplexStrokeFactory.createRouteStyle(points[0], points[i], random, 40);
        TLcdLonLatPolyline route = StyledShapeFactory.createPolyline(routeStyle);
        route.insert2DPoint(0, points[0].getX(), points[0].getY());
        route.insert2DPoint(1, points[i].getX(), points[i].getY());
        model.addElement(route, ILcdModel.NO_EVENT);
      }
    }
    return model;
  }

  static ILcdModel createProcedureModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor("line", "ComplexStroke", "Procedure");
    TLcdGeodeticReference reference = new TLcdGeodeticReference();
    ILcdModel model = new TLcdVectorModel(reference, descriptor);

    List<ALspStyle> style1 = ComplexStrokeFactory.createProcedureStyle(null, null, "25", "PITES 3N", true, Color.black, 0, 8);
    TLcdLonLatPolyline line1 = StyledShapeFactory.createPolyline(style1);
    line1.insert2DPoint(0, 4.493391, 50.892082);
    line1.insert2DPoint(1, 4.489809, 50.883439);
    line1.insert2DPoint(2, 4.489747, 50.883147);
    line1.insert2DPoint(3, 4.489765, 50.882883);
    line1.insert2DPoint(4, 4.489891, 50.882553);
    line1.insert2DPoint(5, 4.490121, 50.882256);
    line1.insert2DPoint(6, 4.505367, 50.866198);
    model.addElement(line1, ILcdModel.NO_EVENT);

    List<ALspStyle> style2 = ComplexStrokeFactory.createProcedureStyle("142°", "R322°", "10", "PITES 3L", true, Color.black, 0, 16);
    TLcdLonLatPolyline line2 = StyledShapeFactory.createPolyline(style2);
    line2.insert2DPoint(0, 4.493391, 50.892082);
    line2.insert2DPoint(1, 4.490546, 50.885066);
    line2.insert2DPoint(2, 4.490502, 50.884790);
    line2.insert2DPoint(3, 4.490542, 50.884554);
    line2.insert2DPoint(4, 4.490668, 50.884311);
    line2.insert2DPoint(5, 4.490891, 50.884086);
    line2.insert2DPoint(6, 4.503783, 50.873391);
    model.addElement(line2, ILcdModel.NO_EVENT);

    List<ALspStyle> style3 = ComplexStrokeFactory.createProcedureStyle("108°", null, "12", null, true, Color.black, 16, 8);
    TLcdLonLatPolyline line3 = StyledShapeFactory.createPolyline(style3);
    line3.insert2DPoint(0, 4.503783, 50.873391);
    line3.insert2DPoint(1, 4.507086, 50.870842);
    line3.insert2DPoint(2, 4.507848, 50.870347);
    line3.insert2DPoint(3, 4.508674, 50.869982);
    line3.insert2DPoint(4, 4.509715, 50.869677);
    line3.insert2DPoint(5, 4.510777, 50.869521);
    line3.insert2DPoint(6, 4.515076, 50.869128);
    model.addElement(line3, ILcdModel.NO_EVENT);

    List<ALspStyle> style4 = ComplexStrokeFactory.createProcedureStyle("130°", "R310°", "65", null, true, Color.black, 8, 16);
    TLcdLonLatPolyline line4 = StyledShapeFactory.createPolyline(style4);
    line4.insert2DPoint(0, 4.505367, 50.866198);
    line4.insert2DPoint(1, 4.538190, 50.857604);
    model.addElement(line4, ILcdModel.NO_EVENT);

    List<ALspStyle> style5 = ComplexStrokeFactory.createProcedureStyle("135°", "R315°", "49", null, true, Color.black, 8, 16);
    TLcdLonLatPolyline line5 = StyledShapeFactory.createPolyline(style5);
    line5.insert2DPoint(0, 4.515076, 50.869128);
    line5.insert2DPoint(1, 4.538190, 50.857604);
    model.addElement(line5, ILcdModel.NO_EVENT);

    List<ALspStyle> style6 = ComplexStrokeFactory.createProcedureStyle("117°", "R243°", "17", null, true, Color.black, 16, 8);
    TLcdLonLatPolyline line6 = StyledShapeFactory.createPolyline(style6);
    line6.insert2DPoint(0, 4.538190, 50.857604);
    line6.insert2DPoint(1, 4.553632, 50.854226);
    model.addElement(line6, ILcdModel.NO_EVENT);

    List<ALspStyle> style7 = ComplexStrokeFactory.createProcedureStyle(null, "R288°", null, null, true, Color.darkGray, 16, 8);
    TLcdLonLatPolyline line7 = StyledShapeFactory.createPolyline(style7);
    line7.insert2DPoint(0, 4.527636, 50.867479);
    line7.insert2DPoint(1, 4.515076, 50.869128);
    model.addElement(line7, ILcdModel.NO_EVENT);

    List<ALspStyle> crossIconStyle = IconFactory.createCrossIconStyle();
    List<ALspStyle> triangleIconStyle = IconFactory.createTriangleIconStyle();
    List<ALspStyle> navaidIconStyle = IconFactory.createNavaidIconStyle();

    TLcdLonLatPoint point1 = StyledShapeFactory.createPoint(4.505367, 50.866198, crossIconStyle);
    model.addElement(point1, ILcdModel.NO_EVENT);

    TLcdLonLatPoint point2 = StyledShapeFactory.createPoint(4.515076, 50.869128, triangleIconStyle);
    model.addElement(point2, ILcdModel.NO_EVENT);

    TLcdLonLatPoint point3 = StyledShapeFactory.createPoint(4.538190, 50.857604, navaidIconStyle);
    model.addElement(point3, ILcdModel.NO_EVENT);

    TLcdLonLatPoint point4 = StyledShapeFactory.createPoint(4.553632, 50.854226, triangleIconStyle);
    model.addElement(point4, ILcdModel.NO_EVENT);

    TLcdLonLatPoint point5 = StyledShapeFactory.createPoint(4.503783, 50.873391, navaidIconStyle);
    model.addElement(point5, ILcdModel.NO_EVENT);

    TLcdLonLatPoint point6 = StyledShapeFactory.createPoint(4.527636, 50.867479, navaidIconStyle);
    model.addElement(point6, ILcdModel.NO_EVENT);

    return model;
  }

  static ILcdModel createAirspaceModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor("line", "ComplexStroke", "Airspace");
    TLcdGeodeticReference reference = new TLcdGeodeticReference();
    ILcdModel model = new TLcdVectorModel(reference, descriptor);

    List<ALspStyle> restrictedStyle1 = ComplexStrokeFactory.createAirspaceStyle(true, new Color(123, 15, 6));
    List<ALspStyle> restrictedStyle2 = ComplexStrokeFactory.createAirspaceStyle(true, new Color(15, 2, 101));
    List<ALspStyle> regularStyle = ComplexStrokeFactory.createAirspaceStyle(false, new Color(15, 2, 101));
    TLcdLonLatPolygon shape1 = StyledShapeFactory.createPolygon(restrictedStyle1);
    shape1.insert2DPoint(0, -078.23569716, 37.69091874);
    shape1.insert2DPoint(1, -078.20545147, 37.22880680);
    shape1.insert2DPoint(2, -078.04822336, 36.66902675);
    shape1.insert2DPoint(3, -077.78533153, 36.69059910);
    shape1.insert2DPoint(4, -077.86481368, 37.20155578);
    shape1.insert2DPoint(5, -077.54688508, 37.42183488);
    shape1.insert2DPoint(6, -077.82620807, 37.73522165);
    model.addElement(shape1, ILcdModel.NO_EVENT);

    TLcdLonLatPolygon shape2 = StyledShapeFactory.createPolygon(restrictedStyle2);
    shape2.insert2DPoint(0, -077.54688508, 37.42183488);
    shape2.insert2DPoint(1, -077.82620807, 37.73522165);
    shape2.insert2DPoint(2, -078.23569716, 37.69091874);
    shape2.insert2DPoint(3, -078.16230402, 38.05087933);
    shape2.insert2DPoint(4, -076.94509165, 38.06223392);
    shape2.insert2DPoint(5, -076.89015390, 37.52395396);
    shape2.insert2DPoint(6, -077.18353811, 37.37414559);
    model.addElement(shape2, ILcdModel.NO_EVENT);

    TLcdLonLatPolygon shape3 = StyledShapeFactory.createPolygon(regularStyle);
    shape3.insert2DPoint(0, -077.54688508, 37.42183488);
    shape3.insert2DPoint(1, -077.86481368, 37.20155578);
    shape3.insert2DPoint(2, -077.78533153, 36.69059910);
    shape3.insert2DPoint(3, -078.04822336, 36.66902675);
    shape3.insert2DPoint(4, -077.95337951, 36.32725212);
    shape3.insert2DPoint(5, -076.76568908, 36.27502099);
    shape3.insert2DPoint(6, -076.92657458, 37.07623741);
    shape3.insert2DPoint(7, -077.18353811, 37.37414559);
    model.addElement(shape3, ILcdModel.NO_EVENT);

    ILcdEllipsoid ellipsoid = reference.getGeodeticDatum().getEllipsoid();
    TLcdLonLatPoint center = new TLcdLonLatPoint(-076.04822336, 37.07902675);
    ILcdShape shape4 = StyledShapeFactory.createCircle(center, 50000, ellipsoid, restrictedStyle1);
    model.addElement(shape4, ILcdModel.NO_EVENT);

    return model;
  }
}
