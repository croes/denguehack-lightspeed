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
package samples.gxy.labels.offset;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.luciad.util.ILcdFunction;
import com.luciad.view.ALcdLabelLocations;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;

/**
 * This class is used to paint the labels for a given layer. This class assumes that the labels
 * are painted using an ILcdGXYLabelPainter2.
 */
public class LayerLabelPaintingUtil {

  /**
   * This method paints the labels for the given layer. It collects these labels using the
   * applyOnInteract() method of ILcdGXYEditableLabelsLayer. To collect these labels, it
   * uses the given paint mode. Labels are only painted when they are also marked as painted
   * in the layers ALcdLabelLocations. When a label is painted, the given label painter mode
   * is used.
   *
   * @param aGraphics         the graphics on which the labels are painted.
   * @param aGXYView          the gxy view.
   * @param aLayer            the labels layer for which labels are painted.
   * @param aLabelPainterMode the label painter mode to use when painting the labels.
   * @param aPaintMode        the paint mode used to collect the painted labels.
   */
  public static void paintLabels(Graphics aGraphics,
                                 ILcdGXYView aGXYView,
                                 ILcdGXYEditableLabelsLayer aLayer,
                                 int aLabelPainterMode,
                                 int aPaintMode) {

    // Collect all labels that need to be painted.
    List<PaintedLabelInfo> labels = new ArrayList<PaintedLabelInfo>();
    collectPlacedLabels(labels, aLayer, aGXYView, aPaintMode, aGraphics);

    TLcdGXYContext context = new TLcdGXYContext(aGXYView, aLayer);

    // Paint the labels in reverse order. This guarantees that the labels that were placed first
    // are painted on top of the other labels.
    ListIterator<PaintedLabelInfo> iterator = labels.listIterator(labels.size());
    while (iterator.hasPrevious()) {
      PaintedLabelInfo label = iterator.previous();

      Object label_object = label.getLabel().getDomainObject();
      ILcdGXYLabelPainter2 label_painter = (ILcdGXYLabelPainter2) aLayer.getGXYLabelPainter(label_object);
      if (label_painter == null) {
        throw new RuntimeException("No label painter for object " + label_object);
      }

      label_painter.setLabelIndex(label.getLabel().getLabelIndex());
      label_painter.setSubLabelIndex(label.getLabel().getSubLabelIndex());
      label_painter.setLabelLocation(label.getLocation());

      // Paint the label
      label_painter.paintLabel(aGraphics, aLabelPainterMode, context);
    }
  }

  private static void collectPlacedLabels(List<PaintedLabelInfo> aLabelsSFCT, ILcdGXYEditableLabelsLayer aLayer, ILcdGXYView aView, int aPaintMode, Graphics aGraphics) {
    final Set<TLcdLabelIdentifier> possible_labels = new HashSet<TLcdLabelIdentifier>();
    ILcdFunction collect_function = new ILcdFunction() {
      public boolean applyOn(Object aLabel) throws IllegalArgumentException {
        possible_labels.add((TLcdLabelIdentifier) aLabel);
        return true;
      }
    };
    // Collect the labels that can be painted.
    aLayer.applyOnInteractLabels(collect_function, aGraphics, aPaintMode, aView);

    // Filter these labels (only labels flagged painted should be painted) and fill in their values.
    CollectPaintedLabelsFunction function = new CollectPaintedLabelsFunction(aLabelsSFCT, aLayer, aView, possible_labels);
    ALcdLabelLocations label_locations = aLayer.getLabelLocations();
    label_locations.applyOnPaintedLabelLocations(aView, function);
  }

  private static class CollectPaintedLabelsFunction extends ALcdLabelLocations.LabelLocationFunction {

    private List<PaintedLabelInfo> fPlacedLabels;
    private Set<TLcdLabelIdentifier> fPossibleLabels;
    private ILcdGXYEditableLabelsLayer fLayer;

    public CollectPaintedLabelsFunction(List<PaintedLabelInfo> aLabelsSFCT, ILcdGXYEditableLabelsLayer aLayer, ILcdGXYView aView, Set<TLcdLabelIdentifier> aPossibleLabels) {
      fPlacedLabels = aLabelsSFCT;
      fLayer = aLayer;
      fPossibleLabels = aPossibleLabels;
    }

    public boolean applyOnLabelLocation(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView, TLcdLabelLocation aLocation) {
      TLcdLabelIdentifier label_identifier = new TLcdLabelIdentifier(fLayer, aObject, aLabelIndex, aSubLabelIndex);
      if (!fPossibleLabels.contains(label_identifier)) {
        return true;
      }

      TLcdLabelLocation location = fLayer.getLabelLocations().createLabelLocation();
      location.copyFrom(aLocation);

      PaintedLabelInfo label_info = new PaintedLabelInfo(label_identifier, location);
      fPlacedLabels.add(label_info);

      return true;
    }
  }

  private static class PaintedLabelInfo {

    private TLcdLabelIdentifier fLabel;
    private TLcdLabelLocation fLocation;

    private PaintedLabelInfo(TLcdLabelIdentifier aLabel, TLcdLabelLocation aLocation) {
      fLabel = aLabel;
      fLocation = aLocation;
    }

    public TLcdLabelIdentifier getLabel() {
      return fLabel;
    }

    public TLcdLabelLocation getLocation() {
      return fLocation;
    }
  }
}
