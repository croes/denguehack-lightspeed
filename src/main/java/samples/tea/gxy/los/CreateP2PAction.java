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

import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.tea.*;
import com.luciad.util.ILcdFireEventMode;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action that creates a point-to-point intervisibility object from the input
 * of a point-to-point panel.
 */
class CreateP2PAction extends AbstractAction {

  private P2PPanel                     fP2PPanel;
  private ALcdTerrainElevationProvider fTerrainElevationProvider;
  private TLcdP2PCoverageFactory       fP2PCoverageFactory;

  CreateP2PAction( P2PPanel aP2PPanel, ALcdTerrainElevationProvider aTerrainElevationProvider ) {
    fP2PPanel                 = aP2PPanel;
    fTerrainElevationProvider = aTerrainElevationProvider;
    fP2PCoverageFactory       = new TLcdP2PCoverageFactory();
  }

  public void actionPerformed( ActionEvent e ) {
    try {
      ILcdModel        model            = fP2PPanel.retrieveTargetLayer().getModel();
      ILcdGeoReference aTargetReference = (ILcdGeoReference) model.getModelReference();

      TLcdP2PCoverageFactory       aP2PCoverageFactory       = fP2PCoverageFactory;
      ALcdTerrainElevationProvider aTerrainElevationProvider = fTerrainElevationProvider;

      int aComputationAlgorithm = P2PPanel.PROPAGATION_FUNCTION;

      // Create a linear area to compute the line-of-sight for. A point-to-point coverage describes
      // both the defining points and the sampling step.
      ILcdP2PCoverage aP2PCoverage = fP2PPanel.createP2PCoverage();

      // Create a propagation function describing how the detection wave propagates (visual, radar).
      ILcdP2PPropagationFunction p2p_propagation_function = createP2PPropagationFunction(
              aTerrainElevationProvider,
              aComputationAlgorithm
      );

      // Create the intervisibility according to the 'radar' propagation function on the covered area.
      ILcdExtendedPoint2PointIntervisibility p2p_intervisibility = createExtendedPoint2PointIntervisibility(
              aP2PCoverageFactory,
              p2p_propagation_function,
              aP2PCoverage,
              aTerrainElevationProvider,
              aTargetReference
      );

      // Create a propagation function describing how the detection wave propagates (visual, radar)
      // that also implements the sky in background functionality.
      ILcdP2PPropagationFunction p2p_propagation_function_skybackground = createP2PPropagationFunction(
              aTerrainElevationProvider, P2PPanel.PROPAGATION_FUNCTION_SKY_BACKGROUND
      );

      // Create the intervisibility according to the 'skybackground' propagation function on the covered
      // area.
      ILcdExtendedPoint2PointIntervisibility p2p_skybackground = createExtendedPoint2PointIntervisibility(
              aP2PCoverageFactory,
              p2p_propagation_function_skybackground,
              aP2PCoverage,
              aTerrainElevationProvider,
              aTargetReference
      );

      // Combine both point-to-point computations.
      P2PIntervisibility my_p2p = new P2PIntervisibility( p2p_intervisibility,  p2p_skybackground );

      // Add the raster to the layer.
      model.removeAllElements( ILcdFireEventMode.FIRE_LATER );
      model.addElement( my_p2p, ILcdFireEventMode.FIRE_NOW );
    }
    catch ( IllegalArgumentException e1 ) {
      JOptionPane.showMessageDialog(
              null, e1.getMessage(), "Invalid input for point-to-point .computation", JOptionPane.ERROR_MESSAGE
      );
    }
  }

  private ILcdP2PPropagationFunction createP2PPropagationFunction(
                                             ALcdTerrainElevationProvider aTerrainElevationProvider,
                                             int                          aComputationAlgorithm ) {
    if ( aComputationAlgorithm == P2PPanel.PROPAGATION_FUNCTION ) {
      return new TLcdP2PRadarPropagationFunction(
              TLcdEarthRepresentationMode.SPHERICAL_EULER_RADIUS,
              aTerrainElevationProvider,
              1.0       // K-factor 1.0 : visual point-to-point
      );
    }
    if ( aComputationAlgorithm == P2PPanel.PROPAGATION_FUNCTION_SKY_BACKGROUND ) {
      return new TLcdP2PRadarPropagationFunctionSkyBackground(
              TLcdEarthRepresentationMode.SPHERICAL_EULER_RADIUS,
              aTerrainElevationProvider,
              1.0,      // K-factor 1.0 : visual point-to-point
              50000     // SkyDistance : distance after the second point in which the terrain
                        //               is taken into account as possible background.
      );
    }
    throw new IllegalArgumentException( "Unknown propagation function." );
  }

  private ILcdExtendedPoint2PointIntervisibility createExtendedPoint2PointIntervisibility(
                                             TLcdP2PCoverageFactory       aP2PCoverageFactory,
                                             ILcdP2PPropagationFunction   aP2PPropagationFunction,
                                             ILcdP2PCoverage              aP2PCoverage,
                                             ALcdTerrainElevationProvider aTerrainElevationProvider,
                                             ILcdGeoReference             aTargetReference ) {
    return aP2PCoverageFactory.createPoint2PointIntervisibility(
            aP2PPropagationFunction,
            aP2PCoverage,
            aTargetReference,
            aTerrainElevationProvider
    );
  }

}
