package com.luciad.dengue.timeview;

import com.luciad.dengue.timeview.constraints.HorizontalPanningConstraint;
import com.luciad.dengue.timeview.constraints.LimitNavigationConstraint;
import com.luciad.dengue.timeview.model.TimeAxisModel;
import com.luciad.dengue.timeview.model.TimeHeightReference;
import com.luciad.dengue.timeview.styling.CurrentTimeStyler;
import com.luciad.dengue.timeview.styling.TimeTickLabelStyler;
import com.luciad.dengue.timeview.styling.TimeTickStyler;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.util.*;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.luciad.view.lightspeed.layer.TLspPaintState.REGULAR;

/**
 * Creates a non-georeferenced view where X is time and Y is height.
 * It adds navigation constraints so that you can only pan horizontally, and not navigate beyond the currently set time range.
 */
public class TimeView implements ILcdChangeSource {

  private long intervalStart;
  private long intervalEnd;

  private final TimeHeightReference fTimeHeightReference = new TimeHeightReference();
  private final TimeAxisModel fTimeAxisModel = new TimeAxisModel(fTimeHeightReference);
  private final ILspAWTView fView;
  private final LimitNavigationConstraint fLimitNavigationConstraint2D;

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();

  public TimeView() {
    fView = TLspViewBuilder.newBuilder()
                           .viewType(ILspView.ViewType.VIEW_2D)
                           .worldReference(fTimeHeightReference)
                           .background(new JTextField().getBackground())
                           .size(20000, 200)
                           .buildAWTView();

    // Current time indicator
    fView.addLayer(TLspShapeLayerBuilder.newBuilder()
                                        .model(fTimeAxisModel.getCurrentTimeModel())
                                        .bodyStyler(REGULAR, new CurrentTimeStyler())
                                        .selectable(false)
                                        .culling(false)
                                        .build());

    // Time axis/ticks
    fView.addLayer(TLspShapeLayerBuilder.newBuilder()
                                        .model(fTimeAxisModel.getTimeTicksModel())
                                        .bodyStyler(REGULAR, new TimeTickStyler())
                                        .labelStyler(REGULAR, new TimeTickLabelStyler())
                                        .selectable(false)
                                        .culling(false)
                                        .build());

    TLspViewXYZWorldTransformation2D vwt = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();

    fLimitNavigationConstraint2D = new LimitNavigationConstraint(fView);
    vwt.addConstraint(new HorizontalPanningConstraint());
    vwt.addConstraint(fLimitNavigationConstraint2D);

    vwt.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        TLspViewXYZWorldTransformation2D transf = (TLspViewXYZWorldTransformation2D) evt.getSource();
        ILcdBounds bounds = transf.getFrustum().getBounds();
        long time = (long) (bounds.getLocation().getX() + (bounds.getWidth() / 2.0));
        intervalStart = (long) bounds.getLocation().getX();
        intervalEnd = (long) bounds.getLocation().getX() + (long) bounds.getWidth();
        fTimeAxisModel.setTime(time);
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

  public void setTime(double aTime) {
    TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();
    ILcd2DEditablePoint newWorldOrigin = v2w.getWorldOrigin().cloneAs2DEditablePoint();
    newWorldOrigin.move2D(aTime, 0);
    v2w.lookAt(newWorldOrigin, v2w.getViewOrigin(), v2w.getScaleX(), v2w.getScaleY(), v2w.getRotation());
  }

  public long getTime() {
    return fTimeAxisModel.getTime();
  }

  public long getIntervalStart() {
    return intervalStart;
  }

  public long getIntervalEnd() {
    return intervalEnd;
  }

  public void updateTimeRange(long aBeginTime, long aEndTime) {
    intervalStart = aBeginTime;
    intervalEnd = aEndTime;

    fTimeAxisModel.setRange(aBeginTime, aEndTime);
    fTimeHeightReference.updateBounds(aBeginTime, aEndTime);
    fLimitNavigationConstraint2D.setAreaOfInterest(fTimeHeightReference.get2DEditableBounds());

    fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));

    try {
      new TLspViewNavigationUtil(fView).fit(fTimeHeightReference.get2DEditableBounds(), null);
    } catch (TLcdOutOfBoundsException e) {
      throw new IllegalStateException(e);
    }

    setTime(aBeginTime);
  }

  public TimeHeightReference getTimeHeightReference() {
    return fTimeHeightReference;
  }
}
