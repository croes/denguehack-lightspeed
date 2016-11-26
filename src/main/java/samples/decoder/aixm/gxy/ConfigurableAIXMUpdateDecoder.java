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
package samples.decoder.aixm.gxy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import com.luciad.format.aixm.decoder.*;
import com.luciad.format.aixm.model.aerodrome.TLcdAIXMAerodromeModelDescriptor;
import com.luciad.format.aixm.model.aerodrome.TLcdAIXMRunwayModelDescriptor;
import com.luciad.format.aixm.model.airspace.TLcdAIXMAirspaceModelDescriptor;
import com.luciad.format.aixm.model.geoborder.TLcdAIXMGeoborderModelDescriptor;
import com.luciad.format.aixm.model.navaid.TLcdAIXMDMEModelDescriptor;
import com.luciad.format.aixm.model.navaid.TLcdAIXMDesignatedPointModelDescriptor;
import com.luciad.format.aixm.model.navaid.TLcdAIXMILSModelDescriptor;
import com.luciad.format.aixm.model.navaid.TLcdAIXMMarkerModelDescriptor;
import com.luciad.format.aixm.model.navaid.TLcdAIXMNDBModelDescriptor;
import com.luciad.format.aixm.model.navaid.TLcdAIXMTACANModelDescriptor;
import com.luciad.format.aixm.model.navaid.TLcdAIXMVORModelDescriptor;
import com.luciad.format.aixm.model.obstacle.TLcdAIXMObstacleModelDescriptor;
import com.luciad.format.aixm.model.route.TLcdAIXMRouteModelDescriptor;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcdModelList;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;

/**
 * This class can be used to decode AIXM update files. It uses internally a
 * <code>TLcdAIXMModelDecoder</code> instance for the decoding process.
 * When calling <code>update(String, ILcdModel)</code> , a dialog
 * will show-up where the user can select which models should be updated.
 * The corresponding <code>ILcdAIXMHandler</code> instances will then be registered
 * on the <code>TLcdAIXMModelDecoder</code> object.
 */
