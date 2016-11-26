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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import samples.common.HaloLabel;

/**
 * Color based legend panel.
 */
public class ColorLegend extends JPanel {

  private boolean fOverlay;

  /**
   * Create a new Legend.
   * @param aLevelLabels Labels to use for each level. Should have same length as aLevelColors.
   * @param aLevelColors Colors to use for each level. Should have same length as aLevelLabels.
   * @param aOverlay true if the panel is used as an overlay on top of the view
   */
  public ColorLegend(String[] aLevelLabels, Color[] aLevelColors, boolean aOverlay) {
    this(aLevelLabels, aLevelColors, new String[0], new Color[0], aOverlay, false);
  }

  /**
   * Create a new Legend.
   * @param aLevelLabels Labels to use for each level. Should have same length as aLevelColors.
   * @param aLevelColors Colors to use for each level. Should have same length as aLevelLabels.
   * @param aSpecialLabels Labels to use for each special value. Should have same length as aSpecialColors.
   * @param aSpecialColors Colors to use for each special value. Should have same length as aSpecialLabels.
   * @param aOverlay true if the panel is used as an overlay on top of the view
   * @param aReverseOrder true if the first entry should be placed at the bottom iso at the top
   */
  public ColorLegend(String[] aLevelLabels, Color[] aLevelColors, String[] aSpecialLabels, Color[] aSpecialColors, boolean aOverlay, boolean aReverseOrder) {
    super();
    fOverlay = aOverlay;
    setOpaque(false);

    String[] labels = new String[aLevelColors.length + aSpecialColors.length];
    System.arraycopy(aLevelLabels, 0, labels, 0, aLevelLabels.length);
    System.arraycopy(aSpecialLabels, 0, labels, aLevelLabels.length, aSpecialLabels.length);

    Color[] colors = new Color[aLevelColors.length + aSpecialColors.length];
    System.arraycopy(aLevelColors, 0, colors, 0, aLevelColors.length);
    System.arraycopy(aSpecialColors, 0, colors, aLevelColors.length, aSpecialColors.length);

    JPanel color_panel = new JPanel(new GridLayout(colors.length, 1, 0, 2));

    color_panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    if (fOverlay) {
      color_panel.setBackground(new Color(0, 0, 0, 70));
    }

    if (aReverseOrder) {
      for (int i = colors.length - 1; i >= 0; i--) {
        color_panel.add(new LegendEntry(colors[i], labels[i], true));
      }
    } else {
      for (int i = 0; i < colors.length; i++) {
        color_panel.add(new LegendEntry(colors[i], labels[i], true));
      }
    }

    JPanel legend_panel = new JPanel(new BorderLayout());
    legend_panel.add(BorderLayout.NORTH, color_panel);
    if (fOverlay) {
      legend_panel.setOpaque(false);
    }

    legend_panel.add(BorderLayout.CENTER, Box.createGlue());

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, legend_panel);
  }

  /**
   * A class to represent a single entry in the legend.
   */
  class LegendEntry extends Container {
    final JLabel fIconLabel;
    final JLabel fTextLabel;
    final LegendIcon fIcon;

    public LegendEntry(Color aColor, String aLabel, boolean aWithAltitude) {
      fIconLabel = new JLabel();
      fIcon = new LegendIcon(40, 20, aColor);
      fIconLabel.setIcon(fIcon);
      String text = aWithAltitude ? aLabel : "";
      fTextLabel = fOverlay ? new HaloLabel(text) : new JLabel(text);

      setLayout(new BorderLayout(5, 0));
      add(BorderLayout.WEST, fIconLabel);
      add(BorderLayout.CENTER, fTextLabel);
    }
  }

  /**
   * A class to represent the legend icon.
   */
  private static final class LegendIcon implements Icon {

    private final int fHeight;
    private final int fWidth;
    private final Color fColor;

    LegendIcon(int aWidth, int aHeight, Color aColor) {
      fWidth = aWidth;
      fHeight = aHeight;
      fColor = aColor;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(fColor);
      g.fillRect(3, 3, fWidth - 8, fHeight - 8);
      g.setColor(Color.black);
      g.drawRect(3, 3, fWidth - 8, fHeight - 8);

    }

    @Override
    public int getIconWidth() {
      if (fWidth > 0) {
        return fWidth;
      }
      return 0;
    }

    @Override
    public int getIconHeight() {
      if (fHeight > 0) {
        return fHeight;
      }
      return 0;
    }

    @Override
    public Object clone() {
      return new LegendIcon(fWidth, fHeight, new Color(fColor.getRed(), fColor.getGreen(), fColor.getBlue(), fColor.getAlpha()));
    }
  }
}
