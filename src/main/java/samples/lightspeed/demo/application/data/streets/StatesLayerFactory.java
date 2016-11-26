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

import static samples.lightspeed.demo.application.data.streets.DetailLevelStyler.getLevelOfDetail;

import java.awt.Color;
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
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * A layer factory for states.
 */
public class StatesLayerFactory extends ALspSingleLayerFactory {

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(2.5e-5, 5e-5),
      new TLcdInterval(5e-5, 1e-4),
      new TLcdInterval(1e-4, 2e-4),
      new TLcdInterval(2e-4, 5e-4),
      new TLcdInterval(5e-4, 1e-3),
      new TLcdInterval(1e-3, 2e-3)
  };

  private static List<List<ALspStyle>> DETAIL_STYLES;
  private static List<List<ALspStyle>> SELECTED_DETAIL_STYLES;

  static {
    initializeStyles();
  }

  // Set-up a couple of different styles for different view scales and state populations.
  @SuppressWarnings("unchecked")
  private static void initializeStyles() {
    TLspTextStyle textStyle = TLspTextStyle.newBuilder()
                                           .font("Dialog-BOLD-12")
                                           .textColor(new Color(0, 0, 0))
                                           .haloColor(new Color(210, 255, 255))
                                           .haloThickness(1)
                                           .build();
    TLspLabelOpacityStyle.Builder<?> opacityColorStyle = TLspLabelOpacityStyle.newBuilder();
    ALspLabelTextProviderStyle nameTextProviderStyle = TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("STATE_NAME").build();
    ALspLabelTextProviderStyle abbrTextProviderStyle = TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("STATE_ABBR").build();

    DETAIL_STYLES = asList(
        asList(abbrTextProviderStyle, textStyle, opacityColorStyle.opacity(0.4f).build()),
        asList(abbrTextProviderStyle, textStyle, opacityColorStyle.opacity(0.6f).build()),
        asList(nameTextProviderStyle, textStyle, opacityColorStyle.opacity(0.8f).build()),
        asList(nameTextProviderStyle, textStyle, opacityColorStyle.opacity(1.0f).build()),
        asList(nameTextProviderStyle, textStyle, opacityColorStyle.opacity(0.75f).build()),
        asList(nameTextProviderStyle, textStyle, opacityColorStyle.opacity(0.5f).build())
    );

    textStyle = textStyle.asBuilder().textColor(Color.red).build();

    SELECTED_DETAIL_STYLES = asList(
        asList(abbrTextProviderStyle, textStyle),
        asList(abbrTextProviderStyle, textStyle),
        asList(nameTextProviderStyle, textStyle),
        asList(nameTextProviderStyle, textStyle),
        asList(nameTextProviderStyle, textStyle),
        asList(nameTextProviderStyle, textStyle)
    );
  }

  private StaticDetailLevelStyler fLabelStyler;
  private StaticDetailLevelStyler fSelectedLabelStyler;

  /**
   * Creates a new states layer factory for the given view.
   */
  public StatesLayerFactory() {
    fLabelStyler = new StaticDetailLevelStyler(DETAIL_LEVELS, DETAIL_STYLES);
    fSelectedLabelStyler = new StaticDetailLevelStyler(DETAIL_LEVELS, SELECTED_DETAIL_STYLES);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    // Configure body stylers.
    TLspLineStyle.Builder lineStyleBuilder = TLspLineStyle.newBuilder()
                                                          .dashPattern(new TLspLineStyle.DashPattern(TLspLineStyle.DashPattern.SOLID, 1))
                                                          .width(1.0f)
                                                          .color(new Color(180, 240, 220))
                                                          .elevationMode(ElevationMode.ON_TERRAIN);

    ILspLabelPriorityProvider priorityProvider = new StatesLabelPriorityProvider(0, 1000);

    layerBuilder.model(aModel)
                .label("States")
                .selectable(true)
                .bodyEditable(false)
                .labelEditable(false)
                .bodyStyler(TLspPaintState.REGULAR, new TLspStyler(lineStyleBuilder.build()))
                .bodyStyler(TLspPaintState.SELECTED, new TLspStyler(lineStyleBuilder.color(Color.RED).build()))
                .labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder().priority(priorityProvider).styler(fLabelStyler).build())
                .labelStyler(TLspPaintState.SELECTED, TLspLabelStyler.newBuilder().priority(priorityProvider).styler(fSelectedLabelStyler).build());

    return layerBuilder.build();
  }

  /**
   * Returns the population of the given state object.
   *
   * @param aObject a state object.
   *
   * @return the population of the given state object.
   */
  protected int getStatePopulation(Object aObject) {
    ILcdDataObject dataObject = (ILcdDataObject) aObject;

    Integer population = (Integer) dataObject.getValue("POP1996");

    if (population == null) {
      return 0;
    }

    return population;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().endsWith("states.shp");
  }

  private class StatesLabelPriorityProvider implements ILspLabelPriorityProvider {
    private int fHighestPriority;
    private int fLowestPriority;

    public StatesLabelPriorityProvider(int aHighestPriority, int aLowestPriority) {
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
      // Retrieve the priority of a label relative to another label of the same layer.
      int population = getStatePopulation(aDomainObject);

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

    private double getGlobalPriority(TLspContext aContext) {
      // Retrieve the global priority of city labels. This priority is based on the view scale,
      // and makes sure labels of different layers interact correctly.
      int detailLevel = getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      if (detailLevel == -1) {
        return 1.0;
      }

      if (detailLevel == 0) {
        return 0.5;
      }
      if (detailLevel == 1) {
        return 0.1;
      }
      if (detailLevel == 2) {
        return 0.25;
      }
      if (detailLevel == 3) {
        return 0.5;
      }
      if (detailLevel == 4) {
        return 0.75;
      }
      if (detailLevel == 5) {
        return 0.9;
      }

      return 1.0;
    }
  }
}
