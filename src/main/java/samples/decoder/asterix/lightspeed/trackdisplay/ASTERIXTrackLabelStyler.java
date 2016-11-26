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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import com.luciad.model.ILcdModel;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.manipulation.ALspInteractiveLabelProvider;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.style.ALspSwingLabelStyler;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLabelBoxStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;

import samples.decoder.asterix.lightspeed.trackdisplay.ASTERIXTrackAdditionalData.HoveredLabel;
import samples.lightspeed.labels.interactive.LabelComponentProvider;

/**
 * Styler that styles ASTERIX tracks using interactive labels.
 */
class ASTERIXTrackLabelStyler extends ALspSwingLabelStyler {

  // Use a slightly transparent color to avoid a bug in java where text anti-aliasing can produce artifacts.
  static final Color LABEL_COLOR = new Color(128, 128, 128, 254);

  private static final String TRACK_SUBLABEL_ID = "trackSublabelID";
  private static final TLspLabelBoxStyle ON_HOVER_DARK_BACKGROUND_STYLE = TLspLabelBoxStyle.newBuilder().fillColor(new Color(80, 80, 80)).build();
  private static final TLspPinLineStyle PIN_LINE_STYLE = TLspPinLineStyle.newBuilder().pinEndPosition(TLspPinLineStyle.PinEndPosition.MIDDLE_OF_BOUNDS_ON_EDGE).color(LABEL_COLOR).build();

  // Use a labeling algorithm that places labels above or below the object. Also make sure that labels are not
  // removed when they move outside the view bounds. This way their pin remains visible.
  private static final TLspLabelingAlgorithm BASE_LABELING_ALGORITHM = new TLspLabelingAlgorithm(new TLspLabelLocationProvider(24, TLspLabelLocationProvider.Location.NORTH, TLspLabelLocationProvider.Location.SOUTH));
  static {
    // Keep all labels, always
    BASE_LABELING_ALGORITHM.setForcedPlacementThresholdPriority(Integer.MAX_VALUE);
  }

  private final List<ALspStyle> fAdditionalStyles = new ArrayList<>();
  private final TLspPaintState fPaintState;
  private final TLspPaintRepresentation fPaintRepresentation;

  private final LabelComponentProvider fRegularLabelComponentProvider;
  private final LabelComponentProvider fHighlightedLabelComponentProvider;
  private final LabelComponentProvider fInteractiveLabelComponentProvider;

  private final ASTERIXTrackLabelInvalidator fInvalidator;

  private final ASTERIXTrackAdditionalData fAdditionalData;

  // Keep a reference to the layer, since we need this in HoveredLabelChangeListener. This means that this styler
  // instance should only be used in 1 layer at the time (which is a good practice anyway).
  private ILspLayer fLayer;

  protected ASTERIXTrackLabelStyler(ALspInteractiveLabelProvider aInteractiveLabelProvider,
                                    TLspPaintRepresentationState aPaintRepresentationState,
                                    LabelComponentProvider aRegularLabelComponentProvider,
                                    LabelComponentProvider aHighlightedLabelComponentProvider,
                                    LabelComponentProvider aInteractiveLabelComponentProvider,
                                    ASTERIXTrackAdditionalData aAdditionalData) {
    super(aInteractiveLabelProvider, aPaintRepresentationState, true);
    fPaintState = aPaintRepresentationState.getPaintState();
    fPaintRepresentation = aPaintRepresentationState.getPaintRepresentation();

    fRegularLabelComponentProvider = aRegularLabelComponentProvider;
    fHighlightedLabelComponentProvider = aHighlightedLabelComponentProvider;
    fInteractiveLabelComponentProvider = aInteractiveLabelComponentProvider;

    fAdditionalData = aAdditionalData;
    fAdditionalData.addPropertyChangeListener(new HoveredLabelChangeListener(this));
    fInvalidator = new ASTERIXTrackLabelInvalidator(aAdditionalData);
    fAdditionalStyles.add(PIN_LINE_STYLE);
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    checkLayer(aContext);
    for (Object object : aObjects) {
      ALspStyle componentStyle = getComponentStyle(object, TRACK_SUBLABEL_ID, aContext);
      if (componentStyle != null) {
        List<ALspStyle> styles = new ArrayList<>(4);
        styles.add(componentStyle);
        styles.addAll(fAdditionalStyles);

        // When hovering over a label, it will get a darker background
        TLspLabelID label = new TLspLabelID(aContext.getLayer(), TLspPaintRepresentation.LABEL, object, TRACK_SUBLABEL_ID);
        if (fAdditionalData.isHoveredLabel(label, fPaintState, aContext.getView())) {
          styles.add(ON_HOVER_DARK_BACKGROUND_STYLE);
        }

        aStyleCollector.object(object)
                       .label(TRACK_SUBLABEL_ID)
                       .algorithm(BASE_LABELING_ALGORITHM)
                       .styles(styles)
                       .group(TLspLabelPlacer.DEFAULT_REALTIME_GROUP)
                       .submit();
      }
    }
  }

