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
package samples.lightspeed.demo.application.data.los;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.model.ILcdModel;
import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.tea.TLcdCoverageAltitudeMode;
import com.luciad.tea.lightspeed.los.TLspLOSCalculator;
import com.luciad.tea.lightspeed.los.view.TLspLOSCoverageStyle;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.gui.ColorMapLegend;
import samples.lightspeed.demo.application.gui.menu.IThemePanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;
import samples.tea.lightspeed.los.view.LOSCoverageStyler;

/**
 * Builder for panel that allow the user to modify the style mode of
 * a LOS Coverage: either a default color-code 3D surface or a draped
 * visibility coverage.
 */
class LOSStylePanelBuilder implements IThemePanelFactory {

  /**
   * The levels for regular line-of-sight.
   */
  private static final double[] LOS_LEVELS = new double[]{
      ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE,  // value used for unknown visibilities
      0, 50, 100, 250,
  };

  /**
   * The colors for regular line-of-sight.
   */
  private static final Color[] LOS_COLORS = new Color[]{
      Color.blue,                                 // color used for unknown visibilities
      Color.red,
      new Color(220, 100, 10),
      Color.yellow,
      Color.green,
  };

  /**
   * The levels for regular line-of-sight.
   */
  private static final double[] VISIBILITY_LEVELS = new double[]{
      ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE,  // value used for unknown visibilities
      0,
      5.001
  };

  /**
   * The colors for regular line-of-sight visibility.
   */
  private static final Color[] VISIBILITY_COLORS = new Color[]{
      Color.blue,                                 // color used for unknown visibilities
      DemoUIColors.TRANSPARENT,
      Color.DARK_GRAY
  };

  private final TLcdColorMap fLOSColorMap;
  private final TLcdColorMap fVisibilityColorMap;
  private LOSMode fMode = LOSMode.DEFAULT;
  private TLspLOSCoverageStyle fLOSStyle;
  private TLspLOSCoverageStyle fVisibilityStyle;
  private LOSCoverageStyler fLOSCoverageStyler;
  private List<JPanel> fThemePanels;
  private TLspLOSCalculator fLOSCalculator;
  private ILcdModel fInputModel;
  private JPanel fContentPanel;
  private ColorMapLegend fColorMapLegend;

  /**
   * Creates a new LOS style panel builder with default color settings.
   *
   * @param aLOSCoverageStyler the styler responsible for styling the LOS coverages
   * @param aLOSCalculator     the LOS calculator that will calculate the LOS values
   * @param aInputModel        the input model based upon which the LOS model is calculated
   */
  LOSStylePanelBuilder(LOSCoverageStyler aLOSCoverageStyler, TLspLOSCalculator aLOSCalculator, ILcdModel aInputModel) {
    this(aLOSCoverageStyler, aLOSCalculator, aInputModel, LOS_LEVELS, LOS_COLORS, VISIBILITY_LEVELS, VISIBILITY_COLORS);
  }

