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
package samples.decoder.asdi;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterStyle;

public class SelectionDashedLineStyle implements ILcdGXYPainterStyle {
  private Color fColor;
  private Color fSelectionColor;
  private Stroke fStroke;
  private Stroke fSelectionStroke;

  public SelectionDashedLineStyle(Color aColor, Color aSelectionColor) {
    fColor = aColor;
    fStroke = new BasicStroke(1.0f);
    fSelectionColor = aSelectionColor;
    fSelectionStroke = new BasicStroke(
        1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{10f, 10f}, 0f);
  }

  public void setupGraphics(Graphics aGraphics, Object aObject, int aMode, ILcdGXYContext aContext) {
    Graphics2D g2d = ((Graphics2D) aGraphics);
    if ((aMode & ILcdGXYPainter.SELECTED) != 0) {
      g2d.setColor(fSelectionColor);
      g2d.setStroke(fSelectionStroke);
    } else {
      g2d.setColor(fColor);
      g2d.setStroke(fStroke);
    }
  }
}
