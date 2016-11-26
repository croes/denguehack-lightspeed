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
package samples.gxy.labels.offset;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;

import com.luciad.gui.ILcdIcon;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdInterval;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.ALcdLabelLocations;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.*;

/**
 * This layer wrapper makes sure body labels are always painted together with the bodies, while
 * text labels are painted on top later. To do this, applyOnInteract() and paint() are overridden.
 * <p>
 * Doing it this way ensures that the used label placers still collect body labels as well as
 * text labels. So both are decluttered correctly. At the same time it guarantees that the
 * body labels and the text labels are painted at the correct time. When this layer paints
 * the bodies, it also paints the labels that are returned by applyOnInteract() with a given
 * paint mode that contains ILcdGXYLayer.BODIES. Similarly, during the painting of labels it
 * paints the labels that are returned by this method with a given paint mode that contains
 * ILcdGXYLayer#LABELS.
 * <p>
 * This layer will only work correctly when the ILcdGXYEditableLabelsLayer#PLACED_LABELS flag
 * is used during painting, and not ILcdGXYLayer#LABELS. In the latter case an exception is thrown.
 */
public class BodyLabelsLayerWrapper implements ILcdGXYEditableLabelsLayer {

  // Settings
  private ILcdGXYEditableLabelsLayer fDelegate;
  private boolean fLabeled;
  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  // Temporary
  private TLcdLabelLocation fWorkLocation;

  public static BodyLabelsLayerWrapper createLayerWrapper(TLcdGXYLayer aWrappedLayer, TLcdLabelLocations aLabelLocations, ILcdGXYView aGXYView) {
    BodyLabelsLayerWrapper wrapper = new BodyLabelsLayerWrapper(aWrappedLayer);

    // Make sure the ALcdLabelLocations point to the correct layer
    aLabelLocations.setLayer(wrapper);
    aWrappedLayer.setLabelLocations(aLabelLocations);

    wrapper.fWorkLocation = aLabelLocations.createLabelLocation();

    // We need to invalidate the bodies of the layer when the labels have changed, because
    // we paint the labels together with the bodies (see BodyLabelsLayer).
    LabelLocationsInvalidationListener listener = new LabelLocationsInvalidationListener(wrapper, aLabelLocations, aGXYView);
    aLabelLocations.addLabelLocationListener(listener);
    aLabelLocations.addLabelPaintedListener(listener);

    return wrapper;
  }

  private BodyLabelsLayerWrapper(ILcdGXYEditableLabelsLayer aWrappedLayer) {
    fDelegate = aWrappedLayer;

    // Make sure property changes are correctly redirected. This is done to make sure that the "source"
    // field of the PropertyChangeEvents points to this wrapper instead of the wrapped layer.
    fDelegate.addPropertyChangeListener(new PropertyRedirector());

    fLabeled = fDelegate.isLabeled();

    // The "labeled" property of the delegate layer is always set to 'true'. This wrapper stores an
    // other "labeled" field that can be set and retrieved using isLabeled() and setLabeled().
    aWrappedLayer.setLabeled(true);
  }

  public boolean isLabeled() {
    return fLabeled;
  }

  public void setLabeled(boolean aLabeled) {
    if (fLabeled != aLabeled) {
      boolean old = fLabeled;
      fLabeled = aLabeled;
      fPropertyChangeSupport.firePropertyChange("labeled", Boolean.valueOf(old), Boolean.valueOf(fLabeled));
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aPropertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aPropertyChangeListener);
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYView aGXYView) {
    boolean has_labels_flag = (aMode & ILcdGXYLayer.LABELS) != 0;
    if (has_labels_flag) {
      throw new IllegalArgumentException("This layer only works correctly when ILcdGXYEditableLabelsLayer#PLACED_LABELS is used.");
    }

    // Don't paint the labels here
    fDelegate.paint(aGraphics, aMode & ~ILcdGXYEditableLabelsLayer.PLACED_LABELS, aGXYView);

    boolean has_selected_flag = (aMode & ILcdGXYLayer.SELECTION) != 0;
    boolean has_placed_labels_flag = (aMode & ILcdGXYEditableLabelsLayer.PLACED_LABELS) != 0;
    if (has_placed_labels_flag) {
      // Paint the non-body labels ourselves
      int paint_mode = ILcdGXYLayer.LABELS;
      paint_mode |= has_selected_flag ? ILcdGXYLayer.SELECTION : ILcdGXYLayer.ALL;

      int label_painter_mode = ILcdGXYLabelPainter2.BODY;
      label_painter_mode |= has_selected_flag ? ILcdGXYLabelPainter2.SELECTED : ILcdGXYLabelPainter2.DEFAULT;

      LayerLabelPaintingUtil.paintLabels(aGraphics, aGXYView, this, label_painter_mode, paint_mode);
    }

    boolean has_bodies_flag = (aMode & ILcdGXYLayer.BODIES) != 0;
    if (has_bodies_flag) {
      // Paint the body labels when painting the bodies of the domain objects
      int paint_mode = ILcdGXYLayer.BODIES;
      paint_mode |= has_selected_flag ? ILcdGXYLayer.SELECTION : ILcdGXYLayer.ALL;

      int label_painter_mode = ILcdGXYLabelPainter2.BODY;
      label_painter_mode |= has_selected_flag ? ILcdGXYLabelPainter2.SELECTED : ILcdGXYLabelPainter2.DEFAULT;

      LayerLabelPaintingUtil.paintLabels(aGraphics, aGXYView, this, label_painter_mode, paint_mode);
    }
  }

