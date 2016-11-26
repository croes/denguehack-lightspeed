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
package samples.opengl.selectionmodel;

import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.controller.*;

import java.awt.event.MouseEvent;
import java.util.*;

/**
 * A selection model that selects the rearmost object from the list of potential
 * objects to select.
 */
class RearmostSelectionModel implements ILcdGLSelectionModel {


  public void chooseAndSelectObject( TLcdGLHitRecord[] aPotentialSelection,
                                     MouseEvent aMouseEvent,
                                     ILcdGLView aGLView ) {
    // The select controller returns hit records for all objects it encounters,
    // even those that are contained in layers that are not selectable. To
    // avoid selecting non-selectable objects, we filter the potential selection
    // list here.
    ArrayList potentialSelection = new ArrayList();
    for ( int i = 0; i < aPotentialSelection.length ; i++ ) {
      TLcdGLHitRecord record = aPotentialSelection[ i ];
      if ( record != null && record.getLayer().isSelectable() )
        potentialSelection.add( record );
    }

    // Loop over each hit record finding that record containing the largest
    // maxZ value. This record contains a reference to the object
    // that lies the furthest away from the camera and also overlaps
    // with the click position.
    TLcdGLHitRecord recordToSelect = null;
    long maxz = 0L;
    Iterator potentialSelectionIterator = potentialSelection.iterator();
    while ( potentialSelectionIterator.hasNext() ) {
      TLcdGLHitRecord record = (TLcdGLHitRecord) potentialSelectionIterator.next();
      if ( record.getMaxz() > maxz ) {
        maxz = record.getMaxz();
        recordToSelect = record;
      }
    }

    // If a valid record was found...
    if ( recordToSelect != null ) {
      // Toggle the selected state of the object contained in the record
      ILcdGLLayer layer = recordToSelect.getLayer();
      Object object = recordToSelect.getObject();
      layer.selectObject(
              object,
              !layer.isSelected( object ),
              ILcdFireEventMode.FIRE_NOW
      );
    }
  }
}
