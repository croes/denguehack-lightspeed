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
package samples.gxy.editmodes;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeListener;

import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.painter.TLcdGXYBoundsPainter;

/**
 * Painter wrapper that adds rotation.
 * Object rotation is defined and retrieved using the RotatingPainterSupport class.
 * The model objects must implement ILcdShape.
 */
public class RotatingPainterWrapper implements ILcdGXYPainter, ILcdGXYPainterProvider {

  private ILcdGXYPainter fPainter;
  private ILcdGXYPainterProvider fPainterProvider;
  private Object fObject;
  private RotationSupport fObjectRotationSupport;
  private ILcdGXYPainter fBoundsPainter;

  private static Cursor sCursorRotating = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  public RotatingPainterWrapper(ILcdGXYPainter aPainter, RotationSupport aObjectRotationSupport) {
    fPainter = aPainter;
    fObjectRotationSupport = aObjectRotationSupport;
    fBoundsPainter = new TLcdGXYBoundsPainter();
  }

  public RotatingPainterWrapper(ILcdGXYPainterProvider aPainterProvider, RotationSupport aObjectRotationSupport) {
    fPainterProvider = aPainterProvider;
    fObjectRotationSupport = aObjectRotationSupport;
    fBoundsPainter = new TLcdGXYBoundsPainter();
  }

  public void paint(Graphics aGraphics, int aRenderMode, ILcdGXYContext aGXYContext) {

    ILcdShape object = (ILcdShape) fObject;
    int delegateMode = aRenderMode;
    boolean rotated = false;

    // Applies a rotation if necessary.
    if (((aRenderMode & ILcdGXYPainter.BODY) != 0)
        &&
        ((aRenderMode & ILcdGXYPainter.TRANSLATING) != 0)) {
      // For this render mode, we make a copy of our object,
      // we edit it like it has been rotated and then paint it.
      object = (ILcdShape) object.clone();
      rotated = privateEdit(object, paintModeToEditMode(aRenderMode), aGXYContext);

      if (rotated) {
        // Makes sure to draw the rotated copy...
        fPainter.setObject(object);
        // ... without applying extra editing.
        delegateMode = delegateMode & ~ILcdGXYPainter.TRANSLATING;
      }
    }

    // Paints the rotated object. Leave out the handles: we have our own.
    fPainter.paint(aGraphics, delegateMode & ~ILcdGXYPainter.HANDLES, aGXYContext);

    // Restores the original object if necessary.
    if (rotated) {
      fPainter.setObject(getObject());
    }

    // Draws rotation handles.
    if (((aRenderMode & ILcdGXYPainter.HANDLES) != 0) &&   // When asked for, but not while creating the object.
        ((aRenderMode & ILcdGXYPainter.RESHAPING) == 0) && // Don't support reshaping.
        ((aRenderMode & ILcdGXYPainter.CREATING) == 0) &&  // Don't support creation.
        ((aRenderMode & ILcdGXYPainter.TRANSLATING) == 0)  // No need to draw handles while rotating.
        ) {

      fBoundsPainter.paint(aGraphics, ILcdGXYPainter.BODY, aGXYContext);
      fObjectRotationSupport.drawRotationHandles(aGraphics, aRenderMode, aGXYContext, getObject());
    }
  }

  private boolean privateEdit(Object aObject, int aMode, ILcdGXYContext aGXYContext) {
    return fObjectRotationSupport.rotateObject(aObject, aGXYContext);
  }

  public void setObject(Object aObject) {
    fObject = aObject;
    fPainter.setObject(aObject);
    fBoundsPainter.setObject(((ILcdBounded) aObject).getBounds());
  }

  public Object getObject() {
    return fObject;
  }

  public void boundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, ILcd2DEditableBounds aBoundsSFCT) throws TLcdNoBoundsException {
    fPainter.boundsSFCT(aGraphics, aMode, aGXYContext, aBoundsSFCT);
  }

  public boolean isTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return fObjectRotationSupport.isRotationHandleTouched(aMode, aGXYContext, getObject()) ||
           fPainter.isTouched(aGraphics, aMode, aGXYContext) || fBoundsPainter.isTouched(aGraphics, aMode, aGXYContext);
  }

  public void anchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
    fPainter.anchorPointSFCT(aGraphics, aMode, aGXYContext, aPointSFCT);
  }

  public boolean supportSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fPainter.supportSnap(aGraphics, aGXYContext);
  }

  public Object snapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fPainter.snapTarget(aGraphics, aGXYContext);
  }

  public Cursor getCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return sCursorRotating;
  }

  public String getDisplayName() {
    return fPainter.getDisplayName();
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPainter.addPropertyChangeListener(aPropertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPainter.removePropertyChangeListener(aPropertyChangeListener);
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    if (fPainterProvider != null) {
      fPainter = fPainterProvider.getGXYPainter(aObject);
    }
    if (aObject != getObject()) {
      setObject(aObject);
    }
    return this;
  }

  public Object clone() {
    try {
      RotatingPainterWrapper clone = (RotatingPainterWrapper) super.clone();
      // deeply cloned state
      clone.fPainter = (ILcdGXYPainter) fPainter.clone();
      clone.fBoundsPainter = (ILcdGXYPainter) fBoundsPainter.clone();
      clone.fPainterProvider = (ILcdGXYPainterProvider) fPainterProvider.clone();
      // transient state
      clone.fObjectRotationSupport = (RotationSupport) fObjectRotationSupport.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("super.clone should be supported but it isn't.", e);
    }
  }

  private int paintModeToEditMode(int aRenderMode) {
    return (aRenderMode & ILcdGXYPainter.TRANSLATING) != 0 ? ILcdGXYEditor.TRANSLATED :
           (aRenderMode & ILcdGXYPainter.CREATING) != 0 ? ILcdGXYEditor.CREATING : ILcdGXYEditor.RESHAPED;
  }

  private int editModeToPainterMode(int aMode) {
    return (aMode & ILcdGXYEditor.TRANSLATED) != 0 ? ILcdGXYPainter.TRANSLATING :
           (aMode & ILcdGXYEditor.CREATING) != 0 ? ILcdGXYPainter.CREATING : ILcdGXYPainter.RESHAPING;
  }
}
