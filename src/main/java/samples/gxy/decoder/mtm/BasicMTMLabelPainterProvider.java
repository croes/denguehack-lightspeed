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
package samples.gxy.decoder.mtm;

import java.awt.Color;
import java.awt.Font;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;

/**
 * A basic label painter provider that can display labels for points, polylines
 * and polygons.
 */
class BasicMTMLabelPainterProvider implements ILcdGXYLabelPainterProvider {

  /**
   * Internal label painter.
   */
  protected TLcdGXYDataObjectLabelPainter fLabelPainter = new TLcdGXYDataObjectLabelPainter();

  public BasicMTMLabelPainterProvider() {
    fLabelPainter.setFont(new Font("Lucida Sans", Font.PLAIN, 11));
    fLabelPainter.setForeground(Color.WHITE);
    fLabelPainter.setHaloColor(Color.BLACK);
    fLabelPainter.setHaloEnabled(true);
  }

  public ILcdGXYLabelPainter getGXYLabelPainter(Object aObject) {
    if (aObject instanceof ILcdDataObject) {
      ILcdDataObject data_object = (ILcdDataObject) aObject;
      fLabelPainter.setObject(aObject);
      fLabelPainter.setExpressions(data_object.getDataType().getDeclaredProperties().get(0).getName());
      return fLabelPainter;
    }

    return null;
  }

  /*
   * Gets the data object label painter.
   */
  public TLcdGXYDataObjectLabelPainter getLabelPainter() {
    return fLabelPainter;
  }

  /*
   * Sets the data object label painter.
   */
  public void setLabelPainter(TLcdGXYDataObjectLabelPainter aLabelPainter) {
    fLabelPainter = aLabelPainter;
  }

  public Object clone() {
    try {
      BasicMTMLabelPainterProvider result = (BasicMTMLabelPainterProvider) super.clone();
      result.fLabelPainter = (TLcdGXYDataObjectLabelPainter) fLabelPainter.clone();
      return result;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Cloning is not supported for this Object : " + this, e);
    }
  }
}
