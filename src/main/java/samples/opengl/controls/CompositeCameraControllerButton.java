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
package samples.opengl.controls;

import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewEvent;
import com.luciad.view.opengl.controller.composite.TLcdGLCompositeController;

import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A button that interacts with a TLcdGLCompositeController. While the button
 * held down, it sends simulated mouse input to the controller, thereby causing
 * the 3D view to be updated.
 */
class CompositeCameraControllerButton extends JButton implements MouseListener {

  private static final Dimension BUTTON_DIMENSIONS = new Dimension( 24, 24 );


  private TLcdGLCompositeController fCompositeController;
  private ILcdGLView fGLView;

  private int fDeltaX = 0;
  private int fDeltaY = 0;
  private int fModifiers = 0;

  private CameraAnimator fAnimator = new CameraAnimator();

  public CompositeCameraControllerButton( ILcdGLView aGLView, TLcdGLCompositeController aController, int aDeltaX, int aDeltaY, int aModifiers ) {
    super();
    addMouseListener( this );
    fGLView = aGLView;
    fDeltaX = aDeltaX;
    fDeltaY = aDeltaY;
    fModifiers = aModifiers;
    fCompositeController = aController;
  }

  @Override
  public Dimension getPreferredSize() {
    return BUTTON_DIMENSIONS;
  }

  @Override
  public Dimension getMinimumSize() {
    return BUTTON_DIMENSIONS;
  }

  public void mousePressed( MouseEvent me ) {
    if ( me.getModifiers() == MouseEvent.BUTTON1_MASK ) {
      fAnimator.init();
      fGLView.addViewListener( fAnimator );
      fGLView.repaint();
    }
  }

  public void mouseReleased( MouseEvent me ) {
    if ( me.getModifiers() == MouseEvent.BUTTON1_MASK ) {
      fGLView.removeViewListener( fAnimator );
    }
  }

  public void mouseClicked( MouseEvent me ) {
  }

  public void mouseEntered( MouseEvent aEvent ) {
  }

  public void mouseExited( MouseEvent aEvent ) {
  }

  /**
   * An ILcdGLViewListener which continuously repaints the 3D view and sends
   * simulated mouse input to the controller.
   */
  private class CameraAnimator extends ALcdGLViewAdapter {

    private double fTime;

    public void init() {
      fTime = System.nanoTime() * 1e-9;
    }

    public void postRender( TLcdGLViewEvent aEvent ) {

      // Measure the time elapsed. It is used below to scale the simulated mouse movement.
      double t = System.nanoTime() * 1e-9;
      double dt = t - fTime;
      fTime = t;

      int x0 = aEvent.getView().getWidth() / 2;
      int y0 = aEvent.getView().getHeight() - 20;

      double dx = dt * fDeltaX;
      double dy = dt * fDeltaY;

      dx = dx < 0 ? Math.floor( dx ) : Math.ceil( dx );
      dy = dy < 0 ? Math.floor( dy ) : Math.ceil( dy );

      double x1 = x0 + dx;
      double y1 = y0 + dy;

      // Delegate to the appropriate controller action.
      if ( ( fModifiers & MouseEvent.BUTTON1_MASK ) != 0 ) {
        fCompositeController.getPanAction().mousePressed( fGLView, x0, y0 );
        fCompositeController.getPanAction().interact( fGLView, x1, y1, dx, dy );
        fCompositeController.getPanAction().mouseReleased( fGLView, ( int ) x1, ( int ) y1 );
      }
      else if ( ( fModifiers & MouseEvent.BUTTON2_MASK ) != 0 ) {
        fCompositeController.getZoomAction().mousePressed( fGLView, x0, y0 );
        fCompositeController.getZoomAction().interact( fGLView, x1, y1, -dx, -dy );
        fCompositeController.getZoomAction().mouseReleased( fGLView, ( int ) x1, ( int ) y1 );
      }
      else if ( ( fModifiers & MouseEvent.BUTTON3_MASK ) != 0 ) {
        fCompositeController.getRotateAction().mousePressed( fGLView, x0, y0 );
        fCompositeController.getRotateAction().interact( fGLView, x1, y1, dx, dy );
        fCompositeController.getRotateAction().mouseReleased( fGLView, ( int ) x1, ( int ) y1 );
      }

      // Repaint.
      Thread.yield();
      fGLView.repaint();
    }
  }
}
