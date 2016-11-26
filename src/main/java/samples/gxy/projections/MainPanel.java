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
package samples.gxy.projections;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.projection.TLcdOrthographic;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.gxy.common.GXYSample;

/**
 * This sample demonstrates how to switch between several available projections
 * of the earth and to recenter the origin of the projection.
 * <p/>
 * Select a projection in the ProjectionComboBox and this projection is
 * applied to the view by means of SetProjectionAction. SetProjectionAction
 * will change the ILcdWorldReference of the ILcdGXYView with a new
 * ILcdGridReference based on the chosen ILcdProjection class, but will try to
 * preserve as much as possible the current view bounds by good choice of
 * ILcdProjection parameters.
 * Use the cross-haired button in the tool bar to recenter the origin of the
 * projection by clicking a point on the map. Depending on the ILcdProjection
 * type, the GXYCenterMapController will change the appropriate parameters.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-20.00, 20.00, 80.00, 40.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Set the initial projection of the ILcdGXYView
    TLcdGridReference reference = new TLcdGridReference(
        new TLcdGeodeticDatum(),
        new TLcdOrthographic(10, 40)
    );
    getView().setXYWorldReference(reference);

    // Add a projection combo box.
    getToolBars()[0].addComponent(new ProjectionComboBox(getView()));
    getToolBars()[0].addSpace();

    // Add a controller to center the map.
    GXYCenterMapController centerMapController = new GXYCenterMapController();
    getToolBars()[0].addGXYController(centerMapController);

    // Select the center map controller
    getView().setGXYController(getToolBars()[0].getGXYController(centerMapController));
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Projections");
  }
}
