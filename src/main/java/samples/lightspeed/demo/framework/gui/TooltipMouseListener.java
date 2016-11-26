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
package samples.lightspeed.demo.framework.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspTouchInfo;
import com.luciad.view.lightspeed.layer.ALspViewTouchInfo;
import com.luciad.view.lightspeed.layer.ALspWorldTouchInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsTouchQuery;

/**
 * Mouse listener which shows a tooltip
 */
public final class TooltipMouseListener extends MouseAdapter {

  private final ILspAWTView fView;
  private Object fOldIdentifier;
  private final List<ILspLayer> fLayers;
  private final Container fContainer;

  private final List<TooltipLogic> fTooltipLogics;

  private Component fLabel = null;
  private Object fCurrentModelElement = null;
  private ILcdLayer fCurrentLayer = null;

  private final Timer fThrottleTimer;
  private MouseEvent fLastMouseEvent;

  private boolean canShowToolTip = true;

  public TooltipMouseListener(ILspAWTView aView, List<ILspLayer> aLayers, List<TooltipLogic> aTooltipLogics) {
    fView = aView;
    fLayers = aLayers;
    fContainer = fView.getOverlayComponent();
    fTooltipLogics = aTooltipLogics;
    fThrottleTimer = new Timer(250, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        timerFinished();
      }
    });
    fThrottleTimer.setRepeats(false);
  }

  public void stop() {
    fThrottleTimer.stop();
  }

  public void start() {
    fThrottleTimer.start();
  }

  private void timerFinished() {
    Object newIdentifier = fView.getViewXYZWorldTransformation().getIdentifier();
    if (fOldIdentifier == null) {
      fOldIdentifier = newIdentifier;
    }
    if (fOldIdentifier != newIdentifier) {
      fOldIdentifier = newIdentifier;
      return;
    }
    if (fLastMouseEvent == null) {
      return;
    }
    warnTooltipLogicsTooltipWillBeRecalculated();
    for (ILspLayer layer : fLayers) {
      if (!(layer.isVisible())) {
        continue;
      }
      TooltipLogic logic = findTooltipLogicForLayer(layer);
      if (logic == null || !logic.shouldConsiderLayer(layer)) {
        continue;
      }

      if (layer instanceof ILspInteractivePaintableLayer) {
        TLspContext context = new TLspContext(layer, fView);
        Collection<ALspTouchInfo> result = ((ILspInteractivePaintableLayer) layer).query(
            new TLspPaintedObjectsTouchQuery(
                TLspPaintRepresentationState.REGULAR_BODY,
                (ILcdPoint) new TLcdXYPoint(fLastMouseEvent.getX(), fLastMouseEvent.getY()),
                logic.getQuerySensitivity()
            ) {
              @Override
              public boolean touched(ALspViewTouchInfo aTouchInfo) {
                super.touched(aTouchInfo);
                return false; // we only need 1 plot
              }

              @Override
              public boolean touched(ALspWorldTouchInfo aTouchInfo) {
                super.touched(aTouchInfo);
                return false; // we only need 1 plot
              }
            }, context
        );
        if (result.size() > 0) {
          ALspTouchInfo hit = result.iterator().next();
          logic.updateForFoundModelElement(hit.getDomainObject(), layer, fLastMouseEvent, this);
          return;
        }
      }
    }
    clearTooltip();
  }

  private TooltipLogic findTooltipLogicForLayer(ILcdLayer layer) {
    TooltipLogic logic = null;
    for (TooltipLogic tooltipLogic : fTooltipLogics) {
      if (tooltipLogic.canHandleLayer(layer)) {
        logic = tooltipLogic;
        break;
      }
    }
    return logic;
  }

  private void warnTooltipLogicsTooltipWillBeRecalculated() {
    for (TooltipLogic tooltipLogic : fTooltipLogics) {
      tooltipLogic.willRecalculateTooltip();
    }
  }

  private void clearTooltip() {
    for (TooltipLogic tooltipLogic : fTooltipLogics) {
      tooltipLogic.updateForFoundModelElement(null, null, fLastMouseEvent, this);
    }
    fCurrentLayer = null;
    fCurrentModelElement = null;
    fLabel = null;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    fLastMouseEvent = e;
    clearTooltip();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    fLastMouseEvent = e;
    clearTooltip();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    fLastMouseEvent = e;
    clearTooltip();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    fLastMouseEvent = e;
    clearTooltip();
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    fLastMouseEvent = e;
    clearTooltip();
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    fLastMouseEvent = e;
    for (TooltipLogic tooltipLogic : fTooltipLogics) {
      tooltipLogic.updateForFoundModelElement(fCurrentModelElement, fCurrentLayer, fLastMouseEvent, this);
    }
    if (!(fThrottleTimer.isRunning())) {
      fThrottleTimer.start();
    }
  }

  public void setCanShowToolTip(boolean aCanShow) {
    canShowToolTip = aCanShow;
  }

  public boolean isCanShowToolTip() {
    return canShowToolTip;
  }

  /**
   * Sets the tooltip for {@code aModelElement}
   *
   * @param aModelElement The element for which the tooltip must be shown. Can be {@code null}, in
   *                      which case the currently showing tooltip will be removed
   * @param aX            The x coordinate for the tooltip (middle of tooltip)
   * @param aY            The y coordinate for the tooltip (top of tooltip - 15px margin will be added to be below mouse)
   */
  public void showTooltip(Object aModelElement, ILcdLayer aLayer, int aX, int aY) {
//    if(!canShowToolTip){
//      return;
//    }
    if (aModelElement == null || aLayer == null) {
      if (fLabel != null) {
        fContainer.remove(fLabel);
        revalidate(fContainer);
        fContainer.repaint();
        fLabel = null;
      }
      fCurrentModelElement = null;
      fCurrentLayer = null;
      return;
    }
    TooltipLogic logic = findTooltipLogicForLayer(aLayer);
    if (logic == null) {
      return;
    }
    if (aModelElement != fCurrentModelElement) {
      fCurrentModelElement = aModelElement;
      fCurrentLayer = aLayer;
      if (fLabel != null) {
        fContainer.remove(fLabel);
      }
      if (!isCanShowToolTip()) {
        return;
      }
      fLabel = logic.createLabelComponent(aModelElement);
      fContainer.add(fLabel, TLcdOverlayLayout.Location.NO_LAYOUT);
      revalidate(fContainer);
      fContainer.repaint();
    } else {
      if (!isCanShowToolTip()) {
        if (fLabel != null) {
          fContainer.remove(fLabel);
          fLabel = null;
          fContainer.repaint();
        }
      }
    }

    if (null != fLabel) {
      Dimension preferredSize = fLabel.getPreferredSize();
      int w = preferredSize.width + 10;
      int h = preferredSize.height + 6;
      int startX = aX - (w / 2);
      int startY = aY + 15;

      clamp(fLabel, fView.getOverlayComponent(), startX, startY, w, h);

    }
  }

  private static void clamp(Component aComponent, Container aContainer, int aX, int aY, int aWidth, int aHeight) {

    if (aX + aWidth > aContainer.getWidth()) {
      aX = aX - (aX + aWidth - aContainer.getWidth());
    } else if (aX < 0) {
      aX = 5;
    }

    if (aY + aHeight > aContainer.getHeight()) {
      aY = aY - (aY + aHeight - aContainer.getHeight());
    } else if (aY < 0) {
      aY = 5;
    }

    aComponent.setBounds(aX, aY, aWidth, aHeight);
  }

  private void revalidate(Component aComponent) {
    synchronized (aComponent.getTreeLock()) {
      aComponent.invalidate();

      Container root = aComponent.getParent();
      if (root == null) {
        // There's no parents. Just validate itself.
        aComponent.validate();
      } else {
        while (!(root instanceof JComponent) || !((JComponent) root).isValidateRoot()) {
          if (root.getParent() == null) {
            // If there's no validate roots, we'll validate the
            // topmost container
            break;
          }

          root = root.getParent();
        }

        root.validate();
      }
    }
  }

  public static interface TooltipLogic {
    /**
     * Method to determine which layers can be handled by this instance. All other methods which
     * accept a layer parameter will only be called when this method returned {@code true}
     *
     * @param aLayer The layer
     *
     * @return {@code true} when this instance can handle {@code aLayer}
     */
    public boolean canHandleLayer(ILcdLayer aLayer);

    /**
     * Return {@code true} when the layer should be considered for the tooltip
     *
     * @param aLayer The layer
     *
     * @return {@code true} when the layer should be considered for the tooltip
     */
    public boolean shouldConsiderLayer(ILcdLayer aLayer);

    /**
     * Function which is called right before the tooltip is re-calculated.
     */
    public void willRecalculateTooltip();

    /**
     * Returns a {@code Component} which will be shown as tooltip for {@code aModelElement}
     *
     * @param aModelElement The model element. Will be an element of a layer which passes teh {@link
     *                      #canHandleLayer(ILcdLayer)} check
     *
     * @return a {@code Component} which will be shown as tooltip for {@code aModelElement}
     */
    public Component createLabelComponent(Object aModelElement);

    /**
     * Called each time the timer is finished. It is the responsibility of this method to update the
     * tooltip. An example implementation would be <pre class="code"> aTooltipMouseListener.showTooltip(
     * aModelElement, aMouseEvent.getX(), aMouseEvent.getY() ); </pre>
     *
     * @param aModelElement         The model element. Can be {@code null}
     * @param aLayer                The layer containing the model element. Can be {@code null}
     * @param aMouseEvent           The mouse event
     * @param aTooltipMouseListener The tooltip mouse listener
     */
    public void updateForFoundModelElement(Object aModelElement, ILcdLayer aLayer, MouseEvent aMouseEvent, TooltipMouseListener aTooltipMouseListener);

    /**
     * Returns the sensitivity which will used by the query performed on the layers of this
     * instance
     *
     * @return the sensitivity which will used by the query performed on the layers of this
     *         instance
     */
    public int getQuerySensitivity();
  }
}
