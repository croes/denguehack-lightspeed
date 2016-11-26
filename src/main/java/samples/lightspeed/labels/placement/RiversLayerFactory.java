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
package samples.lightspeed.labels.placement;

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.NORTH_EAST;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPolypoint;
import com.luciad.shape.ILcdShapeList;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspCurvedPathLabelingAlgorithm;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.label.location.ALspLabelLocations;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.location.TLspPathLabelLocation;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspLabelOpacityStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * Layer factory for rivers. This layer factory adds two labels for each river:
 * <ol>
 *   <li>A label indicating the river's name, placed along the river itself, following its path.</li>
 *   <li>A label indicating the river's spring, placed at the spring of the river.</li>
 * </ol>
 * A custom styler is used for this.
 *
 * Note that the river's springs are determined as the first point of the first polyline, which
 * might not correspond to reality.
 */
public class RiversLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    TLspLineStyle regularStyle = TLspLineStyle.newBuilder().color(Color.blue).width(1.0f).opacity(0.5f).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();
    TLspLineStyle selectionStyle = TLspLineStyle.newBuilder().color(Color.red).width(1.0f).opacity(0.5f).elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).build();

    // Configure the label styling
    TLspTextStyle regularLabelStyle = TLspTextStyle.newBuilder().font(Font.decode("Default-BOLD-12")).textColor(new Color(128, 128, 255)).haloColor(Color.black).build();
    TLspTextStyle selectionLabelStyle = regularLabelStyle.asBuilder().textColor(Color.red).build();
    TLspTextStyle editingLabelStyle = regularLabelStyle.asBuilder().textColor(Color.orange).build();

    layerBuilder.model(aModel)
                .selectable(true)
                .labelEditable(true)
                .labelStyler(TLspPaintState.REGULAR, new RiverLabelStyler(regularLabelStyle))
                .labelStyler(TLspPaintState.SELECTED, new RiverLabelStyler(selectionLabelStyle))
                .labelStyler(TLspPaintState.EDITED, new RiverLabelStyler(editingLabelStyle))
                .bodyStyler(TLspPaintState.REGULAR, regularStyle)
                .bodyStyler(TLspPaintState.SELECTED, selectionStyle)
                .bodyStyler(TLspPaintState.EDITED, selectionStyle)
                .synchronizePainters(false);

    return layerBuilder.build();
  }

  /**
   * Styler that add two labels for each river:
   * <ol>
   *   <li>A label indicating the river's name, placed along the river itself, following its path.</li>
   *   <li>A label indicating the river's spring, placed at the spring of the river.</li>
   * </ol>
   * Note that the river's springs are determined as the first point of the first polyline, which
   * might not correspond to reality.
   */
  private static class RiverLabelStyler extends ALspLabelStyler implements ILspCustomizableStyler {

    private static final String NAME_SUBLABEL_ID = "name";
    private static final String SPRING_SUBLABEL_ID = "spring";

    private final ILspLabelingAlgorithm fCurvedLabelAlgorithm = new TLspCurvedPathLabelingAlgorithm();
    private final ILspLabelingAlgorithm fNorthEastLocationAlgorithm = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(10, NORTH_EAST));

    private ALspStyleTargetProvider fAnchorPointProvider = new ALspStyleTargetProvider() {
      @Override
      public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
        ILcdShapeList shapeList = (ILcdShapeList) aObject;
        ILcdPolypoint polypoint = (ILcdPolypoint) shapeList.getShape(0);
        aResultSFCT.add(polypoint.getPoint(0));
      }

    };
    private ALspLabelTextProviderStyle fRiverSpringTextProvider = new ALspLabelTextProviderStyle() {
      @Override
      public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
        return new String[]{aDomainObject.toString() + " spring"};
      }
    };
    private final PropertyChangeListener fCustomizableStyleListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent aPropertyChangeEvent) {
        fireStyleChangeEvent(); // When the customizable style changes, a style change event should be fired
      }
    };

    private final TLspCustomizableStyle fTextStyle;
    private final TLspCustomizableStyle fLighterTextStyle;
    private final TLspCustomizableStyle fLabelOpacityStyle;

    public RiverLabelStyler(TLspTextStyle aTextStyle) {
      fTextStyle = createCustomizableStyle(aTextStyle);
      fLighterTextStyle = createCustomizableStyle(aTextStyle.asBuilder().textColor(aTextStyle.getTextColor().brighter()).build());
      fLabelOpacityStyle = createCustomizableStyle(TLspLabelOpacityStyle.newBuilder().build());
    }

    private TLspCustomizableStyle createCustomizableStyle(ALspStyle aStyle) {
      TLspCustomizableStyle customizableStyle = new TLspCustomizableStyle(aStyle, true);
      customizableStyle.addPropertyChangeListener(fCustomizableStyleListener);
      return customizableStyle;
    }

    @Override
    public Collection<TLspCustomizableStyle> getStyles() {
      return Arrays.asList(fTextStyle, fLighterTextStyle, fLabelOpacityStyle);
    }

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        aStyleCollector
            .object(object)
            .label(NAME_SUBLABEL_ID)
            .styles(getRiverStyler(object, aContext))
            .algorithm(fCurvedLabelAlgorithm)
            .submit();
      }

      aStyleCollector.objects(aObjects)
                     .label(SPRING_SUBLABEL_ID)
                     .styles(getSpringStyles())
                     .algorithm(fNorthEastLocationAlgorithm)
                     .geometry(fAnchorPointProvider)
                     .submit();
    }

    private List<ALspStyle> getRiverStyler(Object aObject, TLspContext aContext) {
      List<ALspStyle> styles = new ArrayList<ALspStyle>();
      if (fTextStyle.isEnabled()) {
        styles.add(fTextStyle.getStyle());
      }
      if (fLabelOpacityStyle.isEnabled()) {
        styles.add(fLabelOpacityStyle.getStyle());
      }
      if (shouldPaintPin(aObject, NAME_SUBLABEL_ID, aContext)) {
        styles.add(TLspPinLineStyle.newBuilder().build());
      }
      return styles;
    }

    private List<ALspStyle> getSpringStyles() {
      List<ALspStyle> styles = new ArrayList<ALspStyle>();
      styles.add(TLspPinLineStyle.newBuilder().build());
      styles.add(fRiverSpringTextProvider);
      if (fLighterTextStyle.isEnabled()) {
        styles.add(fLighterTextStyle.getStyle());
      }
      if (fLabelOpacityStyle.isEnabled()) {
        styles.add(fLabelOpacityStyle.getStyle());
      }
      return styles;
    }

    private boolean shouldPaintPin(Object aObject, Object aSubLabelID, TLspContext aContext) {
      ALspLabelLocations labelLocations = aContext.getView().getLabelPlacer().getLabelLocations();
      TLspLabelID labelID = new TLspLabelID(aContext.getLayer(), TLspPaintRepresentation.LABEL, aObject, aSubLabelID);
      ALspLabelLocations.LocationInfo locationInfo = labelLocations.getLabelLocation(aContext.getView(), labelID);
      return locationInfo.getLocation() != null && !(locationInfo.getLocation() instanceof TLspPathLabelLocation);
    }
  }
}