  /**
   * Creates a new LOS style panel builder with the given color settings.
   *
   * @param aLOSCoverageStyler the styler responsible for styling the LOS coverages
   * @param aLOSCalculator     the LOS calculator that will calculate the LOS values
   * @param aInputModel        the input model based upon which the LOS model is calculated
   * @param aLOSLevels         the levels for the LOS color map
   * @param aLOSColors         the colors that are associated to the given LOS levels
   * @param aLOSLevels         the levels for the visibility color map
   * @param aLOSColors         the colors that are associated to the given visibility levels
   */
  public LOSStylePanelBuilder(LOSCoverageStyler aLOSCoverageStyler,
                              TLspLOSCalculator aLOSCalculator,
                              ILcdModel aInputModel,
                              double[] aLOSLevels,
                              Color[] aLOSColors,
                              double[] aVisibilityLevels,
                              Color[] aVisibilityColors) {
    fLOSCoverageStyler = aLOSCoverageStyler;
    fInputModel = aInputModel;
    fLOSCalculator = aLOSCalculator;
    fLOSCalculator.setCoverageAltitudeMode(TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL);
    fLOSColorMap = new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE),
                                    aLOSLevels, aLOSColors);

    fLOSColorMap.setMasterOpacity(64);
    fLOSStyle = TLspLOSCoverageStyle.newBuilder().colorMap(fLOSColorMap).elevationMode(ElevationMode.ABOVE_ELLIPSOID).build();
    fLOSCoverageStyler.setLOSCoverageStyle(fLOSStyle);
    fVisibilityColorMap = new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE),
                                           aVisibilityLevels,
                                           aVisibilityColors);
    fVisibilityColorMap.setMasterOpacity(128);
    fVisibilityStyle = TLspLOSCoverageStyle.newBuilder()
                                           .colorMap(fVisibilityColorMap)
                                           .elevationMode(ElevationMode.ON_TERRAIN)
                                           .build();
  }

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    if (fThemePanels == null) {
      fThemePanels = createNewThemePanels();
    }
    return fThemePanels;
  }

  private List<JPanel> createNewThemePanels() {
    boolean isTouchUI = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

    DefaultFormBuilder builder;

    if (isTouchUI) {
      builder = new DefaultFormBuilder(new FormLayout("p"));
      builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));
    } else {
      builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p"));
    }
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Toggle Style", 15, true) {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(200, 25);
      }
    };
    builder.append(titleLabel, isTouchUI ? 1 : 3);
    builder.nextLine();

    // Add radio button for regular style
    AbstractButton losButton = isTouchUI ? new JButton("Gradient coloring") : new JRadioButton();
    losButton.setSelected(true);
    losButton.setOpaque(false);
    losButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setMode(LOSMode.DEFAULT);
      }
    });

    builder.append(losButton);
    if (!isTouchUI) {
      builder.append(new HaloLabel("Line of Sight"));
    }
    builder.nextLine();

    // Add radio button for visibility style
    AbstractButton visibilityButton = isTouchUI ? new JButton("Binary coloring") : new JRadioButton();
    visibilityButton.setSelected(false);
    visibilityButton.setOpaque(false);
    visibilityButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setMode(LOSMode.VISIBILITY);
      }
    });

    builder.append(visibilityButton);
    if (!isTouchUI) {
      builder.append(new HaloLabel("Visibility"));
    }
    builder.nextLine();

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(losButton);
    buttonGroup.add(visibilityButton);

    builder.appendSeparator();

    TLcdColorMap newColorMap = fMode.createColorMapToVisualize(fLOSColorMap);
    fColorMapLegend = new ColorMapLegend(newColorMap, ColorMapLegend.Orientation.LEFT_TO_RIGHT);
    fColorMapLegend.setPreferredSize(new Dimension(70, 25));
    builder.append(fColorMapLegend, builder.getColumnCount());

    JPanel legendLabels = new JPanel();
    BoxLayout layout = new BoxLayout(legendLabels, BoxLayout.X_AXIS);
    legendLabels.setLayout(layout);
    legendLabels.setPreferredSize(fColorMapLegend.getPreferredSize());
    legendLabels.setOpaque(false);

    HaloLabel visible = new HaloLabel("Visible", 12, false);
    visible.setAlignmentX(Component.LEFT_ALIGNMENT);
    HaloLabel invisible = new HaloLabel("Invisible", 12, false);
    invisible.setAlignmentX(Component.RIGHT_ALIGNMENT);

    legendLabels.add(visible);
    legendLabels.add(Box.createHorizontalGlue());
    legendLabels.add(invisible);

    builder.append(legendLabels, builder.getColumnCount());
    builder.nextLine();

    // Create content panel and set its size. Not doing the
    // latter will cause it to be invisible, due to no size.
    fContentPanel = builder.getPanel();
    fContentPanel.setSize(fContentPanel.getLayout().preferredLayoutSize(fContentPanel));

    List<JPanel> panels = new ArrayList<JPanel>();
    panels.add(fContentPanel);

    return panels;
  }

  public void setMode(LOSMode aLOSMode) {
    if (fMode == aLOSMode) {
      return;
    }
    switch (aLOSMode) {
    case DEFAULT:
      fLOSCalculator.setCoverageAltitudeMode(TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL);
      fLOSCoverageStyler.setLOSCoverageStyle(fLOSStyle);
      fColorMapLegend.setColorMap(aLOSMode.createColorMapToVisualize(fLOSColorMap));
      break;
    case VISIBILITY:
      fLOSCalculator.setCoverageAltitudeMode(TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL);
      fLOSCoverageStyler.setLOSCoverageStyle(fVisibilityStyle);
      fColorMapLegend.setColorMap(aLOSMode.createColorMapToVisualize(fVisibilityColorMap));
      break;
    default:
    }
    fMode = aLOSMode;
    fInputModel.elementChanged(fInputModel.elements().nextElement(), ILcdModel.FIRE_NOW);
  }

  private enum LOSMode {

    DEFAULT {
      @Override
      protected double getMaximumLevel(double aColorMapMaximumLevel) {
        return aColorMapMaximumLevel;
      }
    },
    VISIBILITY {
      @Override
      protected double getMaximumLevel(double aColorMapMaximumLevel) {
        return Math.max(VISIBILITY_LEVELS[VISIBILITY_LEVELS.length - 1] * 2, aColorMapMaximumLevel);
      }
    };

    protected abstract double getMaximumLevel(double aColorMapMaximumLevel);

    /**
     * Removes the unknown color mapping because that hardly ever shows up and might confuse users.
     */
    TLcdColorMap createColorMapToVisualize(TLcdColorMap aColorMap) {
      double[] levels = new double[aColorMap.getLevelCount() - 1];
      for (int i = 0; i < aColorMap.getLevelCount() - 1; i++) {
        levels[i] = aColorMap.getLevel(i + 1);
      }
      Color[] colors = new Color[aColorMap.getColorCount() - 1];
      for (int i = 0; i < aColorMap.getColorCount() - 1; i++) {
        colors[i] = aColorMap.getColor(i + 1);
      }
      return new TLcdColorMap(new TLcdInterval(0, getMaximumLevel(aColorMap.getLevel(aColorMap.getLevelCount() - 1))), levels, colors);
    }

  }

}
