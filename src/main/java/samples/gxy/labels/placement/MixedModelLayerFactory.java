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
package samples.gxy.labels.placement;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdCircularArc;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatCircularArcByCenterPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelLabelingAlgorithmProvider;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.ILcdGXYLabelingPathProvider;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYOnPathLabelingAlgorithm;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.common.AntiAliasedLabelPainter;
import samples.gxy.decoder.MapSupport;

/**
 * Layer factory that creates a layer for a model with a mixture of different shapes. This class
 * also provides functionality to customize labeling.
 */
public class MixedModelLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel) {
    // Create a layer for the mixed model
    TLcdGXYLayer layer = new TLcdGXYLayer(aModel.getModelDescriptor().getDisplayName());
    layer.setModel(aModel);
    layer.setLabeled(true);
    layer.setVisible(true);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

    // Create a new painter/editor provider.
    TLcdGXYShapePainter painterProvider = new TLcdGXYShapePainter();
    painterProvider.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 5, Color.ORANGE));
    painterProvider.setSelectedIcon(new TLcdSymbol(TLcdSymbol.RECT, 7, Color.RED));
    painterProvider.setLineStyle(new TLcdG2DLineStyle(Color.WHITE, Color.RED));
    layer.setGXYPainterProvider(painterProvider);
    layer.setGXYEditorProvider(painterProvider);

    // Create a label painter that handles the labels differently, based on the domain object.
    TLcdGXYLabelPainter label_painter = new MixedLabelPainter();
    label_painter.setFilled(true);
    label_painter.setFrame(true);
    label_painter.setSelectionColor(Color.RED);
    label_painter.setFont(new Font("Dialog", Font.PLAIN, 12));
    // Add anti-aliasing to increase the legibility of the (rotated) labels.
    AntiAliasedLabelPainter antiAliasedLabelPainter = new AntiAliasedLabelPainter((ILcdGXYLabelPainterProvider) label_painter);
    layer.setGXYLabelPainterProvider(antiAliasedLabelPainter);

    return layer;
  }

  public static ILcdModel createMixedModel() {
    // Create a model that contains a mix of points, polylines and arcs.
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor(new TLcdModelDescriptor("none", "mixed model", "Mixed layer"));
    TLcdGeodeticReference reference = new TLcdGeodeticReference();
    model.setModelReference(reference);

    // Add a series of point
    TLcdLonLatPoint p1 = new TLcdLonLatPoint(-15.0, -10.0);
    TLcdLonLatPoint p2 = new TLcdLonLatPoint(-5.0, 0.0);
    TLcdLonLatPoint p3 = new TLcdLonLatPoint(5.0, -7.5);
    TLcdLonLatPoint p4 = new TLcdLonLatPoint(10.0, 10.0);
    TLcdLonLatPoint p5 = new TLcdLonLatPoint(15.0, -7.5);
    model.addElement(p1, ILcdFireEventMode.NO_EVENT);
    model.addElement(p2, ILcdFireEventMode.NO_EVENT);
    model.addElement(p3, ILcdFireEventMode.NO_EVENT);
    model.addElement(p4, ILcdFireEventMode.NO_EVENT);
    model.addElement(p5, ILcdFireEventMode.NO_EVENT);

    // Add a polyline consisting of those points
    ILcd2DEditablePoint[] points = {p1, p2, p3, p4, p5};
    ILcd2DEditablePointList point_list = new TLcd2DEditablePointList(points, false);
    TLcdLonLatPolyline polyline = new TLcdLonLatPolyline(point_list);
    model.addElement(polyline, ILcdFireEventMode.NO_EVENT);

    // Add an arc
    TLcdLonLatCircularArcByCenterPoint arc = new TLcdLonLatCircularArcByCenterPoint(-10, 10, 1000000, 0, 270, reference.getGeodeticDatum().getEllipsoid());
    model.addElement(arc, ILcdFireEventMode.NO_EVENT);

    return model;
  }

  /**
   * This label painter handles labels of point and polylines differently
   *
   * Point labels :
   * - only 1 label is provided
   * - the label is painted in orange
   * - the label contains "point" as text
   *
   * Polyline labels :
   * - the number of provided labels equals the number of segments of the polyline
   * - the labels are painted in white
   * - the labels contain "segment" as text
   *
   * Arc labels :
   * - the number of provided labels is 5 (this is the maximal number of labels placed per arc)
   * - the labels are painted in green
   * - the labels contain " arc " as text
   */
  private static class MixedLabelPainter extends TLcdGXYLabelPainter {

    private String[] fPointLabels = {"point"};
    private String[] fLineLabels = {"segment"};
    private String[] fArcLabels = {"arc"};

    @Override
    protected String[] retrieveLabels(int aMode, ILcdGXYContext aGXYContext) {
      if (getObject() instanceof ILcdPoint) {
        return fPointLabels;
      } else if (getObject() instanceof ILcdCircularArc) {
        return fArcLabels;
      } else {
        return fLineLabels;
      }
    }

    @Override
    public int getLabelCount(Graphics aGraphics, ILcdGXYContext aGXYContext) {
      if (getObject() instanceof ILcdPolyline) {
        ILcdPolyline polyline = (ILcdPolyline) getObject();
        return polyline.getPointCount() - 1;
      } else if (getObject() instanceof ILcdCircularArc) {
        return 5;
      } else {
        return 1;
      }
    }

    @Override
    public void setObject(Object aObject) {
      super.setObject(aObject);
      if (aObject instanceof ILcdPolyline) {
        setBackground(Color.WHITE);
      } else if (aObject instanceof ILcdCircularArc) {
        setBackground(Color.GREEN);
      } else {
        setBackground(Color.ORANGE);
      }
    }
  }

  /**
   * This labeling algorithm provider returns
   * - a path algorithm for labels of polylines of the mixed layer
   * - a location list labeling algorithm for labels of points of the mixed layer
   * - a path algorithm for labels of arcs of the mixed layer
   * - a location list labeling algorithm for labels of all other layers
   *
   * It also sets the priorities
   * - the label of the first segment of a polyline has the highest priority
   * - the labels of points have lower priorities
   * - the labels of arcs have lover priorities
   * - the labels of the other segments of a polyline have even lower priorities
   * - all other labels have the lowest priority
   */
  public static class MixedLabelingAlgorithmProvider implements
                                                      ILcdGXYLabelLabelingAlgorithmProvider<ALcdGXYDiscretePlacementsLabelingAlgorithm> {

    private TLcdGXYLocationListLabelingAlgorithm fDefaultAlgorithm;
    private TLcdGXYLocationListLabelingAlgorithm fPointAlgorithm;
    private TLcdGXYOnPathLabelingAlgorithm fPolylineAlgorithm;
    private TLcdGXYOnPathLabelingAlgorithm fArcAlgorithm;

    public MixedLabelingAlgorithmProvider() {
      fDefaultAlgorithm = new TLcdGXYLocationListLabelingAlgorithm();
      fDefaultAlgorithm.setLabelPriorityProvider(new ILcdGXYMultiLabelPriorityProvider() {
        @Override
        public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
          return 1000;
        }
      });

      fPointAlgorithm = new TLcdGXYLocationListLabelingAlgorithm();
      fPointAlgorithm.setLabelPriorityProvider(new ILcdGXYMultiLabelPriorityProvider() {
        @Override
        public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
          return 10;
        }
      });

      fPolylineAlgorithm = new TLcdGXYOnPathLabelingAlgorithm(new SegmentPathProvider(), false);
      fPolylineAlgorithm.setReusePreviousLocations(true);
      fPolylineAlgorithm.setLabelPriorityProvider(new ILcdGXYMultiLabelPriorityProvider() {
        @Override
        public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
          // The first segment always has the highest priority and will never be removed in favor of an other label
          if (aLabelIndex == 0) {
            return 1;
          } else {
            return 20;
          }
        }
      });

      fArcAlgorithm = new TLcdGXYOnPathLabelingAlgorithm();
      fArcAlgorithm.setMinimumGap(300);
      fArcAlgorithm.setAlignmentMode(TLcdGXYOnPathLabelingAlgorithm.AlignmentMode.LEFT);
      fArcAlgorithm.setLabelPriorityProvider(new ILcdGXYMultiLabelPriorityProvider() {
        @Override
        public int getPriority(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
          return 15;
        }
      });
    }

    @Override
    public ALcdGXYDiscretePlacementsLabelingAlgorithm getLabelingAlgorithm(TLcdLabelIdentifier aLabel) {
      if (aLabel.getDomainObject() instanceof ILcdPoint) {
        return fPointAlgorithm;
      } else if (aLabel.getDomainObject() instanceof ILcdCircularArc) {
        return fArcAlgorithm;
      } else {
        return fPolylineAlgorithm;
      }
    }
  }

  /**
   * This labeling path provider, used by TLcdGXYOnPathLabelingAlgorithm, returns an awt path
   * per polyline segment. This way we can ensure that every segment of the polyline is labeled.
   */
  private static class SegmentPathProvider implements ILcdGXYLabelingPathProvider {

    // We use our own path painter to retrieve paths of segments.
    private TLcdGXYPointListPainter fSegmentPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);

    @Override
    public boolean getPathsSFCT(Object aDomainObject, List<TLcdLabelIdentifier> aLabels, ILcdGXYContext aGXYContext, List<ILcdAWTPath> aPathsSFCT) {
      if (!(aDomainObject instanceof ILcdPolyline)) {
        return false;
      }
      ILcdPolyline polyline = (ILcdPolyline) aDomainObject;

      int segment_count = Math.min(aLabels.size(), polyline.getPointCount() - 1);

      // Split the polyline into segments
      List<ILcdPolyline> polylines = new ArrayList<ILcdPolyline>();
      for (int i = 0; i < segment_count; i++) {
        ILcdPoint point1 = polyline.getPoint(i);
        ILcdPoint point2 = polyline.getPoint(i + 1);
        TLcdLonLatPolyline segment = new TLcdLonLatPolyline();
        segment.insert2DPoint(0, point1.getX(), point1.getY());
        segment.insert2DPoint(1, point2.getX(), point2.getY());
        polylines.add(segment);
      }

      for (int i = 0; i < aLabels.size(); i++) {
        if (i < segment_count) {
          // Calculate a path for each segment
          ILcdPolyline segment = polylines.get(i);
          TLcdAWTPath awt_path = new TLcdAWTPath(100, 1.0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
          fSegmentPainter.setObject(segment);
          fSegmentPainter.appendAWTPath(aGXYContext, ILcdGXYPainter.BODY | ILcdGXYPainter.DEFAULT, awt_path);
          aPathsSFCT.add(awt_path);
        } else {
          aPathsSFCT.add(null);
        }
      }

      return true;
    }
  }
}
