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
package samples.common;

import java.awt.Color;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;

public class MapColors {

  public static final Color SELECTION = new Color(255, 165, 0, 200);

  public static final Color INTERACTIVE_OUTLINE = Color.white;
  public static final Color INTERACTIVE_FILL = new Color(58, 179, 71, 200);

  public static final Color BACKGROUND_FILL = new Color(100, 100, 100, 128);
  public static final Color BACKGROUND_OUTLINE = new Color(100, 100, 100);
  public static final Color SELECTION_FILL = new Color(SELECTION.getRed(), SELECTION.getGreen(), SELECTION.getBlue(), 100);
  public static final Color SELECTION_OUTLINE = new Color(255, 140, 0, 100);

  public static final Color ICON_OUTLINE = new Color(155, 180, 222, 200);
  public static final Color ICON_FILL = new Color(0, 78, 146, 200);
  public static final Color ICON_SELECTION = SELECTION;

  public static final Color LABEL = new Color(244, 247, 255, 200);
  public static final Color LABEL_HALO = new Color(73, 70, 70, 200);

  public static final Color GRID_PROMINENT_COLOR = new Color(1f, 1f, 1f, 0.5f);
  public static final Color GRID_ALTERNATE_COLOR = new Color(1f, 1f, 1f, 0.625f);
  public static final Color GRID_EXCEPTIONAL_COLOR = new Color(1f, 1f, 1f, 0.9f);

  public static final Color GRID_PROMINENT_LABEL_COLOR = new Color(0f, 0f, 0f, 0.5f);
  public static final Color GRID_ALTERNATE_LABEL_COLOR = new Color(0f, 0f, 0f, 0.7f);
  public static final Color GRID_EXCEPTIONAL_LABEL_COLOR = new Color(0f, 0f, 0f, 0.8f);

  private static final int SNAP_POINT_SIZE = 12;
  private static final Color SNAP_COLOR = new Color(198, 143, 40);

  public static ILcdIcon createIcon(boolean aSelection) {
    TLcdSymbol symbol = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 15, aSelection ? ICON_SELECTION : ICON_OUTLINE, ICON_FILL);
    symbol.setAntiAliasing(true);
    return symbol;
  }

  public static ILcdIcon createInteractiveIcon(boolean aSelection) {
    TLcdSymbol symbol = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 15, INTERACTIVE_OUTLINE, aSelection ? ICON_SELECTION : INTERACTIVE_FILL);
    symbol.setAntiAliasing(true);
    return symbol;
  }

  public static ILcdIcon createSnapIcon() {
    return new TLcdSymbol(TLcdSymbol.CROSS_RECT, SNAP_POINT_SIZE, SNAP_COLOR);
  }
}
