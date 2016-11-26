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
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;

/**
 * This label painter wrapper makes it easy to configure the delegate label painter based on
 * a given gxy context.
 */
public abstract class ConfigurableGXYLabelPainterWrapper implements ILcdGXYLabelPainterProvider, ILcdGXYLabelPainter2 {

  private ILcdGXYLabelPainter2 fDelegate;
  private PropertyChangeSupport fPropertySupport = new PropertyChangeSupport(this);

  protected ConfigurableGXYLabelPainterWrapper(ILcdGXYLabelPainter2 aDelegate) {
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
   * Returns the delegate label painter.
   * @return the delegate label painter.
   */
  public ILcdGXYLabelPainter2 getDelegate() {
    return fDelegate;
  }

  /**
   * Configures the given label painter for the given context and object.
   * @param aLabelPainter  the given label painter.
   * @param aGXYContext    the gxy context
   * @param aObject        the object
   * @return <code>true</code> if it was possible to configure the given label painter using the
   *         given level of detail, and <code>false</code> otherwise. If <code>false</code> is
   *         returned, the label painter will not be used to paint labels or return bounds, ... .
   */
  protected abstract boolean configureLabelPainter(ILcdGXYLabelPainter2 aLabelPainter, ILcdGXYContext aGXYContext, Object aObject);

  public void setLabelLocation(TLcdLabelLocation aLabelLocation) {
    fDelegate.setLabelLocation(aLabelLocation);
  }

  public void setObject(Object aObject) {
    fDelegate.setObject(aObject);
  }

  public TLcdLabelLocation getLabelLocation() {
    return fDelegate.getLabelLocation();
  }

  public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    if (configureLabelPainter(fDelegate, aGXYContext, getObject())) {
      fDelegate.paintLabel(aGraphics, aMode, aGXYContext);
    }
  }

  public double labelBoundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Rectangle aRectangleSFCT) throws TLcdNoBoundsException {
    if (configureLabelPainter(fDelegate, aGXYContext, getObject())) {
      return fDelegate.labelBoundsSFCT(aGraphics, aMode, aGXYContext, aRectangleSFCT);
    } else {
      throw new TLcdNoBoundsException();
    }
  }

  public boolean isLabelTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return configureLabelPainter(fDelegate, aGXYContext, getObject()) &&
           fDelegate.isLabelTouched(aGraphics, aMode, aGXYContext);
  }

  public void labelAnchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
    if (configureLabelPainter(fDelegate, aGXYContext, getObject())) {
      fDelegate.labelAnchorPointSFCT(aGraphics, aMode, aGXYContext, aPointSFCT);
    } else {
      throw new TLcdNoBoundsException();
    }
  }

  public boolean supportLabelSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return configureLabelPainter(fDelegate, aGXYContext, getObject()) &&
           fDelegate.supportLabelSnap(aGraphics, aGXYContext);
  }

  public Object labelSnapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    if (configureLabelPainter(fDelegate, aGXYContext, getObject())) {
      return fDelegate.labelSnapTarget(aGraphics, aGXYContext);
    } else {
      return null;
    }
  }

  public Cursor getLabelCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    if (configureLabelPainter(fDelegate, aGXYContext, getObject())) {
      return fDelegate.getLabelCursor(aGraphics, aMode, aGXYContext);
    } else {
      return null;
    }
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

  public ILcdGXYLabelPainter getGXYLabelPainter(Object aObject) {
    setObject(aObject);
    return this;
  }

  public void setLabelIndex(int aLabelIndex) {
    fDelegate.setLabelIndex(aLabelIndex);
  }

  public int getLabelIndex() {
    return fDelegate.getLabelIndex();
  }

  public int getLabelCount(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    if (configureLabelPainter(fDelegate, aGXYContext, getObject())) {
      return fDelegate.getLabelCount(aGraphics, aGXYContext);
    }
    return 0;
  }

  public int getSubLabelIndex() {
    return fDelegate.getSubLabelIndex();
  }

  public void setSubLabelIndex(int aSubLabelIndex) {
    fDelegate.setSubLabelIndex(aSubLabelIndex);
  }

  public int getSubLabelCount(int aLabelIndex) {
    return fDelegate.getSubLabelCount(aLabelIndex);
  }

  public Object getObject() {
    return fDelegate.getObject();
  }

  public void setLocationIndex(int aLocationIndex) {
    fDelegate.setLocationIndex(aLocationIndex);
  }

  public int getLocationIndex() {
    return fDelegate.getLocationIndex();
  }

  public int getPossibleLocationCount(Graphics aGraphics) {
    return fDelegate.getPossibleLocationCount(aGraphics);
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
