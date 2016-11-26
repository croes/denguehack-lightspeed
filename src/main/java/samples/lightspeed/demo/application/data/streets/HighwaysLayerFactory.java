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
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconImageUtil;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.ILspLabelPriorityProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspOnPathLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspWorldSizedLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

/**
 * A layer factory for highways
 */
public class HighwaysLayerFactory extends ALspSingleLayerFactory {

  private static enum HighwayType {
    INTERSTATE_ROUTE, US_ROUTE, STATE_ROUTE;

    private HighwayTypeFilter fHighwayTypeFilter = new HighwayTypeFilter(this);

    public HighwayTypeFilter getFilter() {
      return fHighwayTypeFilter;
    }
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

  private static final int[] HIGHWAY_LAYERS = {
      0, 2, 4
  };

  private static final Color[] HIGHWAY_HALO_COLORS = {
      new Color(210, 140, 60).darker(),
      new Color(210, 200, 80).darker(),
      new Color(210, 210, 175).darker()
  };

  private static final float[][] HIGHWAY_LINE_WIDTHS = {
      {0, 0, 1, 1.3f, 1.9f, 1.9f, 3.2f, 5},
      {0, 0, 0, 1, 1.3f, 1.9f, 3.2f, 5},
      {0, 0, 0, 0, 1, 1.3f, 2.5f, 3.7f}
  };

  /**
   * Creates a new highways layer factory for the given view
   */
  public HighwaysLayerFactory() {
  }

  /**
   * Return the type of the given highway object.
   *
   * @param aObject a given highway object.
   *
   * @return the type of the given highway object.
   */
  private static HighwayType getHighwayType(Object aObject) {
    ILcdDataObject dataObject = (ILcdDataObject) aObject;

    String name = dataObject.getValue("NAME").toString();

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
   *
   * @param aObject a highway object.
   *
   * @return the number of the given highway object.
   */
  private static int getHighwayNumber(Object aObject) {
    ILcdDataObject data_object = (ILcdDataObject) aObject;
    String name = data_object.getValue("NAME").toString();

    final String route = " Route ";
    int startIndexOfHighwayNumber = name.indexOf(route) + route.length();

    // Find the index of the first digit after the text "Route".
    while (!Character.isDigit(name.charAt(startIndexOfHighwayNumber)) && startIndexOfHighwayNumber < name.length() - 1) {
      startIndexOfHighwayNumber++;
    }

    // Find the last index with a digit after the text "Route" or the end of the string.
    int endIndexOfHighwayNumber = startIndexOfHighwayNumber;
    while (endIndexOfHighwayNumber < name.length() - 1 && Character.isDigit(name.charAt(endIndexOfHighwayNumber + 1))) {
      endIndexOfHighwayNumber++;
    }

    String highWayNumber = name.substring(startIndexOfHighwayNumber, endIndexOfHighwayNumber + 1);
    return Integer.valueOf(highWayNumber);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    // Create configuration for labeling.
    TLspOnPathLabelingAlgorithm algorithm = new TLspOnPathLabelingAlgorithm();
    algorithm.setAllowRotation(false);
    ILspLabelPriorityProvider priorityProvider = new HighwaysLabelPriorityProvider(0, 1000);
    layerBuilder.model(aModel)
                .label("Highways")
                .selectable(true)
                .bodyEditable(false)
                .bodyStyler(TLspPaintState.REGULAR, new HighwayStyler(false))
                .bodyStyler(TLspPaintState.SELECTED, new HighwayStyler(true))
                .labelEditable(false)
                .labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder()
                                                                    .priority(priorityProvider)
                                                                    .algorithm(algorithm)
                                                                    .styler(new HighwayIconStyler(false))
                                                                    .build())
                .labelStyler(TLspPaintState.SELECTED, TLspLabelStyler.newBuilder()
                                                                     .priority(priorityProvider)
                                                                     .algorithm(algorithm)
                                                                     .styler(new HighwayIconStyler(true))
                                                                     .build());
    return layerBuilder.build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getSourceName().endsWith("roadtrl020.shp") ||
           aModel.getModelDescriptor().getSourceName().endsWith("highways.shp");
  }

  private static class HighwayTypeFilter extends ALspStyleTargetProvider {
    private HighwayType fHighwayType;

    private HighwayTypeFilter(HighwayType aHighwayType) {
      super();
      fHighwayType = aHighwayType;
    }

    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      if (fHighwayType == getHighwayType(aObject)) {
        aResultSFCT.add(aObject);
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fHighwayType == null) ? 0 : fHighwayType.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }

      HighwayTypeFilter other = (HighwayTypeFilter) obj;

      return fHighwayType == other.fHighwayType;
    }
  }

  /**
   * Class that provides styles for highways
   */
  private class HighwayStyler extends DetailLevelStyler {

    private boolean fIsSelectionStyler;
    private Map<ALspStyleTargetProvider, List<ALspStyle>>[] fStyles;

    @SuppressWarnings("unchecked")
    public HighwayStyler(boolean aIsSelectionStyler) {
      fIsSelectionStyler = aIsSelectionStyler;

      fStyles = new Map[HIGHWAY_LINE_WIDTHS[0].length];

      // Process all level of details.
      for (int levelOfDetail = 0; levelOfDetail < fStyles.length; levelOfDetail++) {
        fStyles[levelOfDetail] = new HashMap<ALspStyleTargetProvider, List<ALspStyle>>(HighwayType.values().length);

        // Process all high way types.
        for (int highway_type = 0; highway_type < HighwayType.values().length; highway_type++) {
          List<ALspStyle> result = new ArrayList<ALspStyle>();
          fStyles[levelOfDetail].put(HighwayType.values()[highway_type].getFilter(), result);
          float line_width = HIGHWAY_LINE_WIDTHS[highway_type][levelOfDetail];

          // If the object has a width that is, if it is visible.
          if (line_width > 0) {
            if (levelOfDetail >= 7) {
              line_width = 30;
              TLspWorldSizedLineStyle.Builder<?> lineStyleBuilder = TLspWorldSizedLineStyle.newBuilder();
              lineStyleBuilder.color(fIsSelectionStyler ? Color.RED : HIGHWAY_HALO_COLORS[highway_type])
                              .width(line_width * 2.5f)
                              .elevationMode(ElevationMode.ON_TERRAIN)
                              .zOrder(HIGHWAY_LAYERS[highway_type] + 1);
              result.add(lineStyleBuilder.build());
              result.add(lineStyleBuilder.color(HIGHWAY_COLORS[highway_type])
                                         .width(line_width)
                                         .elevationMode(ElevationMode.ON_TERRAIN).build());
            } else {
              TLspLineStyle.Builder<?> lineStyleBuilder = TLspLineStyle.newBuilder();
              lineStyleBuilder.color(fIsSelectionStyler ? Color.RED : HIGHWAY_HALO_COLORS[highway_type])
                              .width(line_width * 3f)
                              .elevationMode(ElevationMode.ON_TERRAIN)
                              .zOrder(HIGHWAY_LAYERS[highway_type]);
              result.add(lineStyleBuilder.build());
              result.add(lineStyleBuilder.color(HIGHWAY_COLORS[highway_type])
                                         .width(line_width)
                                         .elevationMode(ElevationMode.ON_TERRAIN)
                                         .zOrder(HIGHWAY_LAYERS[highway_type] + 1)
                                         .build());
            }
          }
        }
      }
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      int levelOfDetail = getLevelOfDetail(aContext.getViewXYZWorldTransformation());

      // If the highways are visible and have styles configured for them, then submit those styles.
      if (levelOfDetail >= 0 && fStyles != null) {
        final Map<ALspStyleTargetProvider, List<ALspStyle>> styles = fStyles[levelOfDetail];

        for (Entry<ALspStyleTargetProvider, List<ALspStyle>> styleEntry : styles.entrySet()) {
          final List<ALspStyle> objectStyles = styleEntry.getValue();

          if (objectStyles != null && !objectStyles.isEmpty()) {
            aStyleCollector.objects(aObjects).geometry(styleEntry.getKey()).styles(objectStyles).submit();
          }
        }
      } else {
        aStyleCollector.objects(aObjects).hide().submit();
      }
    }

    @Override
    protected ILcdInterval[] getDetailLevels() {
      return DETAIL_LEVELS;
    }
  }

  private class HighwaysLabelPriorityProvider implements ILspLabelPriorityProvider {
    private int fHighestPriority;
    private int fLowestPriority;

    public HighwaysLabelPriorityProvider(int aHighestPriority, int aLowestPriority) {
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
      // Retrieve the priority of a label relative to an other label of the same layer.
      HighwayType highwayType = getHighwayType(aObject);

      double priority = 1.0;

      if (highwayType == HighwayType.INTERSTATE_ROUTE) {
        priority = 0.5;
      } else if (highwayType == HighwayType.US_ROUTE) {
        priority = 0.75;
      } else if (highwayType == HighwayType.STATE_ROUTE) {
        priority = 1.0;
      }

      return priority;
    }

    private double getGlobalPriority(TLspContext aContext) {
      // Retrieve the global priority of highway labels. This priority is based on the view scale,
      // and makes sure labels of different layers interact correctly.
      int levelOfDetail = getLevelOfDetail(aContext.getViewXYZWorldTransformation(), DETAIL_LEVELS);

      if (levelOfDetail == -1) {
        return 1.0;
      }

      return 1.0 - 0.05 * levelOfDetail;
    }
  }

  /**
   * This provider chooses an icon to paint, depending on the highway type. It also sets the
   * highway number text on top of this icon.
   */
  private static class HighwayIconStyler extends DetailLevelStyler {
    private static Font NUMBER_FONT = new Font("Dialog", Font.BOLD, 12);
    private static Color[] TEXT_COLORS = {Color.white, Color.darkGray, Color.darkGray};
    private static ILcdIcon[][] ICONS = {
        {
            new ShiftedImageIcon("samples/images/highwayIcons/interstate_small.png", 2),
            new ShiftedImageIcon("samples/images/highwayIcons/us_small.png", 2),
            new ShiftedImageIcon("samples/images/highwayIcons/state_large.png", 2)
        },
        {
            new ShiftedImageIcon("samples/images/highwayIcons/interstate_large.png", 2),
            new ShiftedImageIcon("samples/images/highwayIcons/us_large.png", 2),
            new ShiftedImageIcon("samples/images/highwayIcons/state_large.png", 2)
        }
    };

    private final boolean fSelected;

    public HighwayIconStyler(boolean aSelected) {
      fSelected = aSelected;
    }

    private final ALspLabelTextProviderStyle fTextProvider = new ALspLabelTextProviderStyle() {
      @Override
      public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
        int highway_number = getHighwayNumber(aDomainObject);
        return new String[]{Integer.toString(highway_number)};
      }
    };

    @Override
    protected ILcdInterval[] getDetailLevels() {
      return DETAIL_LEVELS;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      int levelOfDetail = getLevelOfDetail(aContext.getViewXYZWorldTransformation());

      // If the current level of detail is visible.
      if (levelOfDetail >= 0) {
        for (Object object : aObjects) {
          HighwayType highway_type = getHighwayType(object);

          if (HIGHWAY_LINE_WIDTHS[highway_type.ordinal()][levelOfDetail] <= 0) {
            aStyleCollector.object(object).hide().submit();
          } else {
            // Use a larger icon when the highway number is more than 3 digits long.
            ILcdIcon icon = getHighwayNumber(object) < 100 ? ICONS[0][highway_type.ordinal()]
                                                           : ICONS[1][highway_type.ordinal()];

            final List<ALspStyle> styles = asList(TLspIconStyle.newBuilder().icon(icon).build(),
                                                  TLspTextStyle.newBuilder().haloThickness(0)
                                                               .textColor(TEXT_COLORS[highway_type.ordinal()])
                                                               .font(NUMBER_FONT)
                                                               .build(),
                                                  fTextProvider,
                                                  fSelected ? TLspLabelOpacityStyle.newBuilder().color(new Color(.5f, .8f, .8f)).build() : TLspLabelOpacityStyle.newBuilder().build()
            );

            aStyleCollector.object(object).styles(styles).submit();
          }
        }
      } else {
        aStyleCollector.objects(aObjects).hide().submit();
      }
    }

  }

  /**
   * An icon that is shifted a bit, such that the center of the icon is not in the center
   * of the image.
   */
  private static class ShiftedImageIcon implements ILcdIcon {
    private final int fVerticalShift;
    private final Image fImage;

    public ShiftedImageIcon(String aFile, int aVerticalShift) {
      fImage = new TLcdIconImageUtil().getImage(aFile);
      fVerticalShift = aVerticalShift;
    }

    @Override
    public int getIconWidth() {
      return fImage.getWidth(null);
    }

    @Override
    public int getIconHeight() {
      return fImage.getHeight(null) + fVerticalShift;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.drawImage(fImage, x, y, fImage.getWidth(null), fImage.getHeight(null), c);
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone"})
    public Object clone() {
      throw new UnsupportedOperationException();
    }
  }
}
