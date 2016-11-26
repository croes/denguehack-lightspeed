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
package samples.common.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;

import samples.common.UIColors;
import com.luciad.util.TLcdPair;

/**
 * Utility class for highlighting texts.
 */
public class TextHighlighter {
  private static String sHighlightFontTag;
  private static String sRegularFontTag;

  private static String sSelectedHighlightFontTag;
  private static String sSelectedRegularFontTag;

  private static boolean sInitialized = false;

  static {
    UIManager.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        sInitialized = false;
      }
    });
  }

  private static void initializeColors() {
    if (!sInitialized) {
      sInitialized = true;

      Color foreground = UIColors.fg();
      Color background = UIColors.mid(foreground, UIColors.bg(), 0.8);

      String foregroundColor = Integer.toHexString(foreground.getRGB() & 0xFFFFFF);
      String backgroundColor = Integer.toHexString(background.getRGB() & 0xFFFFFF);

      sHighlightFontTag = String.format("<font color=#%s style='background-color:#%s;'>", foregroundColor, backgroundColor);
      sRegularFontTag = String.format("<font color=#%s>", foregroundColor);

      foreground = UIColors.fgHighlight();
      background = UIColors.mid(foreground, UIColors.bgHighlight(), 0.8);

      foregroundColor = Integer.toHexString(foreground.getRGB() & 0xFFFFFF);
      backgroundColor = Integer.toHexString(background.getRGB() & 0xFFFFFF);

      sSelectedHighlightFontTag = String.format("<font color=#%s style='background-color:#%s;'>", foregroundColor, backgroundColor);
      sSelectedRegularFontTag = String.format("<font color=#%s>", foregroundColor);
    }
  }

  private TextHighlighter() {
  }

  /**
   * <p>
   *   Returns an html formatted string where the text corresponds top the given pattern, is highlighted.
   * </p>
   *
   * <p>
   *   The difference with the {@link #createHighlightedSelectedText(Pattern, String)} method are the colors used for the text
   *   and the highlighting.
   *   This method uses colors suited for when the text is used for a non-selected item.
   * </p>
   *
   * @param aPattern the given pattern
   * @param aText the given text
   * @return the html formatted / highlighted text.
   */
  public static String createHighlightedText(Pattern aPattern, String aText) {
    initializeColors();
    return highlight(aPattern, aText, sRegularFontTag, sHighlightFontTag);
  }

  /**
   * <p>
   *   Returns an html formatted string where the areas of text, corresponding to the indices defined in {@code aAreasToHighlight},
   *   is highlighted.
   * </p>
   *
   * <p>
   *   The difference with the {@link #createHighlightedSelectedText(List, String)} method are the colors used for the text
   *   and the highlighting.
   *   This method uses colors suited for when the text is used for a non-selected item.
   * </p>
   *
   * @param aAreasToHighlight List of pairs of indices, where each pair constant the start index (inclusive) and the end index (exclusive).
   *                          The substrings of {@code aText} represented by those indices will be highlighted
   * @param aText The text. Should not contain any HTML tags.
   * @return the html formatted / highlighted text
   */
  public static String createHighlightedText(List<TLcdPair<Integer, Integer>> aAreasToHighlight, String aText) {
    initializeColors();
    return highlight(aAreasToHighlight, aText, sRegularFontTag, sHighlightFontTag);
  }

  /**
   * <p>
   *   Returns an html formatted string where the text corresponds top the given pattern, is highlighted.
   * </p>
   *
   * <p>
   *   The difference with the {@link #createHighlightedText(Pattern, String)} method are the colors used for the text
   *   and the highlighting.
   *   This method uses colors suited for when the text is used for a selected item.
   * </p>
   *
   * @param aPattern the given pattern
   * @param aText the given text
   * @return the html formatted / highlighted text.
   */
  public static String createHighlightedSelectedText(Pattern aPattern, String aText) {
    initializeColors();
    return highlight(aPattern, aText, sSelectedRegularFontTag, sSelectedHighlightFontTag);
  }

  /**
   * <p>
   *   Returns an html formatted string where the areas of text, corresponding to the indices defined in {@code aAreasToHighlight},
   *   is highlighted.
   * </p>
   *
   * <p>
   *   The difference with the {@link #createHighlightedText(List, String)} method are the colors used for the text
   *   and the highlighting.
   *   This method uses colors suited for when the text is used for a selected item.
   * </p>
   *
   * @param aAreasToHighlight List of pairs of indices, where each pair constant the start index (inclusive) and the end index (exclusive).
   *                          The substrings of {@code aText} represented by those indices will be highlighted
   * @param aText The text. Should not contain any HTML tags.
   * @return the html formatted / highlighted text
   */
  public static String createHighlightedSelectedText(List<TLcdPair<Integer, Integer>> aAreasToHighlight, String aText) {
    initializeColors();
    return highlight(aAreasToHighlight, aText, sSelectedRegularFontTag, sSelectedHighlightFontTag);
  }

  private static String highlight(Pattern aPattern, String aText, String aRegularFontTag, String aHighlightedFontTag) {
    if (aText == null) {
      return null;
    }

    String text = stripHTMLTags(aText);

    if (aPattern != null) {
      Matcher matcher = aPattern.matcher(text);
      if (matcher.find() && !matcher.pattern().pattern().isEmpty()) {
        return highlight(Collections.singletonList(new TLcdPair<Integer, Integer>(matcher.start(), matcher.end())),
                         text,
                         aRegularFontTag,
                         aHighlightedFontTag);
      }
    }
    return highlight(Collections.<TLcdPair<Integer, Integer>>emptyList(), aText, aRegularFontTag, aHighlightedFontTag);
  }

  private static String highlight(List<TLcdPair<Integer, Integer>> aAreasToHighlight, String aText, String aRegularFontTag, String aHighlightedFontTag) {
    if (aText == null) {
      return null;
    }

    if (!aAreasToHighlight.isEmpty()) {
      StringBuilder sb = new StringBuilder();

      sb.append("<html><nobr>");

      int previousMatchEnd = 0;
      for (TLcdPair<Integer, Integer> indicesPair : aAreasToHighlight) {
        int matchStart = indicesPair.getKey();
        int matchEnd = indicesPair.getValue();
        String regularText = aText.substring(previousMatchEnd, matchStart);
        if (!regularText.isEmpty()) {
          sb.append(aRegularFontTag).append(regularText).append("</font>");
        }

        String match = aText.substring(matchStart, matchEnd);
        previousMatchEnd = matchEnd;
        if (!match.isEmpty()) {
          sb.append(aHighlightedFontTag).append(match).append("</font>");
        }
      }
      String rest = aText.substring(previousMatchEnd, aText.length());
      if (!rest.isEmpty()) {
        sb.append(aRegularFontTag).append(rest).append("</font>");
      }

      sb.append("</nobr></html>");
      return sb.toString();
    }
    //always use HTML tags, even when no match is found. This ensures that the same font color is used as when a match is found
    return "<html><nobr>" + aRegularFontTag + aText + "</font></nobr></html>";
  }

  private static String stripHTMLTags(String aText) {
    String text = aText;
    text = text.replace("<html>", "");
    text = text.replace("<nobr>", "");
    text = text.replace("</nobr>", "");
    text = text.replace("</html>", "");
    return text;
  }

}
