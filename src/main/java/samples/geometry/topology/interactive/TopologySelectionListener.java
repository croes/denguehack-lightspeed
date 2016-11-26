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
package samples.geometry.topology.interactive;

import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.gxy.ILcdGXYLayer;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Selection listener that calculates topological relations for two selected shapes.
 */
public class TopologySelectionListener implements ILcdSelectionListener {

  TopologyRelationProvider fTopologyRelationProvider;
  ILcdGXYLayer fLayer;

  public TopologySelectionListener( TopologyRelationProvider aTopologyRelationProvider,
                                    ILcdGXYLayer aLayer ) {
    fTopologyRelationProvider = aTopologyRelationProvider;
    fLayer = aLayer;
  }


  public void selectionChanged( TLcdSelectionChangedEvent aSelectionEvent ) {

      ArrayList<ILcdShape> selectedShapes = new ArrayList<ILcdShape>();
    Enumeration elements = fLayer.selectedObjects();
    while ( elements.hasMoreElements() ) {
      selectedShapes.add( ( ILcdShape ) elements.nextElement() );
    }

    if ( selectedShapes.size() != 2 ) {
      fTopologyRelationProvider.setDefaultText();
      fTopologyRelationProvider.setShapes( null, null );
    }
    else {
      fTopologyRelationProvider.setShapes( selectedShapes.get( 0 ), selectedShapes.get( 1 ) );
      fTopologyRelationProvider.updateRelations();
    }
  }

}

