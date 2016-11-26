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
package samples.fusion.client.metadata;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.luciad.fusion.client.ALfnClientEnvironment;
import com.luciad.fusion.client.TLfnClientFactory;
import com.luciad.fusion.core.ALfnEnvironment;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.ELfnResourceType;
import com.luciad.fusion.tilestore.TLfnServiceException;
import com.luciad.fusion.tilestore.metadata.ALfnAssetMetadata;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;

import samples.fusion.client.common.QueryPanel;
import samples.fusion.client.common.ResourceHandler;
import samples.fusion.client.metadata.panel.AssetHolder;
import samples.fusion.client.metadata.panel.MetadataSelectionListener;
import samples.fusion.client.metadata.panel.MetadataTablePanel;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;

/**
 * Sample demonstrating how to create and use a proxy model to connect to a LuciadFusion server, to retrieve tiles from
 * the server and to display the tile data in a 2D map.
 * <p/>
 * To use this sample a LuciadFusion server must be running. You can for example use the
 * <code>samples.fusion.server</code> sample to start a local LuciadFusion server.
 */
public class MainPanel extends LightspeedSample {

  private ALfnEnvironment fEnvironment;

  private TLfnClientFactory fClientFactory;

  private QueryPanel fQueryServerPanel;

  private BoundsLayerFactory fBoundsLayerFactory = new BoundsLayerFactory();

  private MetadataSelectionListener fMetadataSelectionListener;

  @Override
  protected ILspAWTView createView() {
    // Create a LuciadFusion environment: this is the main entry point for LuciadFusion
    fEnvironment = ALfnEnvironment.newInstance();
    ALfnClientEnvironment clientEnvironment = ALfnClientEnvironment.newInstance(fEnvironment);
    fClientFactory = new TLfnClientFactory(clientEnvironment);
    return super.createView();
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Create the default tool bar and layer control
    ResourceHandler resourceHandler = new MyResourceListHandler();

    // Create a panel to query capabilities from the server
    fQueryServerPanel = new QueryPanel(fEnvironment, fClientFactory, this, resourceHandler, ELfnResourceType.ASSET);
    JPanel urlPanel = TitledPanel.createTitledPanel("LuciadFusion service settings", fQueryServerPanel);

    // create a panel to show retrieved metadata in a table form
    MetadataTablePanel metadataTablePanel = new MetadataTablePanel(fQueryServerPanel);
    JPanel southPanel = TitledPanel.createTitledPanel("Asset metadata properties", metadataTablePanel);

    fMetadataSelectionListener = new MetadataSelectionListener(fQueryServerPanel, metadataTablePanel);

    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.add(getToolBars()[0], BorderLayout.NORTH);
    northPanel.add(urlPanel, BorderLayout.SOUTH);

    add(northPanel, BorderLayout.NORTH);
    add(southPanel, BorderLayout.SOUTH);
  }

  private void createLayer(TLspLayerTreeNode aNode, ALfnAssetMetadata aAssetMetadata) {
    ILcdModel model = createModel(aAssetMetadata);
    ILspLayer layer = fBoundsLayerFactory.createLayer(model);
    layer.addSelectionListener(new SingleSelectionListener(layer));
    aNode.addLayer(layer);
    layer.addSelectionListener(fMetadataSelectionListener);
  }

  private ILcdModel createModel(ALfnAssetMetadata aAssetMetadata) {
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference((ILcdModelReference) aAssetMetadata.getGeoReference());
    model.setModelDescriptor(new TLcdModelDescriptor(aAssetMetadata.getId(), "Asset", aAssetMetadata.getName()));
    model.addElement(new AssetHolder(aAssetMetadata), ILcdFireEventMode.FIRE_LATER);
    return model;
  }

  /**
   * Resource handler that accepts only assets and adds them as layers to a map panel.
   */
  private class MyResourceListHandler implements ResourceHandler {

    private String fName;

    public void updateTileStore(ALfnTileStore aTileStore) {
      fName = aTileStore.getURI().toASCIIString();
    }

    public void updateResources(List<ResourceInfo> aResourceInfos) {
      if (aResourceInfos.isEmpty()) {
        return;
      }
      TLspLayerTreeNode node = new TLspLayerTreeNode(fName);

      ALfnTileStore client = fQueryServerPanel.getTileStore();
      for (ResourceInfo ri : aResourceInfos) {
        try {
          ALfnAssetMetadata asset = client.getResourceMetadata(ri.getId());
          if (asset != null) {
            createLayer(node, asset);
          }
        } catch (IOException | TLfnServiceException e) {
          JOptionPane
              .showMessageDialog(TLcdAWTUtil.findParentFrame(MainPanel.this), "Failed to retrieve asset", "Error",
                                 JOptionPane.ERROR_MESSAGE);
        }
      }
      getView().addLayer(node);

    }
  }

  private class SingleSelectionListener implements ILcdSelectionListener {

    private final ILspLayer fLayer;

    public SingleSelectionListener(ILspLayer aLayer) {
      fLayer = aLayer;
    }

    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      // Remove the selection in all other layers when an object is selected
      if (aSelectionEvent.selectedElements().hasMoreElements()) {
        Enumeration layerEn = getView().layers();
        while (layerEn.hasMoreElements()) {
          ILspLayer layer = (ILspLayer) layerEn.nextElement();
          if (layer != fLayer) {
            Enumeration selectionEn = layer.selectedObjects();
            while (selectionEn.hasMoreElements()) {
              Object o = selectionEn.nextElement();
              layer.selectObject(o, false, ILcdFireEventMode.FIRE_LATER);
            }
          }
          layer.fireCollectedSelectionChanges();
        }
      }
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "LuciadFusion Metadata");
  }

}
