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

import java.awt.Color;
import java.awt.Font;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Timer;
import java.util.TimerTask;

import com.luciad.model.TLcdVectorModel;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspOpenGLProfile;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.layer.ALspLayer;
import com.luciad.view.lightspeed.layer.ILspPaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.services.glcache.ALspGLResource;
import com.luciad.view.lightspeed.services.glcache.ILspGLResourceCache;
import com.luciad.view.lightspeed.util.opengl.texturefont.TLspTextureFont;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * A custom layer that draws performance and memory information on top of the view.
 */
public class PerformanceOverlay extends ALspLayer implements ILspPaintableLayer {

  private static final float MAX_FPS = 120;

  private static final Format FLOAT_FORMAT_WITH_DECIMALS = new DecimalFormat("#0.00");
  private static final Format FLOAT_FORMAT_NO_DECIMALS = new DecimalFormat("#0");
  private static final Format INT_FORMAT = new DecimalFormat("###,###,###,###");
  private static final String SOURCE_STRING = "Performance overlay";

  private MyViewListener fFrameTimeListener = new MyViewListener();

  private float fTargetFPS;
  private float fMaxFPS;

  private double fWallClockTime;
  private long fCPUTime;
  private double fCPULoad;
  private Timer fCPUTimer;

  public PerformanceOverlay() {
    super("Performance");
    setModel(new TLcdVectorModel());
    addPaintRepresentation(TLspPaintRepresentation.LABEL);
    fTargetFPS = (float) ALcdAnimationManager.getInstance().getTargetUpdateRate();
    fMaxFPS = Math.min(MAX_FPS, fTargetFPS * 2);

    setSelectable(false);
    setEditable(false);
    setVisible(true);

    fFrameRateMonitor.init();
  }

