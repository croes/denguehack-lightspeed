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

import com.luciad.format.raster.TLcdJetIndexColorModelFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.tea.viewshed.ILcdViewshed;
import com.luciad.tea.viewshed.TLcdBoundedCompositeViewshed;
import com.luciad.tea.viewshed.TLcdCompositeViewshed;
import com.luciad.tea.viewshed.TLcdDirectionalViewshedObserver;
import com.luciad.tea.viewshed.TLcdMinimalVisibilityComposite;
import com.luciad.tea.viewshed.TLcdTerrainViewshedFactory;
import com.luciad.tea.viewshed.TLcdViewshedMultilevelRaster;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.*;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.gxy.ILcdGXYLayer;
import samples.tea.gxy.viewshed.PolygonViewshedFactory;
import samples.tea.gxy.viewshed.ViewshedUtil;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.IndexColorModel;

/**
 * Action to calculate a composite viewshed of the terrain and model, based on
 * the various settings in <code>SunPositionPanelModel</code>
 */
class CreateViewshedAction extends AbstractAction {
  private static final String ERROR_MESSAGE_TITLE = "Invalid input for viewshed computation.";
  private SunPositionPanelModel fSunPositionPanelModel;
  private ILcdModel fBuildingModel;
  private ILcdModel fTerrainModel;
  private ILcdGXYLayer fTargetLayer;

  public CreateViewshedAction( SunPositionPanelModel aSunPositionModel ) {
    super("Update");
    fSunPositionPanelModel = aSunPositionModel;
  }

  /**
   * Sets the parameters for this action
   * @param aBuildingModel A model with buildings that need to be extruded
   * @param aTerrainModel A model for terrain
   * @param aTargetLayer A target layer in which the viewshed should be put.
   */
  public void setParameters( ILcdModel aBuildingModel, ILcdModel aTerrainModel, ILcdGXYLayer aTargetLayer ) {
    fBuildingModel = aBuildingModel;
    fTerrainModel = aTerrainModel;
    fTargetLayer = aTargetLayer;
  }

  public void actionPerformed( ActionEvent e ) {
    try {
      ILcdModel model = fTargetLayer.getModel();
      //Create a reference for the viewshed
      ILcdGeoReference viewshedReference = ViewshedUtil.createViewshedReference( fSunPositionPanelModel.getCenterPosition(), fSunPositionPanelModel.getCenterPositionReference() );

      //Create a viewing direction based on the location of the sun
      TLcdDirectionalViewshedObserver directionalViewshedObserver = createViewingDirection( fSunPositionPanelModel.getSunPosition(), fSunPositionPanelModel.getSunPositionReference(), viewshedReference );

      //Create a viewshed for the buildings
      PolygonViewshedFactory polygonViewshedFactory = new PolygonViewshedFactory( viewshedReference, directionalViewshedObserver );
      ILcdModelReference buildingModelReference = fBuildingModel.getModelReference();
      TLcdGeoReference2GeoReference buildingTransformation = ViewshedUtil.createTransformation( buildingModelReference,viewshedReference );
      ILcdViewshed polygonViewshed = polygonViewshedFactory.createViewshed( fBuildingModel, buildingTransformation, ViewshedUtil.createHeightProvider( buildingModelReference, fTerrainModel ) );

      //Create a viewshed for terrain
      TLcdTerrainViewshedFactory terrainViewshedFactory = new TLcdTerrainViewshedFactory( viewshedReference, directionalViewshedObserver,fSunPositionPanelModel.getStepSize() );
      ILcdModelReference terrainModelReference = fTerrainModel.getModelReference();
      TLcdGeoReference2GeoReference terrainTransformation = ViewshedUtil.createTransformation( terrainModelReference,viewshedReference );
      terrainViewshedFactory.setStepSize(fSunPositionPanelModel.getStepSize());
      ILcdHeightProvider terrainHeightProvider = ViewshedUtil.createHeightProvider( terrainModelReference, fTerrainModel);
      ILcdViewshed terrainViewshed = terrainViewshedFactory.createViewshed( terrainHeightProvider, terrainHeightProvider.getBounds(), terrainTransformation );

      //Composite the viewsheds into a single viewshed
      ILcdViewshed viewshed = null;
      if ( terrainViewshed instanceof TLcdBoundedCompositeViewshed && polygonViewshed instanceof TLcdBoundedCompositeViewshed ) {
        //If both viewsheds are TLcdBoundedObjectViewshed, then we can merge them into one to improve performance
        TLcdBoundedCompositeViewshed boundedObjectViewshed = new TLcdBoundedCompositeViewshed( viewshedReference, directionalViewshedObserver );
        boundedObjectViewshed.addAllObjects( ( TLcdBoundedCompositeViewshed ) terrainViewshed );
        boundedObjectViewshed.addAllObjects( ( TLcdBoundedCompositeViewshed ) polygonViewshed );
        viewshed = boundedObjectViewshed;
      }
      else {
        //Fall back if the viewsheds return something else. Viewsheds will be accelerated separately.
        TLcdCompositeViewshed compositeMinimalViewshed = new TLcdCompositeViewshed();
        compositeMinimalViewshed.setComposite( new TLcdMinimalVisibilityComposite() );
        compositeMinimalViewshed.addViewshed( terrainViewshed );
        compositeMinimalViewshed.addViewshed( polygonViewshed );
        viewshed = compositeMinimalViewshed;
      }

      //Clear the model that the viewshed was in
      model.removeAllElements( ILcdFireEventMode.FIRE_LATER );
      if ( viewshed != null ) {
        //Create a raster for the viewshed
        TLcdViewshedMultilevelRaster multilevelRaster = createViewshedRaster( viewshed, model.getModelReference(), viewshedReference );
        model.addElement( multilevelRaster, ILcdFireEventMode.FIRE_NOW );
      }
    }
    catch ( IllegalArgumentException exception ) {
      showErrorMessage( exception.getMessage() );
    }
  }

