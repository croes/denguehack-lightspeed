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
package samples.gxy.labels.streets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYOnPathLabelingAlgorithm;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.common.MapColors;
import samples.gxy.labels.common.ConfigurableGXYPainterWrapper;

/**
 * This layer factory creates a highways layer. This highways layer consists of
 * - Interstate Highways
 * - US Highways
 * - State Highways
 *
 * This layer factory can be customized to use other data as well. For this, a set of
 * methods can be overridden :
 * - protected ILcdGXYPainter createHighwayPainter( ILcdGXYPainterStyle )
 * - protected ILcdGXYLabelingPathProvider createLabelingPathProvider( ILcdGXYPainter )
 * - protected HighwayType getHighwayType( Object )
 * - protected int getHighwayNumber( Object )
 */
class HighwaysLayerFactory implements ILcdGXYLayerFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(HighwaysLayerFactory.class.getName());

  protected enum HighwayType {
    INTERSTATE_ROUTE,
    US_ROUTE,
    STATE_ROUTE
  }

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(0.0, 1e-4),
      new TLcdInterval(1e-4, 5e-4),
      new TLcdInterval(5e-4, 1e-3),
      new TLcdInterval(1e-3, 2e-3),
      new TLcdInterval(2e-3, 5e-3),
      new TLcdInterval(5e-3, 2e-2),
      new TLcdInterval(2e-2, 1e-1),
      new TLcdInterval(1e-1, Double.MAX_VALUE)
  };

  private static final Color[] HIGHWAY_COLORS = {
      new Color(255, 175, 100),
      new Color(255, 230, 120),
      new Color(255, 255, 200)
  };

  private static final Color[] HIGHWAY_HALO_COLORS = {
      new Color(210, 140, 60),
      new Color(210, 200, 80),
      new Color(210, 210, 175)
  };

  private static final int[][] HIGHWAY_LINE_WIDTHS = {
      {0, 1, 3, 4, 6, 6, 10, 16},
      {0, 0, 1, 3, 4, 6, 10, 16},
      {0, 0, 0, 1, 3, 4, 8, 12}
  };

  private static final double[] LABEL_SCALES = {5e-4, 2e-3, 5e-3};

  @Override
  public ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel) {
    if (aModel == null) {
      return null;
    }

    TLcdGXYLayer layer = new HighwaysLayer("Highways");
    layer.setModel(aModel);

    layer.setLabeled(true);
    layer.setEditable(false);
    layer.setLabelsEditable(false);
    layer.setSelectable(true);
    layer.setVisible(true);
    layer.setGXYPen(new TLcdGeodeticPen());

    configureHighwayPaintingOrder(layer);
    configureHighwayPainting(layer);
    configureHighwayLabelPainting(layer);

    return layer;
  }

  private static ILcdGXYMultiLabelPriorityProvider createLabelPriorityProvider(final int aLargestPriority, final int aSmallestPriority) {
    return new ILcdGXYMultiLabelPriorityProvider() {
      @Override
      public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
        double relative_priority = getRelativePriority(aObject);
        double global_priority = getGlobalPriority(aGXYContext.getGXYView());
        double priority = global_priority * 0.5 + relative_priority * 0.5;
        return aLargestPriority + (int) (priority * (double) (aSmallestPriority - aLargestPriority));
      }

      private double getRelativePriority(Object aObject) {
        // Retrieve the priority of a label relative to an other label of the same layer.
        HighwayType highway_type = getHighwayType(aObject);

        double priority = 1.0;
        if (highway_type == HighwayType.INTERSTATE_ROUTE) {
          priority = 0.5;
        } else if (highway_type == HighwayType.US_ROUTE) {
          priority = 0.75;
        } else if (highway_type == HighwayType.STATE_ROUTE) {
          priority = 1.0;
        }

        return priority;
      }

      private double getGlobalPriority(ILcdGXYView aGXYView) {
        // Retrieve the global priority of highway labels. This priority is based on the view scale,
        // and makes sure labels of different layers interact correctly.
        int detail_level = getLevelOfDetail(aGXYView);
        if (detail_level == -1) {
          return 1.0;
        }
        return 1.0 - 0.1 * detail_level;
      }
    };
  }

  private static int getLevelOfDetail(ILcdGXYView aGXYView) {
    double scale = aGXYView.getScale();
    for (int i = 0; i < DETAIL_LEVELS.length; i++) {
      ILcdInterval interval = DETAIL_LEVELS[i];
      if (interval.getMin() <= scale && scale <= interval.getMax()) {
        return i;
      }
    }
    return -1;
  }

  public static ALcdGXYDiscretePlacementsLabelingAlgorithm createLabelingAlgorithm(int aLargestPriority, int aSmallestPriority) {
    TLcdGXYOnPathLabelingAlgorithm algorithm = new TLcdGXYOnPathLabelingAlgorithm();
    algorithm.setReusePreviousLocations(true);
    algorithm.setLabelPriorityProvider(createLabelPriorityProvider(aLargestPriority, aSmallestPriority));
    algorithm.setMinimumGap(300);
    algorithm.setAllowRotation(false);
    return algorithm;
  }

  private void configureHighwayPaintingOrder(TLcdGXYLayer aGXYLayer) {
    // Sort the objects based on importance. This is done to make sure that Interstate Routes are
    // drawn over US Routes, and that US Routes are drawn over State Routes.
    aGXYLayer.setModelElementComparator(new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        int highway_type1 = getHighwayType(o1).ordinal();
        int highway_type2 = getHighwayType(o2).ordinal();
        int diff = highway_type2 - highway_type1;
        return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
      }
    });
  }

  private void configureHighwayPainting(TLcdGXYLayer aGXYLayer) {
    ILcdGXYPainterProvider[] painter_provider_array = new ILcdGXYPainterProvider[2];
    painter_provider_array[0] = new HighwayPainter(true);
    painter_provider_array[1] = new HighwayPainter(false);
    aGXYLayer.setGXYPainterProviderArray(painter_provider_array);
  }

  private void configureHighwayLabelPainting(TLcdGXYLayer aGXYLayer) {
    // Create a label painter that draws highway icons
    TLcdGXYStampLabelPainter label_painter = new TLcdGXYStampLabelPainter(new HighwayLabelStamp()) {
      @Override
      public int getLabelCount(Graphics aGraphics, ILcdGXYContext aGXYContext) {
        return 2;
      }
    };
    aGXYLayer.setGXYLabelPainterProvider(label_painter);
    aGXYLayer.setGXYLabelEditorProvider(null);
    aGXYLayer.setLabeled(true);
  }

  /**
   * This label stamp chooses an icon to paint, depending on the highway type. It also paints
   * the highway number on top of this icon.
   */
  private class HighwayLabelStamp extends ALcdGXYLabelStamp {

    private Font NUMBER_FONT = new Font("Dialog", Font.BOLD, 12);
    private Color[] TEXT_COLORS = {Color.white, Color.darkGray, Color.darkGray};
    private int[] VERTICAL_OFFSETS = {2, 3, 3};
    private ILcdIcon[][] ICONS = {
        {
            new TLcdImageIcon("samples/images/highwayIcons/interstate_small.png"),
            new TLcdImageIcon("samples/images/highwayIcons/us_small.png"),
            new TLcdImageIcon("samples/images/highwayIcons/state_large.png")
        },
        {
            new TLcdImageIcon("samples/images/highwayIcons/interstate_large.png"),
            new TLcdImageIcon("samples/images/highwayIcons/us_large.png"),
            new TLcdImageIcon("samples/images/highwayIcons/state_large.png")
        }
    };

    @Override
    public void dimensionSFCT(Graphics aGraphics, Object aObject, int aLabelIndex, int aSubLabelIndex, int aMode, ILcdGXYContext aContext, Dimension aDimensionSFCT) throws TLcdNoBoundsException {
      HighwayType highway_type = getHighwayType(aObject);
      int highway_number = getHighwayNumber(aObject);
      ILcdIcon icon = getHighwayIcon(highway_type, highway_number);
      aDimensionSFCT.setSize(icon.getIconWidth(), icon.getIconHeight());
    }

    @Override
    public void paint(Graphics aGraphics, Object aObject, int aLabelIndex, int aSubLabelIndex, int aMode, ILcdGXYContext aContext, int aX, int aY, double aRotation) {
      HighwayType highway_type = getHighwayType(aObject);
      int highway_number = getHighwayNumber(aObject);
      String highway_string = Integer.toString(highway_number);

      if (aGraphics.getFont() != NUMBER_FONT) {
        aGraphics.setFont(NUMBER_FONT);
      }

      ILcdIcon icon = getHighwayIcon(highway_type, highway_number);
      icon.paintIcon(null, aGraphics, 0, 0);

      // Enable anti-aliasing for the label text
      Graphics2D g2d = (Graphics2D) aGraphics;
      RenderingHints rendering_hints = g2d.getRenderingHints();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Calculate the correct text position
      FontMetrics fm = aGraphics.getFontMetrics();
      Rectangle2D string_bounds = fm.getStringBounds(highway_string, aGraphics);
      double text_width = string_bounds.getWidth();
      double text_height = string_bounds.getHeight();
      int text_x = (int) (0.5 * ((double) icon.getIconWidth() - text_width) + 0.5);
      int text_y = (int) (0.5 * ((double) icon.getIconHeight() + text_height) + 0.5) - VERTICAL_OFFSETS[highway_type.ordinal()];

      // Draw the string in the correct color
      boolean selected = (aMode & ILcdGXYLabelPainter2.SELECTED) != 0;
      aGraphics.setColor(selected ? MapColors.SELECTION.darker() : TEXT_COLORS[highway_type.ordinal()]);
      aGraphics.drawString(highway_string, text_x, text_y);

      // Restore the rendering hints
      g2d.setRenderingHints(rendering_hints);
    }

    private ILcdIcon getHighwayIcon(HighwayType aHighwayType, int aHighwayNumber) {
      // Use a larger icon when the number has 3 or more digits.
      if (aHighwayNumber < 100) {
        return ICONS[0][aHighwayType.ordinal()];
      } else {
        return ICONS[1][aHighwayType.ordinal()];
      }
    }
  }

  /**
   * This painter adds the following functionality :
   * - anti-aliasing
   * - line widths and colors based on the highway type and the level of detail
   */
  private class HighwayPainter extends ConfigurableGXYPainterWrapper {

    private boolean fHaloPainter;
    private TLcdStrokeLineStyle fLineStyle;

    private Map<Integer, BasicStroke> fStrokes = new HashMap<Integer, BasicStroke>();

    public HighwayPainter(boolean aHaloPainter) {
      fHaloPainter = aHaloPainter;
      fLineStyle = TLcdStrokeLineStyle.newBuilder()
                                      .antiAliasing(true)
                                      .selectionColor(MapColors.SELECTION).build();
      setDelegate(createHighwayPainter(fLineStyle));
    }

    @Override
    protected boolean configurePainter(ILcdGXYPainter aPainter, ILcdGXYContext aGXYContext, Object aObject) {
      int level_of_detail = getLevelOfDetail(aGXYContext.getGXYView());
      if (level_of_detail == -1) {
        return false;
      }

      HighwayType highway_type = getHighwayType(aObject);
      int line_width = HIGHWAY_LINE_WIDTHS[highway_type.ordinal()][level_of_detail];
      if (!fHaloPainter) {
        line_width -= 2;
      }
      if (line_width <= 0) {
        return false;
      }

      Color color = fHaloPainter ? HIGHWAY_HALO_COLORS[highway_type.ordinal()] : HIGHWAY_COLORS[highway_type.ordinal()];

      fLineStyle.setStroke(getBasicStroke(line_width));
      fLineStyle.setColor(color);

      return true;
    }

    private BasicStroke getBasicStroke(int aWidth) {
      BasicStroke stroke = fStrokes.get(aWidth);
      if (stroke == null) {
        stroke = new BasicStroke(aWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        fStrokes.put(aWidth, stroke);
      }
      return stroke;
    }
  }

  private class HighwaysLayer extends TLcdGXYLayer {

    public HighwaysLayer(String aLabel) {
      super(aLabel);
    }

    @Override
    public int applyOnInteractLabels(final ILcdFunction aLabelFunction, Graphics aGraphics, int aPaintMode, ILcdGXYView aGXYView) {
      final double scale = aGXYView.getScale();

      // Only apply the function on labels when the scale is larger than a certain scale.
      ILcdFunction filter_function = new ILcdFunction() {
        @Override
        public boolean applyOn(Object aObject) throws IllegalArgumentException {
          TLcdLabelIdentifier label_identifier = (TLcdLabelIdentifier) aObject;
          HighwayType highway_type = getHighwayType(label_identifier.getDomainObject());
          double minimum_scale = LABEL_SCALES[highway_type.ordinal()];
          return scale <= minimum_scale ||
                 aLabelFunction.applyOn(aObject);
        }
      };
      return super.applyOnInteractLabels(filter_function, aGraphics, aPaintMode, aGXYView);
    }

    @Override
    protected int applyOnInteract(final ILcdFunction aFunction, Graphics aGraphics, int aPaintMode, ILcdModel aModel, ILcdGXYView aGXYView) {
      final double scale = aGXYView.getScale();

      // Only apply the function on labels when the scale is larger than a certain scale.
      ILcdFunction filter_function = new ILcdFunction() {
        @Override
        public boolean applyOn(Object aObject) throws IllegalArgumentException {
          double minimum_scale = getMinimumScale(aObject);
          return scale <= minimum_scale || aFunction.applyOn(aObject);
        }

        private double getMinimumScale(Object aObject) {
          HighwayType highway_type = getHighwayType(aObject);
          int[] line_widths = HIGHWAY_LINE_WIDTHS[highway_type.ordinal()];
          int index = -1;
          for (int i = 0; i < line_widths.length; i++) {
            if (line_widths[i] > 0) {
              index = i;
              break;
            }
          }

          if (index == -1) {
            return Double.MAX_VALUE;
          }
          return DETAIL_LEVELS[index].getMin();
        }
      };
      return super.applyOnInteract(filter_function, aGraphics, aPaintMode, aModel, aGXYView);
    }
  }

  // Data specific methods

  private static final String HIGHWAYS_DATA_PATH = "Data/Shp/Usa/highways/roadtrl020.shp";

  /**
   * Creates a highway model.
   * @return a highway model.
   */
  public static ILcdModel createModel() {
    try {
      TLcdSHPModelDecoder model_decoder = new TLcdSHPModelDecoder();
      return model_decoder.decode(HIGHWAYS_DATA_PATH);
    } catch (IOException e) {
      sLogger.error("Unable to load data : " + HIGHWAYS_DATA_PATH);
      return null;
    }
  }

  /**
   * Return the type of the given highway object.
   * @param aObject a given highway object.
   * @return the type of the given highway object.
   */
  private static HighwayType getHighwayType(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    String name = data_object.getValue("NAME").toString();

    if (name.contains("Interstate Route")) {
      return HighwayType.INTERSTATE_ROUTE;
    } else if (name.contains("US Route")) {
      return HighwayType.US_ROUTE;
    } else if (name.contains("State Route")) {
      return HighwayType.STATE_ROUTE;
    }
    throw new IllegalArgumentException("Unknown highway type : " + aObject);
  }

  /**
   * Return the number of the given highway object.
   * @param aObject a highway object.
   * @return the number of the given highway object.
   */
  protected int getHighwayNumber(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    String name = data_object.getValue("NAME").toString();

    int index = name.indexOf(" Route ");
    int start_index = index + 7;

    while (!Character.isDigit(name.charAt(start_index)) && start_index < name.length() - 1) {
      start_index++;
    }
    int end_index = start_index;
    while (end_index < name.length() - 1 && Character.isDigit(name.charAt(end_index + 1))) {
      end_index++;
    }

    String number_string = name.substring(start_index, end_index + 1);
    return new Integer(number_string);
  }

  /**
   * Create a painter for the highways, using the given line style.
   * @param aLineStyle a given line style.
   * @return a painter for the highways.
   */
  protected ILcdGXYPainter createHighwayPainter(ILcdGXYPainterStyle aLineStyle) {
    TLcdGXYShapeListPainter shape_list_painter = new TLcdGXYShapeListPainter();
    TLcdGXYPointListPainter point_list_painter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
    shape_list_painter.setShapeGXYPainterProvider(point_list_painter);
    point_list_painter.setLineStyle(aLineStyle);
    return shape_list_painter;
  }
}
