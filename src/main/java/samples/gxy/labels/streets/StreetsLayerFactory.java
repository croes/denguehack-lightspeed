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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYCurvedPathLabelingAlgorithm;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.common.MapColors;
import samples.gxy.labels.common.ConfigurableGXYPainterWrapper;

/**
 * This layer factory creates a streets layer. This layer contains the streets of
 * Washington D.C.
 *
 * This layer factory can be customized to use other data as well. For this, a set of
 * methods can be overridden :
 * - protected String getStreetName( Object )
 * - protected ILcdGXYPainter createStatesPainter( ILcdGXYPainterStyle )
 * - protected ILcdGXYLabelingPathProvider createLabelingPathProvider( ILcdGXYPainter )
 */
class StreetsLayerFactory implements ILcdGXYLayerFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(StreetsLayerFactory.class.getName());

  private static final Color STREET_HALO_COLOR = new Color(210, 210, 210);
  private static final Color STREET_COLOR = new Color(255, 255, 220);

  private static final Color STREET_LABEL_TEXT_COLOR = Color.darkGray;
  private static final Color STREET_LABEL_HALO_COLOR = new Color(255, 255, 220);

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(0.05, 0.1),
      new TLcdInterval(0.1, 0.2),
      new TLcdInterval(0.2, 0.5),
      new TLcdInterval(0.5, 1.0),
      new TLcdInterval(1.0, Double.MAX_VALUE)
  };

  private static final int[] STREET_LINE_WIDTHS = {1, 3, 4, 8, 12};
  private static final double STREETS_LABEL_SCALE = 0.3;

  @Override
  public ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel) {
    if (aModel == null) {
      return null;
    }

    TLcdGXYLayer layer = new StreetLayer("Streets");
    layer.setModel(aModel);

    layer.setLabeled(true);
    layer.setEditable(false);
    layer.setLabelsEditable(false);
    layer.setSelectable(true);
    layer.setVisible(true);
    layer.setGXYPen(new TLcdGeodeticPen());

    configureStreetPainting(layer);
    configureStreetLabelPainting(layer);

    return layer;
  }

  private static ILcdGXYMultiLabelPriorityProvider createLabelPriorityProvider(final int aLargestPriority, final int aSmallestPriority) {
    return new ILcdGXYMultiLabelPriorityProvider() {
      @Override
      public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
        double relative_priority = getRelativePriority();
        double global_priority = getGlobalPriority(aGXYContext.getGXYView());
        double priority = global_priority * 0.5 + relative_priority * 0.5;
        return aLargestPriority + (int) (priority * (double) (aSmallestPriority - aLargestPriority));
      }

      private double getRelativePriority() {
        // Retrieve the priority of a label relative to an other label of the same layer.
        return 1.0;
      }

      private double getGlobalPriority(ILcdGXYView aGXYView) {
        // Retrieve the global priority of street labels. This priority is based on the view scale,
        // and makes sure labels of different layers interact correctly.
        int detail_level = getLevelOfDetail(aGXYView);
        if (detail_level == -1) {
          return 1.0;
        }

        if (detail_level == 0) {
          return 1.0;
        }
        if (detail_level == 1) {
          return 0.9;
        }
        if (detail_level == 2) {
          return 0.8;
        }
        if (detail_level == 3) {
          return 0.7;
        }
        if (detail_level == 4) {
          return 0.6;
        }
        return 1.0;
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
    TLcdGXYCurvedPathLabelingAlgorithm algorithm = new TLcdGXYCurvedPathLabelingAlgorithm(createCurvedPathLabelPainter());
    algorithm.setReusePreviousLocations(true);
    algorithm.setLabelPriorityProvider(createLabelPriorityProvider(aLargestPriority, aSmallestPriority));
    return algorithm;
  }

  private void configureStreetLabelPainting(TLcdGXYLayer aGXYLayer) {
    // Create a label painter that drapes the labels over a path
    TLcdGXYCurvedPathLabelPainter curved_path_label_painter = createCurvedPathLabelPainter();
    aGXYLayer.setGXYLabelPainterProvider(curved_path_label_painter);
    aGXYLayer.setGXYLabelEditorProvider(null);
    // This label painter requires a special type of label locations
    aGXYLayer.setLabelLocations(new TLcdLabelLocations(aGXYLayer, new TLcdGXYCurvedPathLabelLocation()));
    aGXYLayer.setLabeled(true);
    aGXYLayer.setLabelScaleRange(new TLcdInterval(STREETS_LABEL_SCALE, Double.MAX_VALUE));
  }

  private static TLcdGXYCurvedPathLabelPainter createCurvedPathLabelPainter() {
    TLcdGXYCurvedPathLabelPainter curved_path_label_painter = new StreetCurvedPathLabelPainter();
    curved_path_label_painter.setFont(new Font("Dialog", Font.PLAIN, 12));
    curved_path_label_painter.setForeground(STREET_LABEL_TEXT_COLOR);
    curved_path_label_painter.setHaloColor(STREET_LABEL_HALO_COLOR);
    curved_path_label_painter.setHaloEnabled(true);
    curved_path_label_painter.setExtraCharacterSpacing(0.0);
    curved_path_label_painter.setSelectionColor(MapColors.SELECTION.darker());
    return curved_path_label_painter;
  }

  private void configureStreetPainting(TLcdGXYLayer aGXYLayer) {
    ILcdGXYPainterProvider[] painter_provider_array = new ILcdGXYPainterProvider[2];
    painter_provider_array[0] = new StreetPainter(true);
    painter_provider_array[1] = new StreetPainter(false);
    aGXYLayer.setGXYPainterProviderArray(painter_provider_array);
  }

  /**
   * This label painter adds the following functionality :
   * - anti-aliasing
   * - the retrieval of the correct label text
   */
  private static class StreetCurvedPathLabelPainter extends TLcdGXYCurvedPathLabelPainter {

    public StreetCurvedPathLabelPainter() {
      super();
    }

    @Override
    public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
      // Make sure the labels are anti-aliased
      Graphics2D g2d = (Graphics2D) aGraphics;
      RenderingHints rendering_hints = g2d.getRenderingHints();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      super.paintLabel(aGraphics, aMode, aGXYContext);

      // Restore the rendering hints
      g2d.setRenderingHints(rendering_hints);
    }

    @Override
    public String retrieveLabels(Object aObject, int aLabelIndex, int aSubLabelIndex) {
      return getStreetName(aObject);
    }
  }

  /**
   * This painter adds the following functionality :
   * - anti-aliasing
   * - line widths based on the level of detail
   */
  private class StreetPainter extends ConfigurableGXYPainterWrapper {

    private boolean fHaloPainter;
    private TLcdStrokeLineStyle fLineStyle;

    private Map<Integer, BasicStroke> fStrokes = new HashMap<Integer, BasicStroke>();

    public StreetPainter(boolean aHaloPainter) {
      fHaloPainter = aHaloPainter;
      fLineStyle = TLcdStrokeLineStyle.newBuilder()
                                      .antiAliasing(true)
                                      .color(aHaloPainter ? STREET_HALO_COLOR : STREET_COLOR)
                                      .selectionColor(MapColors.SELECTION).build();
      setDelegate(createStreetPainter(fLineStyle));
    }

    @Override
    protected boolean configurePainter(ILcdGXYPainter aPainter, ILcdGXYContext aGXYContext, Object aObject) {
      int level_of_detail = getLevelOfDetail(aGXYContext.getGXYView());
      if (level_of_detail == -1) {
        return false;
      }

      int line_width = STREET_LINE_WIDTHS[level_of_detail];
      if (!fHaloPainter) {
        line_width -= 2;
      }
      if (line_width <= 0) {
        return false;
      }

      fLineStyle.setStroke(getBasicStroke(line_width));
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

  private static class StreetLayer extends TLcdGXYLayer {

    public StreetLayer(String aLabel) {
      super(aLabel);
    }

    @Override
    protected int applyOnInteract(final ILcdFunction aFunction, Graphics aGraphics, int aPaintMode, ILcdModel aModel, ILcdGXYView aGXYView) {
      final double scale = aGXYView.getScale();

      // Start to show streets from a certain scale
      final int[] count = {0};
      ILcdFunction filter_function = new ILcdFunction() {
        @Override
        public boolean applyOn(Object aObject) throws IllegalArgumentException {
          double minimum_scale = getMinimumScale();
          if (scale > minimum_scale) {
            aFunction.applyOn(aObject);
            count[0]++;
          }
          return true;
        }

        private double getMinimumScale() {
          int index = -1;
          for (int i = 0; i < STREET_LINE_WIDTHS.length; i++) {
            if (STREET_LINE_WIDTHS[i] > 0) {
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
      super.applyOnInteract(filter_function, aGraphics, aPaintMode, aModel, aGXYView);
      return count[0];
    }
  }

  // Data specific methods

  private static final String STREETS_DATA_PATH = "Data/Shp/Dc/streets.shp";

  /**
   * Creates a model with street data.
   * @return a model with street data.
   */
  public static ILcdModel createModel() {
    try {
      TLcdSHPModelDecoder model_decoder = new TLcdSHPModelDecoder();
      return model_decoder.decode(STREETS_DATA_PATH);
    } catch (IOException e) {
      sLogger.error("Unable to load data : " + STREETS_DATA_PATH);
      return null;
    }
  }

  /**
   * Returns the name of the given street.
   * @param aObject a given street object.
   * @return the name of the given street.
   */
  private static String getStreetName(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    Object name = data_object.getValue("STREET");
    return name.toString();
  }

  /**
   * Create a painter for the highways, using the given line style.
   * @param aLineStyle a given line style.
   * @return a painter for the highways.
   */
  protected ILcdGXYPainter createStreetPainter(ILcdGXYPainterStyle aLineStyle) {
    TLcdGXYShapeListPainter shape_list_painter = new TLcdGXYShapeListPainter();
    TLcdGXYPointListPainter point_list_painter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
    shape_list_painter.setShapeGXYPainterProvider(point_list_painter);
    point_list_painter.setLineStyle(aLineStyle);
    return shape_list_painter;
  }
}
