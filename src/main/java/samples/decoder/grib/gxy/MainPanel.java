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
package samples.decoder.grib.gxy;

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.SampleData;
import samples.common.formatsupport.GXYOpenSupport;
import samples.common.formatsupport.OpenAction;
import samples.decoder.grib.GRIBCustomization;
import samples.decoder.grib.GRIBModelFactory;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates how to decode and visualize several GRIB files.
 * @see com.luciad.format.grib.TLcdGRIBModelDecoder
 */
public class MainPanel extends GXYSample {

  protected void createGUI() {
    super.createGUI();
    GXYOpenSupport openSupport = new GXYOpenSupport( getView() );
    openSupport.addStatusListener(getStatusBar());
    getToolBars()[0].addAction(new OpenAction(openSupport));
  }

  protected void addData() throws IOException {

    // Add the world layer
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());

    try {
      GRIBModelFactory modelFactory = new GRIBModelFactory("");
      ILcdGXYLayer layer = addGRIBLayer( modelFactory.decodeTemperature() );
      GXYLayerUtil.fitGXYLayer( getView(), layer );
      addGRIBLayer( modelFactory.decodeGeoPotential() );
      addGRIBHumidityBulletin( modelFactory.decodeHumidityBulletin() );
      addGRIBLayerWithIsobars( modelFactory.decodeIsobars() );
      addGRIBLayer( modelFactory.decodeCloudCover() );
      addGRIBLayer( modelFactory.decodeWind() );
      addGRIBCustomNumericLayer( modelFactory.decodeNumericTemperature() );
    }
    catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  private void addGRIBCustomNumericLayer( ILcdModel aModel ) {
    GRIBLayerFactory layerFactory = new GRIBLayerFactory();
    layerFactory.setGRIBIcon( GRIBCustomization.createNumericTemperatureIcon( aModel, 1 ) );
    // Set the space between the icons in the painter.
    layerFactory.setIconSpacing( 150 );
    GXYLayerUtil.addGXYLayer( getView(), layerFactory.createGXYLayer( aModel ) );
  }

  private ILcdGXYLayer addGRIBLayer( ILcdModel aModel ) throws IOException {
    ILcdGXYLayer layer = new GRIBLayerFactory().createGXYLayer( aModel );
    GXYLayerUtil.addGXYLayer( getView(), layer );
    return layer;
  }

  private void addGRIBLayerWithIsobars( ILcdModel aModel ) throws IOException {
    // The isobar model contains polylines, so it's not a GRIB model.
    // We use a custom layer factory for it.
    ILcdGXYLayer layer = new IsobarLayerFactory().createGXYLayer( aModel );
    GXYLayerUtil.addGXYLayer( getView(), layer );
  }

  private void addGRIBHumidityBulletin( ILcdModel aModel ) throws IOException {
    // To make it easier to differentiate the different models of the bulletin, we give
    // them custom colors in a special layer factory.
    GXYLayerUtil.addGXYLayer( getView(), new CustomGRIBBulletinLayerFactory().createGXYLayer( aModel ) );
  }

  // Main method

  public static void main( final String[] aArgs ) {
    startSample( MainPanel.class, "Decoding GRIB" );
  }
}
