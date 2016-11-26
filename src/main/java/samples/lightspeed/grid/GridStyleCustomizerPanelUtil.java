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
package samples.lightspeed.grid;

import static java.awt.Color.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * Class containing utility methods to create a customizer panel for (grid) styles
 */
final class GridStyleCustomizerPanelUtil {

  static Color transparent(Color aColor) {
    return new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), 110);
  }

  /**
   * Creates a combo-box which will update {@code aStyleToUpdate} when the selection in the combo-box is
   * changed. Only suited when the {@code ALspStyle} is a {@code TLspTextStyle} or a {@code TLspLineStyle}.
   * @param aStylesToUpdate The styles to update when the selection in the combo-box is changed
   * @return the combo-box
   */
  static JComboBox createStyleCombobox(final TLspCustomizableStyle... aStylesToUpdate) {
    Style[] styles = new Style[]{
        new Style("<html><font style='font-weight: normal;' color=black>White</font></html>", transparent(WHITE), Style.Size.SMALL),
        new Style("<html><font style='font-weight: normal;' color=red>Red</font></html>", transparent(RED), Style.Size.SMALL),
        new Style("<html><font style='font-weight: bold;'   color=red>Red</font></html>", transparent(RED), Style.Size.LARGE),
        new Style("<html><font style='font-weight: normal;' color=green>Green</font></html>", transparent(GREEN), Style.Size.SMALL),
        new Style("<html><font style='font-weight: bold;'   color=green>Green</font></html>", transparent(GREEN), Style.Size.LARGE),
        new Style("<html><font style='font-weight: normal;' color=blue>Blue</font></html>", transparent(BLUE), Style.Size.SMALL),
        new Style("<html><font style='font-weight: bold;'   color=blue>Blue</font></html>", transparent(BLUE), Style.Size.LARGE)
    };
    final JComboBox comboBox = new JComboBox(styles);

    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for (TLspCustomizableStyle styleToUpdate : aStylesToUpdate) {
          updateStyle((Style) comboBox.getSelectedItem(), styleToUpdate);
        }
      }
    });

    // Select a combo box item, depending on the current styles
    Style style = findStyle(styles, aStylesToUpdate);
    if (style != null) {
      comboBox.setSelectedItem(style);
    }

    return comboBox;
  }

  private static Style findStyle(Style[] aStyles, TLspCustomizableStyle... aCustomizableStyles) {
    for (TLspCustomizableStyle customizableStyle : aCustomizableStyles) {
      if (customizableStyle.getStyle() instanceof TLspLineStyle) {
        TLspLineStyle lineStyle = (TLspLineStyle) customizableStyle.getStyle();
        for (Style style : aStyles) {
          double size = style.fSize == Style.Size.SMALL ? 1 : 3;
          if (style.fColor.equals(lineStyle.getColor()) && size == lineStyle.getWidth()) {
            return style;
          }
        }
      }
    }
    return null;
  }

  private static void updateStyle(Style aStyle, TLspCustomizableStyle aStyleToUpdateSFCT) {
    int size = aStyle.fSize == Style.Size.SMALL ? 1 : 3;
    Font font = Font.decode("Default-BOLD-" + (10 + (2 * size)));

    ALspStyle style = aStyleToUpdateSFCT.getStyle();

    if (style instanceof TLspLineStyle) {
      TLspLineStyle newLineStyle = ((TLspLineStyle) style).asBuilder()
                                                          .color(aStyle.fColor)
                                                          .width(size)
                                                          .build();

      aStyleToUpdateSFCT.setStyle(newLineStyle);
    } else if (style instanceof TLspTextStyle) {
      TLspTextStyle newTextStyle = ((TLspTextStyle) style).asBuilder()
                                                          .textColor(transparent(black))
                                                          .haloColor(aStyle.fColor)
                                                          .font(font)
                                                          .build();

      aStyleToUpdateSFCT.setStyle(newTextStyle);
    }
  }

  private static class Style {
    private enum Size {
      SMALL,
      LARGE
    }

    private String fName;
    private Color fColor;
    private Size fSize;

    private Style(String aName, Color aColor, Size aSize) {
      fName = aName;
      fColor = aColor;
      fSize = aSize;
    }

    @Override
    public String toString() {
      return fName;
    }
  }
}
