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
package samples.tea.lightspeed.distance;

import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.Enumeration;

import com.luciad.geometry.ILcdSegmentScanner;
import com.luciad.geometry.ellipsoidal.TLcdGeodeticSegmentScanner;
import com.luciad.geometry.ellipsoidal.TLcdRhumblineSegmentScanner;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.tea.ALcdTerrainElevationProvider;
import com.luciad.tea.TLcdTerrainDistanceUtil;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdInvalidateable;
import com.luciad.util.concurrent.TLcdLockUtil.Lock;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyler;

import samples.lightspeed.common.controller.ControllerFactory;

/**
 * Extension of <code>TLspRulerController</code>  which takes into account underlying terrain for distance calculations.
 */
class TerrainRulerController extends TLspRulerController {

  private final ILspCustomizableStyler fOriginalCircleStyler;
  private final TLspCustomizableStyler fNoCircleCircleStyler;
  private TLcdTerrainDistanceUtil fTerrainDistanceUtil = new TLcdTerrainDistanceUtil();
  private TLcdGeodeticSegmentScanner fGeodeticSegmentScanner = new TLcdGeodeticSegmentScanner();
  private TLcdRhumblineSegmentScanner fRhumblineSegmentScanner = new TLcdRhumblineSegmentScanner();
  private ALcdTerrainElevationProvider fTerrainElevationProvider;
  private TLcdGeodeticReference fGeodeticReference = new TLcdGeodeticReference();

  private double fAbsoluteTolerance = 10;
  private double fRelativeTolerance = 0.01;
  private int fMinimumSamples = 100;
  private int fMaximumSamples = 1000;
  private int fMinimumStepSize = 10;
  private boolean fUseTerrain = true;

  public TerrainRulerController() {
    setName("Terrain ruler");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.MEASURE_ICON));
    setShortDescription("Terrain Ruler: Click to measure distances over terrain - Double click to stop");
    fOriginalCircleStyler = getCircleStyler();
    fNoCircleCircleStyler = new TLspCustomizableStyler(new TLspCustomizableStyle[0]);
    updateCircleStyling();
    setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().or().rightMouseButton().or().keyEvents().build());
    appendController(ControllerFactory.createNavigationController());
  }

  /**
   * Sets the terrain elevation provider which is responsible for return elevation data on any segment
   * of the ruler.
   * @param aTerrainElevationProvider the terrain elevation provider which is responsible for return elevation data on any segment
   * of the ruler.
   */
  public void setTerrainElevationProvider(ALcdTerrainElevationProvider aTerrainElevationProvider) {
    fTerrainElevationProvider = aTerrainElevationProvider;
  }

  /**
   * Calculates the distance between the two points, taking into account the path defined by the measure mode.
   * Depending on the value of {@link #isUseTerrain()} the terrain altitude provided by the terrain elevation provider
   * is taken into account.
   * @param aStartPoint the first point
   * @param aEndPoint the second point
   * @param aModelReference the reference the points are defined in
   * @param aMeasureMode the mode defining the path from the start point to the end point
   * @return the distance between the two points, taking into account the path defined by the measuring mode.
   */
  protected double calculateDistance(ILcdPoint aStartPoint,
                                     ILcdPoint aEndPoint,
                                     ILcdModelReference aModelReference,
                                     MeasureMode aMeasureMode) {
    if (fUseTerrain && aModelReference instanceof ILcdGeoReference) {
      ILcdGeoReference geoReference = (ILcdGeoReference) aModelReference;
      ILcdSegmentScanner segmentScanner;
      try {
        segmentScanner = retrieveSegmentScanner(aMeasureMode, geoReference);
      } catch (IllegalArgumentException e) {
        return Double.NaN;
      }

      double startStepSize = super.calculateDistance(aStartPoint, aEndPoint, aModelReference, aMeasureMode)/fMinimumSamples;
      return fTerrainDistanceUtil.terrainDistance(
              aStartPoint,
              geoReference,
              aEndPoint,
              geoReference,
              segmentScanner,
              fTerrainElevationProvider,
              startStepSize,
              fMaximumSamples,
              fAbsoluteTolerance,
              fRelativeTolerance,
              fMinimumStepSize
      );
    } else {
      return super.calculateDistance(aStartPoint, aEndPoint, aModelReference, aMeasureMode);
    }
  }

  /**
   * Sets whether terrain should be taken into account when computing the distance. Default value is true.
   * @param aUseTerrain whether terrain should be taken into account when computing the distance.
   */
  public void setUseTerrain(boolean aUseTerrain) {
    if (fUseTerrain != aUseTerrain) {
      fUseTerrain = aUseTerrain;
      recalculateMeasurements();
      updateCircleStyling();
    }
  }

  /**
   * Returns whether terrain should be taken into account when computing the distance.
   * @return whether terrain should be taken into account when computing the distance.
   * @see #setUseTerrain(boolean)
   */
  public boolean isUseTerrain() {
    return fUseTerrain;
  }

  private void updateCircleStyling() {
    if (isUseTerrain()) {
      setCircleStyler(fNoCircleCircleStyler);
    } else {
      setCircleStyler(fOriginalCircleStyler);
    }
  }

  private void recalculateMeasurements() {
    ILcdModel model = getModel();
    if (model != null) {
      try (Lock autoUnlock = writeLock(model)) {
        Enumeration elements = model.elements();
        while (elements.hasMoreElements()) {
          Object element = elements.nextElement();
          model.elementChanged(element, ILcdFireEventMode.FIRE_LATER);
          if (element instanceof ILcdInvalidateable) {
            ((ILcdInvalidateable) element).invalidateObject();
          }
        }
      }
      model.fireCollectedModelChanges();
    }
  }

  private ILcdModel getModel() {
    if (isAddLayerToView()) {
      Enumeration layers = getView().layers();
      while (layers.hasMoreElements()) {
        ILspLayer layer = (ILspLayer) layers.nextElement();
        if (layer.getLabel().equals("Ruler Measurement")) {
          return layer.getModel();
        }
      }
      return null;
    }
    return getLayered().getLayer(0).getModel();
  }

  /**
   * Returns a suitable segment scanner depending on the measuring mode and the reference of the start and end point.
   * The context is passed for the projection plane mode.
   * @param aMeasureMode the mode defining the path between the two points.
   * @param aGeoReference the reference of the two points.
   * @return a <code>TLcdGeodeticSegmentScanner</code> for <code>MeasureMode.MEASURE_GEODETIC</code>,
   * a <code>TLcdRhumblineSegmentScanner</code> for <code>MeasureMode.MEASURE_RHUMB</code>
   * @throws IllegalArgumentException for all other MeasureModes.
   */
  public ILcdSegmentScanner retrieveSegmentScanner(MeasureMode aMeasureMode, ILcdGeoReference aGeoReference) throws IllegalArgumentException {
    switch (aMeasureMode) {
      case MEASURE_GEODETIC:
        fGeodeticReference.setGeodeticDatum(aGeoReference.getGeodeticDatum());
        fGeodeticSegmentScanner.setGeodeticReference(fGeodeticReference);
        return fGeodeticSegmentScanner;
      case MEASURE_RHUMB:
        fGeodeticReference.setGeodeticDatum(aGeoReference.getGeodeticDatum());
        fRhumblineSegmentScanner.setGeodeticReference(fGeodeticReference);
        return fRhumblineSegmentScanner;
      default:
        throw new IllegalArgumentException("Measure mode " + aMeasureMode + " not supported for the terrain ruler.");
    }
  }

}
