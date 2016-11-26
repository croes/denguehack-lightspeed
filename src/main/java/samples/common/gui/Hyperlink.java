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

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import samples.common.HaloLabel;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Hyper link based on a URL or an ILcdAction.
 *
 * Note: behavior is very similar to JXHyperlink (although it does not support html).
 * The major difference is that, when the action is disabled, only the underlining is omitted.
 * JXHyperlink instead uses a disabled text style, which is hardly readable when on the map.
 */
public class Hyperlink extends HaloLabel {

  private final ILcdAction fAction;
  private boolean fRollover = false;

  /**
   * Opens the given URL or file in a web browser or file manager, respectively.
   */
  public Hyperlink(final String aURLOrFile, String aText) {
    this(new ALcdAction(aText) {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          if (aURLOrFile.startsWith("http")) {
            Desktop.getDesktop().browse(URI.create(aURLOrFile));
          } else {
            Desktop.getDesktop().open(new File(aURLOrFile));
          }
        } catch (IOException e1) {
          TLcdLoggerFactory.getLogger(Hyperlink.class).warn("Could not open " + aURLOrFile);
        }
      }
    });
  }

  /**
   * Triggers the action when clicking.
   * The action's name is used as the link text.
   * @param aAction the action to perform when clicking.
   */
  public Hyperlink(ILcdAction aAction) {
    fAction = aAction;
    setOpaque(false);
    setFocusable(false);

    update();
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        fRollover = true;
        update();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        fRollover = false;
        update();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (fAction.isEnabled()) {
          fAction.actionPerformed(new ActionEvent(Hyperlink.this, ActionEvent.ACTION_PERFORMED, ""));
        }
      }
    });

    // Listen for changes in the action
    aAction.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (ILcdAction.NAME.equals(evt.getPropertyName()) ||
            ("enabled".equals(evt.getPropertyName()))) {
          update();
        }
      }
    });
  }

  private void update() {
    setCursor((fRollover && fAction.isEnabled()) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null);
    setText((String) fAction.getValue(ILcdAction.NAME));
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (fAction.isEnabled() && fRollover) {
      g.setColor(getForeground());
      Rectangle textBounds = getTextBounds();
      g.drawLine(textBounds.x,
                 textBounds.y + textBounds.height - 1,
                 textBounds.x + textBounds.width - 1,
                 textBounds.y + textBounds.height - 1);
    }
  }

}
