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
package samples.lightspeed.demo.framework.util.video.stream;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.gstreamer.Buffer;
import org.gstreamer.ClockTime;
import org.gstreamer.Gst;
import org.gstreamer.State;
import org.gstreamer.elements.PlayBin2;

import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.framework.data.IOUtil;

/**
 * Video stream decoder using GStreamer.
 */
public class GStreamerVideoStream {

  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  private String fPath;
  private int fWidth;
  private int fHeight;
  private float fFrameRate;
  private float fDuration;

  private ClockTime fClockTime;
  private PlayBin2 fPlayBin;
  private IntBuffer fBuffer;
  private long fOffset = 0;

  private static final String GSTREAMER_URL = "http://www.gstreamer.com/";
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(GStreamerVideoStream.class);

  private ArrayList<VideoStreamListener> fListeners = new ArrayList<VideoStreamListener>();
  private List<VirtualCamera> fCameraFiles;

  public enum StreamState {
    STOPPED,
    PLAYING,
    PAUSED
  }

  private StreamState fState;

  public GStreamerVideoStream(String aPath) throws Exception {
    fPath = aPath;
    BufferedReader reader = IOUtil.createReader(aPath, "meta.txt");
    try {
      fWidth = Integer.parseInt(reader.readLine());
      fHeight = Integer.parseInt(reader.readLine());
      fDuration = Float.parseFloat(reader.readLine());
      fFrameRate = Integer.parseInt(reader.readLine());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    loadCameraFiles();

    try {
      Gst.init();
    } catch (Error e) {
      sLogger.error("Could not initialize the GStreamer library. " +
                    "To install GStreamer, please refer to " + GSTREAMER_URL);
      throw new IllegalStateException("Could not initialize GStreamer.", e);
    }

    fPlayBin = new PlayBin2("VideoPlayer");
    fPlayBin.setInputFile(IOUtil.getFile(fPath, "movie.mpg"));

    try {
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

          VirtualCamera camera = getCurrentCamera();
          fBuffer = aIntBuffer;

          for (VideoStreamListener l : fListeners) {
            l.frame(camera, width, height, aIntBuffer);
          }
        }
      });
      sink.setPassDirectBuffer(true);
      fPlayBin.setVideoSink(sink);
      fPlayBin.setState(State.PAUSED);
      fPlayBin.seek(0, TimeUnit.NANOSECONDS);
      fPlayBin.getState(-1);
      fPlayBin.setState(State.PAUSED);
      fOffset = fPlayBin.queryPosition().toNanos();
    } catch (Error e) {
      sLogger.error("Could not initialize the GStreamer video stream. " +
                    "The GStreamer library might not be installed correctly. " +
                    "To install GStreamer, please refer to " + GSTREAMER_URL);
      throw new IllegalStateException("Could not initialize GStreamer video stream.", e);
    }
  }

  private void loadCameraFiles() {
    try {
      fCameraFiles = getCameras();
    } catch (IOException e) {
      sLogger.warn("Failed to load UAV camera files" +
                   " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }

  public int getWidth() {
    return fWidth;
  }

  public int getHeight() {
    return fHeight;
  }

  public float getFrameRate() {
    return fFrameRate;
  }

  public float getDuration() {
    return fDuration;
  }

  public int getFrameCount() {
    return (int) (getFrameRate() * getDuration());
  }

  public boolean isPlaying() {
    return fPlayBin.isPlaying();
  }

  public StreamState getState() {
    return fState;
  }

  private void setState(StreamState aState) {
    StreamState oldState = fState;
    fState = aState;
    fPropertyChangeSupport.firePropertyChange("state", oldState, fState);
  }

  private int mapToIndex(float aTime) {
    int result = (int) (aTime / fDuration * (fFrameRate * fDuration - 1));
    if (result < 0) {
      result = 0;
    }
    if (result > fFrameRate * fDuration - 1) {
      result = (int) (fFrameRate * fDuration - 1);
    }
    return result;
  }

  public IntBuffer getCurrentBuffer() {
    return fBuffer;
  }

  public int getCurrentFrame() {
    long nanos = fClockTime != null ? fClockTime.toNanos() : 0;
    if (nanos > 0) {
      nanos -= fOffset;
    }
    float v = getPlayBinDuration();
    return mapToIndex((float) nanos / v * getDuration());
  }

  private float getPlayBinDuration() {
    float v = (float) fPlayBin.queryDuration(TimeUnit.NANOSECONDS);
    if (v == 0) {
      v = (float) (getDuration() * 1e9);
    }
    return v;
  }

  public VirtualCamera getCurrentCamera() {
    int index = getCurrentFrame();
    if (index < 0 || index >= fCameraFiles.size()) {
      return null;
    }
    return fCameraFiles.get(index);
  }

  private List<VirtualCamera> getCameras() throws IOException {
    ArrayList<VirtualCamera> result = new ArrayList<VirtualCamera>();

    BufferedReader in = IOUtil.createReader(fPath, "movieref.txt");
    String line = in.readLine();
    while (line != null) {
      StringTokenizer tokenizer = new StringTokenizer(line);
      FloatBuffer textureMatrix = FloatBuffer.allocate(16);
      while (tokenizer.hasMoreTokens()) {
        textureMatrix.put(Float.parseFloat(tokenizer.nextToken()));
      }
      TLcdXYZPoint eye = new TLcdXYZPoint();
      readPoint3D(in, eye);
      TLcdXYZPoint ref = new TLcdXYZPoint();
      readPoint3D(in, ref);
      TLcdXYZPoint up = new TLcdXYZPoint();
      readPoint3D(in, up);
      TLcdXYPoint nearAndFar = new TLcdXYPoint();
      readPoint2D(in, nearAndFar);
      double fov = readDouble(in);
      double aspect = readDouble(in);
      TLcdXYZPoint yawPitchRoll = new TLcdXYZPoint();
      readPoint3D(in, yawPitchRoll);

      VirtualCamera camera = new VirtualCamera(
          textureMatrix,
          eye, ref, up,
          nearAndFar.getX(), nearAndFar.getY(),
          fov,
          aspect,
          yawPitchRoll.getX(), yawPitchRoll.getY(), yawPitchRoll.getZ());

      result.add(camera);
      line = in.readLine();
    }
    return result;
  }

  // Reads a line and parses it as 3 doubles.
  private static void readPoint3D(BufferedReader aReader, TLcdXYZPoint aPointSFCT) throws IOException {
    String line = aReader.readLine();
    if (line == null) {
      throw new IOException(aReader + " does not contain enough data.");
    }
    String[] coordinates = line.split(" ");
    aPointSFCT.move3D(
        Double.parseDouble(coordinates[0]),
        Double.parseDouble(coordinates[1]),
        Double.parseDouble(coordinates[2])
    );
  }

  // Reads a line and parses it as 2 doubles.
  private static void readPoint2D(BufferedReader aReader, TLcdXYPoint aPointSFCT) throws IOException {
    String line = aReader.readLine();
    if (line == null) {
      throw new IOException(aReader + " does not contain enough data.");
    }
    String[] coordinates = line.split(" ");
    aPointSFCT.move2D(
        Double.parseDouble(coordinates[0]),
        Double.parseDouble(coordinates[1])
    );
  }

  // Reads a line and parses it as 1 double.
  private static double readDouble(BufferedReader aReader) throws IOException {
    String line = aReader.readLine();
    if (line == null) {
      throw new IOException(aReader + " does not contain enough data.");
    }
    return Double.parseDouble(line);
  }

  public void stop() {
    fPlayBin.seek(0, TimeUnit.NANOSECONDS);
    fPlayBin.setState(State.PAUSED);
    setState(StreamState.STOPPED);
  }

  public void play() {
    fPlayBin.setState(State.PLAYING);
    setState(StreamState.PLAYING);
  }

  public void pause() {
    fPlayBin.setState(State.PAUSED);
    setState(StreamState.PAUSED);
  }

  public void seek(int aFrame) {
    if (aFrame == getCurrentFrame()) {
      return;
    }
    float fraction = (float) aFrame / (getDuration() * getFrameRate() - 1);
    long position = fOffset + (long) (fraction * getPlayBinDuration());
    fPlayBin.seek(position, TimeUnit.NANOSECONDS);
  }

  public void addVideoStreamListener(VideoStreamListener aListener) {
    addVideoStreamListener(aListener, false);
  }

  public void addVideoStreamListener(VideoStreamListener aListener, boolean aReplayLastFrame) {
    fListeners.add(aListener);
    if (aReplayLastFrame) {
      aListener.frame(
          getCurrentCamera(),
          getWidth(),
          getHeight(),
          getCurrentBuffer()
      );
    }
  }

  public void removeVideoStreamListener(VideoStreamListener aListener) {
    fListeners.remove(aListener);
  }

  /**
   * Listener that receives updates when a video stream is played back.
   */
  public static interface VideoStreamListener {

    /**
     * Called when a new frame is available.
     *
     * @param aCamera video camera parameters
     * @param aWidth  width of the frame in pixels
     * @param aHeight height of the frame in pixels
     * @param aImage  image data
     */
    public void frame(VirtualCamera aCamera, int aWidth, int aHeight, IntBuffer aImage);
  }

}
