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
package samples.realtime.lightspeed.offsetIcon;

import static java.awt.Color.black;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import samples.lightspeed.labels.util.LspLabelPainterUtil;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.lightspeed.labeling.TLspContinuousLabelingAlgorithm;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.label.TLspLabelEditor;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelDependencyProvider;
import com.luciad.view.lightspeed.label.algorithm.ILspLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.location.ILspStampLocationLabelPainter;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

import samples.gxy.common.AntiAliasedIcon;

class OffsetIconLayerFactory extends ALspSingleLayerFactory {

  private static final String PARENT_SUBLABEL_ID = "parent";
  private static final String CHILD_SUBLABEL_ID = "child";

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspPinLineStyle childPinStyle = TLspPinLineStyle.newBuilder().color(new Color(240, 215, 140)).build();
    TLspTextStyle defaultChildLabelStyle = TLspTextStyle.newBuilder().font(Font.decode("Default-BOLD-12")).textColor(new Color(255, 255, 255)).haloColor(black).build();
    TLspTextStyle selectedChildLabelStyle = defaultChildLabelStyle.asBuilder().textColor(Color.red).build();
    TLspTextStyle editedChildLabelStyle = defaultChildLabelStyle.asBuilder().textColor(Color.orange).build();

    TLspPinLineStyle parentPinStyle = TLspPinLineStyle.newBuilder().build();
    ILcdIcon defaultLabelIcon = new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 24, black, new Color(240, 215, 140)));
    ILcdIcon selectedLabelIcon = new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 24, black, Color.red));
    ILcdIcon editedLabelIcon = new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 24, black, Color.orange));
    TLspIconStyle defaultParentLabelStyle = TLspIconStyle.newBuilder().icon(defaultLabelIcon).build();
    TLspIconStyle selectedParentLabelStyle = TLspIconStyle.newBuilder().icon(selectedLabelIcon).build();
    TLspIconStyle editedParentLabelStyle = TLspIconStyle.newBuilder().icon(editedLabelIcon).build();

    TLspContinuousLabelingAlgorithm labelingAlgorithm = createLabelingAlgorithm();

    ParentChildStyler defaultLabelStyler = new ParentChildStyler(labelingAlgorithm);
    defaultLabelStyler.setParentStyles(parentPinStyle, defaultParentLabelStyle);
    defaultLabelStyler.setChildStyles(childPinStyle, defaultChildLabelStyle);

    ParentChildStyler selectedLabelStyler = new ParentChildStyler(labelingAlgorithm);
    selectedLabelStyler.setParentStyles(parentPinStyle, selectedParentLabelStyle);
    selectedLabelStyler.setChildStyles(childPinStyle, selectedChildLabelStyle);

    ParentChildStyler editedLabelStyler = new ParentChildStyler(labelingAlgorithm);
    editedLabelStyler.setParentStyles(parentPinStyle, editedParentLabelStyle);
    editedLabelStyler.setChildStyles(childPinStyle, editedChildLabelStyle);

    // Make labels sticky during editing. Because of this, the continuous algorithm won't
    // move the labels during editing.
    TLspLabelEditor labelEditor = new TLspLabelEditor();
    labelEditor.setStickyOnEdit(true);

    ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder()
                                                               .model(aModel)
                                                               .bodyStyler(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, new Color(240, 215, 140))).build())
                                                               .bodyStyler(TLspPaintState.SELECTED, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, Color.red)).build())
                                                               .bodyStyler(TLspPaintState.EDITED, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, Color.orange)).build())
                                                               .labelStyler(TLspPaintState.REGULAR, defaultLabelStyler)
                                                               .labelStyler(TLspPaintState.SELECTED, selectedLabelStyler)
                                                               .labelStyler(TLspPaintState.EDITED, editedLabelStyler)
                                                               .selectable(true)
                                                               .labelEditable(true)
                                                               .labelEditor(labelEditor)
                                                               .labelScaleRange(new TLcdInterval(2e-4, Double.MAX_VALUE))
                                                               .build();

    for (TLspPaintRepresentation paintRepresentation : layer.getPaintRepresentations()) {
      layer.setVisible(paintRepresentation, true);
    }

    return layer;
  }

  private TLspContinuousLabelingAlgorithm createLabelingAlgorithm() {
    TLspContinuousLabelingAlgorithm algorithm = new TLspContinuousLabelingAlgorithm() {
      @Override
      protected void retrieveDesiredLocation(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext, Point aRelativeLocationSFCT) {
        boolean offsetIcon = aLabel.getSubLabelID().equals(PARENT_SUBLABEL_ID);
        if (offsetIcon) {
          // Offset of the label anchor point (which is the middle of the bounds) with respect to
          // the object anchor point (which is the city).
          aRelativeLocationSFCT.setLocation(20, -20);
        } else {
          double offsetX = 20.0;
          double offsetY = 0.0;

          ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(aLabel);
          try {
            Dimension2D dimension = new Dimension();
            labelPainter.labelDimensionSFCT(aLabel, aPaintState, aContext, dimension);
            offsetX += dimension.getWidth() * 0.5;
          } catch (TLcdNoBoundsException e) {
            // Shouldn't happen
          }
          aRelativeLocationSFCT.setLocation(offsetX, offsetY);
        }
      }
    };
    // Avoid slave labels getting a random offset by e.g. zooming out, and making them appear with
    // an invalid previous location.
    algorithm.setReuseLocationsScaleRatioInterval(new TLcdInterval(0.0, Double.MAX_VALUE));

    // Define the master-slave dependency as follows : all labels with the child sublabel ID are
    // a slave of the label with the parent sublabel ID.
    algorithm.setMasterSlaveDependencyProvider(new ILspLabelDependencyProvider() {
      @Override
      public void getDependingLabels(TLspLabelID aLabel, TLspPaintState aPaintState, TLspContext aContext, List<TLspLabelID> aDependingIdentifiersSFCT) {
        if (aLabel.getSubLabelID().equals(CHILD_SUBLABEL_ID)) {
          aDependingIdentifiersSFCT.add(new TLspLabelID(
              aLabel.getLayer(),
              aLabel.getPaintRepresentation(),
              aLabel.getDomainObject(),
              PARENT_SUBLABEL_ID
          ));
        }
      }
    });

    algorithm.setMaxDistance(30);

    return algorithm;
  }

  private static class ParentChildStyler extends ALspLabelStyler {

    private List<ALspStyle> fParentStyles = new ArrayList<ALspStyle>();
    private List<ALspStyle> fChildStyles = new ArrayList<ALspStyle>();
    private final ILspLabelingAlgorithm fLabelingAlgorithm;

    public ParentChildStyler(ILspLabelingAlgorithm aLabelingAlgorithm) {
      fLabelingAlgorithm = aLabelingAlgorithm;
    }

    private void setParentStyles(ALspStyle... aStyles) {
      Collections.addAll(fParentStyles, aStyles);
    }

    private void setChildStyles(ALspStyle... aStyles) {
      Collections.addAll(fChildStyles, aStyles);
    }

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      aStyleCollector.objects(aObjects).algorithm(fLabelingAlgorithm).submit();
      aStyleCollector.objects(aObjects).label(PARENT_SUBLABEL_ID).styles(fParentStyles).submit();
      aStyleCollector.objects(aObjects).label(CHILD_SUBLABEL_ID).anchorLabel(PARENT_SUBLABEL_ID).styles(fChildStyles).submit();
    }
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return true;
  }
}