class ConfigurableAIXMUpdateDecoder implements ILcdInputStreamFactoryCapable {

private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ConfigurableAIXMUpdateDecoder.class.getName());

  private Frame fOwner;
  private JDialog fDialog;
  private JButton fOkButton;

  /**
   * Array with names to display for each handler. Order must
   * correspond to the ILcdAIXMHandler array fHandlers.
   */
  private String fHandlerNames[] = {
          "Aerodrome",
          "DME Navaid",
          "VOR Navaid",
          "NDB Navaid",
          "TACAN Navaid",
          "ILS Navaid",
          "Marker Navaid",
          "Designated Points",
          "Obstacles",
          "Runways",
          "Route",
          "Airspace",
          "Geoborder",
  };
  private ILcdAIXMHandler[] fHandlers = new ILcdAIXMHandler[] {
          new TLcdAIXMAerodromeHandler(),
          new TLcdAIXMDMEHandler(),
          new TLcdAIXMVORHandler(),
          new TLcdAIXMNDBHandler(),
          new TLcdAIXMTACANHandler(),
          new TLcdAIXMILSHandler(),
          new TLcdAIXMMarkerHandler(),
          new TLcdAIXMDesignatedPointHandler(),
          new TLcdAIXMObstacleHandler(),
          new TLcdAIXMRunwayHandler(),
          new TLcdAIXMRouteHandler(),
          new TLcdAIXMAirspaceHandler(),
          new TLcdAIXMGeoborderHandler()
  };
  /**
   * Array with the available AIXM model descriptor classes. Order must
   * correspond to the ILcdAIXMHandler array fHandlers.
   */
  private Class<? extends ILcdModelDescriptor>[] fModelDescriptors = new Class[] {
          TLcdAIXMAerodromeModelDescriptor.class,
          TLcdAIXMDMEModelDescriptor.class,
          TLcdAIXMVORModelDescriptor.class,
          TLcdAIXMNDBModelDescriptor.class,
          TLcdAIXMTACANModelDescriptor.class,
          TLcdAIXMILSModelDescriptor.class,
          TLcdAIXMMarkerModelDescriptor.class,
          TLcdAIXMDesignatedPointModelDescriptor.class,
          TLcdAIXMObstacleModelDescriptor.class,
          TLcdAIXMRunwayModelDescriptor.class,
          TLcdAIXMRouteModelDescriptor.class,
          TLcdAIXMAirspaceModelDescriptor.class,
          TLcdAIXMGeoborderModelDescriptor.class
  };
  private TLcdAIXMModelDecoder fDecoder = new TLcdAIXMModelDecoder();
  private boolean fHandlerToggles[] = new boolean[fHandlerNames.length];
  private JCheckBox fHandlerCheckBoxes[] = new JCheckBox[fHandlerNames.length];
  private JComboBox fHandlerComboBoxes[] = new JComboBox[fHandlerNames.length];
  private LayerCellRenderer fLayerCellRenderer = new LayerCellRenderer();
  private Dimension fPreferredDimension = new Dimension( 227, 404 );

  /**
   * Creates a new <code>ConfigurableAIXMUpdateDecoder</code> with the given <code>Frame</code>
   * instance as owner.
   */
  public ConfigurableAIXMUpdateDecoder( Frame aOwner ) {
    fOwner = aOwner;

  }

  public String getDisplayName() {
    return "AIXM Update Decoder";
  }

  public boolean canUpdateFromSource( String aSourceName ) {
    return fDecoder.canUpdateFromSource( aSourceName );
  }

  public ILcdModel updateSFCT( String aSourceName, ILcdGXYLayer[] aLayerArray ) throws IOException {
    // Show a dialog in which the user can select the models that need to be updated.
    configure( aLayerArray );

    TLcdModelList model_list = null;

    if ( getSelectedHandlerCount() > 0 ) {
      model_list = new TLcdModelList();
      for ( int index = 0; index < fHandlers.length ; index++ ) {
        if ( fHandlerToggles[ index ] ) {
          JComboBox combo_box = fHandlerComboBoxes[ index ];
          model_list.addModel( ( (ILcdLayer) combo_box.getSelectedItem() ).getModel() );
        }
      }

      // Update.
      fDecoder.updateSFCT( aSourceName, model_list );
    }

    return model_list;
  }

  public void setInputStreamFactory( ILcdInputStreamFactory aInputStreamFactory ) {
    fDecoder.setInputStreamFactory( aInputStreamFactory );
  }

  public ILcdInputStreamFactory getInputStreamFactory() {
    return fDecoder.getInputStreamFactory();
  }

  private void configure( ILcdGXYLayer[] aLayerArray ) {
    // Remove all the ILcdAIXMHandler instances that are currently registered on the decoder.
    fDecoder.removeAllHandlersForTypeToBeDecoded();

    resetHandlerToggles();

    // Let the user choose which domain objects should be decoded.
    createDialog( aLayerArray );
    showDialog();

    // Register the necessary ILcdAIXMHandler instances on the decoder.
    for ( int index = 0; index < fHandlers.length ; index++ ) {
      if ( fHandlerToggles[ index ] ) {
        ILcdAIXMHandler handler = fHandlers[ index ];
        fDecoder.addHandlerForTypeToBeDecoded( handler );
      }
    }

    // Create a bounds to filter on.
    TLcdLonLatBounds bounds_to_filter_on = new TLcdLonLatBounds( -180, -90, 360, 180 ); //default value
    fDecoder.setBoundsToFilterOn( bounds_to_filter_on );
  }

  private void resetHandlerToggles() {
    for ( int i = 0; i < fHandlerToggles.length ; i++ ) {
      fHandlerToggles[ i ] = false;
      if ( fHandlerCheckBoxes[ i ] != null )
        fHandlerCheckBoxes[ i ].setSelected( false );
    }
  }

  private void createDialog( ILcdGXYLayer[] aLayerArray ) {
    Frame owner = fOwner;

    JPanel config_panel = new JPanel( new BorderLayout( 10, 10 ) );
    config_panel.add( new JLabel( "Choose AIXM layers to update:   " ), BorderLayout.NORTH );

    fOkButton = new JButton( "Ok" );
    fOkButton.setEnabled( false );
    fOkButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        fDialog.setVisible( false );
      }
    } );
    Box south_panel = new Box( BoxLayout.X_AXIS );
    south_panel.add( Box.createHorizontalGlue() );
    south_panel.add( fOkButton );
    south_panel.add( Box.createHorizontalGlue() );

    config_panel.add( south_panel, BorderLayout.SOUTH );

    JPanel handler_toggle_panel = createHandlerTogglePanel( aLayerArray );
    config_panel.add( handler_toggle_panel, BorderLayout.CENTER );
    config_panel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ),
            BorderFactory.createCompoundBorder( BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ) ) );
    //config_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    fDialog = new JDialog( owner, "Open AIXM update file", true );
    fDialog.getContentPane().add( config_panel );
    fDialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
    fDialog.addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) {
        resetHandlerToggles();
      }
    } );
    fDialog.pack();
    fPreferredDimension = fDialog.getSize();
    fDialog.addComponentListener( new ComponentAdapter() {
      public void componentResized( ComponentEvent e ) {
        if ( fDialog.getWidth() < fPreferredDimension.getWidth() || fDialog.getHeight() < fPreferredDimension.getHeight() )
        {
          fDialog.setSize( new Dimension( Math.max( fDialog.getWidth(), (int) fPreferredDimension.getWidth() ), Math.max( fDialog.getHeight(), (int) fPreferredDimension.getHeight() ) ) );
        }
      }
    } );
  }

  private JPanel createHandlerTogglePanel( ILcdGXYLayer[] aLayerArray ) {
    JPanel panel = new JPanel( new GridBagLayout() );
    GridBagConstraints gb_constraints = new GridBagConstraints(
            0, 0, 1, 1, 1, 1,
            GridBagConstraints.WEST,
            GridBagConstraints.BOTH,
            new Insets( 0, 0, 0, 10 ), 0, 0 );

    ItemListener listener = new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        CheckBoxWithIndex check_box = (CheckBoxWithIndex) e.getSource();
        fHandlerToggles[ check_box.getIndex() ] = ( e.getStateChange() == ItemEvent.SELECTED );
        fHandlerComboBoxes[ check_box.getIndex() ].setEnabled( ( e.getStateChange() == ItemEvent.SELECTED ) );
        fOkButton.setEnabled( getSelectedHandlerCount() > 0 );
      }
    };

    // Add top label for the two columns.
    JLabel type_label = new JLabel( "Type" );
    panel.add( type_label, gb_constraints );
    gb_constraints.gridx = 1;
    JLabel model_label = new JLabel( "Layer" );
    panel.add( model_label, gb_constraints );

    for ( int toggle_index = 0; toggle_index < fHandlerNames.length ; toggle_index++ ) {
      java.util.List<ILcdGXYLayer> layers = retrieveLayersByType( aLayerArray, fModelDescriptors[toggle_index] );

      if ( layers.size() > 0 ) {
        // Create checkbox.
        String name = fHandlerNames[ toggle_index ];
        JCheckBox check_box = new CheckBoxWithIndex( name, fHandlerToggles[ toggle_index ], toggle_index );
        fHandlerCheckBoxes[ toggle_index ] = check_box;
        check_box.addItemListener( listener );

        gb_constraints.gridx = 0;
        gb_constraints.gridy++;
        gb_constraints.weightx = 0;
        gb_constraints.fill = GridBagConstraints.NONE;
        gb_constraints.insets = new Insets( 0, 0, 0, 10 );
        panel.add( check_box, gb_constraints );

        // Create combobox.
        JComboBox combo_box = new JComboBox( new Vector<ILcdGXYLayer>( layers ) );
        combo_box.setRenderer( fLayerCellRenderer );
        combo_box.setEnabled( false );
        fHandlerComboBoxes[ toggle_index ] = combo_box;

        gb_constraints.gridx = 1;
        gb_constraints.weightx = 1;
        gb_constraints.fill = GridBagConstraints.HORIZONTAL;
        gb_constraints.insets = new Insets( 0, 0, 0, 0 );
        panel.add( combo_box, gb_constraints );
      }
    }

    return panel;
  }

  private java.util.List<ILcdGXYLayer> retrieveLayersByType( ILcdGXYLayer[] aLayer, Class<? extends ILcdModelDescriptor> aModelDescriptor ) {
    java.util.List<ILcdGXYLayer> layers = new ArrayList<ILcdGXYLayer>();
    for ( int i = 0; i < aLayer.length ; i++ ) {
      if ( aLayer[ i ].getModel().getModelDescriptor().getClass().equals( aModelDescriptor ) ) {
        layers.add( aLayer[ i ] );
      }
    }

    return layers;
  }

  private int getSelectedHandlerCount() {
    int count = 0;

    for ( int i = 0; i < fHandlerToggles.length ; i++ ) {
      if ( fHandlerToggles[ i ] ) count++;
    }

    return count;
  }

  /**
   * Show the dialog on the AWT thread.
   */
  private void showDialog() {
    Runnable show = new Runnable() {
      public void run() {
        TLcdAWTUtil.centerWindow( fDialog );
        fDialog.setVisible( true );
      }
    };

    if ( SwingUtilities.isEventDispatchThread() ) {
      show.run();
    } else {
      try {
        SwingUtilities.invokeAndWait( show );
      } catch ( InterruptedException e ) {
        sLogger.error( "Could not show configuration dialog" );
        return;
      } catch ( InvocationTargetException e ) {
        sLogger.error("Could not show configuration dialog", e);
        return;
      }
    }
  }


  /**
   * A simple checkbox with index information (in a list or array).
   */
  private static class CheckBoxWithIndex extends JCheckBox {
    int fIndex;

    public CheckBoxWithIndex( String text, boolean selected, int aIndex ) {
      super( text, selected );
      fIndex = aIndex;
    }

    public int getIndex() {
      return fIndex;
    }

    public void setIndex( int aIndex ) {
      fIndex = aIndex;
    }
  }

  /**
   * A <code>ListCellRenderer</code> implementation for lists containing
   * <code>ILcdModel</code> objects. It displays the display name, followed
   * by the source name between brackets.
   */
  private static class LayerCellRenderer extends JLabel implements ListCellRenderer {
    public LayerCellRenderer() {
      setOpaque( true );
    }

    public Component getListCellRendererComponent( JList aList, Object aValue,
                                                   int aIndex, boolean aIsSelected,
                                                   boolean aHasFocus ) {
      setText( ( (ILcdGXYLayer) aValue ).getLabel() + " ( " + ( (ILcdGXYLayer) aValue ).getModel().getModelDescriptor().getSourceName() + " ) " );

      if ( aIsSelected ) {
        setBackground( aList.getSelectionBackground() );
        setForeground( aList.getSelectionForeground() );
      } else {
        setBackground( aList.getBackground() );
        setForeground( aList.getForeground() );
      }

      return this;
    }
  }
}
