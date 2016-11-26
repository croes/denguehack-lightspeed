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
package samples.opengl.clipping;

import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLClippingPlane;
import com.luciad.view.opengl.TLcdGLClippingPlaneManager;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import org.hiranabe.vecmath.Vector3d;
import samples.opengl.common.Abstract3DPanel;
import samples.gxy.common.TitledPanel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main panel of the first sample application.
 */
class MainPanel extends Abstract3DPanel {

  private static TLcdGLClippingPlane sActivePlaneX;
  private static TLcdGLClippingPlane sActivePlaneY;

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    Abstract3DPanel.setupCamera( canvas );
    Abstract3DPanel.setupLights( canvas );

    ILcdGLCamera camera = canvas.getCamera();
    camera.setAltitudeExaggerationFactor( 10 );

    return canvas;
  }

  protected void createGUI() {
    super.createGUI();
    setComponentSouth( createClippingPlaneControlPanel( getCanvas() ) );
  }

  protected void addData() {
    // Add the initial data
    getCanvas().addModel( ModelFactory.createEllipseModel() );
  }

  private static void createClippingPlanePositiveXAxis( ILcdGLView aCanvas ) {
    Vector3d normal = new Vector3d( 1, 0, 0 );
    Vector3d point = new Vector3d( 0, 0, 0 );
    sActivePlaneX = createClippingPlane( aCanvas, normal, point, sActivePlaneX );
  }

  private static void createClippingPlanePositiveYAxis( ILcdGLView aCanvas ) {
    Vector3d normal = new Vector3d( 0, 1, 0 );
    Vector3d point = new Vector3d( 0, 0, 0 );
    sActivePlaneY = createClippingPlane( aCanvas, normal, point, sActivePlaneY );
  }

  private static void createClippingPlaneNegativeXAxis( ILcdGLView aCanvas ) {
    Vector3d normal = new Vector3d( -1, 0, 0 );
    Vector3d point = new Vector3d( 0, 0, 0 );
    sActivePlaneX = createClippingPlane( aCanvas, normal, point, sActivePlaneX );
  }

  private static void createClippingPlaneNegativeYAxis( ILcdGLView aCanvas ) {
    Vector3d normal = new Vector3d( 0, -1, 0 );
    Vector3d point = new Vector3d( 0, 0, 0 );
    sActivePlaneY = createClippingPlane( aCanvas, normal, point, sActivePlaneY );
  }

  private static void disableClippingXAxis() {
    if ( sActivePlaneX != null ) {
      sActivePlaneX.dispose();
      sActivePlaneX = null;
    }
  }

  private static void disableClippingYAxis() {
    if ( sActivePlaneY != null ) {
      sActivePlaneY.dispose();
      sActivePlaneY = null;
    }
  }

  private static TLcdGLClippingPlane createClippingPlane( ILcdGLView aCanvas, Vector3d aNormal, Vector3d aPoint, TLcdGLClippingPlane aPlaneToReplace ) {
    boolean auto_update_to_restore = aCanvas.isAutoUpdate();
    TLcdGLClippingPlane plane;
    try {
      aCanvas.setAutoUpdate( false );
      TLcdGLClippingPlaneManager clippingPlaneManager = aCanvas.getClippingPlaneManager();
      if ( aPlaneToReplace != null ) {
        aPlaneToReplace.dispose();
      }

      plane = clippingPlaneManager.createClippingPlane();
      plane.setEnabled( true );

      plane.setPlaneEquation( aNormal, aPoint );
    } finally {
      aCanvas.setAutoUpdate( auto_update_to_restore );
    }
    aCanvas.invalidate( true, MainPanel.class, "New clipping plane" );
    aCanvas.repaint();
    return plane;
  }

  private JPanel createClippingPlaneControlPanel( final ILcdGLView aGLView ) {
    JRadioButton positiveX = new JRadioButton( "Positive" );
    positiveX.addActionListener(
            new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                if ( ( (JRadioButton) e.getSource() ).isSelected() )
                  createClippingPlanePositiveXAxis( aGLView );
              }
            }
    );
    JRadioButton negativeX = new JRadioButton( "Negative" );
    negativeX.addActionListener(
            new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                if ( ( (JRadioButton) e.getSource() ).isSelected() )
                  createClippingPlaneNegativeXAxis( aGLView );
              }
            }
    );
    JRadioButton noneX = new JRadioButton( "No clipping" );
    noneX.addActionListener(
            new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                if ( ( (JRadioButton) e.getSource() ).isSelected() )
                  disableClippingXAxis();
              }
            }
    );

    JRadioButton positiveY = new JRadioButton( "Positive" );
    positiveY.addActionListener(
            new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                if ( ( (JRadioButton) e.getSource() ).isSelected() )
                  createClippingPlanePositiveYAxis( aGLView );
              }
            }
    );
    JRadioButton negativeY = new JRadioButton( "Negative" );
    negativeY.addActionListener(
            new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                if ( ( (JRadioButton) e.getSource() ).isSelected() )
                  createClippingPlaneNegativeYAxis( aGLView );
              }
            }
    );
    JRadioButton noneY = new JRadioButton( "No clipping" );
    noneY.addActionListener(
            new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                if ( ( (JRadioButton) e.getSource() ).isSelected() )
                  disableClippingYAxis();
              }
            }
    );

    ButtonGroup xButtons = new ButtonGroup();
    xButtons.add( negativeX );
    xButtons.add( positiveX );
    xButtons.add( noneX );

    ButtonGroup yButtons = new ButtonGroup();
    yButtons.add( negativeY );
    yButtons.add( positiveY );
    yButtons.add( noneY );

    JPanel xPanel = new JPanel();
    xPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    xPanel.setLayout( new BoxLayout( xPanel, BoxLayout.X_AXIS ) );
    xPanel.add( positiveX );
    xPanel.add( negativeX );
    xPanel.add( noneX );

    JPanel yPanel = new JPanel();
    yPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    yPanel.setLayout( new BoxLayout( yPanel, BoxLayout.X_AXIS ) );
    yPanel.add( positiveY );
    yPanel.add( negativeY );
    yPanel.add( noneY );

    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
    panel.add( TitledPanel.createTitledPanel( " X-axis", xPanel, TitledPanel.NORTH | TitledPanel.EAST ) );
    panel.add( TitledPanel.createTitledPanel( " Y-axis", yPanel, TitledPanel.NORTH ) );

    noneX.doClick();
    noneY.doClick();

    return panel;
  }

}
