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
package samples.lightspeed.demo.application.gui.menu;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.data.density.DensityTheme;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Creates theme panels for the density theme.
 */
public class DensityPanelFactory implements IThemePanelFactory {

  public DensityPanelFactory() {
    // Default constructor
  }

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    if (!(aTheme instanceof DensityTheme)) {
      return new ArrayList<JPanel>();
    }
    return Collections.singletonList(createThemePanel((DensityTheme) aTheme));
  }

  public JPanel createThemePanel(DensityTheme aTheme) {
    if (aTheme.getLayers().isEmpty()) {
      return null;
    }

    boolean isTouchUI = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

    DefaultFormBuilder builder;

    if (isTouchUI) {
      builder = new DefaultFormBuilder(new FormLayout("p"));
      builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));
    } else {
      builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p"));
    }
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Theme Layers", 15, true) {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(200, 25);
      }
    };
    builder.append(titleLabel, isTouchUI ? 1 : 3);
    builder.nextLine();

    if (isTouchUI) {
      addButtons(aTheme, builder);
    } else {
      addRadioButtons(aTheme, builder);
    }

    // Create content panel and set its size (not doing the latter will cause it to be invisible)
    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());
    return contentPanel;
  }

  private void addRadioButtons(DensityTheme aTheme, DefaultFormBuilder aBuilder) {
    // Create exclusive layer controller for themes with radio buttons
    RadioButtonLayerController layerController = new RadioButtonLayerController();
    layerController.setLayerId(aTheme.getLayerIDs().get(0));
    BeanAdapter<RadioButtonLayerController> adapter = new BeanAdapter<RadioButtonLayerController>(layerController, true);
    BeanAdapter<RadioButtonLayerController>.SimplePropertyAdapter valueModel = adapter
        .getValueModel("layerId");

    for (String id : aTheme.getLayerIDs()) {
      List<ILspLayer> layers = Framework.getInstance().getLayersWithID(id);
      if (layers.isEmpty()) {
        continue;
      }

      JRadioButton rb = new JRadioButton();
      Bindings.bind(rb, valueModel, id);

      rb.setOpaque(false);

      aBuilder.append(rb);
      aBuilder.append(new HaloLabel(layers.get(0).getLabel()));
      aBuilder.nextLine();
    }
  }

  private void addButtons(final DensityTheme aTheme, DefaultFormBuilder aBuilder) {

    Map<String, List<ILspLayer>> layerLabelMap = new HashMap<String, List<ILspLayer>>();
    for (ILspLayer layer : aTheme.getLayers()) {
      List<ILspLayer> layers = layerLabelMap.get(layer.getLabel());
      if (layers == null) {
        layers = new ArrayList<ILspLayer>();
        layerLabelMap.put(layer.getLabel(), layers);
      }
      layers.add(layer);
    }

    for (final List<ILspLayer> layerSet : layerLabelMap.values()) {
      JButton button = new JButton();
      button.setText(layerSet.get(0).getLabel());
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setVisible(aTheme, layerSet.get(0).getLabel());
        }
      });

      aBuilder.append(button);
      aBuilder.nextLine();
    }
  }

  private void setVisible(DensityTheme aTheme, String aLabel) {
    for (ILspLayer layer : aTheme.getLayers()) {
      if (aLabel.equals(layer.getLabel())) {
        layer.setVisible(true);
      } else {
        layer.setVisible(false);
      }
    }
  }

  /**
   * Controller used to control the visibility of the layers associated exclusively to one id. That is, this controller is used to enable visibility of theme layers in an either-or fashion: no two layers that are associated to a different layer id can be visible at the same time.
   */
  public static class RadioButtonLayerController {

    private String fLayerId;
    private PropertyChangeSupport fPropertyChangeSupport;

    public RadioButtonLayerController() {
      fPropertyChangeSupport = new PropertyChangeSupport(this);
    }

    public String getLayerId() {
      return fLayerId;
    }

    public void setLayerId(String aLayerId) {
      Framework app = Framework.getInstance();

      // Disable layers associated with old layer id
      if (fLayerId != null) {
        for (ILspLayer layer : app.getLayersWithID(fLayerId)) {
          layer.setVisible(false);
        }
      }

      // Set new layer id
      String oldValue = fLayerId;
      fLayerId = aLayerId;
      fPropertyChangeSupport.firePropertyChange("layerId", oldValue, fLayerId);

      // Enable layers with new layer id
      for (ILspLayer layer : app.getLayersWithID(fLayerId)) {
        layer.setVisible(true);
      }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
      fPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
      fPropertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

}
