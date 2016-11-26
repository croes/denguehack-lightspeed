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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYInPathLabelingAlgorithm;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.common.MapColors;
import samples.gxy.labels.common.ConfigurableGXYLabelPainterWrapper;
import samples.gxy.labels.common.ConfigurableGXYPainterWrapper;

/**
 * This layer factory creates a states layer.
 *
 * This layer factory can be customized to use other data as well. For this, a set of
 * methods can be overridden :
 * - protected int getStatePopulation( Object )
 * - protected ILcdGXYPainter createStatesPainter( ILcdGXYPainterStyle, ILcdGXYPainterStyle )
 * - protected ILcdGXYLabelingPathProvider createLabelingPathProvider( ILcdGXYPainter )
 */
class StatesLayerFactory implements ILcdGXYLayerFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(StatesLayerFactory.class.getName());

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(2.5e-5, 5e-5),
      new TLcdInterval(5e-5, 1e-4),
      new TLcdInterval(1e-4, 2e-4),
      new TLcdInterval(2e-4, 5e-4),
      new TLcdInterval(5e-4, 1e-3),
      new TLcdInterval(1e-3, Double.MAX_VALUE)
  };

  private static final Font FONT = new Font("Dialog", Font.BOLD, 12);
  private static final String[] STATE_LABELS = {null, "STATE_ABBR", "STATE_NAME", "STATE_NAME", "STATE_NAME", null};
  private static final Color[] STATE_LABEL_COLORS = {null, Color.gray, Color.darkGray, Color.black, Color.darkGray, null};
  private static final Color[] STATE_OUTLINE_COLORS = {new Color(224, 224, 224), Color.lightGray, Color.lightGray, Color.lightGray, Color.lightGray, new Color(224, 224, 224)};

  @Override
  public ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel) {
    if (aModel == null) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer("States");
    layer.setModel(aModel);

    layer.setLabeled(true);
    layer.setEditable(false);
    layer.setLabelsEditable(false);
    layer.setVisible(true);
    layer.setGXYPen(new TLcdGeodeticPen());

    configureStatesPainting(layer);
    configureStatesLabelPainting(layer);

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
        int population = getStatePopulation(aObject);

        double priority;
        if (population < 1000000) {
          priority = 1.0;
        } else if (population < 2000000) {
          priority = 0.75;
        } else if (population < 5000000) {
          priority = 0.5;
        } else if (population < 10000000) {
          priority = 0.25;
        } else {
          priority = 0.0;
        }

        return priority;
      }

      private double getGlobalPriority(ILcdGXYView aGXYView) {
        // Retrieve the global priority of city labels. This priority is based on the view scale,
        // and makes sure labels of different layers interact correctly.
        int detail_level = getLevelOfDetail(aGXYView);
        if (detail_level == -1) {
          return 1.0;
        }

        if (detail_level == 0) {
          return 0.5;
        }
        if (detail_level == 1) {
          return 0.1;
        }
        if (detail_level == 2) {
          return 0.25;
        }
        if (detail_level == 3) {
          return 0.5;
        }
        if (detail_level == 4) {
          return 0.75;
        }
        if (detail_level == 5) {
          return 0.9;
        }
        return 1.0;
      }
    };
  }

  public static ALcdGXYDiscretePlacementsLabelingAlgorithm createLabelingAlgorithm(int aLargestPriority, int aSmallestPriority) {
    TLcdGXYInPathLabelingAlgorithm algorithm = new TLcdGXYInPathLabelingAlgorithm();
    algorithm.setLabelPriorityProvider(createLabelPriorityProvider(aLargestPriority, aSmallestPriority));
    return algorithm;
  }

  private void configureStatesPainting(TLcdGXYLayer aGXYLayer) {
    aGXYLayer.setGXYPainterProvider(new StatesPainter());
  }

  private void configureStatesLabelPainting(TLcdGXYLayer aGXYLayer) {
    StatesLabelPainter label_painter = new StatesLabelPainter();
    aGXYLayer.setGXYLabelPainterProvider(label_painter);
    aGXYLayer.setLabelScaleRange(getLabelInterval());
  }

  private ILcdInterval getLabelInterval() {
    if (DETAIL_LEVELS.length == 0) {
      return new TLcdInterval(Double.MAX_VALUE, Double.MAX_VALUE);
    }
    double min = Double.NaN;
    double max = Double.NaN;
    for (int i = 0; i < DETAIL_LEVELS.length; i++) {
      String label = STATE_LABELS[i];
      if (label != null) {
        ILcdInterval interval = DETAIL_LEVELS[i];
        if (Double.isNaN(min) || Double.isNaN(max)) {
          min = interval.getMin();
          max = interval.getMax();
        } else {
          min = Math.min(min, interval.getMin());
          max = Math.max(max, interval.getMax());
        }
      }
    }
    return new TLcdInterval(min, max);
  }

  public class StatesPainter extends ConfigurableGXYPainterWrapper {

    private TLcdStrokeLineStyle fLineStyle;

    public StatesPainter() {
      ILcdGXYPainterStyle fill_style = new TLcdGXYPainterColorStyle(new Color(240, 240, 235));
      fLineStyle = TLcdStrokeLineStyle.newBuilder()
                                      .selectionColor(MapColors.SELECTION)
                                      .antiAliasing(true)
                                      .lineWidth(1.0f)
                                      .solidLineStyle().build();
      setDelegate(createStatesPainter(fLineStyle, fill_style));
    }

    @Override
    protected boolean configurePainter(ILcdGXYPainter aPainter, ILcdGXYContext aGXYContext, Object aObject) {
      int level_of_detail = getLevelOfDetail(aGXYContext.getGXYView());
      if (level_of_detail == -1) {
        return false;
      }

      Color line_color = STATE_OUTLINE_COLORS[level_of_detail];
      fLineStyle.setColor(line_color);

      return true;
    }
  }

  /**
   * This label painter adds the following functionality :
   * - anti-aliasing
   * - different font colors and label text based on the view scale
   */
  public class StatesLabelPainter extends ConfigurableGXYLabelPainterWrapper {

    private TLcdGXYDataObjectLabelPainter fDelegate;

    public StatesLabelPainter() {
      super(new TLcdGXYDataObjectLabelPainter());
      fDelegate = (TLcdGXYDataObjectLabelPainter) getDelegate();
      fDelegate.setFont(FONT);
      fDelegate.setHaloEnabled(true);
      fDelegate.setHaloColor(Color.white);
      fDelegate.setHaloThickness(1);
      fDelegate.setWithPin(false);
      fDelegate.setSelectionColor(MapColors.SELECTION.darker());
    }

    @Override
    protected boolean configureLabelPainter(ILcdGXYLabelPainter2 aLabelPainter, ILcdGXYContext aGXYContext, Object aObject) {
      int level_of_detail = getLevelOfDetail(aGXYContext.getGXYView());
      if (level_of_detail == -1) {
        return false;
      }

      Color font_color = STATE_LABEL_COLORS[level_of_detail];
      fDelegate.setForeground(font_color);

      String expressions = STATE_LABELS[level_of_detail];
      if (expressions == null) {
        return false;
      }
      fDelegate.setExpressions(expressions);

      return true;
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

  // Data specific methods

  private static final String STATES_DATA_PATH = "Data/Shp/Usa/states.shp";

  /**
   * Creates a model with states data.
   * @return a model with states data.
   */
  public static ILcdModel createModel() {
    try {
      TLcdSHPModelDecoder model_decoder = new TLcdSHPModelDecoder();
      return model_decoder.decode(STATES_DATA_PATH);
    } catch (IOException e) {
      sLogger.error("Unable to load data : " + STATES_DATA_PATH);
      return null;
    }
  }

  /**
   * Returns the population of the given state object.
   * @param aObject a state object.
   * @return the population of the given state object.
   */
  private static int getStatePopulation(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    Integer population = (Integer) data_object.getValue("POP1996");
    if (population == null) {
      return 0;
    }
    return population;
  }

  /**
   * Create a painter for the states, using the given style.
   * @param aLineStyle a given line style.
   * @param aFillStyle a given fill style.
   * @return a painter for the states.
   */
  protected ILcdGXYPainter createStatesPainter(ILcdGXYPainterStyle aLineStyle, ILcdGXYPainterStyle aFillStyle) {
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setSelectionMode(ALcdGXYAreaPainter.OUTLINED);
    painter.setEditMode(ALcdGXYAreaPainter.OUTLINED);
    painter.setLineStyle(aLineStyle);
    painter.setFillStyle(aFillStyle);
    return painter;
  }
}
