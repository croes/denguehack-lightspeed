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
package samples.decoder.kml22.gxy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.luciad.format.kml22.model.TLcdKML22DynamicModel;
import com.luciad.format.kml22.model.TLcdKML22Kml;
import com.luciad.format.kml22.model.TLcdKML22ModelDescriptor;
import com.luciad.format.kml22.model.TLcdKML22Parameters;
import com.luciad.format.kml22.util.TLcdKML22ResourceProvider;
import com.luciad.format.kml22.view.gxy.TLcdKML22GXYRegionFilter;
import com.luciad.format.kml22.view.gxy.TLcdKML22GXYViewFitAction;
import com.luciad.format.kml22.view.gxy.TLcdKML22GXYViewParametersUpdater;
import com.luciad.format.kml22.view.swing.TLcdKML22BalloonContentProvider;
import com.luciad.format.kml22.xml.TLcdKML22ModelDecoder;
import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.BalloonViewSelectionListener;
import samples.common.LuciadFrame;
import com.luciad.io.TLcdStatusInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.swing.TLcdGXYBalloonManager;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.common.formatsupport.OpenTransferHandler;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.decoder.kml22.common.modelcontenttree.ModelNodeTreePanel;
import samples.decoder.kml22.common.timetoolbar.TimeToolbarFactory;
import samples.common.SamplePanel;
import samples.gxy.common.OverlayPanel;
import samples.gxy.common.ProgressUtil;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * This sample demonstrates KML 2.2 in a 2D view.
 */
public class MainPanel extends SamplePanel {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( MainPanel.class.getName() );

  private TLcdKML22ResourceProvider fResourceProvider;
  private TLcdKML22Parameters fParameters;
  private ModelNodeTreePanel fModelNodeTreePanel;
  private TLcdKML22ModelDecoder fModelDecoder;

  private TLcdMapJPanel fMapJPanel;
  private LayerFactory fLayerFactory;
  private TLcdKML22GXYRegionFilter fGXYRegionFilter;
  private OverlayPanel fOverlayPanel;

  public MainPanel() {
    super();
    initialize();
  }

  /**
   *  Creates and initializes all fields and components necessary for this sample application.
   */
  private void initialize() {
    //Create model decoder
    fModelDecoder = new TLcdKML22ModelDecoder();
    //Create resource providers
    fResourceProvider = fModelDecoder.getResourceProvider();
    //Create 2D view
    fMapJPanel = SampleMapJPanelFactory.createMapJPanel();
    fMapJPanel.setGXYLayerFactory( fLayerFactory );
    fMapJPanel.addLayeredListener( new DisposeModelLayeredListener() );
    fLayerFactory = new LayerFactory( fResourceProvider, fMapJPanel );
    fOverlayPanel = new OverlayPanel( fMapJPanel );
    //Create KML parameter map
    fParameters = new TLcdKML22Parameters();

    // Make sure the parameter map gets updated whenever the view changes.
    TLcdKML22GXYViewParametersUpdater updater = new TLcdKML22GXYViewParametersUpdater( fParameters );
    fMapJPanel.addPropertyChangeListener( new ParameterUpdateNotifier( updater, fMapJPanel ) );
    fGXYRegionFilter = new TLcdKML22GXYRegionFilter( fMapJPanel );
    //Add balloon support
    TLcdKML22BalloonContentProvider balloonContentProvider = new TLcdKML22BalloonContentProvider( fResourceProvider );

    TLcdGXYBalloonManager balloonManager = new TLcdGXYBalloonManager( fMapJPanel, fOverlayPanel , TLcdOverlayLayout.Location.NO_LAYOUT,balloonContentProvider);

    //create time toolbar
    TimeToolbarFactory timeToolbarFactory = new TimeToolbarFactory();
    JPanel timeToolbar = timeToolbarFactory.createTimeToolbar(fMapJPanel);
    fOverlayPanel.add( timeToolbar, TLcdOverlayLayout.Location.SOUTH );
    fMapJPanel.addComponentListener( new ResizeListener( timeToolbar ) );

    //Create model content tree
    fModelNodeTreePanel = new ModelNodeTreePanel( fMapJPanel, fResourceProvider,balloonManager );
    fModelNodeTreePanel.addViewFitAction( new TLcdKML22GXYViewFitAction( ) );
    BalloonViewSelectionListener layerViewSelectionBalloonListener = new BalloonViewSelectionListener( fMapJPanel, balloonManager );
    fMapJPanel.getRootNode().addHierarchySelectionListener( layerViewSelectionBalloonListener );
    fMapJPanel.getRootNode().addHierarchyLayeredListener( layerViewSelectionBalloonListener );
    fMapJPanel.getRootNode().addHierarchyPropertyChangeListener( layerViewSelectionBalloonListener );
  }



  /**
   * Build the sample GUI.
   */
  protected void createGUI() {
    // Create the default toolbar and layer control.
    ToolBar tool_bar = new ToolBar( fMapJPanel, true, this );

    // Add an open action to the tool bar.
    OpenSupport open_support = new MyOpenSupport( this, Collections.singletonList( fModelDecoder ) );
    open_support.addStatusListener(ProgressUtil.createProgressDialog(this, "Loading KML data"));
    OpenAction open_action = new OpenAction( open_support );
    // Drag and drop.
    fMapJPanel.setTransferHandler( new OpenTransferHandler( open_support ) );
    tool_bar.addAction( open_action );

    // Create map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
            "Map", fOverlayPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    //Obtain layer controls
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    // Add the components to the sample.
    setLayout( new BorderLayout() );
    add( BorderLayout.NORTH, tool_bar );
    add( BorderLayout.CENTER, map_panel );
    add( BorderLayout.EAST, fModelNodeTreePanel );
    add( BorderLayout.WEST,layer_control);
  }