  /**
   * Creates a multi level viewshed raster for a given viewshed.
   *
   * @param aViewshed The viewshed for which to create a multi level raster
   * @param aViewshedModelReference The reference of the model in which the viewshed raster resides
   * @param aViewshedReference The reference of the viewshed itself
   * @return a {@link TLcdViewshedMultilevelRaster}
   */
  private TLcdViewshedMultilevelRaster createViewshedRaster( ILcdViewshed aViewshed, ILcdModelReference aViewshedModelReference, ILcdGeoReference aViewshedReference ) {
    //Create color model for viewshed raster
    TLcdJetIndexColorModelFactory colorModelFactory = new TLcdJetIndexColorModelFactory();
    colorModelFactory.setBasicColor( 0, new Color(128,128,128,150) );
    colorModelFactory.setBasicColor( 255, new Color(128,128,128,0) );
    IndexColorModel colorModel = ( IndexColorModel ) colorModelFactory.createColorModel();

    ILcdBounds bounds = ( ( ILcdBounded ) fTerrainModel ).getBounds();
    TLcdLonLatHeightBounds transformedBoundsSFCT = new TLcdLonLatHeightBounds(  );
    TLcdGeoReference2GeoReference transformation = new TLcdGeoReference2GeoReference(  );
    transformation.setSourceReference( fTerrainModel.getModelReference() );
    transformation.setDestinationReference( aViewshedModelReference );
    try {
      transformation.sourceBounds2destinationSFCT( bounds, transformedBoundsSFCT );
    } catch ( TLcdNoBoundsException e1 ) {
      throw new IllegalArgumentException( "Could not convert terrain bounds to viewshed model bounds." );
    }
    TLcdGeoReference2GeoReference modelToViewshed = ViewshedUtil.createTransformation( aViewshedModelReference, aViewshedReference );
    TLcdViewshedMultilevelRaster multilevelRaster = new TLcdViewshedMultilevelRaster( transformedBoundsSFCT, aViewshed, modelToViewshed, colorModel, TLcdSharedBuffer.getBufferInstance() );

    multilevelRaster.setHeightProvider( ViewshedUtil.createHeightProvider( aViewshedModelReference, fTerrainModel ) );

    multilevelRaster.setTargetHeight( fSunPositionPanelModel.getTargetSamplingHeightOffset() );
    return multilevelRaster;
  }

  /**
   * Creates a viewing direction for the position of the sun.
   *
   * @param aSunPosition The position of the sun projected onto the earth
   * @param aSunPositionReference The reference in which the sun position is defined in
   * @param aViewshedReference The reference of the viewshed
   * @return a {@link TLcdDirectionalViewshedObserver}
   */
  private TLcdDirectionalViewshedObserver createViewingDirection( ILcdPoint aSunPosition, ILcdGeoReference aSunPositionReference, ILcdGeoReference aViewshedReference ) {
    TLcdXYZPoint sunEarthPositionPoint1 = new TLcdXYZPoint();
    TLcdXYZPoint sunEarthPositionPoint2 = new TLcdXYZPoint();
    ILcd3DEditablePoint sunPosition1 = aSunPosition.cloneAs3DEditablePoint();
    ILcd3DEditablePoint sunPosition2 = aSunPosition.cloneAs3DEditablePoint();
    //move second point up, and transform this point as well
    sunPosition2.translate3D( 0,0,100 );
    ViewshedUtil.transform( sunPosition1, aSunPositionReference, aViewshedReference, sunEarthPositionPoint1 );
    ViewshedUtil.transform( sunPosition2, aSunPositionReference, aViewshedReference, sunEarthPositionPoint2 );
    //The viewing direction is the difference between the two points.
    ILcdPoint viewingDirectionVector = new TLcdXYZPoint( sunEarthPositionPoint1.getX()-sunEarthPositionPoint2.getX(),
                                                         sunEarthPositionPoint1.getY()-sunEarthPositionPoint2.getY(),
                                                         sunEarthPositionPoint1.getZ()-sunEarthPositionPoint2.getZ() );
    return new TLcdDirectionalViewshedObserver( viewingDirectionVector );
  }

  private static void showErrorMessage( String aMessage ) {
    JOptionPane.showMessageDialog(
              null, aMessage, ERROR_MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE
    );
  }
}
