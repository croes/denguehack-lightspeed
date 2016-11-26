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
package samples.lightspeed.debug;

/**
 * A utility class to keep track of the frame rate of the view. Frame rates are
 * measured in frames per second.
 * <p/>
 * The frame rate is the average frame rate measured over a number of frames.
 *
 */
class FramerateMonitor {
  // Number of samples to use for smoothing the fps counter.
  private final static int FPS_SMOOTH = 20;
  private static final int SAMPLE_COUNT = 500;

  private double fFPS;                  // Sum of framerate samples.
  private int fFPSC;                    // Number of framerate samples taken.
  private long fTime;
  private double fCurrentFrameRate;
  private double fLastFrameTime;

  private float[] fFPSSamples = new float[SAMPLE_COUNT];
  private int fSamplePointer = 0;

  private long fUsedBytes;
  private long fBytesInLastFrame;
  private long fSumUsedBytes;
  private long fMaxUsedBytes;
  private long fMinUsedBytes;
  private double fCurrentAvgUsedBytes;
  private double fCurrentMinUsedBytes;
  private double fCurrentMaxUsedBytes;

  public void init() {
    fCurrentFrameRate = 1.0;
    fFPS = 0.0;
    fFPSC = 0;
    fTime = System.nanoTime();
    fUsedBytes = Runtime.getRuntime().freeMemory();
  }

  /**
   * Call this method exactly one time during each repaint operation.
   */
  public void tick() {
    long t = System.nanoTime();
    fLastFrameTime = (t - fTime) * 1E-6;
    fTime = t;

    // Take framerate sample:
    fFPS += 1000.0f / fLastFrameTime;
    fFPSC++;
    fFPSSamples[fSamplePointer] = 1000.0f / (float) getLastFrameTime();
    fSamplePointer = (fSamplePointer + 1) % fFPSSamples.length;

    // Take memory sample
    long freeBytes = Runtime.getRuntime().freeMemory();
    long totalBytes = Runtime.getRuntime().totalMemory();
    long usedBytes = totalBytes - freeBytes;
    fBytesInLastFrame = Math.max(0, usedBytes - fUsedBytes);
    fSumUsedBytes += fBytesInLastFrame;
    fMinUsedBytes = Math.min(fBytesInLastFrame, fMinUsedBytes);
    fMaxUsedBytes = Math.max(fBytesInLastFrame, fMaxUsedBytes);
    fUsedBytes = usedBytes;

    // If number of samples is high enough, average them and display the result:
    if (fFPSC == FPS_SMOOTH) {
      fFPSC = 0;
      fFPS = fFPS / FPS_SMOOTH;
      fCurrentFrameRate = fFPS;
      fFPS = 0.0f;

      fCurrentAvgUsedBytes = (double) fSumUsedBytes / (double) FPS_SMOOTH;
      fCurrentMinUsedBytes = fMinUsedBytes;
      fCurrentMaxUsedBytes = fMaxUsedBytes;
      fSumUsedBytes = 0;
      fMinUsedBytes = totalBytes;
      fMaxUsedBytes = 0;
    }

  }

  /**
   * Returns the current frame rate.
   *
   * @return The current frame rate in frames per second.
   */
  public double getFrameRate() {
    return fCurrentFrameRate;
  }

  /**
   * Returns the duration of the last repaint. This corresponds to the time
   * between the two last invocations of the <code>tick()</code> method, so may
   * include some time during which the view was waiting for a repaint call.
   *
   * @return The duration of the last repaint in milliseconds.
   */
  public double getLastFrameTime() {
    return fLastFrameTime;
  }

  /**
   * Returns the index of the current sample in {@link #getFPSSamples()}.
   *
   * @return the index in {@code [0, getSampleCount()[}
   */
  public int getSamplePointer() {
    return fSamplePointer;
  }

  /**
   * Returns the current frame rate samples.
   *
   * @return the samples
   */
  public float[] getFPSSamples() {
    return fFPSSamples;
  }

  /**
   * Returns the number of frame rate samples.
   *
   * @return the number of frame rate samples.
   */
  public int getSampleCount() {
    return SAMPLE_COUNT;
  }

  /**
   * Returns the number of bytes allocated per frame.
   *
   * @return the number of bytes
   */
  public double getAverageBytesPerFrame() {
    return fCurrentAvgUsedBytes;
  }

  /**
   * Returns the minimum number of bytes allocated in per frame.
   *
   * @return the number of bytes
   */
  public double getMinimumBytesPerFrame() {
    return fCurrentMinUsedBytes;
  }

  /**
   * Returns the maximum number of bytes allocated in per frame.
   *
   * @return the number of bytes
   */
  public double getMaximumBytesPerFrame() {
    return fCurrentMaxUsedBytes;
  }
}
