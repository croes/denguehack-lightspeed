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
package samples.tea.gxy.los;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounded;
import com.luciad.tea.*;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.contour.TLcdComplexPolygonContourFinder;
import com.luciad.view.gxy.ILcdGXYPainter;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * Action to calculate the line-of-sight, given a center point a radius and a desired pixel density for the
 * resulting calculation.
 */
class CreateLOSAction extends AbstractAction {

  private static final String ERROR_MESSAGE_TITLE = "Invalid input for line-of-sight computation.";

  private LOSPanel                     fLOSPanel;
  private ALcdTerrainElevationProvider fTerrainElevationProvider;
  private TLcdLOSCoverageFactory       fLOSCoverageFactory;

  CreateLOSAction( LOSPanel aLOSPanel, ALcdTerrainElevationProvider aTerrainElevationProvider ) {
    fLOSPanel                 = aLOSPanel;
    fTerrainElevationProvider = aTerrainElevationProvider;
    fLOSCoverageFactory       = new TLcdLOSCoverageFactory();
  }

  public void actionPerformed( ActionEvent e ) {
    try {
      ILcdModel        model               = fLOSPanel.retrieveTargetLayer().getModel();
      ILcdGeoReference aTargetReference    = (ILcdGeoReference) model.getModelReference();
      double           aTargetPixelDensity = fLOSPanel.retrievePixelDensity();

      double aMinVerticalAngle = fLOSPanel.retrieveMinVerticalAngle();
      double aMaxVerticalAngle = fLOSPanel.retrieveMaxVerticalAngle();

      TLcdCoverageAltitudeMode aAltitudeMode = fLOSPanel.retrieveAltitudeMode();
      int aComputationAlgorithm = fLOSPanel.retrieveComputationAlgorithm();
      double aFixedHeightAboveEllipsoid = fLOSPanel.retrieveFixedHeightAboveEllipsoid();

      TLcdCoverageFillMode aFillMode = fLOSPanel.retrieveFillMode();

      TLcdLOSCoverageFactory       aLOSCoverageFactory       = fLOSCoverageFactory;
      ALcdTerrainElevationProvider aTerrainElevationProvider = fTerrainElevationProvider;

      // Create a circular area to compute the line-of-sight for. An line-of-sight coverage describes
      // both the outline of the area and the sampling inside the area.
      ILcdLOSCoverage aLOSCoverage = fLOSPanel.createLOSCoverage();

      // Create a propagation function describing how the detection wave propagates (visual, radar).
      ILcdLOSPropagationFunction los_propagation_function = createLOSPropagationFunction(
              aTerrainElevationProvider,
              aAltitudeMode,
              aMinVerticalAngle,
              aMaxVerticalAngle,
              aComputationAlgorithm,
              aFixedHeightAboveEllipsoid,
              fLOSPanel.retrieveRadarTiltAngle(),
              fLOSPanel.retrieveRadarTiltAzimuth(),
              fLOSPanel.retrieveKFactor()
      );

      // Compute and create the line-of-sight coverage matrix according to the propagation function
      // on the covered area.
      ILcdLOSCoverageMatrix los_coverage_matrix = createLOSCoverageMatrix(
              aLOSCoverageFactory,
              los_propagation_function,
              aLOSCoverage,
              ( ILcdGeoReference ) model.getModelReference()
      );

      if(fLOSPanel.isOutputAsContours()) {
        // Convert the line-of-sight coverage matrix into a line-of-sight contours.
        ILcdBounded[] contours = createLOSCoverageContour(
                fLOSCoverageFactory,
                los_coverage_matrix,
                aTargetReference,
                aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION_FIXED_HEIGHT ?  new double[0] : LOSPainter.getLosLevelsInterval(),
                aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION_FIXED_HEIGHT ?  LOSPainter.getLosLevelsFixedHeight() : LOSPainter.getLosLevelsSpecial());

        model.removeAllElements( ILcdFireEventMode.FIRE_LATER );
        addArrayToModelSFCT(contours, model);


        // Update the painter of the target layer, to use the correct color model.
        ILcdGXYPainter painter = fLOSPanel.retrieveTargetLayer().getGXYPainter( contours[0] );
        if ( painter instanceof LOSPainter ) {
          ( ( LOSPainter ) painter ).setComputationAlgorithm( aComputationAlgorithm );
        }
      } else {
        // Convert the line-of-sight coverage matrix into a line-of-sight coverage raster.
        ILcdRaster los_coverage_raster = createLOSCoverageRaster(
                aLOSCoverageFactory,
                los_coverage_matrix,
                aTargetReference,
                aTargetPixelDensity,
                aFillMode,
                aComputationAlgorithm
        );

        // Add the raster to the layer.
        model.removeAllElements( ILcdFireEventMode.FIRE_LATER );
        model.addElement( los_coverage_raster, ILcdFireEventMode.FIRE_NOW );

        // Update the painter of the target layer, to use the correct color model.
        ILcdGXYPainter painter = fLOSPanel.retrieveTargetLayer().getGXYPainter( los_coverage_raster );
        if ( painter instanceof LOSPainter ) {
          ( ( LOSPainter ) painter ).setComputationAlgorithm( aComputationAlgorithm );
        }
      }

    }
    catch ( IllegalArgumentException exception ) {
      showErrorMessage( exception.getMessage() );
    }
  }


