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
package samples.lightspeed.demo.application.controller;

import java.awt.AWTEvent;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.gui.ILcdIcon;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.view.ILcdLayered;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * @since 2013.0
 */
public class TouchControllerFilter implements ILspController {
  private final ILspController fDelegate;

  private Map<Long, OneEuroFilter[]> fFilterMap;

  public TouchControllerFilter(ILspController aDelegate) {
    fDelegate = aDelegate;
    fFilterMap = new HashMap<Long, OneEuroFilter[]>();
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aListener) {
    fDelegate.removePropertyChangeListener(aListener);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aListener) {
    fDelegate.addPropertyChangeListener(aListener);
  }

  @Override
  public String getName() {
    return fDelegate.getName();
  }

  @Override
  public String getShortDescription() {
    return fDelegate.getShortDescription();
  }

  @Override
  public ILcdIcon getIcon() {
    return fDelegate.getIcon();
  }

  @Override
  public void startInteraction(ILspView aView) {
    fDelegate.startInteraction(aView);
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    fDelegate.terminateInteraction(aView);
  }

  @Override
  public TLspPaintProgress paint(ILcdGLDrawable aGLDrawable, ILspView aView, TLspPaintPhase aPaintPhase) {
    return fDelegate.paint(aGLDrawable, aView, aPaintPhase);
  }

  @Override
  public ILspController getNextController() {
    return fDelegate.getNextController();
  }

  @Override
  public void appendController(ILspController aEnd) {
    fDelegate.appendController(aEnd);
  }

  @Override
  public ILcdLayered getLayered() {
    return fDelegate.getLayered();
  }

  @Override
  public void handleAWTEvent(AWTEvent aEvent) {
    if (aEvent instanceof TLcdTouchEvent) {

      TLcdTouchEvent touchEvent = (TLcdTouchEvent) aEvent;
      try {
        fDelegate.handleAWTEvent(filterTouchEvent(touchEvent));
      } catch (Exception e) {
      }
    } else {
      fDelegate.handleAWTEvent(aEvent);
    }
  }

  private static final double FREQUENCY = 150;
  private static final double MIN_CUTOFF = 1.5;

  private TLcdTouchEvent filterTouchEvent(TLcdTouchEvent aTouchEvent) throws Exception {
    TLcdTouchPoint touchPoint = aTouchEvent.getModifiedTouchPoint();
    final long id = touchPoint.getID();

    if (touchPoint.getState().equals(TLcdTouchPoint.State.UP)) {
      fFilterMap.remove(id);

      return aTouchEvent;
    } else {

      if (!fFilterMap.containsKey(id)) {
        //We make 2 filters, 1 for x and 1 for y
        fFilterMap.put(id, new OneEuroFilter[]{new OneEuroFilter(FREQUENCY, MIN_CUTOFF), new OneEuroFilter(FREQUENCY)});
      }

      final double timeStamp = aTouchEvent.getTimeStamp() * 0.001;
      final OneEuroFilter[] filters = fFilterMap.get(id);

      double filteredX = filters[0].filter(touchPoint.getLocation().getX(), timeStamp);
      double filteredY = filters[1].filter(touchPoint.getLocation().getY(), timeStamp);

      TLcdTouchPoint newTouchPoint = touchPoint.cloneAs(touchPoint.getState(), id, new Point((int) filteredX, (int) filteredY), touchPoint.getTapCount(), touchPoint.getType(), touchPoint.isConsumed(), touchPoint.getContactAreaWidth(), touchPoint.getContactAreaHeight());
      List<TLcdTouchPoint> touchPoints = new ArrayList<TLcdTouchPoint>(aTouchEvent.getTouchPoints());
      int index = touchPoints.indexOf(touchPoint);
      touchPoints.set(index, newTouchPoint);
      return aTouchEvent.cloneAs(aTouchEvent.getTouchEventID(), aTouchEvent.getSource(), touchPoints, aTouchEvent.getTouchDeviceID(), aTouchEvent.getUserID(), aTouchEvent.getTimeStamp());
    }
  }

