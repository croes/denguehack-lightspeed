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
package samples.tea.gxy.viewshed.directional;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;

import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.concurrent.painting.AsynchronousLayerFactory;

public class MainPanel extends GXYSample {

  private TLcdLonLatPoint fSunDefaultPoint = new TLcdLonLatPoint( 0.0,0.0 );
  private ILcdGeoReference fSunPositionReference = new TLcdGeodeticReference(new TLcdGeodeticDatum(  ) );
  private TLcdLonLatPoint fCenterPosition = new TLcdLonLatPoint( -74.5, 42.4 );

  private CreateViewshedAction fCreateViewshedAction;
  private SunPositionPanelModel fSunPositionModel;

  @Override
  protected JPanel createSettingsPanel() {
    //Create eye position panel
    fSunPositionModel = new SunPositionPanelModel( fSunDefaultPoint, fSunPositionReference, 250, 3.5d, fCenterPosition, fSunPositionReference );
    //Create action that generates viewsheds for this sample
    fCreateViewshedAction = new CreateViewshedAction( fSunPositionModel );
    return new SunPositionPanel( fSunPositionModel, fCreateViewshedAction );
  }

  @Override
  protected void addData() throws IOException {
    TLcdCompositeGXYLayerFactory services = new TLcdCompositeGXYLayerFactory(
        ServiceRegistry.getInstance().query( ILcdGXYLayerFactory.class ) );
    TLcdCompositeGXYLayerFactory layerFactory = new TLcdCompositeGXYLayerFactory( new ViewshedLayerFactory(), services );

    ViewshedModelFactory viewshedModelFactory = new ViewshedModelFactory();
    ILcdModel pointModel = viewshedModelFactory.createPointModel( fSunDefaultPoint, fSunPositionReference );
    ILcd2DEditablePoint pointObject = ( ILcd2DEditablePoint ) pointModel.elements().nextElement();

    ILcdGXYLayer pointLayer = layerFactory.createGXYLayer( pointModel );
    ILcdGXYLayer viewshedLayer = AsynchronousLayerFactory.createAsynchronousLayer( layerFactory.createGXYLayer( viewshedModelFactory.createViewshedModel() ) );
    ILcdGXYLayer ithacaLayer = layerFactory.createGXYLayer( viewshedModelFactory.createBuildingsModel() );
    ILcdGXYLayer terrainLayer = layerFactory.createGXYLayer( viewshedModelFactory.createTerrainModel() );

    fCreateViewshedAction.setParameters( ithacaLayer.getModel(), terrainLayer.getModel(), viewshedLayer );
    fSunPositionModel.setPointModel( pointModel, pointObject );
    fSunPositionModel.addPropertyChangeListener( new MySunPositionPanelListener( pointModel,pointObject ) );
    GXYLayerUtil.addGXYLayer( getView(), terrainLayer, false, false );
    GXYLayerUtil.addGXYLayer( getView(), viewshedLayer,false,false );
    GXYLayerUtil.addGXYLayer( getView(), ithacaLayer,false,false );
    GXYLayerUtil.addGXYLayer( getView(), pointLayer,false,false );
    GXYLayerUtil.fitGXYLayer( getView(), ithacaLayer );
  }

  public static void main( final String[] aArgs ) {
    startSample( MainPanel.class, "Directional Viewshed" );
  }

  /**
   * Listens to the sun position panel, and adjusts the model that contains the sun point
   * accordingly.
   */
  private static class MySunPositionPanelListener implements PropertyChangeListener {
    private ILcdModel fModelToNotify;
    private ILcd2DEditablePoint fPointToModify;

    public MySunPositionPanelListener( ILcdModel aModelToNotify, ILcd2DEditablePoint aPointToModify ) {
      fModelToNotify = aModelToNotify;
      fPointToModify = aPointToModify;
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      if(evt.getPropertyName().equals( SunPositionPanelModel.SUN_LONGITUDE_PROPERTY_NAME )){
        fPointToModify.move2D( ( Double ) evt.getNewValue(),fPointToModify.getY() );
      }else if(evt.getPropertyName().equals( SunPositionPanelModel.SUN_LATITUDE_PROPERTY_NAME )){
        fPointToModify.move2D( fPointToModify.getX(), ( Double ) evt.getNewValue() );
      }
      fModelToNotify.elementChanged( fPointToModify, ILcdModel.FIRE_NOW );
    }
  }
}

