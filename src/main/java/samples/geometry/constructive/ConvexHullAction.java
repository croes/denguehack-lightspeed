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

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geometry.ILcdConstructiveGeometry;
import com.luciad.geometry.ellipsoidal.TLcdEllipsoidalConstructiveGeometry;
import com.luciad.model.ILcdModel;
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

public class ConvexHullAction extends ALcdObjectSelectionAction {

  private TLcdGXYDragRectangleController fSelectionController;

  private ILcdLayer fTargetLayer; //the layer to which the resulting shapes are added

  public ConvexHullAction( ILcdLayer aTargetLayer, TLcdGXYDragRectangleController aController, ILcdGXYView aView ) {
    super( aView,
           new ILcdFilter<TLcdDomainObjectContext>() {
             @Override
             public boolean accept( TLcdDomainObjectContext aObject ) {
               return aObject.getDomainObject() instanceof ILcdShape;
             }
           }, 1, -1, false );
    setName( "Convex Hull" );
    fTargetLayer = aTargetLayer;
    fSelectionController = aController;
  }

  @Override
  protected void actionPerformed( ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection ) {
    List<ILcdShape> shapes = new ArrayList<ILcdShape>();
    for ( TLcdDomainObjectContext domainObjectContext : aSelection ) {
      shapes.add( ( ILcdShape ) domainObjectContext.getDomainObject() );
    }

    ILcdConstructiveGeometry geometry = new TLcdEllipsoidalConstructiveGeometry(new TLcdEllipsoid(), 1e-10);

    ILcdShape hull = geometry.convexHull( shapes.toArray( new ILcdShape[shapes.size()] ) );

    fTargetLayer.getModel().addElement( hull, ILcdModel.FIRE_NOW );

    //select the new object, deselect all other objects
    for ( Enumeration layers = fSelectionController.getGXYView().layers(); layers.hasMoreElements(); ) {
      ILcdGXYLayer layer = ( ILcdGXYLayer ) layers.nextElement();
      for ( Enumeration selection = layer.selectedObjects(); selection.hasMoreElements(); ) {
        layer.selectObject( selection.nextElement(), false, ILcdFireEventMode.FIRE_LATER );
      }
    }
    fTargetLayer.selectObject( hull, true, ILcdFireEventMode.FIRE_LATER );
    fTargetLayer.fireCollectedSelectionChanges();
  }
}
