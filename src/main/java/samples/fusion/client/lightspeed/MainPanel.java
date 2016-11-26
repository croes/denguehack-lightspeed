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
package samples.fusion.client.lightspeed;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.luciad.fusion.client.ALfnClientEnvironment;
import com.luciad.fusion.client.TLfnClientFactory;
import com.luciad.fusion.core.ALfnEnvironment;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.ELfnDataType;
import com.luciad.fusion.tilestore.ELfnResourceType;
import com.luciad.fusion.tilestore.model.ALfnTileStoreModel;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDecoder;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.fusion.client.common.CoverageListPanel;
import samples.fusion.client.common.QueryPanel;
import samples.fusion.client.common.ResourceHandler;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.ToolBar;

/**
 * Main GUI panel of the proxy sample.
 */
public class MainPanel extends LightspeedSample {

  public static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger("com.luciad");

  private ALfnEnvironment fEnvironment;

  private TLfnClientFactory fClientFactory;

  private CoverageListPanel fCoveragePanel;

  private QueryPanel fQueryServerPanel;

  @Override
  protected ILspAWTView createView() {
    fEnvironment = ALfnEnvironment.newInstance();
    ALfnClientEnvironment clientEnvironment = ALfnClientEnvironment.newInstance(fEnvironment);
    fClientFactory = new TLfnClientFactory(clientEnvironment);
    return createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // create a panel to query tilesets from the server
    fCoveragePanel = new CoverageListPanel(new CreateLayerListener());
    fCoveragePanel.setSelectionMode(CoverageListPanel.SelectionMode.MULTIPLE_COVERAGES);
    JPanel coveragePanel = TitledPanel.createTitledPanel("Coverages", fCoveragePanel);
    addComponentToRightPanel(coveragePanel);

    // create a panel to perform a query to an Earth server
    fQueryServerPanel = new QueryPanel(fEnvironment, fClientFactory, this, fCoveragePanel, ELfnResourceType.COVERAGE);
    fQueryServerPanel
        .setSelectableTypes(ELfnDataType.IMAGE, ELfnDataType.ELEVATION, ELfnDataType.VECTOR, ELfnDataType.MULTIVALUED, ELfnDataType.RASTER);
    JPanel urlPanel = TitledPanel.createTitledPanel("LuciadFusion service settings", fQueryServerPanel);

    JPanel toolbarPanel = new JPanel();
    toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
    ToolBar[] toolBars = getToolBars();
    for (JToolBar toolBar : toolBars) {
      toolbarPanel.add(toolBar);
    }

    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.add(toolbarPanel, BorderLayout.NORTH);
    northPanel.add(urlPanel, BorderLayout.SOUTH);

    add(northPanel, BorderLayout.NORTH);
  }

  protected void addFusionModelsToView() {
    for (ResourceHandler.ResourceInfo resourceInfo : fCoveragePanel.getSelectedCoverages()) {
      // Create a model for the selected client and coverage
      if (!ResourceHandler.ResourceInfo.TYPE_COVERAGE.equals(resourceInfo.getType())) {
        continue; // only coverages are supported
      }
      try {
        ILcdModel model = createModel(fEnvironment, fQueryServerPanel.getTileStore(), resourceInfo.getId());
        Collection<ILspLayer> layers = getView().addLayersFor(model);
        FitUtil.fitOnLayers(this, getView(), true, layers.toArray(new ILspLayer[layers.size()]));
      } catch (Exception e) {
        TLcdLoggerFactory.getLogger("com.luciad").warn("Failed to retrieve coverage: " + resourceInfo, e);
        JOptionPane.showMessageDialog(TLcdAWTUtil.findParentFrame(this),
                                      "Failed to retrieve coverage: " + resourceInfo + ", reason: " + e.getMessage(),
                                      "Coverage retrieval failed", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  protected ALfnTileStoreModel createModel(ALfnEnvironment aEnvironment, ALfnTileStore aTileStore,
                                           String aCoverageId) throws IOException {
    TLfnTileStoreModelDecoder modelDecoder = new TLfnTileStoreModelDecoder(aEnvironment);
    return modelDecoder.decode(aTileStore, aCoverageId);
  }

  /**
   * Listener for the "Create layer" action.
   */
  private class CreateLayerListener implements ActionListener {

    public void actionPerformed(ActionEvent aEvent) {
      addFusionModelsToView();
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "LuciadFusion Client");
  }

}
