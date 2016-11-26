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
package samples.gxy.rectification;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.controller.ALcdGXYController;

/**
 * A controller that creates a new pair of tie points at the location of the last mouse click. Each
 * created point is placed in the appropriate model.
 */
public class CreateTiePointController extends ALcdGXYController implements
                                                                MouseListener {
  private TiePointsRectifier fTiePointsRectifier;
  private ILcdGXYView fImgView;

  public CreateTiePointController(ILcdGXYView aImgView, TiePointsRectifier aTiePointsRectifier) {
    fImgView = aImgView;
    fTiePointsRectifier = aTiePointsRectifier;

    setName("Mouse Location");
    setShortDescription("Create a new tie point");
    setIcon(new TLcdImageIcon("images/gui/i16_cross.gif"));
    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }

  public void mouseClicked(MouseEvent aEvent) {
    if (aEvent.getSource() == fImgView && fImgView.layerCount() > 0) {
      try {

        //---------------------------------------------------------
        // Transform the AWT coordinates of the reference view into
        // tie point reference coordinates
        ILcdGXYLayer tie_point_src_layer = (ILcdGXYLayer) fImgView.getLayer(1);
        TLcdGXYContext context = new TLcdGXYContext(fImgView, tie_point_src_layer);

        ILcdGXYViewXYWorldTransformation vw = context.getGXYViewXYWorldTransformation();
        ILcdModelXYWorldTransformation mw = context.getModelXYWorldTransformation();

        TLcdXYZPoint pct1 = new TLcdXYZPoint();
        TLcdXYZPoint pct2 = new TLcdXYZPoint();
        vw.viewXYPoint2worldSFCT(aEvent.getX(), aEvent.getY(), pct1);
        mw.worldPoint2modelSFCT(pct1, pct2);

        // Add the tie point in pixel coordinates
        fTiePointsRectifier.getSourceTiePointModel().addElement(pct2, ILcdFireEventMode.FIRE_LATER);

        // Compute the corresponding location in the (axes-aligned) target raster coordinates
        // Normally for polynomial/rational rectified references these operations
        // will have no effect (scale is always 1, origin is always 0), but we'll
        // do it for completeness.
        ILcdBounds bounds = fTiePointsRectifier.getTargetRasterModel().getBounds();
        double scale_x = bounds.getWidth() / fTiePointsRectifier.getImageWidth();
        double scale_y = bounds.getHeight() / fTiePointsRectifier.getImageHeight();
        double x = bounds.getLocation().getX();
        double y = bounds.getLocation().getY();

        pct1.move2D(pct2.getX() * scale_x + x,
                    pct2.getY() * scale_y + y);

        ILcdModel raster_model = fTiePointsRectifier.getTargetRasterModel();
        ILcdModel tie_points_model = fTiePointsRectifier.getTargetTiePointModel();

        ILcdGeoReference raster_reference = (ILcdGeoReference) raster_model.getModelReference();
        ILcdGeoReference tie_pct_reference = (ILcdGeoReference) tie_points_model.getModelReference();

        TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference(raster_reference,
                                                                              tie_pct_reference);

        TLcdXYZPoint pct3 = new TLcdXYZPoint();
        g2g.sourcePoint2destinationSFCT(pct1, pct3);
        fTiePointsRectifier.getTargetTiePointModel().addElement(pct3, ILcdFireEventMode.FIRE_LATER);

        fTiePointsRectifier.getTiePointPairs().add(new TiePointsRectifier.TiePointPair(pct2, pct3));
        fTiePointsRectifier.getSourceTiePointModel().fireCollectedModelChanges();
        fTiePointsRectifier.getTargetTiePointModel().fireCollectedModelChanges();
      } catch (TLcdOutOfBoundsException ex) {
        // We can't add the point if it can't be transformed.
      }
    }
  }

  public void mousePressed(MouseEvent event) {
  }

  public void mouseReleased(MouseEvent event) {
  }

  public void mouseEntered(MouseEvent event) {
  }

  public void mouseExited(MouseEvent event) {
  }
}
