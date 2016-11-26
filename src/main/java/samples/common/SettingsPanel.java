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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A panel with a vertical layout, consisting of optionally titled components.
 */
public class SettingsPanel extends JPanel {
  public SettingsPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  /**
   * Create a content builder for the panel. Any existing content is removed.
   */
  public ContentBuilder contentBuilder() {
    return new ContentBuilder();
  }

  public class ContentBuilder {
    private int fPadding = 5;
    private TitledCollapsiblePane fCurrentCollapsible;

    private ContentBuilder() {
      SettingsPanel.this.removeAll();
    }

    /**
     * Set the padding size for newly added components. The default is 5.
     */
    public ContentBuilder pad(int aPadding) {
      fPadding = aPadding;
      return this;
    }

    /**
     * Add a component without title.
     */
    public ContentBuilder pane(JComponent aComponent) {
      return pane(null, false, aComponent);
    }

    /**
     * Add a component with a title.
     */
    public ContentBuilder pane(String aTitle, JComponent aComponent) {
      return pane(aTitle, false, aComponent);
    }

    /**
     * Add a component with a title, which can be collapsible.
     */
    public ContentBuilder pane(String aTitle, boolean aCollapsible, JComponent aComponent) {
      fCurrentCollapsible = null;
      aComponent.setAlignmentX(LEFT_ALIGNMENT);
      if (fPadding > 0) {
        aComponent.setBorder(BorderFactory.createEmptyBorder(fPadding, fPadding, fPadding, fPadding));
      }
      if (aTitle == null) {
        SettingsPanel.this.add(aComponent);
      } else {
        fCurrentCollapsible = new TitledCollapsiblePane(aTitle, aComponent, aCollapsible);
        fCurrentCollapsible.setAlignmentX(LEFT_ALIGNMENT);
        SettingsPanel.this.add(fCurrentCollapsible);
      }
      return this;
    }

    /**
     * Collapse the most recently added component, if possible.
     */
    public ContentBuilder collapse() {
      if (fCurrentCollapsible != null) {
        fCurrentCollapsible.setCollapsed(true);
      }
      return this;
    }

    /**
     * Finalize and return the panel.
     */
    public SettingsPanel build() {
      return SettingsPanel.this;
    }
  }
}
