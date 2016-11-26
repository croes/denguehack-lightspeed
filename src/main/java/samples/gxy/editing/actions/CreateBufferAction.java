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
package samples.gxy.editing.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdGeoBuffer;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdLonLatGeoBuffer;
import com.luciad.shape.shape2D.TLcdXYGeoBuffer;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.ILcdLayer;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Creates geo-buffers for all selected shapes.
 */
public class CreateBufferAction extends ALcdObjectSelectionAction {

  public CreateBufferAction(ILcdGXYView aView) {
    super(aView,
          new ILcdFilter<TLcdDomainObjectContext>() {
            @Override
            public boolean accept(TLcdDomainObjectContext aObject) {
              Object el = aObject.getDomainObject();
              return (el instanceof ILcdPoint ||
                      el instanceof ILcdPolyline ||
                      el instanceof ILcdPolygon ||
                      el instanceof ILcdComplexPolygon);
            }
          }, 1, -1, false);
    setName("Create geo-buffer");
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    for (TLcdDomainObjectContext domainObjectContext : aSelection) {
      ILcdShape shape = (ILcdShape) domainObjectContext.getDomainObject();

      ILcdBounds bounds = shape.getBounds();

      double bufferWidth = bounds.getWidth() + bounds.getHeight();
      ILcdModel model = domainObjectContext.getModel();
      if (model.getModelReference() instanceof ILcdGeodeticReference) {
        bufferWidth *= 5000;
      } else {
        bufferWidth /= 20;
      }
      if (bufferWidth == 0) {
        bufferWidth = 50000;
      }

      ILcdGeoBuffer geoBuffer = (model.getModelReference() instanceof ILcdGridReference) ?
                                new TLcdXYGeoBuffer(shape, bufferWidth) :
                                new TLcdLonLatGeoBuffer(shape, bufferWidth);

      model.addElement(geoBuffer, ILcdFireEventMode.FIRE_NOW);

      ILcdLayer layer = domainObjectContext.getLayer();
      layer.selectObject(shape, false, ILcdFireEventMode.FIRE_NOW);
      domainObjectContext.getLayer().fireCollectedSelectionChanges();
    }
  }
}

