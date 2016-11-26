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
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;

import com.luciad.model.TLcdVectorModel;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
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
 * Layer that displays OpenGL resource usage.
 * <p/>
 * Only use this class for debugging since it has a small
 * performance impact as it wraps the view's {@code ILspGLResourceCache}
 * with an {@code StatisticsGLResourceCacheWrapper} to collect usage statistics.
 */
public class GLResourceOverlay extends ALspLayer implements ILspPaintableLayer {

  private static final Format FLOAT_FORMAT_1 = new DecimalFormat("#0.00");
  private static final Format FLOAT_FORMAT_2 = new DecimalFormat("000.00");

  private double[] fUsageSamples;
  private int fUsagePointer;

  private StatisticsGLResourceCacheListener fListener;

  public GLResourceOverlay() {
    super("GL Resources");
    setModel(new TLcdVectorModel());
    addPaintRepresentation(TLspPaintRepresentation.LABEL);
    fUsageSamples = new double[500];
    fUsagePointer = 0;

    setSelectable(false);
    setEditable(false);
    setVisible(true);
  }

  public TLspPaintProgress paint(ILcdGLDrawable aGLAutoDrawable, TLspPaintPhase aPhase, TLspPaintRepresentationState aPaintRepresentationState, ILspView aView) {
    if (!canPaint(aPhase, aPaintRepresentationState)) {
      return TLspPaintProgress.COMPLETE;
    }
    if (aView.getViewXYZWorldTransformation().getOutputDevice() == ALspViewXYZWorldTransformation.OutputDevice.PRINTER) {
      return TLspPaintProgress.COMPLETE;
    }

    // Temporarily restore the previous GL for all the drawing we do here.
    StatisticsGL statisticsGL = null;

    // Restore the original GL if needed.
    if (aGLAutoDrawable.getGL() instanceof StatisticsGL) {
      statisticsGL = (StatisticsGL) aGLAutoDrawable.getGL();
      aGLAutoDrawable.setGL(statisticsGL.getDelegate());
    }

    ILspGLResourceCache cache = aView.getServices().getGLResourceCache();

    if (fListener == null) {
      fListener = new StatisticsGLResourceCacheListener();
      cache.addCacheListener(fListener);
    }

    double currentUsage = ((double) fListener.getBytesUsedSinceLastReset()) / (1024.0 * 1024.0);
    int currentCount = fListener.getResourcesCountUsedSinceLastReset();
    fUsageSamples[fUsagePointer] = currentUsage;
    fUsagePointer = (fUsagePointer + 1) % fUsageSamples.length;
    fListener.reset();

    TLspTextureFont textRenderer = retrieveTextureFont(aView);
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

    gl.glEnable(ILcdGL.GL_BLEND);
    gl.glBlendFunc(ILcdGL.GL_SRC_ALPHA, ILcdGL.GL_ONE_MINUS_SRC_ALPHA);

    float x0 = 30f;
    float y0 = 82.5f + (hasPerformanceOverlay(aView) ? 95 : 0);

    gl.glEnable(ILcdGL.GL_BLEND);
    gl.glBlendFunc(ILcdGL.GL_SRC_ALPHA, ILcdGL.GL_ONE_MINUS_SRC_ALPHA);

    int margin = 6;
    gl.glLineWidth(1);
    gl.glEnable(ILcdGL.GL_TEXTURE_2D);
    gl.glColor4f(1, 1, 1, 1);

    textRenderer.bind(aGLAutoDrawable);

    double bytes = (double) cache.getBytes() / (1024.0 * 1024.0);
    double bytesLimit = (double) cache.getMaxBytes() / (1024.0 * 1024.0);
    double maxBytesUsed = (double) fListener.getMaxBytesUsed() / (1024.0 * 1024.0);
    int maxCountUsed = fListener.getMaxCountUsed();
    int nbResources = cache.getResourceCount();
    Collection<String> identifiers = cache.getSourceStringsInDecreasingOrder();

    int nonZeroIdentifiersCount = 0;
    for (Object o : identifiers) {
      nonZeroIdentifiersCount += cache.getBytes(o.toString()) == 0 ? 0 : 1;
    }

    int x = (int) (x0) - margin;
    int y = (int) (y0 + 15 * (1 + nonZeroIdentifiersCount));

    String resourcesCached = nbResources + " cached resources:";
    String resourcesCacheMemory = "Resource cache memory: " + FLOAT_FORMAT_1.format(bytes) +
                                  " MB / " + FLOAT_FORMAT_1.format(bytesLimit) + " MB";
    String resourcesUsed = "Resources used/frame: " + currentCount + " (" + FLOAT_FORMAT_1.format(currentUsage) + " MB)";
    String maxResourcesUsed = "Max resources used/frame: " + maxCountUsed + " (" + FLOAT_FORMAT_1.format(maxBytesUsed) + " MB)";

    textRenderer.drawString(aGLAutoDrawable, x, y, 0, resourcesCacheMemory);
    y -= 15;
    textRenderer.drawString(aGLAutoDrawable, x, y, 0, resourcesCached);
    y -= 15;
    x += 10;

    for (Object o : identifiers) {
      double kb = (double) cache.getBytes(o.toString()) / (1024.0 * 1024.0);
      if (kb > 0) {
        resourcesCached = FLOAT_FORMAT_2.format(kb) + " MB: " + o.toString();
        textRenderer.drawString(aGLAutoDrawable, x, y, 0, resourcesCached);
        y -= 15;
      }
    }
    y -= 5;
    x -= 10;

    textRenderer.drawString(aGLAutoDrawable, x, y, 0, resourcesUsed);
    y -= 15;
    textRenderer.drawString(aGLAutoDrawable, x, y, 0, maxResourcesUsed);
    textRenderer.unbind(aGLAutoDrawable);

    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPopMatrix();

    gl.glPopClientAttrib();
    gl.glPopAttrib();

    if (statisticsGL != null) {
      aGLAutoDrawable.setGL(statisticsGL);
    }

    return TLspPaintProgress.COMPLETE;
  }

