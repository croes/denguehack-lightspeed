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
package samples.symbology.nvg.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import com.luciad.format.nvg.model.TLcdNVGStyle;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.view.ILcdView;

import samples.decoder.nvg.NVGSymbolFilter;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.nvg.common.INVGControllerModel;

/**
 * <p>Abstract Action class to create <code>TLcdNVG20Content</code> with or without Military Symbology.
 * Implementations of this abstract will be displayed in a toolbar per NVG Geometry.
 * </p>
 * <p>Whenever the user wants to create a Military Symbol, this action checks if the desired symbol
 * fits (using a {@link NVGSymbolFilter}) its geometry. If so, it becomes enabled
 * (clickable if it's in a button). If user clicks this action, it changes the current controller of the
 * view with its own controller. Thus the symbol is being created with the geometry of this action.
 * </p>
 *
 * <p>If user clicks this action before selecting a symbol, it lets user to create the <code>TLcdNVG20Content</code>
 * defined in this action without a symbol
 * </p>
 */
public abstract class ANVGCreateContentAction extends ALcdAction {
  /**
   * Default style for symbolized content
   */
  private static final TLcdNVGStyle sSYMBOL_STYLE = new TLcdNVGStyle();
  /**
   * Style for shapes without symbols
   */
  private static final TLcdNVGStyle sSHAPE_STYLE = new TLcdNVGStyle();

  static {
    sSHAPE_STYLE.setStrokeWidth(2.0f);
    sSHAPE_STYLE.setStrokeColor(Color.YELLOW);
  }

  /**
   * Action to create shapes (Rectangle, Arrow etc.)
   */
  private ALcdAction fCreateShapeAction;
  /**
   * Current behaviour of this action
   */
  private ALcdAction fCurrentAction;
  /**
   * Symbol filter for the geometry of this action
   */
  private NVGSymbolFilter fFilter;
  /**
   * Geometry of this action
   */
  private final TLcdNVG20Content fNVGContent;
  /**
   * Geometry type of the action (for filter)
   */
  private final TLcdNVG20Content.ShapeType fGeometryType;

  private final ILcdView fView;
  private final ILcdUndoableListener fUndoableListener;

  protected ANVGCreateContentAction(Builder aBuilder) {
    fView = aBuilder.fView;
    fUndoableListener = aBuilder.fUndoableListener;
    fNVGContent = aBuilder.fNVGContent;
    fGeometryType = aBuilder.fShapeType;
    fFilter = new NVGSymbolFilter();
    fFilter.setSelectedGeometryType(fGeometryType);
    fView.addPropertyChangeListener(createControllerChangeListener());
    setIcon(aBuilder.fIcon);
  }

  protected void initialize() {
    fCreateShapeAction = createShapeAction();
    fCurrentAction = fCreateShapeAction;
    putValue(ILcdAction.SHORT_DESCRIPTION, fGeometryType.name());
    setSelected(false);
  }

  /**
   * Sets the state of the action depending on the provided <code>INVGControllerModel</code>
   * This action can have states below;
   * <ul>
   * <li><code>aControllerModel</code> is created by this action and a Military Symbol is being edited. So the action is enabled and selected</li>
   * <li><code>aControllerModel</code> is not created by this action and and a Military Symbol is being edited.
   * If the edited symbol is supported by this action's geometry, it is enabled but not selected otherwise it's not enabled</li>
   * <li><code>aControllerModel</code> is created by this action and it edits a geometry without a symbol, then the action is enabled and selected</li>
   * <li><code>aControllerModel</code> is not created by this action and it edits a geometry without a symbol, then the action is enabled and but not selected</li>
   * </ul>
   * @param aControllerModel controller model to be tested for state
   */
  protected void setActionState(INVGControllerModel aControllerModel) {
    boolean controllerModelEditsSymbol = isControllerModelEditsSymbol(aControllerModel);
    boolean isOwnerOfControllerModel = isOwnerOfControllerModel(aControllerModel);
    if (controllerModelEditsSymbol) {
      if (!isOwnerOfControllerModel) {
        symbolEditedByOtherAction(aControllerModel);
      } else {
        symbolEditedByThisAction();
      }
    } else if (!isShapeCreatedByThisAction(aControllerModel)) {
      setSelected(false);
      setEnabled(true);
      fCurrentAction = fCreateShapeAction;
    } else {
      noEditing();
    }
  }

