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
package samples.gxy.grid.multilevel.gars;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;

/**
 * A component that displays the mouse position in function of a GARS grid.
 */
public class GARSFormatComponent
    extends JLabel
    implements MouseMotionListener {

  private TLcdGXYContext fGXYContext = new TLcdGXYContext();

  // temp variables required to transform the mouse position into a string.
  private TLcdXYPoint fTempWorldPoint = new TLcdXYPoint();
  private TLcdLonLatHeightPoint fTempModelPoint = new TLcdLonLatHeightPoint();
  private GARSGridCoordinate fTempGARSGridCoordinate = new GARSGridCoordinate();
  private GARSCoordinateFormat fGARSCoordinateFormat = new GARSCoordinateFormat();
  private StringBuffer fCoordinateBuffer = new StringBuffer();

  // the fixed size of the component. Required so that the components in the flowlayout do not wiggle.
  private Dimension fSize = new Dimension(100, 20);

  private GARSGridLayer fGARSGridLayer = new GARSGridLayer();
  private ILcdGXYView fGXYView;

  public GARSFormatComponent(ILcdGXYView aGXYView) {
    fGXYView = aGXYView;
    if (aGXYView instanceof JComponent) {
      ((JComponent) aGXYView).addMouseMotionListener(this);
    }
  }

  public Dimension getMinimumSize() {
    return fSize;
  }

  public Dimension getMaximumSize() {
    return fSize;
  }

  public Dimension getPreferredSize() {
    return fSize;
  }

  public void mouseDragged(MouseEvent me) {
    mouseMoved(me);
  }

  public void mouseMoved(MouseEvent me) {
    fGXYContext.resetFor(fGARSGridLayer, fGXYView);
    Point mouse_position = me.getPoint();
    try {
      // transform the mouse position to model coordinates expressed in the reference underlying the grid.
      fGXYContext.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(mouse_position, fTempWorldPoint);
      fGXYContext.getModelXYWorldTransformation().worldPoint2modelSFCT(fTempWorldPoint, fTempModelPoint);
    } catch (TLcdOutOfBoundsException e) {
      // the mouse is outside the world.
      setText("Outside World");
      return;
    }

    try {
      // depending on the level that is currently visible, find the GARS coordinate that covers the
      // the mouse position.
      if (fGARSGridLayer.isPaintLevel(GARSGridCoordinate.KEYPAD, fGXYContext)) {
        GARSGridUtil.GARSKeypadAtSFCT(fTempModelPoint, fGARSGridLayer.getGARSGrid(), (ILcdGeoReference) fGARSGridLayer.getModel().getModelReference(), fTempGARSGridCoordinate);
      } else if (fGARSGridLayer.isPaintLevel(GARSGridCoordinate.QUADRANT, fGXYContext)) {
        GARSGridUtil.GARSQuadrantAtSFCT(fTempModelPoint, fGARSGridLayer.getGARSGrid(), (ILcdGeoReference) fGARSGridLayer.getModel().getModelReference(), fTempGARSGridCoordinate);
      } else {
        GARSGridUtil.GARSCellAtSFCT(fTempModelPoint, fGARSGridLayer.getGARSGrid(), (ILcdGeoReference) fGARSGridLayer.getModel().getModelReference(), fTempGARSGridCoordinate);
      }
      // convert the GARS coordinate into a string.
      fCoordinateBuffer.delete(0, fCoordinateBuffer.length());
      fGARSCoordinateFormat.format(fTempGARSGridCoordinate, fCoordinateBuffer, null);
      setText(fCoordinateBuffer.toString());
    } catch (TLcdOutOfBoundsException e) {
      // we are outside the grid.
      setText("Outside GARS grid");
    }
  }
}
