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

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
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
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

/**
 * A layer factory for countries.
 */
public class CountriesLayerFactory extends ALspSingleLayerFactory {

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(5e-6, 2e-5),
      new TLcdInterval(2e-5, 5e-5),
      new TLcdInterval(5e-5, 1e-4),
      new TLcdInterval(1e-4, 2e-4),
      new TLcdInterval(2e-4, 4e-4),
      new TLcdInterval(4e-4, 8e-4),
      new TLcdInterval(8e-4, 16e-4)
  };

  private static final List<List<ALspStyle>> LARGE_STYLES;
  private static final List<List<ALspStyle>> MEDIUM_STYLES;
  private static final List<List<ALspStyle>> SMALL_STYLES;

  private static final List<List<ALspStyle>> SELECTED_LARGE_STYLES;
  private static final List<List<ALspStyle>> SELECTED_MEDIUM_STYLES;
  private static final List<List<ALspStyle>> SELECTED_SMALL_STYLES;

  static {
    TLspTextStyle textStyle = TLspTextStyle.newBuilder()
                                           .font("Dialog-BOLD-16")
                                           .textColor(Color.black)
                                           .haloColor(Color.white)
                                           .haloThickness(1)
                                           .build();

    LARGE_STYLES = getStyles(textStyle, 0.6f, 0.8f, 1.0f, 1.0f, 0.8f, 0.6f, 0.4f);
    textStyle = textStyle.asBuilder().textColor(Color.red).build();
    SELECTED_LARGE_STYLES = getStyles(textStyle, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    textStyle = textStyle.asBuilder().font("Dialog-BOLD-14").textColor(Color.darkGray).build();

    MEDIUM_STYLES = getStyles(textStyle, 0.4f, 0.6f, 0.8f, 1.0f, 0.8f, 0.6f, 0.4f);
    textStyle = textStyle.asBuilder().textColor(Color.red).build();
    SELECTED_MEDIUM_STYLES = getStyles(textStyle.asBuilder().textColor(Color.red).build(), 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    textStyle = textStyle.asBuilder().font("Dialog-BOLD-10").textColor(Color.darkGray).build();

    SMALL_STYLES = getStyles(textStyle, 0.25f, 0.5f, 0.75f, 1.0f, 0.8f, 0.6f, 0.4f);
    textStyle = textStyle.asBuilder().textColor(Color.red).build();
    SELECTED_SMALL_STYLES = getStyles(textStyle.asBuilder().textColor(Color.red).build(), 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
  }

  // Set-up a couple of different styles for different view scales and country populations
  @SuppressWarnings("unchecked")
  private static List<List<ALspStyle>> getStyles(TLspTextStyle aTextStyle, float... aTransparencies) {
    TLspLabelOpacityStyle.Builder<?> opacityColorStyle = TLspLabelOpacityStyle.newBuilder();

    return asList(
        asList(aTextStyle, opacityColorStyle.opacity(aTransparencies[0]).build()),
        asList(aTextStyle, opacityColorStyle.opacity(aTransparencies[1]).build()),
        asList(aTextStyle, opacityColorStyle.opacity(aTransparencies[2]).build()),
        asList(aTextStyle, opacityColorStyle.opacity(aTransparencies[3]).build()),
        asList(aTextStyle, opacityColorStyle.opacity(aTransparencies[4]).build()),
        asList(aTextStyle, opacityColorStyle.opacity(aTransparencies[5]).build()),
        asList(aTextStyle, opacityColorStyle.opacity(aTransparencies[6]).build())
    );
  }

  /**
   * Creates a new countries layer factory for the given view
   */
  public CountriesLayerFactory() {
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    ILspLabelPriorityProvider priorityProvider = new CountriesPriorityProvider(0, 1000);

    layerBuilder.model(aModel)
                .label("Countries")
                .selectable(true)
                .bodyEditable(false)
                .labelEditable(false)
                .bodyStyler(TLspPaintState.REGULAR, TLspLineStyle.newBuilder()
                                                                 .color(new Color(180, 180, 250))
                                                                 .width(1.0f)
                                                                 .elevationMode(ElevationMode.ON_TERRAIN)
                                                                 .build())
                .bodyStyler(TLspPaintState.SELECTED, TLspLineStyle.newBuilder()
                                                                  .color(new Color(145, 0, 20))
                                                                  .width(1.5f)
                                                                  .elevationMode(ElevationMode.ON_TERRAIN)
                                                                  .build())
                .labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder()
                                                                    .priority(priorityProvider)
                                                                    .styler(new CountryLabelStyler(asList(LARGE_STYLES, MEDIUM_STYLES, SMALL_STYLES)))
                                                                    .build())
                .labelStyler(TLspPaintState.SELECTED, TLspLabelStyler.newBuilder()
                                                                     .priority(priorityProvider)
                                                                     .styler(new CountryLabelStyler(asList(SELECTED_LARGE_STYLES, SELECTED_MEDIUM_STYLES, SELECTED_SMALL_STYLES)))
                                                                     .build());

    return layerBuilder.build();
  }

  private int getCountrySize(Object aObject) {
    final int BIG_COUNTRY_POPULATION = 1000000;
    final int AVERAGE_COUNTRY_POPULATION = 10000;

    int population = getCountryPopulation(aObject);

    if (population > BIG_COUNTRY_POPULATION) {
      return 0;
    }

    if (population > AVERAGE_COUNTRY_POPULATION) {
      return 1;
    }

    return 2;
  }

  /**
   * Returns the population of the given country object.
   *
   * @param aObject a country object.
   *
   * @return the population of the given country object.
   */
  protected int getCountryPopulation(Object aObject) {
    ILcdDataObject dataObject = (ILcdDataObject) aObject;

    Integer population = (Integer) dataObject.getValue("POP_1994");

    if (population == null) {
      return 0;
    }

    return population;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().endsWith("world.shp");
  }

  private class CountryLabelStyler extends StaticDetailLevelStyler {

    private final List<List<List<ALspStyle>>> fStyles;

    private CountryLabelStyler(List<List<List<ALspStyle>>> aStyles) {
      // No need to pass a list of list styles here since we are manually overriding style
      super(DETAIL_LEVELS);
      fStyles = aStyles;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      int levelOfDetail = getLevelOfDetail(aContext.getViewXYZWorldTransformation());

      if (levelOfDetail >= 0 && fStyles != null) {
        for (Object object : aObjects) {
          final List<ALspStyle> styles = fStyles.get(getCountrySize(object)).get(levelOfDetail);

          if (styles != null) {
            aStyleCollector.object(object).styles(styles).submit();
          }
        }
      } else {
        aStyleCollector.objects(aObjects).hide().submit();
      }
    }
  }

  private class CountriesPriorityProvider implements ILspLabelPriorityProvider {
    private int fHighestPriority;
    private int fLowestPriority;

    public CountriesPriorityProvider(int aHighestPriority, int aLowestPriority) {
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

    private double getRelativePriority(Object aDomainObject) {
      // Retrieve the priority of a label relative to an other label of the same layer.
      int population = getCountryPopulation(aDomainObject);

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

    private double getGlobalPriority(TLspContext aContext) {
      // Retrieve the global priority of country labels. This priority is based on the view scale,
      // and makes sure labels of different layers interact correctly.
      int detailLevel = DetailLevelStyler.getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      if (detailLevel == -1) {
        return 1.0;
      }

      if (detailLevel == 0) {
        return 0.2;
      }
      if (detailLevel == 1) {
        return 0.1;
      }
      if (detailLevel == 2) {
        return 0.0;
      }
      if (detailLevel == 3) {
        return 0.1;
      }
      if (detailLevel == 4) {
        return 0.2;
      }

      return 1.0;
    }
  }
}
