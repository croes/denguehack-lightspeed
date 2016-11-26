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
package samples.lucy.heightprovider;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyMapComponent;
import com.luciad.lucy.util.height.TLcyViewHeightProvider;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.height.ALcdModelHeightProviderFactory;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Panel that displays height information. In this panel, the minimum, maximum and average
 * height of the visible area of the Map view are shown. These values are updated by pressing the
 * Update button in the pane, or when switching to a different 2D map.
 *
 * When changing the rotation, zoom or position in the Map view, use the Update button in the Height
 * pane to get the new values.
 *
 * If no height data is available, the height is displayed as "Unknown". To get height data, a layer
 * with height data, such as a DMED file, has to be opened and made visible in the view.
 *
 * To calculate the minimum and maximum height in the visible area, a grid of sample points
 * in the view are taken. This means that the view is zoomed out, chances are big that the
 * true lowest and heighest point are missed and a less extreme value is displayed.
 *
 * To make the height calculation possible, there must be height data available in the visible
 * area.
 */
public class HeightPanel extends JPanel {

  private ILcyLucyEnv fLucyEnv;
  JTextArea fText = new JTextArea();
  JButton fUpdateButton = new JButton("Update");
  private TLcdGeodeticReference fPointReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());

  public HeightPanel(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;

    setLayout(new BorderLayout());
    add(fText, BorderLayout.CENTER);
    add(fUpdateButton, BorderLayout.SOUTH);

    fText.setEditable(false);

    fUpdateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateHeight(fLucyEnv.getMapManager().getActiveMapComponent());
      }
    });

    fLucyEnv.getMapManager().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent aEvent) {
        if ("activeMapComponent".equals(aEvent.getPropertyName())) {
          updateHeight(fLucyEnv.getMapManager().getActiveMapComponent());
        }
      }
    });

    updateHeight(fLucyEnv.getMapManager().getActiveMapComponent());
  }

  /**
   * Calculates the min, max and average height in the visible area of the view, based on some
   * sample points. A TLcyViewHeightProvider is used to calculate the heights.
   * @param aMapComponent The current map component
   */
  private void updateHeight(ILcyMapComponent aMapComponent) {
    if (aMapComponent == null) {
      return;
    }

    ILcdGXYView view = aMapComponent.getMainView();

    Map<String, Object> requiredProperties = new HashMap<String, Object>();
    requiredProperties.put(ALcdModelHeightProviderFactory.KEY_GEO_REFERENCE, fPointReference);
    Map<String, Object> optionalProperties = new HashMap<String, Object>();
    ILcdHeightProvider heightProvider = new TLcyViewHeightProvider<ILcdGXYView>(view, requiredProperties, optionalProperties, fLucyEnv);

    int sampleGridSize = 100; //number of point samples to take in one direction (in total sampleGridSize * sampleGridSize samples are taken)

    double minHeight = Double.NaN;
    double maxHeight = Double.NaN;
    double averageHeight = Double.NaN;
    int numSuccessfulSamples = 0;

    for (int y = 0; y < sampleGridSize; y++) {
      for (int x = 0; x < sampleGridSize; x++) {
        int screenX = x * view.getWidth() / sampleGridSize;
        int screenY = y * view.getHeight() / sampleGridSize;
        ILcdPoint worldPoint = viewToWorld(new Point(screenX, screenY), view);
        if (worldPoint == null) {
          continue;
        }

        double height = heightProvider.retrieveHeightAt(worldPoint);

        if (!Double.isNaN(height)) {
          if (Double.isNaN(minHeight) || height < minHeight) {
            minHeight = height;
          }
          if (Double.isNaN(maxHeight) || height > maxHeight) {
            maxHeight = height;
          }
          if (Double.isNaN(averageHeight)) {
            averageHeight = height;
          } else {
            averageHeight += height;
          }
          numSuccessfulSamples++;
        }
      }
    }
    averageHeight /= numSuccessfulSamples;

    TLcdAltitudeFormat altitudeFormat = fLucyEnv.getDefaultAltitudeFormat();

    String message = "";
    message += "Minimum height in visible area: " + altitudeFormat.format(minHeight) + "\n";
    message += "Maximum height in visible area: " + altitudeFormat.format(maxHeight) + "\n";
    message += "Average height in visible area: " + altitudeFormat.format(averageHeight) + "\n";
    fText.setText(message);
  }

  private ILcdPoint viewToWorld(Point aViewPoint, ILcdGXYView aView) {
    ILcdXYWorldReference xy_world_ref = aView.getXYWorldReference();
    try {
      ILcd2DEditablePoint XYPoint = new TLcdXYPoint();
      ILcd3DEditablePoint LLHPoint = new TLcdLonLatHeightPoint();
      TLcdLonLatPoint LLPoint = new TLcdLonLatPoint();
      TLcdGeodetic2Grid geodetic2Grid = new TLcdGeodetic2Grid();
      geodetic2Grid.setModelReference(fPointReference);

      aView.getGXYViewXYWorldTransformation().viewXYPoint2worldSFCT(aViewPoint.x,
                                                                    aViewPoint.y,
                                                                    XYPoint);
      geodetic2Grid.setXYWorldReference(xy_world_ref);
      geodetic2Grid.worldPoint2modelSFCT(XYPoint, LLHPoint);
      LLPoint.move2D(LLHPoint.getX(), LLHPoint.getY());
      return LLPoint;
    } catch (TLcdOutOfBoundsException outOfBoundE) {
      return null;
    }
  }
}