  private boolean canPaint(TLspPaintPhase aPhase, TLspPaintRepresentationState aPaintRepresentationState) {
    return isVisible(aPaintRepresentationState) &&
           isVisible() &&
           aPhase.getPaintOpacity() != TLspPaintPhase.PaintOpacity.OPAQUE &&
           aPhase.getPaintDraping() != TLspPaintPhase.PaintDraping.DRAPING &&
           aPaintRepresentationState.getPaintState() == TLspPaintState.REGULAR;
  }

  private TLspTextureFont retrieveTextureFont(ILspView aView) {
    ILspGLResourceCache resourceCache = aView.getServices().getGLResourceCache();
    Object[] key = {this};

    ALspGLResource resource = resourceCache.get(key);

    TLspTextureFont textureFont = null;
    if (resource instanceof TLspTextureFont) {
      textureFont = ((TLspTextureFont) resource);
    }

    if (textureFont == null) {
      textureFont = new TLspTextureFont(new Font("Default", Font.BOLD, 12), Color.white, Color.black) {
        @Override
        public String getSourceString() {
          return "GL resource overlay";
        }
      };
      resourceCache.put(key, textureFont);
    }
    return textureFont;
  }

  private boolean hasPerformanceOverlay(ILspView aView) {
    for (int i = 0; i < aView.layerCount(); i++) {
      if (aView.getLayer(i) instanceof PerformanceOverlay) {
        return aView.getLayer(i).isVisible();
      }
    }
    return false;
  }

  public ILcdModelXYZWorldTransformation getModelXYZWorldTransformation(ILspView aView) {
    return null;
  }

  public void setVisible(TLspPaintRepresentationState aPaintRepresentationState, boolean aVisible) {
    // Do nothing
  }

  public boolean isVisible(TLspPaintRepresentation aPaintRepresentation) {
    return aPaintRepresentation == TLspPaintRepresentation.LABEL;
  }

  @Override
  public void setVisible(TLspPaintRepresentation aPaintRepresentation, boolean aVisible) {
    // Do nothing
  }

  @Override
  public boolean isVisible(TLspPaintRepresentationState aPaintRepresentationState) {
    return isVisible(aPaintRepresentationState.getPaintRepresentation());
  }

  @Override
  public void unregisterView(ILspView aView) {
    super.unregisterView(aView);
  }

  @Override
  public void setEditable(TLspPaintRepresentation aPaintRepresentation, boolean aEditable) {
    // Do nothing
  }

  @Override
  public boolean isEditable(TLspPaintRepresentation aPaintRepresentation) {
    return false;
  }

  @Override
  public TLspOpenGLProfile getRequiredOpenGLProfile() {
    return TLspOpenGLProfile.LIGHTSPEED_MINIMUM;
  }

}
