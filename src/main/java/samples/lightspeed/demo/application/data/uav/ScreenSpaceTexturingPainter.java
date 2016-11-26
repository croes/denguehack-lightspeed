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
package samples.lightspeed.demo.application.data.uav;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.collections.TLcdIdentityHashSet;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspOpenGLProfile;
import com.luciad.view.lightspeed.TLspPaintPass;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.layer.ALspViewBoundsInfo;
import com.luciad.view.lightspeed.layer.ALspViewTouchInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.paintgroup.TLspPaintGroup;
import com.luciad.view.lightspeed.layer.paintgroup.TLspPaintGroupsChangedEvent;
import com.luciad.view.lightspeed.painter.ILspEditableStyledPainter;
import com.luciad.view.lightspeed.query.ALspPaintQuery;
import com.luciad.view.lightspeed.query.TLspIsTouchedQuery;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsBoundsQuery;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsQuery;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsTouchQuery;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyleChangeListener;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyleChangeEvent;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;

/**
 * Paints textured quads for {@link ScreenSpaceBounds} objects.
 */
class ScreenSpaceTexturingPainter implements ILspEditableStyledPainter, ILspStyleChangeListener {

  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  private Map<ILspInteractivePaintableLayer, TLcdIdentityHashSet<TLspPaintRepresentation>> fLayers = new TLcdWeakIdentityHashMap<ILspInteractivePaintableLayer, TLcdIdentityHashSet<TLspPaintRepresentation>>();

  private GStreamerVideoStream fVideoStream;
  private int fWidth;
  private int fHeight;
  private IntBuffer fIntBuffer;

  private final TLcdXYBounds fTempBounds = new TLcdXYBounds();

  private Map<TLspPaintState, ILspStyler> fStyleMap;

  public ScreenSpaceTexturingPainter() {
    fStyleMap = new HashMap<TLspPaintState, ILspStyler>();
  }

  public void setVideoStream(GStreamerVideoStream aVideoStream) {
    fVideoStream = aVideoStream;
  }

  private ViewDependentState getViewDependentState(ILspView aView) {
    Object[] keys = new Object[]{aView, this};
    ViewDependentState state = (ViewDependentState) aView.getServices().getMultiKeyCache().get(keys);
    if (state == null) {
      state = new ViewDependentState();
      aView.getServices().getMultiKeyCache().put(keys, state);
    }
    return state;
  }

  public void setVideoStreamDirty(int aWidth, int aHeight, IntBuffer aIntBuffer) {
    for (ILspLayer layer : getCurrentLayers()) {
      for (ILspView view : layer.getCurrentViews()) {
        ViewDependentState state = getViewDependentState(view);
        state.fVideoStreamDirty = true;
      }
    }
    fIntBuffer = aIntBuffer;
    fWidth = aWidth;
    fHeight = aHeight;
  }

  private void setUpTexturing(ILcdGLDrawable aGLDrawable, ILspView aView) {
    ViewDependentState state = getViewDependentState(aView);

    if (state.fVideoStreamDirty && fVideoStream != null) {
      IntBuffer image = fIntBuffer;
      if (image == null) {
        return;
      }

      if (state.fTexture == null) {
        state.fTexture = new IntBufferTextureObject(fWidth, fHeight);
      }
      state.fTexture.setBuffer(fIntBuffer);
      state.fVideoStreamDirty = false;
    }

    ILcdGL gl = aGLDrawable.getGL();
    if (state.fTexture != null) {
      gl.glActiveTexture(ILcdGL.GL_TEXTURE0);
      gl.glEnable(ILcdGL.GL_TEXTURE_2D);
      state.fTexture.bind(aGLDrawable);
    }
  }

  private void cleanUpTexturing(ILcdGLDrawable aGLDrawable, ILspView aView) {
    ViewDependentState state = getViewDependentState(aView);
    ILcdGL gl = aGLDrawable.getGL();
    if (state.fTexture != null) {
      gl.glActiveTexture(ILcdGL.GL_TEXTURE0);
      gl.glEnable(ILcdGL.GL_TEXTURE_2D);
      state.fTexture.unbind(aGLDrawable);
    }
  }

