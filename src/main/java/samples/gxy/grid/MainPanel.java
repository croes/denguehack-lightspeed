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
package samples.gxy.grid;

import javax.swing.JPanel;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.gxy.common.GXYSample;
import samples.gxy.common.GXYLayerSelectionPanel;
import samples.gxy.common.TitledPanel;
import samples.gxy.grid.multilevel.MultilevelGridDrawController;
import samples.gxy.grid.multilevel.StatusComboBox;
import samples.gxy.grid.multilevel.cgrs.CGRSFormatComponent;
import samples.gxy.grid.multilevel.gars.GARSFormatComponent;
import samples.gxy.projections.GXYCenterMapController;
import samples.gxy.projections.ProjectionComboBox;

/**
 * This sample demonstrates several grid layer implementations.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-10.00, 34.00, 30.00, 30.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    getToolBars()[0].addComponent(new ProjectionComboBox(getView(), 0));
    getToolBars()[0].addSpace(2, 10);
    getToolBars()[0].addGXYController(new GXYCenterMapController());
    MultilevelGridDrawController controller = new MultilevelGridDrawController();
    getToolBars()[0].addGXYController(controller);
    getToolBars()[0].addComponent(new StatusComboBox(controller));
    getView().setGridLayer(null); // we'll manage the grid layer ourselves in this sample
  }

  @Override
  protected JPanel createSettingsPanel() {
    GXYLayerSelectionPanel gridTypePanel = new GXYLayerSelectionPanel(getView(), getOverlayPanel());
    gridTypePanel.addLayer("Lon Lat", GridLayerFactory.createLonLatGridLayer(), null, com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    gridTypePanel.addLayer("Border", GridLayerFactory.createBorderGridLayer(), null, com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    gridTypePanel.addLayer("Georef", GridLayerFactory.createGeoRefGridLayer(), null, com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    gridTypePanel.addLayer("XY", new XYGridLayerFactory().createXYGridLayer(), null, com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    gridTypePanel.addLayer("MGRS", new MGRSGridLayerFactory().createMGRSGridLayer(), null, com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    gridTypePanel.addLayer("CGRS", GridLayerFactory.createCGRSLayer(), new CGRSFormatComponent(getView()), com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    gridTypePanel.addLayer("GARS", GridLayerFactory.createGARSLayer(), new GARSFormatComponent(getView()), com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    gridTypePanel.addLayer("OSGR", GridLayerFactory.createOSGRLayer(), null, com.luciad.gui.swing.TLcdOverlayLayout.Location.SOUTH);
    return TitledPanel.createTitledPanel("Grid type", gridTypePanel);
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Map grids");
  }

}
