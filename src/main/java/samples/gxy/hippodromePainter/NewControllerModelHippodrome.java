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
package samples.gxy.hippodromePainter;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;

/**
 * A <code>ALcdGXYNewControllerModel2</code> extension that provides IHippodrome objects
 * to the new controller. Depending on the mode set, it will provide geodetic (lon,lat) or
 * grid (xy) hippodromes.
 * <p/>
 * The objects are added to layers which have as type Hippodrome and whose modelreference is
 * compatible with the object created.
 */
public class NewControllerModelHippodrome extends ALcdGXYNewControllerModel2 {

  public enum Mode {GEODETIC, GRID}

  private Mode fMode = Mode.GEODETIC;

  public NewControllerModelHippodrome(Mode aMode) {
    fMode = aMode;
  }

  private Mode getMode() {
    return fMode;
  }

  public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    switch (fMode) {
    case GEODETIC:
      return new LonLatHippodrome();
    case GRID:
      return new XYHippodrome();
    default:
      return null;
    }
  }

  public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    Enumeration layers = aContext.getGXYView().layers();

    ILcdGXYLayer result = null;

    while (layers.hasMoreElements() && result == null) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();

      if (layer.getModel().getModelDescriptor().getTypeName().equals("Hippodrome")) {
        if ((getMode() == Mode.GEODETIC) && (layer.getModel().getModelReference() instanceof ILcdGeodeticReference)) {
          result = layer;
        } else if ((getMode() == Mode.GRID) && (layer.getModel().getModelReference() instanceof ILcdGridReference)) {
          result = layer;
        }
      }

    }

    return result;
  }

}
