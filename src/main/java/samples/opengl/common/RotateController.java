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

import java.awt.event.*;

/**
 * A controller for the 3D view that allows users to rotate the camera around a fixed point.
 */
public class RotateController extends ALcdGLController {
  private MyMouseAdapter fMouseAdapter = new MyMouseAdapter();
  private MyMouseMotionAdapter fMouseMotionAdapter = new MyMouseMotionAdapter();
  private TLcdGLCartesianFixedReferenceCameraAdapter fCameraAdapter = new TLcdGLCartesianFixedReferenceCameraAdapter();
  private int fOldX, fOldY;

  public RotateController() {
    setName( "Rotate" );
    setShortDescription( "Rotate the view using the mouse" );
    setIcon( new TLcdGUIIcon( TLcdGUIIcon.ROTATE_3D_16 ) );
  }

  public void startInteraction( ILcdGLView aGLView ) {
    super.startInteraction( aGLView );
    aGLView.addMouseListener( fMouseAdapter );
    aGLView.addMouseMotionListener( fMouseMotionAdapter );
  }

  public void terminateInteraction( ILcdGLView aGLView ) {
    super.terminateInteraction( aGLView );
    aGLView.removeMouseListener( fMouseAdapter );
    aGLView.removeMouseMotionListener( fMouseMotionAdapter );
  }

  private class MyMouseAdapter extends MouseAdapter {
    public void mousePressed( MouseEvent e ) {
      fCameraAdapter.setCamera( ( (ILcdGLView) e.getSource() ).getCamera() );
      fOldX = e.getX();
      fOldY = e.getY();
    }
  }

  private class MyMouseMotionAdapter extends MouseMotionAdapter {
    public void mouseDragged( MouseEvent e ) {
      double pitch = fCameraAdapter.getPitch();
      double yaw = fCameraAdapter.getYaw();
      pitch -= e.getY() - fOldY;
      yaw += e.getX() - fOldX;
      fCameraAdapter.setPitch( pitch );
      fCameraAdapter.setYaw( yaw );
      fOldX = e.getX();
      fOldY = e.getY();
    }
  }
}
