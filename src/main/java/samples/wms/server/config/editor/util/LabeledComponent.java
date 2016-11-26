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
package samples.wms.server.config.editor.util;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A convenience class that puts a JLabel in front of another component and
 * allows you to specify the exact width of the label.
 */
public class LabeledComponent extends JPanel {

  private JLabel fLabel;
  private int fLabelWidth = 100;
  private JComponent fComponent;

  public LabeledComponent(String aLabel, JComponent aComponent) {

    fLabel = new JLabel(aLabel);
    fComponent = aComponent;

    setLayout(new BorderLayout(2, 2));
    add(BorderLayout.WEST, fLabel);
    add(BorderLayout.CENTER, fComponent);

    updateComponentWidths();
  }

  private void updateComponentWidths() {
    int labelHeight = fLabel.getPreferredSize().height;
    fLabel.setMinimumSize(new Dimension(fLabelWidth, labelHeight));
    fLabel.setPreferredSize(new Dimension(fLabelWidth, labelHeight));
    int componentWidth = fComponent.getPreferredSize().width;
    int componentHeight = fComponent.getPreferredSize().height;
    fComponent.setMinimumSize(new Dimension(componentWidth, componentHeight));
    fComponent.setPreferredSize(new Dimension(componentWidth, componentHeight));
    doLayout();
  }

  public void setLabelWidth(int aWidth) {
    fLabelWidth = aWidth;
    updateComponentWidths();
  }

  public void setToolTipText(String aText) {
    fComponent.setToolTipText(aText);
  }

  public JComponent getComponent() {
    return fComponent;
  }
}
