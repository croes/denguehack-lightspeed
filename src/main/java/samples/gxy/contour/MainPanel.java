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
package samples.gxy.contour;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.labeling.TLcdGXYAsynchronousLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.gui.ColorLegend;
import samples.gxy.common.GXYLayerSelectionPanel;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.labels.DefaultGXYLabelingAlgorithmProvider;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.contour.complexPolygon.CreateComplexPolygonContoursAction;
import samples.gxy.contour.polyline.CreatePolylineContoursAction;

/**
 * This sample demonstrates several contour finders.
 * A raster with elevation data is fed to a contour finder, which uses a
 * contour builder to represent the contours as polylines or polygons.
 * The result is painted with a modified TLcdGXYPointListPainter that paints the pointlists
 * in the desired color.
 */
public class MainPanel extends GXYSample {

  private JPanel fLegendPanel;
  private GXYLayerSelectionPanel fContourTypePanel;
  private ContourLevels fContourLevels = new ContourLevels();
  private DefaultGXYLabelingAlgorithmProvider fLabelProvider;

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel map = super.createMap();
    fLabelProvider = new DefaultGXYLabelingAlgorithmProvider();
    TLcdGXYCompositeLabelingAlgorithm labelAlgorithm = new TLcdGXYCompositeLabelingAlgorithm(fLabelProvider);
    TLcdGXYAsynchronousLabelPlacer labelPlacer = new TLcdGXYAsynchronousLabelPlacer(labelAlgorithm);
    map.setGXYViewLabelPlacer(labelPlacer);
    return map;
  }

  @Override
  protected JPanel createSettingsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    fLegendPanel = new JPanel(new BorderLayout());
    fLegendPanel.setOpaque(false);
    panel.add(createContourTypePanel(), BorderLayout.NORTH);
    getOverlayPanel().add(fLegendPanel, TLcdOverlayLayout.Location.NORTH_WEST);
    return panel;
  }

  private JPanel createContourTypePanel() {
    fContourTypePanel = new GXYLayerSelectionPanel(getView(), fLegendPanel);
    return TitledPanel.createTitledPanel("Contour type", fContourTypePanel);
  }

  protected void addData() throws IOException {
    super.addData();

    ILcdGXYLayer dmedLayer = GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().addToView(getView()).fit().getLayer();

    CreateComplexPolygonContoursAction polygonContoursAction = new CreateComplexPolygonContoursAction(fContourLevels, false, getStatusBar());
    CreateComplexPolygonContoursAction disjointPolygonContoursAction = new CreateComplexPolygonContoursAction(fContourLevels, true, getStatusBar());
    CreatePolylineContoursAction polylineContoursAction = new CreatePolylineContoursAction(fContourLevels, false, getStatusBar());
    CreatePolylineContoursAction strokedPolylineContoursAction = new CreatePolylineContoursAction(fContourLevels, true, getStatusBar());

    polylineContoursAction.setDMEDLayer(dmedLayer);
    strokedPolylineContoursAction.setDMEDLayer(dmedLayer);
    polygonContoursAction.setDMEDLayer(dmedLayer);
    disjointPolygonContoursAction.setDMEDLayer(dmedLayer);

    polylineContoursAction.setGXYView(getView());
    strokedPolylineContoursAction.setGXYView(getView());
    polygonContoursAction.setGXYView(getView());
    disjointPolygonContoursAction.setGXYView(getView());

    ColorLegend polyLineLegend = new ColorLegend(fContourLevels.getLevelLabels(false), fContourLevels.getLevelColors(false), fContourLevels.getSpecialLabels(), fContourLevels.getSpecialColors(), true, true);
    ColorLegend polygonLegend = new ColorLegend(fContourLevels.getLevelLabels(true), fContourLevels.getLevelColors(true), fContourLevels.getSpecialLabels(), fContourLevels.getSpecialColors(), true, true);

    // calculate the contours
    ILcdGXYLayer polylineContourLayer = polylineContoursAction.createContourLayer();
    ILcdGXYLayer strokedPolylineLayer = strokedPolylineContoursAction.createContourLayer();
    ILcdGXYLayer polygonContourLayer = polygonContoursAction.createContourLayer(getStatusBar());
    ILcdGXYLayer disjointPolygonLayer = disjointPolygonContoursAction.createContourLayer(getStatusBar());

    // add the different types to a selection panel
    fContourTypePanel.addLayer("Polyline", polylineContourLayer, polyLineLegend, BorderLayout.CENTER);
    fContourTypePanel.addLayer("Stroked Polyline", strokedPolylineLayer, polyLineLegend, BorderLayout.CENTER);
    fContourTypePanel.addLayer("Polygon", polygonContourLayer, polygonLegend, BorderLayout.CENTER);
    fContourTypePanel.addLayer("Disjoint polygon", disjointPolygonLayer, polygonLegend, BorderLayout.CENTER);
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Contours");
  }

}
