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
package samples.gxy.labels.interactive;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ALcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLabelEditor;
import com.luciad.view.gxy.ILcdGXYLabelEditorProvider;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;

/**
 * This ILcdGXYLabelPainter2 is a composite label painter. It uses the abstract method
 * {@link #getLabelPainter()} to choose between two delegate label painters.
 *
 * This ILcdGXYLabelPainter2 can be used for interactive labels. It is constructed
 * using two ILcdGXYLabelPainter2s. One label painter is used for the normal
 * (non-interactive) labels. The other label painter is used for the interactive labels.
 */
public abstract class CompositeLabelPainter extends ALcdGXYLabelPainter implements ILcdGXYLabelEditor,
                                                                                   ILcdGXYLabelPainterProvider,
                                                                                   ILcdGXYLabelEditorProvider {

  private ILcdGXYLabelPainter2 fLabelPainter1;
  private ILcdGXYLabelPainter2 fLabelPainter2;

  /**
   * Create a new <code>TLcdGXYInteractiveLabelPainter</code> using the given label painters.
   *
   * @param aLabelPainter            the label painter used to handle non-interactive label.
   * @param aInteractiveLabelPainter the label painter used to handle interactive label.
   */
  public CompositeLabelPainter(ILcdGXYLabelPainter2 aLabelPainter, ILcdGXYLabelPainter2 aInteractiveLabelPainter) {
    fLabelPainter1 = aLabelPainter;
    fLabelPainter1.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChangeEvent(new PropertyChangeEvent(CompositeLabelPainter.this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
      }
    });
    fLabelPainter2 = aInteractiveLabelPainter;
    fLabelPainter2.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChangeEvent(new PropertyChangeEvent(CompositeLabelPainter.this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
      }
    });
  }

  public ILcdGXYLabelPainter2 getLabelPainter1() {
    return fLabelPainter1;
  }

  public ILcdGXYLabelPainter2 getLabelPainter2() {
    return fLabelPainter2;
  }

  protected abstract ILcdGXYLabelPainter2 getLabelPainter();

  @Override
  public void setLabelLocation(TLcdLabelLocation aLabelLocation) {
    super.setLabelLocation(aLabelLocation);
    fLabelPainter1.setLabelLocation(aLabelLocation);
    fLabelPainter2.setLabelLocation(aLabelLocation);
  }

  @Override
  public void setObject(Object aObject) {
    super.setObject(aObject);
    fLabelPainter1.setObject(aObject);
    fLabelPainter2.setObject(aObject);
  }

  @Override
  public void setLabelIndex(int aLabelIndex) {
    super.setLabelIndex(aLabelIndex);
    fLabelPainter1.setLabelIndex(aLabelIndex);
    fLabelPainter2.setLabelIndex(aLabelIndex);
  }

  @Override
  public void setSubLabelIndex(int aSubLabelIndex) {
    super.setSubLabelIndex(aSubLabelIndex);
    fLabelPainter1.setSubLabelIndex(aSubLabelIndex);
    fLabelPainter2.setSubLabelIndex(aSubLabelIndex);
  }

  @Override
  public int getLabelCount(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    int label_count = fLabelPainter1.getLabelCount(aGraphics, aGXYContext);
    int interactive_label_count = fLabelPainter2.getLabelCount(aGraphics, aGXYContext);
    if (label_count != interactive_label_count) {
      throw new IllegalStateException("Both ILcdGXYLabelPainter2's should return the same value for getLabelCount()");
    }
    return label_count;
  }

  @Override
  public int getSubLabelCount(int aLabelIndex) {
    int sublabel_count = fLabelPainter1.getSubLabelCount(aLabelIndex);
    int interactive_sublabel_count = fLabelPainter2.getSubLabelCount(aLabelIndex);
    if (sublabel_count != interactive_sublabel_count) {
      throw new IllegalStateException("Both ILcdGXYLabelPainter2's should return the same value for getSubLabelCount()");
    }
    return sublabel_count;
  }

  @Override
  public void setLocationIndex(int aLocationIndex) {
    super.setLocationIndex(aLocationIndex);
    fLabelPainter1.setLocationIndex(aLocationIndex);
    fLabelPainter2.setLocationIndex(aLocationIndex);
  }

  public int getPossibleLocationCount(Graphics aGraphics) {
    int location_count = fLabelPainter1.getPossibleLocationCount(aGraphics);
    int interactive_location_count = fLabelPainter2.getPossibleLocationCount(aGraphics);
    if (location_count != interactive_location_count) {
      throw new IllegalStateException("Both ILcdGXYLabelPainter2's should return the same value for getPossibleLocationCount()");
    }
    return location_count;
  }

  @Override
  public Object clone() {
    CompositeLabelPainter clone = (CompositeLabelPainter) super.clone();
    clone.fLabelPainter1 = (ILcdGXYLabelPainter2) fLabelPainter1.clone();
    clone.fLabelPainter2 = (ILcdGXYLabelPainter2) fLabelPainter2.clone();
    return clone;
  }

  @Override
  public Cursor getLabelCursor(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    return label_painter.getLabelCursor(aGraphics, aMode, aGXYContext);
  }

  @Override
  public boolean supportLabelSnap(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    return label_painter.supportLabelSnap(aGraphics, aGXYContext);
  }

  @Override
  public Object labelSnapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    return label_painter.labelSnapTarget(aGraphics, aGXYContext);
  }

  public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    label_painter.paintLabel(aGraphics, aMode, aGXYContext);
  }

  public double labelBoundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Rectangle aRectangleSFCT) throws TLcdNoBoundsException {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    return label_painter.labelBoundsSFCT(aGraphics, aMode, aGXYContext, aRectangleSFCT);
  }

  public boolean isLabelTouched(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    return label_painter.isLabelTouched(aGraphics, aMode, aGXYContext);
  }

  public void labelAnchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    label_painter.labelAnchorPointSFCT(aGraphics, aMode, aGXYContext, aPointSFCT);
  }

  public boolean editLabel(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    if (label_painter instanceof ILcdGXYLabelEditor) {
      ILcdGXYLabelEditor label_editor = (ILcdGXYLabelEditor) label_painter;
      return label_editor.editLabel(aGraphics, aMode, aGXYContext);
    }
    return false;
  }

  public int getLabelCreationClickCount() {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    if (label_painter instanceof ILcdGXYLabelEditor) {
      ILcdGXYLabelEditor label_editor = (ILcdGXYLabelEditor) label_painter;
      return label_editor.getLabelCreationClickCount();
    }
    return 0;
  }

  public boolean acceptSnapTargetForLabel(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    ILcdGXYLabelPainter2 label_painter = getLabelPainter();
    if (label_painter instanceof ILcdGXYLabelEditor) {
      ILcdGXYLabelEditor label_editor = (ILcdGXYLabelEditor) label_painter;
      return label_editor.acceptSnapTargetForLabel(aGraphics, aGXYContext);
    }
    return false;
  }

  public ILcdGXYLabelEditor getGXYLabelEditor(Object aObject) {
    setObject(aObject);
    return this;
  }

  public ILcdGXYLabelPainter getGXYLabelPainter(Object aObject) {
    setObject(aObject);
    return this;
  }
}
