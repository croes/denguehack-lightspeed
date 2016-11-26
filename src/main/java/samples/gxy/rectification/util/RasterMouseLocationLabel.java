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
package samples.gxy.rectification.util;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JLabel;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.TLcdGXYContext;

/**
 * A component that displays the current mouse position expressed in raster pixel coordinates.
 */
public class RasterMouseLocationLabel extends JLabel {

  private ILcdGXYView fGXYView;

  public RasterMouseLocationLabel(ILcdGXYView aGXYView) {
    if (aGXYView instanceof Component) {
      fGXYView = aGXYView;
      Component comp = (Component) aGXYView;
      comp.addMouseMotionListener(new MyMouseMotionListener());
    } else {
      throw new IllegalArgumentException("ILcdGXYView is not an instance of Component[" + aGXYView + "]");
    }
  }

  private class MyMouseMotionListener extends MouseMotionAdapter {

    public void mouseDragged(MouseEvent e) {
      this.mouseMoved(e);
    }

    public void mouseMoved(MouseEvent e) {
      if (fGXYView.layerCount() == 0) {
        return;
      }
      ILcdLayer layer = fGXYView.getLayer(0);
      ILcdModel model = layer.getModel();

      if (model == null) {
        return;
      }
      ILcdModelReference reference = model.getModelReference();
      if (!(reference instanceof ILcdGridReference)) {
        return;
      }

      try {
        TLcdGXYContext context = new TLcdGXYContext(fGXYView, (ILcdGXYLayer) fGXYView.getLayer(0));

        ILcdGXYViewXYWorldTransformation vw = context.getGXYViewXYWorldTransformation();
        ILcdModelXYWorldTransformation mw = context.getModelXYWorldTransformation();

        TLcdXYPoint pct1 = new TLcdXYPoint();
        TLcdXYZPoint pct2 = new TLcdXYZPoint();
        vw.viewXYPoint2worldSFCT(e.getX(), e.getY(), pct1);
        mw.worldPoint2modelSFCT(pct1, pct2);

        setText((int) pct2.getX() + ", " + (int) pct2.getY() + " px");
      } catch (TLcdOutOfBoundsException ex) {
        setText("Out Of Bounds");
      }
    }
  }
}

