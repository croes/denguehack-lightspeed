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
package samples.lightspeed.demo.framework.gui;

import java.awt.Color;

import samples.common.UIColors;

/**
 * Collection of colors used in the Lightspeed demo.
 */
public class DemoUIColors {
  /**
   * Color for panels overlayed on the map. Partially transparent.
   *
   * @see #PANEL_BORDER_COLOR
   */
  public static final Color PANEL_COLOR = UIColors.bg();
  public static final Color SEMI_TRANSPARENT_PANEL_COLOR = new Color(PANEL_COLOR.getRed(), PANEL_COLOR.getGreen(), PANEL_COLOR.getBlue(), (int) (0.7 * 255));
  /**
   * Color for the border of the panels, overlayed on the map.
   *
   * @see #PANEL_COLOR
   */
  public static final Color PANEL_BORDER_COLOR = UIColors.mid(UIColors.fg(), PANEL_COLOR, 0.5);

  public static final Color INFO_PANEL_COLOR = UIColors.bg();

  public static final Color SUB_PANEL_COLOR = UIColors.bgHighlight();

  public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

  public static final Color HIGHLIGHTED_TEXT_COLOR = new Color(192, 217, 42);

  public static final Color HYPSOMETRY_ARROW_COLOR = new Color(192, 217, 42);
}
