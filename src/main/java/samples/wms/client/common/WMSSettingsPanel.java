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
package samples.wms.client.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.luciad.model.ILcdModel;

import samples.common.OptionsPanelBuilder;
import samples.wms.client.WMSLayerList;

/**
 * Settings panel for WMS.
 */
public class WMSSettingsPanel extends JPanel {

  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);
  private final WMSLayerList fWMSLayerList;
  private boolean fTiled = true;

  public WMSSettingsPanel() {
    OptionsPanelBuilder panelBuilder = OptionsPanelBuilder.newInstance();
    // Add wms layer list
    fWMSLayerList = new WMSLayerList();
    JScrollPane scrollPane = new JScrollPane(fWMSLayerList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    // Add buttons below the wms layer list to easily select all layers / none.
    JPanel selectButtonPanel = new JPanel();
    selectButtonPanel.setLayout(new BoxLayout(selectButtonPanel, BoxLayout.X_AXIS));
    JButton selectAllButton = new JButton("Select all");
    JButton selectNoneButton = new JButton("Clear all");
    selectAllButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fWMSLayerList.selectAll();
      }
    });
    selectNoneButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fWMSLayerList.clearAll();
      }
    });
    selectButtonPanel.add(selectAllButton);
    selectButtonPanel.add(selectNoneButton);

    panelBuilder.fixedPanel("Available WMS layers")
                .component(scrollPane)
                .component(selectButtonPanel);

    // Add painter settings
    panelBuilder.fixedPanel("Painting settings")
                .checkBox(
                    "Tiled",
                    "Tiled",
                    fTiled,
                    "<html>" +
                    "<p><strong>Switches between tiled and non-tiled painting.</strong></p>" +
                    "<p>Tiled painting typically offers better performance while<br\\>" +
                    "non-tiled painting allows pixel-perfect results in a 2D view.</p>" +
                    "</html>",
                    "",
                    new PropertyChangeListener() {
                      @Override
                      public void propertyChange(PropertyChangeEvent evt) {
                        fTiled = (Boolean) evt.getNewValue();
                        fPropertyChangeSupport.firePropertyChange("tiled", evt.getOldValue(), evt.getNewValue());
                      }
                    });
    // Populate the panel
    panelBuilder.populate(this);
  }

  public void setWMSModel(ILcdModel aWMSModel) {
    if (aWMSModel == null) {
      fWMSLayerList.removeWMSModel();
    } else {
      fWMSLayerList.setWMSModel(aWMSModel);
    }
  }

  public boolean isTiled() {
    return fTiled;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }
}
