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

import static com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2.*;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.event.MouseInputAdapter;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYPanController;

/**
 * Like a regular select/edit controller, but it pans the view if you drag a portion of the map without
 * any selectable and editable elements.
 */
public class EditPanController implements ILcdGXYController, MouseListener, MouseMotionListener {

  private static final MouseInputAdapter DUMMY_LISTENER = new MouseInputAdapter() {
  };

  private final TLcdGXYEditController2 fEditController;
  private final TLcdGXYPanController fPanController;
  private ILcdGXYController fCurrentController;
  private MouseEvent fDelayedPress;

  public EditPanController() {
    this(new TLcdGXYEditController2());
  }

  public EditPanController(TLcdGXYEditController2 aEditController) {
    fPanController = new TLcdGXYPanController();
    fPanController.setDragViewOnPan(true);
    fPanController.setDrawLineOnPan(false);
    fPanController.setCursor(aEditController.getCursor());
    fEditController = aEditController;
  }

  @Override
  public void startInteraction(ILcdGXYView aGXYView) {
    fEditController.startInteraction(aGXYView);
    fPanController.startInteraction(aGXYView);

  }

  @Override
  public void terminateInteraction(ILcdGXYView aGXYView) {
    fEditController.terminateInteraction(aGXYView);
    fPanController.terminateInteraction(aGXYView);
  }

  @Override
  public void viewRepaint(ILcdGXYView aGXYView) {
  }

  @Override
  public void paint(Graphics aGraphics) {
    fEditController.paint(aGraphics);
    fPanController.paint(aGraphics);
  }

  @Override
  public Cursor getCursor() {
    return fEditController.getCursor();
  }

  @Override
  public String getName() {
    return fEditController.getName();
  }

  @Override
  public String getShortDescription() {
    return fEditController.getShortDescription();
  }

  @Override
  public ILcdIcon getIcon() {
    return fEditController.getIcon();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    // make shift trigger rectangle selection
    if (e.isShiftDown()) {
      fCurrentController = fEditController;
    } else {
      if (aboveEditableSelectionCandidates(e)) {
        // edit controller
        fCurrentController = fEditController;
      } else {
        // we only want panning for drag events, so delay the press
        fCurrentController = null;
        fDelayedPress = e;
      }
    }
    getCurrentMouseListener().mousePressed(e);
  }

  private boolean aboveEditableSelectionCandidates(MouseEvent e) {
    ILcdGXYLayerSubsetList candidates = fEditController.getSelectControllerModel().selectionCandidates(
        fEditController.getGXYView(), new Rectangle(e.getX() - 2, e.getY() - 2, 4, 4),
        INPUT_MODE_POINT, e, SELECT_BY_WHAT_BODIES_ON_CLICK | SELECT_BY_WHAT_LABELS_ON_CLICK, SELECT_HOW_ADD);
    Enumeration layers = candidates.layers();
    boolean editableSelectionCandidates = false;
    while (layers.hasMoreElements()) {
      ILcdLayer layer = (ILcdLayer) layers.nextElement();
      if (layer.isEditable() ||
          layer instanceof ILcdGXYEditableLabelsLayer && ((ILcdGXYEditableLabelsLayer) layer).isLabelsEditable()) {
        editableSelectionCandidates = true;
      }
    }
    return editableSelectionCandidates;
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (fCurrentController == null && fDelayedPress != null) {
      // drag operation should trigger a pan
      // flush the pressed event to the pan controller
      fCurrentController = fPanController;
      getCurrentMouseListener().mousePressed(fDelayedPress);
      fDelayedPress = null;
    }
    getCurrentMouseMotionListener().mouseDragged(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (fDelayedPress != null) {
      // no drag operation, so no pan
      // flush the pressed event to the select controller to be able to deselect objects
      fCurrentController = fEditController;
      getCurrentMouseListener().mousePressed(fDelayedPress);
      fDelayedPress = null;
    }
    getCurrentMouseListener().mouseReleased(e);
    // any drag operation has finished by now, so switch back
    fCurrentController = fEditController;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    getCurrentMouseListener().mouseClicked(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    // updates the mouse cursor
    fEditController.mouseMoved(e);
  }

  private MouseListener getCurrentMouseListener() {
    if ( fCurrentController instanceof MouseListener) {
      return (MouseListener) fCurrentController;
    } else {
      return DUMMY_LISTENER;
    }

  }
  private MouseMotionListener getCurrentMouseMotionListener() {
    if ( fCurrentController instanceof MouseMotionListener) {
      return (MouseMotionListener) fCurrentController;
    } else {
      return DUMMY_LISTENER;
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    fPanController.mouseEntered(e);
    fEditController.mouseEntered(e);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    fPanController.mouseEntered(e);
    fEditController.mouseEntered(e);
  }

}
