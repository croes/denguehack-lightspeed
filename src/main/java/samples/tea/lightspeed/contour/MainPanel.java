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
package samples.tea.lightspeed.contour;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.imaging.ALcdMultilevelImageMosaic;
import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.LspLayerSelectionPanel;
import samples.tea.Legend;

/**
 * This is the entry class for this demo.
 */
public class MainPanel extends LightspeedSample {

  private CreatePolygonContoursAction fPolygonAction = new CreatePolygonContoursAction();
  private CreatePolylineContoursAction fPolylineAction = new CreatePolylineContoursAction();

  @Override
  protected void addData() throws IOException {
    super.addData();
    final ILspLayer dtedLayer = LspDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(getView()).fit().getLayer();
    if (dtedLayer != null) {
      // the DTED model contains the altitude information.
      ALcdMultilevelImageMosaic multilevelImageMosaic =
          (ALcdMultilevelImageMosaic) dtedLayer.getModel().elements().nextElement();

      fPolylineAction.setView(MainPanel.this);
      fPolylineAction.setImageModel(dtedLayer.getModel());
      fPolylineAction.setBounds(multilevelImageMosaic.getBounds().cloneAs2DEditableBounds());
      ILspLayer polylineLayer = fPolylineAction.createContourLayer();

      fPolygonAction.setView(MainPanel.this);
      fPolygonAction.setImageModel(dtedLayer.getModel());
      fPolygonAction.setBounds(multilevelImageMosaic.getBounds().cloneAs2DEditableBounds());
      ILspLayer polygonLayer = fPolygonAction.createContourLayer();

      JPanel panel = new JPanel(new BorderLayout());
      LspLayerSelectionPanel selectionPanel = new LspLayerSelectionPanel(panel, getView());
      JPanel legend = createLegend();
      selectionPanel.addLayer(polygonLayer.getLabel(), polygonLayer, legend, BorderLayout.CENTER);
      selectionPanel.addLayer(polylineLayer.getLabel(), polylineLayer);

      JPanel layerPanel = TitledPanel.createTitledPanel("Contour type", selectionPanel);
      panel.add(layerPanel, BorderLayout.NORTH);
      panel.add(legend, BorderLayout.SOUTH);
      addComponentToRightPanel(panel);
    }
  }

  private JPanel createLegend() {
    double[] contourLevels = ContourLevels.getContourLevels();
    double[] contourLevelsSpecial = ContourLevels.getContourLevelsSpecial();
    double[] extendedContourLevels = new double[contourLevelsSpecial.length + contourLevels.length];
    System.arraycopy(contourLevelsSpecial, 0, extendedContourLevels, 0, contourLevelsSpecial.length);
    System.arraycopy(contourLevels, 0, extendedContourLevels, contourLevelsSpecial.length, contourLevels.length);

    Color[] contourColors = ContourLevels.getContourColors();
    String[] labels = new String[extendedContourLevels.length];
    for (int labelIndex = 0; labelIndex < extendedContourLevels.length; labelIndex++) {
      double colorLevel = extendedContourLevels[labelIndex];
      labels[labelIndex] = colorLevel != ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE
                             ? "\u2264 " + Integer.toString((int) colorLevel) + " m"
                             : "Unknown";
    }
    return new Legend(contourColors, labels, false);
  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        startSample(MainPanel.class, "Contours");
      }
    });
  }

}
