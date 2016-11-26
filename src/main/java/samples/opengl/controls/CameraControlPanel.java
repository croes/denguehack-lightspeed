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

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.controller.composite.TLcdGLCompositeController;
import samples.gxy.common.TitledPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This class defines a GUI to interact with a TLcdGLCompositeController.
 */
class CameraControlPanel extends JPanel {

  private CompositeCameraControllerButton fTiltLeft;
  private CompositeCameraControllerButton fTiltRight;
  private CompositeCameraControllerButton fTiltUp;
  private CompositeCameraControllerButton fTiltDown;
  private CompositeCameraControllerButton fZoomIn;
  private CompositeCameraControllerButton fZoomOut;
  private CompositeCameraControllerButton fPanN;
  private CompositeCameraControllerButton fPanE;
  private CompositeCameraControllerButton fPanS;
  private CompositeCameraControllerButton fPanW;

  private CompositeCameraControllerButton[] fAllButtons;
  private TLcdSWIcon[] fIcons;

  public CameraControlPanel( final ILcdGLView aGLView, TLcdGLCompositeController aController ) {
    super();

    int step = 150;
    int b1 = MouseEvent.BUTTON1_MASK;
    int b2 = MouseEvent.BUTTON2_MASK;
    int b3 = MouseEvent.BUTTON3_MASK;

    fTiltLeft = new CompositeCameraControllerButton( aGLView, aController, step, 0, b3 );
    fTiltRight = new CompositeCameraControllerButton( aGLView, aController, -step, 0, b3 );
    fTiltUp = new CompositeCameraControllerButton( aGLView, aController, 0, step, b3 );
    fTiltDown = new CompositeCameraControllerButton( aGLView, aController, 0, -step, b3 );
    fZoomIn = new CompositeCameraControllerButton( aGLView, aController, 0, step, b2 );
    fZoomOut = new CompositeCameraControllerButton( aGLView, aController, 0, -step, b2 );
    fPanN = new CompositeCameraControllerButton( aGLView, aController, 0, step, b1 );
    fPanS = new CompositeCameraControllerButton( aGLView, aController, 0, -step, b1 );
    fPanE = new CompositeCameraControllerButton( aGLView, aController, -step, 0, b1 );
    fPanW = new CompositeCameraControllerButton( aGLView, aController, step, 0, b1 );

    fAllButtons = new CompositeCameraControllerButton[] {
            fTiltLeft, fTiltRight, fTiltUp, fTiltDown,
            fZoomIn, fZoomOut,
            fPanN, fPanE, fPanS, fPanW
    };

    fIcons = new TLcdSWIcon[] {
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_tiltright.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_tiltleft.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_tiltup.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_tiltdown.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_loopplus.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_loopminus.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_up.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_right.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_down.gif" ) ),
            new TLcdSWIcon( new TLcdImageIcon( "images/gui/3d/i16_left.gif" ) ),
    };

    addButtons();
    configureButtons();
  }

  private void addButtons() {

    JPanel container = new JPanel( new GridBagLayout() );

    GridBagConstraints c = new GridBagConstraints(
            0, 0, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fTiltLeft, c );

    c = new GridBagConstraints(
            4, 0, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fTiltRight, c );

    c = new GridBagConstraints(
            2, 0, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fPanN, c );

    c = new GridBagConstraints(
            3, 1, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fPanE, c );

    c = new GridBagConstraints(
            2, 2, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fPanS, c );

    c = new GridBagConstraints(
            1, 1, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fPanW, c );

    c = new GridBagConstraints(
            5, 0, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fTiltUp, c );

    c = new GridBagConstraints(
            5, 2, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fTiltDown, c );

    c = new GridBagConstraints(
            6, 0, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fZoomIn, c );

    c = new GridBagConstraints(
            6, 2, 1, 1,
            1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets( 2, 2, 2, 2 ),
            0, 0
    );
    container.add( fZoomOut, c );

    setLayout( new BorderLayout() );
    add( BorderLayout.CENTER, TitledPanel.createTitledPanel( "Camera Control", container ) );
  }

  private void configureButtons() {
    for ( int i = 0; i < fAllButtons.length ; i++ ) {
      fAllButtons[ i ].setIcon( fIcons[ i ] );
    }
  }
}
