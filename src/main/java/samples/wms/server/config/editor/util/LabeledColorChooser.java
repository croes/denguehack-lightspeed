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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.gui.TLcdAWTUtil;

/**
 * Convenience class to display a labeled button which, when clicked, pops up a
 * color chooser dialog.
 */
public class LabeledColorChooser extends LabeledComponent {

  private List fChangeListeners = new ArrayList();

  private ColorPickPanel getColorPickPanel() {
    return (ColorPickPanel) getComponent();
  }

  public LabeledColorChooser(String aLabel, Color aDefaultColor) {
    super(aLabel, new ColorPickPanel());

    getColorPickPanel().setColor(aDefaultColor);
    getColorPickPanel().addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        if ("color".equals(evt.getPropertyName())) {
          ChangeEvent ce = new ChangeEvent(getColorPickPanel());
          for (int i = 0; i < fChangeListeners.size(); i++) {
            ChangeListener l = (ChangeListener) fChangeListeners.get(i);
            l.stateChanged(ce);
          }
        }
      }
    });
  }

  public Color getColor() {
    return getColorPickPanel().getColor();
  }

  public void addChangeListener(ChangeListener aListener) {
    fChangeListeners.add(aListener);
  }

  private static class ColorPickPanel extends JPanel {

    public ColorPickPanel() {
      // Show a Bevel border by default.
      setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.gray));

      MouseAdapter mouseAdapter = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          if (isEnabled() && e.getComponent().contains(e.getPoint())) {
            setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.gray));
          }
        }

        public void mouseReleased(MouseEvent e) {
          if (isEnabled() && e.getComponent().contains(e.getPoint())) {
            setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.gray));
            showColorChooser(e);
          }
        }
      };
      addMouseListener(mouseAdapter);
    }

    public Color getColor() {
      return getBackground();
    }

    public void setColor(Color aColor) {
      setBackground(aColor);
    }

    private void showColorChooser(AWTEvent aEvent) {
      Color oldColor = getColor();
      Color newColor = JColorChooser.showDialog(TLcdAWTUtil.findParentFrame(aEvent), "Choose a color", oldColor);
      if (newColor != null && !newColor.equals(oldColor)) {
        setBackground(newColor);
        firePropertyChange("color", oldColor, newColor);
      }
    }
  }
}
