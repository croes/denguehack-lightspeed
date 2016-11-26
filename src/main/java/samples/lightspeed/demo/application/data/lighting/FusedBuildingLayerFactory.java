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
package samples.lightspeed.demo.application.data.lighting;

import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.fusion.client.view.lightspeed.TLspFusionGeometryProvider;
import com.luciad.fusion.client.view.lightspeed.TLspFusionVectorLayerBuilder;
import com.luciad.fusion.tilestore.model.vector.ILfnTiledSurface;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.ILcdSurface;
import com.luciad.shape.TLcdShapeList;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

public class FusedBuildingLayerFactory extends AbstractLayerFactory {
  private static final String BUILDINGS_HEIGHT_FIELD_NAME = "HEIGHT";
  private Properties fProperties;

  @Override
  public void configure(Properties aProperties) {
    fProperties = aProperties;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    final ALspStyle lineStyle = TLspLineStyle.newBuilder()
                                             .color(getColor(fProperties, "body.lineColor", "FFFFC800"))
                                             .elevationMode(ABOVE_TERRAIN)
                                             .build();
    final ALspStyle fillStyle = TLspFillStyle.newBuilder()
                                             .color(getColor(fProperties, "body.fillColor", "FFFFC800"))
                                             .elevationMode(ABOVE_TERRAIN)
                                             .build();

    final ALspStyleTargetProvider extrudedShapeProvider = new ALspStyleTargetProvider() {
      @Override
      public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
        Object levelGeometry = TLspFusionGeometryProvider.getGeometry(aObject, aContext);
        if (levelGeometry instanceof ILcdShapeList) {
          if (((ILcdShapeList) levelGeometry).getShapeCount() == 0) {
            return;
          }
          levelGeometry = ((ILcdShapeList) levelGeometry).getShape(0);
        }
        if (levelGeometry instanceof ILfnTiledSurface) {
          Collection<? extends ILcdSurface> fills = ((ILfnTiledSurface) levelGeometry).getFills();
          levelGeometry = new TLcdShapeList(fills.toArray(new ILcdShape[fills.size()]));
        }
        if (levelGeometry == null) {
          return;
        }

        ILcdDataObject dataObject = (ILcdDataObject) aObject;
        float height = (Float) dataObject.getValue(BUILDINGS_HEIGHT_FIELD_NAME);
        if (height < 0.1) {
          return;
        }
        height /= 3f; // defined in feet

        aResultSFCT.add(new TLcdExtrudedShape((ILcdShape) levelGeometry, -3, height));
      }
    };

    final ALspStyleTargetProvider skipFlatBuildingsProvider = new ALspStyleTargetProvider() {
      @Override
      public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
        Object levelGeometry = TLspFusionGeometryProvider.getGeometry(aObject, aContext);
        if (levelGeometry instanceof ILcdShapeList) {
          if (((ILcdShapeList) levelGeometry).getShapeCount() == 0) {
            return;
          }
          levelGeometry = ((ILcdShapeList) levelGeometry).getShape(0);
        }
        if (levelGeometry instanceof ILfnTiledSurface) {
          Collection<? extends ILcdSurface> fills = ((ILfnTiledSurface) levelGeometry).getFills();
          levelGeometry = new TLcdShapeList(fills.toArray(new ILcdShape[fills.size()]));
        }
        if (levelGeometry == null) {
          return;
        }

        ILcdDataObject dataObject = (ILcdDataObject) aObject;
        float height = (Float) dataObject.getValue(BUILDINGS_HEIGHT_FIELD_NAME);
        if (height < 0.1) {
          return;
        }
        aResultSFCT.add(levelGeometry);
      }
    };

    ALspStyler styler = new ALspStyler() {
      @Override
      public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
        if (aContext.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D) {
          if (fProperties.containsKey("body.lineColor")) {
            //Only draw outlines if the lineColor is provided
            aStyleCollector.objects(aObjects).geometry(extrudedShapeProvider).styles(fillStyle, lineStyle).submit();
          } else {
            aStyleCollector.objects(aObjects).geometry(extrudedShapeProvider).style(fillStyle).submit();
          }

        } else {
          if (fProperties.containsKey("body.lineColor")) {
            //Only draw outlines if the lineColor is provided
            aStyleCollector.objects(aObjects).geometry(skipFlatBuildingsProvider).styles(fillStyle, lineStyle).submit();
          } else {
            aStyleCollector.objects(aObjects).geometry(skipFlatBuildingsProvider).style(fillStyle).submit();
          }
        }
      }
    };

    double minScale = Double.parseDouble(fProperties.getProperty("body.minScale", "0.0"));
    double maxScale = Double.parseDouble(fProperties.getProperty("body.maxScale", "" + Double.MAX_VALUE));

    ILspLayer layer = TLspFusionVectorLayerBuilder.newBuilder()
                                                  .model(aModel)
                                                  .bodyStyler(TLspPaintState.REGULAR, styler)
                                                  .bodyScaleRange(new TLcdInterval(minScale, maxScale))
                                                  .selectable(false)
                                                  .build();

    return Collections.<ILspLayer>singletonList(layer);
  }
}
