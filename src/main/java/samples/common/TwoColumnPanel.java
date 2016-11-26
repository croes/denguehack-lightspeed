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

import java.awt.Component;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A simple panel with a two column layout, ideal for use in forms.
 */
public class TwoColumnPanel extends JPanel {
  private static final Font DEFAULT_LABEL_FONT = new JLabel().getFont().deriveFont(Font.PLAIN);

  public TwoColumnPanel() {
    final GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    setLayout(layout);
    setAlignmentX(Component.LEFT_ALIGNMENT);
  }

  /**
   * Create a content builder for the panel. Any existing content is removed.
   */
  public ContentBuilder contentBuilder() {
    return new ContentBuilder(this);
  }

  public static class ContentBuilder {
    private final TwoColumnPanel fPanel;
    private final GroupLayout fLayout;
    private final GroupLayout.SequentialGroup fHGroup;
    private final GroupLayout.SequentialGroup fVGroup;
    private final GroupLayout.ParallelGroup fLabelGroup;
    private final GroupLayout.ParallelGroup fFieldGroup;

    private Font fLabelFont = DEFAULT_LABEL_FONT;

    /**
     * Set up a new layout builder. The supplied panel will be cleared.
     * @param aPanel panel to build the layout in, which must have a {@linkplain GroupLayout}.
     */
    private ContentBuilder(TwoColumnPanel aPanel) {
      fPanel = aPanel;
      fLayout = (GroupLayout) aPanel.getLayout();
      fHGroup = fLayout.createSequentialGroup();
      fVGroup = fLayout.createSequentialGroup();
      fLabelGroup = fLayout.createParallelGroup(GroupLayout.Alignment.TRAILING);
      fFieldGroup = fLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
      fPanel.removeAll();
    }

    /**
     * Set the font used when creating labels.
     */
    public ContentBuilder labelFont(Font aLabelFont) {
      fLabelFont = aLabelFont;
      return this;
    }

    /**
     * Add a labeled component.
     */
    public ContentBuilder row(String aLabel, JComponent aField) {
      final JLabel label = new JLabel(aLabel);
      label.setFont(fLabelFont);
      return row(label, aField);
    }

    /**
     * Add a labeled component using a custom label.
     */
    public ContentBuilder row(JComponent aLabel, JComponent aField) {
      fLabelGroup.addComponent(aLabel);
      fFieldGroup.addComponent(aField);
      fVGroup.addGroup(fLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                              .addComponent(aLabel).addComponent(aField));
      return this;
    }

    /**
     * Finalize and return the panel.
     */
    public TwoColumnPanel build() {
      fHGroup.addGroup(fLabelGroup);
      fHGroup.addGroup(fFieldGroup);
      fLayout.setHorizontalGroup(fHGroup);
      fLayout.setVerticalGroup(fVGroup);
      fPanel.revalidate();
      return fPanel;
    }
  }
}
