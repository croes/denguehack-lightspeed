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
package samples.lightspeed.customization.hippodrome;

import java.util.Enumeration;

import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.gxy.hippodromePainter.IHippodrome;
import samples.gxy.hippodromePainter.LonLatHippodrome;
import samples.gxy.hippodromePainter.XYHippodrome;

/**
 * An <code>ALspCreateControllerModel</code> extension that provides IHippodrome objects
 * to the new controller. Depending on the mode set, it will provide geodetic (lon,lat) or
 * grid (xy) hippodromes and will wrap the IHippodrome in an extruded shape or not.
 * <p/>
 * The objects are added to layers which have as type Hippodrome and whose model reference is
 * compatible with the object created.
 */
class CreateControllerModelHippodrome extends ALspCreateControllerModel {

  public enum Mode {GEODETIC, GRID}

  private Mode fMode = Mode.GEODETIC;

  private boolean fExtruded;

  public CreateControllerModelHippodrome(Mode aMode) {
    fMode = aMode;
    fExtruded = false;
  }

  public boolean isExtruded() {
    return fExtruded;
  }

  public void setExtruded(boolean aExtruded) {
    fExtruded = aExtruded;
  }

  private Mode getMode() {
    return fMode;
  }

  @Override
  public ILspInteractivePaintableLayer getLayer(ILspView aView) {
    Enumeration<?> layers = aView.layers();

    while (layers.hasMoreElements()) {
      ILspLayer layer = (ILspLayer) layers.nextElement();

      if (layer.getModel().getModelDescriptor().getTypeName().equals("Hippodrome")) {
        if ((getMode() == Mode.GEODETIC) &&
            (layer.getModel().getModelReference() instanceof ILcdGeodeticReference) ||
            (getMode() == Mode.GRID) &&
            (layer.getModel().getModelReference() instanceof ILcdGridReference)) {
          return (ILspInteractivePaintableLayer) layer;
        }
      }
    }

    return null;
  }

  @Override
  public TLspPaintRepresentation getPaintRepresentation(ILspInteractivePaintableLayer aLayer, ILspView aView) {
    return TLspPaintRepresentation.BODY;
  }

  @Override
  public Object create(ILspView aView, ILspLayer aLayer) {
    IHippodrome hippodrome;

    switch (fMode) {
    case GEODETIC:
      hippodrome = new LonLatHippodrome();
      break;
    case GRID:
      hippodrome = new XYHippodrome();
      break;
    default:
      return null;
    }

    if (fExtruded) {
      return new TLcdExtrudedShape(hippodrome);
    } else {
      return hippodrome;
    }
  }

}