  public static void addArrayToModelSFCT( Object[] aObjects, ILcdModel aModelSFCT ) {
    TLcdLockUtil.writeLock( aModelSFCT );
    try {
      for ( int index = 0; index < aObjects.length ; index++ ) {
        aModelSFCT.addElement( aObjects[ index ], ILcdFireEventMode.FIRE_LATER );
      }
    } finally {
      TLcdLockUtil.writeUnlock( aModelSFCT );
    }
    aModelSFCT.fireCollectedModelChanges();
  }


  private ILcdLOSPropagationFunction createLOSPropagationFunction(
                                           ALcdTerrainElevationProvider aTerrainElevationProvider,
                                           TLcdCoverageAltitudeMode     aAltitudeMode,
                                           double                       aMinVerticalAngle,
                                           double                       aMaxVerticalAngle,
                                           int                          aComputationAlgorithm,
                                           double                       aFixedHeightAboveEllipsoid,
                                           double aMaxRadarTiltAngle,
                                           double aMaxRadarTiltAzimuth,
                                           double aKFactor) {
    TLcdLOSRadarPropagationFunction propagation_function = new TLcdLOSRadarPropagationFunction(
            TLcdEarthRepresentationMode.SPHERICAL_EULER_RADIUS,
            aTerrainElevationProvider,
            aAltitudeMode,
            aMinVerticalAngle,
            aMaxVerticalAngle,
            aMaxRadarTiltAngle,
            aMaxRadarTiltAzimuth,
            aKFactor       // K-factor 1.0 : visual line-of-sight
    );

    if ( aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION ) {
        return propagation_function;
    }
    if ( aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION_SKY_BACKGROUND ) {
      return new TLcdLOSRadarPropagationFunctionSkyBackground( propagation_function, aAltitudeMode );
    }
    if ( aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION_FIXED_HEIGHT ) {
      return new TLcdLOSPropagationFunctionFixedHeight( propagation_function, aFixedHeightAboveEllipsoid, aAltitudeMode );
    }
    throw new IllegalArgumentException( "Unknown propagation function." );
  }

  private ILcdLOSCoverageMatrix createLOSCoverageMatrix(
                                           TLcdLOSCoverageFactory     aLOSCoverageFactory,
                                           ILcdLOSPropagationFunction aLOSPropagationFunction,
                                           ILcdLOSCoverage            aLOSCoverage,
                                           ILcdGeoReference           aTargetReference ) {

    ILcdGeoReference matrix_reference = aTargetReference;
    // Make sure the matrix reference is a geodetic reference.
    if ( !( matrix_reference instanceof ILcdGeodeticReference ) ) {
      matrix_reference = new TLcdGeodeticReference(ModelFactory.getGeoidGeodeticDatum() );
    }

    return aLOSCoverageFactory.createLOSCoverageMatrix(
        aLOSPropagationFunction,
        aLOSCoverage,
        matrix_reference
    );
}

  private ILcdRaster createLOSCoverageRaster( TLcdLOSCoverageFactory aLOSCoverageFactory,
                                              ILcdLOSCoverageMatrix  aLOSCoverageMatrix,
                                              ILcdGeoReference       aTargetReference,
                                              double                 aTargetPixelDensity,
                                              TLcdCoverageFillMode   aFillMode,
                                              int                    aComputationAlgorithm ) {

    ILcdMatrixRasterValueMapper matrix_raster_value_mapper;
    if ( aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION ) {
      matrix_raster_value_mapper = new MatrixRasterValueMapperRadar();
    }
    else if ( aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION_SKY_BACKGROUND ) {
      matrix_raster_value_mapper = new MatrixRasterValueMapperRadarSkyBackground();
    }
    else if ( aComputationAlgorithm == LOSPanel.PROPAGATION_FUNCTION_FIXED_HEIGHT ) {
      matrix_raster_value_mapper = new MatrixRasterValueMapperFixedHeight();
    }
    else {
      throw new IllegalArgumentException( "Unknown propagation function." );
    }

    return aLOSCoverageFactory.createLOSCoverageRaster(
            aLOSCoverageMatrix,
            matrix_raster_value_mapper,
            aTargetReference,
            aTargetPixelDensity,
            aFillMode
    );
  }

  private static void showErrorMessage( String aMessage ) {
    JOptionPane.showMessageDialog(
              null, aMessage, ERROR_MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE
    );
  }




  private ILcdBounded[] createLOSCoverageContour( TLcdLOSCoverageFactory aLOSCoverageFactory,
                                               ILcdLOSCoverageMatrix  aLOSCoverageMatrix,
                                               ILcdGeoReference       aTargetReference,
                                               double[] aIntervalLevels,
                                               double[] aSpecialLevels) {

    //ILcdLOSCoverageMatrix m = new TempUtil.ResamplingLOSCoverageMatrix(1, 1, aLOSCoverageMatrix);
    return aLOSCoverageFactory.createLOSCoverageContours(
            aLOSCoverageMatrix,
            aTargetReference,
            TLcdComplexPolygonContourFinder.IntervalMode.INTERVAL,
            aIntervalLevels,
            aSpecialLevels
    );
  }
}
