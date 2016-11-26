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
import java.awt.SystemColor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 * <p>Cross look&feel UI colors. The aim is to consolidate colors in a single place, so they can be maintained and made to
 * work with various look and feels.</p>
 *
 * <p>Use the below pairs of colors. Between brackets, you can find typical values for a light look & feel. For a dark
 * look and feel, it would be the opposite.</p>
 * <ul>
 *   <li>{@link #fg()} (text, black) on either {@link #bg()} (white) or {@link #bgSubtle()} (light gray, e.g. JPanel). These
 *   are the mostly used colors in the UI.</li>
 *   <li>{@link #fgHighlight()} (white) on {@link #bgHighlight()} (blue) to display highlighted text, for example
 *   selected text or when pointing at a menu item.</li>
 *   <li>{@link #fgAccent()} (blue, orange, green) on either {@link #bg()} or {@link #bgSubtle()}, for example to draw
 *   attention to a small element such as the current time in a time line.</li>
 * </ul>
 */
public class UIColors {

  private static boolean sInitialized = false;

  private static Color sForegroundHighlight;
  private static Color sBackgroundHighlight;

  private static Color sForegroundAccent;

  private static Color sForeground;
  private static Color sForegroundHint;
  private static Color sBackgroundSubtle;
  private static Color sBackground;

  private static Color sMapBackground;

  private UIColors() {
  }

  static {
    UIManager.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        sInitialized = false;
      }
    });
  }

  private static void initColors() {
    if (!sInitialized) {
      sInitialized = true;

      JList list = new JList();
      JLabel label = new JLabel();

      sForeground = getColor("List.foreground", getColor(label.getForeground(), SystemColor.textText));
      sBackground = getColor("List.background", getColor(label.getBackground(), SystemColor.text));

      sForegroundHighlight = getColor(list.getSelectionForeground(), SystemColor.textHighlightText);
      sBackgroundHighlight = getColor(list.getSelectionBackground(), SystemColor.textHighlight);

      // Use lime color for BlackLimeLookAndFeel. Use the selected list background color otherwise, which
      // is usually a distinguishing color (e.g. blue or orange or so)
      sForegroundAccent = getColor("nimbusOrange", sBackgroundHighlight);

      sBackgroundSubtle = getColor(label.getBackground(), SystemColor.text);

      sForegroundHint = mid(sBackgroundSubtle, sForeground, 0.6);

      // Use sea blue as background, unless the UI is too dark
      sMapBackground = isDarkTheme() ? sBackground.darker() : new Color(163, 193, 222);
    }
  }

  private static Color getColor(String aKey, Color aDefault) {
    Color color = UIManager.getColor(aKey);
    if (color == null) {
      color = aDefault;
    }
    // Avoid ever return colors that implement UIResource, as the look & feel can again overrule those
    return new Color(color.getRed(), color.getGreen(), color.getBlue());
  }

  private static Color getColor(Color aColor, Color aDefault) {
    return aColor == null ? aDefault : aColor;
  }

  /**
   * Calculates a color between the two given colors.
   *
   * @param aColorForZero Color returned when percentage is 0.
   * @param aColorForOne  Color returned when percentage is 1.
   * @param aPercent      Percentage: 0..1. 0 means calculated color is aColorForZero, 1 means
   *                      calculated color is aColorForOne. Interpolated colors between 0 and 1.
   *
   * @return The calculated color.
   */
  public static Color mid(Color aColorForZero, Color aColorForOne, double aPercent) {
    return new Color(mid(aColorForZero.getRed(), aColorForOne.getRed(), aPercent),
                     mid(aColorForZero.getGreen(), aColorForOne.getGreen(), aPercent),
                     mid(aColorForZero.getBlue(), aColorForOne.getBlue(), aPercent));
  }

  /**
   * Changes the alpha value of a given color.
   *
   * @param aColor The color.
   * @param aAlpha The new alpha value.
   *
   * @return The same color but with the new alpha value.
   */
  public static Color alpha(Color aColor, int aAlpha) {
    return new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), aAlpha);
  }

  private static int mid(int c1, int c2, double aPercent) {
    return (int) Math.round(c1 * (1 - aPercent) + c2 * aPercent);
  }

  public static boolean isDarkTheme() {
    Color bg = bgSubtle();
    int avg = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3;
    return avg < 150;
  }

  /**
   * See {@link UIColors}.
   */
  public static Color fgHighlight() {
    initColors();
    return sForegroundHighlight;
  }

  /**
   * See {@link UIColors}.
   */
  public static Color bgHighlight() {
    initColors();
    return sBackgroundHighlight;
  }

  /**
   * See {@link UIColors}.
   */
  public static Color fgAccent() {
    initColors();
    return sForegroundAccent;
  }

  /**
   * See {@link UIColors}.
   */
  public static Color fg() {
    initColors();
    return sForeground;
  }

  /**
   * @return A color that is less emphasized compared to the regular foreground color. It is used
   * to display for example hint text next to a label.
   */
  public static Color fgHint() {
    initColors();
    return sForegroundHint;
  }

  /**
   * See {@link UIColors}.
   */
  public static Color bgSubtle() {
    initColors();
    return sBackgroundSubtle;
  }

  /**
   * See {@link UIColors}.
   */
  public static Color bg() {
    initColors();
    return sBackground;
  }

  /**
   * @return Background color for the map. Typically blue for light UI's and dark for dark UI's.
   */
  public static Color bgMap() {
    initColors();
    return sMapBackground;
  }

  /**
   * Retrieves a named color from the UI manager.
   * @param aKey The key for the color.
   * @param aDefault A default value, in case the UI color is null.
   * @return The color.
   */
  public static Color getUIColor(String aKey, Color aDefault) {
    Color color = UIManager.getColor(aKey);
    if (color == null) {
      color = aDefault;
    }
    return color;
  }
}
