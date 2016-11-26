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
package samples.lightspeed.timeview;

import static com.luciad.view.lightspeed.layer.TLspPaintState.REGULAR;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.lightspeed.timeview.constraints.HorizontalNavigationConstraint;
import samples.lightspeed.timeview.constraints.LimitNavigationConstraint;
import samples.lightspeed.timeview.model.TimeAxisModel;
import samples.lightspeed.timeview.model.TimeReference;
import samples.lightspeed.timeview.styling.TimeAxisLabelStyler;
import samples.lightspeed.timeview.styling.TimeAxisStyler;

/**
 * This view shows a time line with labels.
 * <p>
 *   It adds navigation constraints so that you can only pan horizontally,
 *   and not navigate beyond the currently set time range.
 * </p>
 * <p>
 *   It is a non-georeferenced view where X is time and Y is an unspecified number.
 * </p>
 * <p>
 * Notes:
 * <ul>
 *   <li>
 *     Use {@link #getView()} to get the Lightspeed {@link ILspView view} and add layers to it.
 *     The geometry (either in the model, or submitted through a {@link com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider style target provider})
 *     should be in the {@link TimeReference#INSTANCE time reference}.
 *   </li>
 *   <li>Use {@link #setTime} to programmatically move the time line.</li>
 *   <li>Use {@link #getTime()} and {@link #addChangeListener change events} to listen to time line changes.</li>
 *   <li>Use {@link #setValidRange} to set the valid time and Y range.  Navigation will be constrained to that range.</li>
 *   <li>Use {@link #getVisibleRange} to get the time and Y extent currently visible on the view.</li>
 * </ul>
 *
 * </p>
 * <p>
 *   You should use {@link TimeSlider}: it wraps a time view and adds replay controls.
 * </p>
 */
class TimeView implements ILcdChangeSource {

  private final TimeAxisModel fTimeAxisModel = new TimeAxisModel(TimeReference.INSTANCE);
  private final ILspAWTView fView;

  private final HorizontalNavigationConstraint fHorizontalNavigationConstraint;
  private final LimitNavigationConstraint fLimitNavigationConstraint2D;

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();
  private TLcdXYBounds fValidRange = new TLcdXYBounds();

  TimeView() {
    fView = TLspViewBuilder.newBuilder()
                           .viewType(ILspView.ViewType.VIEW_2D)
                           .worldReference(TimeReference.INSTANCE)
                           .background(new JTextField().getBackground())
                           .size(200, 200)
                           .buildAWTView();

    // Time axis/ticks
    fView.addLayer(TLspShapeLayerBuilder.newBuilder()
                                        .model(fTimeAxisModel)
                                        .bodyStyler(REGULAR, new TimeAxisStyler())
                                        .labelStyler(REGULAR, new TimeAxisLabelStyler())
                                        .selectable(false)
                                        .culling(false)
                                        .build());

    TLspViewXYZWorldTransformation2D vwt = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();

    fLimitNavigationConstraint2D = new LimitNavigationConstraint(fView);
    fHorizontalNavigationConstraint = new HorizontalNavigationConstraint();
    vwt.addConstraint(fHorizontalNavigationConstraint);
    vwt.addConstraint(fLimitNavigationConstraint2D);

    vwt.addPropertyChangeListener(new PropertyChangeListener() {
      // Synchronizes the visible bounds with the current time.
      // This implements the "time slider" behavior.
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        fTimeAxisModel.setTime(getTime());
        fChangeSupport.fireChangeEvent(new TLcdChangeEvent(TimeView.this));
      }
    });
  }

  public ILspAWTView getView() {
    return fView;
  }

  @Override
  public void addChangeListener(ILcdChangeListener aChangeListener) {
    fChangeSupport.addChangeListener(aChangeListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aChangeListener) {
    fChangeSupport.removeChangeListener(aChangeListener);
  }

  /**
   * Navigate the view so that the given time is in the center of the view.
   */
  public void setTime(long aCurrentTime) {
    double currentTime = Math.min(Math.max(aCurrentTime, fValidRange.getLocation().getX()), (fValidRange.getLocation().getX() + fValidRange.getWidth()));
    TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();
    ILcd2DEditablePoint newWorldOrigin = v2w.getWorldOrigin().cloneAs2DEditablePoint();
    newWorldOrigin.move2D(currentTime, 0);
    v2w.lookAt(newWorldOrigin, v2w.getViewOrigin(), v2w.getScaleX(), v2w.getScaleY(), v2w.getRotation());
  }

  /**
   * Get the time that is currently in the center of the time line.
   * <p/>
   * Use {@link #addChangeListener change events} to get notified of changes.
   */
  public long getTime() {
    TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();
    return (long) v2w.getWorldOrigin().getX();
  }

  /**
   * Get the time and Y range that is currently visible on the time view.
   * <p/>
   * Use {@link #addChangeListener change events} to get notified of changes.
   */
  public ILcdBounds getVisibleRange() {
    TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();
    return v2w.getFrustum().getBounds();
  }

  /**
   * Set the valid time and Y range.
   * <p/>
   * Navigation will be constrained to these ranges.
   */
  public void setValidRange(long aBeginTime, long aEndTime, double aYMin, double aYMax) {
    fValidRange = new TLcdXYBounds(aBeginTime, aYMin, aEndTime - aBeginTime, aYMax - aYMin);

    fTimeAxisModel.setValidRange(aBeginTime, aEndTime, aYMin, aYMax);
    fLimitNavigationConstraint2D.setAreaOfInterest(fValidRange);

    TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();
    v2w.removeConstraint(fLimitNavigationConstraint2D);
    v2w.removeConstraint(fHorizontalNavigationConstraint);

    ILcd2DEditablePoint worldOrigin = fValidRange.getLocation().cloneAs2DEditablePoint();
    worldOrigin.translate2D(fValidRange.getWidth() / 2, fValidRange.getHeight() / 2);
    Point viewOrigin = new Point(v2w.getWidth() / 2, v2w.getHeight() / 2);
    double scaleX = v2w.getWidth() / (fValidRange.getWidth() * 1.1);
    double scaleY = v2w.getHeight() / fValidRange.getHeight();
    v2w.lookAt(worldOrigin, viewOrigin, scaleX, scaleY, 0);

    v2w.addConstraint(fHorizontalNavigationConstraint);
    v2w.addConstraint(fLimitNavigationConstraint2D);

    fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
  }
}
