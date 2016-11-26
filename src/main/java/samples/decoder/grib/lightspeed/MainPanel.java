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
package samples.decoder.grib.lightspeed;

import java.io.IOException;

import com.luciad.format.grib.lightspeed.TLspGRIBLayerBuilder;
import com.luciad.model.ILcdModel;

import samples.decoder.grib.GRIBCustomization;
import samples.decoder.grib.GRIBModelFactory;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates how to decode and visualize several GRIB files.
 * @see com.luciad.format.grib.TLcdGRIBModelDecoder
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel  {

  @Override
  protected void addData() throws IOException {
    super.addData();

    GRIBModelFactory modelFactory = new GRIBModelFactory( "" );
    // Regular GRIB layers.
    LspDataUtil.instance().model(modelFactory.decodeTemperature()).layer().addToView(getView()).fit();
    LspDataUtil.instance().model(modelFactory.decodeGeoPotential()).layer().addToView(getView());
    LspDataUtil.instance().model(modelFactory.decodeIsobars()).layer().addToView(getView());
    LspDataUtil.instance().model(modelFactory.decodeCloudCover()).layer().addToView(getView());
    LspDataUtil.instance().model(modelFactory.decodeWind()).layer().addToView(getView());

    // Customized GRIB layer.
    ILcdModel model = modelFactory.decodeNumericTemperature();
    GRIBLayerFactory layerFactory = new GRIBLayerFactory();
    layerFactory.setIcon(TLspGRIBLayerBuilder.createParameterizedIcon(GRIBCustomization.createNumericTemperatureIcon(model, 0)));
    layerFactory.setIconSpacing( 150 );
    LspDataUtil.instance().model(model).layer(layerFactory).addToView(getView());
  }

  public static void main( final String[] aArgs ) {
    startSample(MainPanel.class, "GRIB");
  }
}
