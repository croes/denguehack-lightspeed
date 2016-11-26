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

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayerSubsetList;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2;

import samples.common.MetaKeyUtil;

/**
 * Adds multi-mode support to {@link TLcdGXYEditController2
 * TLcdGXYEditController2}. This allows mode-aware painters/editors to act according to the active
 * mode of the controller. Note that while the controller defines the different modes, it is the
 * painter/editor that actually defines how to paint in that mode.
 * <p/>
 * By clicking on an already selected object, the controller toggles between the available
 * {@link Mode}s. When a previously unselected object is clicked, the controller's mode is reset to
 * {@link Mode#DEFAULT DEFAULT}.
 */
public class MultiModeController extends TLcdGXYEditController2 {

  /**
   * <p> Defines the available modes of {@link MultiModeController}. There are 2 modes available:
   * <ul> <li>{@link Mode#DEFAULT DEFAULT}</li> <li>{@link
   * Mode#ROTATION ROTATION}</li> </ul> </p>
   */
  public enum Mode {
    /**
     * Default controller mode
     */
    DEFAULT,

    /**
     * Rotation controller mode
     */
    ROTATION
  }

  private Mode fMode;
  private ILcdGXYLayerSubsetList fLastSelected;
  private boolean fSelectionChanged;

  /**
   * Creates a new multi-mode controller in default mode.
   */
  public MultiModeController() {
    this(Mode.DEFAULT);
  }

  /**
   * Creates a new multi-mode controller in a given mode.
   *
   * @param aMode the mode in which to initialize this controller
   */
  public MultiModeController(Mode aMode) {
    fMode = wrapMode(aMode.ordinal());
    fLastSelected = new TLcdGXYLayerSubsetList();
    fSelectionChanged = false;

    setName("A multi-mode edit controller");
    setShortDescription("Click on a selected object to switch between regular editing mode and rotation mode");
  }

  /**
   * Returns the mode this controller is currently in.
   */
  public Mode getMode() {
    return fMode;
  }

  // toggles between the different modes
  private void toggleMode() {
    Mode newMode = wrapMode(getMode().ordinal() + 1);
    setMode(newMode);
  }

  // wraps a number around the total number of available modes
  private Mode wrapMode(int aModeNumber) {
    Mode[] modes = Mode.values();
    return modes[aModeNumber % modes.length];
  }

  // sets the mode of this controller
  private void setMode(Mode aMode) {
    fMode = aMode;
  }

  @Override
  public void mousePressed(MouseEvent me) {
    // only handle regular clicks
    if (me.isShiftDown() || me.isAltDown() || MetaKeyUtil.isCMDDown(me)) {
      super.mousePressed(me);
      return;
    }

    ILcdGXYLayerSubsetList selected = findSelected(me);
    Enumeration elements = selected.elements();

    fSelectionChanged = !elements.hasMoreElements() || allNewSelectedElements(selected);
    if (fSelectionChanged) {
      fLastSelected.removeAllElements();

      while (elements.hasMoreElements()) {
        Object element = elements.nextElement();
        fLastSelected.addElement(element, selected.retrieveGXYLayer(element));
      }

      setMode(Mode.DEFAULT);
    }

    super.mousePressed(me);
  }

  @Override
  public void mouseClicked(MouseEvent me) {
    // only handle regular clicks
    if (me.getClickCount() > 1 || me.isShiftDown() || me.isAltDown() || MetaKeyUtil.isCMDDown(me)) {
      super.mouseClicked(me);
      return;
    }

    if (!fSelectionChanged) {
      toggleMode();
      getGXYView().invalidateSelection(true, this, "Controller mode changed");
    }

    super.mouseClicked(me);
  }

  // verifies whether the newly selected elements were also selected last time
  private boolean allNewSelectedElements(ILcdGXYLayerSubsetList newSelectedElementList) {
    Enumeration newSelectedElements = newSelectedElementList.elements();

    while (newSelectedElements.hasMoreElements()) {
      Object element = newSelectedElements.nextElement();

      if (fLastSelected.contains(element)) {
        return false;
      }
    }

    return true;
  }

  // finds the selected elements
  private ILcdGXYLayerSubsetList findSelected(MouseEvent aMouseEvent) {
    ILcdGXYView aGXYView = getGXYView();
    Rectangle aSelectionBounds = new Rectangle(aMouseEvent.getPoint());

    int aMouseMode = TLcdGXYSelectControllerModel2.INPUT_MODE_POINT;
    int aSelectByWhatMode = TLcdGXYSelectControllerModel2.SELECT_BY_WHAT_BODIES_ON_CLICK;
    int aSelectByHowMode = TLcdGXYSelectControllerModel2.SELECT_HOW_FIRST_TOUCHED;

    ILcdGXYLayerSubsetList selectionCandidates = getSelectControllerModel()
        .selectionCandidates(aGXYView, aSelectionBounds, aMouseMode,
                             aMouseEvent, aSelectByWhatMode, aSelectByHowMode);

    return selectionCandidates;
  }

}
