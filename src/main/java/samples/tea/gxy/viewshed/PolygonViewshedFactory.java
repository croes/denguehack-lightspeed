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
package samples.tea.gxy.viewshed;

import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.tea.viewshed.ALcdViewshedObserver;
import com.luciad.tea.viewshed.ILcdViewshed;
import com.luciad.tea.viewshed.TLcdBoundedCompositeViewshed;
import com.luciad.tea.viewshed.TLcdExtrudedPolygonViewshedFactory;
import com.luciad.transformation.ILcdModelModelTransformation;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.height.ILcdHeightProvider;

import java.util.Enumeration;

/**
 * The <code>PolygonViewshedFactory</code> acts as a convenience class to
 * add a model of polygons to a <code>TLcdBoundedObjectViewshed</code> It does
 * so by taking each polygon, wrapping it in an extruded polygon and offsetting
 * its height according to the given height provider.
 */
public class PolygonViewshedFactory {

  private TLcdExtrudedPolygonViewshedFactory fExtrudedPolygonViewshedFactory;
  private ILcdGeoReference fViewshedReference;
  private ALcdViewshedObserver fViewshedObserver;

  public PolygonViewshedFactory( ILcdGeoReference aViewshedReference, ALcdViewshedObserver aViewshedObserver ) {
    fViewshedReference = aViewshedReference;
    fViewshedObserver = aViewshedObserver;
    fExtrudedPolygonViewshedFactory = new TLcdExtrudedPolygonViewshedFactory( aViewshedReference, aViewshedObserver );
  }

  /**
   * Creates viewshed for model consisting of polygons to a viewshed, after extruding each element and
   * adjusting its height to match the given height provider.
   * @param aModel A model containing heights
   * @param aTransformation a transformation that transforms each element of the model to a geocentric
   * @param aHeightProvider a height provider that gives the offset of each extruded shape.
   * @return A viewshed
   */
  public ILcdViewshed createViewshed( ILcdModel aModel, ILcdModelModelTransformation aTransformation, ILcdHeightProvider aHeightProvider ) {
    TLcdBoundedCompositeViewshed boundedObjectViewshed = new TLcdBoundedCompositeViewshed( fViewshedReference, fViewshedObserver );
    Enumeration elements = aModel.elements();
    while ( elements.hasMoreElements() ) {
      Object domainObject = elements.nextElement();
      if ( domainObject instanceof ILcdShapeList ) {
        int shapeCount = ( ( ILcdShapeList ) domainObject ).getShapeCount();
        for ( int i = 0; i < shapeCount; i++ ) {
          if ( ( ( ILcdShapeList ) domainObject ).getShape( i ) instanceof ILcdComplexPolygon ) {
            ILcdComplexPolygon complexPolygon = ( ILcdComplexPolygon ) ( ( ILcdShapeList ) domainObject ).getShape( i );
            for ( int j = 0; j < complexPolygon.getPolygonCount(); ++j ) {
              ILcdPolygon polygon = complexPolygon.getPolygon( j );
              ILcdBounds polygonBounds = polygon.getBounds();
              double buildingHeight = retrieveBuildingHeight( polygonBounds );
              double terrainHeight = retrieveTerrainHeight( aHeightProvider, polygonBounds );
              TLcdExtrudedShape extrudedShape = new TLcdExtrudedShape( polygon, terrainHeight, buildingHeight + terrainHeight );

              try {
                ILcdBounds bounds = extrudedShape.getBounds();
                TLcdXYZBounds geocentricBounds = new TLcdXYZBounds();
                aTransformation.sourceBounds2destinationSFCT( bounds, geocentricBounds );
                ILcdViewshed viewshed = fExtrudedPolygonViewshedFactory.createViewshed( extrudedShape, aTransformation );
                if ( viewshed != null ) {
                  boundedObjectViewshed.addBoundedObject( extrudedShape, aTransformation, viewshed );
                }
              } catch ( TLcdNoBoundsException e ) {
                e.printStackTrace();
              }
            }
          }
        }
      }
    }
    return boundedObjectViewshed;
  }


  private double retrieveTerrainHeight( ILcdHeightProvider aHeightProvider, ILcdBounds aPolygonBounds ) {
    double terrainHeight = 0;
    if(aHeightProvider!=null){
      terrainHeight = aHeightProvider.retrieveHeightAt( aPolygonBounds.getFocusPoint() );
      if(Double.isNaN( terrainHeight )){
        terrainHeight=0;
      }
    }
    return terrainHeight;
  }

  private double retrieveBuildingHeight( ILcdBounds aPolygonBounds ) {
    double buildingHeight = aPolygonBounds.getWidth()* aPolygonBounds.getHeight();
    buildingHeight = Math.sqrt( buildingHeight );
    buildingHeight/=3.d;
    buildingHeight = Math.min( buildingHeight, 53.0 );
    buildingHeight = Math.max( buildingHeight, 3.0d );
    return buildingHeight;
  }
}