  @Override
  public TLspPaintProgress paintObjects(ILcdGLDrawable aGLDrawable, List<TLspPaintGroup> aPaintGroups, TLspPaintPass aPass, TLspContext aContext) {
    if (getStyler(aPass.getPaintState()) == null) {
      return TLspPaintProgress.COMPLETE;
    }
    if (aPass.getPaintDraping() == TLspPaintPhase.PaintDraping.DRAPING) {
      return TLspPaintProgress.COMPLETE;
    }
    if (aPass.getPaintOpacity() == TLspPaintPhase.PaintOpacity.TRANSPARENT) {
      return TLspPaintProgress.COMPLETE;
    }

    setUpTexturing(aGLDrawable, aContext.getView());
    try {
      ILspStyler styler = getStyler(aPass.getPaintState());
      List<ALspStyle> styles = new ArrayList<ALspStyle>();

      for (TLspPaintGroup paintGroup : aPaintGroups) {
        MyStyleCollector collector = new MyStyleCollector(paintGroup.getObjects());
        styler.style(paintGroup.getObjects(), collector, aContext);

        for (Object o : paintGroup.getObjects()) {
          styles.clear();

          // Since the object is the geometry itself (bounds), we query
          // the styles from the collector's style map with key NULL
          styles.addAll(collector.getStyles());

          paintQuad(aGLDrawable, o, styles, aContext);
        }
      }

      return TLspPaintProgress.COMPLETE;
    } finally {
      cleanUpTexturing(aGLDrawable, aContext.getView());
    }
  }

  private TLspLineStyle getFirstLineStyle(List<ALspStyle> aCompositeStyle) {
    if (aCompositeStyle == null) {
      return null;
    }
    for (ALspStyle s : aCompositeStyle) {
      if (s instanceof TLspLineStyle) {
        return (TLspLineStyle) s;
      }
    }
    return null;
  }

  private void paintQuad(ILcdGLDrawable aGLDrawable, Object o, List<ALspStyle> aStyle, TLspContext aContext) {
    if (aContext.getViewXYZWorldTransformation().getOutputDevice() == ALspViewXYZWorldTransformation.OutputDevice.PRINTER) {
      return; // do not print screen overlays
    }
    if (aStyle == null) {
      return;
    }
    if (!getAbsoluteBounds(o, aContext, fTempBounds)) {
      return;
    }
    ILcdBounds b = fTempBounds;

    TLspLineStyle lineStyle = getFirstLineStyle(aStyle);

    double x = b.getLocation().getX();
    double y = b.getLocation().getY();
    double w = b.getWidth();
    double h = b.getHeight();

    ILcdGL gl = aGLDrawable.getGL();
    gl.glPushAttrib(ILcdGL.GL_ALL_ATTRIB_BITS);
    gl.glDisable(ILcdGL.GL_DEPTH_TEST);
    ScreenSpaceUtil.beginOrthoRendering(aGLDrawable);
    gl.glBegin(ILcdGL.GL_QUADS);
    gl.glTexCoord2i(0, 0);
    gl.glVertex2d(x, y);
    gl.glTexCoord2i(0, 1);
    gl.glVertex2d(x, y + h);

    gl.glTexCoord2i(1, 1);
    gl.glVertex2d(x + w, y + h);
    gl.glTexCoord2i(1, 0);
    gl.glVertex2d(x + w, y);
    gl.glEnd();

    gl.glDisable(ILcdGL.GL_TEXTURE_2D);
    if (lineStyle != null) {
      gl.glLineWidth((float) lineStyle.getWidth());

      Color color = lineStyle.getColor();
      float[] comps = new float[4];
      color.getComponents(comps);
      gl.glColor4fv(comps);
      gl.glBegin(ILcdGL.GL_LINE_LOOP);
      gl.glVertex2d(x, y);
      gl.glVertex2d(x, y + h);
      gl.glVertex2d(x + w, y + h);
      gl.glVertex2d(x + w, y);
      gl.glEnd();

    }
    ScreenSpaceUtil.endOrthoRendering(aGLDrawable);
    gl.glEnable(ILcdGL.GL_DEPTH_TEST);
    gl.glPopAttrib();
  }

  @Override
  public TLspOpenGLProfile getRequiredOpenGLProfile() {
    return TLspOpenGLProfile.LIGHTSPEED_MINIMUM;
  }

  private boolean getAbsoluteBounds(Object aObject, TLspContext aContext, ILcd2DEditableBounds aResultSFCT) {
    if (aObject instanceof ScreenSpaceBounds) {
      ScreenSpaceBounds screenSpaceBounds = (ScreenSpaceBounds) aObject;
      screenSpaceBounds.retrieveAbsoluteBoundsSFCT(
          aContext.getView().getWidth(),
          aContext.getView().getHeight(),
          aResultSFCT
      );
      return true;
    }
    return false;
  }

  @Override
  public void styleChanged(TLspStyleChangeEvent aEvent) {
  }

  @Override
  public void prepareChanges(List<TLspPaintGroupsChangedEvent> aEvents, Object aEventId, Callback aCallback) {
    if (aCallback != null) {
      aCallback.changesReady(aEventId);
    }
  }

