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
package samples.lightspeed.demo.application.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.swing.TLcdRangeSlider;
import com.luciad.util.collections.ILcdList;
import com.luciad.util.collections.ILcdListListener;
import com.luciad.util.collections.TLcdListEvent;
import com.luciad.view.lightspeed.ILspView;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.gui.menu.IThemePanelFactory;
import samples.lightspeed.demo.framework.application.Framework;

/**
 * A panel factory for plot themes.
 */
public abstract class PlotPanelFactory implements IThemePanelFactory {

  protected DefaultFormBuilder createPanel(String aTitle) {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p"));
    builder.border(Borders.DIALOG);

    // Add title
    builder.append(createLabel(aTitle, 15), 3);
    builder.nextLine();
    return builder;
  }

  protected static void invalidateViews(Object aSource) {
    List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
    for (ILspView view : views) {
      view.invalidate(true, aSource, "Plot style changed");
    }
  }

  protected void addComponent(String aLabel, JComponent aComponent, DefaultFormBuilder aBuilder) {
    aBuilder.append(createLabel(aLabel, 12));
    aBuilder.append(aComponent);
    aBuilder.nextLine();
  }

  protected JPanel getPanel(DefaultFormBuilder aBuilder) {
    JPanel panel = aBuilder.getPanel();
    panel.setSize(panel.getLayout().preferredLayoutSize(panel));
    return panel;
  }

  protected JComponent createLabel(String aText, int aSize) {
    return new HaloLabel(aText, aSize, true);
  }

  protected JComponent createRangeSlider(final RangeModel aModel) {
    double min = aModel.getMinimum();
    double max = aModel.getMaximum();
    final TLcdRangeSlider slider = new TLcdRangeSlider(min, max, JSlider.HORIZONTAL);
    slider.setRangeMinimum(aModel.getRangeMinimum());
    slider.setRangeMaximum(aModel.getRangeMaximum());
    slider.setOpaque(false);

    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        aModel.setRange(
            slider.getModel().getRangeMinimum(),
            slider.getModel().getRangeMaximum()
        );
      }
    });
    aModel.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        double min = aModel.getMinimum();
        double max = aModel.getMaximum();
        double rangeMin = aModel.getRangeMinimum();
        double rangeMax = aModel.getRangeMaximum();
        slider.getModel().setMinimum(min);
        slider.getModel().setMaximum(max);
        slider.getModel().setRange(rangeMin, rangeMax);
      }
    });
    return slider;
  }

  protected JComponent createComboBox(final ComboBoxModel aModel, ListCellRenderer aListCellRenderer, int aExpectedListSize) {
    final JList list = new JList(aModel);
    list.setVisibleRowCount(Math.min(aExpectedListSize, 3));
    list.setOpaque(false);
    list.setCellRenderer(aListCellRenderer);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      {
        list.setSelectedValue(aModel.getSelectedItem(), true);
      }

      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          int selectionIndex = list.getSelectionModel().getMinSelectionIndex();
          Object selectedItem = selectionIndex == -1 ? null : aModel.getElementAt(selectionIndex);
          aModel.setSelectedItem(selectedItem);
        }
      }
    });
    if (list.getVisibleRowCount() < aExpectedListSize) {
      return new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    } else {
      return list;
    }
  }

  private static interface RangeModel {
    double getMinimum();

    double getMaximum();

    double getRangeMinimum();

    double getRangeMaximum();

    void setRange(double aStart, double aEnd);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
  }

  protected static abstract class ARangeModel implements RangeModel {
    private final Object fSource;
    private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

    protected ARangeModel(Object aSource) {
      fSource = aSource;
    }

    @Override
    public void setRange(double aStart, double aEnd) {
      doSetRange(aStart, aEnd);
      invalidateViews(fSource);
    }

    protected abstract void doSetRange(double aStart, double aEnd);

    public void fireChanged() {
      fPropertyChangeSupport.firePropertyChange("rangeModel", null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
      fPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
      fPropertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  protected static abstract class ListComboBoxModel<T> extends AbstractListModel implements ComboBoxModel {

    private final ILcdList<T> fValues;
    private final T fAnyValue;

    protected ListComboBoxModel(ILcdList<T> aValues, T aAnyValue) {
      fValues = aValues;
      fAnyValue = aAnyValue;
      fValues.addListListener(new ILcdListListener<T>() {
        @Override
        public void listChanged(TLcdListEvent<T> aListEvent) {
          TLcdAWTUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
              ListComboBoxModel.this.fireContentsChanged(fValues, 0, getSize());
            }
          });
        }
      });
    }

    @Override
    public int getSize() {
      synchronized (fValues) {
        return fValues.size() + 1;
      }
    }

    @Override
    public Object getElementAt(int index) {
      if (index == 0) {
        return fAnyValue;
      }
      synchronized (fValues) {
        return fValues.get(index - 1);
      }
    }

    @Override
    public Object getSelectedItem() {
      T selected = getSelectedValue();
      return selected == null ? fAnyValue : selected;
    }

    @Override
    public void setSelectedItem(Object anObject) {
      setSelectedValue(anObject == fAnyValue ? null : (T) anObject);
    }

    protected abstract T getSelectedValue();

    protected abstract void setSelectedValue(T aValue);
  }
}
