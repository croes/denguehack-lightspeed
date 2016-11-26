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
package samples.lucy.tableview;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;

import org.jdesktop.swingx.JXTable;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ALcyModelCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerRunnable;

/**
 * Customizer panel containing the table view. Adjust or extend {@link TableViewGUIFactory} in case
 * you want to modify the created table customizer panel
 */
public class TableViewCustomizerPanel extends ALcyModelCustomizerPanel {

  private boolean fObjectSet = false;
  private ILcyLucyEnv fLucyEnv;
  private JTable fTable;
  private TFitCenterSelectionActiveSettableSupport fFitCenterSelectionActiveSettableSupport;
  private PropertyChangeListener fTableModelListener;

  /**
   * Create a new <p>TableViewCustomizerPanel</p>. Users of this class should still set a {@link
   * TLcyModelContext} as object and the {@link
   * #setTable(JTable) table}.
   *
   * @param aModelContextFilter   The filter
   * @param aName                 The name for the customizer panel
   * @param aLucyEnv              The Lucy back-end
   * @param aTableViewLogic       Support class containing the logic to fit/center on objects
   * @param aModelContext         A model context containing the view, layer and model for which
   *                              this customizer panel is created. Only equal model context
   *                              instances can be set afterwards. The model context should be
   *                              accepted by <code>aModelContextFilter</code>. Must not be
   *                              <code>null</code>
   */
  public TableViewCustomizerPanel(ILcdFilter aModelContextFilter,
                                  String aName,
                                  ILcyLucyEnv aLucyEnv,
                                  ITableViewLogic aTableViewLogic,
                                  TLcyModelContext aModelContext) {
    super(aModelContextFilter, aName);
    fLucyEnv = aLucyEnv;
    fFitCenterSelectionActiveSettableSupport = new TFitCenterSelectionActiveSettableSupport(aTableViewLogic, aLucyEnv);
    setLayout(new BorderLayout());

    fFitCenterSelectionActiveSettableSupport.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt.getPropertyName(),
                           evt.getOldValue(),
                           evt.getNewValue());
      }
    });

    if (aModelContext != null && canSetObject(aModelContext)) {
      setObject(aModelContext);
    } else {
      throw new IllegalArgumentException("The provided model context is not valid for this customizer panel");
    }
    fTableModelListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("changesPending".equals(evt.getPropertyName())) {
          if ((Boolean) evt.getNewValue()) {
            setChangesPending(true);
          }
        }
      }
    };
  }

  /**
   * {@inheritDoc}
   *
   * <p>We only allow to set the object once. Once a non-<code>null</code> object has been set, we
   * only allow to set <code>null</code>, on which the customizer panel will be cleaned up.
   * Afterwards, it is impossible to set another object on the panel. This allows our panels to be
   * cached, and stores the state. E.g. when switching layer selection back and forth, the state of
   * the created table views will remain the same.</p>
   */
  @Override
  public boolean canSetObject(Object aObject) {
    //once an object has been set, only accept null objects or the current object
    if (fObjectSet) {
      return aObject == null || aObject.equals(getObject());
    }
    return super.canSetObject(aObject);
  }

  @Override
  public void setObject(Object aObject) {
    if (!canSetObject(aObject)) {
      throw new IllegalArgumentException("The object [" + aObject + "] cannot be set on this customizer panel. Call canSetObject before calling setObject");
    }
    //make sure we remove this as context from the actionbar manager the first time a null object is set
    if (fObjectSet && aObject == null && getObject() != null) {
      fLucyEnv.getUserInterfaceManager().getActionBarManager().disposeContext(this);
    }
    super.setObject(aObject);
    //change the boolean flag as soon as a non-null object is set
    if (aObject != null) {
      fObjectSet = true;
      initTableAndModelContext();
      ILcdLayer layer = ((TLcyModelContext) aObject).getLayer();
      if (layer != null) {
        putValue(ILcyCustomizerPanel.NAME, layer.getLabel());
        putValue(ILcyCustomizerPanel.SMALL_ICON, layer.getIcon());
        //add listener which keeps the name and icon in sync
        layer.addPropertyChangeListener(new LayerNameAndIconListener(this));
      }
    }
  }

  @Override
  protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
    //the customizer panel is created for a certain object, and we do not allow to change the object
    //nothing should happen here
  }

  @Override
  protected boolean applyChangesImpl() {
    try {
      //if the table model implements CustomizerPanelTableModel, delegate the call to the model
      final boolean[] result = new boolean[]{true};
      ILcdLayer layer = ((TLcyModelContext) getObject()).getLayer();
      if (layer instanceof ILcdGXYLayer) {
        if (fTable != null &&
            fTable.getModel() instanceof CustomizerPanelTableModel) {
          fLucyEnv.getMapManager().getAsynchronousPaintFacade().invokeAndWaitOnGXYLayer((ILcdGXYLayer) layer, new ILcdGXYAsynchronousLayerRunnable() {
            @Override
            public void run(ILcdGXYLayer aSafeGXYLayer) {
              result[0] = ((CustomizerPanelTableModel) fTable.getModel()).applyChangesOnObject();
            }
          });
        }
      } else {
        result[0] = ((CustomizerPanelTableModel) fTable.getModel()).applyChangesOnObject();
      }

      //return true for nochanges
      return result[0];
    } catch (InterruptedException e) {
      return false;
    }
  }

  /**
   * Set the table of this customizer panel. The table will not be added to the UI of the customizer
   * panel.
   *
   * @param aTable the table of this customizer panel
   */
  protected void setTable(JTable aTable) {
    fTable = aTable;
    initTableAndModelContext();
  }

  /**
   * Returns the table of this customizer panel.
   *
   * @return the table of this customizer panel.
   */
  public JTable getTable() {
    return fTable;
  }

  /**
   * Returns whether auto-fitting is active
   *
   * @return <code>true</code> when auto-fitting is active
   */
  boolean isAutoFit() {
    return fFitCenterSelectionActiveSettableSupport.isAutoFit();
  }

  /**
   * Returns whether auto-centering is active
   *
   * @return <code>true</code> when auto-centering is active
   */
  boolean isAutoCenter() {
    return fFitCenterSelectionActiveSettableSupport.isAutoCenter();
  }

  /**
   * Returns whether auto-selection synchronization is active
   *
   * @return <code>true</code> when auto-selection synchronization is active
   */
  boolean isAutoSelect() {
    return fFitCenterSelectionActiveSettableSupport.isAutoSelect();
  }

  /**
   * Set auto-centering active. Should only be used when a <code>TLcyModelContext</code> is {@link
   * #setObject(Object) set} as object on this instance, and a {@link #setTable(JTable)
   * table}
   *
   * @param aAutoFit <code>true</code> when auto-fitting must be switched on, <code>false</code>
   *                 otherwise
   */
  void setAutoFit(boolean aAutoFit) {
    fFitCenterSelectionActiveSettableSupport.setAutoFit(aAutoFit);
  }

  /**
   * Set auto-centering active. Should only be used when a <code>TLcyModelContext</code> is {@link
   * #setObject(Object) set} as object on this instance, and a {@link #setTable(JTable)
   * table}
   *
   * @param aAutoCenter <code>true</code> when auto-centering must be switched on,
   *                    <code>false</code> otherwise
   */
  void setAutoCenter(boolean aAutoCenter) {
    fFitCenterSelectionActiveSettableSupport.setAutoCenter(aAutoCenter);
  }

  /**
   * Set auto-selection synchronization active. Should only be used when a
   * <code>TLcyModelContext</code> is {@link #setObject(Object) set} as object on this instance, and
   * a {@link #setTable(JTable) table}
   *
   * @param aAutoSelect <code>true</code> when auto-selection synchronization must be switched on,
   *                    <code>false</code> otherwise
   */
  void setAutoSelect(boolean aAutoSelect) {
    fFitCenterSelectionActiveSettableSupport.setAutoSelect(aAutoSelect);
  }

  /**
   * Returns the support class for fitting, centering and selection
   *
   * @return the support class for fitting, centering and selection
   */
  TFitCenterSelectionActiveSettableSupport getFitCenterSelectionActiveSettableSupport() {
    return fFitCenterSelectionActiveSettableSupport;
  }

  private void initTableAndModelContext() {
    if (getModelContext() != null &&
        fTable instanceof JXTable) {
      fFitCenterSelectionActiveSettableSupport.setModelContextAndTable(getModelContext(), fTable);
    }
    if (fTable != null && fTable.getModel() instanceof CustomizerPanelTableModel) {
      //make sure the listener is never added twice
      ((CustomizerPanelTableModel) fTable.getModel()).removePropertyChangeListener(fTableModelListener);
      ((CustomizerPanelTableModel) fTable.getModel()).addPropertyChangeListener(fTableModelListener);
    }
  }

  @Override
  public String toString() {
    return "TableViewCustomizerPanel";
  }
}
