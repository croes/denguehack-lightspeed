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
package samples.gxy.labels.streets;

import java.awt.EventQueue;
import java.io.IOException;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.projection.TLcdPseudoMercator;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.map.TLcdMapJPanel;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample demonstrates street labeling. To do this, it uses a few techniques :
 * - Level of detail painters : Objects are painted differently (or not at all) based on the view scale.
 * - Level of detail labels   : Labels are painted differently (or not at all) based on the view scale.
 * - Dynamic model filtering  : Model objects are often filtered based on the view scale. This is done
 *                              to improve performance. Objects are only handled/painted when the view
 *                              is in a certain scale range.
 * - Dynamic label priorities : The label priorities are based on a global priority and a relative priority.
 *                              The global priority is based on the view scale, and makes sure labels
 *                              of different layers interact correctly. The relative priority makes sure
 *                              labels of the same layer interact correctly.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-77.06, 38.88, 0.06, 0.04);
  }

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel map = super.createMap();
    TLcdGridReference reference = new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdPseudoMercator());
    map.setXYWorldReference(reference);
    return map;
  }

  @Override
  protected void addData() throws IOException {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        // Add countries layer
        GXYDataUtil.instance()
                   .model(CountriesLayerFactory.createModel())
                   .layer(new CountriesLayerFactory())
                   .labelingAlgorithm(CountriesLayerFactory.createLabelingAlgorithm(0, 1000))
                   .addToView(getView());

        // Add States layer
        GXYDataUtil.instance()
                   .model(StatesLayerFactory.createModel())
                   .layer(new StatesLayerFactory())
                   .labelingAlgorithm(StatesLayerFactory.createLabelingAlgorithm(0, 1000))
                   .addToView(getView());

        // Add Streets layer
        GXYDataUtil.instance()
                   .model(StreetsLayerFactory.createModel())
                   .layer(new StreetsLayerFactory())
                   .labelingAlgorithm(StreetsLayerFactory.createLabelingAlgorithm(0, 1000))
                   .addToView(getView());

        // Add Highways layer
        GXYDataUtil.instance()
                   .model(HighwaysLayerFactory.createModel())
                   .layer(new HighwaysLayerFactory())
                   .labelingAlgorithm(HighwaysLayerFactory.createLabelingAlgorithm(0, 1000))
                   .addToView(getView());

        // Add Cities Layer
        GXYDataUtil.instance()
                   .model(CitiesLayerFactory.createModel())
                   .layer(new CitiesLayerFactory())
                   .labelingAlgorithm(CitiesLayerFactory.createLabelingAlgorithm(0, 1000))
                   .addToView(getView());

        getView().setWithGridLayer(false);
      }
    });
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Street Labeling");
  }
}
