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
package samples.gxy.common;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.TLcdGXYAsynchronousLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.gxy.swing.TLcdGXYScaleIndicator;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.gxy.GXYScaleSupport;
import samples.common.gxy.GXYViewNavigationUtil;
import samples.common.UIColors;
import samples.gxy.common.labels.GXYLabelingAlgorithmProvider;
import samples.gxy.grid.GridLayerFactory;

/**
 * Factory implementation that creates <code>TLcdMapJPanel</code>s with
 * some default sample settings. It adds the following behavior:
 * <ul>
 *   <li>background color</li>
 *   <li>scale icon in the lower right corner</li>
 *   <li>logo icon in the lower left corner</li>
 *   <li>label placer for label decluttering</li>
 *   <li>anti-aliased grid layer with custom colors</li>
 *   <li>fitting on the specified bounds</li>
 * </ul>
 */
public class SampleMapJPanelFactory {

  /**
   * Creates a map fitted on the entire world, with default label decluttering.
   * @return a map.
   */
  public static TLcdMapJPanel createMapJPanel() {
    return createMapJPanel(null);
  }

  /**
   * Creates a map fitted on the given bounds, without label decluttering.
   * @param aInitialBoundsToFit the bounds to fit the map to, expressed in WGS-84 coordinates
   * @return a map.
   */
  public static TLcdMapJPanel createMapJPanel(ILcdBounds aInitialBoundsToFit) {
    return createMapJPanel(aInitialBoundsToFit, true);
  }


  /**
   * Creates a map fitted on the given bounds, with or without label decluttering.
   * @param aInitialBoundsToFit the bounds to fit the map to, expressed in WGS-84 coordinates
   * @param aLabelDecluttering if true, labels will be decluttered
   * @return a map.
   */
  public static TLcdMapJPanel createMapJPanel(ILcdBounds aInitialBoundsToFit, boolean aLabelDecluttering) {
    TLcdMapJPanel map = new TLcdMapJPanel();

    // Smart panning may increase panning performance but disabling it gives best results when
    // painting asynchronous content or when painting objects of constant pixel size.
    map.setSmartPan(false);

    // Set background color to represent sea.
    map.setBackground(UIColors.bgMap());

    // Add a scale icon
    map.putCornerIcon(new TLcdGXYScaleIndicator(map), ILcdGXYView.LOWERRIGHT);

    // Constrains zooming from 1 : 1 000 000 000 all the way up to 1 : 10.
    double screenResolution1 = -1;
    double minScaleInMeters = GXYScaleSupport.mapScale2InternalScale(1.0 / 1000000000, screenResolution1, map.getXYWorldReference());
    double screenResolution = -1;
    double maxScaleInMeters = GXYScaleSupport.mapScale2InternalScale(1.0 / 10, screenResolution, map.getXYWorldReference());
    map.setMinScale(metersToWorldUnits(minScaleInMeters, map.getXYWorldReference()));
    map.setMaxScale(metersToWorldUnits(maxScaleInMeters, map.getXYWorldReference()));

    if (aLabelDecluttering) {
      // Create a view label placer, that will take care of placing the labels.
      // It leaves the drawing of the labels to the layer.
      GXYLabelingAlgorithmProvider provider = new GXYLabelingAlgorithmProvider();
      TLcdGXYCompositeLabelingAlgorithm algorithm = new TLcdGXYCompositeLabelingAlgorithm(provider);
      TLcdGXYAsynchronousLabelPlacer view_label_placer = new TLcdGXYAsynchronousLabelPlacer(algorithm);
      map.setGXYViewLabelPlacer(view_label_placer);
    }

    // Set up a grid layer.
    map.setGridLayer(GridLayerFactory.createLonLatGridLayer());

    scheduleInitialFit(map, aInitialBoundsToFit);

    // Replaces the copyright string by a nice logo.
    LogoIcon.setupLogo(map);

    // Make sure the min and max scale remain sensible values
    map.addPropertyChangeListener(new ScalePropertyChangeListener());


    return map;
  }

  public static void scheduleInitialFit(final ILcdGXYView aGXYView, final ILcdBounds aInitialBoundsToFit) {
    if (aInitialBoundsToFit != null) {
      // wait until the GUI is fully realized
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            GXYViewNavigationUtil.fitOnBounds(aGXYView, aInitialBoundsToFit, new TLcdGeodeticReference());
          } catch (TLcdNoBoundsException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  /**
   * Used to make sure that the min and max scale of the view are adjusted when the UOM
   * of the world reference changes.
   */
  private static class ScalePropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("XYWorldReference".equals(evt.getPropertyName())) {
        TLcdMapJPanel map = (TLcdMapJPanel) evt.getSource();
        ILcdXYWorldReference oldWorldReference = (ILcdXYWorldReference) evt.getOldValue();
        ILcdXYWorldReference newWorldReference = (ILcdXYWorldReference) evt.getNewValue();
        if (oldWorldReference != null && newWorldReference != null) {
          map.setMinScale(convertScale(map.getMinScale(), oldWorldReference, newWorldReference));
          map.setMaxScale(convertScale(map.getMaxScale(), oldWorldReference, newWorldReference));
        }
      }
    }
  }

  private static double convertScale(double aOldScale, ILcdXYWorldReference aOldWorldReference, ILcdXYWorldReference aNewWorldReference) {
    double minScaleInMeters = worldUnitsToMeters(aOldScale, aOldWorldReference);
    return metersToWorldUnits(minScaleInMeters, aNewWorldReference);
  }

  private static double metersToWorldUnits(double aScaleInMeters, ILcdXYWorldReference aWorldReference) {
    if (aWorldReference instanceof ILcdGridReference) {
      ILcdGridReference gridReference = (ILcdGridReference) aWorldReference;
      return aScaleInMeters * gridReference.getUnitOfMeasure();
    }
    return aScaleInMeters;
  }

  private static double worldUnitsToMeters(double aScaleInWorldUnits, ILcdXYWorldReference aWorldReference) {
    if (aWorldReference instanceof ILcdGridReference) {
      ILcdGridReference gridReference = (ILcdGridReference) aWorldReference;
      return aScaleInWorldUnits / gridReference.getUnitOfMeasure();
    }
    return aScaleInWorldUnits;
  }
}
