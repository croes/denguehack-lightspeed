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
package samples.realtime.gxy.clusterLabeling;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Timer;

import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.ALcdLabelLocations;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabeledObjectInfo;

/**
 * This mouse listener will declutter the icons near the mouse when it stops.
 */
class DeclutteringMouseListener implements MouseListener, MouseMotionListener {

  private static final int MOUSE_DELAY = 150;
  private static final double MOUSE_MOVE_THRESHOLD = 2.0d;
  private static final Dimension SENSITIVITY = new Dimension(30, 30);

  // This timer is here to initiate the decluttering if the mouse has stopped moving for a moment.
  private final Timer fInitiateDeclutteringTimer;

  private MouseEvent fLastMouseEvent;

  private final ILcdGXYView fGXYView;
  private final ILcdFilter fModelFilter;
  private final AnimatedDeclutterLabelingAlgorithmProvider fAlgorithmProvider;

  public DeclutteringMouseListener(ILcdFilter aModelFilter, ILcdGXYView aGXYView, AnimatedDeclutterLabelingAlgorithmProvider aAlgorithmProvider) {
    fGXYView = aGXYView;
    fModelFilter = aModelFilter;
    fAlgorithmProvider = aAlgorithmProvider;

    // When the fInitiateDeclutteringTimer fires, it starts to declutter the icons
    // and labels in the neighborhood of the mouse.
    fInitiateDeclutteringTimer = new Timer(0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        declutterNear(fLastMouseEvent.getX(), fLastMouseEvent.getY());
      }
    });
    fInitiateDeclutteringTimer.setInitialDelay(MOUSE_DELAY);
    fInitiateDeclutteringTimer.setRepeats(false);
  }

  private void declutterNear(int aX, int aY) {
    final Graphics graphics = fGXYView.getGraphics();

    try {
      // Find the labels of all objects that are in range of the mouse, and remember their objects.
      List<TLcdCollectedLabeledObjectInfo> objects_to_declutter = findLabeledObjectsNearLocation(aX, aY, graphics);
      if (objects_to_declutter.size() <= 1) {
        return;
      }
      fAlgorithmProvider.declutterObjects(objects_to_declutter);
    } finally {
      if (graphics != null) {
        graphics.dispose();
      }
    }
  }

  /**
   * Finds the objects whose labels are near the specified location.
   *
   * @param aX        The x-coordinate in view coordinates of the location.
   * @param aY        The y-coordinate in view coordinates of the location.
   * @param aGraphics The graphics on which the calculations can be performed.
   *
   * @return A List of domain objects, whose labels are near the mouse location.
   */
  private List<TLcdCollectedLabeledObjectInfo> findLabeledObjectsNearLocation(int aX, int aY, final Graphics aGraphics) {
    final Rectangle rectangle = new Rectangle(aX, aY, SENSITIVITY.width, SENSITIVITY.height);
    rectangle.translate(-SENSITIVITY.width / 2, -SENSITIVITY.height / 2);

    final TLcdGXYContext context = new TLcdGXYContext();
    final List<TLcdCollectedLabeledObjectInfo> to_declutter = new ArrayList<TLcdCollectedLabeledObjectInfo>();
    for (Enumeration layers = fGXYView.layers(); layers.hasMoreElements(); ) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();

      if (!fModelFilter.accept(layer.getModel())) {
        continue;
      }

      if (!(layer instanceof ILcdGXYEditableLabelsLayer)) {
        continue;
      }
      final ILcdGXYEditableLabelsLayer labels_layer = (ILcdGXYEditableLabelsLayer) layer;

      context.resetFor(layer, fGXYView);

      labels_layer.getLabelLocations().applyOnPaintedLabelLocations(fGXYView, new ALcdLabelLocations.LabelLocationFunction() {
        public boolean applyOnLabelLocation(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView, TLcdLabelLocation aLocation) {
          try {
            ILcdGXYLabelPainter2 label_painter = (ILcdGXYLabelPainter2) labels_layer.getGXYLabelPainter(aObject);
            label_painter.setLabelIndex(aLabelIndex);
            label_painter.setSubLabelIndex(aSubLabelIndex);
            label_painter.setLabelLocation(aLocation);
            Rectangle result = new Rectangle();
            label_painter.labelBoundsSFCT(aGraphics, ILcdGXYLabelPainter2.DEFAULT | ILcdGXYLabelPainter2.BODY, context, result);
            if (result.intersects(rectangle) && !containsObject(to_declutter, aObject)) {
              ILcdGXYPainter painter = labels_layer.getGXYPainter(aObject);
              TLcdXYBounds objectBounds = new TLcdXYBounds();
              painter.boundsSFCT(aGraphics, ILcdGXYPainter.DEFAULT | ILcdGXYPainter.BODY, context, objectBounds);
              if (rectangle.intersects(objectBounds.getLocation().getX(),
                                       objectBounds.getLocation().getY(),
                                       objectBounds.getWidth(),
                                       objectBounds.getHeight())) {
                TLcdCollectedLabeledObjectInfo labeled_object = new TLcdCollectedLabeledObjectInfo(aObject, labels_layer);
                to_declutter.add(labeled_object);
              }
            }
          } catch (TLcdNoBoundsException ignored) {
            // Ignore, this label will not be decluttered
          }
          return true;
        }
      });
    }

    return to_declutter;
  }

  private boolean containsObject(List<TLcdCollectedLabeledObjectInfo> aLabeledObjects, Object aDomainObject) {
    for (TLcdCollectedLabeledObjectInfo labeled_object : aLabeledObjects) {
      if (labeled_object.getDomainObject() == aDomainObject) {
        return true;
      }
    }
    return false;
  }

  public void mouseMoved(MouseEvent aMouseEvent) {
    scheduleDecluttering(aMouseEvent);
  }

  public void mouseDragged(MouseEvent aMouseEvent) {
    scheduleDecluttering(aMouseEvent);
  }

  public void mouseExited(MouseEvent e) {
    fInitiateDeclutteringTimer.stop();
    fAlgorithmProvider.stopDecluttering();
  }

  // We are not interested in these 4 events
  public void mouseClicked(MouseEvent aMouseEvent) {
  }

  public void mousePressed(MouseEvent aMouseEvent) {
  }

  public void mouseReleased(MouseEvent aMouseEvent) {
  }

  public void mouseEntered(MouseEvent aMouseEvent) {
  }

  private void scheduleDecluttering(MouseEvent aMouseEvent) {
    // Do not let small mouse movements delay the decluttering.
    if (fLastMouseEvent != null) {
      double distance = aMouseEvent.getPoint().distance(fLastMouseEvent.getPoint());
      if (distance < MOUSE_MOVE_THRESHOLD) {
        return;
      }
    }

    List<TLcdCollectedLabelInfo> labels_to_declutter = fAlgorithmProvider.getLabelsToDeclutter();

    Rectangle region_of_decluttered = getRegion(labels_to_declutter, fGXYView);
    if (region_of_decluttered == null || !region_of_decluttered.contains(aMouseEvent.getPoint())) {
      fAlgorithmProvider.stopDecluttering();
      fInitiateDeclutteringTimer.stop();
      fInitiateDeclutteringTimer.restart();
    }
    fLastMouseEvent = aMouseEvent;
  }

  private static Rectangle getRegion(List<TLcdCollectedLabelInfo> aLabelsToDeclutter, ILcdGXYView aGXYView) {
    if (aLabelsToDeclutter != null && aLabelsToDeclutter.size() > 0) {
      Graphics graphics = aGXYView.getGraphics();
      try {
        Rectangle single_bounds = new Rectangle();
        Rectangle all_bounds = null;

        TLcdGXYContext context = new TLcdGXYContext();
        for (TLcdCollectedLabelInfo label_to_declutter : aLabelsToDeclutter) {
          ILcdGXYLayer layer = (ILcdGXYLayer) label_to_declutter.getLabeledObject().getLayer();
          if (context.getGXYLayer() != layer) {
            context.resetFor(layer, aGXYView);
          }

          if (all_bounds == null) {
            all_bounds = new Rectangle();
            labelBoundsSFCT(label_to_declutter, graphics, context, all_bounds);
          } else {
            labelBoundsSFCT(label_to_declutter, graphics, context, single_bounds);
            all_bounds = all_bounds.union(single_bounds);
          }
        }
        return all_bounds;
      } catch (TLcdNoBoundsException e) {
        return null;
      } finally {
        if (graphics != null) {
          graphics.dispose();
        }
      }
    } else {
      return null;
    }
  }

  private static void labelBoundsSFCT(TLcdCollectedLabelInfo aLabel, Graphics aGraphics, TLcdGXYContext aContext, Rectangle aBoundsSFCT) throws TLcdNoBoundsException {
    ILcdGXYEditableLabelsLayer labels_layer = (ILcdGXYEditableLabelsLayer) aContext.getGXYLayer();
    ILcdGXYView view = aContext.getGXYView();
    ALcdLabelLocations locations = labels_layer.getLabelLocations();
    TLcdLabelLocation work_location = locations.createLabelLocation();
    locations.getLabelLocationSFCT(aLabel.getLabelIdentifier().getDomainObject(),
                                   aLabel.getLabelIdentifier().getLabelIndex(),
                                   aLabel.getLabelIdentifier().getSubLabelIndex(),
                                   view, work_location);
    ILcdGXYLabelPainter2 label_painter = (ILcdGXYLabelPainter2) aContext.getGXYLayer().getGXYLabelPainter(aLabel.getLabelIdentifier().getDomainObject());
    label_painter.setLabelIndex(aLabel.getLabelIdentifier().getLabelIndex());
    label_painter.setSubLabelIndex(aLabel.getLabelIdentifier().getSubLabelIndex());
    label_painter.setLabelLocation(work_location);
    label_painter.labelBoundsSFCT(aGraphics, ILcdGXYLabelPainter2.DEFAULT | ILcdGXYLabelPainter2.BODY, aContext, aBoundsSFCT);
  }
}
