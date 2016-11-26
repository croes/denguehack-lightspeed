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

import com.luciad.input.touch.TLcdTouchEvent;

/**
 * An interface describing a generic gesture recognizer.
 */
public interface IGestureRecognizer {

  public enum Status {
    PROCESSING,    // The chain of events received so far is not recognized but also not rejected.
    FAILED,        // The chain of events does not match the gesture model.
    RECOGNIZED
  }

  ;  // The last chain of events has been recognized as a valid gesture.

  /**
   * Initializes the gesture recognizer.
   */
  public void startGestureRecognition();

  /**
   * Process a new touch event.
   * @param aTouchEvent
   */
  public void handleEvent(TLcdTouchEvent aTouchEvent);

  /**
   * Abandons gesture recognition for the current event chain. This is useful when it has become
   * obvious that the current event chain cannot be interpreted as the gesture, or when another
   * recognizer has already identified this event chain as a gesture.
   */
  public void stopGestureRecognition();

  /**
   * Returns the status of this recognizer.
   * @return PROCESSING, FAILED, or RECOGNIZED.
   */
  public Status getStatus();

  /**
   * Returns the gesture that has been recognized.
   * @return the last recognized gesture, or null if the state is not RECOGNIZED.
   */
  public IGesture getGesture();

  /**
   * Adds a listener that should be notified about status changes.
   * @param aListener a listener that should be notified about status changes.
   */
  public void addStatusListener(IGestureRecognizerListener aListener);

  /**
   * Removes a status change listener.
   * @param aListener the listener to be removed.
   */
  public void removeStatusListener(IGestureRecognizerListener aListener);
}
