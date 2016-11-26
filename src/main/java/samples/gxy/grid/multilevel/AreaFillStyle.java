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
package samples.gxy.grid.multilevel;

import java.awt.Color;
import java.awt.Graphics;

import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainterStyle;

/**
 * A fill style for grid coordinates that can have a status, where the color changes depending on the status of
 * the coordinate.
 */
public class AreaFillStyle implements ILcdGXYPainterStyle {

  private static final Color HOSTILE_COLOR = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 150);
  private static final Color CLEAR_COLOR = new Color(Color.green.getRed(), Color.green.getGreen(), Color.green.getBlue(), 150);
  private static final Color UNKOWN_COLOR = new Color(Color.orange.getRed(), Color.orange.getGreen(), Color.orange.getBlue(), 150);

  public void setupGraphics(Graphics graphics, Object object, int aPaintMode, ILcdGXYContext aGXYContext) {
    if (object instanceof samples.gxy.grid.multilevel.StatusedEditableMultilevelGridCoordinate) {
      samples.gxy.grid.multilevel.StatusedEditableMultilevelGridCoordinate coordinate = (samples.gxy.grid.multilevel.StatusedEditableMultilevelGridCoordinate) object;
      String status = coordinate.getStatus();
      if (status.equalsIgnoreCase(samples.gxy.grid.multilevel.StatusedEditableMultilevelGridCoordinate.HOSTILE)) {
        graphics.setColor(HOSTILE_COLOR);
      } else if (status.equalsIgnoreCase(samples.gxy.grid.multilevel.StatusedEditableMultilevelGridCoordinate.CLEAR)) {
        graphics.setColor(CLEAR_COLOR);
      } else { // status = unknown
        graphics.setColor(UNKOWN_COLOR);
      }
    }
  }
}
