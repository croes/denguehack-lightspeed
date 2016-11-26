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
package samples.gxy.labels.common;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;

/**
 * This painter wrapper makes it easy to configure the delegate painter based on
 * a given gxy context.
 */
public abstract class ConfigurableGXYPainterWrapper implements ILcdGXYPainterProvider, ILcdGXYPainter {

  private ILcdGXYPainter fDelegate;
  private PropertyChangeSupport fPropertySupport = new PropertyChangeSupport(this);

  protected ConfigurableGXYPainterWrapper() {
  }

  /**
   * Sets the delegate painter. This should be done before calling any other methods.
   * @param aDelegate a given painter.
   */
  public void setDelegate(ILcdGXYPainter aDelegate) {
    if (fDelegate != null) {
      throw new IllegalArgumentException("Delegate painter is already set");
    }

    fDelegate = aDelegate;
    fDelegate.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        fPropertySupport.firePropertyChange(evt.getPropertyName(),
                                            evt.getOldValue(),
                                            evt.getNewValue());
      }
    });
  }

  /**
   * Returns the delegate painter.
   * @return the delegate painter.
   */
  public ILcdGXYPainter getDelegate() {
    return fDelegate;
  }

  /**
   * Configures the given painter for the given context and object.
   * @param aPainter    the given painter.
   * @param aGXYContext the gxy context
   * @param aObject     the object
   * @return <code>true</code> if it was possible to configure the given painter using the
   *         given level of detail, and <code>false</code> otherwise. If <code>false</code> is
   *         returned, the painter will not be used to paint or return bounds, ... .
   */
  protected abstract boolean configurePainter(ILcdGXYPainter aPainter, ILcdGXYContext aGXYContext, Object aObject);

  public void setObject(Object aObject) {
    fDelegate.setObject(aObject);
  }

  public Object getObject() {
    return fDelegate.getObject();
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    if (configurePainter(fDelegate, aGXYContext, getObject())) {
      fDelegate.paint(aGraphics, aMode, aGXYContext);
    }
  }

  public void boundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, ILcd2DEditableBounds aBoundsSFCT) throws TLcdNoBoundsException {
    if (configurePainter(fDelegate, aGXYContext, getObject())) {
      fDelegate.boundsSFCT(aGraphics, aMode, aGXYContext, aBoundsSFCT);
    } else {
      throw new TLcdNoBoundsException();
    }
  }

  public boolean isTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return configurePainter(fDelegate, aGXYContext, getObject()) &&
           fDelegate.isTouched(aGraphics, aMode, aGXYContext);
  }

  public void anchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
    if (configurePainter(fDelegate, aGXYContext, getObject())) {
      fDelegate.anchorPointSFCT(aGraphics, aMode, aGXYContext, aPointSFCT);
    } else {
      throw new TLcdNoBoundsException();
    }
  }

  public boolean supportSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return configurePainter(fDelegate, aGXYContext, getObject()) &&
           fDelegate.supportSnap(aGraphics, aGXYContext);
  }

  public Object snapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    if (configurePainter(fDelegate, aGXYContext, getObject())) {
      return fDelegate.snapTarget(aGraphics, aGXYContext);
    }
    return null;
  }

  public Cursor getCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    if (configurePainter(fDelegate, aGXYContext, getObject())) {
      return fDelegate.getCursor(aGraphics, aMode, aGXYContext);
    }
    return null;
  }

  public String getDisplayName() {
    return fDelegate.getDisplayName();
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertySupport.addPropertyChangeListener(aPropertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertySupport.removePropertyChangeListener(aPropertyChangeListener);
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    setObject(aObject);
    return this;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Cloning is not supported for this object : " + this);
    }
  }
}
