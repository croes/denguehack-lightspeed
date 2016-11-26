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

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdUndoableEvent;
import samples.common.search.TextFieldComboBox;
import samples.common.undo.DataPropertyValueUndoable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;

/**
 * Builder for creating configuration panels that consist of multiple options, typically checkboxes, radio buttons
 * and/or toggle buttons. This builder is suited for the displaying of {@code ILcdDataObject}'s, and
 * can automatically synchronize between the data object and the UI.
 * <p/>
 * The populate() method of the builder returns an object holder which can be used to set and get the object that
 * is to be configured by the panel.
 */
public class DataObjectOptionsPanelBuilder {

  private OptionsPanelBuilder fOptionsPanelBuilder = OptionsPanelBuilder.newInstance();
  private PanelBuilder fCurrentPanelBuilder;

  // Data object support
  private final TLcdDataType fDataType;
  private ILcdDataObject fDataObject;

  private ILcdUndoableListener fUndoableListener;
  private DataObjectHolder fHolder;

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();
  private final ILcdModelListener fModelListener = new ModelListener();
  private final List<Updater> fUiUpdaters = new ArrayList<Updater>();
  private final PropertyChangeListener fDataObjectUpdater = new DataObjectUpdater();

  /**
   * Creates a new options panel builder, configured for the given data object.
   * <p/>
   * All initial values of the options will be taken from the data object.
   *
   * @param aDataObject the data object that is configured by this panel.
   *
   * @return the builder instance.
   */
  public static DataObjectOptionsPanelBuilder newInstance(ILcdDataObject aDataObject) {
    return new DataObjectOptionsPanelBuilder(aDataObject.getDataType(), aDataObject);
  }

  /**
   * Creates a new options panel builder, configured for the given data type.
   * Only objects of this type (or subtypes hereof) may be set on the option panel afterwards.
   *
   * @param aDataType the type of the data objects that will be configured by this panel.
   *
   * @return the builder instance.
   */
  public static DataObjectOptionsPanelBuilder newInstance(TLcdDataType aDataType) {
    return new DataObjectOptionsPanelBuilder(aDataType, null);
  }

  private DataObjectOptionsPanelBuilder(TLcdDataType aDataType, ILcdDataObject aDataObject) {
    fDataType = aDataType;
    fDataObject = aDataObject;
  }

  /**
   * Final call on this builder, which populates the given component with all the panels created by this builder.
   * The returned data object holder can be used to set and get the object being configured by the UI panel(s).
   *
   * @param aComponent the component to which the panel(s) of this builder should be added.
   *
   * @return an object holder which can be used to set and get the object being configured by the panel, or null if
   *         the builder was not initialized for use with data objects.
   */
  public DataObjectHolder populate(JComponent aComponent) {
    if (fHolder != null) {
      throw new UnsupportedOperationException("The populate method can only be called once.");
    }
    buildCurrentPanel();
    OptionsPanelBuilder.Handle handle = fOptionsPanelBuilder.populate(aComponent);
    fHolder = new DataObjectHolder(handle);
    fHolder.updateUIFromDataObject();
    return fHolder;
  }

  /**
   * Adds the given change listener to be notified of any changes to the data object.
   *
   * @param aChangeListener the listener to inform of any changes to the data object
   * @return this builder
   */
  public DataObjectOptionsPanelBuilder changeListener(ILcdChangeListener aChangeListener) {
    fChangeSupport.addChangeListener(aChangeListener);
    return this;
  }

  /**
   * Adds the given undoable listener to be notified of any undoable changes to the data object
   * @param aUndoableListener the listener to be notified
   * @return this builder
   */
  public DataObjectOptionsPanelBuilder undoListener(ILcdUndoableListener aUndoableListener) {
    fUndoableListener = aUndoableListener;
    return this;
  }

  /**
   * Creates a new fixed panel (not collapsible).
   *
   * @param aTitle a title for the new panel.
   *
   * @return this builder.
   */
  public PanelBuilder fixedPanel(String aTitle) {
    buildCurrentPanel();
    return fCurrentPanelBuilder = new PanelBuilder(aTitle, false);
  }

  /**
   * Creates a new collapsible panel.
   *
   * @param aTitle a title for the new panel.
   *
   * @return this builder.
   */
  public PanelBuilder collapsiblePanel(String aTitle) {
    buildCurrentPanel();
    return fCurrentPanelBuilder = new PanelBuilder(aTitle, true);
  }

