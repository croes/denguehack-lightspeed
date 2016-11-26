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
package samples.gxy.common.controller;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.ILcdAssoc;
import com.luciad.util.ILcdFunction;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.util.collections.TLcdCollectionEvent;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayerSubsetList;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;

/**
 * Lazily updated ILcdGXYLayerSubsetList for a given view, containing the visible objects of the
 * {@link #getSnappableLayers() configured layers}.
 * This is very suited for maintaining a list of targets that objects can snap to.
 *
 * @see TLcdGXYEditController2#setSnappables
 * @see TLcdGXYNewController2#setSnappables
 */
public class SnappablesSubsetList implements ILcdGXYLayerSubsetList {

  /**
   * An upper limit for the amount of snappables, to avoid computational overload.
   */
  private static final int MAX_SNAP_TARGET_COUNT = 100;

  private final ILcdGXYView fMap;
  private final ILcdCollection<ILcdGXYLayer> fSnappableLayers = new TLcdArrayList<ILcdGXYLayer>();

  private final TLcdGXYLayerSubsetList fCachedSubsetList = new TLcdGXYLayerSubsetList();
  private final MyModelChangeListener fModelChangeListener = new MyModelChangeListener();
  private final MySnapUpdateFunction fMySnapUpdateFunction = new MySnapUpdateFunction();

  private boolean fValidCache = false;

  /**
   * Creates an empty snappables list for the given view.
   * After calling this method, layers needs to be added to {@link #getSnappableLayers()}
   */
  public SnappablesSubsetList(ILcdGXYView aMap) {
    fMap = aMap;
    fMap.addPropertyChangeListener(new MyViewChangeListener());
    fSnappableLayers.addCollectionListener(new MySnappableLayersListener());
  }

  /**
   * Add or remove layers from this list to mark them as snappable.
   * @return an editable collection of the layers that can be snapped to
   */
  public ILcdCollection<ILcdGXYLayer> getSnappableLayers() {
    return fSnappableLayers;
  }

  public void addElement(Object aObject, ILcdGXYLayer aGXYLayer) {
    throw new UnsupportedOperationException("This implementation cannot be manipulated directly");
  }

  public void removeElement(Object aObject, ILcdGXYLayer aGXYLayer) {
    throw new UnsupportedOperationException("This implementation cannot be manipulated directly");
  }

  public void removeAllElements() {
    throw new UnsupportedOperationException("This implementation cannot be manipulated directly");
  }

  public Enumeration layers() {
    updateCacheIfNeeded();
    return fCachedSubsetList.layers();
  }

  public Enumeration layerSubset(ILcdGXYLayer aGXYLayer) {
    updateCacheIfNeeded();
    return fCachedSubsetList.layerSubset(aGXYLayer);
  }

  public ILcdGXYLayer retrieveGXYLayer(Object aObject) {
    updateCacheIfNeeded();
    return fCachedSubsetList.retrieveGXYLayer(aObject);
  }

  public ILcdAssoc[] asAssocs() {
    updateCacheIfNeeded();
    return fCachedSubsetList.asAssocs();
  }

  public Enumeration elements() {
    updateCacheIfNeeded();
    return fCachedSubsetList.elements();
  }

  public int size() {
    updateCacheIfNeeded();
    return fCachedSubsetList.size();
  }

  public boolean contains(Object aObject) {
    updateCacheIfNeeded();
    return fCachedSubsetList.contains(aObject);
  }

  private void updateCacheIfNeeded() {
    if (!fValidCache) {
      calculateCache();
    }
  }

  private void invalidateCache() {
    fValidCache = false;
  }

  private void calculateCache() {
    fCachedSubsetList.removeAllElements();
    ILcdGXYView view = fMap;
    if (view.getWidth() > 0 && view.getHeight() > 0) {
      Rectangle bounds = new Rectangle(0, 0, fMap.getWidth(), fMap.getHeight());
      fMySnapUpdateFunction.fCount = 0;
      for (ILcdGXYLayer layer : fSnappableLayers) {
        fMySnapUpdateFunction.fGXYLayer = layer;
        layer.applyOnInteract(fMySnapUpdateFunction, bounds, true, view);
      }
    }
    // clear the function to avoid memory leaks
    fMySnapUpdateFunction.fGXYLayer = null;
    fValidCache = true;
  }

  private class MySnapUpdateFunction implements ILcdFunction {
    public ILcdGXYLayer fGXYLayer;
    public int fCount;

    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      fCachedSubsetList.addElement(aObject, fGXYLayer);
      fCount++;
      return fCount < MAX_SNAP_TARGET_COUNT;
    }
  }

  private class MyViewChangeListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent aEvent) {
      String name = aEvent.getPropertyName();
      if ((name == null ||
           name.equalsIgnoreCase("XYWorldReference") ||
           name.equalsIgnoreCase("Scale") ||
           name.equalsIgnoreCase("ViewOrigin") ||
           name.equalsIgnoreCase("WorldOrigin"))) {
        invalidateCache();
      }
    }
  }

  private class MyModelChangeListener implements ILcdModelListener {
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      invalidateCache();
    }
  }

  private class MySnappableLayersListener implements ILcdCollectionListener<ILcdGXYLayer> {
    @Override
    public void collectionChanged(TLcdCollectionEvent<ILcdGXYLayer> aCollectionEvent) {
      switch (aCollectionEvent.getType()) {
      case ELEMENT_ADDED:
        aCollectionEvent.getElement().getModel().addModelListener(fModelChangeListener);
        invalidateCache();
        break;
      case ELEMENT_REMOVED:
        aCollectionEvent.getElement().getModel().removeModelListener(fModelChangeListener);
        invalidateCache();
        break;
      }
    }
  }
}