  private class OneEuroFilter {
    double freq;
    double mincutoff;
    double beta_;
    double dcutoff;
    LowPassFilter x;
    LowPassFilter dx;
    double lasttime;
    double UndefinedTime = -1;

    double alpha(double cutoff) {
      double te = 1.0 / freq;
      double tau = 1.0 / (2 * Math.PI * cutoff);
      return 1.0 / (1.0 + tau / te);
    }

    void setFrequency(double f) throws Exception {
      if (f <= 0) {
        throw new Exception("freq should be >0");
      }
      freq = f;
    }

    void setMinCutoff(double mc) throws Exception {
      if (mc <= 0) {
        throw new Exception("mincutoff should be >0");
      }
      mincutoff = mc;
    }

    void setBeta(double b) {
      beta_ = b;
    }

    void setDerivateCutoff(double dc) throws Exception {
      if (dc <= 0) {
        throw new Exception("dcutoff should be >0");
      }
      dcutoff = dc;
    }

    public OneEuroFilter(double freq) throws Exception {
      init(freq, 1.0, 0.0, 1.0);
    }

    public OneEuroFilter(double freq, double mincutoff) throws Exception {
      init(freq, mincutoff, 0.0, 1.0);
    }

    public OneEuroFilter(double freq, double mincutoff, double beta_) throws Exception {
      init(freq, mincutoff, beta_, 1.0);
    }

    public OneEuroFilter(double freq, double mincutoff, double beta_, double dcutoff) throws Exception {
      init(freq, mincutoff, beta_, dcutoff);
    }

    private void init(double freq,
                      double mincutoff, double beta_, double dcutoff) throws Exception {
      setFrequency(freq);
      setMinCutoff(mincutoff);
      setBeta(beta_);
      setDerivateCutoff(dcutoff);
      x = new LowPassFilter(alpha(mincutoff));
      dx = new LowPassFilter(alpha(dcutoff));
      lasttime = UndefinedTime;
    }

    double filter(double value) throws Exception {
      return filter(value, UndefinedTime);
    }

    double filter(double value, double timestamp) throws Exception {
      // update the sampling frequency based on timestamps
      if (lasttime != UndefinedTime && timestamp != UndefinedTime) {
        freq = 1.0 / (timestamp - lasttime);
      }

      lasttime = timestamp;
      // estimate the current variation per second
      double dvalue = x.hasLastRawValue() ? (value - x.lastRawValue()) * freq : 0.0; // FIXME: 0.0 or value?
      double edvalue = dx.filterWithAlpha(dvalue, alpha(dcutoff));
      // use it to update the cutoff frequency
      double cutoff = mincutoff + beta_ * Math.abs(edvalue);
      // filter the given value
      return x.filterWithAlpha(value, alpha(cutoff));
    }

    class LowPassFilter {

      double y, a, s;
      boolean initialized;

      void setAlpha(double alpha) throws Exception {
        if (alpha <= 0.0 || alpha > 1.0) {
          throw new Exception("alpha should be in (0.0., 1.0]");
        }
        a = alpha;
      }

      public LowPassFilter(double alpha) throws Exception {
        init(alpha, 0);
      }

      public LowPassFilter(double alpha, double initval) throws Exception {
        init(alpha, initval);
      }

      private void init(double alpha, double initval) throws Exception {
        y = s = initval;
        setAlpha(alpha);
        initialized = false;
      }

      public double filter(double value) {
        double result;
        if (initialized) {
          result = a * value + (1.0 - a) * s;
        } else {
          result = value;
          initialized = true;
        }
        y = value;
        s = result;
        return result;
      }

      public double filterWithAlpha(double value, double alpha) throws Exception {
        setAlpha(alpha);
        return filter(value);
      }

      public boolean hasLastRawValue() {
        return initialized;
      }

      public double lastRawValue() {
        return y;
      }
    }
  }

}

