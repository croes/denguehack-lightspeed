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
package samples.geometry.constructive;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geometry.ILcdConstructiveGeometry;
import com.luciad.geometry.cartesian.TLcdCartesianConstructiveGeometry;
import com.luciad.geometry.ellipsoidal.TLcdEllipsoidalConstructiveGeometry;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.ILcdLayer;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYDragRectangleController;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ConstructiveGeometryAction extends ALcdObjectSelectionAction {

  private TLcdGXYDragRectangleController fSelectionController;

  private Type fType;

  private ILcdLayer fTargetLayer; //the layer to which the resulting shapes are added

  public enum Type {
    INTERSECTION("Intersection"),
    UNION("Union"),
    DIFFERENCE("Difference"),
    INVERTED_DIFFERENCE("Inverted Difference"),
    SYMMETRIC_DIFERENCE("Symmetric Difference");

    public final String fName;

    Type(String aName) {
      fName = aName;
    }
  }

  public ConstructiveGeometryAction( ILcdLayer aTargetLayer, Type aType, TLcdGXYDragRectangleController aController, ILcdGXYView aView ) {
    super( aView,
           new ILcdFilter<TLcdDomainObjectContext>() {
             @Override
             public boolean accept( TLcdDomainObjectContext aObject ) {
               return aObject.getDomainObject() instanceof ILcdShape;
             }
           }, 2, -1, false );
    setName( aType.fName );
    fTargetLayer = aTargetLayer;
    fSelectionController = aController;
    fType = aType;
  }

  @Override
  protected void actionPerformed( ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection ) {
    ILcdModelReference ref = aSelection.get( 0 ).getModel().getModelReference();
    List<ILcdShape> shapes = new ArrayList<ILcdShape>();
    for ( TLcdDomainObjectContext domainObjectContext : aSelection ) {
      shapes.add( ( ILcdShape ) domainObjectContext.getDomainObject() );
    }

    ILcdConstructiveGeometry geometry;
    if( ref instanceof ILcdGeodeticReference ) {
      ILcdGeodeticReference geoReference = (ILcdGeodeticReference)ref;
      ILcdEllipsoid ellipsoid = geoReference.getGeodeticDatum().getEllipsoid();
      geometry = new TLcdEllipsoidalConstructiveGeometry( ellipsoid, 1e-10 );
    }
    else {
      geometry = new TLcdCartesianConstructiveGeometry( 1e-4 );
    }

    ILcdShape constructed = null;

    if(fType == Type.DIFFERENCE || fType == Type.INVERTED_DIFFERENCE) {
      /*
      To keep the difference and inverted difference operations consistent, the position in the
      world is used to determine which is the first and second shape for the subtraction. For the
      difference operation, the other shapes will be subtracted from the leftmost shape. For
      the inverted difference operation, the other shapes will be subtracted from the rightmost shape.
       */
      ILcdShape positiveShape = shapes.get(0);
      int index = 0;

      for(int i = 1; i < shapes.size(); i++) {
        ILcdShape test = shapes.get(i);

        boolean left = positiveShape.getBounds().getLocation().getX() < test.getBounds().getLocation().getX()
                       || positiveShape.getBounds().getLocation().getX() == test.getBounds().getLocation().getX()
                          && shapes.get(0).getBounds().getLocation().getY() < test.getBounds().getLocation().getY();

        if( (left && fType == Type.INVERTED_DIFFERENCE) || (!left && fType == Type.DIFFERENCE)) {
          positiveShape = test;
          index = i;
        }

        //put the positive shape in front of the array so it'll be the positive shape of the difference operation
        shapes.set( index, shapes.get(0) );
        shapes.set(0, positiveShape);
      }
    }


    ILcdShape[] shapeArray = shapes.toArray(new ILcdShape[shapes.size()]);

    switch ( fType ) {
      case INTERSECTION:
        constructed = geometry.intersection( shapeArray );
        break;
      case UNION:
        constructed = geometry.union( shapeArray );
        break;
      case DIFFERENCE:
      case INVERTED_DIFFERENCE:
        constructed = geometry.difference( shapeArray );
        break;
      case SYMMETRIC_DIFERENCE:
        constructed = geometry.symmetricDifference( shapeArray );
        break;
    }

    if ( constructed == null) return;


    fTargetLayer.getModel().addElement( constructed, ILcdModel.FIRE_NOW );

    //select the new object, deselect all other objects
    for ( Enumeration layers = fSelectionController.getGXYView().layers(); layers.hasMoreElements(); ) {
      ILcdGXYLayer layer = ( ILcdGXYLayer ) layers.nextElement();
      for ( Enumeration selection = layer.selectedObjects(); selection.hasMoreElements(); ) {
        layer.selectObject( selection.nextElement(), false, ILcdFireEventMode.FIRE_LATER );
      }
    }
    fTargetLayer.selectObject( constructed, true, ILcdFireEventMode.FIRE_LATER );
    fTargetLayer.fireCollectedSelectionChanges();
  }
}
