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
package samples.common.gui.blacklime;

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;

/**
 * Gathers all colors used by {@link BlackLimeLookAndFeel}. This makes it easy to customize them.
 *
 * All colors implement the UIResource marker interface, to satisfy instanceof tests in Swing.
 */
final class ColorPalette {
  // If you want to change this color, also search&replace it in the Lucy configuration files
  public static final ColorUIResource blue = new ColorUIResource(0, 154, 224);

  // If you want to change this color, also search&replace it in the Lucy configuration files
  public static final ColorUIResource lime = new ColorUIResource(192, 217, 42);

  public static final ColorUIResource limeEmph = ColorPalette.derive(lime, 0.05);
  public static final ColorUIResource darkestGrey = new ColorUIResource(13, 20, 28);
  public static final ColorUIResource darkGrey = new ColorUIResource(23, 28, 34);
  public static final ColorUIResource mediumGrey = new ColorUIResource(30, 37, 45);

  public static final ColorUIResource blueGrey = new ColorUIResource(61, 74, 89);
  public static final ColorUIResource blueGreySubtle = derive(blueGrey, -0.02);
  public static final ColorUIResource blueGreyEmph = ColorPalette.derive(blueGrey, 0.05);
  public static final ColorUIResource lightGrey = new ColorUIResource(102, 114, 128);
  public static final ColorUIResource neutralGrey = new ColorUIResource(68, 68, 68);
  public static final ColorUIResource buttonBorder = new ColorUIResource(65, 73, 84);
  public static final ColorUIResource dialogBorder = new ColorUIResource(41, 46, 51);
  public static final ColorUIResource red = new ColorUIResource(109, 46, 34);

  public static final ColorUIResource text = new ColorUIResource(0xFFFFFF);
  public static final ColorUIResource textOnLimeBackground = darkGrey;
  public static final ColorUIResource disabledText = new ColorUIResource(168, 168, 168);

  // Slightly darker than blueGrey so that you can still distinguish it when displayed on top of blueGrey.
  // This happens when a check box is displayed in a JTree where the respective row is selected, and thus
  // the background is painted in blueGrey.
  public static final ColorUIResource checkBoxBg = new ColorUIResource(43, 53, 64);
  public static final ColorUIResource checkBoxBgEmph = ColorPalette.derive(checkBoxBg, 0.05);

  public static ColorUIResource derive(Color aIn, double aBrightnessOffset) {
    float[] channels = Color.RGBtoHSB(aIn.getRed(), aIn.getGreen(), aIn.getBlue(), null);
    channels[2] = Math.max(0, Math.min(channels[2] + (float) aBrightnessOffset, 1));
    Color color = new Color(Color.HSBtoRGB(channels[0], channels[1], channels[2]));
    return new ColorUIResource(new Color(color.getRed(), color.getGreen(), color.getBlue(), aIn.getAlpha()));
  }
}
