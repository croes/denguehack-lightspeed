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
package samples.opengl.lighting;

import com.luciad.view.opengl.ALcdGLLight;
import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLDirectionalLight;
import com.luciad.view.opengl.TLcdGLPositionalLight;
import com.luciad.view.opengl.TLcdGLSpotLight;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import samples.opengl.common.Abstract3DPanel;
import samples.gxy.common.TitledPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main panel of the lighting application.
 */
class MainPanel extends Abstract3DPanel {

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    Abstract3DPanel.setupCamera( canvas );
    ILcdGLCamera camera = canvas.getCamera();
    camera.setAltitudeExaggerationFactor( 10 );
    return canvas;
  }

  protected void createGUI() {
    super.createGUI();
    setComponentSouth( createLightControlPanel( getCanvas() ) );
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createEllipseModel() );
  }

  private static void createStationarySpotLight( ILcdGLView aCanvas ) {
    boolean auto_update_to_restore = aCanvas.isAutoUpdate();
    try {
    aCanvas.setAutoUpdate( false );
    TLcdGLSpotLight spotLight = new TLcdGLSpotLight( ALcdGLLight.LightType.STATIONARY );
    spotLight.setSpotCutoff( 45 );
    spotLight.setPosition( new float[] { 0, -30000, 30000 } );
    spotLight.setDirection( new float[] { 0, 1, -1 } );
    spotLight.setDiffuseColor( Color.white );
    spotLight.setEnabled( true );
    aCanvas.setLightingEnabled( true );
    aCanvas.getLights().clear();
    aCanvas.getLights().add(spotLight);
    aCanvas.setAutoUpdate( true );
    } finally {
      aCanvas.setAutoUpdate(auto_update_to_restore);
    }
    aCanvas.invalidate( true, MainPanel.class, "New light" );
  }

  private static void createViewpointSpotLight( ILcdGLView aCanvas ) {
    boolean auto_update_to_restore = aCanvas.isAutoUpdate();
    try {
    aCanvas.setAutoUpdate( false );
    TLcdGLSpotLight spotLight = new TLcdGLSpotLight(ALcdGLLight.LightType.VIEWPOINT);
    spotLight.setSpotCutoff( 45 );
    spotLight.setPosition( new float[] { 0, 0, 0 } );
    spotLight.setDirection( new float[] { 0, 0, -1 } );
    spotLight.setDiffuseColor( Color.white );
    spotLight.setEnabled( true );
    aCanvas.setLightingEnabled( true );
    aCanvas.getLights().clear();
    aCanvas.getLights().add(spotLight);
    aCanvas.setAutoUpdate( true );
    } finally {
      aCanvas.setAutoUpdate(auto_update_to_restore);
    }
    aCanvas.invalidate( true, MainPanel.class, "New light" );
  }

  private static void createStationaryDirectionalLight( ILcdGLView aCanvas ) {
    boolean auto_update_to_restore = aCanvas.isAutoUpdate();
    try {
    aCanvas.setAutoUpdate( false );
    TLcdGLDirectionalLight directionalLight = new TLcdGLDirectionalLight(ALcdGLLight.LightType.STATIONARY);
    directionalLight.setDirection( new float[] { 0, 1, -1 } );
    directionalLight.setDiffuseColor( Color.white );
    directionalLight.setEnabled( true );
    aCanvas.setLightingEnabled( true );
    aCanvas.getLights().clear();
    aCanvas.getLights().add(directionalLight);
    aCanvas.setAutoUpdate( true );
    } finally {
      aCanvas.setAutoUpdate(auto_update_to_restore);
    }
    aCanvas.invalidate( true, MainPanel.class, "New light" );
  }

  private static void createViewpointDirectionalLight( ILcdGLView aCanvas ) {
    boolean auto_update_to_restore = aCanvas.isAutoUpdate();
    try {
    aCanvas.setAutoUpdate( false );
    TLcdGLDirectionalLight directionalLight = new TLcdGLDirectionalLight( ALcdGLLight.LightType.VIEWPOINT);
    directionalLight.setDirection( new float[] { 0, 0, -1 } );
    directionalLight.setEnabled( true );
    directionalLight.setDiffuseColor( Color.white );
    aCanvas.setLightingEnabled( true );
    aCanvas.getLights().clear();
    aCanvas.getLights().add(directionalLight);
    aCanvas.setAutoUpdate( true );
    } finally {
      aCanvas.setAutoUpdate(auto_update_to_restore);
    }
    aCanvas.invalidate( true, MainPanel.class, "New light" );
  }

  private static void createStationaryPositionalLight( ILcdGLView aCanvas ) {
    boolean auto_update_to_restore = aCanvas.isAutoUpdate();
    try {
    aCanvas.setAutoUpdate( false );
    TLcdGLPositionalLight positionalLight = new TLcdGLPositionalLight(ALcdGLLight.LightType.STATIONARY);
    positionalLight.setPosition( new float[] { 0, -30000, 30000 } );
    positionalLight.setEnabled( true );
    positionalLight.setDiffuseColor( Color.white );
    aCanvas.setLightingEnabled( true );
    aCanvas.getLights().clear();
    aCanvas.getLights().add(positionalLight);
    aCanvas.setAutoUpdate( true );
    } finally {
      aCanvas.setAutoUpdate(auto_update_to_restore);
    }
    aCanvas.invalidate( true, MainPanel.class, "New light" );
  }

  private static void createViewpointPositionalLight( ILcdGLView aCanvas ) {
    boolean auto_update_to_restore = aCanvas.isAutoUpdate();
    try {
    aCanvas.setAutoUpdate( false );
    TLcdGLPositionalLight positionalLight = new TLcdGLPositionalLight(ALcdGLLight.LightType.VIEWPOINT);
    positionalLight.setPosition( new float[] { 0, 0, 0 } );
    positionalLight.setEnabled( true );
    positionalLight.setDiffuseColor( Color.white );
    aCanvas.setLightingEnabled( true );
    aCanvas.getLights().clear();
    aCanvas.getLights().add(positionalLight);
    aCanvas.setAutoUpdate( true );
    } finally {
      aCanvas.setAutoUpdate(auto_update_to_restore);
    }
    aCanvas.invalidate( true, MainPanel.class, "New light" );
  }

  private JPanel createLightControlPanel( final ILcdGLView aGLView ) {
    JRadioButton stationarySpotLightButton = new JRadioButton( "Spot" );
    stationarySpotLightButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        if ( ( (JRadioButton) e.getSource() ).isSelected() )
          createStationarySpotLight( aGLView );
      }
    } );
    JRadioButton viewpointSpotLightButton = new JRadioButton( "Spot" );
    viewpointSpotLightButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        if ( ( (JRadioButton) e.getSource() ).isSelected() )
          createViewpointSpotLight( aGLView );
      }
    } );

    JRadioButton stationaryDirectionalLightButton = new JRadioButton( "Directional" );
    stationaryDirectionalLightButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        if ( ( (JRadioButton) e.getSource() ).isSelected() )
          createStationaryDirectionalLight( aGLView );
      }
    } );
    JRadioButton viewpointDirectionalLightButton = new JRadioButton( "Directional" );
    viewpointDirectionalLightButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        if ( ( (JRadioButton) e.getSource() ).isSelected() )
          createViewpointDirectionalLight( aGLView );
      }
    } );

    JRadioButton stationaryPositionalLightButton = new JRadioButton( "Positional" );
    stationaryPositionalLightButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        if ( ( (JRadioButton) e.getSource() ).isSelected() )
          createStationaryPositionalLight( aGLView );
      }
    } );
    JRadioButton viewpointPositionalLightButton = new JRadioButton( "Positional" );
    viewpointPositionalLightButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        if ( ( (JRadioButton) e.getSource() ).isSelected() )
          createViewpointPositionalLight( aGLView );
      }
    } );

    ButtonGroup buttons = new ButtonGroup();
    buttons.add( viewpointSpotLightButton );
    buttons.add( stationarySpotLightButton );
    buttons.add( viewpointDirectionalLightButton );
    buttons.add( stationaryDirectionalLightButton );
    buttons.add( viewpointPositionalLightButton );
    buttons.add( stationaryPositionalLightButton );

    JPanel stationaryPanel = new JPanel();
    stationaryPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    stationaryPanel.setLayout( new BoxLayout( stationaryPanel, BoxLayout.X_AXIS ) );
    stationaryPanel.add( stationarySpotLightButton );
    stationaryPanel.add( stationaryDirectionalLightButton );
    stationaryPanel.add( stationaryPositionalLightButton );

    JPanel viewpointPanel = new JPanel();
    viewpointPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    viewpointPanel.setLayout( new BoxLayout( viewpointPanel, BoxLayout.X_AXIS ) );
    viewpointPanel.add( viewpointSpotLightButton );
    viewpointPanel.add( viewpointDirectionalLightButton );
    viewpointPanel.add( viewpointPositionalLightButton );

    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
    panel.add( TitledPanel.createTitledPanel( "Stationary", stationaryPanel, TitledPanel.NORTH | TitledPanel.EAST ) );
    panel.add( TitledPanel.createTitledPanel( "Viewpoint", viewpointPanel, TitledPanel.NORTH ) );
    panel.add( Box.createHorizontalGlue() );

    stationarySpotLightButton.doClick();

    return panel;
  }
}
