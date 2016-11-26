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
package samples.opengl.common;

import com.luciad.gui.TLcdGUIIcon;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.controller.ALcdGLController;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

/**
 * A controller for the 3D view that allows users to zoom in and out by moving
 * the camera closer or farther along its line of sight.
 */
public class ZoomDistanceController extends ALcdGLController {
  private TLcdGLCartesianFixedReferenceCameraAdapter fCameraAdapter = new TLcdGLCartesianFixedReferenceCameraAdapter();
  private MyMouseAdapter fMouseAdapter = new MyMouseAdapter();
  private int fOldY;

  private double fMinDistance = 0.0;
  private double fMaxDistance = 1000000.0;

  public ZoomDistanceController() {
    this.setName( "Zoom" );
    this.setShortDescription( "Zoom distance" );
    this.setIcon( new TLcdGUIIcon( TLcdGUIIcon.LOOP16 ) );
  }

  public void startInteraction( ILcdGLView aGLView ) {
    super.startInteraction( aGLView );
    aGLView.addMouseListener( fMouseAdapter );
    aGLView.addMouseMotionListener( fMouseAdapter );
  }

  public void terminateInteraction( ILcdGLView aGLView ) {
    super.terminateInteraction( aGLView );
    aGLView.removeMouseListener( fMouseAdapter );
    aGLView.removeMouseMotionListener( fMouseAdapter );
  }

  private class MyMouseAdapter extends MouseInputAdapter {
    public void mousePressed( MouseEvent e ) {
      fOldY = e.getY();
    }

    public void mouseDragged( MouseEvent e ) {
      ILcdGLCamera camera = ( (ILcdGLView) e.getSource() ).getCamera();
      fCameraAdapter.setCamera( camera );
      double d = fCameraAdapter.getDistance();

      // Multiplying the fov by a factor ensures a relative change in fov:
      // Small fov values result in small changes and vice versa.
      double factor = 1.0 + ( e.getY() - fOldY ) / 500.0;
      factor = Math.max( factor, 0.01 ); // Prevent negative factors
      d = factor * d;

      d = Math.min( Math.max( d, fMinDistance ), fMaxDistance );
      fCameraAdapter.setDistance( d );
      fOldY = e.getY();
    }
  }
}
