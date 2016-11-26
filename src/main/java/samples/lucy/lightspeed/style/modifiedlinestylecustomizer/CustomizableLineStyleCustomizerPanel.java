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
package samples.lucy.lightspeed.style.modifiedlinestylecustomizer;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanel;
import com.luciad.lucy.map.style.lightspeed.TLcyLspCustomizableStyleContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * <p>Customizer panel for a single <code>TLcyLspCustomizableStyleContext</code> providing context for a
 * <code>TLspCustomizableStyle</code> containing a <code>TLspLineStyle</code>,
 * or for an array containing at least one of those <code>TLcyLspCustomizableStyleContext</code> instances.</p>
 */
class CustomizableLineStyleCustomizerPanel extends ALcyCustomizerPanel {

  /**
   * Filter for the objects this panel accepts:
   * to pass an object should be a context containing a customizable line style or an array containing at least one of such contexts.
   */
  static final ILcdFilter<Object> FILTER = new ILcdFilter<Object>() {
    @Override
    public boolean accept(Object aObject) {
      if (aObject == null) {
        return true;
      }

      return !getCustomizableLineStyleContexts(aObject).isEmpty();
    }
  };

  static List<TLcyLspCustomizableStyleContext> getCustomizableLineStyleContexts(Object aObject) {
    List<TLcyLspCustomizableStyleContext> result = new ArrayList<>();
    if (aObject instanceof TLcyLspCustomizableStyleContext) {
      result.add((TLcyLspCustomizableStyleContext) aObject);
    } else if (aObject instanceof Object[]) {
      Object[] array = (Object[]) aObject;
      for (Object o : array) {
        if (o instanceof TLcyLspCustomizableStyleContext &&
            TLspLineStyle.class.equals(((TLcyLspCustomizableStyleContext) o).getStyle().getStyle().getClass())) {
          result.add((TLcyLspCustomizableStyleContext) o);
        }
      }
    }
    return result;
  }

  private final WeakPropertyChangeListener fStylePropertyChangeListener;
  private final JSpinner fSpinner;
  private final ChangeListener fSpinnerChangeListener;

  public CustomizableLineStyleCustomizerPanel() {
    super(FILTER, TLcyLang.getString("Line style"));
    fStylePropertyChangeListener = new WeakPropertyChangeListener(this);

    fSpinnerChangeListener = new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        setChangesPending(true);
      }
    };

    fSpinner = createSpinner();

    initUI();
  }

  /**
   * Add all components to the panel by using the TLcyTwoColumnLayoutBuilder, which makes sure
   * all components are aligned in the same way as the other style panels
   */
  private void initUI() {
    TLcyTwoColumnLayoutBuilder builder = TLcyTwoColumnLayoutBuilder.newBuilder();
    builder.
               row().columnOne(new JLabel(TLcyLang.getString("Width")), fSpinner).
               build();
    builder.populate(this);
  }

  private JSpinner createSpinner() {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    spinner.addChangeListener(fSpinnerChangeListener);
    return spinner;
  }

  @Override
  protected boolean applyChangesImpl() {
    List<TLcyLspCustomizableStyleContext> customizableStyleContexts = getCustomizableLineStyleContexts(getObject());
    //apply the changes on all styles
    for (TLcyLspCustomizableStyleContext customizableStyleContext : customizableStyleContexts) {
      TLspCustomizableStyle customizableStyle = customizableStyleContext.getStyle();
      TLspLineStyle lineStyle = (TLspLineStyle) customizableStyle.getStyle();
      float newWidth = ((SpinnerNumberModel) fSpinner.getModel()).getNumber().floatValue();
      TLspLineStyle updatedStyle = TLspLineStyle.newBuilder().all(lineStyle).width(newWidth).build();

      //remove the listener temporarily since we are updating the style, hence are not interested in the event
      customizableStyle.removePropertyChangeListener(fStylePropertyChangeListener);
      customizableStyle.setStyle(updatedStyle);
      customizableStyle.addPropertyChangeListener(fStylePropertyChangeListener);
    }
    return !customizableStyleContexts.isEmpty();
  }

  @Override
  protected void updateCustomizerPanelFromObject() {
    List<TLcyLspCustomizableStyleContext> customizableStyleContexts = getCustomizableLineStyleContexts(getObject());
    //use the first style to init the value of the spinner
    if (!customizableStyleContexts.isEmpty()) {
      TLspCustomizableStyle customizableStyle = customizableStyleContexts.get(0).getStyle();
      TLspLineStyle lineStyle = (TLspLineStyle) customizableStyle.getStyle();
      fSpinner.removeChangeListener(fSpinnerChangeListener);
      fSpinner.getModel().setValue(new Float(lineStyle.getWidth()).intValue());
      fSpinner.addChangeListener(fSpinnerChangeListener);
    }
  }

  @Override
  public void setObject(Object aObject) {
    //clean up the listeners we have added
    List<TLcyLspCustomizableStyleContext> originalStyleContexts = getCustomizableLineStyleContexts(getObject());
    for (TLcyLspCustomizableStyleContext customizableStyleContext : originalStyleContexts) {
      customizableStyleContext.getStyle().removePropertyChangeListener(fStylePropertyChangeListener);
    }

    super.setObject(aObject);

    //add listeners to track external changes made to the style
    List<TLcyLspCustomizableStyleContext> customizableStyleContexts = getCustomizableLineStyleContexts(getObject());
    for (TLcyLspCustomizableStyleContext customizableStyleContext : customizableStyleContexts) {
      customizableStyleContext.getStyle().addPropertyChangeListener(fStylePropertyChangeListener);
    }
  }

  /**
   * PropertyChangeListener implementation which updates the customizer panel when the <code>TLspCustomizableStyle</code>
   * changes. Listener does not keep a strong reference to the <code>CustomizableLineStyleCustomizerPanel</code>,
   * and will remove itself when the corresponding customizer panel no longer exists.
   */
  private static class WeakPropertyChangeListener extends
                                                  ALcdWeakPropertyChangeListener<CustomizableLineStyleCustomizerPanel> {

    private WeakPropertyChangeListener(CustomizableLineStyleCustomizerPanel aPanel) {
      super(aPanel);
    }

    @Override
    protected void propertyChangeImpl(CustomizableLineStyleCustomizerPanel aPanel, PropertyChangeEvent aPropertyChangeEvent) {
      if ("style".equals(aPropertyChangeEvent.getPropertyName())) {
        aPanel.updateCustomizerPanelFromObject();
      }
    }
  }
}
