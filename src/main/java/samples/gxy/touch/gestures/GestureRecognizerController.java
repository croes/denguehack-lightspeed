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
package samples.gxy.touch.gestures;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.view.gxy.controller.ALcdGXYChainableController;

/**
 * A controller that groups several gesture recognizers. In this sample the controller does not
 * trigger any action, but provides visual feedback whenever a gesture is recognized.
 *
 */
public class GestureRecognizerController extends ALcdGXYChainableController
    implements IGestureRecognizerListener {

  private Vector<IGestureRecognizer> fGestureRecognizers;

  private boolean fVisualFeedback;
  private Timer fVisualFeedbackTimer;
  private IGesture fLastGesture;

  private FlickGestureRecognizer fLeftFlickRecognizer;
  private FlickGestureRecognizer fRightFlickRecognizer;
  private EllipseGestureRecognizer fEllipseGestureRecognizer;
  private ZGestureRecognizer fZGestureRecognizer;

  // The ID of the touch events currently being analyzed.
  private int fTouchEventID;
  private Vector<TLcdTouchEvent> fEventBuffer;

  private enum Status {
    PROCESSING,    // Some recognizers are still processing events (undecided)
    FAILED,        // All recognizers rejected this event chain.
    RECOGNIZED
  }

  ;   // One recognizer has identified a gesture.
  private Status fStatus;

  public GestureRecognizerController() {
    setIcon(new TLcdImageIcon("images/gui/3d/i16_fitclip.gif"));
    fGestureRecognizers = new Vector<IGestureRecognizer>();

    // Recognizer for a flick gesture, going horizontally from left to right.
    fRightFlickRecognizer =
        new FlickGestureRecognizer(1, // minimum segment length, in centimeters
                                   6, // maximum segment length, in centimeters
                                   0.1, // maximum deviation of a point from the straight line
                                   0, // trigonometric direction, in degrees
                                   40, // acceptable direction error, in degrees
                                   400,// maximum duration of the gesture, in milliseconds
                                   2 // two fingers)
        );

    // Recognizer for a flick gesture, going horizontally from right to left.
    fLeftFlickRecognizer =
        new FlickGestureRecognizer(1,   // minimum segment length, in centimeters
                                   6,   // maximum segment length, in centimeters
                                   0.1, // maximum deviation of a point from the straight line
                                   180, // trigonometric direction, in degrees
                                   40,  // acceptable direction error, in degrees
                                   400, // maximum duration of the gesture, in milliseconds
                                   2    // two fingers)
        );

    // Recognizer for an ellipse.
    fEllipseGestureRecognizer = new EllipseGestureRecognizer();

    // Recognizer for a "Z" gesture (3 segment polyline).
    fZGestureRecognizer = new ZGestureRecognizer();

    fGestureRecognizers.add(fLeftFlickRecognizer);
    fGestureRecognizers.add(fRightFlickRecognizer);
    fGestureRecognizers.add(fEllipseGestureRecognizer);
    fGestureRecognizers.add(fZGestureRecognizer);
    fRightFlickRecognizer.addStatusListener(this);
    fLeftFlickRecognizer.addStatusListener(this);
    fEllipseGestureRecognizer.addStatusListener(this);
    fZGestureRecognizer.addStatusListener(this);

    fStatus = Status.FAILED;
    fEventBuffer = new Vector<TLcdTouchEvent>();
    setShortDescription("Recognizes and displays various gestures.");
  }

  public void handleAWTEvent(AWTEvent aEvent) {

    if (aEvent instanceof TLcdTouchEvent) {

      TLcdTouchEvent event = (TLcdTouchEvent) aEvent;
      if (fGestureRecognizers.size() == 0) {
        // If all the recognizers are disabled, pass the event along and don't interfere.
        if (getNextGXYController() != null) {
          getNextGXYController().handleAWTEvent(aEvent);
        }
        return;
      }

      if (fStatus != Status.PROCESSING) {
        List<TLcdTouchPoint> descriptors = event.getTouchPoints();
        if (descriptors.size() == 1 &&
            descriptors.get(0).getTapCount() == 1 &&
            descriptors.get(0).getState() ==
            TLcdTouchPoint.State.DOWN) {
          // This is the beginning of a new chain of touch events.
          fStatus = Status.PROCESSING;
          fTouchEventID = event.getID();

          for (IGestureRecognizer recognizer : fGestureRecognizers) {
            recognizer.startGestureRecognition();
          }
        } else {
          // This event is in the middle of an ongoing event chain. Pass it along.
          if (getNextGXYController() != null) {
            getNextGXYController().handleAWTEvent(aEvent);
          }
          return;
        }
      }
      if (event.getID() != fTouchEventID) {
        // Not the event chain that we are currently tracking.
        if (getNextGXYController() != null) {
          getNextGXYController().handleAWTEvent(aEvent);
        }
        return;
      }

      // Add this touch event to the buffer.
      fEventBuffer.add(event);

      for (IGestureRecognizer recognizer : fGestureRecognizers) {
        recognizer.handleEvent(event);
      }
    }
  }

  private void handleGesture(IGesture aGesture) {
    // Draw something on the screen.    
    fVisualFeedback = true;
    fLastGesture = aGesture;
    getGXYView().repaint();

    // Clean up the screen after one second.
    if (fVisualFeedbackTimer != null) {
      fVisualFeedbackTimer.cancel();
    }
    fVisualFeedbackTimer = new Timer();
    fVisualFeedbackTimer.schedule(new TimerTask() {
      public void run() {
        fVisualFeedback = false;
        fLastGesture = null;
        getGXYView().repaint();
      }
    }, 1000);
  }

  public void gestureRecognizerStatusChanged(IGestureRecognizer aRecognizer) {
    if (aRecognizer.getStatus() == IGestureRecognizer.Status.RECOGNIZED) {
      handleGesture(aRecognizer.getGesture());
      for (IGestureRecognizer recognizer : fGestureRecognizers) {
        recognizer.stopGestureRecognition();
      }
      fStatus = Status.RECOGNIZED;

      // Do not pass along to the next controller the constituent events of this gesture.
      fEventBuffer.clear();
      return;
    }

    // The recognizer reported a failure. See if there are any recognizers still active.
    for (IGestureRecognizer recognizer : fGestureRecognizers) {
      if (recognizer.getStatus() != IGestureRecognizer.Status.FAILED) {
        // At least one recognizer is still processing the current event chain. Do nothing.
        return;
      }
    }

    // At this point we are sure that this chain of events does not describe any gesture.
    // We can now pass all the buffered events to the next controller in the chain.
    if (getNextGXYController() != null) {
      for (TLcdTouchEvent ev : fEventBuffer) {
        getNextGXYController().handleAWTEvent(ev);
      }
    }

    // Cleanup the buffer.
    fEventBuffer.clear();

    // Forward all subsequent events belonging to this chain to the next controller.
    fStatus = Status.FAILED;
  }

  public void paint(Graphics aGraphics) {
    super.paint(aGraphics);
    if (fVisualFeedback) {
      Graphics2D g2 = (Graphics2D) aGraphics;
      Color color = g2.getColor();
      Stroke stroke = g2.getStroke();
      g2.setColor(Color.green);
      g2.setStroke(new BasicStroke(5));

      if (fLastGesture instanceof FlickGesture) {
        paintFlickGesture(aGraphics, (FlickGesture) fLastGesture, true);
      } else if (fLastGesture instanceof ZGesture) {
        paintZGesture(aGraphics, (ZGesture) fLastGesture);
      } else if (fLastGesture instanceof EllipseGesture) {
        paintEllipseGesture(g2, (EllipseGesture) fLastGesture);
      }

      g2.setColor(color);
      g2.setStroke(stroke);
    }
  }

  private void paintFlickGesture(Graphics aGraphics, FlickGesture aFlick, boolean aArrow) {
    double angle = Math.toRadians(aFlick.getDirection());
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    double dx = aFlick.getLength() / 2 * cos;
    double dy = aFlick.getLength() / 2 * sin;

    int end_x = (int) (aFlick.getLocationX() + dx);
    int end_y = (int) (aFlick.getLocationY() + dy);
    aGraphics.drawLine((int) (aFlick.getLocationX() - dx),
                       (int) (aFlick.getLocationY() - dy),
                       end_x, end_y);
    if (!aArrow) {
      return;
    }

    angle = Math.toRadians(aFlick.getDirection() + 135);
    cos = Math.cos(angle);
    sin = Math.sin(angle);
    dx = aFlick.getLength() / 8 * cos;
    dy = aFlick.getLength() / 8 * sin;

    aGraphics.drawLine(end_x, end_y, (int) (end_x + dx), (int) (end_y + dy));

    angle = Math.toRadians(aFlick.getDirection() - 135);
    cos = Math.cos(angle);
    sin = Math.sin(angle);
    dx = aFlick.getLength() / 8 * cos;
    dy = aFlick.getLength() / 8 * sin;

    aGraphics.drawLine(end_x, end_y, (int) (end_x + dx), (int) (end_y + dy));
  }

  private void paintZGesture(Graphics aGraphics, ZGesture aZGesture) {
    double X = aZGesture.getSegmentLocationX(0);
    double Y = aZGesture.getSegmentLocationY(0);
    double angle = Math.toRadians(aZGesture.getDirection(0));
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    double length = aZGesture.getSegmentLength(0);
    double dx = length / 2 * cos;
    double dy = length / 2 * sin;

    double end_x = (int) (X + dx);
    double end_y = (int) (Y + dy);
    aGraphics.drawLine((int) (X - dx), (int) (Y - dy), (int) end_x, (int) end_y);

    for (int i = 1; i < aZGesture.getSegmentCount(); i++) {
      X = end_x;
      Y = end_y;
      angle = Math.toRadians(aZGesture.getDirection(i));
      cos = Math.cos(angle);
      sin = Math.sin(angle);
      length = aZGesture.getSegmentLength(i);
      dx = length * cos;
      dy = length * sin;

      end_x = (int) (X + dx);
      end_y = (int) (Y + dy);
      aGraphics.drawLine((int) X, (int) Y, (int) end_x, (int) end_y);
    }
  }

  private void paintEllipseGesture(Graphics2D aGraphics, EllipseGesture aEllipseGesture) {
    AffineTransform saveAT = aGraphics.getTransform();
    aGraphics.translate(aEllipseGesture.getLocationX(), aEllipseGesture.getLocationY());
    aGraphics.rotate(Math.toRadians(aEllipseGesture.getRotation()));

    double a = aEllipseGesture.getA();
    double b = aEllipseGesture.getB();
    aGraphics.drawArc((int) (-a),
                      (int) (-b),
                      (int) a * 2,
                      (int) b * 2,
                      0, 360);

    aGraphics.setTransform(saveAT);
  }

  /**
   * Activates or deactivates the given gesture recognizer.
   * @param aRecognizer the gesture recognizer
   * @param aEnabled true if the recognizer should be activated, false otherwise
   */
  public void enableRecognizer(IGestureRecognizer aRecognizer, boolean aEnabled) {
    if (aEnabled) {
      if (fGestureRecognizers.contains(aRecognizer)) {
        return;
      }
      fGestureRecognizers.add(aRecognizer);
      aRecognizer.addStatusListener(this);
    } else {
      fGestureRecognizers.remove(aRecognizer);
      aRecognizer.removeStatusListener(this);
    }
  }

  public FlickGestureRecognizer getLeftFlickRecognizer() {
    return fLeftFlickRecognizer;
  }

  public FlickGestureRecognizer getRightFlickRecognizer() {
    return fRightFlickRecognizer;
  }

  public EllipseGestureRecognizer getEllipseGestureRecognizer() {
    return fEllipseGestureRecognizer;
  }

  public ZGestureRecognizer getZGestureRecognizer() {
    return fZGestureRecognizer;
  }

}
