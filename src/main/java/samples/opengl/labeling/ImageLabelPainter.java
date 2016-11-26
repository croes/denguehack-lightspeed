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
package samples.opengl.labeling;

import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.painter.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Example implementation of ALcdGLImageLabelPainter. This painter draws labels
 * consisting of a single-line string in an oval frame.
 */
class ImageLabelPainter extends ALcdGLImageLabelPainter {

  private Font fFont;

  public ImageLabelPainter(Font aFont) {
    super();
    fFont = aFont;
  }

  protected String retrieveLabel(Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext) {
    // By default, use toString() to determine the label contents.
    return aObject.toString();
  }

  protected void labelDimensionSFCT(Graphics aGraphics, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext, Dimension aLabelDimensionSFCT) {
    String str = retrieveLabel(aObject, aMode, aContext);

    // Compute the dimensions of the label string to determine the required size of the label image.
    Graphics2D graphics = (Graphics2D) aGraphics;
    graphics.setFont(fFont);
    FontMetrics fontMetrics = graphics.getFontMetrics(fFont);
    Rectangle2D r = fontMetrics.getStringBounds(str, graphics);

    // Add some extra margin to the dimensions to make room for the oval frame.
    int w = (int) r.getWidth() + 20;
    int h = (int) r.getHeight() + 20;

    aLabelDimensionSFCT.setSize(w, h);
  }

  protected void paintLabelImage(Graphics aGraphics, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext) {
    String str = retrieveLabel(aObject, aMode, aContext);

    Graphics2D graphics = (Graphics2D) aGraphics;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw the label text.
    graphics.setFont(fFont);
    FontMetrics fontMetrics = graphics.getFontMetrics(fFont);
    Rectangle2D r = fontMetrics.getStringBounds(str, graphics);
    int descent = fontMetrics.getMaxDescent();
    int w = (int) r.getWidth() + 20;
    int h = (int) r.getHeight() + 20;
    graphics.setColor(aMode.isPaintAsSelected() ? Color.red : Color.blue);
    graphics.drawString(str, 11, h - 11 - descent);
    graphics.drawOval(0, 0, w - 1, h - 1);
  }
}
