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
package samples.gxy.common.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.map.TLcdMapLocation;

/**
 * This component displays the model coordinates of the location under the
 * mousepointer on a map component in DDMMSS format.
 * A TLcdMapLocation is used to compute the model coordinates.
 */
public class MouseLocationComponentAWT
    extends Component {

  private TLcdMapLocation fMapLocation = new TLcdMapLocation();
  private Dimension fPreferredSize = new Dimension(150, 25);
  private boolean fTrackDragging = true;


  public MouseLocationComponentAWT(ILcdGXYView aGXYView) {
    if (aGXYView instanceof Component) {
      TLcdMapLocation map_location = new TLcdMapLocation();
      map_location.setGXYView(aGXYView);
      initialize(map_location, (Component) aGXYView);
    } else {
      throw new IllegalArgumentException(
          "ILcdGXYView is not an instance of Component[" + aGXYView + "]");
    }
  }

  public MouseLocationComponentAWT(TLcdMapLocation aMapLocation,
                                   Component aGXYViewComponent) {
    initialize(aMapLocation, aGXYViewComponent);
  }


  private void initialize(TLcdMapLocation aMapLocation,
                          Component aGXYViewComponent) {
    fMapLocation = aMapLocation;
    aGXYViewComponent.addMouseMotionListener(new MyMouseMotionListener());
    fMapLocation.setMode(TLcdMapLocation.LOCATION_DDMMSS);
    fMapLocation.setAlignment(TLcdMapLocation.CENTER);
  }

  public TLcdMapLocation getMapLocation() {
    return fMapLocation;
  }


  class MyMouseMotionListener extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent e) {
      if (fTrackDragging) {
        this.mouseMoved(e);
      }
    }

    public void mouseMoved(MouseEvent e) {
      fMapLocation.mouseMoved(e);
      repaint();
    }
  }


  public void paint(Graphics aGraphics) {
    fMapLocation.paint(
        aGraphics,
        new Rectangle(0, 0, getSize().width, getSize().height)
                      );
  }

  public Dimension getMinimumSize() {
    return fPreferredSize;
  }

  public Dimension getPreferredSize() {
    return fPreferredSize;
  }
}

