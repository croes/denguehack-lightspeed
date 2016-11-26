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
package samples.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.swing.Timer;

import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.swing.ALcdBalloonDescriptor;
import com.luciad.view.swing.ALcdBalloonManager;
import com.luciad.view.swing.TLcdModelElementBalloonDescriptor;

/**
 * Listener which listens to selection changes and when a single object is selected, it is set on
 * the balloon manager.
 *
 * Typical use:
 * <code><pre>
 * BalloonViewSelectionListener balloonListener = new BalloonViewSelectionListener( view, balloonManager );
 * view.getRootNode().addHierarchySelectionListener( balloonListener );
 * view.getRootNode().addHierarchyPropertyChangeListener( balloonListener );
 * view.getRootNode().addHierarchyLayeredListener( balloonListener );
 * </pre></code>
 */
public class BalloonViewSelectionListener implements ILcdSelectionListener, ILcdLayeredListener,
                                                     PropertyChangeListener {
  /**
   * Use a delay to show the balloon
   */
  private Timer fTimer;
  private ILcdView fView;
  private ALcdBalloonManager fBalloonManager;
  private ALcdBalloonDescriptor fLastDescriptor;

  /**
   * Creates a new <code>ViewSelectionBalloonListener</code> instance.
   * @param aView the view that contains the elements that are related to this <code>ILcdSelectionListener</code>
   * @param aBalloonManager a balloon manager to notify when an element on the view has been set.
   */
  public BalloonViewSelectionListener(ILcdView aView, ALcdBalloonManager aBalloonManager) {
    if (aView == null || aBalloonManager == null) {
      throw new IllegalArgumentException("Arguments of ViewSelectionBalloonListener must not be null");
    }

    fView = aView;
    fBalloonManager = aBalloonManager;
    //use a delay of 150 ms
    fTimer = new Timer(150, new MyActionListener());
    fTimer.setRepeats(false);
  }

  /**
   * Triggers an update for the current balloon when a change in selection is triggered.
   * @param aSelectionEvent a <code>TLcdSelectionChangedEvent</code> detailing the changes
   */
  public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
    notifyUpdate();
  }

  /**
   *  Triggers an update for the current balloon when a layer was added or removed.
   * @param e a <code>TLcdLayeredEvent</code>
   */
  public void layeredStateChanged(TLcdLayeredEvent e) {
    if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED || e.getID() == TLcdLayeredEvent.LAYER_ADDED) {
      notifyUpdate();
    }
  }

  public void propertyChange(PropertyChangeEvent evt) {
    if ("visible".equals(evt.getPropertyName())) {
      notifyUpdate();
    }
  }

  /**
   * Method that is called when the state of the balloon should be updated. It will execute the
   * update with a small delay, allowing new calls to this method to delay the update.
   */
  private void notifyUpdate() {
    if (fBalloonManager != null) {
      fLastDescriptor = fBalloonManager.getBalloonDescriptor();
    }
    if (fTimer.isRunning()) {
      fTimer.restart();
    } else {
      fTimer.start();
    }
  }

  /**
   * Listener which shows a balloon as only one object is selected on the view. The listener will be
   * triggered when the timer ends.
   */
  private class MyActionListener implements ActionListener {
    private Object fSelectedObject;
    private ILcdLayer fLayer;

    public void actionPerformed(ActionEvent aActionEvent) {
      if (fBalloonManager != null &&
          !(isNewDescriptorSet(fBalloonManager))) {
        calculateSelectedObject();
        if (fSelectedObject != null &&
            fLayer != null &&
            fLayer.isVisible()) {
          fBalloonManager.setBalloonDescriptor(new TLcdModelElementBalloonDescriptor(fSelectedObject, fLayer.getModel(), fLayer));
        } else {
          fBalloonManager.setBalloonDescriptor(null);
        }
      }
      //clear the fields
      fSelectedObject = null;
      fLayer = null;
      fLastDescriptor = null;
    }

    private boolean isNewDescriptorSet(ALcdBalloonManager aBalloonManager) {
      ALcdBalloonDescriptor descriptor = aBalloonManager.getBalloonDescriptor();
      if (fLastDescriptor == null) {
        return !(descriptor == null);
      } else {
        return descriptor == null || fLastDescriptor != descriptor;
      }
    }

    private void calculateSelectedObject() {
      //clear the fields
      fSelectedObject = null;
      fLayer = null;

      if (fView instanceof ILcdLayered) {
        //loop over all layers
        Enumeration layers = ((ILcdLayered) fView).layers();
        boolean mayStopSearching = false;
        while (layers.hasMoreElements() && !mayStopSearching) {
          ILcdLayer layer = (ILcdLayer) layers.nextElement();
          Enumeration enumeration = layer.selectedObjects();
          while (enumeration.hasMoreElements() && !mayStopSearching) {
            Object selectedObject = enumeration.nextElement();
            if (fSelectedObject == null) {
              fLayer = layer;
              fSelectedObject = selectedObject;
            } else {
              //another selected object found, stop this thing and clear the fields
              mayStopSearching = true;
              fLayer = null;
              fSelectedObject = null;
            }
          }
        }
      }
    }
  }
}