  private void buildCurrentPanel() {
    if (fCurrentPanelBuilder != null) {
      fCurrentPanelBuilder.build();
      fCurrentPanelBuilder = null;
    }
  }

  public class PanelBuilder {

    private OptionsPanelBuilder.PanelBuilder fPanelBuilder;
    private OptionBuilder fCurrentOptionBuilder;

    private PanelBuilder(String aTitle, boolean aCollapsible) {
      fPanelBuilder = aCollapsible ? fOptionsPanelBuilder.collapsiblePanel(aTitle) :
                      fOptionsPanelBuilder.fixedPanel(aTitle);
    }

    private void build() {
      buildCurrentOption();
    }

    private void buildCurrentOption() {
      if (fCurrentOptionBuilder != null) {
        fCurrentOptionBuilder.build();
        fCurrentOptionBuilder = null;
      }
    }

    /**
     * Adds a new toggle button group to the current panel, for the given data object property.
     *
     * @param aPropertyName the data object property for which to create a button group.
     *
     * @return this builder
     */
    public ButtonBuilder toggleButtonGroup(String aPropertyName) {
      buildCurrentOption();
      ButtonBuilder buttonBuilder = new ButtonBuilder(aPropertyName, OptionsPanelBuilder.ButtonType.TOGGLE_BUTTON);
      fCurrentOptionBuilder = buttonBuilder;
      return buttonBuilder;
    }

    /**
     * Adds a new radio button group to the current panel, for the given data object property.
     *
     * @param aPropertyName the data object property for which to create a button group.
     *
     * @return this builder
     */
    public ButtonBuilder radioButtonGroup(final String aPropertyName) {
      buildCurrentOption();
      ButtonBuilder buttonBuilder = new ButtonBuilder(aPropertyName, OptionsPanelBuilder.ButtonType.RADIO_BUTTON);
      fCurrentOptionBuilder = buttonBuilder;
      return buttonBuilder;
    }

    /**
     * Adds a new true/false radio button group to the current panel, for the given data object property.
     *
     * @param aPropertyName the data object property for which to create a button group.
     *
     * @return this builder
     */
    public TrueFalseButtonBuilder trueFalseRadioButtonGroup(String aPropertyName) {
      buildCurrentOption();
      TrueFalseButtonBuilder buttonBuilder = new TrueFalseButtonBuilder(aPropertyName, true);
      fCurrentOptionBuilder = buttonBuilder;
      return buttonBuilder;
    }

    /**
     * Adds a new true/false radio button group to the current panel, for the given data object property.
     *
     * @param aPropertyName the data object property for which to create a button group.
     *
     * @return this builder
     */
    public TrueFalseButtonBuilder falseTrueRadioButtonGroup(String aPropertyName) {
      buildCurrentOption();
      TrueFalseButtonBuilder buttonBuilder = new TrueFalseButtonBuilder(aPropertyName, false);
      fCurrentOptionBuilder = buttonBuilder;
      return buttonBuilder;
    }

    /**
     * Creates a new checkbox for the given ILcdDataObject property.
     *
     * @param aPropertyName the property for which to create a checkbox.
     *
     * @return this builder
     */
    public CheckBoxBuilder checkBox(final String aPropertyName) {
      buildCurrentOption();
      CheckBoxBuilder buttonBuilder = new CheckBoxBuilder(aPropertyName);
      fCurrentOptionBuilder = buttonBuilder;
      return buttonBuilder;
    }

    /**
     * Creates a new box with a drop-down list for the given ILcdDataObject property.
     *
     * @param aPropertyName the property for which to create a list box.
     *
     * @return this builder
     */
    public ComboBoxBuilder listBox(final String aPropertyName) {
      buildCurrentOption();
      ComboBoxBuilder builder = new ComboBoxBuilder(aPropertyName);
      fCurrentOptionBuilder = builder;
      return builder;
    }

    /**
     * Creates a text field for the given ILcdDataObject property.
     *
     * @param aPropertyName the property for which to create a text field.
     *
     * @return this builder
     */
    public TextFieldBuilder textField(final String aPropertyName) {
      buildCurrentOption();
      TextFieldBuilder builder = new TextFieldBuilder(aPropertyName);
      fCurrentOptionBuilder = builder;
      return builder;
    }

