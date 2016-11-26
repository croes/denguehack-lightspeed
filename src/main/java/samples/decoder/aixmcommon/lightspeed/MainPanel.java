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
package samples.decoder.aixmcommon.lightspeed;

import java.io.IOException;
import java.util.Collection;

import javax.swing.JToolBar;

import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMMessage;
import com.luciad.gui.ILcdAction;
import com.luciad.model.ILcdModel;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.TLspAWTView;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.controller.selection.TLspSelectController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;

import samples.common.action.ShowPopupAction;
import samples.decoder.aixm51.AIXM51AirspaceCreator;
import samples.decoder.aixm51.AIXM51DesignatedPointCreator;
import samples.decoder.aixm51.AIXM51ModelTreeDecoder;
import samples.decoder.aixm51.AIXM51SaveDataAction;
import samples.decoder.aixm51.transformation.AIXM45To51ModelDecoder;
import samples.decoder.aixmcommon.ShowAIXMPropertiesAction;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;

/**
 * A sample to demonstrate visualization of AIXM 5.x data in a Lightspeed view.
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  public MainPanel( String[] aArgs ) {
    super( aArgs );
  }

  @Override
  protected ILcdAction createSaveAction() {
    //Create a custom save action that exports data to AIXM 5.1
    return new AIXM51SaveDataAction("Data", this, getSelectedLayers());
  }

  @Override
  protected ILspLayerFactory createLayerFactory() {
    //create a layer factory for AIXM5.x layers
    ILspLayerFactory aixmLayerFactory = new AIXM5xLayerFactory();
    //use a composite layer factory to return both the AIXM5.x layer factory as the layer factory created
    //by the super class
    ILspLayerFactory layerFactory = super.createLayerFactory();
    return new TLspCompositeLayerFactory( layerFactory, aixmLayerFactory );
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    // Add an AIXM 5.1 layer with a custom airspace.
    TLcdAIXM51AbstractAIXMMessage airspaceMessage = AIXM51AirspaceCreator.createAirspaceMessage();
    airspaceMessage.addElement( AIXM51AirspaceCreator.createAirspace( "" ), ILcdModel.NO_EVENT );
    getView().addModel( airspaceMessage );

    // Add an AIXM 5.1 layer with a custom designated point.
    TLcdAIXM51AbstractAIXMMessage designatedPointMessage = AIXM51DesignatedPointCreator.createDesignatedPointMessage();
    designatedPointMessage.addElement( AIXM51DesignatedPointCreator.createDesignatedPointFeature(), ILcdModel.NO_EVENT );
    getView().addModel( designatedPointMessage );

    // Add some AIXM 4.5 models
    AIXM45To51ModelDecoder aixm4To5ModelDecoder = new AIXM45To51ModelDecoder();
    ILcdModel aixm51Model = aixm4To5ModelDecoder.decode( "Data/AIXM/4.5/ahp_1.xml" );
    ILspLayer convertedAirportLayer = getView().getLayerFactory().createLayers( aixm51Model ).iterator().next();
    convertedAirportLayer.setLabel( "Converted Airport" );
    getView().addLayer( convertedAirportLayer );

    ILcdModel aixm51AirspaceModel = aixm4To5ModelDecoder.decode( "Data/AIXM/4.5/airspace_1.xml" );
    ILspLayer convertedAirspaceLayer = getView().getLayerFactory().createLayers( aixm51AirspaceModel ).iterator().next();
    convertedAirspaceLayer.setLabel( "Converted Airspace" );
    getView().addLayer( convertedAirspaceLayer );

    // Add some AIXM5.1 data of Chicago
    // use a custom model decoder that generates one model per feature type
    AIXM51ModelTreeDecoder modelTreeDecoder = new AIXM51ModelTreeDecoder();
    getView().addModel( modelTreeDecoder.decode( "Data/AIXM/5.1/Chicago Airspace.aixm51" ) );
    Collection<ILspLayer> layers = getView().addLayersFor( modelTreeDecoder.decode( "Data/AIXM/5.1/Chicago O'Hare.aixm51" ) );
    FitUtil.fitOnLayers(this, layers);
  }

  @Override
  protected JToolBar[] createToolBars( ILspAWTView aView) {
    ToolBar regularToolBar = new ToolBar(aView, this, true, true) {
      @Override
      protected ILspController createDefaultController() {
        TLspEditController editController = ControllerFactory.createDefaultEditController(getUndoManager(), getView());
        TLspSelectController selectController = new TLspSelectController();
        ALspController navigationController = ControllerFactory.createNavigationController();

        editController.appendController(selectController);
        selectController.appendController(navigationController);

        ILcdAction showPropertiesAction = new ShowAIXMPropertiesAction(getView(), this);
        selectController.setDoubleClickAction(showPropertiesAction);

        if (getView() instanceof TLspAWTView) {
          TLcdDeleteSelectionAction deleteSelectionAction = new TLcdDeleteSelectionAction(getView());
          ILcdAction[] actionsForPopup = new ILcdAction[]{deleteSelectionAction, showPropertiesAction};
          ILcdAction showPopupAction = new ShowPopupAction(actionsForPopup, ((TLspAWTView) getView()).getHostComponent());
          selectController.setContextAction(showPopupAction);
        }
        return editController;
      }
    };

    return new JToolBar[]{regularToolBar};
  }

  public static void main( final String[] aArgs ) {
    startSample(MainPanel.class, aArgs, "Visualization of AIXM 5.x data in a Lightspeed view.");
  }

}
