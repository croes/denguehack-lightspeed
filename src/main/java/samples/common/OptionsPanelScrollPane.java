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
import java.awt.Dimension;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * Extension of JScrollPane that is particularly useful for options panels.
 *
 * Consider this options panel, having a collapsible advanced section:
 *
 * <pre>
 * Basic -----------
 *   Option 1
 *   Option 2
 * Advanced ------ ^
 *   Only visible when expanded
 * </pre>
 *
 * The entire panel is wrapped in a scroll pane so that in case there is not enough screen
 * space, the options are still accessible. When for example expanding the advanced section, you
 * preferably want the UI to update itself, so that space becomes available for it, without showing
 * the scroll bars. That's exactly what this scroll pane extension does.
 *
 * Additionally, it has tweaked a few default values.
 */
public class OptionsPanelScrollPane extends JScrollPane {

  public OptionsPanelScrollPane(Component view) {
    super(view);
    getHorizontalScrollBar().setUnitIncrement(10);
    getVerticalScrollBar().setUnitIncrement(10);
  }

  @Override
  public boolean isValidateRoot() {
    // Go back to the JComponent default: whenever this component wants more or less space, resize
    // the parents as well.
    return false;
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension pref = super.getPreferredSize();
    makeRoomForScrollBar(pref);
    return pref;
  }

  private void makeRoomForScrollBar(Dimension aPref) {
    // Workaround for an issue in ScrollPaneLayout where it does not allow enough space for the
    // scroll bars when using the 'as needed' policies.
    // The space for the scroll bars is always reserved, even when they are not visible. This for example
    // prevents glitches during window resizing or animations, and generally avoids that the layout needs
    // 'two takes' to get it right.
    JScrollBar horizontal = getHorizontalScrollBar();
    if (getHorizontalScrollBarPolicy() == HORIZONTAL_SCROLLBAR_AS_NEEDED && horizontal != null) {
      aPref.height += horizontal.getPreferredSize().height;
    }

    JScrollBar vertical = getVerticalScrollBar();
    if (getVerticalScrollBarPolicy() == VERTICAL_SCROLLBAR_AS_NEEDED && vertical != null) {
      aPref.width += vertical.getPreferredSize().width;
    }
  }
}
