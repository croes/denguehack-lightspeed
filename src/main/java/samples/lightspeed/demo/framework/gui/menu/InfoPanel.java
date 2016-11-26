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
package samples.lightspeed.demo.framework.gui.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.lightspeed.ILspAWTView;

import samples.lightspeed.demo.framework.gui.DemoUIColors;

public class InfoPanel extends JPanel {

  private static final int WIDTH = 650;

  // The color of the background layer of the slide menu
  private Color fMenuColor;
  private ILspAWTView fAWTView;
  private boolean fInView = false;
  private JEditorPane fPane;

  private ComponentAdapter fListener = new ComponentAdapter() {
    @Override
    public void componentResized(ComponentEvent e) {
      int preferredHeight = (int) getPreferredSize().getHeight();
      setLocation((e.getComponent().getWidth() - WIDTH) / 2,
                  (e.getComponent().getHeight() - preferredHeight) / 2);
    }
  };

  /////////////////////////////

  public InfoPanel(ILspAWTView aAWTView) {
    fAWTView = aAWTView;
    initGUI();
  }

  private void initGUI() {
    fMenuColor = DemoUIColors.INFO_PANEL_COLOR;
    setOpaque(false);
    setLayout(new BorderLayout());
    setSize(WIDTH, WIDTH);
    fPane = new JEditorPane("text/html", "");
    fPane.setEditable(false);
    fPane.setEnabled(false);
    fPane.setDisabledTextColor(new Color(0.9f, 0.9f, 0.9f, 0.85f));
    fPane.setForeground(new Color(0.9f, 0.9f, 0.9f, 0.85f));
    fPane.setBackground(new Color(0f, 0f, 0f, 0f));
    fPane.setOpaque(false);
    fPane.setMargin(new Insets(5, 5, 5, 5));
    fPane.setFont(new Font("Arial", Font.PLAIN, 13));
    add(fPane, BorderLayout.CENTER);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            removeFromView();
          }
        }
    );
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.add(closeButton);
    buttonPanel.setOpaque(false);
    buttonPanel.setSize(fAWTView.getHostComponent().getWidth() / 2, 30);
    add(buttonPanel, BorderLayout.SOUTH);
    setMaximumSize(new Dimension(WIDTH, fAWTView.getHostComponent().getHeight()));
    setMinimumSize(new Dimension(WIDTH, 0));
    setLocation(fAWTView.getHostComponent().getWidth() / 2, (fAWTView.getHostComponent().getHeight() - WIDTH) / 2);
  }

  public void addToView() {
    if (!fInView) {
      fAWTView.getOverlayComponent().add(this, TLcdOverlayLayout.Location.NO_LAYOUT);
      fAWTView.getOverlayComponent().addComponentListener(fListener);
      fInView = true;
      fAWTView.invalidate(true, this, "panel added");
    }
  }

  public void removeFromView() {
    if (fInView) {
      fAWTView.getOverlayComponent().remove(this);
      fAWTView.getHostComponent().removeComponentListener(fListener);
      JComponent overlayComponent = (JComponent) fAWTView.getOverlayComponent();
      overlayComponent.revalidate();
      overlayComponent.repaint();
      fInView = false;
      fAWTView.invalidate(true, this, "panel removed");
    }
  }

  public void setInfoMessage(String aMessage) {
    fPane.setText(aMessage);
    fPane.setSize(new Dimension(WIDTH, Integer.MAX_VALUE));
    int preferredHeight = (int) getPreferredSize().getHeight();
    setLocation((fAWTView.getHostComponent().getWidth() - WIDTH) / 2,
                (fAWTView.getHostComponent().getHeight() - preferredHeight) / 2);
    setSize(WIDTH, preferredHeight);
    revalidate();
    addToView();
    setVisible(true);
    updateUI();
    fAWTView.invalidate(true, this, "panel updated");
  }

  /**
   * Overwritten paint to draw a semi-transparent background underneath the content of this panel.
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D graphics2D = (Graphics2D) g;

    // Draw background rectangle
    graphics2D.setColor(fMenuColor);
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
    graphics2D.setColor(DemoUIColors.PANEL_BORDER_COLOR);
    graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
  }
}