  @Override
  public void commitChanges(Object aEventId) {
  }

  private static class ViewDependentState {
    IntBufferTextureObject fTexture;
    boolean fVideoStreamDirty = true;
  }

  private static class MyStyleCollector extends ALspStyleCollector {

    public MyStyleCollector(Collection<? extends Object> aObjectsToStyle) {
      super(aObjectsToStyle);
    }

    private ArrayList<ALspStyle> fStyles = new ArrayList<ALspStyle>();

    @Override
    protected void submitImpl() {
      fStyles.clear();
      fStyles.addAll(super.getStyles());
    }

    @Override
    public List<ALspStyle> getStyles() {
      return fStyles;
    }
  }

  @Override
  public void registerLayer(ILspInteractivePaintableLayer aLayer, TLspPaintRepresentation aPaintRepresentation) {
    TLcdIdentityHashSet<TLspPaintRepresentation> paintRepresentations = fLayers.get(aLayer);
    if (paintRepresentations == null) {
      paintRepresentations = new TLcdIdentityHashSet<TLspPaintRepresentation>();
      fLayers.put(aLayer, paintRepresentations);
    }
    paintRepresentations.add(aPaintRepresentation);
  }

  @Override
  public void unregisterLayer(ILspInteractivePaintableLayer aLayer, TLspPaintRepresentation aPaintRepresentation) {
    TLcdIdentityHashSet<TLspPaintRepresentation> paintRepresentations = fLayers.get(aLayer);
    if (paintRepresentations != null) {
      paintRepresentations.remove(aPaintRepresentation);
      if (paintRepresentations.isEmpty()) {
        fLayers.remove(aLayer);
      }
    }
  }

