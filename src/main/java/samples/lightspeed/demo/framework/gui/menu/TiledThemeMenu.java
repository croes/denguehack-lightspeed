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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ILspAWTView;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;

/**
 * Menu to activate a theme. Each theme is represented by a tile with an image.
 */
public class TiledThemeMenu {

  private static final double FADE_IN_DURATION_SECONDS = 0.25;
  private ILspAWTView fAWTView;
  private boolean fInView = false;
  private BufferedImage fIconRepresentation;

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(TiledThemeMenu.class);

  private JPanel fMainPanel = new JPanel();
  private JLayer<JPanel> fMainLayer = new JLayer<>(fMainPanel);

  private float fOpacity = 0f;
  private AbstractTheme fActiveTheme = null;

  private ComponentAdapter fListener = new ComponentAdapter() {
    @Override
    public void componentResized(ComponentEvent e) {
      removeFromView();
      initGUI();
    }
  };

  public TiledThemeMenu(ILspAWTView aAWTView) {
    fAWTView = aAWTView;
    MouseAdapter dummyMouseAdapter = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        removeFromView();
      }
    };
    fMainPanel.addMouseListener(dummyMouseAdapter);
    fMainPanel.addMouseMotionListener(dummyMouseAdapter);
    fMainPanel.addMouseWheelListener(dummyMouseAdapter);
    initGUI();
    initIconRepresentation();
  }

  public BufferedImage getIconRepresentation() {
    return fIconRepresentation;
  }

  private void initGUI() {
    fMainPanel.removeAll();
    fMainPanel.setLayout(new GridBagLayout());
    fMainPanel.setBackground(DemoUIColors.SEMI_TRANSPARENT_PANEL_COLOR);
    //add tile for each theme
    Framework framework = Framework.getInstance();
    AbstractTheme[] themes = framework.getThemes();
    AbstractTheme activeTheme = framework.getActiveTheme();
    fActiveTheme = activeTheme;
    JPanel result = createThemePanel(themes, activeTheme);
    fMainPanel.add(result);
    fMainPanel.setLocation(0, 0);
    fMainLayer.setLocation(0, 0);
    fMainLayer.setUI(new LayerUI<JPanel>() {
      @Override
      public void paint (Graphics g, JComponent c) {
        // Paint offscreen image in view with transparency
        Graphics2D graphics2D = (Graphics2D)g;
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fOpacity));
        super.paint(graphics2D, c);
      }
    });
  }

  private void initIconRepresentation() {
    Framework framework = Framework.getInstance();
    AbstractTheme[] themes = framework.getThemes();
    String screenshotPath = Framework.getInstance().getProperty("theme.screenshot.path");

    BufferedImage[] images = new BufferedImage[themes.length];
    for (int i=0; i<images.length; i++) {
      String name = themes[i].getName();
      BufferedImage image = null;
      try {
        image = IOUtil.readImage(screenshotPath, name+".png");
      } catch (IOException e) {
        // ignored
      }
      images[i] = image;
    }

    fIconRepresentation = getIconRepresentation(3, 3, 10);
  }

  private BufferedImage getIconRepresentation(int aNbColumns, int aNbRows, int aCellSize) {
    int spacing = 2;
    BufferedImage result = new BufferedImage(spacing *(aNbColumns-1)+aNbColumns*aCellSize, spacing *(aNbRows-1)+aNbRows*aCellSize, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2d = result.createGraphics();
    for (int row = 0; row < aNbRows; row++) {
      for (int column = 0; column < aNbColumns; column++) {
        g2d.setColor(new Color(191, 191, 191, 235));
        g2d.fillRect(spacing * column + column * aCellSize, spacing * row + row * aCellSize, aCellSize, aCellSize);
      }
    }
    g2d.dispose();
    return result;
  }

  public void addToView() {
    if (ALcdAnimationManager.getInstance().getAnimation(this)!=null) {
      return;
    }
    if (!fInView) {
      if (fActiveTheme!=Framework.getInstance().getActiveTheme()) {
        initGUI();
      }
      fAWTView.getOverlayComponent().add(fMainLayer, TLcdOverlayLayout.Location.NO_LAYOUT);
      fAWTView.getOverlayComponent().addComponentListener(fListener);
      fAWTView.invalidate(true, this, "panel added");
      ALcdAnimationManager.getInstance().putAnimation(this, new ALcdAnimation(FADE_IN_DURATION_SECONDS) {
        @Override
        protected void setTimeImpl(double aTime) {
          fOpacity = (float) (aTime/getDuration());
          JComponent overlayComponent = (JComponent) fAWTView.getOverlayComponent();
          overlayComponent.revalidate();
          overlayComponent.repaint();
          fInView = true;
          fAWTView.invalidate(true, this, "panel fading");
        }
      });
    }
  }

  public void removeFromView() {
    if (ALcdAnimationManager.getInstance().getAnimation(this)!=null) {
      return;
    }
    if (fInView) {
      ALcdAnimationManager.getInstance().putAnimation(this, new ALcdAnimation(FADE_IN_DURATION_SECONDS) {
        @Override
        protected void setTimeImpl(double aTime) {
          fOpacity = 1f-(float) (aTime/getDuration());
          JComponent overlayComponent = (JComponent) fAWTView.getOverlayComponent();
          overlayComponent.revalidate();
          overlayComponent.repaint();
          fAWTView.invalidate(true, this, "panel fading");
        }

        @Override
        public void stop() {
          fAWTView.getOverlayComponent().remove(fMainLayer);
          fAWTView.getHostComponent().removeComponentListener(fListener);
          JComponent overlayComponent = (JComponent) fAWTView.getOverlayComponent();
          overlayComponent.revalidate();
          overlayComponent.repaint();
          fInView = false;
          fAWTView.invalidate(true, this, "panel removed");
        }
      });
    }
  }

  public void showPanel() {
    fMainPanel.setLocation(0, 0);
    fMainLayer.setLocation(0, 0);
    fMainPanel.setSize(fAWTView.getHostComponent().getWidth(), fAWTView.getHostComponent().getHeight());
    fMainLayer.setSize(fAWTView.getHostComponent().getWidth(), fAWTView.getHostComponent().getHeight());
    fMainPanel.revalidate();
    addToView();
    fMainPanel.setVisible(true);
    fMainPanel.updateUI();
    fAWTView.getOverlayComponent().setComponentZOrder(fMainLayer, 1);
    fAWTView.invalidate(true, this, "panel updated");
  }

  private Dimension getRelativeSize() {
    int width = (int) (fAWTView.getHostComponent().getWidth() * 0.9);
    int height = (int) (fAWTView.getHostComponent().getHeight() * 0.8);

    return new Dimension(width, height);
  }

  private Dimension getRowsAndColumns(AbstractTheme[] aThemes) {
    int numberOfThemes = aThemes.length;
    int numberOfColumns = Math.min(aThemes.length, 5);
    int numberOfRows = 1;

    while (numberOfColumns*numberOfRows < numberOfThemes) {
      numberOfRows++;
    }

    // If the last row only contains one tile, reduce the number of colums
    int numberOfTilesNotOnLastRow = numberOfThemes-numberOfColumns*(numberOfRows-1);
    if (numberOfTilesNotOnLastRow==1 && numberOfColumns > 1) {
      numberOfColumns--;
      numberOfRows = 1;
      while (numberOfColumns*numberOfRows < numberOfThemes) {
        numberOfRows++;
      }
    }
    return new Dimension(numberOfColumns, numberOfRows);
  }

  private JPanel createThemePanel(AbstractTheme[] aThemes, AbstractTheme aActiveTheme) {
    Arrays.sort(aThemes, new Comparator<AbstractTheme>() {
      @Override
      public int compare(AbstractTheme o1, AbstractTheme o2) {
        String themeName1 = o1.getName();
        String themeName2 = o2.getName();
        return themeName1.compareTo(themeName2);
      }
    });
    Dimension rowsAndColumns = getRowsAndColumns(aThemes);
    int numberOfColumns = rowsAndColumns.width;
    int numberOfRows = rowsAndColumns.height;

    Dimension d = getRelativeSize();
    int thumbnailSize = Math.min(400, Math.min(d.width/numberOfColumns , (d.height-10*numberOfRows)/numberOfRows));

    DefaultFormBuilder panelBuilder = createThemePanelBuilder(numberOfColumns, numberOfRows);
    panelBuilder.opaque(false);

    for (int i = 0; i < aThemes.length; i++) {
      JPanel themePanel = new JPanel();
      themePanel.setOpaque(false);
      themePanel.setLayout(new BorderLayout());
      AbstractTheme theme = aThemes[i];
      try {
        String screenshotPath = Framework.getInstance().getProperty("theme.screenshot.path");
        JButton themeButton = new ThemeButton(screenshotPath, theme.getName(), theme==aActiveTheme, thumbnailSize);
        themeButton.addActionListener(new ActivateThemeListener(theme));
        themePanel.add(themeButton, BorderLayout.NORTH);
      } catch (IOException e) {
        sLogger.error(e.getMessage());
      }

      panelBuilder.append(themePanel);
      if (i > 0 && (i + 1) % numberOfColumns == 0) {
        panelBuilder.nextRow();
      }
    }

    return panelBuilder.getPanel();
  }


  private DefaultFormBuilder createThemePanelBuilder(int aNumberOfColumns, int aNumberOfRows) {
    String layoutStringColumns = getLayoutString(aNumberOfColumns);
    String layoutStringRows = getLayoutString(aNumberOfRows);
    return new DefaultFormBuilder(new FormLayout(layoutStringColumns, layoutStringRows));
  }

  private String getLayoutString(int aCount) {
    String layoutStringColumns = "";
    for (int i = 0; i < aCount - 1; i++) {
      layoutStringColumns += "p, 0dlu, ";
    }
    layoutStringColumns += "p";
    return layoutStringColumns;
  }

  public boolean isInView() {
    return fInView;
  }

  private static class ThemeButton extends JButton {

    public ThemeButton(String aImagePath, String aTheme, boolean aHighlighted, int aSize) throws IOException {
      setVerticalTextPosition(BOTTOM);
      setHorizontalTextPosition(CENTER);
      setVerticalAlignment(BOTTOM);
      setIconTextGap(1);
      setText(aTheme);
      if (aHighlighted) {
        setForeground(DemoUIColors.HIGHLIGHTED_TEXT_COLOR);
      }
      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      BufferedImage image = null;
      try {
        image = IOUtil.readImage(aImagePath, aTheme+".png");
        image = resizeThemeImage(image, aSize, aSize);
       }
      catch (Exception e) {
        // Fallback if no image found
        image = new BufferedImage(aSize, aSize, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(new Color(14,52,72));
        graphics2D.fillRect(0, 0, aSize, aSize);
        graphics2D.dispose();
      }
      Icon icon = new ImageIcon(image);
      setIcon(icon);
      Icon focusIcon = new FocusIcon(icon);
      setRolloverIcon(focusIcon);
      setPressedIcon(focusIcon);

      setBackground(new Color(0f,0f,0f,0f));
      setHorizontalAlignment(CENTER);
      setOpaque(false);
    }

    private BufferedImage resizeThemeImage(BufferedImage aImage, int aWidth, int aHeight) {
      BufferedImage bi = new BufferedImage(aWidth, aHeight, BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics2D graphics2D = bi.createGraphics();
      graphics2D.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
      graphics2D.drawImage(aImage, 0, 0, aWidth, aHeight, null);
      graphics2D.dispose();
      return bi;
    }
  }

  private class ActivateThemeListener implements ActionListener {

    private final AbstractTheme fTheme;

    public ActivateThemeListener(AbstractTheme aTheme) {
      fTheme = aTheme;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Framework framework = Framework.getInstance();
      framework.setActiveTheme(fTheme);
      removeFromView();
    }
  }

}
