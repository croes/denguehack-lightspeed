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
package samples.gxy.height;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.util.height.ILcdModelHeightProviderFactory;
import com.luciad.util.height.TLcdImageModelHeightProviderFactory;
import com.luciad.util.height.TLcdViewHeightProvider;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.map.TLcdMapLocation;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.RasterLayerFactory;

/**
 * This sample illustrates the use of height providers in LuciadLightspeed. It shows a raster
 * containing height data. The height values of the data under the mouse cursor
 * are displayed in a label on the toolbar.
 * <p>
 * The sample uses a view height provider to retrieve height values.
 */
public class MainPanel extends GXYSample {

  private ILcdGeoReference fGeoReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());

  private static final String fDmedPath = "Data/Dted/Alps/dmed";

  @Override
  protected void createGUI() {
    super.createGUI();
    JLabel heightLabel = new JLabel();
    // Link the mouse pointer location in the view with the label.
    new MapHeightLocation(getView(),
                          createHeightProvider(),
                          fGeoReference,
                          heightLabel);
    getToolBars()[0].addComponent(heightLabel);
  }

  /*
   * Create a height provider using the entire view as height source.
   */
  private ILcdHeightProvider createHeightProvider() {
    ILcdModelHeightProviderFactory factory = new TLcdImageModelHeightProviderFactory();

    Map<String, Object> requiredProperties = new HashMap<String, Object>();
    Map<String, Object> optionalProperties = new HashMap<String, Object>();
    requiredProperties.put(ILcdModelHeightProviderFactory.KEY_GEO_REFERENCE,
                           fGeoReference);

    return new TLcdViewHeightProvider<ILcdGXYView>(getView(),
                                                   factory,
                                                   requiredProperties,
                                                   optionalProperties);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Load height data.
    ILcdModelDecoder decoder = new TLcdDMEDModelDecoder(TLcdSharedBuffer.getBufferInstance());
    ILcdModel model = decoder.decode("" + fDmedPath);

    ILcdGXYLayerFactory layer_factory = new RasterLayerFactory();
    ILcdGXYLayer layer = layer_factory.createGXYLayer(model);

    GXYLayerUtil.addGXYLayer(getView(), layer);
    GXYLayerUtil.fitGXYLayer(getView(), layer);
  }

  /**
   * Displays the height at the mouse pointer location.
   */
  private static class MapHeightLocation extends TLcdMapLocation {

    private JLabel fHeightLabel;
    private ILcdHeightProvider fHeightProvider;

    public MapHeightLocation(ILcdGXYView aGXYView,
                             ILcdHeightProvider aHeightProvider,
                             ILcdGeoReference aGeoReference,
                             JLabel aHeightLabel) {
      super(aGeoReference.getGeodeticDatum());
      super.setGXYView(aGXYView);

      fHeightLabel = aHeightLabel;
      fHeightProvider = aHeightProvider;
    }

    // Implementations for MouseMotionListener.

    public void mouseMoved(MouseEvent e) {

      // Compute the lon/lat coordinates.
      super.mouseMoved(e);

      // Retrieve the height from the height provider.
      ILcdPoint mouseLocation = new TLcdLonLatPoint(getLon(), getLat());
      double height = fHeightProvider.retrieveHeightAt(mouseLocation);

      // Update the label text.
      if (Double.isNaN(height)) {
        fHeightLabel.setText("no height data");
      } else {
        fHeightLabel.setText("height " + height + "m");
      }
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Height");
  }
}
