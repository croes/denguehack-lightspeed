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
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * Convenience class for an unobtrusive horizontal split pane.
 */
public class HorizontalSplitPane extends JSplitPane {

  public HorizontalSplitPane(JComponent aLeftPanel, JComponent aRightPanel) {
    super(JSplitPane.HORIZONTAL_SPLIT, true, aLeftPanel, aRightPanel);
    setOneTouchExpandable(true);
    setBorder(new LineBorder(Color.black, 0));
    // The SynthLookAndFeel does not like changing the UI
    if (!getUI().toString().contains("SynthSplitPaneUI")) {
      setUI(new FlatSplitPaneUI());
    }
    // JSplitPane by default responds to the arrow keys.
    // Map-based applications probably want to reserve these for navigation purposes, so we deactivate these bindings.
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
  }

  // Adds a more minimalistic appearance to a split pane.
  private class FlatSplitPaneUI extends BasicSplitPaneUI {
    public BasicSplitPaneDivider createDefaultDivider() {
      return new BasicSplitPaneDivider(this) {
        public void setBorder(Border b) {
        }
      };
    }
  }

}