  private Collection<ILspInteractivePaintableLayer> getCurrentLayers() {
    return fLayers.keySet();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aPropertyChangeListener);

  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aPropertyChangeListener);
  }

  private ILcdShape getShape(Object aObject) {
    if (aObject instanceof ILcdShape) {
      return (ILcdShape) aObject;
    }
    if (aObject instanceof ILcdBounded) {
      return ((ILcdBounded) aObject).getBounds();
    }
    return null;
  }

  @Override
  public ILspStyler getStyler(TLspPaintState aPaintState) {
    ILspStyler styler = fStyleMap.get(aPaintState);
    if (styler == null && aPaintState == TLspPaintState.EDITED) {
      return getStyler(TLspPaintState.SELECTED);
    } else if (styler == null && aPaintState == TLspPaintState.SELECTED) {
      return getStyler(TLspPaintState.REGULAR);
    } else {
      return styler;
    }
  }

  /**
   * Sets a new styler. If the styler is not {@code null}, the listener
   * registers itself with the styler to receive style change events.
   *
   * @param aStyler
   *          the new styler
   */
  public void setStyler(TLspPaintState aPaintState, ILspStyler aStyler) {
    ILspStyler old = fStyleMap.get(aPaintState);
    if (old != null) {
      old.removeStyleChangeListener(this);
    }
    fStyleMap.put(aPaintState, aStyler);
    if (aStyler != null) {
      aStyler.addStyleChangeListener(this);
    }
    fPropertyChangeSupport.firePropertyChange("styler", old, aStyler);
    styleChanged(new TLspStyleChangeEvent(aStyler, null, null, null));
  }

  @Override
  public <T> boolean query(List<TLspPaintGroup> aPaintGroups, ALspPaintQuery<T> aQuery, TLspPaintRepresentationState aPaintRepresentationState, TLspContext aContext) {
    if (aQuery instanceof TLspIsTouchedQuery) {
      TLspIsTouchedQuery query = (TLspIsTouchedQuery) aQuery;
      TLcdXYBounds objectViewBounds = new TLcdXYBounds();
      if (getAbsoluteBounds(query.getObject(), aContext, objectViewBounds)) {
        TLcdXYBounds queryViewBounds = getBounds(query.getViewPoint(), query.getSensitivity());
        if (objectViewBounds.interacts2D(queryViewBounds)) {
          objectViewBounds.setTo2DIntersection(queryViewBounds);
          query.touched(new ViewTouchInfo(query.getObject(), objectViewBounds.getLocation(), false));
        }
      }
      return true;
    }
    if (aQuery instanceof com.luciad.view.lightspeed.query.TLspBoundsQuery) {
      com.luciad.view.lightspeed.query.TLspBoundsQuery query = (com.luciad.view.lightspeed.query.TLspBoundsQuery) aQuery;
      TLcdXYBounds objectViewBounds = new TLcdXYBounds();
      if (getAbsoluteBounds(query.getObject(), aContext, objectViewBounds)) {
        query.bounds(new ViewBoundsInfo(query.getObject(), objectViewBounds, false));
      }
      return true;
    }
    if (aQuery instanceof TLspPaintedObjectsTouchQuery) {
      TLspPaintedObjectsTouchQuery query = (TLspPaintedObjectsTouchQuery) aQuery;
      TLcdXYBounds queryViewBounds = getBounds(query.getViewBounds(), query.getSensitivity());
      for (TLspPaintGroup paintGroup : aPaintGroups) {
        for (Object object : paintGroup.getObjects()) {
          TLcdXYBounds objectViewBounds = new TLcdXYBounds();
          if (!getAbsoluteBounds(object, aContext, objectViewBounds)) {
            continue;
          }
          if (queryViewBounds == null || objectViewBounds.interacts2D(queryViewBounds)) {
            objectViewBounds.setTo2DIntersection(queryViewBounds);
            query.touched(new ViewTouchInfo(object, objectViewBounds.getLocation(), false));
          }
        }
      }
      return true;
    }
    if (aQuery instanceof TLspPaintedObjectsBoundsQuery) {
      TLspPaintedObjectsBoundsQuery query = (TLspPaintedObjectsBoundsQuery) aQuery;
      TLcdXYBounds queryViewBounds = getBounds(query.getViewBounds(), query.getSensitivity());
      for (TLspPaintGroup paintGroup : aPaintGroups) {
        for (Object object : paintGroup.getObjects()) {
          TLcdXYBounds objectViewBounds = new TLcdXYBounds();
          if (!getAbsoluteBounds(object, aContext, objectViewBounds)) {
            continue;
          }
          if (queryViewBounds == null || objectViewBounds.interacts2D(queryViewBounds)) {
            query.bounds(new ViewBoundsInfo(object, objectViewBounds, false));
          }
        }
      }
      return true;
    }
    if (aQuery instanceof TLspPaintedObjectsQuery) {
      TLspPaintedObjectsQuery query = (TLspPaintedObjectsQuery) aQuery;
      TLcdXYBounds queryViewBounds = getBounds(query.getViewBounds(), 0.0);
      for (TLspPaintGroup paintGroup : aPaintGroups) {
        for (Object object : paintGroup.getObjects()) {
          TLcdXYBounds objectViewBounds = new TLcdXYBounds();
          if (!getAbsoluteBounds(object, aContext, objectViewBounds)) {
            continue;
          }
          if (queryViewBounds == null || objectViewBounds.interacts2D(queryViewBounds)) {
            query.object(object);
          }
        }
      }
      return true;
    }
    throw new UnsupportedOperationException("Unsupported query: " + aQuery);
  }

  private TLcdXYBounds getBounds(ILcdPoint aViewPoint, double aSensitivity) {
    return getBounds(aViewPoint.getBounds(), aSensitivity);
  }

  private TLcdXYBounds getBounds(ILcdBounds aViewBounds, double aSensitivity) {
    return new TLcdXYBounds(
        aViewBounds.getLocation().getX() - aSensitivity,
        aViewBounds.getLocation().getY() - aSensitivity,
        aViewBounds.getWidth() + 2 * aSensitivity,
        aViewBounds.getHeight() + 2 * aSensitivity
    );
  }

  private static class ViewTouchInfo extends ALspViewTouchInfo {
    private final Object fObject;
    private final ILcdPoint fLocation;
    private final boolean fHasDepth;

    public ViewTouchInfo(Object aObject, ILcdPoint aLocation, boolean aHasDepth) {
      fObject = aObject;
      fLocation = aLocation;
      fHasDepth = aHasDepth;
    }

    @Override
    public ILcdPoint getTouchedViewPoint() {
      return fLocation;
    }

    @Override
    public boolean hasDepth() {
      return fHasDepth;
    }

    @Override
    public Object getDomainObject() {
      return fObject;
    }
  }

  private static class ViewBoundsInfo extends ALspViewBoundsInfo {
    private final Object fObject;
    private final TLcdXYBounds fObjectViewBounds;
    private final boolean fHasDepth;

    public ViewBoundsInfo(Object aObject, TLcdXYBounds aObjectViewBounds, boolean aHasDepth) {
      fObject = aObject;
      fObjectViewBounds = aObjectViewBounds;
      fHasDepth = aHasDepth;
    }

    @Override
    public ILcdBounds getViewBounds() {
      return fObjectViewBounds;
    }

    @Override
    public boolean hasDepth() {
      return fHasDepth;
    }

    @Override
    public Object getDomainObject() {
      return fObject;
    }
  }
}
