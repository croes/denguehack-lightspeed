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

/**
 * This layer factory creates a countries layer.
 *
 * This layer factory can be customized to use other data as well. For this, a set of
 * methods can be overridden :
 * - protected int getCountryPopulation( Object )
 * - protected ILcdGXYPainterProvider createCountriesPainterProvider( ILcdGXYPainterStyle, ILcdGXYPainterStyle )
 * - protected ILcdGXYLabelingPathProvider createLabelingPathProvider()
 */
class CountriesLayerFactory implements ILcdGXYLayerFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(CountriesLayerFactory.class.getName());

  private static final Font LARGE_FONT = new Font("Dialog", Font.BOLD, 16);
  private static final Font MEDIUM_FONT = new Font("Dialog", Font.BOLD, 12);
  private static final Font SMALL_FONT = new Font("Dialog", Font.PLAIN, 9);

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(5e-6, 2e-5),
      new TLcdInterval(2e-5, 5e-5),
      new TLcdInterval(5e-5, 1e-4),
      new TLcdInterval(1e-4, 2e-4),
      new TLcdInterval(2e-4, 5e-4)
  };

  private static final Font[][] LABEL_FONTS = {
      {LARGE_FONT, LARGE_FONT, LARGE_FONT, LARGE_FONT, LARGE_FONT},
      {MEDIUM_FONT, MEDIUM_FONT, LARGE_FONT, LARGE_FONT, LARGE_FONT},
      {SMALL_FONT, SMALL_FONT, MEDIUM_FONT, MEDIUM_FONT, MEDIUM_FONT}
  };

  private static final Color[][] LABEL_COLORS = {
      {Color.black, Color.black, Color.black, Color.black, Color.black},
      {Color.darkGray, Color.darkGray, Color.darkGray, Color.black, Color.black},
      {Color.gray, Color.darkGray, Color.darkGray, Color.black, Color.black}
  };

  @Override
  public ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel) {
    if (aModel == null) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer("Countries");
    layer.setModel(aModel);

    layer.setLabeled(true);
    layer.setEditable(false);
    layer.setLabelsEditable(false);
    layer.setVisible(true);
    layer.setGXYPen(new TLcdGeodeticPen());

    configureCountriesPainting(layer);
    configureCountriesLabelPainting(layer);

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
        int population = getCountryPopulation(aObject);

        double priority;
        if (population < 300000) {
          priority = 1.0;
        } else if (population < 1000000) {
          priority = 0.875;
        } else if (population < 3000000) {
          priority = 0.75;
        } else if (population < 10000000) {
          priority = 0.625;
        } else if (population < 30000000) {
          priority = 0.5;
        } else if (population < 100000000) {
          priority = 0.375;
        } else if (population < 300000000) {
          priority = 0.25;
        } else if (population < 1000000000) {
          priority = 0.125;
        } else {
          priority = 0.0;
        }
        return priority;
      }

      private double getGlobalPriority(ILcdGXYView aGXYView) {
        // Retrieve the global priority of country labels. This priority is based on the view scale,
        // and makes sure labels of different layers interact correctly.
        int detail_level = getLevelOfDetail(aGXYView);
        if (detail_level == -1) {
          return 1.0;
        }

        if (detail_level == 0) {
          return 0.2;
        }
        if (detail_level == 1) {
          return 0.1;
        }
        if (detail_level == 2) {
          return 0.0;
        }
        if (detail_level == 3) {
          return 0.1;
        }
        if (detail_level == 4) {
          return 0.2;
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

  private void configureCountriesPainting(TLcdGXYLayer aGXYLayer) {
    ILcdGXYPainterStyle fill_style = new TLcdGXYPainterColorStyle(new Color(245, 245, 240));
    ILcdGXYPainterStyle line_style = new TLcdGXYPainterColorStyle(new Color(224, 224, 224), MapColors.SELECTION);
    aGXYLayer.setGXYPainterProvider(createCountriesPainterProvider(line_style, fill_style));
  }

  private void configureCountriesLabelPainting(TLcdGXYLayer aGXYLayer) {
    CountriesLabelPainter label_painter = new CountriesLabelPainter();
    aGXYLayer.setGXYLabelPainterProvider(label_painter);
    aGXYLayer.setLabelScaleRange(getLabelInterval());
  }

  private ILcdInterval getLabelInterval() {
    if (DETAIL_LEVELS.length == 0) {
      return new TLcdInterval(Double.MAX_VALUE, Double.MAX_VALUE);
    }
    double min = DETAIL_LEVELS[0].getMin();
    double max = DETAIL_LEVELS[0].getMax();
    for (ILcdInterval interval : DETAIL_LEVELS) {
      min = Math.min(min, interval.getMin());
      max = Math.max(max, interval.getMax());
    }
    return new TLcdInterval(min, max);
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

  private int getCountrySize(Object aObject) {
    int population = getCountryPopulation(aObject);
    if (population > 100000000) {
      return 0;
    }
    if (population > 10000000) {
      return 1;
    }
    return 2;
  }

  /**
   * This label painter adds the following functionality :
   * - anti-aliasing
   * - different fonts and font colors based on the view scale
   */
  public class CountriesLabelPainter extends ConfigurableGXYLabelPainterWrapper {

    private TLcdGXYDataObjectLabelPainter fDelegate;

    public CountriesLabelPainter() {
      super(new TLcdGXYDataObjectLabelPainter());
      fDelegate = (TLcdGXYDataObjectLabelPainter) getDelegate();
      fDelegate.setExpressions("COUNTRY");
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

      int country_size = getCountrySize(aObject);

      Font font = LABEL_FONTS[country_size][level_of_detail];
      if (font != fDelegate.getFont()) {
        fDelegate.setFont(font);
      }

      Color font_color = LABEL_COLORS[country_size][level_of_detail];
      fDelegate.setForeground(font_color);

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

  // Data specific methods

  private static final String COUNTRIES_DATA_PATH = "Data/Shp/World/world.shp";

  public static ILcdModel createModel() {
    try {
      TLcdSHPModelDecoder model_decoder = new TLcdSHPModelDecoder();
      return model_decoder.decode(COUNTRIES_DATA_PATH);
    } catch (IOException e) {
      sLogger.error("Unable to load data : " + COUNTRIES_DATA_PATH);
      return null;
    }
  }

  /**
   * Returns the population of the given state object.
   * @param aObject a state object.
   * @return the population of the given state object.
   */
  private static int getCountryPopulation(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    Integer population = (Integer) data_object.getValue("POP_1994");
    if (population == null) {
      return 0;
    }
    return population;
  }

  /**
   * Create a painter for the countries, using the given style.
   * @param aLineStyle a given line style.
   * @param aFillStyle a given fill style.
   * @return a painter for the states.
   */
  protected ILcdGXYPainterProvider createCountriesPainterProvider(ILcdGXYPainterStyle aLineStyle, ILcdGXYPainterStyle aFillStyle) {
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setMode(ALcdGXYAreaPainter.OUTLINED_FILLED);
    painter.setSelectionMode(ALcdGXYAreaPainter.OUTLINED);
    painter.setEditMode(ALcdGXYAreaPainter.OUTLINED);
    painter.setLineStyle(aLineStyle);
    painter.setFillStyle(aFillStyle);
    return painter;
  }
}
