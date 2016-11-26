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
package samples.gxy.statisticalPainter;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.projection.TLcdVerticalPerspective;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample shows some specialized implementations of painters and
 * label painters on data object-implementing shapes.
 * <p/>
 * This sample demonstrates displaying an ILcdModel differently in different
 * layers of a view by use of different ILcdGXYPainter
 * implementations to display the same ILcdDataObject ILcdShape objects.
 * Per ILcdGXYLayer a different data object or a derivation of a data object of the same
 * data is displayed. This is achieved by setting the same ILcdModel to the
 * ILcdGXYLayer objects and assigning specific ILcdGXYPainter implementations to
 * that ILcdGXYLayer. Creation of the ILcdGXYLayer objects is performed by
 * ILcdGXYLayerFactory implementations.
 * <p/>
 * The BackGroundLayerFactory class creates an ILcdGXYLayer that displays the
 * data as polygons and labels them with their name and area.
 * The PopulationChangeFactory class creates an ILcdGXYLayer that displays the
 * data as rectangle icons using the PopulationChangePainter. This painter
 * returns different size rectangles depending on the population change.
 * The PopulationDensityLayerFactory class creates an ILcdGXYLayer that displays
 * the data as filled polygons using the PopulationDensityPainter. The polygons are filled
 * with a grey gradient depending on the population density.
 * <p/>
 * The data contains the USA counties, their population history and the
 * their area. From these properties the population change and the population
 * density is derived and displayed in the ILcdGXYView. As background the
 * ILcdShape objects are displayed as non filled polygons.
 *
 * @version 1.0, 08 November 1999
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-80.55, 33.25, 6.55, 7.50);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    // Set the initial projectionReference of the ILcdGXYView
    TLcdGridReference reference = new TLcdGridReference(
        new TLcdGeodeticDatum(),
        new TLcdVerticalPerspective(-90.0, 0.0, 0.0, 6.61292),
        0.0, 0.0, 1.0, 1.0, 0.0
    );
    getView().setXYWorldReference(reference);
  }

  @Override
  protected JPanel createSettingsPanel() {
    JPanel legend = new JPanel(new GridLayout(2, 1));
    legend.add(TitledPanel.createTitledPanel("Population Change", new PopulationChangeLegend()));
    legend.add(TitledPanel.createTitledPanel("Population Density", new PopulationDensityLegend()));
    Dimension preferredSize = legend.getPreferredSize();
    legend.setPreferredSize(new Dimension(preferredSize.width + 8, preferredSize.height));
    legend.setMinimumSize(new Dimension(preferredSize.width + 8, preferredSize.height));
    return legend;
  }

  @Override
  protected void addData() throws IOException {
    GXYDataUtil.instance().model(SampleData.US_COUNTIES).layer(new PopulationDensityLayerFactory()).addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_COUNTIES).layer(new PopulationChangeLayerFactory()).addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Painter implementations");
  }
}