  private void checkLayer(TLspContext aContext) {
    if (fLayer == null) {
      fLayer = aContext.getLayer();
    } else {
      if (fLayer != aContext.getLayer()) {
        throw new IllegalArgumentException("Styler should be used for at most 1 layer at a time");
      }
    }
  }

  @Override
  protected JComponent getComponent(Object aObject, Object aSublabelId, TLspContext aContext) {
    if (isInteractiveEditedLabel(aObject, aSublabelId, aContext)) {
      return fInteractiveLabelComponentProvider.getComponent(aObject, aSublabelId, aContext);
    } else {
      TLspLabelID label = new TLspLabelID(aContext.getLayer(), fPaintRepresentation, aObject, aSublabelId);
      if (fAdditionalData.isHighlighted(aObject) || fAdditionalData.isHoveredLabel(label, fPaintState, aContext.getView())) {
        return fHighlightedLabelComponentProvider.getComponent(aObject, aSublabelId, aContext);
      } else {
        return fRegularLabelComponentProvider.getComponent(aObject, aSublabelId, aContext);
      }
    }
  }

  @Override
  protected boolean shouldInvalidateLabel(Object aObject, Object aSubLabelID, TLspContext aContext) {
    TLspLabelID label = new TLspLabelID(aContext.getLayer(), fPaintRepresentation, aObject, aSubLabelID);
    boolean highlighted = fAdditionalData.isHighlighted(aObject);
    boolean hovering = fAdditionalData.isHoveredLabel(label, fPaintState, aContext.getView());
    ILcdModel model = aContext.getModel();
    TLcdLockUtil.readLock(model);
    try {
      return fInvalidator.shouldInvalidateLabel(aObject, highlighted || hovering);
    } finally {
      TLcdLockUtil.readUnlock(model);
    }
  }

  /**
   * Makes sure that labels are updated when the mouse hovers over them. In that case, they get a
   * different background color.
   */
  private static class HoveredLabelChangeListener extends ALcdWeakPropertyChangeListener<ASTERIXTrackLabelStyler> {

    public HoveredLabelChangeListener(ASTERIXTrackLabelStyler aStyler) {
      super(aStyler);
    }

    @Override
    protected void propertyChangeImpl(ASTERIXTrackLabelStyler aStyler, PropertyChangeEvent aEvent) {
      if ("hoveredLabel".equals(aEvent.getPropertyName())) {
        Collection<Object> objectsToFire = new ArrayList<>();
        ILspView view = null;
        HoveredLabel oldValue = (HoveredLabel) aEvent.getOldValue();
        if (isValidLabel(aStyler, oldValue)) {
          objectsToFire.add(oldValue.getLabelID().getDomainObject());
          view = oldValue.getView();
        }
        HoveredLabel newValue = (HoveredLabel) aEvent.getNewValue();
        if (isValidLabel(aStyler, newValue)) {
          objectsToFire.add(newValue.getLabelID().getDomainObject());
          view = newValue.getView();
        }
        if (!objectsToFire.isEmpty()) {
          TLspContext context = new TLspContext(aStyler.fLayer, view);
          for (Object object : objectsToFire) {
            aStyler.componentChanged(object, TRACK_SUBLABEL_ID, context);
          }
        }
      }
    }

    private boolean isValidLabel(ASTERIXTrackLabelStyler aStyler, HoveredLabel aOldValue) {
      return aOldValue != null &&
             aOldValue.getPaintState().equals(aStyler.fPaintState) &&
             aOldValue.getLabelID().getLayer() == aStyler.fLayer;
    }
  }
}
