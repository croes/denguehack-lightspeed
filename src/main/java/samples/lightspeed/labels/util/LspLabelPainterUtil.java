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
package samples.lightspeed.labels.util;

import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.TLspLabelPlacement;
import com.luciad.view.lightspeed.label.location.ALspLabelLocation;
import com.luciad.view.lightspeed.label.location.ALspLabelLocations;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.ILspPainter;
import com.luciad.view.lightspeed.painter.label.ILspLabelPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.location.ILspPathLocationLabelPainter;
import com.luciad.view.lightspeed.painter.label.location.ILspStampLocationLabelPainter;

/**
 * Contains utility methods related to labels and label painting.
 */
public final class LspLabelPainterUtil {

  private LspLabelPainterUtil() {
  }

  /**
   * Returns the label painter for the given label, and throws an {@code IllegalArgumentException} if none can be found.
   * @param aLabel a label.
   * @return the label painter for the given label. Never {@code null}.
   */
  public static ILspLabelPainter getLabelPainter(TLspLabelID aLabel) {
    if (!(aLabel.getLayer() instanceof ILspInteractivePaintableLayer)) {
      throw new IllegalArgumentException("Layer should be an ILspInteractivePaintableLayer");
    }
    ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLabel.getLayer();
    ILspPainter painter = layer.getPainter(aLabel.getPaintRepresentation());
    if (!(painter instanceof ILspLabelPainter)) {
      throw new IllegalArgumentException("Painter should be an ILspLabelPainter");
    }
    return (ILspLabelPainter) painter;
  }

  /**
   * Returns the stamp location label painter for the given label, and throws an {@code IllegalArgumentException}
   * if none can be found.
   * @param aLabel a label.
   * @return the stamp location label painter for the given label. Never {@code null}.
   */
  public static ILspStampLocationLabelPainter getStampLabelPainter(TLspLabelID aLabel) {
    ILspLabelPainter labelPainter = getLabelPainter(aLabel);
    if (!(labelPainter instanceof ILspStampLocationLabelPainter)) {
      throw new IllegalArgumentException("Painter should be an ILspStampLocationLabelPainter");
    }
    return (ILspStampLocationLabelPainter) labelPainter;
  }

  /**
   * Returns the path location label painter for the given label, and throws an {@code IllegalArgumentException}
   * if none can be found.
   * @param aLabel a label.
   * @return the path location label painter for the given label. Never {@code null}.
   */
  public static ILspPathLocationLabelPainter getPathLabelPainter(TLspLabelID aLabel) {
    ILspLabelPainter labelPainter = getLabelPainter(aLabel);
    if (!(labelPainter instanceof ILspPathLocationLabelPainter)) {
      throw new IllegalArgumentException("Painter should be an ILspPathLocationLabelPainter");
    }
    return (ILspPathLocationLabelPainter) labelPainter;
  }

  /**
   * Creates a label placement for the given label and label location.
   *
   * @param aLabel                 a label
   * @param aLabelLocation         a label location
   * @param aCurrentLabelLocations the current label locations
   * @param aPaintState            the paint state
   * @param aContext               the context
   * @return a label placement for the given label and label location.
   */
  public static TLspLabelPlacement createLabelPlacement(TLspLabelID aLabel,
                                                        ALspLabelLocation aLabelLocation,
                                                        ALspLabelLocations aCurrentLabelLocations,
                                                        TLspPaintState aPaintState,
                                                        TLspContext aContext) {
    ILspLabelPainter labelPainter = getLabelPainter(aLabel);
    try {
      ILcd3DEditableBounds bounds = new TLcdXYZBounds();
      double rotation = labelPainter.labelBoundsSFCT(aLabel, aLabelLocation, aCurrentLabelLocations, aPaintState, aContext, bounds);

      double x = bounds.getLocation().getX();
      double y = bounds.getLocation().getY();
      double w = bounds.getWidth();
      double h = bounds.getHeight();
      return new TLspLabelPlacement(
          aLabel,
          aLabelLocation,
          true,
          x, y, w, h, rotation
      );
    } catch (TLcdNoBoundsException e) {
      // No label location can be calculated
      return null;
    }
  }
}