  public int applyOnInteractLabels(ILcdFunction aLabelFunction, Graphics aGraphics, int aPaintMode, ILcdGXYView aGXYView) {
    boolean has_labels_flag = (aPaintMode & ILcdGXYLayer.LABELS) != 0;
    boolean has_bodies_flag = (aPaintMode & ILcdGXYLayer.BODIES) != 0;

    if (has_labels_flag && has_bodies_flag) {
      // Return all body labels
      int paint_mode = aPaintMode & ~ILcdGXYLayer.LABELS;
      int count = applyOnInteractLabels(aLabelFunction, aGraphics, paint_mode, aGXYView);

      // Return all non-body labels
      paint_mode = aPaintMode & ~ILcdGXYLayer.BODIES;
      count += applyOnInteractLabels(aLabelFunction, aGraphics, paint_mode, aGXYView);

      return count;
    } else if (has_labels_flag) {
      // Make sure only non-body labels are returned.
      LabelFunctionWrapper wrapper = new LabelFunctionWrapper(aLabelFunction, false, aGXYView);
      fDelegate.applyOnInteractLabels(wrapper, aGraphics, aPaintMode, aGXYView);
      return wrapper.getCount();
    } else if (has_bodies_flag) {
      // Make sure only body labels are returned.
      LabelFunctionWrapper wrapper = new LabelFunctionWrapper(aLabelFunction, true, aGXYView);
      int paint_mode = aPaintMode & ~ILcdGXYLayer.BODIES;
      paint_mode |= ILcdGXYLayer.LABELS;
      fDelegate.applyOnInteractLabels(wrapper, aGraphics, paint_mode, aGXYView);
      return wrapper.getCount();
    } else {
      return fDelegate.applyOnInteractLabels(aLabelFunction, aGraphics, aPaintMode, aGXYView);
    }
  }

  private boolean isBodyLabel(TLcdLabelIdentifier aLabelIdentifier, ILcdGXYView aGXYView) {
    getLabelLocations().getLabelLocationSFCT(aLabelIdentifier.getDomainObject(),
                                             aLabelIdentifier.getLabelIndex(),
                                             aLabelIdentifier.getSubLabelIndex(),
                                             aGXYView, fWorkLocation);
    return fWorkLocation.isBodyLabel();
  }

  private class LabelFunctionWrapper implements ILcdFunction {

    private ILcdGXYView fGXYView;
    private ILcdFunction fDelegate;
    private boolean fReturnBodies;
    private int fCount = 0;

    private LabelFunctionWrapper(ILcdFunction aDelegate, boolean aReturnBodies, ILcdGXYView aGXYView) {
      fDelegate = aDelegate;
      fReturnBodies = aReturnBodies;
      fGXYView = aGXYView;
    }

    public boolean applyOn(Object aLabel) throws IllegalArgumentException {
      TLcdLabelIdentifier label = (TLcdLabelIdentifier) aLabel;
      boolean is_body = isBodyLabel(label, fGXYView);

      // Return body labels
      if (fReturnBodies && is_body) {
        fCount++;
        return fDelegate.applyOn(createLabelForCorrectLayer(label));
      }

      // Return text labels
      if (!fReturnBodies && !is_body && fLabeled) {
        fCount++;
        return fDelegate.applyOn(createLabelForCorrectLayer(label));
      }
      return true;
    }

    private TLcdLabelIdentifier createLabelForCorrectLayer(TLcdLabelIdentifier aLabel) {
      // This code makes sure that the TLcdLabelIdentifier passed to the label function contain
      // this BodyLabelsLayerWrapper instead of the wrapped layer.
      return new TLcdLabelIdentifier(BodyLabelsLayerWrapper.this,
                                     aLabel.getDomainObject(),
                                     aLabel.getLabelIndex(),
                                     aLabel.getSubLabelIndex());
    }

    public int getCount() {
      return fCount;
    }
  }

