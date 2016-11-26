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
package samples.decoder.aixmcommon.gxy;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JFileChooser;

import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMMessage;
import com.luciad.format.aixm51.view.gxy.TLcdAIXM51LabelingAlgorithm;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.io.TLcdStatusInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelLabelingAlgorithmProvider;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;

import samples.common.action.ShowPopupAction;
import samples.common.action.ShowPropertiesAction;
import samples.common.dataObjectDisplayTree.DataObjectTreeCellRenderer;
import samples.common.dataObjectDisplayTree.ISOMeasureTreeCellRenderer;
import samples.common.serviceregistry.ServiceRegistry;
import samples.decoder.aixm5.AIXM5ModelTreeDecoder;
import samples.decoder.aixm5.AIXM5TreeCellRenderer;
import samples.decoder.aixm5.gxy.AIXM5LayerFactory;
import samples.decoder.aixm51.AIXM51AirspaceCreator;
import samples.decoder.aixm51.AIXM51DesignatedPointCreator;
import samples.decoder.aixm51.AIXM51ModelTreeDecoder;
import samples.decoder.aixm51.AIXM51TreeCellRenderer;
import samples.decoder.aixm51.transformation.AIXM45To51ModelDecoder;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * This sample demonstrates the LuciadLightspeed AIXM 5.1 decoding, encoding and
 * visualization capabilities.
 * <p>
 * It allows the user to choose an AIXM 5.1 XML file and adds it as an
 * additional layer to the view. Furthermore, the layer controls allow the user
 * to select elements, show labels and edit them. Using the context menu, it is
 * possible to show the properties of a specific AIXM 5.1 feature. Finally,
 * decoded AIXM 5.1 layers can be saved again to a file.
 * </p>
 */
public class MainPanel extends GXYSample {

  private final AIXM51ModelTreeDecoder f51ModelDecoder = new AIXM51ModelTreeDecoder();
  private final AIXM5ModelTreeDecoder f50ModelDecoder = new AIXM5ModelTreeDecoder();
  private final AIXM45To51ModelDecoder fTransformingDecoder = new AIXM45To51ModelDecoder();
  private final ILcdGXYLayerFactory f51LayerFactory = new AIXM51LayerFactory();
  private final ILcdGXYLayerFactory f50LayerFactory = new AIXM5LayerFactory();

  /**
   * Build the sample GUI.
   */
  protected void createGUI() {
    super.createGUI();
    ToolBar toolBar = getToolBars()[0];
    TLcdGXYEditController2 editController = toolBar.getGXYControllerEdit();

    // Actions on mouse right button click in select mode
    DataObjectTreeCellRenderer cellRenderer = new DataObjectTreeCellRenderer();
    cellRenderer.addCellRenderer( new AIXM51TreeCellRenderer() );
    cellRenderer.addCellRenderer( new AIXM5TreeCellRenderer() );
    cellRenderer.addCellRenderer( new ISOMeasureTreeCellRenderer() );
    ILcdAction showPropertiesAction = new ShowPropertiesAction( getView(), cellRenderer, null );
    TLcdDeleteSelectionAction deleteSelectionAction = new TLcdDeleteSelectionAction(getView());
    ILcdAction[] rightMouseButtonActions = {
            showPropertiesAction,
            deleteSelectionAction,
    };
    ShowPopupAction showPopupAction = new ShowPopupAction( rightMouseButtonActions, getView() );
    editController.setRightClickAction( showPopupAction );
    editController.setDoubleClickAction( showPropertiesAction );

    // Configure AIXM 5 Specific labeling.
    ServiceRegistry.getInstance().register(new ILcdGXYLabelLabelingAlgorithmProvider<ILcdGXYLabelingAlgorithm>() {
      private TLcdAIXM51LabelingAlgorithm fLabelAlgorithm = new TLcdAIXM51LabelingAlgorithm();

      @Override
      public ILcdGXYLabelingAlgorithm getLabelingAlgorithm(TLcdLabelIdentifier aLabel ) {
        if ( fLabelAlgorithm.canHandle( aLabel ) ) {
          return fLabelAlgorithm;
        }
        return null;
      }
    });

    // Add an action for opening AIXM 5.1 data to the toolbar.
    AIXMDataChooserAction dataChooser = new AIXMDataChooserAction( this );
    toolBar.addAction( dataChooser );

    // Add an action for saving AIXM 5.1 data to the toolbar.
    AIXM5xSaveDataAction dataSaver = new AIXM5xSaveDataAction("", this, getSelectedLayers());
    toolBar.addAction( dataSaver );
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Add an AIXM 5.1 layer with a custom airspace.
    TLcdAIXM51AbstractAIXMMessage airspaceMessage = AIXM51AirspaceCreator.createAirspaceMessage();
    airspaceMessage.addElement( AIXM51AirspaceCreator.createAirspace(""), ILcdModel.NO_EVENT );
    final ILcdGXYLayer airspaceLayer = f51LayerFactory.createGXYLayer( airspaceMessage );
    GXYLayerUtil.addGXYLayer( getView(), airspaceLayer );

    // Add an AIXM 5.1 layer with a custom designated point.
    TLcdAIXM51AbstractAIXMMessage designatedPointMessage = AIXM51DesignatedPointCreator.createDesignatedPointMessage();
    designatedPointMessage.addElement( AIXM51DesignatedPointCreator.createDesignatedPointFeature(), ILcdModel.NO_EVENT );
    final ILcdGXYLayer designatedPointLayer = f51LayerFactory.createGXYLayer( designatedPointMessage );
    GXYLayerUtil.addGXYLayer( getView(), designatedPointLayer );

    ILcdModel aixm51Model = fTransformingDecoder.decode( "Data/AIXM/4.5/ahp_1.xml" );

    final ILcdGXYLayer convertedAirportLayer = f51LayerFactory.createGXYLayer( aixm51Model );
    convertedAirportLayer.setLabel( "Converted Airport" );
    GXYLayerUtil.addGXYLayer( getView(), convertedAirportLayer );

    aixm51Model = fTransformingDecoder.decode( "Data/AIXM/4.5/airspace_1.xml" );
    final ILcdGXYLayer convertedAirspaceLayer = f51LayerFactory.createGXYLayer( aixm51Model );
    convertedAirspaceLayer.setLabel( "Converted Airspace" );
    GXYLayerUtil.addGXYLayer( getView(), convertedAirspaceLayer );

    // Initially fit on the area around the preloaded airspace.
    TLcdLonLatBounds bounds = new TLcdLonLatBounds(2.498055556, 49.38611111, 4.053055556, 2.145);
    GXYLayerUtil.fitGXYLayer( getView(), airspaceLayer, bounds );
  }

