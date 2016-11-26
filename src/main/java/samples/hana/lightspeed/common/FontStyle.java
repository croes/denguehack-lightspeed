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
package samples.hana.lightspeed.common;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.JLabel;

import samples.common.HaloLabel;

public enum FontStyle {
  H1, H2, NORMAL, SMALL_BOLD;

  private static final HashMap<FontStyle, Integer> SIZES = new HashMap<FontStyle, Integer>();
  private static final HashMap<FontStyle, Integer> STYLES = new HashMap<FontStyle, Integer>();
  private static final HashMap<FontStyle, Color> COLORS = new HashMap<FontStyle, Color>();

  static {
    SIZES.put(H1, 18);
    SIZES.put(H2, 14);
    SIZES.put(NORMAL, 12);
    SIZES.put(SMALL_BOLD, 10);

    STYLES.put(H1, Font.BOLD);
    STYLES.put(H2, Font.BOLD);
    STYLES.put(NORMAL, Font.PLAIN);
    STYLES.put(SMALL_BOLD, Font.BOLD);

    COLORS.put(H1, ColorPalette.text);
    COLORS.put(H2, ColorPalette.text);
    COLORS.put(NORMAL, ColorPalette.text);
    COLORS.put(SMALL_BOLD, ColorPalette.text);
  }

  public static Font getFont(FontStyle aStyle) {
    return new JLabel().getFont().deriveFont(STYLES.get(aStyle), SIZES.get(aStyle));
  }

  public static HaloLabel createHaloLabel(String aText, FontStyle aStyle) {
    return createHaloLabel(aText, aStyle, false);
  }

  public static HaloLabel createHaloLabel(String aText, FontStyle aStyle, boolean aCentered) {
    HaloLabel haloLabel = new HaloLabel(aText, 5, aCentered);
    haloLabel.setFont(FontStyle.getFont(aStyle));
    haloLabel.setTextColor(COLORS.get(aStyle));
    return haloLabel;
  }
}
