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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.gstreamer.Bin;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.Structure;
import org.gstreamer.elements.BaseSink;
import org.gstreamer.elements.FakeSink;
import org.gstreamer.lowlevel.GstBinAPI;
import org.gstreamer.lowlevel.GstNative;

public class RGBDataSink extends Bin {
  private static final GstBinAPI sGST = GstNative.load(GstBinAPI.class);
  private BaseSink fVideoSink;
  private boolean fPassDirectBuffer = false;
  private Listener fListener;

  public static interface Listener {
    void rgbFrame(boolean isPrerollFrame, int width, int height, IntBuffer rgb, Buffer buffer);
  }

  public RGBDataSink(String name, Listener listener) {
    super(initializer(sGST.ptr_gst_bin_new(name)));
    this.fListener = listener;
    fVideoSink = (FakeSink) ElementFactory.make("fakesink", "VideoSink");
    fVideoSink.set("signal-handoffs", true);
    fVideoSink.set("sync", true);
    fVideoSink.set("preroll-queue-len", 1);
    fVideoSink.connect((BaseSink.HANDOFF) new VideoHandoffListener());
    fVideoSink.connect((BaseSink.PREROLL_HANDOFF) new VideoHandoffListener());

    Element conv = ElementFactory.make("ffmpegcolorspace", "ColorConverter");
    Element videofilter = ElementFactory.make("capsfilter", "ColorFilter");
    StringBuilder caps = new StringBuilder("video/x-raw-rgb, bpp=32, depth=24, endianness=(int)4321, ");

    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
      caps.append("red_mask=(int)0xFF00, green_mask=(int)0xFF0000, blue_mask=(int)0xFF000000");
    } else {
      caps.append("red_mask=(int)0xFF0000, green_mask=(int)0xFF00, blue_mask=(int)0xFF");
    }
    videofilter.setCaps(new Caps(caps.toString()));
    addMany(conv, videofilter, fVideoSink);
    Element.linkMany(conv, videofilter, fVideoSink);

    addPad(new GhostPad("sink", conv.getStaticPad("sink")));
  }

  public RGBDataSink(String name, Pipeline pipeline, Listener listener) {
    super(initializer(sGST.ptr_gst_bin_new(name)));
    fListener = listener;

    Element element = pipeline.getElementByName("VideoSink");
    if (element != null) {
      // BaseSink which cannot be casted to FakeSink.
      fVideoSink = (BaseSink) element;

      fVideoSink.set("signal-handoffs", true);
      fVideoSink.set("sync", true);
      fVideoSink.set("preroll-queue-len", 1);
      fVideoSink.connect((BaseSink.HANDOFF) new VideoHandoffListener());
      fVideoSink.connect((BaseSink.PREROLL_HANDOFF) new VideoHandoffListener());
    } else {
      fVideoSink = null;
      throw new RuntimeException("Element with name VideoSink not found in the pipeline");
    }
  }

  /**
   * Sets the listener to null. This should be used when disposing
   * the parent object that contains the listener method, to make sure
   * that no dangling references remain to the parent.
   */
  public void removeListener() {
    fListener = null;
  }

  /**
   * Indicate whether the {@link RGBDataSink} should pass the native {@link IntBuffer}
   * to the listener, or should copy it to a heap buffer.  The default is to pass
   * a heap {@link IntBuffer} copy of the data
   *
   * @param passThru If true, pass through the native IntBuffer instead of
   *                 copying it to a heap IntBuffer.
   */
  public void setPassDirectBuffer(boolean passThru) {
    fPassDirectBuffer = passThru;
  }

  /**
   * Gets the actual gstreamer sink element.
   *
   * @return a BaseSink
   */
  public BaseSink getSinkElement() {
    return fVideoSink;
  }

  private class VideoHandoffListener implements BaseSink.HANDOFF, BaseSink.PREROLL_HANDOFF {
    private IntBuffer fBackBuffer;
    private IntBuffer fFrontBuffer;

    public void handoff(BaseSink sink, Buffer buffer, Pad pad) {
      doHandoff(buffer, false);
    }

    public void prerollHandoff(BaseSink sink, Buffer buffer, Pad pad) {
      doHandoff(buffer, true);
    }

    private void doHandoff(Buffer buffer, boolean isPrerollFrame) {
      Caps caps = buffer.getCaps();
      Structure struct = caps.getStructure(0);

      int width = struct.getInteger("width");
      int height = struct.getInteger("height");
      if (width < 1 || height < 1) {
        return;
      }
      if (fPassDirectBuffer) {
        if (fBackBuffer == null || fFrontBuffer == null) {
          fBackBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
          fFrontBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        }

      } else {
        if (fBackBuffer == null || fFrontBuffer == null) {
          fBackBuffer = ByteBuffer.allocate(width * height * 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
          fFrontBuffer = ByteBuffer.allocate(width * height * 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        }
      }

      fBackBuffer.rewind();
      fBackBuffer.put(buffer.getByteBuffer().asIntBuffer());
      IntBuffer temp = fFrontBuffer;
      fFrontBuffer = fBackBuffer;
      fBackBuffer = temp;

      fFrontBuffer.rewind();
      fListener.rgbFrame(isPrerollFrame, width, height, fFrontBuffer, buffer);

      // Dispose of the gstreamer buffer immediately to avoid more being
      // allocated before the java GC kicks in
      buffer.dispose();
    }
  }
}

