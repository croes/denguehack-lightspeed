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
package samples.lucy.printing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Utility class to decorate and compose print preview components.
 */
public final class PrintableComponentFactorySupport {
  private static final Border MATTE_BORDER = BorderFactory.createMatteBorder(1, 1, 2, 2, Color.BLACK);

  private PrintableComponentFactorySupport() {
    //private constructor, class only contains utility methods
  }

  public static JPanel createCompositePanel(Component aClassification, JPanel aHeaderPanel, JPanel aOverviewPanel, JPanel aViewPanel, JPanel aLegendPanel) {
    Dimension space = new Dimension(4, 4);
    JPanel contents = new JPanel(new GridBagLayout());
    contents.setBackground(Color.WHITE);

    GridBagConstraints con = new GridBagConstraints(
        0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0
    );

    contents.add(aHeaderPanel, con);
    con.gridy++;
    contents.add(Box.createRigidArea(space), con);
    con.weighty = 1;
    con.gridy++;
    contents.add(aViewPanel, con);

    con.gridheight = GridBagConstraints.REMAINDER;
    con.weightx = 0;
    con.gridy = 0;
    con.gridx++;
    contents.add(Box.createRigidArea(space), con);

    con.gridheight = 1;
    con.weighty = 0;
    con.gridx++;
    contents.add(aOverviewPanel, con);
    con.gridy++;
    contents.add(Box.createRigidArea(space), con);
    con.gridy++;
    contents.add(aLegendPanel, con);

    if (aClassification == null) {
      return contents;
    }
    JPanel contentsWithClassification = new JPanel(new BorderLayout(10, 10));
    contentsWithClassification.setBackground(contents.getBackground());
    contentsWithClassification.add(aClassification, BorderLayout.NORTH);
    contentsWithClassification.add(contents, BorderLayout.CENTER);

    return contentsWithClassification;
  }

  public static JPanel createLegendPanel(Component aLegend, Component aScaleLabel, Component aScaleIcon) {
    JPanel legendPanel = new JPanel(new BorderLayout());
    legendPanel.setOpaque(false);
    legendPanel.setBorder(BorderFactory.createCompoundBorder(
        MATTE_BORDER, BorderFactory.createEmptyBorder(10, 10, 10, 10)
                                                            ));

    if (aLegend != null || aScaleLabel != null || aScaleIcon != null) {
      if (aLegend != null) {
        legendPanel.add(aLegend, BorderLayout.NORTH);
      }

      legendPanel.add(Box.createGlue(), BorderLayout.CENTER);

      if (aScaleLabel != null || aScaleIcon != null) {
        JPanel scalePanel = new JPanel(new GridLayout(0, 1));
        scalePanel.setOpaque(false);
        if (aScaleLabel != null) {
          scalePanel.add(aScaleLabel);
        }
        if (aScaleIcon != null) {
          scalePanel.add(aScaleIcon);
        }
        legendPanel.add(scalePanel, BorderLayout.SOUTH);
      }
    }
    return legendPanel;
  }

  public static JPanel createViewPanel(Component aViewComponent) {
    JPanel viewPanel = new JPanel(new BorderLayout());
    viewPanel.setOpaque(false);
    viewPanel.setBorder(MATTE_BORDER);

    if (aViewComponent != null) {
      viewPanel.add(aViewComponent, BorderLayout.CENTER);
    }
    return viewPanel;
  }

  public static JPanel createOverviewPanel(Component aOverview) {
    JPanel overviewPanel = new JPanel(new BorderLayout());
    overviewPanel.setOpaque(false);
    overviewPanel.setBorder(MATTE_BORDER);

    if (aOverview != null) {
      // set minimum overviewPanel size to same as the overview preferred size
      overviewPanel.setMinimumSize(aOverview.getPreferredSize());
      overviewPanel.add(aOverview, BorderLayout.CENTER);
    }
    return overviewPanel;
  }

  public static JPanel createTitlePanel(Component aHeaderTextComponent, Component aTitleTextComponent) {
    JPanel titlePanel = new JPanel(new BorderLayout());
    titlePanel.setOpaque(false);
    titlePanel.setBorder(BorderFactory.createCompoundBorder(
        MATTE_BORDER, BorderFactory.createEmptyBorder(10, 10, 10, 10)
                                                           ));

    if (aHeaderTextComponent != null) {
      titlePanel.add(aHeaderTextComponent, BorderLayout.NORTH);
    }

    if (aTitleTextComponent != null) {
      titlePanel.add(aTitleTextComponent, BorderLayout.CENTER);
    }
    return titlePanel;
  }
}
