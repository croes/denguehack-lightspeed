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
package samples.gxy.labels.placement;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.TLcdGXYCurvedPathLabelPainter;

class AntiAliasedCurvedPathLabelPainter extends TLcdGXYCurvedPathLabelPainter {

  public AntiAliasedCurvedPathLabelPainter() {
    super();
  }

  public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    // Make sure the labels are anti-aliased
    Graphics2D g2d = (Graphics2D) aGraphics;
    RenderingHints rendering_hints = g2d.getRenderingHints();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    super.paintLabel(aGraphics, aMode, aGXYContext);

    // Restore the rendering hints
    g2d.setRenderingHints(rendering_hints);
  }
}
