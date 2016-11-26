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
package samples.gxy.highlight;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.WeakHashMap;

import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYController;
import com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2;

/**
 * This controller highlights the domain object under the mouse cursor. Highlighting is supported
 * for registered layers implementing HighlightLayer.
 *
 * @see #registerLayer(HighlightLayer)
 */
public class HighlightController extends ALcdGXYController implements MouseMotionListener {

  private Set<HighlightLayer> fLayers = Collections.newSetFromMap(new WeakHashMap<HighlightLayer, Boolean>());
  private TLcdDomainObjectContext fCurrentObject = null;

  /**
   * Registers a layer for which to check if objects need to be highlighted.
   *
   * @param aLayer a layer that is notified when it should highlight an object
   */
  public void registerLayer(HighlightLayer aLayer) {
    fLayers.add(aLayer);
  }

  /**
   * Unregisters the given layer.
   *
   * @param aLayer a layer.
   */
  public void unregisterLayer(HighlightLayer aLayer) {
    fLayers.remove(aLayer);
    if (fCurrentObject != null && fCurrentObject.getLayer() == aLayer) {
      fCurrentObject = null;
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    // nothing to do
  }

  @Override
  public void mouseMoved(MouseEvent aEvent) {
    TLcdDomainObjectContext previousObject = fCurrentObject;
    // Retrieve the object that should be highlighted
    TLcdDomainObjectContext newObject = getObjectUnderPoint(new Point(aEvent.getX(), aEvent.getY()));
    if (previousObject != newObject) {
      fCurrentObject = newObject;
    }
    getGXYView().repaint();
  }

  @Override
  public void paint(Graphics aGraphics) {
    super.paint(aGraphics);
    if (fCurrentObject != null) {
      ((HighlightLayer) fCurrentObject.getLayer()).paintHighlightedObject(fCurrentObject.getDomainObject(), aGraphics, getGXYView());
    }
  }

  /**
   * Returns the object under the cursor that should be highlighted.
   *
   * @param aViewPoint the cursor position in view coordinates
   *
   * @return the object that should be highlighted or {@code null} if no object was found
   */
  private TLcdDomainObjectContext getObjectUnderPoint(final Point aViewPoint) {

    TLcdGXYSelectControllerModel2 model = new TLcdGXYSelectControllerModel2();
    ILcdGXYLayerSubsetList candidates = model.selectionCandidates(
        getGXYView(),
        new Rectangle(aViewPoint.x, aViewPoint.y, 1, 1),
        TLcdGXYSelectControllerModel2.INPUT_MODE_POINT,
        null,
        TLcdGXYSelectControllerModel2.SELECT_BY_WHAT_BODIES_ON_CLICK |
        TLcdGXYSelectControllerModel2.SELECT_BY_WHAT_LABELS_ON_CLICK,
        TLcdGXYSelectControllerModel2.SELECT_HOW_ADD);

    for (ILcdGXYLayer layer : fLayers) {
      Enumeration subset = candidates.layerSubset(layer);
      if (subset.hasMoreElements()) {
        return new TLcdDomainObjectContext(subset.nextElement(), layer.getModel(), layer, getGXYView());
      }
    }
    return null;
  }

  /**
   * Layer that supports highlighted objects.
   */
  public static interface HighlightLayer extends ILcdGXYLayer {

    /**
     * This method paints the given object as highlighted.
     *
     * @param aObject the object that should be highlighted.
     */
    void paintHighlightedObject(Object aObject, Graphics aGraphics, ILcdGXYView aGXYView);
  }

}
