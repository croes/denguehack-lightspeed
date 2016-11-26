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
package samples.hana.lightspeed.ui;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.TwoColumnLayoutBuilder;

import samples.hana.lightspeed.common.ColorPalette;
import samples.hana.lightspeed.common.HangingTitlePanel;

/**
 * Legend UI element for wind speed colors.
 */
public class WindSpeedLegendComponent extends JPanel {

  private WindSpeedLegendComponent() {
  }

  public static JComponent create() {
    TwoColumnLayoutBuilder builder = TwoColumnLayoutBuilder.newBuilder();
    builder.row().columnOne(getIcon(ColorPalette.d), getLabel("00-34")).build();
    builder.row().columnOne(getIcon(ColorPalette.c), getLabel("34-50")).build();
    builder.row().columnOne(getIcon(ColorPalette.b), getLabel("50-64")).build();
    builder.row().columnOne(getIcon(ColorPalette.a), getLabel("64+")).build();

    JPanel legend = new JPanel();
    builder.populate(legend);
    legend.setOpaque(false);

    JComponent titledLegend = HangingTitlePanel.create("Wind Speed (mph)", legend);
    titledLegend.setCursor(Cursor.getDefaultCursor());
    return titledLegend;
  }

  private static JLabel getLabel(String aText) {
    return new JLabel(aText);
  }

  private static JLabel getIcon(Color aD) {
    return new JLabel(new TLcdSWIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 16, Color.white, aD)));
  }
}