    /**
     * Adds the given component to this panel.
     *
     * @param aComponent a component to add to this panel.
     *
     * @return this builder
     */
    public PanelBuilder component(JComponent aComponent) {
      buildCurrentOption();
      fPanelBuilder.component(aComponent);
      return this;
    }

    /**
     * Returns the delegate panel builder.
     * Should never be called when an option is being built.
     *
     * @return the delegate panel builder
     */
    public OptionsPanelBuilder.PanelBuilder getPanelBuilder() {
      buildCurrentOption();
      return fPanelBuilder;
    }

    public abstract class OptionBuilder {

      String fPropertyName;
      String fTooltip;
      String fHint;
      PropertyChangeListener[] fPropertyChangeListeners;
      Collection<?> fValues;
      Collection<String> fDescriptions;

      OptionBuilder(String aPropertyName) {
        fPropertyName = aPropertyName;
      }

      public OptionBuilder tooltip(String aTooltip) {
        fTooltip = aTooltip;
        return this;
      }

      public OptionBuilder hint(String aHint) {
        fHint = aHint;
        return this;
      }

      public OptionBuilder listeners(PropertyChangeListener... aPropertyChangeListeners) {
        fPropertyChangeListeners = aPropertyChangeListeners;
        return this;
      }

      /**
       * Manually overrides the possible values, instead of trying to obtain them from the data type.
       * @param aValues a collection of possible values
       * @return this builder
       */
      public OptionBuilder possibleValues(Collection<String> aValues) {
        possibleValues(aValues, aValues);
        return this;
      }

      /**
       * Manually overrides the possible values, instead of trying to obtain them from the data type.
       * @param aValues a collection of possible values
       * @param aDescriptions the String representation of the possible values
       * @return this builder
       */
      public OptionBuilder possibleValues(Collection<?> aValues, Collection<String> aDescriptions) {
        fValues = aValues;
        fDescriptions = aDescriptions;
        return this;
      }

      abstract void build();

      /**
       * Adds a new toggle button group to the current panel, for the given data object property.
       *
       * @param aPropertyName the data object property for which to create a button group.
       *
       * @return this builder
       */
      public ButtonBuilder toggleButtonGroup(String aPropertyName) {
        return PanelBuilder.this.toggleButtonGroup(aPropertyName);
      }

      /**
       * Adds a new radio button group to the current panel, for the given data object property.
       *
       * @param aPropertyName the data object property for which to create a button group.
       *
       * @return this builder
       */
      public ButtonBuilder radioButtonGroup(final String aPropertyName) {
        return PanelBuilder.this.radioButtonGroup(aPropertyName);

      }

      /**
       * Adds a new true/false radio button group to the current panel, for the given data object property.
       *
       * @param aPropertyName the data object property for which to create a button group.
       *
       * @return this builder
       */
      public TrueFalseButtonBuilder trueFalseRadioButtonGroup(String aPropertyName) {
        return PanelBuilder.this.trueFalseRadioButtonGroup(aPropertyName);

      }

      /**
       * Adds a new true/false radio button group to the current panel, for the given data object property.
       *
       * @param aPropertyName the data object property for which to create a button group.
       *
       * @return this builder
       */
      public TrueFalseButtonBuilder falseTrueRadioButtonGroup(String aPropertyName) {
        return PanelBuilder.this.falseTrueRadioButtonGroup(aPropertyName);
      }

      /**
       * Creates a new checkbox for the given ILcdDataObject property.
       *
       * @param aPropertyName the property for which to create a checkbox.
       *
       * @return this builder
       */
      public CheckBoxBuilder checkBox(final String aPropertyName) {
        return PanelBuilder.this.checkBox(aPropertyName);
      }

      /**
       * Creates a new box with a drop-down list for the given ILcdDataObject property.
       *
       * @param aPropertyName the property for which to create a box.
       *
       * @return this builder
       */
      public ComboBoxBuilder listBox(String aPropertyName) {
        return PanelBuilder.this.listBox(aPropertyName);
      }

      /**
       * Creates a new text field for the given ILcdDataObject property.
       *
       * @param aPropertyName the property for which to create a field.
       *
       * @return this builder
       */
      public TextFieldBuilder textField(String aPropertyName) {
        return PanelBuilder.this.textField(aPropertyName);
      }

