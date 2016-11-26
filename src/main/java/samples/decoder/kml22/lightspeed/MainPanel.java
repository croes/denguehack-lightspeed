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
package samples.decoder.kml22.lightspeed;

import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.format.kml22.view.lightspeed.TLspKML22ViewFitAction;
import com.luciad.format.kml22.view.swing.TLcdKML22BalloonContentProvider;
import com.luciad.format.kml22.xml.TLcdKML22ModelDecoder;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.swing.TLspBalloonManager;

import samples.common.BalloonViewSelectionListener;
import samples.decoder.kml22.common.modelcontenttree.ModelNodeTreePanel;
import samples.decoder.kml22.common.timetoolbar.TimeToolbarFactory;
import samples.lightspeed.common.FitUtil;

/**
 * The KML decoder sample illustrates the use of <code>TLcdKML22ModelDecoder</code> and
 * <code>TLspKML22LayerBuilder</code> to add KML models to an <code>ILspView</code>.
 * <p/>
 * This sample also demonstrates how to set up balloons for KML, how to render the KML model content tree,
 * and how to set up the time toolbar for KML.
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  private final static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( MainPanel.class.getName() );

  private TLcdKML22ModelDecoder fModelDecoder;

  public MainPanel(String[] aArgs) {
    super(aArgs);
  }

  @Override
  protected ILspAWTView createView() {
    return TLspViewBuilder
        .newBuilder()
        .viewType(ILspView.ViewType.VIEW_3D)
        .defaultEffects()
        .buildAWTView();
  }

  @Override
  protected ILspLayerFactory createLayerFactory() {
    return new KMLLayerFactory();
  }

  private TLcdKML22ModelDecoder getModelDecoder() {
    if(fModelDecoder==null){
      fModelDecoder = new TLcdKML22ModelDecoder();
    }
    return fModelDecoder;
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    //Set up the balloon manager
    TLspBalloonManager balloonManager = new TLspBalloonManager(getView(), getView().getOverlayComponent(), TLcdOverlayLayout.Location.NO_LAYOUT, new TLcdKML22BalloonContentProvider(getModelDecoder().getResourceProvider()));

    //Set up the model content tree panel
    ModelNodeTreePanel modelNodeTreePanel = new ModelNodeTreePanel(getView(), getModelDecoder().getResourceProvider(), balloonManager);
    modelNodeTreePanel.addViewFitAction(new TLspKML22ViewFitAction());
    BalloonViewSelectionListener layerViewSelectionBalloonListener = new BalloonViewSelectionListener( getView(), balloonManager);
    getView().getRootNode().addHierarchySelectionListener( layerViewSelectionBalloonListener );
    getView().getRootNode().addHierarchyLayeredListener(layerViewSelectionBalloonListener);
    getView().getRootNode().addHierarchyPropertyChangeListener(layerViewSelectionBalloonListener);
    addComponentToRightPanel(modelNodeTreePanel);

    //Setup the time toolbar
    TimeToolbarFactory timeToolbarFactory = new TimeToolbarFactory();
    JPanel timeToolbar = timeToolbarFactory.createTimeToolbar( getView(), getView().getOverlayComponent() );
    if ( timeToolbar!=null ) {
      getView().getOverlayComponent().add( timeToolbar, TLcdOverlayLayout.Location.SOUTH );
    }
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    try {
      getView().addLayersFor(getModelDecoder().decode("Data/Kml/Leuven_Town_Hall.kmz"));
      // Decode the KML model, create a layer for it and add the layer to the view.
      FitUtil.fitOnLayers(this, getView().addLayersFor(getModelDecoder().decode("Data/Kml/luciad_and_leuven.kml")));
    }
    catch ( IOException ioException ) {
      sLogger.error( "Couldn't load KML data (" + ioException.getMessage() + ")", ioException );
    }
  }

  public static void main( final String[] aArgs ) {
    startSample(MainPanel.class, aArgs, "KML22 Lightspeed");
  }
}
