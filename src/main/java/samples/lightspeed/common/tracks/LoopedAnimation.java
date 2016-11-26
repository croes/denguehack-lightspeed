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
package samples.lightspeed.common.tracks;

import com.luciad.view.animation.ILcdAnimation;

/**
 * A wrapper that converts any existing {@link ILcdAnimation} into a loop. The
 * looping behaviour can be chosen from three possible {@link LoopMode
 * modes}:
 * <ul>
 * <li>Forward: the animation plays from start to finish, then repeats.</li>
 * <li>Backward: the animation plays backwards from finish to start, then repeats in the same
 * direction.</li>
 * <li>Pingpong: the animation first plays from start to finish, then backward from finish to
 * start.</li>
 * </ul>
 *
 * @req LSPR-82
 * @req LSPR-84
 * @req LSPR-85
 * @since 2012.0
 */
public class LoopedAnimation implements ILcdAnimation {

  private ILcdAnimation fLooped;
  private LoopMode fMode;

  /**
   * Used by {@link LoopedAnimation} to indicate how an animation should be looped.
   */
  public static enum LoopMode {
    /**
     * The animation plays from start to finish, then repeats.
     */
    FORWARD,
    /**
     * The animation plays backwards from finish to start, then repeats in the same direction.
     */
    BACKWARD,
    /**
     * The animation first plays from start to finish, then backward from finish to start.
     */
    PINGPONG
  }

  /**
   * Creates a new loop from the given animation and with the specified mode.
   *
   * @param aLooped the animation to loop
   * @param aMode   the looping mode to use
   */
  public LoopedAnimation(ILcdAnimation aLooped, LoopMode aMode) {
    fLooped = aLooped;
    fMode = aMode;
  }

  /**
   * Returns the duration of the looped animation in seconds.
   *
   * @return the duration of the looped animation in seconds
   */
  public double getDuration() {
    return fLooped.getDuration() * (fMode == LoopMode.PINGPONG ? 2 : 1);
  }

  /**
   * Invokes the {@code start()} method of the looped animation.
   */
  public void start() {
    fLooped.start();
  }

  /**
   * Invokes the {@code stop()} method of the looped animation.
   */
  public void stop() {
    fLooped.stop();
  }

  @Override
  public void restart() {
    fLooped.restart();
  }

  /**
   * Always returns true.
   *
   * @return {@code true}
   */
  public boolean isLoop() {
    return true;
  }

  /**
   * Invokes the {@code setTime()} method of the looped animation, with a time
   * value determined in function of the looping mode.
   *
   * @param aTime the duration (in seconds) for which the animation has been running
   */
  public void setTime(double aTime) {
    switch (fMode) {
    case FORWARD:
      fLooped.setTime(aTime);
      break;
    case BACKWARD:
      fLooped.setTime(getDuration() - aTime);
      break;
    case PINGPONG:
      if (aTime <= fLooped.getDuration()) {
        fLooped.setTime(aTime);
      } else {
        fLooped.setTime(2 * fLooped.getDuration() - aTime);
      }
      break;
    }
  }

  /**
   * Returns the looping mode of this animation.
   *
   * @return the looping mode of this animation
   */
  public LoopMode getLoopingMode() {
    return fMode;
  }
}