      /**
       * Adds the given component to this panel.
       *
       * @param aComponent a component to add to this panel.
       *
       * @return this builder
       */
      public PanelBuilder component(JComponent aComponent) {
        return PanelBuilder.this.component(aComponent);
      }
    }

    public class TrueFalseButtonBuilder extends OptionBuilder {

      private String fTrueValue;
      private String fFalseValue;
      private boolean fLeftValue;

      TrueFalseButtonBuilder(String aPropertyName, boolean aLeftValue) {
        super(aPropertyName);
        fLeftValue = aLeftValue;
      }

      public TrueFalseButtonBuilder trueValue(String aTrueValue) {
        fTrueValue = aTrueValue;
        return this;
      }

      public TrueFalseButtonBuilder falseValue(String aFalseValue) {
        fFalseValue = aFalseValue;
        return this;
      }

      void build() {
        final AbstractButton[] buttons = fPanelBuilder.addButtonGroup(OptionsPanelBuilder.ButtonType.RADIO_BUTTON,
                                                                      fPropertyName,
                                                                      fDataType.getProperty(fPropertyName).getDisplayName(),
                                                                      new Object[]{fLeftValue, !fLeftValue},
                                                                      Collections.<AbstractButton>emptyList(),
                                                                      fLeftValue ? new String[]{fTrueValue, fFalseValue} : new String[]{fFalseValue, fTrueValue},
                                                                      fLeftValue,
                                                                      fTooltip,
                                                                      fHint,
                                                                      addDataObjectListener(fPropertyChangeListeners));
        fUiUpdaters.add(new Updater() {
          @Override
          public void update() {
            if (fDataObject != null) {
              buttons[fLeftValue == (Boolean) fDataObject.getValue(fPropertyName) ? 0 : 1].setSelected(true);
            }
          }
        });
      }
    }

    public class ButtonBuilder extends OptionBuilder {

      private OptionsPanelBuilder.ButtonType fButtonType;
      private ButtonUpdater fButtonUpdater;
      private final List<AbstractButton> fButtons = new ArrayList<>();

      ButtonBuilder(String aPropertyName,
                    OptionsPanelBuilder.ButtonType aButtonType) {
        super(aPropertyName);
        fButtonType = aButtonType;
      }

      /**
       * Registers a button updater. This gives a developer a hook to update the buttons created by this builder.
       * <p>Do note that this updater does NOT apply to the buttons added through the method {@link #appendButton(AbstractButton)}
       * since those are intended to have a completely custom behavior.</p>
       *
       * @param aButtonUpdater the button updater
       * @return the button builder instance
       */
      public ButtonBuilder buttonEnabler(ButtonUpdater aButtonUpdater) {
        fButtonUpdater = aButtonUpdater;
        return this;
      }

      /**
       * Appends a button to the list of buttons representing the available options. This allows to add a custom button
       * who's purpose is not directly setting the value of a {@link ILcdDataObject} (e.g. for opening a dialog).
       *
       * @param  aButton the button to append
       * @return the button builder instance
       */
      public ButtonBuilder appendButton(AbstractButton aButton) {
        fButtons.add(aButton);
        return this;
      }

      void build() {
        TLcdDataProperty property = fDataType.getProperty(fPropertyName);
        TLcdDataType propertyType = property.getType();
        Set valuesSet = propertyType.getPossibleValues();
        final Object[] values = new Object[valuesSet.size()];
        String[] descriptions = new String[valuesSet.size()];
        int i = 0;
        for (Object value : valuesSet) {
          values[i] = value;
          descriptions[i++] = propertyType.getDisplayName(value);
        }
        final AbstractButton[] buttons = fPanelBuilder.addButtonGroup(fButtonType,
                                                                      fPropertyName,
                                                                      property.getDisplayName(),
                                                                      values,
                                                                      fButtons,
                                                                      descriptions,
                                                                      propertyType.getPossibleValues().iterator().next(),
                                                                      fTooltip,
                                                                      fHint,
                                                                      addDataObjectListener(fPropertyChangeListeners));
        if (fButtonUpdater != null) {
          fButtonUpdater.install(buttons);
        }
        fUiUpdaters.add(new Updater() {
          @Override
          public void update() {
            if (fDataObject != null) {
              Object newValue = fDataObject.getValue(fPropertyName);
              for (int i = 0; i < values.length; i++) {
                if (values[i].equals(newValue)) {
                  buttons[i].setSelected(true);
                }
              }
            }

            if (fButtonUpdater != null) {
              fButtonUpdater.update();
            }
          }
        });
      }

    }

