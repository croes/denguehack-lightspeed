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
package samples.gxy.labels.interactive;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;

/**
 * Swing component that can be used to create a non-interactive label stamp.
 */
public class RegularSwingLabelComponent extends JPanel {

  private static final Color BACKGROUND_COLOR = new Color(191, 205, 219);
  private static final Color FOREGROUND_COLOR = new Color(67, 78, 84);

  private final JLabel fTitle = new JLabel();
  private final JLabel fCommentsLabel = new JLabel();

  private final Map<Object, String> fCitiesCommentMap;

  public RegularSwingLabelComponent(Map<Object, String> aCitiesCommentMap) {
    fCitiesCommentMap = aCitiesCommentMap;
    setLayout(new GridBagLayout());
    initUI();
    setCommentsFont(Font.decode("Default-BOLD-12"));
  }

  public void setLabelFont(Font aFont) {
    fTitle.setFont(aFont);
  }

  public void setCommentsFont(Font aFont) {
    fCommentsLabel.setFont(aFont);
  }

  public void setObject(Object aObject) {
    fTitle.setText(aObject.toString());
    String comment = fCitiesCommentMap.get(aObject);
    fCommentsLabel.setText(comment == null ? "" : comment);
    fCommentsLabel.setBorder(comment == null ? null : BorderFactory.createEmptyBorder(5, 5, 4, 5));
  }

  private void initUI() {
    setBorder(BorderFactory.createLineBorder(Color.black, 1));
    Component title_panel = createTitleComponent();

    GridBagConstraints gbc = new GridBagConstraints(
        0, 0, 1, 1,
        1.0, 0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
    );

    add(title_panel, gbc);

    gbc.gridy++;
    add(fCommentsLabel, gbc);
  }

  private Component createTitleComponent() {
    // configure the title label
    fTitle.setOpaque(true);
    fTitle.setBackground(BACKGROUND_COLOR);
    fTitle.setForeground(FOREGROUND_COLOR);
    fTitle.setHorizontalAlignment(JLabel.CENTER);

    // add all the components with the correct layout
    JPanel title_panel = new MyPanel(new GridBagLayout());
    title_panel.setBackground(BACKGROUND_COLOR);
    title_panel.setOpaque(true);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 5, 0, 5);
    gbc.ipadx = 4;
    gbc.ipady = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    title_panel.add(fTitle, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0d;
    gbc.ipadx = 0;
    gbc.insets = new Insets(1, 0, 1, 0);

    // We do not insert icon labels. So we should provide a component that takes
    // as much space as these labels, to prevent that there is a difference in size between
    // the title panel of an interactive and a non-interactive label.
    JLabel tick = createIconLabel(TLcdIconFactory.create(TLcdIconFactory.APPLY_CHANGES_ICON));
    title_panel.add(Box.createVerticalStrut(tick.getPreferredSize().height), gbc);

    return title_panel;
  }

  private JLabel createIconLabel(ILcdIcon aIcon) {
    JLabel icon_label = new JLabel(new TLcdSWIcon(aIcon));
    icon_label.setOpaque(true);
    return icon_label;
  }

  /**
   * Subclass of JPanel that paints a visual indication that the title can be dragged.
   */
  private static class MyPanel extends JPanel {

    private static final int OFFSET = 2;
    private static final int PATTERN_WIDTH = 2;
    private static final int PATTERN_HEIGHT = 2;

    private final TexturePaint fPaint;

    public MyPanel(GridBagLayout aLayout) {
      super(aLayout);
      BufferedImage image = new BufferedImage(PATTERN_WIDTH, PATTERN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      image.setRGB(0, 0, toInt(255, 255, 255, 100));
      fPaint = new TexturePaint(image, new Rectangle(OFFSET, OFFSET, PATTERN_WIDTH, PATTERN_HEIGHT));
    }

    private static int toInt(int aR, int aG, int aB, int aA) {
      return new Color(aR, aG, aB, aA).getRGB();
    }

    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(fPaint);
      g2d.fillRect(OFFSET, OFFSET, getWidth() - 2 * OFFSET, getHeight() - 2 * OFFSET);
    }
  }
}
