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
package samples.lucy.editabletables.addon;

import static samples.lucy.editabletables.model.EditableTablesDataTypes.*;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.customizer.dataproperty.TLcyDataPropertyValueCustomizerPanelFactories;
import com.luciad.lucy.gui.customizer.dataproperty.TLcyDataPropertyValueFilters;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.TLcyMapManagerEvent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdDataPropertyValueContext;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.lucy.editabletables.domain.EditableTablesPoint;
import samples.lucy.editabletables.model.EditableTablesModelDescriptor;
import samples.lucy.editabletables.view.lightspeed.EditableTablesLspLayerFactory;

/**
 * This add on illustrates how to register customizer panel factories for instances of
 * <code>TLcdDataPropertyValueContext</code>. These allow you to edit property values directly in
 * the table view.
 */
public class EditableTablesAddOn extends ALcyPreferencesAddOn {

  private TLcd2DBoundsIndexedModel fSampleModel;

  /**
   * Creates a new <code>EditableTablesAddOn</code>.
   */
  public EditableTablesAddOn() {
    super(ALcyTool.getLongPrefix(EditableTablesAddOn.class),
          ALcyTool.getShortPrefix(EditableTablesAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    // Add layer factory as services.
    aLucyEnv.addService(new EditableTablesLspLayerFactory());

    TLcyDataPropertyValueCustomizerPanelFactories factories = new TLcyDataPropertyValueCustomizerPanelFactories(aLucyEnv);

    //Register customizers for the properties of the data object.
    for (TLcdDataProperty property : DATA_TYPE.getProperties()) {
      if (!property.equals(MEASURE_PROPERTY) &&  //Measures will be handled later
          !property.equals(READ_ONLY_INT_PROPERTY) && //no customizer to make sure it is read only.
          factories.canCreateCustomizerPanelFactory(property)) {
        //The filter controls of which TLcdDataProperty the values can be edited
        //by the customizer panels created by the new factory.
        ILcdFilter<TLcdDataPropertyValueContext> filter = createFilter(property);
        // Creates a appropriate customizer panel factory for values of the passed TLcdDataProperty
        aLucyEnv.addService(factories.createCustomizerPanelFactory(property, filter));
      }
    }

    //Register customizers for the properties of the nested data object.
    for (TLcdDataProperty property : SUB_DATA_TYPE.getProperties()) {
      if (factories.canCreateCustomizerPanelFactory(property)) {
        //The filter will now accept TLcdDataPropertyValueContexts whose expression points to a property
        //of a the value of a property of the original data object (Nested values).
        ILcdFilter<TLcdDataPropertyValueContext> filter = createFilter(DATA_OBJECT_PROPERTY, property);
        // Creates a customizer for String values, a text field.
        aLucyEnv.addService(factories.createCustomizerPanelFactory(property, filter));
      }
    }

    ILcdFilter<TLcdDataPropertyValueContext> measureFilter = createFilter(MEASURE_PROPERTY);
    //Special case for the measure = specialized format needed.
    aLucyEnv.addService(factories.createTextFieldCustomizerPanelFactory(
        measureFilter, new CustomAltitudeFormat(aLucyEnv)));

    aLucyEnv.getCombinedMapManager().addMapManagerListener(new MyMapManagerListener(), true);
  }

  private ILcdFilter<TLcdDataPropertyValueContext> createFilter(TLcdDataProperty... aProperties) {
    // The table view creates TLcdDataPropertyValueContexts with expression according to the
    // TLcdDataObjectExpressionLanguage. The TLcyDataPropertyValueFilters use the same
    // expression language.
    return TLcyDataPropertyValueFilters.createFilter(DATA_TYPE, aProperties);
  }

  private class MyMapManagerListener implements ILcyGenericMapManagerListener<ILcdView, ILcdLayer> {
    @Override
    public void mapManagerChanged(final TLcyGenericMapManagerEvent aMapManagerEvent) {
      if (aMapManagerEvent.getId() == TLcyMapManagerEvent.MAP_COMPONENT_ADDED) {
        if (fSampleModel == null) {
          fSampleModel = createSampleModel();
        }
        ILcdView view = aMapManagerEvent.getMapComponent().getMainView();
        view.addModel(fSampleModel);

        ILcdLayer layer = findLayer(view);
        if (layer != null) {
          List<ILcdLayer> selectedLayersAsList = new ArrayList<ILcdLayer>();
          selectedLayersAsList.add(layer);
          aMapManagerEvent.getMapComponent().setSelectedLayersAsList(selectedLayersAsList);
          layer.selectObject(fSampleModel.elementAt(0), true, ILcdFireEventMode.FIRE_NOW);
          if (view instanceof ILspView && layer instanceof ILspLayer) {
            // fit on layer.
            ILspView lspView = (ILspView) view;
            ILspLayer lspLayer = (ILspLayer) layer;
            try {
              TLspViewNavigationUtil util = new TLspViewNavigationUtil(lspView);
              util.fit(lspLayer);
            } catch (TLcdNoBoundsException ignore) {
            } catch (TLcdOutOfBoundsException e) {
            }
          }
        }
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            showTableViewAndObjectProperties(aMapManagerEvent.getMapComponent());
          }
        });
      }
    }