    public class ComboBoxBuilder extends OptionBuilder {

      public ComboBoxBuilder(String aPropertyName) {
        super(aPropertyName);
      }

      @Override
      void build() {
        TLcdDataProperty property = fDataType.getProperty(fPropertyName);
        if (property == null) {
          throw new IllegalArgumentException("Property not found " + fPropertyName);
        }
        TLcdDataType propertyType = property.getType();

        final Object[] values;
        final String[] descriptions;
        if (fValues != null) {
          values = fValues.toArray(new Object[fValues.size()]);
          descriptions = fDescriptions.toArray(new String[fDescriptions.size()]);
        } else {
          Set valuesSet = propertyType.getPossibleValues();
          values = new Object[valuesSet.size()];
          descriptions = new String[valuesSet.size()];
          int i = 0;
          for (Object value : valuesSet) {
            values[i] = value;
            descriptions[i++] = propertyType.getDisplayName(value);
          }
        }
        final TextFieldComboBox box = fPanelBuilder.addListBox(fPropertyName,
                                                               property.getDisplayName(),
                                                               fDataObject.getValue(fPropertyName),
                                                               fTooltip,
                                                               values,
                                                               descriptions,
                                                               fHint,
                                                               addDataObjectListener(fPropertyChangeListeners));

        fUiUpdaters.add(new Updater() {
          @Override
          public void update() {
            if (fDataObject != null) {
              List<Object> list = Arrays.asList(values);
              Object value = fDataObject.getValue(fPropertyName);
              if (list.contains(value)) {
                box.setText(descriptions[list.indexOf(value)]);
              }
              box.setEnabled(list.size() > 1);
              box.setEnablePopup(list.size() > 1);
            } else {
              box.setEnabled(false);
              box.setEnablePopup(false);
            }
          }
        });
      }
    }

    public class TextFieldBuilder extends OptionBuilder {

      public TextFieldBuilder(String aPropertyName) {
        super(aPropertyName);
      }

      @Override
      void build() {
        TLcdDataProperty property = fDataType.getProperty(fPropertyName);
        if (property == null) {
          throw new IllegalArgumentException("Property not found " + fPropertyName);
        }
        final JTextField field = fPanelBuilder.addTextField(fPropertyName,
                                                            property.getDisplayName(),
                                                            (String) fDataObject.getValue(fPropertyName),
                                                            fTooltip,
                                                            addDataObjectListener(fPropertyChangeListeners));

        fUiUpdaters.add(new Updater() {
          @Override
          public void update() {
            if (fDataObject != null) {
              field.setText((String) fDataObject.getValue(fPropertyName));
              field.setEnabled(true);
            } else {
              field.setEnabled(false);
            }
          }
        });
      }
    }

    public class CheckBoxBuilder extends OptionBuilder {

      CheckBoxBuilder(String aPropertyName) {
        super(aPropertyName);
      }

      void build() {
        final JCheckBox checkBox = fPanelBuilder.addCheckBox(fPropertyName,
                                                             fDataType.getProperty(fPropertyName).getDisplayName(),
                                                             false,
                                                             fTooltip,
                                                             fHint,
                                                             addDataObjectListener(fPropertyChangeListeners));
        fUiUpdaters.add(new Updater() {
          @Override
          public void update() {
            if (fDataObject != null) {
              checkBox.setSelected((Boolean) fDataObject.getValue(fPropertyName));
            }
          }
        });
      }
    }

  }

  private PropertyChangeListener[] addDataObjectListener(PropertyChangeListener[] aPropertyChangeListener) {
    PropertyChangeListener[] listeners = new PropertyChangeListener[aPropertyChangeListener != null ? aPropertyChangeListener.length + 1 : 1];
    listeners[0] = fDataObjectUpdater;
    if (aPropertyChangeListener != null) {
      System.arraycopy(aPropertyChangeListener, 0, listeners, 1, aPropertyChangeListener.length);
    }
    return listeners;
  }

  /**
   * A holder for the data object configured by the UI panels, created by this builder.
   */
  public class DataObjectHolder {

    private OptionsPanelBuilder.Handle fHandle;
    private boolean fEnabled = true;
    private ILcdModel fModel;
    private Object fModelElement;

