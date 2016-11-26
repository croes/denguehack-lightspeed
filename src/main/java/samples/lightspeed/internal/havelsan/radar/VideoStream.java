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
package samples.lightspeed.internal.havelsan.radar;

import java.io.File;
import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.gstreamer.Buffer;
import org.gstreamer.ClockTime;
import org.gstreamer.Gst;
import org.gstreamer.State;
import org.gstreamer.elements.PlayBin2;

import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;

import samples.lightspeed.demo.framework.util.video.stream.RGBDataSink;

/**
 * @author tomn
 * @since 2012.0
 */
public class VideoStream implements ILcdBounded {

  private PlayBin2 fPlayBin;
  private ClockTime fClockTime;
  private List<Listener> fListeners = new CopyOnWriteArrayList<Listener>();
  private IntBuffer fBuffer;
  private int fWidth;
  private int fHeight;
  private ILcdBounds fBounds;

  public VideoStream(String aFileName, ILcdBounds aBounds) {
    fBounds = aBounds;
    try {
      Gst.init();
    } catch (Error e) {

      int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
      if (bitness == 64) {
        throw new UnsupportedOperationException("GStreamer is not supported on a 64-bit platform (no native libs available).");
      } else {
        throw new IllegalStateException("Could not initialize GStreamer.", e);
      }
    }

    fPlayBin = new PlayBin2("VideoPlayer");
    fPlayBin.setInputFile(new File(aFileName));

    RGBDataSink sink = new RGBDataSink("sink", new RGBDataSink.Listener() {
      @Override
      public void rgbFrame(final boolean aPrerollFrame,
                           final int width, final int height,
                           final IntBuffer aIntBuffer,
                           final Buffer aOriginalBuffer) {
        ClockTime newTime = aOriginalBuffer.getTimestamp();
        if (fClockTime != null && fClockTime.toNanos() == newTime.toNanos()) {
          return;
        }
        fClockTime = newTime;

        fBuffer = aIntBuffer;

        for (Listener l : fListeners) {
          l.frame(width, height, aIntBuffer);
        }
      }
    });
    sink.setPassDirectBuffer(true);
    fPlayBin.setVideoSink(sink);
    fPlayBin.setState(State.PAUSED);
    fPlayBin.seek(0, TimeUnit.NANOSECONDS);
    fPlayBin.getState(-1);
    fPlayBin.setState(State.PAUSED);
    fPlayBin.connect(new PlayBin2.ABOUT_TO_FINISH() {
      @Override
      public void aboutToFinish(PlayBin2 aPlayBin2) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            stop();
            play();
          }
        });
      }
    });

    fWidth = fPlayBin.getVideoSize().width;
    fHeight = fPlayBin.getVideoSize().height;
  }

  public ILcdBounds getBounds() {
    return fBounds;
  }

  public void addVideoStreamListener(Listener aListener) {
    fListeners.add(aListener);
  }

  public void removeVideoStreamListener(Listener aListener) {
    fListeners.remove(aListener);
  }

  public void stop() {
    fPlayBin.seek(0, TimeUnit.NANOSECONDS);
    fPlayBin.setState(State.PAUSED);
  }

  public void play() {
    fPlayBin.setState(State.PLAYING);
  }

  public void pause() {
    fPlayBin.setState(State.PAUSED);
  }

  public IntBuffer getCurrentBuffer() {
    return fBuffer;
  }

  public int getWidth() {
    return fWidth;
  }

  public int getHeight() {
    return fHeight;
  }

  public interface Listener {
    void frame(int aWidth, int aHeight, IntBuffer aData);
  }
}