  /**
   * Load the sample data.
   */
  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);

    // Add a progress bar to the model decoder.
    JDialog progress = ProgressUtil.createProgressDialog( this, "Loading KML22 data..." );
    if ( progress instanceof ILcdStatusListener ) {
      TLcdStatusInputStreamFactory input_stream_factory = new TLcdStatusInputStreamFactory();
      input_stream_factory.addStatusEventListener( ( ILcdStatusListener ) progress );
      fModelDecoder.setInputStreamFactory( input_stream_factory );
    }

    String codebase = "";
    try {
      // Decode the KML model, create a layer for it and add the layer to the view.
      addModel( fModelDecoder.decode( codebase + "Data/Kml/luciad.kml" ) );
    }
    catch ( IOException ioException ) {
      sLogger.error( "Couldn't load KML data (" + ioException.getMessage() + ")", ioException );
    }

    // Remove progress bar from model decoder and dispose dialog.
    if ( progress instanceof ILcdStatusListener ) {
      TLcdStatusInputStreamFactory
          input_stream_factory =
          ( TLcdStatusInputStreamFactory ) fModelDecoder.getInputStreamFactory();
      input_stream_factory.removeStatusEventListener( ( ILcdStatusListener ) progress );
    }
    progress.dispose();
  }

  /**
   * Adds a decoded model to this view by creating a new layer.
   *
   * @param aModel A decoded model to add to the view.
   */
  public void addModel( ILcdModel aModel ) {
    if ( aModel instanceof TLcdKML22Kml ) {
      TLcdKML22Kml kml22Kml = ( TLcdKML22Kml ) aModel;
      TLcdKML22DynamicModel kmlModel = new TLcdKML22DynamicModel( kml22Kml, fResourceProvider, fParameters, fGXYRegionFilter );
      final ILcdGXYLayer newLayer = fLayerFactory.createGXYLayer( kmlModel );

      // Add the source name to the label of the created layer.
      String source_name = getLabelString( aModel );
      newLayer.setLabel( newLayer.getLabel() + " (" + source_name + ")" );

      GXYLayerUtil.addGXYLayer( fMapJPanel, newLayer, true, false );
      GXYLayerUtil.fitGXYLayer( fMapJPanel, newLayer );
    }
    else {
      throw new IllegalArgumentException( "Model must be TLcdKML22Kml" );
    }
  }

  /**
   * Creates a string that can be used as a label for a given model
   * @param aModel A model that needs a label
   * @return A label for the given model
   */
  public static String getLabelString( ILcdModel aModel ) {
    String source_name = aModel.getModelDescriptor().getSourceName();
    int path_separator = Math.max( source_name.lastIndexOf( '/' ), source_name.lastIndexOf( '\\' ) );
    source_name = source_name.substring( path_separator + 1 );
    return source_name;
  }

  /**
   * Class that handles the action of opening new KML files.
   */
  private class MyOpenSupport extends OpenSupport{

    public MyOpenSupport( Component aParent, List<? extends ILcdModelDecoder> aModelDecoders ) {
      super( aParent, aModelDecoders );
    }

    @Override
    protected void modelDecoded( String aSource, ILcdModel aModel ) {
      addModel( aModel );
    }
  }

  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        LuciadFrame luciadFrame = new LuciadFrame( new MainPanel(),
                                                   "KML 2.2 GXY", 950, 600  );
        luciadFrame.setExtendedState( Frame.MAXIMIZED_BOTH );
      }
    } );
  }

    /**
   * Listener that disposes models after their respective layers have been removed.
   */
  private class DisposeModelLayeredListener implements ILcdLayeredListener {
    public void layeredStateChanged( TLcdLayeredEvent e ) {
      if ( e.getID() == TLcdLayeredEvent.LAYER_REMOVED && e.getLayer().getModel().getModelDescriptor() instanceof TLcdKML22ModelDescriptor ) {
        e.getLayer().getModel().dispose();
      }
    }
    }

  private static class ParameterUpdateNotifier implements PropertyChangeListener {

    private final TLcdKML22GXYViewParametersUpdater fUpdater;
    private ILcdGXYView fView;

    public ParameterUpdateNotifier( TLcdKML22GXYViewParametersUpdater aUpdater, ILcdGXYView aGXYView ) {
      fView = aGXYView;
      fUpdater = aUpdater;
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( "worldOrigin".equals( evt.getPropertyName() ) || "scale".equals( evt.getPropertyName() ) ) {
        fUpdater.updateParameters( fView );
      }
    }
  }

    /**
   * <p>Listens to the view, and adapts the location of the timetoolbar</p>
   */
  private static class ResizeListener extends ComponentAdapter {
    private Component fTimeToolbar;

    public ResizeListener( Component aTimeToolbar ) {
      fTimeToolbar = aTimeToolbar;
    }

    public void componentResized( ComponentEvent e ) {
      fTimeToolbar.setLocation( ( int ) ( e.getComponent().getWidth() / 2 - fTimeToolbar.getWidth() / 2. ),
                                e.getComponent().getHeight() - fTimeToolbar.getHeight() - 10 );
    }
  }
}