  private class PropertyRedirector implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      // Don't propagate "labeled" property changes of the wrapped layer. This property change is
      // handled by BodyLabelsLayerWrapper.setLabeled() itself.
      if (!"labeled".equals(evt.getPropertyName())) {
        fPropertyChangeSupport.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
      }
    }
  }

  // Delegate methods

  public ILcdGXYLabelEditor getGXYLabelEditor(Object aObject) {
    return fDelegate.getGXYLabelEditor(aObject);
  }

  public boolean isLabelsEditable() {
    return fDelegate.isLabelsEditable();
  }

  public void setLabelsEditable(boolean aLabelsEditable) {
    fDelegate.setLabelsEditable(aLabelsEditable);
  }

  public boolean isLabelsEditableSupported() {
    return fDelegate.isLabelsEditableSupported();
  }

  public ALcdLabelLocations getLabelLocations() {
    return fDelegate.getLabelLocations();
  }

  public int applyOnInteract(ILcdFunction aFunction, Graphics aGraphics, int aPaintMode, ILcdGXYView aGXYView) {
    return fDelegate.applyOnInteract(aFunction, aGraphics, aPaintMode, aGXYView);
  }

  public int applyOnInteract(ILcdFunction aFunction, Rectangle aBounds, boolean aStrictInteract, ILcdGXYView aGXYView) {
    return fDelegate.applyOnInteract(aFunction, aBounds, aStrictInteract, aGXYView);
  }

  public ILcdBounds getBounds(int aMode, ILcdGXYView aGXYView) throws TLcdNoBoundsException {
    return fDelegate.getBounds(aMode, aGXYView);
  }

  public void stopPainting() {
    fDelegate.stopPainting();
  }

  public ILcdInterval getScaleRange() {
    return fDelegate.getScaleRange();
  }

  public ILcdInterval getLabelScaleRange() {
    return fDelegate.getLabelScaleRange();
  }

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    return fDelegate.getGXYPainter(aObject);
  }

  public ILcdGXYEditor getGXYEditor(Object aObject) {
    return fDelegate.getGXYEditor(aObject);
  }

  public ILcdGXYLabelPainter getGXYLabelPainter(Object aObject) {
    return fDelegate.getGXYLabelPainter(aObject);
  }

  public ILcdGXYPen getGXYPen() {
    return fDelegate.getGXYPen();
  }

  public Class getModelXYWorldTransfoClass() {
    return fDelegate.getModelXYWorldTransfoClass();
  }

  public boolean isLabeledSupported() {
    return fDelegate.isLabeledSupported();
  }

  public ILcdModel getModel() {
    return fDelegate.getModel();
  }

  public boolean isVisible() {
    return fDelegate.isVisible();
  }

  public void setVisible(boolean aVisible) {
    fDelegate.setVisible(aVisible);
  }

  public boolean isSelectableSupported() {
    return fDelegate.isSelectableSupported();
  }

  public boolean isSelectable() {
    return fDelegate.isSelectable();
  }

  public void setSelectable(boolean aSelectable) {
    fDelegate.setSelectable(aSelectable);
  }

  public boolean isEditableSupported() {
    return fDelegate.isEditableSupported();
  }

  public boolean isEditable() {
    return fDelegate.isEditable();
  }

  public void setEditable(boolean aEditable) {
    fDelegate.setEditable(aEditable);
  }

  public void selectObject(Object aObject, boolean aSelection, int aDispatchEventMode) {
    fDelegate.selectObject(aObject, aSelection, aDispatchEventMode);
  }

  public void fireCollectedSelectionChanges() {
    fDelegate.fireCollectedSelectionChanges();
  }

  public void clearSelection(int aDispatchEventMode) {
    fDelegate.clearSelection(aDispatchEventMode);
  }

  public String getLabel() {
    return fDelegate.getLabel();
  }

  public void setLabel(String aLabel) {
    fDelegate.setLabel(aLabel);
  }

  public ILcdIcon getIcon() {
    return fDelegate.getIcon();
  }

  public void setIcon(ILcdIcon aIcon) {
    fDelegate.setIcon(aIcon);
  }

  public int getSelectionCount() {
    return fDelegate.getSelectionCount();
  }

  public Enumeration selectedObjects() {
    return fDelegate.selectedObjects();
  }

  public boolean isSelected(Object aObject) {
    return fDelegate.isSelected(aObject);
  }

  public void addSelectionListener(ILcdSelectionListener aSelectionListener) {
    fDelegate.addSelectionListener(aSelectionListener);
  }

  public void removeSelectionListener(ILcdSelectionListener aSelectionListener) {
    fDelegate.removeSelectionListener(aSelectionListener);
  }
}
