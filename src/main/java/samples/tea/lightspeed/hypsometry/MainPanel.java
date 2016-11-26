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
package samples.tea.lightspeed.hypsometry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.gui.ILcdAction;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.tea.lightspeed.hypsometry.*;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.AngleControlComponent;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.decoder.raster.MixedRasterLayerFactory;

/**
 * This sample demonstrates how to visualize hypsometric computations on elevation data in
 * LuciadLightspeed. Hypsometric shading is applied layer by creating and adding an
 * {@code ILspHypsometricShadingLayer}. Such layers can be created using
 * {@code TLspHypsometricShadingLayerBuilder} and can be further configured with various types of
 * {@code ALspHypsometricShader}. The shading can be applied to either the view's terrain or a
 * specific elevation model.
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  private final static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( MainPanel.class.getName() );

  private ShaderControls fShaderControls;

  /**
   * Display names for the various hypsometric shaders.
   */
  private static final String[] HYPSOMETRY_NAMES = {
      "Ridges & Valleys",
      "Shading",
      "Orientation",
      "Slope",
      "Azimuth",
  };

  /**
   * Instances of the various hypsometric shaders.
   */
  private static final ALspHypsometricShader[] HYPSOMETRY_SHADERS = {
      TLspHypsometricCreaseShader.newBuilder().build(),
      TLspHypsometricHillShadingShader.newBuilder().build(),
      TLspHypsometricOrientationShader.newBuilder().build(),
      TLspHypsometricSlopeAngleShader.newBuilder().build(),
      TLspHypsometricOrientationAngleShader.newBuilder().build()
  };

  /**
   * Color models for the various hypsometric shaders.
   */
  private static final IndexColorModel[] HYPSOMETRY_COLORMODELS = {
      HypsometryColorModelFactory.createRidgeValleyColorModel(),
      HypsometryColorModelFactory.createShadingColorModel(),
      HypsometryColorModelFactory.createOrientationColorModel(),
      HypsometryColorModelFactory.createSlopeAngleColorModel(),
      HypsometryColorModelFactory.createAzimuthColorModel()
  };

  /**
   * The layer that displays the hypsometric shading.
   */
  private ILspEditableStyledLayer fHypsometricLayer;

  public MainPanel(String[] aArgs) {
    super(aArgs);
  }

  @Override
  protected ILcdAction createSaveAction() {
    //Disable saving in this sample
    return null;
  }

  @Override
  protected ILspAWTView createView() {
    ILspAWTView view = super.createView( ILspView.ViewType.VIEW_3D );
    TLspViewXYZWorldTransformation3D w2v = ( TLspViewXYZWorldTransformation3D ) view
        .getViewXYZWorldTransformation();

    TLcdXYZPoint lookAt = new TLcdXYZPoint();
    TLcdEllipsoid.DEFAULT.llh2geocSFCT( new TLcdLonLatHeightPoint( -122.52, 37.85, 0 ), lookAt );
    w2v.lookAt( lookAt, 4e3, 135, -40, 0 );
    
    view.addLayeredListener(new LayeredListener());

    return view;
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    // Add GUI elements to configure the hypsometric shading.
    JPanel hypsometryDataPanel = createHypsometryDataPanel();
    JPanel hypsometryShaderPanel = createHypsometryShaderPanel();
    JPanel hypsometricPanel = new JPanel(new BorderLayout());
    hypsometricPanel.add(hypsometryDataPanel, BorderLayout.NORTH);
    hypsometricPanel.add(hypsometryShaderPanel, BorderLayout.CENTER);
    addComponentToRightPanel(hypsometricPanel);
  }

  @Override
  protected ToolBar[] createToolBars( ILspAWTView aView) {
    ToolBar regularToolBar = new ToolBar( aView, this, true, true );
    return new ToolBar[]{regularToolBar };
  }

  protected void addData() throws IOException {
    super.addData();
    // Create and add the hypsometric shading layer.
    updateHypsometricLayer(null);
  }

  /**
   * Updates the hypsometric shading layer to use the specified layer as elevation source.
   *
   * @param aLayer the elevation source or {@code null} to use all layers in the view
   */
  private void updateHypsometricLayer(ILspLayer aLayer) {
    // Create a new layer based on the current settings
    ALspHypsometricShader shader = HYPSOMETRY_SHADERS[ 0 ];
    IndexColorModel colorModel = HYPSOMETRY_COLORMODELS[ 0 ];
    TLspHypsometricShadingStyle style = getHypsometricShadingStyle();
    if(style != null) {
      shader = style.getShader();
      colorModel = style.getColorModel();
    }
    ILspEditableStyledLayer newHypsometricLayer = createHypsometricShadingLayer(
        aLayer != null ? aLayer.getModel() : null, shader, colorModel
    );

    // Swap the layer
    if(fHypsometricLayer != null) {
      getView().removeLayer(fHypsometricLayer);
    }
    fHypsometricLayer = newHypsometricLayer;
    getView().addLayer( fHypsometricLayer );

    updateShaderControls( shader );
  }

  private TLspHypsometricShadingStyle getHypsometricShadingStyle() {
    if(fHypsometricLayer == null) {
      return null;
    }
    else {
      return getHypsometricShadingStyle(
          fHypsometricLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY)
      );
    }
  }

  private void setHypsometricShadingStyle(TLspHypsometricShadingStyle aStyle) {
    fHypsometricLayer.setStyler(
        TLspPaintRepresentationState.REGULAR_BODY,
        aStyle
    );
  }

  private TLspHypsometricShadingStyle getHypsometricShadingStyle(ILspStyler aStyler) {
    if(aStyler instanceof TLspHypsometricShadingStyle) {
      return (TLspHypsometricShadingStyle) aStyler;
    }
    else {
      return null;
    }
  }

  private ILspEditableStyledLayer createHypsometricShadingLayer(ILcdModel aElevationModel, ALspHypsometricShader aShader, IndexColorModel aColorModel) {
    TLspHypsometricShadingLayerBuilder builder =
        TLspHypsometricShadingLayerBuilder.newBuilder().
            styler(TLspPaintRepresentationState.REGULAR_BODY, TLspHypsometricShadingStyle.newBuilder().shader(aShader).colorModel(aColorModel).build()).
            label("Hypsometry");
    if(aElevationModel == null) {
      // Use the view's terrain as the elevation data source
      builder.elevationFromView();
    }
    else {
      // Use a specific model as the elevation data source
      builder.elevationFromModel(aElevationModel);
    }
    return builder.build();
  }

  /**
   * Creates GUI components for configuring the data that is used for hypsometric shading.
   *
   * @return a panel that can be added to the sample's frame
   */
  private JPanel createHypsometryDataPanel() {
    // Create a panel with a combo box to choose the data source
    JPanel panel = new JPanel();
    JComboBox dataComboBox = new JComboBox();
    final DefaultComboBoxModel model = new DefaultComboBoxModel();
    model.addElement(new HypsometricDataSource(null));
    dataComboBox.setModel(model);
    dataComboBox.setSelectedIndex(0);
    panel.add(dataComboBox);

    // Add all elevation layer to the combo box
    getView().addLayeredListener(new ILcdLayeredListener() {
      @Override
      public void layeredStateChanged(TLcdLayeredEvent e) {
        ILspLayer layer = (ILspLayer) e.getLayer();
        if(e.getID() == TLcdLayeredEvent.LAYER_ADDED) {
          if(layer.getModel() != null && layer != fHypsometricLayer && MixedRasterLayerFactory.containsElevationData(layer.getModel())) {
            model.addElement(new HypsometricDataSource(layer));
          }
        }
        else if(e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
          model.removeElement(new HypsometricDataSource(layer));
        }
      }
    });

    // Update the hypsometry layer when selecting a different data source
    dataComboBox.addItemListener( new ItemListener() {
      @Override
      public void itemStateChanged( ItemEvent e ) {
        if ( e.getStateChange() == ItemEvent.SELECTED ) {
          updateHypsometricLayer( ( ( HypsometricDataSource ) e.getItem() ).getLayer() );
        }
      }
    } );

    return TitledPanel.createTitledPanel("Hypsometry Data", panel);
  }

  /**
   * Creates GUI components for configuring the hypsometric shader.
   *
   * @return a panel that can be added to the sample's frame
   */
  private JPanel createHypsometryShaderPanel() {
    // Create buttons to choose between different hypsometric shaders.
    ButtonGroup group = new ButtonGroup();
    JPanel panel = new JPanel( new BorderLayout() );
    // Each button should apply a different shader/color model combination to the
    // hypsometric layer.
    ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent e ) {
        String name = e.getActionCommand();
        int index = 0;
        while ( !HYPSOMETRY_NAMES[ index ].equals( name ) ) {
          index++;
        }
        ALspHypsometricShader shader = HYPSOMETRY_SHADERS[index];
        setHypsometricShadingStyle(
            TLspHypsometricShadingStyle.newBuilder().
                shader(shader).
                colorModel(HYPSOMETRY_COLORMODELS[ index ]).
                build()
        );

        updateShaderControls(shader);

        getView().invalidate( true, this, "Hypsometry shader change" );
      }
    };

    JPanel radioGroupButtonPanel = new JPanel( new GridLayout(HYPSOMETRY_NAMES.length, 1));

    boolean first = true;
    for ( String name : HYPSOMETRY_NAMES ) {
      JRadioButton button = new JRadioButton( name );
      button.setActionCommand( name );
      button.addActionListener( listener );
      group.add( button );
      radioGroupButtonPanel.add( button );

      if ( first ) {
        button.setSelected( true );
        first = false;
      }
    }

    panel.add(radioGroupButtonPanel, BorderLayout.NORTH);

    // Also add the angle controls that set the reference direction for shaders which support it
    fShaderControls = new ShaderControls();
    JPanel controlPanel = new JPanel();
    controlPanel.add( fShaderControls.getYawControl() );
    controlPanel.add( fShaderControls.getPitchControl() );

    panel.add( controlPanel , BorderLayout.CENTER);


    // Wrap the components in a titled panel and return.
    return TitledPanel.createTitledPanel( "Hypsometry Shader", panel );
  }

  private void updateShaderControls(ALspHypsometricShader shader) {
    // Enable/disable controls
    if ( shader instanceof ALspHypsometric2DDirectionShader ) {
      ALspHypsometric2DDirectionShader directionShader = ( ALspHypsometric2DDirectionShader ) shader;
      fShaderControls.setYawEnabled( true );
      fShaderControls.setPitchEnabled( false );
      fShaderControls.setReferenceDirection( directionShader.getReferenceDirectionX(), directionShader.getReferenceDirectionY());

    }
    else if ( shader instanceof ALspHypsometric3DDirectionShader ) {
      ALspHypsometric3DDirectionShader directionShader = ( ALspHypsometric3DDirectionShader ) shader;
      fShaderControls.setYawEnabled( true );
      fShaderControls.setPitchEnabled( true );
      fShaderControls.setReferenceDirection( directionShader.getReferenceDirectionX(), directionShader.getReferenceDirectionY(), directionShader.getReferenceDirectionZ());
    }
    else {
      fShaderControls.setYawEnabled( false );
      fShaderControls.setPitchEnabled( false );
    }
  }

  /**
   * Ensures all raster layers are inserted below the hypsometry layer.
   */
  private class LayeredListener implements ILcdLayeredListener {
    @Override
    public void layeredStateChanged(TLcdLayeredEvent e) {
      if(e.getID() == TLcdLayeredEvent.LAYER_ADDED) {
        ILcdLayer layer = e.getLayer();
        if(layer instanceof TLspRasterLayer && fHypsometricLayer != null) {
          try {
            int hypsometryLayerIndex = e.getLayered().indexOf(fHypsometricLayer);
            if(e.getNewIndex() > hypsometryLayerIndex) {
              e.getLayered().moveLayerAt(hypsometryLayerIndex, e.getLayer());
            }
          } catch(NoSuchElementException ignored) {
          }
        }
      }
    }
  }

  /**
   * Manages the reference direction controls.
   */
  private class ShaderControls implements ChangeListener {

    // GUI components that control shader properties
    private AngleControlComponent fYawControl;
    private AngleControlComponent fPitchControl;

    private ShaderControls() {
      fYawControl = new AngleControlComponent( AngleControlComponent.Type.FULL, 0, 360 ) {
        @Override
        public Dimension getPreferredSize() {
          return new Dimension( 80, 80 );
        }
      };
      fYawControl.addChangeListener( this );
      fPitchControl = new AngleControlComponent( AngleControlComponent.Type.SEGMENT, 0, 90 ) {
        @Override
        public Dimension getPreferredSize() {
          return new Dimension( 60, 60 );
        }
      };
      fPitchControl.addChangeListener( this );
    }

    public AngleControlComponent getYawControl() {
      return fYawControl;
    }

    public AngleControlComponent getPitchControl() {
      return fPitchControl;
    }

    public void setYawEnabled(boolean aEnabled) {
      fYawControl.setEnabled( aEnabled );
    }

    public void setPitchEnabled(boolean aEnabled) {
      fPitchControl.setEnabled( aEnabled );
    }

    public void setReferenceDirection(double aX, double aY) {
      double yaw = 90-Math.toDegrees( Math.atan2( aY, aX ) );

      while (yaw<0) {
        yaw+=360;
      }
      while (yaw>360) {
        yaw-=360;
      }
      fYawControl.setAngle( yaw );
    }
    public void setReferenceDirection(double aX, double aY, double aZ) {
      setReferenceDirection( aX, aY );
      double pitch = 90-Math.toDegrees( Math.acos( aZ ) );
      while (pitch<0) {
        pitch+=360;
      }

      while(pitch>360) {
        pitch-=360;
      }

      if (pitch>90) {
        pitch = 90;
      }

      fPitchControl.setAngle( pitch );
    }

    @Override
    public void stateChanged( ChangeEvent e ) {
      // Convert yaw and pitch to radians
      double yaw = Math.toRadians( 90 - fYawControl.getAngle() );
      double pitch = Math.toRadians( 90 - fPitchControl.getAngle() );

      // Apply the reference direction to the shaders that use it
      TLspHypsometricShadingStyle style = getHypsometricShadingStyle();
      ALspHypsometricShader shader = style.getShader();
      if ( shader instanceof ALspHypsometric2DDirectionShader ) {
        // Convert spherical to cartesian coordinates
        double x = Math.cos( yaw );
        double y = Math.sin( yaw );

        ALspHypsometric2DDirectionShader directionShader = ( ALspHypsometric2DDirectionShader ) shader;
        shader = directionShader.asBuilder().referenceDirection(x, y).build();
      }
      else if (shader instanceof ALspHypsometric3DDirectionShader ) {
        // Convert spherical to cartesian coordinates
        double x = Math.sin( pitch ) * Math.cos( yaw );
        double y = Math.sin( pitch ) * Math.sin( yaw );
        double z = Math.cos( pitch );

        ALspHypsometric3DDirectionShader directionShader = ( ALspHypsometric3DDirectionShader ) shader;
        shader = directionShader.asBuilder().referenceDirection(x, y, z).build();
      }
      setHypsometricShadingStyle(style.asBuilder().shader(shader).build());
      getView().invalidate( true, this, "Orientation change" );
    }
  }

  /**
   * An elevation data source for the hypsometric computations.
   */
  private static class HypsometricDataSource {
    private final ILspLayer fLayer;

    public HypsometricDataSource(ILspLayer aLayer) {
      fLayer = aLayer;
}

    public ILspLayer getLayer() {
      return fLayer;
    }

    @Override
    public String toString() {
      return fLayer != null ? fLayer.getLabel() : "All layers";
    }

    @Override
    public boolean equals(Object o) {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;

      HypsometricDataSource that = (HypsometricDataSource) o;

      if(fLayer != null ? !fLayer.equals(that.fLayer) : that.fLayer != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return fLayer != null ? fLayer.hashCode() : 0;
    }
  }

  public static void main( final String[] args ) {
    startSample(MainPanel.class, args, "Hypsometric shading");
  }
}
