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
package samples.lucy.util;

import static samples.lucy.util.TransferStyleUtil.transferStyledLayerStyle;

import java.awt.Component;
import java.awt.Cursor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyMapComponent;
import com.luciad.lucy.map.TLcyMapComponentFactory;
import com.luciad.lucy.map.action.TLcyCreateGXYLayerAction;
import com.luciad.lucy.map.action.TLcyGXYSetControllerActiveSettable;
import com.luciad.lucy.map.action.lightspeed.TLcyLspCreateLayerAction;
import com.luciad.lucy.map.action.lightspeed.TLcyLspSetControllerActiveSettable;
import com.luciad.lucy.map.lightspeed.TLcyLspCompositeLayerFactory;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponentFactory;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;

/**
 * Utility class for layer related methods.
 */
public class LayerUtil {

  private static final MessageFormat FORMAT = new MessageFormat(TLcyLang.getString("Make {0} editable?"));

  public static final String NEW_LAYER_ACTION = "newLayerAction";
  private static final String NEW_SHAPE_ACTIVE_SETTABLE = "newActiveSettable";

  private LayerUtil() {
  }

  /**
   * Returns true if all layer of the given collection can be edited. If (some) layers are not
   * editable before this method is called, the user is asked if it is ok to make them editable.
   * If the user does not agree, at least one layer is not editable and false is returned.  If
   * the user does agree, all layers are made editable and true is returned.
   *
   * @param aLayers The collection of layers.
   * @param aParentComponent The parent component for dialogs.
   * @return True if all layers of the given collection can be edited.
   */
  public static boolean canEdit(Collection<? extends ILcdLayer> aLayers, Component aParentComponent) {
    Collection<ILcdLayer> uneditableLayers = getUneditableLayers(aLayers);
    if (uneditableLayers == null) {
      return false;
    }
    if (uneditableLayers.isEmpty()) {
      return true;
    }
    if (uneditableLayers.size() == 1) {
      return canEdit(uneditableLayers.iterator().next(), aParentComponent);
    }
    int result = JOptionPane.showConfirmDialog(aParentComponent,
                                               TLcyLang.getString("Make layers editable?"),
                                               TLcyLang.getString("Two or more target layers are not editable"),
                                               JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      for (ILcdLayer uneditableLayer : uneditableLayers) {
        uneditableLayer.setEditable(true);
      }
      return true;
    }
    return false;
  }

  private static Collection<ILcdLayer> getUneditableLayers(Collection<? extends ILcdLayer> aLayers) {
    List<ILcdLayer> result = new ArrayList<ILcdLayer>();
    for (ILcdLayer aLayer : aLayers) {
      if (!aLayer.isEditableSupported()) {
        return null;
      }
      if (!aLayer.isEditable()) {
        result.add(aLayer);
      }
    }
    return result;
  }

  /**
   * @see #canEdit(Collection, Component)
   */
  public static boolean canEdit(ILcdLayer aLayer, Component aParentComponent) {
    if (aLayer == null) {
      return false;
    }
    if (!aLayer.isEditableSupported()) {
      return false;
    }
    if (aLayer.isEditable()) {
      return true;
    }
    int result = JOptionPane.showConfirmDialog(aParentComponent,
                                               FORMAT.format(new Object[]{aLayer.getLabel()}),
                                               TLcyLang.getString("Target layer is not editable"),
                                               JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      aLayer.setEditable(true);
      return true;
    }
    return false;
  }

