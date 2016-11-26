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
package samples.fusion.client.common;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeListener;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdRasterPainter;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;

/**
 * A painter that can draw both single and multilevel rasters.
 */
public class RasterPainter implements ILcdGXYPainter, ILcdGXYPainterProvider {

  private ILcdRasterPainter fRasterPainter;
  private ILcdRasterPainter fMultilevelRasterPainter;
  private Object fObject;

  public RasterPainter(ILcdRasterPainter aRasterPainter, ILcdRasterPainter aMultilevelRasterPainter) {
    fRasterPainter = aRasterPainter;
    fMultilevelRasterPainter = aMultilevelRasterPainter;
  }

  public void setObject(Object aObject) {
    fObject = aObject;
    if (fObject instanceof ILcdRaster) {
      fRasterPainter.setObject(aObject);
    } else if (fObject instanceof ILcdMultilevelRaster) {
      fMultilevelRasterPainter.setObject(aObject);
    }
  }

  public Object getObject() {
    return fObject;
  }

  public void paint(Graphics aGraphics, int aIndex, ILcdGXYContext aILcdGXYContext) {
    if (fObject instanceof ILcdRaster) {
      fRasterPainter.paint(aGraphics, aIndex, aILcdGXYContext);
    } else if (fObject instanceof ILcdMultilevelRaster) {
      fMultilevelRasterPainter.paint(aGraphics, aIndex, aILcdGXYContext);
    }
  }

  public void boundsSFCT(Graphics aGraphics, int aIndex, ILcdGXYContext aContext,
                         ILcd2DEditableBounds a2DEditableBounds) throws TLcdNoBoundsException {
    if (fObject instanceof ILcdRaster) {
      fRasterPainter.boundsSFCT(aGraphics, aIndex, aContext, a2DEditableBounds);
    } else if (fObject instanceof ILcdMultilevelRaster) {
      fMultilevelRasterPainter.boundsSFCT(aGraphics, aIndex, aContext, a2DEditableBounds);
    }
  }

  public boolean isTouched(Graphics aGraphics, int aIndex, ILcdGXYContext aContext) {
    return false;
  }

  public void anchorPointSFCT(Graphics aGraphics, int i, ILcdGXYContext aContext, Point aPoint)
      throws TLcdNoBoundsException {
    if (fObject instanceof ILcdRaster) {
      fRasterPainter.anchorPointSFCT(aGraphics, i, aContext, aPoint);
    } else if (fObject instanceof ILcdMultilevelRaster) {
      fMultilevelRasterPainter.anchorPointSFCT(aGraphics, i, aContext, aPoint);
    }
  }

  public boolean supportSnap(Graphics aGraphics, ILcdGXYContext aContext) {
    return false;
  }

  public Object snapTarget(Graphics aGraphics, ILcdGXYContext aContext) {
    return null;
  }

  public Cursor getCursor(Graphics aGraphics, int aIndex, ILcdGXYContext aContext) {
    return Cursor.getDefaultCursor();
  }

  public String getDisplayName() {
    return "Raster painter";
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fRasterPainter.addPropertyChangeListener(aPropertyChangeListener);
    fMultilevelRasterPainter.addPropertyChangeListener(aPropertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fRasterPainter.removePropertyChangeListener(aPropertyChangeListener);
    fMultilevelRasterPainter.removePropertyChangeListener(aPropertyChangeListener);
  }

  public Object clone() {
    try {
      RasterPainter clone = (RasterPainter) super.clone();
      clone.fRasterPainter = (ILcdRasterPainter) fRasterPainter.clone();
      clone.fMultilevelRasterPainter = (ILcdRasterPainter) fMultilevelRasterPainter.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    setObject(aObject);
    return this;
  }
}
