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
package samples.gxy.geoid;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.JLabel;

import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.TLcdIndexLookupOp;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;
import com.luciad.view.map.TLcdMapLocation;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.decoder.MapSupport;

/**
 * This sample illustrates support in LuciadLightspeed for geoids. It shows an image
 * containing the geoid heights of the EGM96 datum (Earth Gravity Model, 1996).
 * Blue colors indicate that the geoid heights are negative (geoid below the
 * ellipsoid of the WGS84 datum). Green colors indicate that the geoid heights
 * are positive (geoid above the ellipsoid).
 * <p>
 * The geoid heights are also displayed in a label on the toolbar.
 * <p>
 * Geoid heights are typically used behind the screens, in calculations with
 * references that have geodetic datums based on geoids. They are relevant
 * for elevation data, for instance, which are commonly defined with respect
 * to a few standardized geoid models. For accurate computations, elevations
 * above a geoid can then be transformed to elevations above an ellipsoid.
 */
public class MainPanel extends GXYSample {

  @Override
  protected void createGUI() {
    super.createGUI();
    getToolBars()[0].addComponent(new HeightLabel(getView(), GeoidModelFactory.getDatum()));
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Adds a geoid image layer to the map, showing the progress.
    GXYLayerUtil.addGXYLayer(getView(), createGeoidImageLayer(getStatusBar()));
  }

  /**
   * Creates a layer containing a geoid image.
   */
  private ILcdGXYLayer createGeoidImageLayer(ILcdStatusListener aProgress) {

    ILcdModel model = GeoidModelFactory.createGeoidModel(aProgress, this);

    TLcdGXYImagePainter painter = new TLcdGXYImagePainter();
    painter.setFillOutlineArea(true);
    painter.setForcePainting(true);

    //set an operator chain that is used to map the correct color to an elevation value.
    ALcdImageOperatorChain indexLookupOperatorChain = createIndexLookupOperatorChain();
    painter.setOperatorChain(indexLookupOperatorChain);

    TLcdGXYLayer layer = new TLcdGXYLayer(model);
    layer.setSelectable(false);
    layer.setGXYPen(MapSupport.createPen(model.getModelReference()));
    layer.setGXYPainterProvider(painter);

    return layer;
  }

  private TLcdColorMap createColorMap() {
    // Create an elevation color model for the limited range of geoid heights.
    double[] levels = {
        TLcdDTEDTileDecoder.UNKNOWN_ELEVATION,
        -110.0,
        -40.0,
        40.0,
        110.0,
    };

    Color[] colors = {
        new Color(0, 0, 0, 0),
        new Color(0, 0, 0, 128),
        new Color(0, 0, 255, 128),
        new Color(0, 255, 0, 128),
        new Color(255, 255, 255, 128),

    };

    return new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE), levels, colors);
  }

  /**
   * Create a operator chain with a TLcdIndexLookupOp.
   *
   * @return the operator chain
   */
  private ALcdImageOperatorChain createIndexLookupOperatorChain() {
    TLcdColorMap colorMap = createColorMap();
    final TLcdLookupTable lookupTable = TLcdLookupTable.newBuilder()
                                                       .fromColorMap(colorMap)
                                                       .build();
    return new ALcdImageOperatorChain() {
      @Override
      public ALcdImage apply(ALcdImage aInput) {
        return TLcdIndexLookupOp.indexLookup(aInput, lookupTable);
      }
    };
  }

  /**
   * This displays the geoid height at the mouse pointer location.
   */
  private static class HeightLabel extends JLabel implements MouseMotionListener {

    private final NumberFormat fNumberFormat;
    private ILcdGeodeticDatum fGeodeticDatum;
    private TLcdMapLocation fMapLocation;

    public HeightLabel(ILcdGXYView aGXYView, ILcdGeodeticDatum aGeodeticDatum) {
      fNumberFormat = NumberFormat.getInstance();
      fNumberFormat.setMaximumFractionDigits(4);
      fMapLocation = new TLcdMapLocation(aGeodeticDatum);
      fMapLocation.setGXYView(aGXYView);
      if (aGXYView instanceof Component) {
        ((Component) aGXYView).addMouseMotionListener(this);
      }
      fGeodeticDatum = aGeodeticDatum;
    }

    // Implementations for MouseMotionListener.

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      // Retrieve the geoid height from the geodetic datum.
      double height = fGeodeticDatum.getHeight(fMapLocation.getLon(), fMapLocation.getLat());
      // Update the label text.
      if (Double.isNaN(height)) {
        setText("geoid height unknown");
      } else {
        setText("geoid height " + fNumberFormat.format(height) + " m");
      }
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Geoid");
  }
}
