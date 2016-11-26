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
package samples.lightspeed.internal.printing;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import com.luciad.format.object3d.TLcd3DMeshBuilder;
import com.luciad.format.object3d.TLcd3DMeshStyle;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.shape.shape2D.TLcdXYArc;
import com.luciad.shape.shape2D.TLcdXYArcBand;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.painter.mesh.ILsp3DIcon;
import com.luciad.view.lightspeed.painter.mesh.TLspMesh3DIcon;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;

/**
 * @author Daniel Balog
 * @since 2012.1
 */
public class ModelFactory {
  private ModelFactory() {
  }

  public static ILcdModel createAnimatedModel() {
    final TLcdVectorModel vectorModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("Animated", "Animated", "Animated"));

    final TLcdLonLatPoint pointToAnimate = new TLcdLonLatPoint(-10, 52);
    vectorModel.addElement(pointToAnimate, ILcdModel.FIRE_NOW);

    ALcdAnimationManager.getInstance().putAnimation(vectorModel, new ALcdAnimation(15.0) {
      @Override
      protected void setTimeImpl(double aTime) {
        try (Lock autoUnlock = writeLock(vectorModel)) {
          pointToAnimate.move2D(-10 + aTime * 4, pointToAnimate.getLat());
          vectorModel.elementChanged(pointToAnimate, ILcdModel.FIRE_NOW);
        }
      }

      @Override
      public boolean isLoop() {
        return true;
      }
    });
    return vectorModel;
  }

  public static ILcdModel createVariousIconsModel() {
    final TLcdVectorModel vectorModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("Icons", "Icons", "Icons"));

    vectorModel.addElement(new PointDomainObject(0, 30, TLspIconStyle.ScalingMode.WORLD_SCALING, ILspWorldElevationStyle.ElevationMode.ON_TERRAIN, false), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(3, 30, TLspIconStyle.ScalingMode.VIEW_SCALING, ILspWorldElevationStyle.ElevationMode.ON_TERRAIN, false), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(6, 30, TLspIconStyle.ScalingMode.WORLD_SCALING_CLAMPED, ILspWorldElevationStyle.ElevationMode.ON_TERRAIN, false), ILcdModel.FIRE_NOW);

    vectorModel.addElement(new PointDomainObject(0, 33, TLspIconStyle.ScalingMode.WORLD_SCALING, ILspWorldElevationStyle.ElevationMode.ON_TERRAIN, true), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(3, 33, TLspIconStyle.ScalingMode.VIEW_SCALING, ILspWorldElevationStyle.ElevationMode.ON_TERRAIN, true), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(6, 33, TLspIconStyle.ScalingMode.WORLD_SCALING_CLAMPED, ILspWorldElevationStyle.ElevationMode.ON_TERRAIN, true), ILcdModel.FIRE_NOW);

    vectorModel.addElement(new PointDomainObject(10, 30, TLspIconStyle.ScalingMode.WORLD_SCALING, ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN, false), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(13, 30, TLspIconStyle.ScalingMode.VIEW_SCALING, ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN, false), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(16, 30, TLspIconStyle.ScalingMode.WORLD_SCALING_CLAMPED, ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN, false), ILcdModel.FIRE_NOW);

    vectorModel.addElement(new PointDomainObject(10, 33, TLspIconStyle.ScalingMode.WORLD_SCALING, ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN, true), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(13, 33, TLspIconStyle.ScalingMode.VIEW_SCALING, ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN, true), ILcdModel.FIRE_NOW);
    vectorModel.addElement(new PointDomainObject(16, 33, TLspIconStyle.ScalingMode.WORLD_SCALING_CLAMPED, ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN, true), ILcdModel.FIRE_NOW);
    return vectorModel;
  }

  static ILcdModel createVerticalLinesModel() {
    return createPointsModel(
        "Vertical lines", 100,
        5.0, 10.0, 5.0, 5.0, 10e3,
        Arrays.asList(
            TLspIconStyle.newBuilder().elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN).build(),
            TLspVerticalLineStyle.newBuilder().build()
        )
    );
  }

  static ILcdModel create3dIconsModel(ILspView aView) {
    ILsp3DIcon[] icons = {
        new TLspMesh3DIcon(new TLcd3DMeshBuilder().arrow(1.0, 2.0, 1.0, 4.0).orientation(TLcd3DMeshBuilder.AxisOrientation.X_ALIGNED).style(create3DMeshStyle(new Color(154, 103, 200))).build()),
        new TLspMesh3DIcon(new TLcd3DMeshBuilder().arrow(1.0, 2.0, 1.0, 4.0).orientation(TLcd3DMeshBuilder.AxisOrientation.X_ALIGNED).style(create3DMeshStyle(new Color(232, 162, 21))).build())
    };
    return createPointsModel(
        "3D Icons", 30,
        12.5, 10.0, 5.0, 5.0, 0.0,
        Arrays.asList(TLsp3DIconStyle.newBuilder().icon(icons[0]).recenterIcon(true).iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING).worldSize(2e4).build()),
        Arrays.asList(TLsp3DIconStyle.newBuilder().icon(icons[1]).recenterIcon(true).iconSizeMode(TLsp3DIconStyle.ScalingMode.SCALE_FACTOR).scale(1e4).build()),
        Arrays.asList(TLsp3DIconStyle.newBuilder().icon(icons[0]).recenterIcon(true).iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING).worldSize(2e4).verticalOffsetFactor(2.0).build()),
        Arrays.asList(TLsp3DIconStyle.newBuilder().icon(icons[1]).recenterIcon(true).iconSizeMode(TLsp3DIconStyle.ScalingMode.SCALE_FACTOR).scale(1e4).verticalOffsetFactor(2.0).build())
    );
  }

  private static TLcd3DMeshStyle create3DMeshStyle(Color aColor) {
    TLcd3DMeshStyle style = new TLcd3DMeshStyle();
    style.setDiffuseColor(aColor);
    return style;
  }

  private static ILcdModel createPointsModel(String aName, int aCount, double aX, double aY, double aWidth, double aHeight, double aDepth, List<? extends ALspStyle>... aStyles) {
    ILcdModel pointsModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("Points", "Points", aName));
    for (int i = 0; i < aCount; i++) {
      double lon = aX + aWidth * Math.random();
      double lat = aY + aHeight * Math.random();
      double height = aDepth * Math.random();
      double orientation = 360.0 * Math.random();
      List<? extends ALspStyle> currStyles = aStyles[i % aStyles.length];
      pointsModel.addElement(new PointDomainObject(lon, lat, height, orientation, currStyles), ILcdModel.NO_EVENT);
    }
    return pointsModel;
  }

  public static ILcdModel createPolygonFillLayer() {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("Polygon fill", "polygon fill", "Polygon fill"));
    TLcdLonLatPolygon polygon = new TLcdLonLatPolygon();
    model.addElement(polygon, ILcdModel.FIRE_NOW);
    polygon.insert2DPoint(0, -5, -5);
    polygon.insert2DPoint(1, -5, 1);
    polygon.insert2DPoint(2, 2, 1);
    polygon.insert2DPoint(3, 1, -5);
    return model;
  }

  public static ILcdModel createStrokedLinesModel(double aOffsetX, double aOffsetY) {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("Lines", "Lines", "Lines"));
    TLcdLonLatPolyline polygon = new TLcdLonLatPolyline();
    model.addElement(polygon, ILcdModel.FIRE_NOW);
    polygon.insert2DPoint(0, 3 + aOffsetX, 1 + aOffsetY);
    polygon.insert2DPoint(1, 0 + aOffsetX, 6 + aOffsetY);
    polygon.insert2DPoint(2, 7 + aOffsetX, 5 + aOffsetY);
    polygon.insert2DPoint(3, 6 + aOffsetX, 1 + aOffsetY);
    return model;
  }

  public static ILcdModel createLinesModel(int aId) {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("Lines", "Lines", "Lines"));
    TLcdLonLatPolyline p1 = new TLcdLonLatPolyline();
    model.addElement(p1, ILcdModel.FIRE_NOW);
    p1.insert2DPoint(0, -79, -31);
    p1.insert2DPoint(1, -75, -30);
    p1.insert2DPoint(2, -75, -25);
    p1.insert2DPoint(3, -79, -24);
    TLcdLonLatPolyline p2 = new TLcdLonLatPolyline();
    model.addElement(p2, ILcdModel.FIRE_NOW);
    p2.insert2DPoint(0, -71, -24);
    p2.insert2DPoint(1, -75, -25);
    p2.insert2DPoint(2, -75, -30);
    p2.insert2DPoint(3, -71, -31);
    double totW = (p1.getBounds().getWidth() + p2.getBounds().getWidth()) * 1.2;
    p1.translate2D(totW * aId, 0.0);
    p2.translate2D(totW * aId, 0.0);
    return model;
  }

  public static ILcdModel createPaintInViewModel() {
    TLcdVectorModel model = new TLcdVectorModel(
        null,
        new TLcdModelDescriptor("Paint in view", "Paint in view", "Paint in view")
    );

    model.addElement(new TLcdXYArc(100, 100, 30, 40, 30, 10, 100), ILcdModel.NO_EVENT);
    model.addElement(new TLcdXYArcBand(200, 100, 30, 50, 10, 100), ILcdModel.NO_EVENT);
    model.addElement(new TLcdXYArc(200, 200, 30, 40, 30, 10, 100), ILcdModel.NO_EVENT);
    model.addElement(new TLcdXYArcBand(300, 200, 30, 50, 10, 100), ILcdModel.NO_EVENT);
    model.addElement(new TLcdXYArc(300, 300, 30, 40, 30, 10, 100), ILcdModel.NO_EVENT);
    model.addElement(new TLcdXYArcBand(400, 300, 30, 50, 10, 100), ILcdModel.NO_EVENT);
    model.addElement(new TLcdXYArc(400, 400, 30, 40, 30, 10, 100), ILcdModel.NO_EVENT);
    model.addElement(new TLcdXYArcBand(500, 400, 30, 50, 10, 100), ILcdModel.NO_EVENT);

    return model;
  }
}
