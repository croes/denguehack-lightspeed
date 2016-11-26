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
package samples.earth.preprocessor;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.earth.metadata.ILcdEarthAsset;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.LuciadFrame;
import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.earth.preprocessor.gui.MetadataPanel;
import samples.earth.preprocessor.gui.PreprocessorPanel;
import samples.earth.preprocessor.gui.PreprocessorSettingsPanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * A sample for importing metadata and synchronizing it with a repository.
 */
public class MainPanel extends SamplePanel {

  private TLcdMapJPanel fMapJPanel;
  private MetadataPanel fMetadataPanel;
  private PreprocessorPanel fPreProPanel;
  private PreprocessorSettingsPanel fPreProSettingsPanel;
  private boolean fDataLoaded = false;

  protected void createGUI() {
    fMapJPanel = SampleMapJPanelFactory.createMapJPanel();

    // Create the default toolbar and layer control
    ToolBar toolBar = new ToolBar(fMapJPanel, true, this);
    toolBar.getAdvancedRulerController().setDistanceFormat(
        new TLcdDistanceFormat(TLcdDistanceUnit.METRE_UNIT));

    // Create a layer control
    LayerControlPanel layerControl =
        LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    JPanel eastPanel = new JPanel(new BorderLayout());
    eastPanel.add(BorderLayout.CENTER, layerControl);

    // Create a map panel
    TitledPanel mapPanel = TitledPanel.createTitledPanel(
        "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    // Create the preprocessing panel
    fMetadataPanel = new MetadataPanel(fMapJPanel);
    fPreProSettingsPanel = new PreprocessorSettingsPanel();
    fPreProPanel = new PreprocessorPanel(fMapJPanel, fMetadataPanel, fPreProSettingsPanel);
    fPreProPanel.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PreprocessorPanel.IS_PREPROCESSING_PROPERTY)) {
          boolean active = !((Boolean) evt.getNewValue()).booleanValue();
          fMetadataPanel.setActive(active);
          fPreProSettingsPanel.setActive(active);
        } else if (evt.getPropertyName().equals(PreprocessorPanel.CURRENT_ASSET_PROPERTY)) {
          fMetadataPanel.setSelectedAsset((ILcdEarthAsset) evt.getNewValue());
        }
      }

    });

    JPanel southEastPanel = new JPanel();
    southEastPanel.setLayout(new BoxLayout(southEastPanel, BoxLayout.Y_AXIS));
    southEastPanel.add(TitledPanel.createTitledPanel(
        "Preprocessing settings", fPreProSettingsPanel, TitledPanel.NORTH));
    southEastPanel.add(TitledPanel.createTitledPanel(
        "Preprocessing progress", fPreProPanel, TitledPanel.NORTH));

    JPanel southPanel = new JPanel();
    southPanel.setLayout(new BorderLayout());
    southPanel.add(TitledPanel.createTitledPanel(
        "Metadata", fMetadataPanel, TitledPanel.NORTH | TitledPanel.EAST), BorderLayout.CENTER);
    southPanel.add(southEastPanel, BorderLayout.EAST);

    // Add the components
    setLayout(new BorderLayout());
    add(toolBar, BorderLayout.NORTH);
    add(mapPanel, BorderLayout.CENTER);
    add(eastPanel, BorderLayout.EAST);
    add(southPanel, BorderLayout.SOUTH);
  }

  protected void addData() throws IOException {
    try {
      super.addData();
      // Add a background layer
      GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);
      fMetadataPanel.loadData();
    } finally {
      setDataLoaded();
    }
  }

  private void setDataLoaded() {
    synchronized (this) {
      fDataLoaded = true;
      notifyAll();
    }
  }

  private void waitForDataLoaded() {
    synchronized (this) {
      while (!fDataLoaded) {
        try {
          wait();
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
  }

  private void preprocess(final String aMetadataFileName, final String aRepositoryDirectory) {
    try {
      waitForDataLoaded();

      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          fMetadataPanel.loadMetadata(aMetadataFileName);
          fPreProSettingsPanel.setRepository(aRepositoryDirectory);
        }
      });

      long start = System.currentTimeMillis();

      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          fPreProPanel.startPreprocessing();
        }
      });

      while (fPreProPanel.isPreprocessing()) {
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          // ignore
        }
      }

      long end = System.currentTimeMillis();
      System.out.println(end - start + " ms");
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    System.exit(0);
  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        MainPanel sample = new MainPanel();
        new LuciadFrame(sample, "Luciad Earth preprocessing", 1024, 768);

        if (aArgs.length == 2) {
          sample.preprocess(aArgs[0], aArgs[1]);
        }
      }
    });
  }
}
