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
package samples.lucy.lightspeed.style.newiconstyle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanel;
import com.luciad.lucy.map.style.lightspeed.TLcyLspCustomizableStyleContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

/**
 * <p>Customizer panel for a single <code>TLcyLspCustomizableStyleContext</code> providing context for a
 * <code>TLspCustomizableStyle</code> containing a
 * <code>TLspIconStyle</code> where the icon is one of a predefined list.</p>
 *
 * <p>The UI contains a combobox to select the icon from</p>
 */
class CustomizableIconStyleCustomizerPanel extends ALcyCustomizerPanel {

  static final List<String> ICON_SOURCE_NAMES = Arrays.asList(
      "images/gui/i16_eyes.gif",
      "images/icons/gui_add_item_16.png",
      "images/icons/move_up_16.png",
      "images/icons/move_up_32.png"
                                                             );

  /**
   * Filter for the objects this panel accepts:
   * to pass an object should be a context containing a customizable icon style using on of the images listed above,
   * or an array containing exactly one of such contexts.
   */
  static final ILcdFilter<Object> FILTER = new ILcdFilter<Object>() {
    @Override
    public boolean accept(Object aObject) {
      if (aObject == null) {
        return true;
      }
      //when it is an array, only accept arrays of length one
      //also accept an array containing only one instance
      if (aObject instanceof Object[] && ((Object[]) aObject).length == 1) {
        return accept(((Object[]) aObject)[0]);
      }
      return getCustomizableIconStyleContexts(aObject).size() == 1;
    }
  };

  private static List<TLcyLspCustomizableStyleContext> getCustomizableIconStyleContexts(Object aObject) {
    List<TLcyLspCustomizableStyleContext> result = new ArrayList<TLcyLspCustomizableStyleContext>();
    if (aObject instanceof TLcyLspCustomizableStyleContext) {
      result.add((TLcyLspCustomizableStyleContext) aObject);
    } else if (aObject instanceof Object[]) {
      Object[] array = (Object[]) aObject;
      for (Object o : array) {
        if (o instanceof TLcyLspCustomizableStyleContext) {
          ALspStyle style = ((TLcyLspCustomizableStyleContext) o).getStyle().getStyle();
          if (TLspIconStyle.class.equals(style.getClass())) {
            TLspIconStyle iconStyle = (TLspIconStyle) style;
            if (iconStyle.getIcon() instanceof TLcdImageIcon) {
              TLcdImageIcon imageIcon = (TLcdImageIcon) iconStyle.getIcon();
              if (imageIcon.getSourceName() != null &&
                  ICON_SOURCE_NAMES.contains(imageIcon.getSourceName())) {
                result.add((TLcyLspCustomizableStyleContext) o);
              }
            }
          }
        }
      }
    }
    return result;
  }

  private final WeakPropertyChangeListener fStylePropertyChangeListener;
  private final JComboBox fComboBox;
  private final ActionListener fComboBoxActionListener;

