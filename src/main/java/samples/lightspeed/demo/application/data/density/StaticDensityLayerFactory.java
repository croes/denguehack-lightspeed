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
package samples.lightspeed.demo.application.data.density;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.density.TLspDensityLayerBuilder;
import com.luciad.view.lightspeed.style.TLspDensityLineStyle;
import com.luciad.view.lightspeed.style.TLspIndexColorModelStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.lightspeed.demo.application.data.dynamictracks.EnrouteTrajectoryLayerFactory;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory that creates static density layers.
 * <p/>
 * This layer factory can be configured with following properties:
 * <table cellspacing="10">
 * <tr> <td><b>Key</b></td> <td><b>Type</b></td> <td><b>Default Value</b></td>
 * <td><b>Description</b></td> </tr>
 * <tr> <td>density.image</td> <td>string</td><td>null</td> <td>Specifies the path of the image
 * used
 * by the density painter. (note: the path should be absolute!)</td>
 * </tr>
 * </table>
 */
public class StaticDensityLayerFactory extends AbstractLayerFactory {

  private TLspIndexColorModelStyle fIndexColorModelStyle;

  @Override
  public void configure(Properties aProperties) {
    DensityIndexColorModelStyleUtil densityStyleUtil = new DensityIndexColorModelStyleUtil();
    fIndexColorModelStyle = densityStyleUtil.retrieveIndexColorModelStyle(aProperties);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel instanceof ILcd2DBoundsIndexedModel &&
           aModel.getModelReference() != null;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer(aModel));
  }

  private ILspLayer createLayer(ILcdModel aModel) {
    aModel = EnrouteTrajectoryLayerFactory.getDerivedModel(aModel);
    ILspStyler styler = TLspDensityLineStyle.newBuilder()
                                            .hardness(0.5f)
                                            .pixelSize(8.0f)
                                            .build();

    return TLspDensityLayerBuilder.newBuilder()
                                  .model(aModel)
                                  .indexColorModel(fIndexColorModelStyle)
                                  .bodyStyler(styler).build();
  }
}
