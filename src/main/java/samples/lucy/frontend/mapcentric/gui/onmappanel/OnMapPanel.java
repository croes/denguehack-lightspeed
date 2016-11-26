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
package samples.lucy.frontend.mapcentric.gui.onmappanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.PanelUI;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.gui.AnimatedResizingPanel;

/**
 * Panel that is composed of two things:
 * - header: a clickable header with a title that allows to show or hide the user content
 * - user content: the actual content
 *
 * It uses animations, see AnimatedResizingPanel.
 *
 */
public class OnMapPanel extends AnimatedResizingPanel {
  private final ToggleExpansionAction fToggleExpansionAction = new ToggleExpansionAction();
  private final JPanel fHeaderPanel;
  final HeaderButton fCollapseExpandButton;
  private final HeaderButton fTitleButton;
  private final JComponent fUserContent;
  private final JComponent fContent;
  private boolean fCollapsed = false;

  // Fetch colors etc. from the UI manager so that they can be configured if needed
  private static <T> T getUIValue(String aName, T aDefault) {
    Object value = UIManager.get("OnMapPanel." + aName);
    if ( value == null ) {
      value = aDefault;
    }
    //noinspection unchecked
    return (T) value;
  }

  public OnMapPanel(JComponent aUserContent) {
    fHeaderPanel = new HeaderPanel();
    fHeaderPanel.setLayout(new BoxLayout(fHeaderPanel, BoxLayout.LINE_AXIS));
    fHeaderPanel.setOpaque(false);
    fHeaderPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        fCollapseExpandButton.doClick();
      }
    });

    fCollapseExpandButton = new HeaderButton(fToggleExpansionAction);
    fCollapseExpandButton.setText(null);
    fTitleButton = new HeaderButton();
    fTitleButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fCollapseExpandButton.doClick();
      }
    });

    fHeaderPanel.add(fTitleButton);
    fHeaderPanel.add(Box.createHorizontalGlue());
    fHeaderPanel.add(fCollapseExpandButton);

    fUserContent = aUserContent;
    fContent = createContent();

    setLayout(new BorderLayout());
    add(fContent, BorderLayout.CENTER);
  }

  @Override
  public void setUI(PanelUI ui) {
    super.setUI(ui);
    Integer t = getUIValue("borderThickness", 1);
    setBorder(BorderFactory.createMatteBorder(t, t, t, t, getUIValue("borderColor", new Color(153, 157, 166))));
  }

  public JComponent getUserContent() {
    return fUserContent;
  }

  private JComponent createContent() {
    // The content pref size depends on being collapsed or not. If collapsed, only the space for
    // the header (north part) is requested. When expanded, the regular pref size including the
    // user content is returned.
    // A simpler approach would be to use setVisible on the user content, but then you
    // wouldn't see the content during the collapse animation.
    JPanel content = new JPanel(new BorderLayout()) {
      @Override
      public Dimension getPreferredSize() {
        if (fCollapsed) {
          return getPrefCollapsedSize();
        } else {
          return super.getPreferredSize();
        }
      }
    };
    content.setOpaque(false);
    content.add(fHeaderPanel, BorderLayout.NORTH);
    content.add(fUserContent, BorderLayout.CENTER);
    return content;
  }

  public Dimension getPrefCollapsedSize() {
    Dimension pref = fHeaderPanel.getPreferredSize();
    Insets i = fContent.getInsets();
    pref.width += i.left + i.right;
    pref.height += i.top + i.bottom;
    return pref;
  }

  public boolean isCollapsed() {
    return fCollapsed;
  }

  public void setCollapsed(boolean aCollapsed) {
    boolean old = isCollapsed();
    fCollapsed = aCollapsed;
    fToggleExpansionAction.updateIcon();
    revalidate();
    firePropertyChange("collapsed", old, aCollapsed);
  }

  public String getTitle() {
    return fTitleButton.getText();
  }

  public void setTitle(String aTitle) {
    String old = getTitle();
    fTitleButton.setText(aTitle);
    firePropertyChange("title", old, aTitle);
  }

  /**
   * Remove this panel from its parent, with a collapse animation (if expanded).
   */
  public void removeWithAnimation() {
    if (!isCollapsed() && isShowing()) {
      setAfterNextAnimationRunnable(new Runnable() {
        @Override
        public void run() {
          getParent().remove(OnMapPanel.this);
        }
      });
      setCollapsed(true);
    } else {
      getParent().remove(this);
    }
  }

  /**
   * Sets the icon which will be shown in the title of the panel
   * @param aTitleIcon The icon. {@code null} to remove the icon.
   */
  public void setTitleIcon(ILcdIcon aTitleIcon) {
    fTitleButton.setIcon(aTitleIcon != null ? new TLcdSWIcon(aTitleIcon) : null);
  }

  /**
   * Actions that shows or hides the user content.
   */
  private class ToggleExpansionAction extends AbstractAction {
    private final Icon fCollapsedIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.COLLAPSED_PANEL_ICON));
    private final Icon fExpandedIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.EXPANDED_PANEL_ICON));

    public ToggleExpansionAction() {
      super("Show/Hide");
      updateIcon();
    }

    void updateIcon() {
      putValue(Action.SMALL_ICON, isCollapsed() ? fCollapsedIcon : fExpandedIcon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setCollapsed(!isCollapsed());
    }
  }

  /**
   * The difference with a regular button is that it looks differently: a gradient is painted
   * on the background.
   */
  class HeaderButton extends JButton {
    private Color fGradientEnd;
    private Color fGradientStart;

    public HeaderButton() {
      super();
    }

    public HeaderButton(Action aAction) {
      super(aAction);
    }

    @Override
    public void setUI(ButtonUI ui) {
      super.setUI(ui);
      setContentAreaFilled(false);
      setBorderPainted(false);
      setHorizontalAlignment(SwingConstants.LEFT);
      setMargin(new Insets(2, 2, 2, 2));
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      setFont(getUIValue("font", new Font(Font.DIALOG, Font.BOLD, 12)));
      setForeground(getUIValue("textColor", Color.WHITE));

      fGradientStart = getUIValue("backgroundGradientStart", Color.LIGHT_GRAY);
      fGradientEnd = getUIValue("backgroundGradientEnd", Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, getHeight()),
                                           new float[]{0, 1}, new Color[]{fGradientStart, fGradientEnd}));
      g.fillRect(0, 0, getWidth(), getHeight());
      super.paintComponent(g);
    }
  }

  private class HeaderPanel extends JPanel {
    private Color fGradientEnd;
    private Color fGradientStart;

    @Override
    public void setUI(PanelUI ui) {
      super.setUI(ui);

      fGradientStart = getUIValue("backgroundGradientStart", Color.LIGHT_GRAY);
      fGradientEnd = getUIValue("backgroundGradientEnd", Color.DARK_GRAY);
    }
    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, getHeight()),
                                           new float[]{0, 1}, new Color[]{fGradientStart, fGradientEnd}));
      g.fillRect(0, 0, getWidth(), getHeight());
      super.paintComponent(g);
    }
  }
}
