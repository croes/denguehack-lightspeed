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
package samples.geometry.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.luciad.geometry.ILcd2DAdvancedBinaryTopology;
import com.luciad.geometry.ILcdIntersectionMatrix;
import com.luciad.geometry.ILcdIntersectionMatrixPattern;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.TLcdFeaturedShapeList;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.TLcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;

/**
 * Selection listener that calculates topological relations of all other objects with the selected
 * object.
 */
public class TopologySelectionListener implements ILcdSelectionListener {

  private ArrayList<TLcdFeaturedShapeList> fObjects = new ArrayList<TLcdFeaturedShapeList>();
  private ArrayList<TLcdLayer> fLayers = new ArrayList<TLcdLayer>();
  private List<TopologyPainterProvider> fPainterProviders = new ArrayList<>();

  ArrayList<TLcdFeaturedShapeList> fPreviousSelectedObjects;

  ILcd2DAdvancedBinaryTopology fTopology;

  public void addPainterProvider(TopologyPainterProvider aPainterProvider) {
    fPainterProviders.add(aPainterProvider);
  }
  public void setTopology( ILcd2DAdvancedBinaryTopology aTopology ) {
    fTopology = aTopology;
  }

  public void selectionChanged( TLcdSelectionChangedEvent aSelectionEvent ) {
    ArrayList<TLcdFeaturedShapeList> selectedObjects = new ArrayList<TLcdFeaturedShapeList>();

    for ( int i = 0; i < fLayers.size(); i++ ) {
      Enumeration elements = fLayers.get( i ).selectedObjects();

      while ( elements.hasMoreElements() ) {
        Object object = elements.nextElement();
        TLcdFeaturedShapeList shapeList = ( TLcdFeaturedShapeList ) object;
        selectedObjects.add( shapeList );
      }
    }

    /*
    Since this selection listener can be added to multiple layers, it can happen that a selection event
    is received for each layer, when the user did only one change, selecting some objects in one layer,
    deselecting them in the other. The topological calculations are done for everything at once. Avoid
    doing these calculations multiple times, by returning when it is detected that the selection is
    the same as when the previous event was received.
    */
    if ( fPreviousSelectedObjects != null && fPreviousSelectedObjects.size() == selectedObjects.size() ) {
      boolean identical = true;
      for ( int i = 0; i < selectedObjects.size(); i++ ) {
        if ( fPreviousSelectedObjects.get( i ) != selectedObjects.get( i ) ) {
          identical = false;
          break;
        }
      }
      if ( identical ) {
        return; //return, because the selection is the same as before, nothing has changed
      }
    }

    fPreviousSelectedObjects = selectedObjects;

    clearRelations();

    for ( int i = 0; i < fObjects.size(); i++ ) {
      TopologyPainterProvider.Relation relation = TopologyPainterProvider.Relation.DEFAULT;

      for ( int j = 0; j < selectedObjects.size(); j++ ) {


        TLcdFeaturedShapeList shapeList1 = fObjects.get( i );
        TLcdFeaturedShapeList shapeList2 = selectedObjects.get( j );

        //don't check an object with itself
        if ( shapeList1 == shapeList2 ) {
          continue;
        }

        /*
        The geometry framework doesn't support calculating topology relations between TLcdFeaturedShapeList.
        Extract the actual shapes out of the shape lists and calculate the relation for the complete
        TLcdFeaturedShapeList based on the relations between all of their shapes.
        */
        for ( int k = 0; k < shapeList1.getShapeCount(); k++ ) {
          for ( int l = 0; l < shapeList2.getShapeCount(); l++ ) {
            ILcdShape shape1 = shapeList1.getShape( k );
            ILcdShape shape2 = shapeList2.getShape( l );

            ILcdIntersectionMatrixPattern contains_or_within = new ILcdIntersectionMatrixPattern() {
              public boolean matches( ILcdIntersectionMatrix aIntersectionMatrix ) {
                return CONTAINS.matches( aIntersectionMatrix )
                    || WITHIN.matches( aIntersectionMatrix );
              }
            };

            if ( fTopology.checkTopology( shape1, shape2, ILcdIntersectionMatrixPattern.CROSSES ) ) {
              relation = TopologyPainterProvider.Relation.CROSSES;
            }
            if ( relation != TopologyPainterProvider.Relation.CROSSES && fTopology.checkTopology( shape1, shape2, ILcdIntersectionMatrixPattern.TOUCHES ) ) {
              relation = TopologyPainterProvider.Relation.TOUCHES;
            }
            if ( relation != TopologyPainterProvider.Relation.CROSSES && relation != TopologyPainterProvider.Relation.TOUCHES && fTopology.checkTopology( shape1, shape2, contains_or_within ) ) {
              relation = TopologyPainterProvider.Relation.CONTAIN_WITHIN;
            }
            //check different topological relations and color the object if a certain relation is true
            if ( relation == TopologyPainterProvider.Relation.DEFAULT && fTopology.checkTopology( shape1, shape2, ILcdIntersectionMatrixPattern.INTERSECTS ) ) {
              relation = TopologyPainterProvider.Relation.INTERSECTS;
            }
          }
        }
      }

      setRelation( fObjects.get( i ), relation );
    }

    //paint all selected objects with the selected color, even if they had some relation to other selected objects.
    for ( int i = 0; i < selectedObjects.size(); i++ ) {
      setRelation( selectedObjects.get( i ), TopologyPainterProvider.Relation.SELECTED );
    }

    //invalidate the layers so that the new painter color settings become visible
    for ( int i = 0; i < fLayers.size(); i++ ) {
      fLayers.get( i ).invalidate();
    }
  }

  private void setRelation( Object aObject, TopologyPainterProvider.Relation aRelation ) {
    for ( TopologyPainterProvider painterProvider : fPainterProviders ) {
      painterProvider.setRelation( aRelation, aObject );
    }
  }

  private void clearRelations() {
    for ( TopologyPainterProvider painterProvider : fPainterProviders ) {
      painterProvider.clearRelations();
    }
  }

  /**
   * Initialize all objects in the layer. This method must be called before this selection listener
   * is added to the layer as selection listener. It's allowed to register multiple layers and add
   * this listener to multiple layers. All objects of the layer must be TLcdFeaturedShapeList
   * instances.
   *
   * @param aLayer The layer to register
   */
  public void registerLayer( ILcdGXYLayer aLayer ) {

    fLayers.add((TLcdLayer) aLayer);

    Enumeration state_elements = aLayer.getModel().elements();
    while ( state_elements.hasMoreElements() ) {
      Object o = state_elements.nextElement();
      fObjects.add( ( TLcdFeaturedShapeList ) o );
    }
  }
}

