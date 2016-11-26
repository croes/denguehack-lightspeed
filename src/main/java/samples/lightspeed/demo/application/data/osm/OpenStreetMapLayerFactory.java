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
package samples.lightspeed.demo.application.data.osm;

import static com.luciad.view.lightspeed.layer.TLspPaintState.REGULAR;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.fusion.client.view.lightspeed.TLspFusionVectorLayerBuilder;
import com.luciad.fusion.tilestore.model.TLfnVectorTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for OpenStreetMap fusion vector data.
 *
 * @since 2012.1
 */
public class OpenStreetMapLayerFactory extends AbstractLayerFactory {

  public enum GeometryType {point, line, area}

  private final OpenStreetMapStyleProvider fStyleProvider = new OpenStreetMapStyleProvider();
  private Properties fProperties;

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fProperties = aProperties;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    String coverageId = ((TLfnVectorTileStoreModelDescriptor) aModel.getModelDescriptor()).getCoverageMetadata().getId();

    GeometryType geometryType = GeometryType.valueOf(fProperties.getProperty(coverageId + ".geometryType", "n/a"));
    double bodyMinScale = Double.valueOf(fProperties.getProperty(coverageId + ".body.minScale", "0.0"));
    double bodyMaxScale = Double.valueOf(fProperties.getProperty(coverageId + ".body.maxScale", "" + Double.MAX_VALUE));
    double labelMinScale = Double.valueOf(fProperties.getProperty(coverageId + ".label.minScale", "" + bodyMinScale));
    double labelMaxScale = Double.valueOf(fProperties.getProperty(coverageId + ".label.maxScale", "" + bodyMaxScale));
    boolean labeled = Boolean.valueOf(fProperties.getProperty(coverageId + ".labeled"));
    ILspLayer layer = TLspFusionVectorLayerBuilder.newBuilder().model(aModel)
                                                  .bodyScaleRange(new TLcdInterval(bodyMinScale, bodyMaxScale))
                                                  .labelScaleRange(new TLcdInterval(labelMinScale, labelMaxScale))
                                                  .bodyStyler(REGULAR, createBodyStyler(geometryType))
                                                  .labelStyler(TLspPaintState.REGULAR, labeled ? createLabelStyler(geometryType) : null)
                                                  .build();
    return Collections.<ILspLayer>singletonList(layer);
  }

  protected ILspStyler createBodyStyler(GeometryType aGeometryType) {
    return new OpenStreetMapStyler(fStyleProvider, aGeometryType);
  }

  protected ILspStyler createLabelStyler(GeometryType aGeometryType) {
    return new OpenStreetMapRoadsLabelStyler();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    ILcdModelDescriptor descriptor = aModel.getModelDescriptor();
    if (descriptor instanceof TLfnVectorTileStoreModelDescriptor) {
      Set<TLcdDataType> modelElementTypes = ((TLfnVectorTileStoreModelDescriptor) descriptor).getModelElementTypes();
      for (TLcdDataType modelElementType : modelElementTypes) {
        if (modelElementType.getDeclaredProperty("osm_id") != null) {
          return true;
        }
      }
    }
    return false;
  }
}
