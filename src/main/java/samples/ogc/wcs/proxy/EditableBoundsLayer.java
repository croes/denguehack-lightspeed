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
package samples.ogc.wcs.proxy;

import java.awt.Color;
import java.awt.Graphics;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYBoundsPainter;
import com.luciad.view.map.TLcdGeodeticPen;

/**
 * An extension of TLcdGXYLayer that places an editable bounds object on the map.
 */
class EditableBoundsLayer extends TLcdGXYLayer {

  private TLcdLonLatBounds fLonLatBounds = null;
  private TLcdGXYBoundsPainter fGXYBoundsPainter = new TLcdGXYBoundsPainter();
  private int fLineWidth = 3;
  private Color fDefaultColor = Color.orange;
  private Color fSelectionColor = Color.cyan;

  public EditableBoundsLayer(double aLongitude, double aLatitude, double aWidth, double aHeight) {

    fLonLatBounds = new TLcdLonLatBounds(aLongitude, aLatitude, aWidth, aHeight);

    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.addElement(fLonLatBounds, ILcdFireEventMode.NO_EVENT);

    this.setModel(model);
    this.setLabel("Bounds");
    this.setSelectable(true);
    this.setEditable(true);
    this.setGXYPen(new TLcdGeodeticPen(false));

    fGXYBoundsPainter.setLineStyle(new MyLineStyle());
    this.setGXYPainterProvider(fGXYBoundsPainter);
    this.setGXYEditorProvider(fGXYBoundsPainter);
  }

  public void setLonLatBounds(TLcdLonLatBounds aLonLatBounds) {
    getModel().removeElement(fLonLatBounds, ILcdFireEventMode.FIRE_LATER);
    fLonLatBounds = aLonLatBounds;
    getModel().addElement(fLonLatBounds, ILcdFireEventMode.FIRE_NOW);
  }

  public TLcdLonLatBounds getLonLatBounds() {
    return fLonLatBounds;
  }

  class MyLineStyle implements ILcdGXYPainterStyle {
    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {
      TLcdAWTUtil.changeStrokeToPlainLine(aGraphics, fLineWidth);
      if (((aMode & ILcdGXYPainter.SELECTED) != 0) &&
          (fSelectionColor != null)) {
        aGraphics.setColor(fSelectionColor);
      } else if (fDefaultColor != null) {
        aGraphics.setColor(fDefaultColor);
      }
    }
  }
}
