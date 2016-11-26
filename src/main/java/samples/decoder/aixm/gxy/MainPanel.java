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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.io.TLcdStatusInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelList;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.decoder.aixm.ConfigurableAIXMSnapshotDecoder;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;




/**
 * This sample demonstrates how to load and display data in AIXM 3.3, 4.0 and 4.5 format.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds( 1.00, 49.75, 7.00, 2.00 );
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    // Add an action for opening AIXM data to the toolbar.
    AIXMDataChooserAction data_chooser = new AIXMDataChooserAction();
    getToolBars()[0].addAction(data_chooser);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    SwingUtilities.invokeLater( new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog( MainPanel.this, new String[] {
            "The LuciadLightspeed distribution does not contain AIXM data.",
            "Please use your own data to load into this sample."
        } );
      }
    } );
  }

  //inner classes

  /**
   * This action uses a JFileChooser to allow the user to open an AIXM data
   * file. After the user has selected a data file, a dialog will show up where
   * the user can choose which domain objects should be decoded. The data file
   * will then be decoded using a <code>TLcdAIXMModelDecoder</code> and the data
   * will be put on the mapJPanel.
   */
  private class AIXMDataChooserAction extends ALcdAction {

    private JFileChooser fFileChooser = new JFileChooser();

    private ILcdGXYLayerFactory fLayerFactory = new LayerFactory();
    private ConfigurableAIXMSnapshotDecoder fAIXMSnapshotDecoder;
    private ConfigurableAIXMUpdateDecoder   fAIXMUpdateDecoder;

    public AIXMDataChooserAction() {
      super("Open AIXM data file", TLcdIconFactory.create(TLcdIconFactory.OPEN_ICON));
      setShortDescription( "Open an AIXM data file" );
      fAIXMSnapshotDecoder = new ConfigurableAIXMSnapshotDecoder( TLcdAWTUtil.findParentFrame( this ) );
      fAIXMUpdateDecoder   = new ConfigurableAIXMUpdateDecoder  ( TLcdAWTUtil.findParentFrame( this ) );
    }

    public void actionPerformed( ActionEvent aEvent ) {
      // Set default directory
      fFileChooser.setSelectedFile( new File("") );

      if ( fFileChooser.showOpenDialog( getView() ) == JFileChooser.APPROVE_OPTION ) {
        final String source_path = fFileChooser.getSelectedFile().getAbsolutePath();

        // Check whether the source contains an AIXM snapshot.
        if ( fAIXMSnapshotDecoder.canDecodeSource( source_path ) ) {
          // Start a thread that decodes the AIXM file.
          Thread load_data_thread = new Thread( new Runnable() {
            public void run() {
              // Link the progress bar to the model decoder.
              TLcdStatusInputStreamFactory input_stream_factory = new TLcdStatusInputStreamFactory();
              input_stream_factory.addStatusEventListener(getStatusBar());
              fAIXMSnapshotDecoder.setInputStreamFactory( input_stream_factory );

              try {
                // Decode the data.
                ILcdModel model = fAIXMSnapshotDecoder.decode( source_path );

                // Create layers and add them to the view.
                GXYLayerUtil.addGXYLayer( getView(), fLayerFactory.createGXYLayer( model ) );
              }
              catch ( IOException ioe ) {
                System.out.println( "IOException while reading AIXM Data = " + ioe );
              }

              // Remove progress bar from model decoder
              input_stream_factory.removeStatusEventListener(getStatusBar());
            }
          } );
          load_data_thread.setPriority( Thread.MIN_PRIORITY );
          load_data_thread.start();
        }
        // Check whether the source contains an AIXM update.
        else if ( fAIXMUpdateDecoder.canUpdateFromSource( source_path ) ) {
          // An update file is selected, so now the user can configure the AIXM decoder
          // and set which models should be updated

          // Start a thread that decodes the AIXM file.
          Thread update_data_thread = new Thread( new Runnable() {
            public void run() {
              // Search AIXM layers.
              java.util.List<ILcdLayer> aixm_layers = new ArrayList<ILcdLayer>();
              for ( int i = 0; i < getView().layerCount() ; i++ ) {
                ILcdLayer layer = getView().getLayer( i );
                if ( layer.getModel().getModelDescriptor().getTypeName().equals( "AIXM" ) ) {
                  aixm_layers.add( layer );
                }
              }

              if ( aixm_layers.isEmpty() ) {
                JOptionPane.showMessageDialog(
                        MainPanel.this,
                        new String[] {
                                "There are currently no layers contain AIXM data.",
                                "Please load AIXM Snapshot data first."
                        }
                );
              } else {
                // Add a progress bar to the model decoder.
                TLcdStatusInputStreamFactory input_stream_factory = new TLcdStatusInputStreamFactory();
                input_stream_factory.addStatusEventListener(getStatusBar());
                fAIXMUpdateDecoder.setInputStreamFactory( input_stream_factory );

                try {
                  // Update the models.
                  final ILcdModel updated_models = fAIXMUpdateDecoder.updateSFCT( source_path, (ILcdGXYLayer[]) aixm_layers.toArray( new ILcdGXYLayer[aixm_layers.size()] ) );

                  if ( updated_models != null ) {
                    // Invalidate the layers of the updated models.
                    SwingUtilities.invokeLater( new Runnable() {
                      public void run() {
                        if ( updated_models instanceof TLcdModelList ) {
                          for ( int i = 0; i < ( (TLcdModelList) updated_models ).getModelCount() ; i++ ) {
                            getView().invalidateGXYLayer(
                                ( ILcdGXYLayer ) getView().layerOf( ( ( TLcdModelList ) updated_models ).getModel( i ) ),
                                true,
                                this,
                                "Model updated."
                            );
                          }
                        } else {
                          getView().invalidateGXYLayer(
                              ( ILcdGXYLayer ) getView().layerOf( updated_models ),
                              true,
                              this,
                              "Model updated."
                          );
                        }
                      }
                    } );
                  }
                }
                catch ( IOException ioe ) {
                  System.out.println( "IOException while reading AIXM Data = " + ioe );
                }

                // Remove progress bar from model decoder
                input_stream_factory.removeStatusEventListener(getStatusBar());
              }
            }
          } );
          update_data_thread.setPriority( Thread.MIN_PRIORITY );
          update_data_thread.start();
        } else {
          JOptionPane.showMessageDialog( getView(), "Invalid AIXM data file. Cannot decode file.", "Cannot decode", JOptionPane.WARNING_MESSAGE );
        }
      }
    }

  }

  public static void main( final String[] aArgs ) {
    startSample( MainPanel.class, "Decoding AIXM" );
  }
}