  public CustomizableIconStyleCustomizerPanel() {
    super(FILTER, TLcyLang.getString("Icon style"));

    fStylePropertyChangeListener = new WeakPropertyChangeListener(this);
    fComboBoxActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setChangesPending(true);
      }
    };

    //add UI elements
    JPanel contents = new JPanel(new FlowLayout());
    contents.add(new JLabel(TLcyLang.getString("Icon style") + ": "));
    fComboBox = createCombobox();
    contents.add(fComboBox);

    setLayout(new BorderLayout());
    add(contents, BorderLayout.CENTER);
  }

  private JComboBox createCombobox() {
    JComboBox result = new JComboBox();
    result.setModel(new DefaultComboBoxModel(ICON_SOURCE_NAMES.toArray(new String[ICON_SOURCE_NAMES.size()])));
    result.setEditable(false);
    result.setRenderer(new IconListCellRenderer(result.getRenderer()));

    result.addActionListener(fComboBoxActionListener);

    return result;
  }

  @Override
  protected boolean applyChangesImpl() {
    TLcyLspCustomizableStyleContext customizableStyleContext = getCustomizableStyleContext();
    if (customizableStyleContext != null) {
      TLspCustomizableStyle customizableStyle = customizableStyleContext.getStyle();
      TLspIconStyle iconStyle = (TLspIconStyle) customizableStyle.getStyle();
      TLcdImageIcon newIcon = new TLcdImageIcon(((String) fComboBox.getSelectedItem()));
      TLspIconStyle updatedStyle = TLspIconStyle.newBuilder().all(iconStyle).icon(newIcon).build();

      //remove the listener temporarily since we are updating the style, hence are not interested in the event
      customizableStyle.removePropertyChangeListener(fStylePropertyChangeListener);
      customizableStyle.setStyle(updatedStyle);
      customizableStyle.addPropertyChangeListener(fStylePropertyChangeListener);

      return true;
    }
    return false;
  }

  @Override
  protected void updateCustomizerPanelFromObject() {
    TLcyLspCustomizableStyleContext customizableStyleContext = getCustomizableStyleContext();
    if (customizableStyleContext != null) {
      TLspIconStyle iconStyle = (TLspIconStyle) customizableStyleContext.getStyle().getStyle();
      //remove the listener since we manually update the selection, for which the changesPending state should not be updated
      fComboBox.removeActionListener(fComboBoxActionListener);
      fComboBox.setSelectedItem(((TLcdImageIcon) iconStyle.getIcon()).getSourceName());
      fComboBox.addActionListener(fComboBoxActionListener);
    }
  }

  @Override
  public void setObject(Object aObject) {
    //clean up the listener we have added
    TLcyLspCustomizableStyleContext originalStyleContext = getCustomizableStyleContext();
    if (originalStyleContext != null) {
      originalStyleContext.getStyle().removePropertyChangeListener(fStylePropertyChangeListener);
    }

    super.setObject(aObject);

    //add a listener to track external changes made to the style
    TLcyLspCustomizableStyleContext customizableStyleContext = getCustomizableStyleContext();
    if (customizableStyleContext != null) {
      customizableStyleContext.getStyle().addPropertyChangeListener(fStylePropertyChangeListener);
    }
  }

  /**
   * Returns a customizable style context containing the customizable style we are currently customizing
   * @return a customizable style context containing the customizable style we are currently customizing. Can be <code>null</code>
   */
  private TLcyLspCustomizableStyleContext getCustomizableStyleContext() {
    List<TLcyLspCustomizableStyleContext> customizableIconStyleContexts = getCustomizableIconStyleContexts(getObject());
    return !customizableIconStyleContexts.isEmpty() ? customizableIconStyleContexts.get(0) : null;
  }

  /**
   * ListCellRenderer which can handle the source name of an image by converting them to a Swing icon and
   * using a delegate renderer to create a component for the Swing icon
   */
  private static class IconListCellRenderer implements ListCellRenderer {
    private final ListCellRenderer fDelegate;

    private IconListCellRenderer(ListCellRenderer aDelegate) {
      fDelegate = aDelegate;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof String && ICON_SOURCE_NAMES.contains(((String) value))) {
        return fDelegate.getListCellRendererComponent(list, new TLcdSWIcon(new TLcdImageIcon((String) value)), index, isSelected, cellHasFocus);
      }
      return fDelegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
  }

  /**
   * PropertyChangeListener implementation which updates the customizer panel when the <code>TLspCustomizableStyle</code>
   * changes. Listener does not keep a strong reference to the <code>CustomizableIconStyleCustomizerPanel</code>,
   * and will remove itself when the corresponding customizer panel no longer exists.
   */
  private static class WeakPropertyChangeListener extends
                                                  ALcdWeakPropertyChangeListener<CustomizableIconStyleCustomizerPanel> {

    private WeakPropertyChangeListener(CustomizableIconStyleCustomizerPanel aPanel) {
      super(aPanel);
    }

    @Override
    protected void propertyChangeImpl(CustomizableIconStyleCustomizerPanel aPanel, PropertyChangeEvent aPropertyChangeEvent) {
      if ("style".equals(aPropertyChangeEvent.getPropertyName())) {
        aPanel.updateCustomizerPanelFromObject();
      }
    }
  }
}
