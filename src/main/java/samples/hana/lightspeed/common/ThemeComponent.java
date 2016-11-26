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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdRotatingIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.TLcdStatusEventSupport;
import com.luciad.view.lightspeed.ILspView;

import samples.common.HaloLabel;

/**
 * Component that allows to cycle through a number of so-called themes. Each theme is represented by a UI and a title.
 * At the top it displays the theme controls:
 * - left and right arrows to cycle through the themes
 * - an indication of the current theme.
 *
 * Animations are used to cycle through the themes.
 */
public class ThemeComponent extends JPanel {
  private final Theme[] fThemes;
  private final JPanel fDots = new JPanel();
  private SlidePanel fThemeContent;

  public static ThemeComponent create(Theme... aThemes) {
    return new ThemeComponent(aThemes);
  }

  private ThemeComponent(Theme... aThemes) {
    fThemes = aThemes;
    setOpaque(true);
    setBackground(ColorPalette.ui_background);

    fThemeContent = createThemeContent(aThemes);

    setLayout(new BorderLayout());
    add(createThemeControls(), BorderLayout.NORTH);
    add(fThemeContent, BorderLayout.CENTER);

    moveTheme(0);

    // Eat all mouse events that are not already consumed by child UI widgets. This makes sure they
    // never reach the map and cause unexpected behavior (e.g. a click can deselect an object on the map)
    addMouseListener(new MouseAdapter() {
    });
    addMouseMotionListener(new MouseMotionAdapter() {
    });
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  protected SlidePanel createThemeContent(Theme[] aThemes) {
    SlidePanel content = new SlidePanel();
    content.setOpaque(false);

    for (Theme theme : aThemes) {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setOpaque(false);
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      HaloLabel title = FontStyle.createHaloLabel(theme.getName(), FontStyle.H1, true);
      JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
      titleBox.setOpaque(false);
      titleBox.add(title);
      POI poi = theme.getPOI();
      if (poi != null) {
        titleBox.add(Box.createHorizontalStrut(5));
        titleBox.add(UIUtil.createFitButton(poi, theme.getView()));
      }
      String infoFileName = theme.getInfoFileName();
      if (infoFileName != null) {
        titleBox.add(UIUtil.createInfoButton(infoFileName));
      }
      ILcdIcon busy = TLcdIconFactory.create(TLcdIconFactory.BUSY_ANIMATED_ICON);
      final JLabel loadingIcon = new JLabel(new TLcdSWIcon(busy), SwingConstants.CENTER);
      titleBox.add(Box.createHorizontalStrut(5));
      titleBox.add(loadingIcon);
      loadingIcon.setVisible(false);
      theme.addBusyListener(new ILcdStatusListener() {
        @Override
        public void statusChanged(final TLcdStatusEvent aStatusEvent) {
          EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              if (aStatusEvent.getID() == TLcdStatusEvent.START_BUSY) {
                loadingIcon.setVisible(true);
              } else if (aStatusEvent.getID() == TLcdStatusEvent.END_BUSY) {
                loadingIcon.setVisible(false);
              }
            }
          });
        }
      });

      panel.add(titleBox);
      panel.add(Box.createVerticalStrut(15));
      panel.add(theme.getGui());
      content.add(panel);
    }

    return content;
  }

  protected JPanel createThemeControls() {
    TLcdSymbol upIcon = new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 11, ColorPalette.text);
    TLcdRotatingIcon leftIcon = new TLcdRotatingIcon(upIcon, false);
    leftIcon.rotate(-Math.PI / 2);
    TLcdRotatingIcon rightIcon = new TLcdRotatingIcon(upIcon, false);
    rightIcon.rotate(Math.PI / 2);

    JButton left = createThemeSwitchButton(leftIcon);
    JButton right = createThemeSwitchButton(rightIcon);
    left.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveTheme(-1);
      }
    });
    right.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveTheme(+1);
      }
    });

    JPanel controls = new JPanel(new BorderLayout());
    controls.setOpaque(false);
    controls.add(left, BorderLayout.WEST);
    controls.add(right, BorderLayout.EAST);
    controls.add(fDots, BorderLayout.CENTER);

    fDots.setOpaque(false);
    fDots.setBorder(new EmptyBorder(3, 0, 0, 0));

    JPanel spaceEater = new JPanel(new BorderLayout());
    spaceEater.setOpaque(false);
    spaceEater.add(controls, BorderLayout.NORTH); // remaining space goes to (empty) center

    return spaceEater;
  }

  protected JButton createThemeSwitchButton(ILcdIcon aIcon) {
    JButton button = new JButton(new TLcdSWIcon(aIcon));
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    return button;
  }

  public void moveTheme(int aDelta) {
    if (fThemes.length == 0) {
      return;
    }
    fThemes[fThemeContent.getSelectedIndex()].deactivate();
    fThemeContent.setSelectedIndex(Math.max(Math.min(fThemeContent.getSelectedIndex() + aDelta, fThemes.length - 1), 0));
    fThemes[fThemeContent.getSelectedIndex()].activate();

    fDots.removeAll();
    for (int i = 0; i < fThemes.length; i++) {
      Color color = (i != fThemeContent.getSelectedIndex()) ? Color.lightGray : Color.white;
      fDots.add(new JLabel(new TLcdSWIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 5, color))));
    }
    fDots.revalidate();
  }

  public static abstract class Theme {
    private final String fName;
    private final String fInfoFileName;
    private final POI fPOI;
    private final TLcdStatusEventSupport fBusyListeners = new TLcdStatusEventSupport();

    protected Theme(String aName, String aInfoFileName, POI aPOI) {
      fName = aName;
      fInfoFileName = aInfoFileName;
      fPOI = aPOI;
    }

    public String getName() {
      return fName;
    }

    public String getInfoFileName() {
      return fInfoFileName;
    }

    public POI getPOI() {
      return fPOI;
    }

    public abstract ILspView getView();

    public abstract Component getGui();

    public abstract void activate();

    public void deactivate() {
    }

    public void addBusyListener(ILcdStatusListener aStatusListener) {
      fBusyListeners.addStatusListener(aStatusListener);
    }

    protected ILcdStatusListener getListener() {
      return fBusyListeners.asListener();
    }
  }

  // Point-of-interest with a description.
  // Draw rectangle in Lucy drawing and copy coordinates here (change them to DD.dddddddd in Edit menu).
  // Choose South-West for the location of the bounds. Don't forget to flip lat/lon.
  public static class POI extends TLcdLonLatBounds {
    private final String fDescription;

    public POI(double aLon, double aLat, double aWidth, double aHeight, String aDescription) {
      super(aLon, aLat, aWidth, aHeight);
      fDescription = aDescription;
    }

    public String getDescription() {
      return fDescription;
    }
  }
}
