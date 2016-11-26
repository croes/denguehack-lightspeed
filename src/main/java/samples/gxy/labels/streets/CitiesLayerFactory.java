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
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdMultilevel2DBoundsIndexedModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLabelPainterLocationLabelingAlgorithm;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.common.MapColors;
import samples.gxy.labels.common.ConfigurableGXYLabelPainterWrapper;
import samples.gxy.labels.common.ConfigurableGXYPainterWrapper;

/**
 * Cities layer factory.
 *
 * This layer factory can be customized to use other data as well. For this, a set of
 * methods can be overridden :
 * - protected int getCityPopulation( Object )
 * - protected String getCityName( Object )
 * - protected ILcdGXYPainter createCitiesPainter( ILcdObjectIconProvider )
 */
class CitiesLayerFactory implements ILcdGXYLayerFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(CitiesLayerFactory.class.getName());

  private static final Font LARGE_FONT = new Font("Dialog", Font.BOLD, 16);
  private static final Font MEDIUM_FONT = new Font("Dialog", Font.BOLD, 13);
  private static final Font SMALL_FONT = new Font("Dialog", Font.BOLD, 9);

  private static final ILcdIcon LARGE_ICON = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 9, Color.darkGray, Color.gray);
  private static final ILcdIcon MEDIUM_ICON = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, Color.darkGray, new Color(160, 160, 160));
  private static final ILcdIcon SMALL_ICON = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 5, Color.darkGray, Color.lightGray);
  private static final ILcdIcon DUMMY_ICON = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 0);

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(5e-5, 1e-4),
      new TLcdInterval(1e-4, 2e-4),
      new TLcdInterval(2e-4, 1e-3),
      new TLcdInterval(1e-3, 2e-3),
      new TLcdInterval(2e-3, 5e-3),
      new TLcdInterval(5e-3, 1e-2),
      new TLcdInterval(1e-2, 2e-2),
      new TLcdInterval(2e-2, 1e-1),
      new TLcdInterval(1e-1, Double.MAX_VALUE)
  };

  private static final ILcdIcon[][] ICONS = {
      {SMALL_ICON, SMALL_ICON, MEDIUM_ICON, MEDIUM_ICON, LARGE_ICON, LARGE_ICON, DUMMY_ICON, DUMMY_ICON, DUMMY_ICON},
      {null, null, null, SMALL_ICON, MEDIUM_ICON, MEDIUM_ICON, DUMMY_ICON, DUMMY_ICON, DUMMY_ICON},
      {null, null, null, null, SMALL_ICON, SMALL_ICON, DUMMY_ICON, DUMMY_ICON, DUMMY_ICON}
  };

  private static final boolean[][] LABELED = {
      {true, true, true, true, true, true, true, true, false},
      {false, false, false, true, true, true, true, true, false},
      {false, false, false, false, true, true, true, true, false}
  };

  private static final Font[][] LABEL_FONTS = {
      {SMALL_FONT, SMALL_FONT, MEDIUM_FONT, LARGE_FONT, LARGE_FONT, LARGE_FONT, LARGE_FONT, LARGE_FONT, null},
      {null, null, SMALL_FONT, SMALL_FONT, MEDIUM_FONT, MEDIUM_FONT, MEDIUM_FONT, MEDIUM_FONT, null},
      {null, null, null, null, SMALL_FONT, SMALL_FONT, SMALL_FONT, MEDIUM_FONT, null}
  };

  private static final Color[][] LABEL_COLORS = {
      {Color.gray, Color.gray, Color.gray, Color.darkGray, Color.darkGray, Color.black, Color.black, Color.black, null},
      {null, null, Color.gray, Color.gray, Color.gray, Color.darkGray, Color.darkGray, Color.darkGray, null},
      {null, null, null, null, Color.gray, Color.gray, Color.gray, Color.gray, null}
  };

  @Override
  public ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel) {
    if (aModel == null) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer("Cities");
    layer.setModel(aModel);

    layer.setLabeled(true);
    layer.setEditable(false);
    layer.setLabelsEditable(false);
    layer.setVisible(true);
    layer.setGXYPen(new TLcdGeodeticPen());

    configureCitiesPainting(layer);
    configureCitiesLabelPainting(layer);

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
        int population = getCityPopulation(aObject);

        double priority;
        if (population > 3000000) {
          priority = 0.0;
        } else if (population > 1000000) {
          priority = 0.25;
        } else if (population > 300000) {
          priority = 0.5;
        } else if (population > 100000) {
          priority = 0.75;
        } else {
          priority = 1.0;
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
          return 1.0;
        }
        if (detail_level == 1) {
          return 0.75;
        }
        if (detail_level == 2) {
          return 0.5;
        }
        if (detail_level == 3) {
          return 0.25;
        }
        if (detail_level == 4) {
          return 0.0;
        }
        if (detail_level == 5) {
          return 0.0;
        }
        if (detail_level == 6) {
          return 0.0;
        }
        if (detail_level == 7) {
          return 0.0;
        }
        if (detail_level == 8) {
          return 0.0;
        }
        return 1.0;
      }
    };
  }

  public static ALcdGXYDiscretePlacementsLabelingAlgorithm createLabelingAlgorithm(int aLargestPriority, int aSmallestPriority) {
    TLcdGXYLabelPainterLocationLabelingAlgorithm algorithm = new TLcdGXYLabelPainterLocationLabelingAlgorithm();
    algorithm.setLabelPriorityProvider(createLabelPriorityProvider(aLargestPriority, aSmallestPriority));
    return algorithm;
  }

  private void configureCitiesPainting(TLcdGXYLayer aGXYLayer) {
    aGXYLayer.setGXYPainterProvider(new CitiesPainter());
  }

  private void configureCitiesLabelPainting(TLcdGXYLayer aGXYLayer) {
    aGXYLayer.setGXYLabelPainterProvider(new CitiesLabelPainter());
  }

  private int getCitySize(Object aObject) {
    int population = getCityPopulation(aObject);
    if (population > 1000000) {
      return 0;
    }
    if (population > 100000) {
      return 1;
    }
    return 2;
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

  /**
   * This painter adds the following functionality :
   * - anti-aliasing
   * - icons based on the city size and the level of detail
   */
  public class CitiesPainter extends ConfigurableGXYPainterWrapper {

    private MyIconProvider fIconProvider;

    protected CitiesPainter() {
      fIconProvider = new MyIconProvider();
      ILcdGXYPainter painter = createCitiesPainter(fIconProvider);
      setDelegate(painter);
    }

    @Override
    protected boolean configurePainter(ILcdGXYPainter aPainter, ILcdGXYContext aGXYContext, Object aObject) {
      fIconProvider.setGXYView(aGXYContext.getGXYView());
      return fIconProvider.canGetIcon(aObject);
    }
  }

  private class MyIconProvider implements ILcdObjectIconProvider {

    private ILcdGXYView fGXYView;

    public void setGXYView(ILcdGXYView aGXYView) {
      fGXYView = aGXYView;
    }

    @Override
    public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
      int level_of_detail = getLevelOfDetail(fGXYView);
      if (level_of_detail == -1) {
        return null;
      }

      int city_size = getCitySize(aObject);
      return ICONS[city_size][level_of_detail];
    }

    @Override
    public boolean canGetIcon(Object aObject) {
      return getIcon(aObject) != null;
    }
  }

  /**
   * This label painter adds the following functionality :
   * - anti-aliasing
   * - different fonts and font colors based on the city size and the view scale
   */
  public class CitiesLabelPainter extends ConfigurableGXYLabelPainterWrapper {

    private TLcdGXYLabelPainter fDelegate;

    public CitiesLabelPainter() {
      super(new TLcdGXYLabelPainter() {
        @Override
        protected String[] retrieveLabels(int aMode, ILcdGXYContext aGXYContext) {
          return new String[]{CitiesLayerFactory.this.getCityName(getObject())};
        }
      });
      fDelegate = (TLcdGXYLabelPainter) getDelegate();
      fDelegate.setHaloEnabled(true);
      fDelegate.setHaloColor(Color.white);
      fDelegate.setHaloThickness(1);
      fDelegate.setWithPin(false);
      fDelegate.setShiftLabelPosition(2);
      fDelegate.setSelectionColor(MapColors.SELECTION.darker());
      fDelegate.setPositionList(new int[]{
          TLcdGXYLabelPainter.EAST,
          TLcdGXYLabelPainter.WEST,
          TLcdGXYLabelPainter.NORTH,
          TLcdGXYLabelPainter.SOUTH,
      });
    }

    @Override
    protected boolean configureLabelPainter(ILcdGXYLabelPainter2 aLabelPainter, ILcdGXYContext aGXYContext, Object aObject) {
      int level_of_detail = getLevelOfDetail(aGXYContext.getGXYView());
      if (level_of_detail == -1) {
        return false;
      }

      int city_size = getCitySize(aObject);

      boolean labeled = LABELED[city_size][level_of_detail];
      if (!labeled) {
        return false;
      }

      Font font = LABEL_FONTS[city_size][level_of_detail];
      if (font != fDelegate.getFont()) {
        fDelegate.setFont(font);
      }

      Color font_color = LABEL_COLORS[city_size][level_of_detail];
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

  private static final String CITIES_DATA_PATH = "Data/Shp/Usa/city_125.shp";

  /**
   * Creates a model with cities data.
   * @return a model with cities data.
   */
  public static ILcdModel createModel() {
    try {
      TLcdSHPModelDecoder model_decoder = new TLcdSHPModelDecoder();
      ILcdModel model = model_decoder.decode(CITIES_DATA_PATH);
      if (model instanceof TLcdMultilevel2DBoundsIndexedModel) {
        // Adjust the ranges, in order to make small cities appear faster
        TLcdMultilevel2DBoundsIndexedModel multi_level_model = (TLcdMultilevel2DBoundsIndexedModel) model;
        double[] ranges = new double[multi_level_model.getLevelCount() + 1];
        for (int i = 0; i < multi_level_model.getLevelCount(); i++) {
          ranges[i] = 1000.0 / Math.pow(2, i);
        }
        ranges[multi_level_model.getLevelCount()] = 0.0;
        multi_level_model.setRange(ranges);
      }
      return model;
    } catch (IOException e) {
      sLogger.error("Unable to load data : " + CITIES_DATA_PATH);
      return null;
    }
  }

  /**
   * Returns the population of the given city object.
   * @param aObject a city object.
   * @return the population of the given city object.
   */
  private static int getCityPopulation(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    Integer population = (Integer) data_object.getValue("TOT_POP");
    if (population == null) {
      return 0;
    }
    return population;
  }

  /**
   * Returns the name of the given city object.
   * @param aObject a city object.
   * @return the name of the given city object.
   */
  protected String getCityName(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    String city_name = data_object.getValue("CITY").toString();
    if (city_name == null) {
      return "";
    }
    return city_name;
  }

  /**
   * Create a painter for the cities, using the given icon provider.
   * @param aIconProvider a given icon provider.
   * @return a painter for the cities.
   */
  protected ILcdGXYPainter createCitiesPainter(ILcdObjectIconProvider aIconProvider) {
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setAntiAliased(true);
    painter.setIconProvider(aIconProvider);
    painter.setSelectedIcon(MapColors.createIcon(true));
    return painter;
  }

}