    /*
     * We registered the actions that trigger the Table View and Object Properties in a virtual
     * action bar "editableTablesBar". In this method we retrieve those actions, and trigger them so
     * the Table View and Object Properties are visible on start up.
     */
    private void showTableViewAndObjectProperties(ILcyGenericMapComponent aMapComponent) {
      ILcyActionBar actionBar = getLucyEnv().getUserInterfaceManager().getActionBarManager().getActionBar(
          "editableTablesBar", aMapComponent);
      for (int i = 0; i < actionBar.getActionBarItemCount(); i++) {
        Object item = actionBar.getActionBarItem(i);
        if (item instanceof ILcdAction) {
          ((ILcdAction) item).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "action"));
        }
      }
    }

    private TLcd2DBoundsIndexedModel createSampleModel() {
      final TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
      model.setModelDescriptor(new EditableTablesModelDescriptor());
      model.setModelReference(new TLcdGeodeticReference());

      for (int index = 0; index < 10; index++) {
        model.addElement(createPoint(index), ILcdModel.NO_EVENT);
      }

      return model;
    }

    private EditableTablesPoint createPoint(int aIndex) {
      double x = Math.random() * 5.0;
      double y = 50 + Math.random() * 5.0;
      EditableTablesPoint point = new EditableTablesPoint(x, y);

      point.setValue(STRING_PROPERTY, Math.random() < 0.4 ? null : ("string " + aIndex));
      point.setValue(BOOLEAN_PROPERTY, aIndex > 8 ? null : aIndex % 3 != 0);
      point.setValue(DOUBLE_PROPERTY, Math.random() < 0.3 ? null : new Double(Math.random() * 1000));
      point.setValue(INTEGER_PROPERTY, aIndex > 2 ? aIndex : null);
      point.setValue(ENUMERATION_PROPERTY, aIndex == 5 ? null : Direction.values()[aIndex % 4]);
      point.setValue(MEASURE_PROPERTY, Math.random() < 0.3 ? null : new Double(Math.random() * 100));
      point.setValue(READ_ONLY_INT_PROPERTY, null);

      ILcdDataObject dataObject = (ILcdDataObject) point.getValue(DATA_OBJECT_PROPERTY);
      dataObject.setValue(SUB_STRING_PROPERTY, "substring" + aIndex);
      dataObject.setValue(SUB_BOOLEAN_PROPERTY, Math.random() < 0.5);
      dataObject.setValue(SUB_FLOAT_PROPERTY, aIndex * 5f);
      dataObject.setValue(SUB_SHORT_PROPERTY, (short) aIndex);
      return point;
    }

    private ILcdLayer findLayer(ILcdView aView) {
      if (aView instanceof ILcdLayered) {
        ILcdLayered view = (ILcdLayered) aView;
        for (int i = 0; i < view.layerCount(); i++) {
          ILcdLayer layer = view.getLayer(i);
          if (layer.getModel().getModelDescriptor() instanceof EditableTablesModelDescriptor) {
            return layer;
          }
        }
      }
      return null;
    }
  }

}