  /**
   * Checks if the edited/created symbol fits this action's geometry.
   * If so, makes this action enabled and sets its behaviour to be able
   * to create the same symbol with its geometry. Otherwise makes this action disabled.
   *
   * @param aControllerModel the controllermodel which creates/edits the symbol
   */
  private void symbolEditedByOtherAction(INVGControllerModel aControllerModel) {
    setSelected(false);
    String sidc = aControllerModel.getSIDC();
    EMilitarySymbology symbology = aControllerModel.getSymbology();
    String sidcMask = MilitarySymbolFacade.getSIDCMask(symbology, sidc);
    fFilter.setSymbology(symbology);
    if (fFilter.accept(sidcMask)) {
      setEnabled(true);
      fCurrentAction = createSymbolAction(sidcMask, symbology);
    } else {
      setEnabled(false);
    }
  }

  /**
   * This action is now editing a symbol, so it is selected and enabled
   */
  private void symbolEditedByThisAction() {
    setEnabled(true);
    setSelected(true);
  }

  /**
   * This action is ready to create a shape without a symbol
   */
  private void noEditing() {
    setEnabled(true);
    fCurrentAction = fCreateShapeAction;
  }

  private static boolean isControllerModelEditsSymbol(INVGControllerModel aControllerModel) {
    return aControllerModel != null && aControllerModel.getSIDC() != null;
  }

  private boolean isOwnerOfControllerModel(INVGControllerModel aControllerModel) {
    return isControllerModelEditsSymbol(aControllerModel) && getNVGContent().getClass().isAssignableFrom(aControllerModel.getContent().getClass());
  }

  private boolean isShapeCreatedByThisAction(INVGControllerModel aControllerModel) {
    return aControllerModel != null && aControllerModel.getContent() != null && getNVGContent().getClass().isAssignableFrom(aControllerModel.getContent().getClass());
  }

  private void setSelected(boolean aSelected) {
    putValue(ILcdAction.SELECTED_KEY, aSelected);
  }

  /**
   *
   * @return creates and returns a <code>PropertyChangeListener</code> which will listen
   * change events of provided <code>ILcdView</code> by {@link Builder}.
   */
  protected abstract PropertyChangeListener createControllerChangeListener();

  /**
   * @return an action which creates <code>TLcdNVG20Content</code> without a symbol.
   */
  protected abstract ALcdAction createShapeAction();

  /**
   * Creates and returns an action which creates a <code>TLcdNVG20SymbolizedContent</code> with provided symbol information
   * @param aHierarchyMask hierarchy mask of the symbol
   * @param aMilitarySymbology symbology of the symbol
   * @return <code>TLcdNVG20SymbolizedContent</code> with symbol
   */
  protected abstract ALcdAction createSymbolAction(String aHierarchyMask, EMilitarySymbology aMilitarySymbology);

  @Override
  public void actionPerformed(ActionEvent e) {
    if (getValue(ILcdAction.SELECTED_KEY) == Boolean.TRUE) {
      fCurrentAction.actionPerformed(e);
    }
  }

  public TLcdNVG20Content getNVGContent() {
    return fNVGContent;
  }

  protected TLcdNVG20Content getNVGContentForSymbol() {
    TLcdNVG20Content content = (TLcdNVG20Content) fNVGContent.clone();
    content.setStyle(sSYMBOL_STYLE);
    return content;
  }

  protected TLcdNVG20Content getNVGContentForShape() {
    TLcdNVG20Content content = (TLcdNVG20Content) fNVGContent.clone();
    content.setStyle(sSHAPE_STYLE);
    return content;
  }

  public ILcdView getView() {
    return fView;
  }

  public ILcdUndoableListener getUndoableListener() {
    return fUndoableListener;
  }

  /**
   * Abstract builder for the action
   * @param <T>
   */
  @SuppressWarnings("unchecked")
  public static abstract class Builder<T extends Builder> {
    private ILcdView fView;
    private ILcdUndoableListener fUndoableListener;
    private TLcdNVG20Content fNVGContent;
    private TLcdNVG20Content.ShapeType fShapeType;
    private ILcdIcon fIcon;

    protected Builder() {
    }

    protected Builder(ANVGCreateContentAction aAction) {
      fView = aAction.fView;
      fUndoableListener = aAction.fUndoableListener;
      fNVGContent = aAction.fNVGContent;
      fShapeType = aAction.fGeometryType;
    }

    public abstract ANVGCreateContentAction build();

    public T view(ILcdView aView) {
      fView = aView;
      return (T) this;
    }

    public T undoableListener(ILcdUndoableListener aUndoableListener) {
      fUndoableListener = aUndoableListener;
      return (T) this;
    }

    public T content(TLcdNVG20Content aContent) {
      fNVGContent = aContent;
      return (T) this;
    }

    public T geometryName(TLcdNVG20Content.ShapeType aShapeType) {
      fShapeType = aShapeType;
      return (T) this;
    }

    public T icon(ILcdIcon aIcon) {
      fIcon = aIcon;
      return (T) this;
    }

  }

}
