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
package samples.decoder.aixm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.format.aixm.decoder.*;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * This class can be used to decode AIXM snapshot files. It uses internally a
 * <code>TLcdAIXMModelDecoder</code> instance for the decoding process.
 * When calling <code>decode(String)</code> , a dialog
 * will show-up where the user can select which domain objects should be decoded.
 * The corresponding <code>ILcdAIXMHandler</code> instances will then be registered
 * on the <code>TLcdAIXMModelDecoder</code> object.
 */
public class ConfigurableAIXMSnapshotDecoder implements ILcdModelDecoder, ILcdInputStreamFactoryCapable {

private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ConfigurableAIXMSnapshotDecoder.class.getName());

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
          "Procedure",
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
          new TLcdAIXMGeoborderHandler(),
          new TLcdAIXMProcedureHandler(),
  };
  private TLcdAIXMModelDecoder fDecoder = new TLcdAIXMModelDecoder();
  private boolean fHandlerToggles[] = new boolean[fHandlerNames.length];
  private JCheckBox fHandlerCheckBoxes[] = new JCheckBox[fHandlerNames.length];
  private Dimension fPreferredDimension = new Dimension( 227, 404 );

  public ConfigurableAIXMSnapshotDecoder() {
    this( null );
  }

  /**
   * Creates a new <code>ConfigurableAIXMSnapshotDecoder</code> with the given <code>Frame</code>
   * instance as owner.
   */
  public ConfigurableAIXMSnapshotDecoder( Frame aOwner ) {
    fOwner = aOwner;

  }

  public String getDisplayName() {
    return "AIXM Snapshot Decoder";
  }

  public boolean canDecodeSource( String aSourceName ) {
    return fDecoder.canDecodeSource( aSourceName );
  }

  public ILcdModel decode( String aSource ) throws IOException {
    // Show a dialog in which the user can select the desired domain objects.
    configure();

    ILcdModel model = null;
    if ( getSelectedHandlerCount() > 0 ) {
      // Decode.
      model = fDecoder.decode( aSource );
    }

    return model;
  }

  public void setInputStreamFactory( ILcdInputStreamFactory aInputStreamFactory ) {
    fDecoder.setInputStreamFactory( aInputStreamFactory );
  }

  public ILcdInputStreamFactory getInputStreamFactory() {
    return fDecoder.getInputStreamFactory();
  }

  private void configure() {
    // Remove all the ILcdAIXMHandler instances that are currently registered on the decoder.
    fDecoder.removeAllHandlersForTypeToBeDecoded();

    resetHandlerToggles();

    // Let the user choose which domain objects should be decoded.
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

  private void createDialog() {
    Frame owner = fOwner;

    JPanel config_panel = new JPanel( new BorderLayout( 10, 10 ) );
    config_panel.add( new JLabel( "Choose AIXM objects to decode:   " ), BorderLayout.NORTH );

    fOkButton = new JButton( "OK" );
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

    JPanel handler_toggle_panel = createHandlerTogglePanel();
    config_panel.add( handler_toggle_panel, BorderLayout.CENTER );
    config_panel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ),
            BorderFactory.createCompoundBorder( BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ) ) );
    fDialog = new JDialog( owner, "Open AIXM snapshot file", true );
    fDialog.getContentPane().add( config_panel );
    fDialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
    fDialog.addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) {
        resetHandlerToggles();
      }
    } );
    fDialog.pack();
    fDialog.addComponentListener( new ComponentAdapter() {
      public void componentResized( ComponentEvent e ) {
        if ( fDialog.getWidth() < fPreferredDimension.getWidth() || fDialog.getHeight() < fPreferredDimension.getHeight() )
        {
          fDialog.setSize( new Dimension( Math.max( fDialog.getWidth(), (int) fPreferredDimension.getWidth() ), Math.max( fDialog.getHeight(), (int) fPreferredDimension.getHeight() ) ) );
        }
      }
    } );
  }

  private JPanel createHandlerTogglePanel() {
    ItemListener listener = new ItemListener() {
      public void itemStateChanged( ItemEvent e ) {
        CheckBoxWithIndex check_box = (CheckBoxWithIndex) e.getSource();
        fHandlerToggles[ check_box.getIndex() ] = ( e.getStateChange() == ItemEvent.SELECTED );
        fOkButton.setEnabled( getSelectedHandlerCount() > 0 );
      }
    };

    JPanel panel = new JPanel( new GridLayout( fHandlerNames.length, 1 ) );
    for ( int toggle_index = 0; toggle_index < fHandlerNames.length ; toggle_index++ ) {
      String name = fHandlerNames[ toggle_index ];
      JCheckBox check_box = new CheckBoxWithIndex( name, fHandlerToggles[ toggle_index ], toggle_index );
      fHandlerCheckBoxes[ toggle_index ] = check_box;
      check_box.addItemListener( listener );
      panel.add( check_box );
    }
    return panel;
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
    if ( fDialog == null )
      createDialog();
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

}
