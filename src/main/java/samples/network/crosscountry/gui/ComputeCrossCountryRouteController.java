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
package samples.network.crosscountry.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.controller.ALcdGXYController;

import samples.network.crosscountry.graph.CrossCountryRasterGraphManager;

/**
 * A controller for computing a cross country route.
 */
public class ComputeCrossCountryRouteController extends ALcdGXYController implements MouseListener {

  private static final ILcdIcon ROUTE_ICON = new TLcdImageIcon("samples/images/shortest_route.png");
  private CrossCountryRasterGraphManager fRasterGraphManager;
  private ILcdGXYLayer fLayer;

  public ComputeCrossCountryRouteController(CrossCountryRasterGraphManager aRasterGraphManager) {
    setShortDescription("Compute route");
    setIcon(ROUTE_ICON);

    fRasterGraphManager = aRasterGraphManager;
  }

  @Override
  public void startInteraction(ILcdGXYView aGXYView) {
    super.startInteraction(aGXYView);
    ILcdModel rasterModel = fRasterGraphManager.getModel();
    if (rasterModel != null) {
      fLayer = (ILcdGXYLayer) aGXYView.layerOf(rasterModel);
      // Make sure the start/end are either both set or both not set
      if (fRasterGraphManager.getStartNode() == null || fRasterGraphManager.getEndNode() == null) {
        fRasterGraphManager.setStartNode(null);
        fRasterGraphManager.setEndNode(null);
      }
    }
  }

  @Override
  public void terminateInteraction(ILcdGXYView aGXYView) {
    fLayer = null;
    super.terminateInteraction(aGXYView);
  }

  private ILcdPoint awtToModel(Point aAWTPoint) {
    try {
      TLcdXYPoint worldPoint = new TLcdXYPoint();
      TLcdGXYContext context = new TLcdGXYContext(getGXYView(), fLayer);
      context.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(aAWTPoint, worldPoint);
      TLcdXYZPoint modelPoint = new TLcdXYZPoint();
      context.getModelXYWorldTransformation().worldPoint2modelSFCT(worldPoint, modelPoint);
      if (!fRasterGraphManager.getRaster().getBounds().contains2D((ILcdPoint) modelPoint)) {
        throw new TLcdOutOfBoundsException("Outisde raster bounds");
      }
      return modelPoint;
    } catch (TLcdOutOfBoundsException e) {
      return null;
    }
  }

  // MouseListener

  public void mouseClicked(MouseEvent e) {
    if (fLayer != null) {
      ILcdPoint modelPoint = awtToModel(e.getPoint());
      if (modelPoint != null) {
        // Reset the start/end if necessary
        if (fRasterGraphManager.getStartNode() != null && fRasterGraphManager.getEndNode() != null) {
          fRasterGraphManager.setStartNode(null);
          fRasterGraphManager.setEndNode(null);
        }
        // Set the start/end node
        if (fRasterGraphManager.getStartNode() == null) {
          fRasterGraphManager.setStartNode(modelPoint);
        } else {
          fRasterGraphManager.setEndNode(modelPoint);
        }
      }
    }
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }
}
