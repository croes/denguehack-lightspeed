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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * This custom title panel creates separators on the left and right side
 * of the given title string. The left separator has a fixed width of 8 pixels,
 * the right separator will fill the remainder of the panel.
 * <p/>
 * For example (
 * | --- aTitle ---------------------------------------------------------- |
 */
public class TitledSeparator extends JPanel {

  private final JLabel fLabel;

  public TitledSeparator(String aTitle) {
    super(new GridBagLayout());

    fLabel = new JLabel(aTitle) {
      public void updateUI() {
        super.updateUI();
        setForeground(UIManager.getColor("TitledBorder.titleColor"));
        Font font = UIManager.getFont("TitledBorder.font");
        font = font == null ? getFont() : font;
        if (font != null) {
          setFont(font.deriveFont(Font.BOLD));
        }
      }
    };

    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    c.weighty = 1;
    c.gridwidth = 1;
    c.gridheight = 3;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    this.add(fLabel, c);

    c.gridx++;
    c.gridy = 0;
    c.weightx = 1;
    c.weighty = 0.60;
    c.gridheight = 1;
    c.insets = new Insets(0, 0, 0, 0);
    this.add(Box.createVerticalGlue(), c);

    c.gridy++;
    c.weighty = 0;
    c.insets = new Insets(0, 4, 0, 0);
    JPanel panel = new JPanel();
    panel.setPreferredSize(new Dimension(0, 1));
    panel.setBackground(UIColors.fg());
    this.add(panel, c);

    c.gridy++;
    c.weighty = 0.40;
    this.add(Box.createVerticalGlue(), c);
  }

  public void setTitle(String aTitle) {
    String old = fLabel.getText();
    fLabel.setText(aTitle);
    firePropertyChange("title", old, aTitle);
  }

}
