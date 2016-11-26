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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.TLcdInterval;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

public class BuildingLayerFactory extends AbstractLayerFactory {
  private static final String BUILDINGS_HEIGHT_FIELD_NAME = "HEIGHT";
  private Map<ILcdDataObject, TLcdExtrudedShape> fExtrudedShapeMap;
  private Properties fProperties;

  public BuildingLayerFactory() {
    fExtrudedShapeMap = new TLcdWeakIdentityHashMap<ILcdDataObject, TLcdExtrudedShape>();
  }

  @Override
  public void configure(Properties aProperties) {
    fProperties = aProperties;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().toLowerCase().endsWith(".shp");
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    final List<ALspStyle> styles = createStyles();
    final ALspStyleTargetProvider provider = createStyleTargetProvider();

    ALspStyler styler = new ALspStyler() {
      @Override
      public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
        aStyleCollector.objects(aObjects).geometry(provider).styles(styles).submit();
      }
    };

    double minScale = Double.parseDouble(fProperties.getProperty("body.minScale", "0.0"));
    double maxScale = Double.parseDouble(fProperties.getProperty("body.maxScale", "" + Double.MAX_VALUE));

    return Collections.<ILspLayer>singletonList(TLspShapeLayerBuilder.newBuilder()
                                                                     .model(aModel)
                                                                     .bodyStyler(TLspPaintState.REGULAR, styler)
                                                                     .selectable(false)
                                                                     .bodyScaleRange(new TLcdInterval(minScale, maxScale))
                                                                     .build());
  }

  /**
   * Creates the styles we use for the buildings.
   *
   * @return the styles
   */
  protected List<ALspStyle> createStyles() {
    final ALspStyle lineStyle = TLspLineStyle.newBuilder()
                                             .color(getColor(fProperties, "body.lineColor", "FFFFC800"))
                                             .elevationMode(ABOVE_TERRAIN)
                                             .build();
    final ALspStyle fillStyle = TLspFillStyle.newBuilder()
                                             .color(getColor(fProperties, "body.fillColor", "FFFFC800"))
                                             .elevationMode(ABOVE_TERRAIN)
                                             .build();
    final List<ALspStyle> styles = new ArrayList<ALspStyle>();
    //only use a linestyle if this is configured in properties
    if (fProperties.containsKey("body.lineColor")) {
      styles.add(lineStyle);
    }
    styles.add(fillStyle);
    return styles;
  }

  /**
   * Creates the style target provider for the buildings.
   * <p/>
   * The style target provider uses the building's {@code HEIGHT} property to extrude a 3D building
   * from the ground surface.
   *
   * @return the style target provider.
   */
  protected ALspStyleTargetProvider createStyleTargetProvider() {
    return new ALspStyleTargetProvider() {
      @Override
      public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
        if (aObject instanceof ILcdShape) {
          ILcdDataObject dataObject = (ILcdDataObject) aObject;

          if (!fExtrudedShapeMap.containsKey(dataObject)) {
            Object value = dataObject.getValue(BUILDINGS_HEIGHT_FIELD_NAME);
            double height = ((Number) value).doubleValue();
            fExtrudedShapeMap.put(dataObject, new TLcdExtrudedShape((ILcdShape) aObject, 0, height));
          }
          aResultSFCT.add(fExtrudedShapeMap.get(dataObject));
        }
      }
    };
  }
}
