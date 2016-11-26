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
package samples.hana.lightspeed.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.common.UIColors;

public class UIUtil {
  public static JPanel wrapWithSpaceEater(Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setOpaque(false);
    p.add(c, BorderLayout.WEST); //extra space goes to (empty) center
    return p;
  }

  public static JComponent createBorderlessButton(Action aAction) {
    //Add the action to a tool bar to make the button look nice
    JToolBar tb = new JToolBar();
    tb.setOpaque(false);
    tb.setBorderPainted(false);
    tb.setFloatable(false);
    tb.add(aAction);
    return tb;
  }

  public static JComponent createFitButton(final ThemeComponent.POI aPOI, final ILspView aView) {
    AbstractAction fit = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fit(aView, aPOI);
      }
    };
    fit.putValue(Action.SMALL_ICON, new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.RECENTER_ICON)));
    fit.putValue(Action.SHORT_DESCRIPTION, aPOI.getDescription());

    return UIUtil.createBorderlessButton(fit);
  }

  public static void fit(ILspView aView, ThemeComponent.POI aPOI) {
    try {
      TLspViewNavigationUtil util = new TLspViewNavigationUtil(aView);
      util.setAnimationSpeedUp(0.5);
      util.animatedFit(aPOI, new TLcdGeodeticReference());
    } catch (TLcdOutOfBoundsException e1) {
      //ignore
    }
  }

  public static JComponent createInfoButton(String aInfoHtmlFileRelativeToClassPath) {
    String resourcePath = File.separator + aInfoHtmlFileRelativeToClassPath;
    final URL resource = UIUtil.class.getResource(resourcePath.replace('\\', '/'));

    try {
      final JPopupMenu popup = new JPopupMenu();
      popup.setBorder(BorderFactory.createEmptyBorder());
      popup.setLayout(new BorderLayout());
      final JEditorPane editor = new JEditorPane();
      editor.setEditable(false);
      editor.setPage(resource);
      popup.add(new JScrollPane(editor) {
        @Override
        public Dimension getPreferredSize() {
          Dimension pref = super.getPreferredSize();
          pref.width = 500; //fixed with to avoid JEditorPane cramming everything on one long line
          return pref;
        }
      });

      AbstractAction showPopup = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          Component invoker = (Component) e.getSource();
          Point popupLocation = getPopupLocation(invoker, popup);
          popup.show(invoker, popupLocation.x, popupLocation.y);
          popup.setVisible(false); //show twice to get the layout right ...
          popup.show(invoker, popupLocation.x, popupLocation.y);
        }
      };
      showPopup.putValue(Action.SMALL_ICON, new TLcdSWIcon(new TLcdImageIcon("samples/hana/lightspeed/ui/information_bw_16.png")));
      showPopup.putValue(Action.SHORT_DESCRIPTION, "Show presenter info");
      return UIUtil.createBorderlessButton(showPopup);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Point getPopupLocation(Component invoker, JPopupMenu aPopup) {
    int popup_h = (int) aPopup.getPreferredSize().getHeight();
    int popup_w = (int) aPopup.getPreferredSize().getWidth();
    int base_x = (int) invoker.getLocationOnScreen().getX();
    int base_y = (int) invoker.getLocationOnScreen().getY();
    int popup_x = 0;
    int popup_y = invoker.getHeight();

    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(invoker.getGraphicsConfiguration());
    Rectangle screen = invoker.getGraphicsConfiguration().getBounds();

    int popup_right_border = base_x + popup_w;
    int screen_right_border = screen.x + screen.width - insets.right;
    if (popup_right_border > screen_right_border) {
      popup_x = screen_right_border - popup_right_border;
    }

    int popup_lower_border = base_y + popup_h + popup_y;
    int screen_lower_border = screen.y + screen.height - insets.bottom;
    if (popup_lower_border > screen_lower_border) {
      int delta_y = base_y - screen.y;
      if (delta_y - popup_h > insets.top) {
        popup_y = -popup_h;
      } else {
        popup_y = -delta_y;
      }
    }

    return new Point(popup_x, popup_y);
  }

  /**
   * Copied from OptionsPanelBuilder.createUnderlinedButton and adapted to show rectangle around selected button instead
   * of just a line.
   *
   * Similar in behavior to a toggle button, but visually different:
   * - It does not paint the regular border or fill
   * - It underlines the selected item
   */
  public static AbstractButton createUnderlinedButton(Action aAction) {
    JToggleButton radio = new JToggleButton(aAction);
    UnderlineSelectedBorder border = new UnderlineSelectedBorder();
    radio.setBorder(border);
    radio.addMouseListener(border);
    radio.setContentAreaFilled(false);
    // some Look&Feels use background color as foreground when disabled&selected, and use a darker
    // background. As we've disabled filling the content area, we need a different color to make
    // sure the text is readable.
    radio.setBackground(UIColors.getUIColor("ToggleButton.disabledText", UIColors.fgHint()));
    radio.setFocusPainted(false);
    Font font = radio.getFont();
    radio.setFont(font.deriveFont((float) font.getSize() - 1));
    return radio;
  }

  /**
   * Border that draws a line all around when the associated button is selected.
   */
  public static class UnderlineSelectedBorder extends MouseAdapter implements Border {
    private static final int PAD = 3;
    private static final Border SELECTED = new LineBorder(UIColors.fgAccent(), PAD);
    private static final Border HOOVER = new LineBorder(transparent(UIColors.fgAccent()), PAD);

    private boolean fHoover = false;

    private static Color transparent(Color aColor) {
      return new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), 128);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      fHoover = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
      fHoover = false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      if (c instanceof AbstractButton) {
        AbstractButton b = (AbstractButton) c;
        if (b.isSelected()) {
          SELECTED.paintBorder(c, g, x, y, width, height);
        } else if (fHoover) {
          HOOVER.paintBorder(c, g, x, y, width, height);
        }
      } else if (c instanceof JPanel) {
        SELECTED.paintBorder(c, g, x, y, width, height);
      }
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(PAD, PAD, PAD, PAD);
    }
  }
}
