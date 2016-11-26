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
package samples.lightspeed.demo.application.data.streets;

import static java.util.Arrays.asList;

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.CENTER;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.ILspLabelPriorityProvider;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle.ScalingMode;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.gxy.common.AntiAliasedIcon;

/**
 * A layer factory for cities
 */
public class CitiesLayerFactory extends ALspSingleLayerFactory {

  private static final String CITIES_DATA_PATH = "city_125.shp";
  private static final String CITIES_POPULATION_FIELD_NAME = "TOT_POP";

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(5e-5, 1e-4),
      new TLcdInterval(1e-4, 2e-4),
      new TLcdInterval(2e-4, 1e-3),
      new TLcdInterval(1e-3, 2e-3),
      new TLcdInterval(2e-3, 5e-3),
      new TLcdInterval(5e-3, 1e-2),
      new TLcdInterval(1e-2, 2e-2),
      new TLcdInterval(2e-2, 1e-1),
      new TLcdInterval(1e-1, 2e-1)
  };

  private static List<List<ALspStyle>> LARGE_STYLES;
  private static List<List<ALspStyle>> MEDIUM_STYLES;
  private static List<List<ALspStyle>> SMALL_STYLES;

  static {
    initializeStyles();
  }

  // Set-up a couple of different styles for different view scales and cities populations.
  @SuppressWarnings("unchecked")
  private static void initializeStyles() {
    TLspTextStyle.Builder textStyle = TLspTextStyle.newBuilder()
                                                   .font("Dialog-BOLD-16")
                                                   .textColor(Color.white)
                                                   .haloColor(Color.black)
                                                   .haloThickness(1);

    TLspLabelOpacityStyle.Builder<?> opacityColorStyle = TLspLabelOpacityStyle.newBuilder();

    LARGE_STYLES = asList(
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(0.4f).build()),
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(0.6f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(0.8f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(1.0f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(1.0f).build()),
        asList(textStyle.font("Dialog-BOLD-16").build(), opacityColorStyle.opacity(1.0f).build()),
        asList(textStyle.font("Dialog-BOLD-16").build(), opacityColorStyle.opacity(0.8f).build()),
        asList(textStyle.font("Dialog-BOLD-16").build(), opacityColorStyle.opacity(0.6f).build()),
        asList(textStyle.font("Dialog-BOLD-16").build(), opacityColorStyle.opacity(0.4f).build())
    );

    MEDIUM_STYLES = asList(
        null,
        null,
        null,
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(0.6f).build()),
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(0.8f).build()),
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(1.0f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(0.8f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(0.6f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(0.4f).build())
    );

    SMALL_STYLES = asList(
        null,
        null,
        null,
        null,
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(0.6f).build()),
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(0.8f).build()),
        asList(textStyle.font("Dialog-BOLD-10").build(), opacityColorStyle.opacity(1.0f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(0.75f).build()),
        asList(textStyle.font("Dialog-BOLD-14").build(), opacityColorStyle.opacity(0.5f).build())
    );
  }

  private CitiesLabelStyler fCityLabelStyler;
  private CitiesLabelStyler fSelectedCityLabelStyler;

  /**
   * Creates a new cities layer factory for the given view
   */
  public CitiesLayerFactory() {
    fCityLabelStyler = new CitiesLabelStyler();
    fSelectedCityLabelStyler = new CitiesLabelStyler();
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    // Configure body styles.
    TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 5, Color.darkGray, Color.lightGray);
    TLcdSymbol selectedIcon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 5, Color.red, Color.red);

    layerBuilder.model(aModel)
                .label("Cities")
                .selectable(true)
                .bodyEditable(false)
                .labelEditable(false)
                .minimumObjectSizeForPainting(0)
                .bodyStyler(TLspPaintState.REGULAR, TLspIconStyle.newBuilder()
                                                                 .icon(new AntiAliasedIcon(icon))
                                                                 .scalingMode(ScalingMode.WORLD_SCALING_CLAMPED)
                                                                 .worldSize(20000)
                                                                 .useOrientation(true)
                                                                 .build())
                .bodyStyler(TLspPaintState.SELECTED, TLspIconStyle.newBuilder()
                                                                  .icon(new AntiAliasedIcon(selectedIcon))
                                                                  .scalingMode(ScalingMode.WORLD_SCALING_CLAMPED)
                                                                  .worldSize(20000)
                                                                  .build())
                .labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder()
                                                                    .priority(new CitiesLabelPriorityProvider(0, 1000))
                                                                    .locations(CENTER)
                                                                    .styler(fCityLabelStyler)
                                                                    .build())
                .labelStyler(TLspPaintState.SELECTED, TLspLabelStyler.newBuilder()
                                                                     .priority(new CitiesLabelPriorityProvider(0, 1000))
                                                                     .locations(CENTER)
                                                                     .styler(fSelectedCityLabelStyler)
                                                                     .build());

    return layerBuilder.build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().endsWith(CITIES_DATA_PATH);
  }

  /**
   * Returns the population of the given city object.
   *
   * @param aObject a city object.
   *
   * @return the population of the given city object.
   */
  protected int getCityPopulation(Object aObject) {
    ILcdDataObject dataObject = (ILcdDataObject) aObject;
    Integer population = (Integer) dataObject.getValue(CITIES_POPULATION_FIELD_NAME);

    if (population == null) {
      return 0;
    }

    return population;
  }

  private int getCitySize(Object aObject) {
    final int BIG_CITY_POPULATION = 1000000;
    final int AVERAGE_CITY_POPULATION = 100000;

    int population = getCityPopulation(aObject);

    if (population > BIG_CITY_POPULATION) {
      return 0;
    }

    if (population > AVERAGE_CITY_POPULATION) {
      return 1;
    }

    return 2;
  }

  private class CitiesLabelStyler extends StaticDetailLevelStyler {

    private final List<List<List<ALspStyle>>> fStyles;

    @SuppressWarnings("unchecked")
    private CitiesLabelStyler() {
      // No need to pass a list of list styles here since we are manually overriding style.
      super(DETAIL_LEVELS);
      fStyles = asList(LARGE_STYLES, MEDIUM_STYLES, SMALL_STYLES);
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      int levelOfDetail = getLevelOfDetail(aContext.getViewXYZWorldTransformation());

      if (levelOfDetail >= 0 && fStyles != null) {
        for (Object object : aObjects) {
          final List<ALspStyle> styles = fStyles.get(getCitySize(object)).get(levelOfDetail);

          if (styles != null) {
            aStyleCollector.object(object).styles(styles).submit();
          }
        }
      } else {
        aStyleCollector.objects(aObjects).hide().submit();
      }
    }
  }

  private class CitiesLabelPriorityProvider implements ILspLabelPriorityProvider {
    private int fHighestPriority;
    private int fLowestPriority;

    public CitiesLabelPriorityProvider(int aHighestPriority, int aLowestPriority) {
      fHighestPriority = aHighestPriority;
      fLowestPriority = aLowestPriority;
    }

    @Override
    public int getPriority(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext) {
      double relativePriority = getRelativePriority(aLabel.getDomainObject());
      double globalPriority = getGlobalPriority(aContext);
      double priority = globalPriority * 0.5 + relativePriority * 0.5;

      return fHighestPriority + (int) (priority * (double) (fLowestPriority - fHighestPriority));
    }

    private double getRelativePriority(Object aObject) {
      // Retrieve the priority of a label relative to another label of the same layer.
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

    private double getGlobalPriority(TLspContext aContext) {
      // Retrieve the global priority of city labels. This priority is based on the view scale,
      // and makes sure labels of different layers interact correctly.
      int detailLevel = DetailLevelStyler.getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      if (detailLevel == -1) {
        return 1.0;
      }

      if (detailLevel == 0) {
        return 1.0;
      }
      if (detailLevel == 1) {
        return 0.75;
      }
      if (detailLevel == 2) {
        return 0.5;
      }
      if (detailLevel == 3) {
        return 0.25;
      }
      if (detailLevel == 4) {
        return 0.0;
      }
      if (detailLevel == 5) {
        return 0.0;
      }
      if (detailLevel == 6) {
        return 0.0;
      }
      if (detailLevel == 7) {
        return 0.0;
      }
      if (detailLevel == 8) {
        return 0.0;
      }

      return 1.0;
    }
  }
}
