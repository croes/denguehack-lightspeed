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
package samples.lightspeed.internal.ecdis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import com.luciad.format.s57.TLcdS57CatalogueModelDescriptor;
import com.luciad.format.s57.TLcdS57ModelDescriptor;
import samples.decoder.ecdis.common.S52DisplaySettingsCustomizer;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.gxy.ILcdGXYView;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.printing.PrintPreviewAction;

/**
 * GUI tool which allows to view ECDIS data in GXY and LSP side-by-side
 *
 * @since 2013.1
 */
public class MainPanel extends samples.lightspeed.internal.gxylsp.MainPanel {

  private final TLcdS52DisplaySettings fS52DisplaySettings = new TLcdS52DisplaySettings();

  protected LightspeedSample createLspSample() {
    return new samples.decoder.ecdis.lightspeed.MainPanel() {
      @Override
      protected void addData() throws IOException {
        //only load some background data
        LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().addToView(getView());
        LspDataUtil.instance().grid().addToView(getView());
      }
    };
  }

  protected GXYSample createGXYSample() {
    return new samples.decoder.ecdis.gxy.MainPanel() {
      @Override
      protected void addData() throws IOException {
        GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
      }
    };
  }

  public void start() {
    super.start();

    keepDisplaySettingsInSync();

    pimpGui();

    try {
      openOnMaps("Data/Ecdis/Unencrypted/US5WA51M/US5WA51M.000");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void pimpGui() {
    removeComponent(fLspSample, S52DisplaySettingsCustomizer.class);
    removeComponent(fGXYSample, S52DisplaySettingsCustomizer.class);

    JButton printButton = new JButton("Print preview");
    printButton.addActionListener(new PrintPreviewAction(fLspSample.getView().getHostComponent(), fLspSample.getView()));

    JPanel customPanel = new JPanel();
    customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.Y_AXIS));
    customPanel.add(printButton);
    customPanel.add(createS64DataPanel());
    customPanel.add(new S52DisplaySettingsCustomizer(fS52DisplaySettings, false));

    fFrame.add(customPanel, BorderLayout.EAST);
  }

  private void removeComponent(Container aContainer, Class aClass) {
    for (Component component : aContainer.getComponents()) {
      if (aClass.isInstance(component)) {
        aContainer.remove(component);
      } else if (component instanceof Container) {
        removeComponent((Container) component, aClass);
      }
    }
  }

  private void keepDisplaySettingsInSync() {
    final TLcdS52DisplaySettings lspDisplaySettings = ((samples.decoder.ecdis.lightspeed.MainPanel) fLspSample).getS52DisplaySettings();
    final TLcdS52DisplaySettings gxyDisplaySettings = ((samples.decoder.ecdis.gxy.MainPanel) fGXYSample).getS52DisplaySettings();
    fS52DisplaySettings.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        lspDisplaySettings.setAll(fS52DisplaySettings);
        gxyDisplaySettings.setAll(fS52DisplaySettings);
        fGXYSample.getView().invalidate(true, this, "display settings");
      }
    });
  }

  private JPanel createS64DataPanel() {
    JPanel result = new JPanel();
    result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

    final MyOpenAction myOpenAction = new MyOpenAction();

    JButton openDataButton = new JButton("Open data");
    openDataButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          removeECDISLayersFromView(fGXYSample.getView());
          removeECDISLayersFromView(fLspSample.getView());
          myOpenAction.actionPerformed(e);
        } catch (Exception e1) {
          throw new RuntimeException(e1);
        }
      }
    });

    result.add(openDataButton);

    if (System.getProperty("data.directory") == null || System.getProperty("data2.directory") == null) {
      return result;
    }

    List<S64ReferenceDataSetLoader.S64ReferenceDataSet> dataSets = S64ReferenceDataSetLoader.getDataSets();

    JButton noaaDataButton = new JButton("Load NOAA");
    JButton westerscheldeDataButton = new JButton("Load Westerschelde");
    final JComboBox comboBox = new JComboBox(dataSets.toArray(new S64ReferenceDataSetLoader.S64ReferenceDataSet[dataSets.size()]));
    comboBox.setLightWeightPopupEnabled(false);
    final JButton loadDataButton = new JButton("Load selected S64");
    JButton openReferencePDFButton = new JButton("Open reference PDF");

    noaaDataButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          removeECDISLayersFromView(fGXYSample.getView());
          removeECDISLayersFromView(fLspSample.getView());
          openOnMaps(S64ReferenceDataSetLoader.ECDIS_TEST_DATA_DIR + "ENC/S57/NOAA/ENC_ROOT");
        } catch (Exception e1) {
          throw new RuntimeException(e1);
        }
      }
    });

    westerscheldeDataButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          removeECDISLayersFromView(fGXYSample.getView());
          removeECDISLayersFromView(fLspSample.getView());
          openOnMaps(S64ReferenceDataSetLoader.ECDIS_TEST_DATA_DIR + "ENC/S57/Schelde/ENC_ROOT");
        } catch (Exception e1) {
          throw new RuntimeException(e1);
        }
      }
    });

    loadDataButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        removeECDISLayersFromView(fGXYSample.getView());
        removeECDISLayersFromView(fLspSample.getView());
        S64ReferenceDataSetLoader.S64ReferenceDataSet dataSet = (S64ReferenceDataSetLoader.S64ReferenceDataSet) comboBox.getSelectedItem();
        List<String> sourceFiles = dataSet.fSourceFiles;
        for (String source : sourceFiles) {
          try {
            openOnMaps(source);
          } catch (Exception e1) {
            throw new RuntimeException(e1);
          }
        }
      }
    });

    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadDataButton.doClick();
      }
    });

    openReferencePDFButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        S64ReferenceDataSetLoader.S64ReferenceDataSet dataSet = (S64ReferenceDataSetLoader.S64ReferenceDataSet) comboBox.getSelectedItem();
        try {
          Desktop.getDesktop().open(new File(dataSet.fReferencePDFSOurceFile));
        } catch (IOException e1) {
          throw new RuntimeException(e1);
        }
      }
    });

    result.add(noaaDataButton);
    result.add(westerscheldeDataButton);
    result.add(comboBox);
    result.add(loadDataButton);
    result.add(openReferencePDFButton);
    return result;
  }

  private void removeECDISLayersFromView(ILcdLayered aView) {
    Enumeration layers = aView.layersBackwards();
    while (layers.hasMoreElements()) {
      ILcdLayer layer = (ILcdLayer) layers.nextElement();
      ILcdModel model = layer.getModel();
      if (model != null && (model.getModelDescriptor() instanceof TLcdS57CatalogueModelDescriptor || model.getModelDescriptor() instanceof TLcdS57ModelDescriptor)) {
        aView.removeLayer(layer);
        if (aView instanceof ILcdGXYView) {
          ((ILcdGXYView) aView).setNumberOfCachedBackgroundLayers(1); // see LSP131-956
        }
      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        new MainPanel().start();
      }
    });
  }
}
