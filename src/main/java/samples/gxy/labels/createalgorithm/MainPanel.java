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
package samples.gxy.labels.createalgorithm;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;

/**
 * This sample demonstrates the creation and usage of labeling algorithms and labeling algorithm
 * wrappers. SampleLabelingAlgorithm is a labeling algorithm that tries to place all labels on its
 * anchor point. There are three wrappers that can be combined with this algorithm and each other.
 * - RotationAlgorithmWrapper : Rotates all labels
 * - MorePositionsAlgorithmWrapper : Creates extra label placements to be tried by offsetting the label by
 *   a few pixels in each direction.
 * - LabelDetailAlgorithmWrapper : Adjusts the number of displayed properties and the size of the font to
 *   create more label placement possibilities.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected JPanel createSettingsPanel() {
    TLcdGXYLabelPlacer labelPlacer = new TLcdGXYLabelPlacer();
    getView().setGXYViewLabelPlacer(labelPlacer);
    // The options panel will install and configure the labeling algorithms.
    return new LabelingOptionsPanel(getView(), labelPlacer);
  }

  protected void addData() throws IOException {
    super.addData();
    GXYUnstyledLayerFactory layerFactory = new GXYUnstyledLayerFactory();
    layerFactory.setLabelsWithPin(true);
    ILcdGXYLayer cities = GXYDataUtil.instance().model(SampleData.US_CITIES).layer(layerFactory).label("Cities").addToView(getView()).getLayer();

    if (cities instanceof TLcdGXYLayer) {
      TLcdGXYLayer layer = (TLcdGXYLayer) cities;
      // LabelDetailAlgorithmWrapper depends on the usage of LabelDetailLabelLocation
      layer.setLabelLocations(new TLcdLabelLocations(layer, new LabelDetailLabelLocation()));
      layer.setLabeled(true);

      TLcdGXYDataObjectLabelPainter oldLabelPainter = (TLcdGXYDataObjectLabelPainter) layer.getGXYLabelPainterProvider();
      TLcdGXYDataObjectLabelPainter newLabelPainter = new LabelDetailLabelPainter(oldLabelPainter);
      newLabelPainter.setFrame(true);
      newLabelPainter.setForeground(Color.WHITE);
      layer.setGXYLabelPainterProvider(newLabelPainter);
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Labeling");
  }

}