  /**
   * Creates a {@code TLcyCreateGXYLayerAction} and inserts it in all configured action bars.
   * Such an action is typically configured to be shown in the ALcyFormatBar of the format.
   * The property keys should be prefixed with {@code aPrefix} + {@code "newLayerAction"}. For example:
   * <pre>MyAddOn.MyFormat.newLayerAction.myFormatBar.insert=true</pre>
   *
   * @param aConfiguration the properties to parse the action bar configuration from.
   * @param aPrefix        the prefix to use for configuring the new layer action. Typically this
   *                       is the format's short prefix.
   * @param aFormat        the format for which these actions should be created.
   *                       If the format exposes a number of {@code ALcyDefaultModelDescriptorFactory}
   *                       instances, only the first one is used.
   * @param aMapComponent  the map component for which the action should be created.
   *
   * @return the action that was inserted.
   * @see TLcyActionBarUtil#insertInConfiguredActionBars
   */
  public static TLcyCreateGXYLayerAction insertCreateLayerAction(ALcyProperties aConfiguration,
                                                                 String aPrefix,
                                                                 ALcyFormat aFormat,
                                                                 ILcyMapComponent aMapComponent) {
    // Create the action
    TLcyCreateGXYLayerAction action = new TLcyCreateGXYLayerAction(aFormat.getLucyEnv(), aMapComponent);
    if (aFormat.getDefaultModelDescriptorFactories().length != 0) {
      action.setDefaultModelDescriptorFactory(aFormat.getDefaultModelDescriptorFactories()[0]);
    }
    action.addUndoableListener(aFormat.getLucyEnv().getUndoManager());
    action.putValue(TLcyActionBarUtil.ID_KEY, (aPrefix + NEW_LAYER_ACTION));

    // Insert the action
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        aFormat.getLucyEnv().getUserInterfaceManager().getActionBarManager(),
        aConfiguration);
    return action;
  }

  /**
   * Creates a {@code TLcyLspCreateLayerAction} and inserts it in all configured action bars.
   * Such an action is typically configured to be shown in the ALcyFormatBar of the format.
   * The property keys should be prefixed with {@code aPrefix} + {@code "newLayerAction"}. For example:
   * <pre>MyAddOn.MyFormat.newLayerAction.myFormatBar.insert=true</pre>
   *
   * @param aConfiguration the properties to parse the action bar configuration from.
   * @param aPrefix        the prefix to use for configuring the new layer action. Typically this
   *                       is the format's short prefix.
   * @param aFormat        the format for which these actions should be created.
   *                       If the format exposes a number of
   *                       {@link com.luciad.lucy.model.ALcyDefaultModelDescriptorFactory}
   *                       instances, only the first one is used.
   * @param aMapComponent  the map component for which the action should be created.
   *
   * @return the action that was inserted.
   * @see TLcyActionBarUtil#insertInConfiguredActionBars
   */
  public static TLcyLspCreateLayerAction insertCreateLayerAction(ALcyProperties aConfiguration,
                                                                 String aPrefix,
                                                                 ALcyFormat aFormat,
                                                                 ILcyGenericMapComponent<ILspView, ILspLayer> aMapComponent) {
    // Create the action
    TLcyLspCreateLayerAction action = new TLcyLspCreateLayerAction(aFormat.getLucyEnv(), aMapComponent);
    if (aFormat.getDefaultModelDescriptorFactories().length != 0) {
      action.setDefaultModelDescriptorFactory(aFormat.getDefaultModelDescriptorFactories()[0]);
    }
    action.addUndoableListener(aFormat.getLucyEnv().getUndoManager());
    action.putValue(TLcyActionBarUtil.ID_KEY, (aPrefix + NEW_LAYER_ACTION));

    // Insert the action
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        aFormat.getLucyEnv().getUserInterfaceManager().getActionBarManager(),
        aConfiguration);
    return action;
  }

  /**
   * Creates a {@code TLcyLspSetControllerActiveSettable} and inserts it in all configured action bars.
   * Such an action is typically configured to be shown in the ALcyFormatBar of the format.
   * The property keys should be prefixed with {@code aPrefix} + {@code "newActiveSettable"}. For example:
   * <pre>MyAddOn.MyFormat.newActiveSettable.myFormatBar.insert=true</pre>
   *
   * @param aConfiguration   the properties to parse the menu item configuration from.
   * @param aPrefix          the prefix to use for configuring the action. Typically this
   *                         is the format's short prefix.
   * @param aLucyEnv         the Lucy back-end
   * @param aMapComponent    the map component for which the active settable should be created.
   * @param aControllerModel the controller model that creates the new shape
   * @return the action that was inserted.
   * @see TLcyActionBarUtil#insertInConfiguredActionBars
   */
  public static TLcyLspSetControllerActiveSettable insertCreateShapeActiveSettable(ALcyProperties aConfiguration,
                                                                                   String aPrefix,
                                                                                   ILcyLucyEnv aLucyEnv,
                                                                                   TLcyLspMapComponent aMapComponent,
                                                                                   ALspCreateControllerModel aControllerModel) {
    // Create the controller
    TLspCreateController newController = new TLspCreateController(aControllerModel);
    newController.setStringTranslator(TLcyLang.getStringTranslator());
    newController.addUndoableListener(aLucyEnv.getUndoManager());
    TLspSetControllerAction editAction = new TLspSetControllerAction(
        aMapComponent.getMainView(),
        aMapComponent.retrieveAvailableController(TLcyLspMapComponentFactory.SELECTION_PAN_CONTROLLER));
    newController.setActionToTriggerAfterCommit(editAction);

    // Create the active settable
    TLcyLspSetControllerActiveSettable activeSettable = new TLcyLspSetControllerActiveSettable(
        newController, aMapComponent.getMainView(), aLucyEnv);
    activeSettable.putValue(TLcyActionBarUtil.ID_KEY, aPrefix + NEW_SHAPE_ACTIVE_SETTABLE);

    // Insert the active settable
    TLcyActionBarUtil.insertInConfiguredActionBars(
        activeSettable,
        aMapComponent,
        aLucyEnv.getUserInterfaceManager().getActionBarManager(),
        aConfiguration, false);
    return activeSettable;
  }

  /**
   * Creates a {@code TLcyGXYSetControllerActiveSettable} and inserts it in all configured action bars.
   * Such an action is typically configured to be shown in the ALcyFormatBar of the format.
   * The property keys should be prefixed with {@code aPrefix} + {@code "newActiveSettable"}. For example:
   * <pre>MyAddOn.MyFormat.newActiveSettable.myFormatBar.insert=true</pre>
   *
   * @param aConfiguration   the properties to parse the menu item configuration from.
   * @param aPrefix          the prefix to use for configuring the action. Typically this
   *                         is the format's short prefix.
   * @param aLucyEnv         the Lucy back-end
   * @param aMapComponent    the map component for which the active settable should be created.
   * @param aControllerModel the controller model that creates the new shape
   * @return the action that was inserted.
   * @see TLcyActionBarUtil#insertInConfiguredActionBars
   */
  public static TLcyGXYSetControllerActiveSettable insertCreateShapeActiveSettable(ALcyProperties aConfiguration,
                                                                                   String aPrefix,
                                                                                   ILcyLucyEnv aLucyEnv,
                                                                                   ILcyMapComponent aMapComponent,
                                                                                   ALcdGXYNewControllerModel2 aControllerModel) {
    // Create the controller
    TLcdGXYNewController2 newController = new TLcdGXYNewController2(aControllerModel);
    newController.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    newController.addUndoableListener(aLucyEnv.getUndoManager());
    TLcdGXYSetControllerAction editAction = new TLcdGXYSetControllerAction(
        aMapComponent.getMainGXYView(),
        aMapComponent.retrieveAvailableGXYController(TLcyMapComponentFactory.SELECT_CONTROLLER));
    newController.setActionToTriggerAfterCommit(editAction);
    newController.setSnappables(aMapComponent.getSnapList());

    // Create the active settable
    TLcyGXYSetControllerActiveSettable activeSettable = new TLcyGXYSetControllerActiveSettable(
        newController, aMapComponent.getMainGXYView(), aLucyEnv);
    activeSettable.putValue(TLcyActionBarUtil.ID_KEY, aPrefix + NEW_SHAPE_ACTIVE_SETTABLE);

    // Insert the active settable
    TLcyActionBarManager actionBarManager = aLucyEnv.getUserInterfaceManager().getActionBarManager();
    TLcyActionBarUtil.insertInConfiguredActionBars(
        activeSettable, aMapComponent, actionBarManager,
        aConfiguration, false);
    return activeSettable;
  }

  /**
   * Copies all visible layers from one view to another.
   *
   * @param aSourceView the source view
   * @param aTargetView the target view
   * @param aLucyEnv the Lucy environment
   */
  public static void copyLayers(ILspView aSourceView, ILspView aTargetView, ILcyLucyEnv aLucyEnv) {
    Enumeration layers = aSourceView.layers();
    TLcyLspCompositeLayerFactory factory = new TLcyLspCompositeLayerFactory(aLucyEnv);

    while (layers.hasMoreElements()) {
      ILspLayer layer = (ILspLayer) layers.nextElement();
      if (!layer.isVisible(TLspPaintRepresentationState.REGULAR_BODY)) {
        return;
      }
      ILcdModel model = layer.getModel();

      ILspLayer newLayer = null;
      if (TLspLonLatGridLayerBuilder.GRID_TYPE_NAME.equals(model.getModelDescriptor().getTypeName())) {
        newLayer = TLspLonLatGridLayerBuilder.newBuilder().build();
      } else if (factory.canCreateLayers(model)) {
        Collection<ILspLayer> newLayers = factory.createLayers(model);
        newLayer = newLayers.iterator().next();
      }

      if (newLayer != null) {
        transferStyledLayerStyle(layer, newLayer, aLucyEnv);
        aTargetView.addLayer(newLayer);
      }
    }
  }

}