    private DataObjectHolder(OptionsPanelBuilder.Handle aHandle) {
      fHandle = aHandle;
    }

    /**
     * Returns the currently active data object.
     *
     * @return the currently active data object.
     */
    public ILcdDataObject getDataObject() {
      return fDataObject;
    }

    /**
     * Sets the data object to be visualized by this option panel(s).
     * Setting the object updates all fields in the panel(s). Once the data object is set, it will automatically be
     * updated from the UI panels.
     *
     * @param aDataObject the object to link the UI panels with.
     */
    public void setDataObject(ILcdDataObject aDataObject) {
      fDataObject = aDataObject;
      updateUIFromDataObject();
    }

    /**
     * Sets the data object, linked to an element contained in a model, to be visualized by this option panel(s).
     * Setting the object updates all fields in the panel(s). Once the data object is set, it will automatically be
     * updated from the UI panels.
     *
     * @param aDataObject the object to link the UI panels with.
     * @param aModel      the model to fire model change events for
     * @param aModelElement the domain object linked to the data object
     */
    public void setDataObject(ILcdModel aModel, Object aModelElement, ILcdDataObject aDataObject) {
      if (fModel != null) {
        fModel.removeModelListener(fModelListener);
      }
      fModel = aModel;
      fModelElement = aModelElement;
      fDataObject = aDataObject;
      updateUIFromDataObject();
    }

    /**
     * Synchronizes the UI with the latest status of the data object that is currently set.
     */
    public void updateUIFromDataObject() {
      ILcdModel model = fHolder.fModel;
      if (model != null) {
        fModel.removeModelListener(fModelListener);
      }
      for (Updater updater : fUiUpdaters) {
        updater.update();
      }
      updateEnabled();
      if (model != null) {
        fModel.addModelListener(fModelListener);
      }
    }

    public void setEnabled(boolean aEnabled) {
      fEnabled = aEnabled;
      updateEnabled();
    }

    private void updateEnabled() {
      boolean enabled = fDataObject != null && fEnabled;
      fHandle.setEnabled(enabled);
    }
  }

  private static interface Updater {
    public void update();
  }

  private class ModelListener implements ILcdModelListener {
    @Override
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      if (aEvent.containsElement(fHolder.fModelElement)) {
        fHolder.updateUIFromDataObject();
      }
    }
  }

  private class DataObjectUpdater implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

      ILcdModel model = fHolder.fModel;

      if (model != null) {
        model.removeModelListener(fModelListener);
        try (Lock autoUnlock = writeLock(model)) {
          fDataObject.setValue(evt.getPropertyName(), evt.getNewValue());
        }
        model.elementChanged(fHolder.fModelElement, ILcdModel.FIRE_NOW);
        model.addModelListener(fModelListener);
      } else {
        fDataObject.setValue(evt.getPropertyName(), evt.getNewValue());
      }

      // undoable support
      if (fUndoableListener != null) {
        DataPropertyValueUndoable undoable = new DataPropertyValueUndoable(
            fHolder.fModel, fHolder.fModelElement, fDataObject, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        fUndoableListener.undoableHappened(new TLcdUndoableEvent(DataObjectOptionsPanelBuilder.this, undoable));
      }

      fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
    }
  }

  /**
   * Button updater that updates the buttons' state in function of the linked data object. It's up to the implementor
   * to decide what that really means. It can be used to change the enabled or visible state of buttons for instance.
   */
  public static abstract class ButtonUpdater {
    private AbstractButton[] fButtons;

    /**
     * Installs the button updater on the given buttons.
     *
     * @param aButtons the buttons who's state needs to be linked with this updater.
     */
    public void install(AbstractButton[] aButtons) {
      fButtons = aButtons;
    }

    /**
     * Update the buttons' state. The real logic is implemented in {@link #updateButtons(AbstractButton[])}. That method
     * will only be invoked if buttons are registered to this updater instance. This function will be triggered by
     * default if the linked data object has changed.
     */
    public final void update() {
      if (fButtons != null) {
        updateButtons(fButtons);
      }
    }

    /**
     * Updates the state of the buttons. It's up to the implementing subclass to implement what this means. This can
     * typically be used to update the enabled/visibility state of buttons.
     *
     * @param aButtons
     */
    public abstract void updateButtons(AbstractButton[] aButtons);
  }
}
