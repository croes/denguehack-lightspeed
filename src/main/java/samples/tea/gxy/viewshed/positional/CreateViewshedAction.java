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
package samples.tea.gxy.viewshed.positional;

import com.luciad.format.raster.TLcdJetIndexColorModelFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.tea.viewshed.ILcdViewshed;
import com.luciad.tea.viewshed.TLcdBoundedCompositeViewshed;
import com.luciad.tea.viewshed.TLcdCompositeViewshed;
import com.luciad.tea.viewshed.TLcdMinimalVisibilityComposite;
import com.luciad.tea.viewshed.TLcdPositionalViewshedObserver;
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
 * the various settings in <code>EyePositionPanelModel</code>
 */
class CreateViewshedAction extends AbstractAction {
  private static final String ERROR_MESSAGE_TITLE = "Invalid input for viewshed computation.";
  private EyePositionPanelModel fEyePositionPanelModel;
  private ILcdModel fBuildingModel;
  private ILcdModel fTerrainModel;
  private ILcdGXYLayer fTargetLayer;

  public CreateViewshedAction( EyePositionPanelModel aEyePositionModel ) {
    super("Update");
    fEyePositionPanelModel = aEyePositionModel;
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

      //Create
      TLcdXYZPoint eyepointViewshedReference = new TLcdXYZPoint();
      double eyeHeight = ViewshedUtil.createHeightProvider( ( ILcdModelReference ) fEyePositionPanelModel.getEyePositionReference(), fTerrainModel ).retrieveHeightAt( fEyePositionPanelModel.getEyePosition() );
      if(Double.isNaN(eyeHeight)){
          eyeHeight=0;
      }
      eyeHeight += fEyePositionPanelModel.getEyeHeightOffset();

      ILcdGeoReference viewshedReference = ViewshedUtil.createViewshedReference( fEyePositionPanelModel.getEyePosition(), fEyePositionPanelModel.getEyePositionReference() );

      ILcdPoint eyePosition = new TLcdXYZPoint( fEyePositionPanelModel.getEyePosition().getX(), fEyePositionPanelModel.getEyePosition().getY(),eyeHeight);
      ViewshedUtil.transform( eyePosition, fEyePositionPanelModel.getEyePositionReference(), viewshedReference, eyepointViewshedReference );

      //Create viewshed observer
      TLcdPositionalViewshedObserver observerPositional = new TLcdPositionalViewshedObserver( eyepointViewshedReference );

      //Create polygon viewshed
      PolygonViewshedFactory polygonViewshedFactory = new PolygonViewshedFactory( viewshedReference, observerPositional );
      ILcdModelReference buildingModelReference = fBuildingModel.getModelReference();
      TLcdGeoReference2GeoReference buildingTransformation = ViewshedUtil.createTransformation( buildingModelReference, viewshedReference );
      ILcdViewshed polygonViewshed = polygonViewshedFactory.createViewshed( fBuildingModel, buildingTransformation, ViewshedUtil.createHeightProvider( buildingModelReference, fTerrainModel ) );

      ILcdModelReference terrainModelReference = fTerrainModel.getModelReference();
      ILcdHeightProvider terrainHeightProvider = ViewshedUtil.createHeightProvider( terrainModelReference, fTerrainModel );
      //Create terrain viewshed
      TLcdTerrainViewshedFactory terrainViewshedFactory = new TLcdTerrainViewshedFactory( viewshedReference, observerPositional,fEyePositionPanelModel.getStepSize() );
      TLcdGeoReference2GeoReference terrainTransformation = ViewshedUtil.createTransformation( terrainModelReference, viewshedReference );
      terrainViewshedFactory.setStepSize( fEyePositionPanelModel.getStepSize() );
      ILcdViewshed terrainViewshed = terrainViewshedFactory.createViewshed( terrainHeightProvider, terrainHeightProvider.getBounds(), terrainTransformation );

      //Composite the two viewsheds
      ILcdViewshed viewshed = null;
      if ( terrainViewshed instanceof TLcdBoundedCompositeViewshed && polygonViewshed instanceof TLcdBoundedCompositeViewshed ) {
        //If both viewsheds are TLcdBoundedObjectViewshed, then we can merge them into one to improve performance
        TLcdBoundedCompositeViewshed boundedObjectViewshed = new TLcdBoundedCompositeViewshed( viewshedReference, observerPositional );
        boundedObjectViewshed.addAllObjects( ( TLcdBoundedCompositeViewshed ) terrainViewshed );
        boundedObjectViewshed.addAllObjects( ( TLcdBoundedCompositeViewshed ) polygonViewshed );
        viewshed = boundedObjectViewshed;
      }
      else {
        //Fall back if the viewsheds return something else. Viewsheds will be accelerated separately.
        TLcdCompositeViewshed compositeMinimalViewshed = new TLcdCompositeViewshed();
        compositeMinimalViewshed.setComposite( new TLcdMinimalVisibilityComposite());
        compositeMinimalViewshed.addViewshed( terrainViewshed );
        compositeMinimalViewshed.addViewshed( polygonViewshed );
        viewshed = compositeMinimalViewshed;
      }

      model.removeAllElements( ILcdFireEventMode.FIRE_LATER );
      if ( viewshed != null ) {
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

    multilevelRaster.setTargetHeight( fEyePositionPanelModel.getTargetSamplingHeightOffset() );
    return multilevelRaster;
  }


  private static void showErrorMessage( String aMessage ) {
    JOptionPane.showMessageDialog(
              null, aMessage, ERROR_MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE
    );
  }
}