  private void startCPUTimer() {
    if (fCPUTimer != null) {
      return;
    }

    fCPUTimer = new Timer("CPU load monitor", true);
    fCPUTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        double current_time = System.nanoTime() * 1e-9;
        long cpu = getJVMCpuTime();
        double delta_cpu = (cpu - fCPUTime) * 1e-9;
        double delta_clock = current_time - fWallClockTime;
        fWallClockTime = current_time;
        fCPUTime = cpu;
        fCPULoad = delta_cpu / (delta_clock * Runtime.getRuntime().availableProcessors());
      }
    }, 0, 1000);
  }

  private void stopCPUTimer() {
    if (fCPUTimer == null) {
      return;
    }
    fCPUTimer.cancel();
    fCPUTimer = null;
  }

  public static long getJVMCpuTime() {
    OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
    if (!(bean instanceof com.sun.management.OperatingSystemMXBean)) {
      return 0L;
    }
    return ((com.sun.management.OperatingSystemMXBean) bean).getProcessCpuTime();
  }

  /**
   * View listener that records actual frame render times.
   * This can be different from the frame rate, since the animation
   * manager introduces a sleep when the frame rate is higher than
   * the requested frame rate.
   */
  private class MyViewListener extends ALspViewAdapter {
    private double fAveragedFrameTime = 0;
    private double fAveragedFrameFPS = 0;

    private long fTime0;
    private long fTempTime;
    private long fTime1;

    @Override
    public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
      aGLDrawable.getGL().glFinish();
      fTempTime = System.nanoTime();
    }

    @Override
    public void postRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
      if (fTempTime != 0) {
        aGLDrawable.getGL().glFinish();
        fTime0 = fTempTime;
        fTime1 = System.nanoTime();

        fAveragedFrameTime = 0.1 * getTime() + 0.9 * fAveragedFrameTime;
        fAveragedFrameFPS = 0.1 * getFPS() + 0.9 * fAveragedFrameFPS;
      }
    }

    public double getTime() {
      return (fTime1 - fTime0) / 1000000.0;
    }

    public double getFPS() {
      double time = getTime();
      return time > 0 ? 1000.0 / time : 0;
    }

    public double getAveragedFrameFPS() {
      return fAveragedFrameFPS;
    }
  }

  @Override
  public TLspOpenGLProfile getRequiredOpenGLProfile() {
    return TLspOpenGLProfile.LIGHTSPEED_MINIMUM;
  }

  public TLspPaintProgress paint(ILcdGLDrawable aGLAutoDrawable, TLspPaintPhase aPhase, TLspPaintRepresentationState aPaintRepresentationState, ILspView aView) {
    if (!isVisible(aPaintRepresentationState)) {
      return TLspPaintProgress.COMPLETE;
    }
    if (aPhase.getPaintOpacity() == TLspPaintPhase.PaintOpacity.OPAQUE) {
      return TLspPaintProgress.COMPLETE;
    }
    if (aPhase.getPaintDraping() == TLspPaintPhase.PaintDraping.DRAPING) {
      return TLspPaintProgress.COMPLETE;
    }
    if (aPaintRepresentationState.getPaintState() != TLspPaintState.REGULAR) {
      return TLspPaintProgress.COMPLETE;
    }
    if (aView.getViewXYZWorldTransformation().getOutputDevice() == ALspViewXYZWorldTransformation.OutputDevice.PRINTER) {
      return TLspPaintProgress.COMPLETE;
    }

    // Temporarily restore the previous GL for all the drawing we do here.
    StatisticsGL statisticsGL;
    // Restore the original GL if needed.
    if (aGLAutoDrawable.getGL() instanceof StatisticsGL) {
      statisticsGL = (StatisticsGL) aGLAutoDrawable.getGL();
      aGLAutoDrawable.setGL(statisticsGL.getDelegate());
    } else {
      statisticsGL = new StatisticsGL(aGLAutoDrawable.getGL());
    }

    TLspTextureFont textRenderer = retrieveTextureFont(aView, false);
    TLspTextureFont smallTextRenderer = retrieveTextureFont(aView, true);

    fFrameRateMonitor.tick();

    // Do not count what we do here in the statistics.
    ILcdGL gl = aGLAutoDrawable.getGL();

    gl.glPushAttrib(ILcdGL.GL_ALL_ATTRIB_BITS);
    gl.glPushClientAttrib(0xFFFFFFFF);

    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glOrtho(0, aView.getWidth(), 0, aView.getHeight(), -1, 1);
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glLoadIdentity();

    gl.glDisable(ILcdGL.GL_LIGHTING);
    gl.glDisable(ILcdGL.GL_TEXTURE_2D);
    gl.glDisable(ILcdGL.GL_DEPTH_TEST);

    int horizontal_step = 1;
    float width = (fFrameRateMonitor.getSampleCount() - 1) * horizontal_step / 2;
    float height = 70;
    float x0 = 45f;
    float y0 = 60f;

    gl.glEnable(ILcdGL.GL_BLEND);
    gl.glBlendFunc(ILcdGL.GL_SRC_ALPHA, ILcdGL.GL_ONE_MINUS_SRC_ALPHA);

    gl.glColor4f(0, 0, 0, 0.5f);
    int margin = 0;
    gl.glBegin(ILcdGL.GL_QUADS);
    gl.glVertex2f(x0 - margin, y0 - 4 * margin);
    gl.glVertex2f(x0 + width + margin, y0 - 4 * margin);
    gl.glVertex2f(x0 + width + margin, y0 + height + 3 * margin);
    gl.glVertex2f(x0 - margin, y0 + height + 3 * margin);
    gl.glEnd();

    gl.glLineWidth(1);
    drawGraph(aView, gl, x0, y0, width, height, horizontal_step);

    double fps = fFrameRateMonitor.getFrameRate();
    int vertices = statisticsGL.getVertexCount();
    int triangles = statisticsGL.getTriangleCount();
    double mvps = fps * (double) vertices / 1E6;
    double cpuLoad = 100.0 * fCPULoad;
    int free = (int) (Runtime.getRuntime().freeMemory() / (1024 * 1024));
    int total = (int) (Runtime.getRuntime().totalMemory() / (1024 * 1024));
    double memPerFrame = fFrameRateMonitor.getAverageBytesPerFrame() / (1024.0 * 1024.0);
    double minMemPerFrame = fFrameRateMonitor.getMinimumBytesPerFrame() / (1024.0 * 1024.0);
    double maxMemPerFrame = fFrameRateMonitor.getMaximumBytesPerFrame() / (1024.0 * 1024.0);

    String gpuMemStr1 = null, gpuMemStr2 = null, gpuMemStr3 = null;

    // NVIDIA
    if (TLspOpenGLProfile.isExtensionAvailable(aGLAutoDrawable, "GL_NVX_gpu_memory_info")) {
      int gpuKBAvailable[] = {0};
      gl.glGetIntegerv(0x9049, gpuKBAvailable);
      int gpuKBTotal[] = {0};
      gl.glGetIntegerv(0x9048, gpuKBTotal);
      int gpuMBAvailable = (int) Math.floor((double) gpuKBAvailable[0] / 1024.0);
      int gpuMBTotal = (int) Math.floor((double) gpuKBTotal[0] / 1024.0);
      gpuMemStr1 = (gpuMBTotal - gpuMBAvailable) + " MB used / " + (gpuMBTotal) + " MB VRAM";
    }
    // AMD/ATI
    else if (TLspOpenGLProfile.isExtensionAvailable(aGLAutoDrawable, "GL_ATI_meminfo")) {
      int[] meminfo = new int[4];
      gl.glGetIntegerv(0x87FB, meminfo);
      gpuMemStr1 = "VBO: " + meminfo[0] / 1024 + " MB free VRAM";
      gl.glGetIntegerv(0x87FC, meminfo);
      gpuMemStr2 = "TEX: " + meminfo[0] / 1024 + " MB free VRAM";
      gl.glGetIntegerv(0x87FD, meminfo);
      gpuMemStr3 = "FBO: " + meminfo[0] / 1024 + " MB free VRAM";
    }

    gl.glEnable(ILcdGL.GL_TEXTURE_2D);

    int x = (int) (x0 + width + margin + 20);

    smallTextRenderer.bind(aGLAutoDrawable);

    gl.glColor4f(1, 1, 1, 1);
    smallTextRenderer.drawString(aGLAutoDrawable, x0 - 15, y0 - 10, 0, "0");
    smallTextRenderer.drawString(aGLAutoDrawable, x0 - 22, y0 + height / 2 - 10, 0, Integer.toString((int) fMaxFPS / 2));
    smallTextRenderer.drawString(aGLAutoDrawable, x0 - 29, y0 + height - 10, 0, Integer.toString((int) fMaxFPS));

    smallTextRenderer.unbind(aGLAutoDrawable);

    textRenderer.bind(aGLAutoDrawable);

    textRenderer.drawString(aGLAutoDrawable, x, 110, 0, "Primitives");
    textRenderer.drawString(aGLAutoDrawable, x, 90, 0, FLOAT_FORMAT_WITH_DECIMALS.format(mvps) + " MVerts/sec");
    textRenderer.drawString(aGLAutoDrawable, x, 75, 0, INT_FORMAT.format(vertices) + " vertices");
    textRenderer.drawString(aGLAutoDrawable, x, 60, 0, INT_FORMAT.format(triangles) + " triangles");

    x += 150;

    textRenderer.drawString(aGLAutoDrawable, x, 110, 0, "Performance");
    textRenderer.drawString(aGLAutoDrawable, x, 90, 0, FLOAT_FORMAT_NO_DECIMALS.format(fFrameTimeListener.getAveragedFrameFPS()) + " FPS");
    textRenderer.drawString(aGLAutoDrawable, x, 75, 0, FLOAT_FORMAT_WITH_DECIMALS.format(fps) + " FPS capped");
    textRenderer.drawString(aGLAutoDrawable, x, 60, 0, FLOAT_FORMAT_NO_DECIMALS.format(cpuLoad) + "% CPU load");

    x += 150;

    textRenderer.drawString(aGLAutoDrawable, x, 110, 0, "Memory");
    String memStr = (total - free) + " MB used / " + total + " MB Heap";
    textRenderer.drawString(aGLAutoDrawable, x, 90, 0, memStr);
    String deltaMemStr = FLOAT_FORMAT_WITH_DECIMALS.format(memPerFrame) + " MB Heap / frame (" +
                         FLOAT_FORMAT_WITH_DECIMALS.format(minMemPerFrame) + " - " +
                         FLOAT_FORMAT_WITH_DECIMALS.format(maxMemPerFrame) + " MB)";
    textRenderer.drawString(aGLAutoDrawable, x, 75, 0, deltaMemStr);
    if (gpuMemStr1 != null) {
      textRenderer.drawString(aGLAutoDrawable, x, 60, 0, gpuMemStr1);
    }
    if (gpuMemStr2 != null) {
      textRenderer.drawString(aGLAutoDrawable, x, 45, 0, gpuMemStr2);
    }
    if (gpuMemStr3 != null) {
      textRenderer.drawString(aGLAutoDrawable, x, 30, 0, gpuMemStr3);
    }

    textRenderer.unbind(aGLAutoDrawable);

    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPopMatrix();

    gl.glPopClientAttrib();
    gl.glPopAttrib();

    // Restore statistics GL
    aGLAutoDrawable.setGL(statisticsGL);
    statisticsGL.resetCounters();

    return TLspPaintProgress.COMPLETE;
  }

  private TLspTextureFont retrieveTextureFont(ILspView aView, boolean aSmallFont) {
    ILspGLResourceCache resourceCache = aView.getServices().getGLResourceCache();
    Object[] key = {this, aSmallFont};

    ALspGLResource resource = resourceCache.get(key);

    TLspTextureFont textureFont = null;
    if (resource instanceof TLspTextureFont) {
      textureFont = (TLspTextureFont) resource;
    }

    if (textureFont == null) {
      textureFont = new TLspTextureFont(new Font("Default",
                                                 aSmallFont ? Font.PLAIN : Font.BOLD,
                                                 aSmallFont ? 9 : 12), Color.white, Color.black) {
        @Override
        public String getSourceString() {
          return SOURCE_STRING;
        }
      };
      resourceCache.put(key, textureFont);
    }
    return textureFont;
  }

  private LineRenderer retrieveLineRenderer(ILspView aView, ILcdGL aGL) {
    ILspGLResourceCache resourceCache = aView.getServices().getGLResourceCache();
    Object[] key = {this, LineRenderer.class};

    LineRenderer lineRenderer = (LineRenderer) resourceCache.get(key);

    if (lineRenderer == null) {
      lineRenderer = new LineRenderer(fFrameRateMonitor.getSampleCount(), aGL);
      resourceCache.put(key, lineRenderer);
    }
    return lineRenderer;
  }

  private FramerateMonitor fFrameRateMonitor = new FramerateMonitor();

  private void drawGraph(ILspView aView, ILcdGL aGL, float aX0, float aY0, float aWidth, float aHeight, int aHorizontalStep) {
    aGL.glBegin(ILcdGL.GL_LINES);

    // Draw grid lines every 10 fps
    aGL.glColor3f(1, 1, 1);
    for (int i = 0; i <= fMaxFPS; i += 10) {
      aGL.glVertex2f(aX0, aY0 + i * aHeight / fMaxFPS);
      aGL.glVertex2f(aX0 + aWidth, aY0 + i * aHeight / fMaxFPS);
    }

    // Draw reference line at target FPS
    if (fTargetFPS <= fMaxFPS) {
      aGL.glColor3f(1.0f, 0.0f, 0.0f);
      aGL.glVertex2f(aX0, aY0 + fTargetFPS * aHeight / fMaxFPS);
      aGL.glVertex2f(aX0 + aWidth, aY0 + fTargetFPS * aHeight / fMaxFPS);
    }

    int samplePointer = fFrameRateMonitor.getSamplePointer();
    float[] fpsSamples = fFrameRateMonitor.getFPSSamples();
    // Draw vertical line at current sample
    aGL.glColor3f(0, 0, 1);
    aGL.glVertex2f(aX0 + samplePointer * aHorizontalStep * aWidth / fpsSamples.length, aY0);
    aGL.glVertex2f(aX0 + samplePointer * aHorizontalStep * aWidth / fpsSamples.length, aY0 + aHeight);
    aGL.glEnd();

    // Draw FPS graph
    aGL.glColor3f(0, 1, 0);
    LineRenderer lineRenderer = retrieveLineRenderer(aView, aGL);
    for (int i = 0; i < fpsSamples.length; i++) {
      float fps = Math.min(fpsSamples[i], fMaxFPS);
      lineRenderer.setPoint(i, aX0 + i * aHorizontalStep * aWidth / fpsSamples.length, aY0 + fps * aHeight / fMaxFPS);
    }
    lineRenderer.paint(aGL, fpsSamples.length);
  }

  public ILcdModelXYZWorldTransformation getModelXYZWorldTransformation(ILspView aView) {
    return null;
  }

  public boolean isVisible(TLspPaintRepresentation aPaintRepresentation) {
    return aPaintRepresentation == TLspPaintRepresentation.LABEL;
  }

  @Override
  public void setVisible(TLspPaintRepresentation aPaintRepresentation, boolean aVisible) {

  }

  @Override
  public void registerView(ILspView aView) {
    super.registerView(aView);
    if (isVisible()) {
      aView.addViewListener(fFrameTimeListener);
    }
  }

  @Override
  public void unregisterView(ILspView aView) {
    super.unregisterView(aView);
    aView.removeViewListener(fFrameTimeListener);
  }

  @Override
  public void setVisible(boolean aVisible) {
    super.setVisible(aVisible);
    if (!isVisible()) {
      stopCPUTimer();
      for (ILspView view : getCurrentViews()) {
        view.removeViewListener(fFrameTimeListener);
      }
    } else {
      for (ILspView view : getCurrentViews()) {
        view.addViewListener(fFrameTimeListener);
      }
      startCPUTimer();
    }

  }

  @Override
  public void setVisible(TLspPaintRepresentationState aPaintRepresentationState, boolean aVisible) {
  }

  @Override
  public boolean isVisible(TLspPaintRepresentationState aPaintRepresentationState) {
    return isVisible(aPaintRepresentationState.getPaintRepresentation());
  }

  @Override
  public void setEditable(TLspPaintRepresentation aPaintRepresentation, boolean aEditable) {
  }

  @Override
  public boolean isEditable(TLspPaintRepresentation aPaintRepresentation) {
    return false;
  }

  /**
   * Simple graph line rendering utility.
   */
  private static class LineRenderer extends ALspGLResource {

    private final FloatBuffer fPointBuffer;
    private int fVbo = -1;

    private LineRenderer(int aMaxPointCount, ILcdGL aGl) {
      super(SOURCE_STRING);
      fPointBuffer = FloatBuffer.allocate(aMaxPointCount * 2);
      fPointBuffer.limit(fPointBuffer.capacity());

      int[] intPtr = new int[1];
      aGl.glGenBuffers(1, intPtr);
      fVbo = intPtr[0];
    }

    public void setPoint(int aIndex, float aX, float aY) {
      fPointBuffer.put(aIndex * 2, aX);
      fPointBuffer.put(aIndex * 2 + 1, aY);
    }

    public void paint(ILcdGL aGL, int aPointCount) {
      aGL.glBindBuffer(ILcdGL.GL_ARRAY_BUFFER, fVbo);
      aGL.glBufferData(ILcdGL.GL_ARRAY_BUFFER, fPointBuffer.capacity() * 4, fPointBuffer, ILcdGL.GL_STREAM_DRAW);

      aGL.glPushClientAttrib(ILcdGL.GL_CLIENT_VERTEX_ARRAY_BIT);
      aGL.glInterleavedArrays(ILcdGL.GL_V2F, 0, 0);
      aGL.glDrawArrays(ILcdGL.GL_LINE_STRIP, 0, aPointCount);
      aGL.glPopClientAttrib();

      aGL.glBindBuffer(ILcdGL.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void destroy(ILcdGLDrawable aGLDrawable) {
      super.destroy(aGLDrawable);
      if (fVbo != -1) {
        aGLDrawable.getGL().glDeleteBuffers(1, new int[]{fVbo});
        fVbo = -1;
      }
    }

    @Override
    public long getBytes() {
      return fPointBuffer.capacity() * 4;
    }
  }
}