  /**
      // Set a default directory.
      fFileChooser.setCurrentDirectory( new File( getBaseDirectory() ) );
      // Set a file filter.
      fFileChooser.setFileFilter(new AIXM5FileFilter());
   * This action uses a <code>JFileChooser</code> to allow the user to open an AIXM 5.1 data file.
   * The data file is decoded using a <code>TLcdAIXM5ModelDecoder</code>.
   * A layer is created using the* <code>AIXM5LayerFactory</code>, which uses a
   * <code>TLcdAIXM5GXYPainterEditorProvider</code> to provide painters for the different AIXM 5.1 elements.
   * The new layer is added to the view.
   */
  private class AIXMDataChooserAction extends ALcdAction {

    private final JFileChooser fFileChooser = new JFileChooser();
    private final Component fParentComponent;

    public AIXMDataChooserAction( Component aParentComponent ) {
      super("Open AIXM 5.1 data file...", TLcdIconFactory.create(TLcdIconFactory.OPEN_ICON));
      fParentComponent = aParentComponent;
      fFileChooser.setCurrentDirectory( new File("") );
      fFileChooser.setFileFilter( new AIXM5xFileFilter() );
    }

    public void actionPerformed( ActionEvent aEvent ) {

      if ( fFileChooser.showOpenDialog( getView() ) == JFileChooser.APPROVE_OPTION ) {
        final String source_path = fFileChooser.getSelectedFile().getAbsolutePath();

        final ILcdModelDecoder decoder;
        final ILcdGXYLayerFactory layerFactory;
        // Check whether the source is a valid AIXM 5.1 file.
        if ( f51ModelDecoder.canDecodeSource( source_path ) ) {
          decoder = f51ModelDecoder;
          layerFactory = f51LayerFactory;
        }
        // try decoding as AIXM 3.3/4.5
        else if ( fTransformingDecoder.canDecodeSource( source_path ) ) {
          decoder = fTransformingDecoder;
          layerFactory = f51LayerFactory;
        } else if ( f50ModelDecoder.canDecodeSource( source_path ) ) {
          decoder = f50ModelDecoder;
          layerFactory = f50LayerFactory;
        } else {
          decoder = null;
          layerFactory = f51LayerFactory;
        }

        if ( decoder != null ) {

          // Start a thread that decodes the AIXM 5.1 file.
          Thread load_data_thread = new Thread( new Runnable() {
            public void run() {
              // Add a progress bar to the model decoder.
              TLcdStatusInputStreamFactory statusInputStreamFactory = new TLcdStatusInputStreamFactory();
              statusInputStreamFactory.addStatusEventListener(getStatusBar());
              ((ILcdInputStreamFactoryCapable) decoder).setInputStreamFactory( statusInputStreamFactory );

              try {
                // Decode the data.
                ILcdModel model = decoder.decode( source_path );

                //The custom model decoder creates separate messages for each feature type (e.g., AirportHeliport, Navaid, ...).
                if ( model instanceof ILcdModelTreeNode ) {
                  // Create layers and add them to the view.
                  final Enumeration<ILcdModel> models = ((ILcdModelTreeNode) model).models();
                  while ( models.hasMoreElements() ) {
                    ILcdModel subModel = models.nextElement();
                    GXYLayerUtil.addGXYLayer( getView(), layerFactory.createGXYLayer( subModel ) );
                  }
                } else {
                  GXYLayerUtil.addGXYLayer( getView(), layerFactory.createGXYLayer( model ) );
                }
              } catch (IOException ioe) {
                ioe.printStackTrace();
                showMessageDialog(fParentComponent, "Invalid AIXM 5.x data file. Cannot decode file.\n" + ioe.getMessage(), "Invalid file", WARNING_MESSAGE);
              } finally {
                // Remove progress bar from the model decoder
                statusInputStreamFactory.removeStatusEventListener(getStatusBar());
              }
            }
          } );
          load_data_thread.setPriority( Thread.MIN_PRIORITY );
          load_data_thread.start();
        }
        else {
          showMessageDialog( getView(), "Invalid AIXM 5.x data file. Cannot decode file.", "Cannot decode", WARNING_MESSAGE );
        }
      }
    }
  }

  public static void main( final String[] aArgs ) {
    startSample( MainPanel.class, "AIXM 5.1 Decoder sample" );
  }
}
