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
package samples.network.numeric.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.network.graph.numeric.TLcdNumericGraph;
import com.luciad.network.graph.numeric.TLcdNumericGraphDecoder;
import com.luciad.shape.ILcdBounded;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.common.layers.GXYLayerUtil;
import samples.network.numeric.graph.IDataObject2GraphIdMap;
import samples.network.numeric.graph.IGraphId2DataIdMap;
import samples.network.numeric.graph.NumericGraphManager;

/**
 * Action for loading a graph configuration.
 */
public abstract class LoadGraphConfigurationAction extends ALcdAction {

  public static final String DATA = "data";
  public static final String TOPOLOGY = "network.topology";
  public static final String VALUES = "network.values";
  public static final String NODE_2_GRAPH_INDEX = "network.node2GraphIndex";
  public static final String EDGE_2_GRAPH_INDEX = "network.edge2GraphIndex";
  public static final String GRAPH_2_EDGE_INDEX = "network.graph2EdgeIndex";

  private JFileChooser fFileChooser = new JFileChooser();

  private Component fParentComponent;

  private ILcdGXYView fView;
  private ILcdModelDecoder fModelDecoder;
  private ILcdGXYLayerFactory fLayerFactory;

  private NumericGraphManager fGraphManager;

  private ILcdGXYLayer fDataLayer;

  private ILcdInputStreamFactory fInputStreamFactory;

  public LoadGraphConfigurationAction(ILcdGXYView aView,
                                      ILcdModelDecoder aModelDecoder,
                                      ILcdGXYLayerFactory aLayerFactory,
                                      NumericGraphManager aGraphManager,
                                      Component aParentComponent,
                                      ILcdInputStreamFactory aInputStreamFactory) {
    super("Load graph configuration", TLcdIconFactory.create(TLcdIconFactory.OPEN_ICON));
    fView = aView;
    fModelDecoder = aModelDecoder;
    fLayerFactory = aLayerFactory;
    fGraphManager = aGraphManager;
    fParentComponent = aParentComponent;
    fInputStreamFactory = aInputStreamFactory;

    fFileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.isDirectory() || f.getAbsolutePath().endsWith(".properties");
      }

      @Override
      public String getDescription() {
        return "Network configuration properties";
      }
    });
  }

  // Implementations for ActionListener.

  public void actionPerformed(ActionEvent e) {
    if (fFileChooser.showOpenDialog(fParentComponent) == JFileChooser.APPROVE_OPTION) {
      final String configurationFile = fFileChooser.getSelectedFile().getAbsolutePath();
      Runnable runnable = new Runnable() {
        public void run() {
          loadGraphConfiguration(configurationFile);
        }
      };
      new Thread(runnable).start();
    }
  }

  protected abstract IDataObject2GraphIdMap createNode2GraphIdMap(String aMapFile) throws IOException;

  protected abstract IDataObject2GraphIdMap createEdge2GraphIdMap(String aMapFile) throws IOException;

  protected abstract IGraphId2DataIdMap createGraphId2EdgeIdMap(String aMapFile) throws IOException;

  public ILcdGXYLayer loadGraphConfiguration(String aPropertiesSource) {
    try {
      Properties properties = new Properties();
      properties.load(fInputStreamFactory.createInputStream(aPropertiesSource));

      // Load model.
      String dataFile = getLocation(properties, aPropertiesSource, DATA);
      ILcdModel model = loadModel(dataFile);

      // Load numeric graph
      String topologyFile = getLocation(properties, aPropertiesSource, TOPOLOGY);
      String valueFile = getLocation(properties, aPropertiesSource, VALUES);
      TLcdNumericGraphDecoder graphDecoder = new TLcdNumericGraphDecoder();
      TLcdNumericGraph graph = graphDecoder.decodeGraph(topologyFile, valueFile);

      // Load indices
      String nodeFile = getLocation(properties, aPropertiesSource, NODE_2_GRAPH_INDEX);
      IDataObject2GraphIdMap node2IdMap = createNode2GraphIdMap(nodeFile);

      String edgeFile = getLocation(properties, aPropertiesSource, EDGE_2_GRAPH_INDEX);
      IDataObject2GraphIdMap edge2IdMap = createEdge2GraphIdMap(edgeFile);

      String inverseEdgeFile = getLocation(properties, aPropertiesSource, GRAPH_2_EDGE_INDEX);
      IGraphId2DataIdMap id2EdgeIdMap = createGraphId2EdgeIdMap(inverseEdgeFile);

      // Configure graph manager.
      fGraphManager.setGraph(graph);
      fGraphManager.setEdgeValueFunction(graph.getEdgeValueFunction());
      fGraphManager.setNode2IdMap(node2IdMap);
      fGraphManager.setEdge2IdMap(edge2IdMap);
      fGraphManager.setId2EdgeIdMap(id2EdgeIdMap);
      fGraphManager.setShortestRouteDistanceTableProvider(graph.getShortestRouteDistanceTableProvider());

      // Remove existing data layer.
      if (fView.containsLayer(fDataLayer)) {
        GXYLayerUtil.removeGXYLayer(fView, fDataLayer, false);
      }
      // Create layer.
      fDataLayer = fLayerFactory.createGXYLayer(model);
      ILcdGXYLayer fittableLayer = getFittableLayer(fDataLayer);
      if (fittableLayer != null) {
        GXYLayerUtil.fitGXYLayer(fView, fittableLayer);
      }
      GXYLayerUtil.addGXYLayer(fView, fDataLayer, true, false);
      return fDataLayer;
    } catch (IOException e1) {
      e1.printStackTrace();
      return null;
    }
  }

  private ILcdGXYLayer getFittableLayer(ILcdGXYLayer aLayer) {
    ILcdModel model = aLayer.getModel();
    if (model != null && model instanceof ILcdBounded) {
      return aLayer;
    } else if (aLayer instanceof ILcdLayerTreeNode) {
      ILcdLayerTreeNode treeNode = (ILcdLayerTreeNode) aLayer;
      for (int i = 0; i < treeNode.layerCount(); i++) {
        ILcdGXYLayer layer = getFittableLayer((ILcdGXYLayer) treeNode.getLayer(i));
        if (layer != null) {
          return layer;
        }
      }
    }
    return null;
  }

  protected ILcdModel loadModel(String aSourceName) throws IOException {
    return fModelDecoder.decode(aSourceName);
  }

  public ILcdModelDecoder getModelDecoder() {
    return fModelDecoder;
  }

  private static String getLocation(Properties aProperties,
                                    String aPropertiesLocation,
                                    String aPropertyName) {
    String location = aProperties.getProperty(aPropertyName);
    return new File(location).isAbsolute() ?
           location : new File(getDirectory(aPropertiesLocation), location).getAbsolutePath();
  }

  private static String getDirectory(String aLocation) {
    TLcdIOUtil ioUtil = new TLcdIOUtil();
    ioUtil.setSourceName(aLocation);
    if (ioUtil.getFileName() != null) {
      return new File(ioUtil.getFileName()).getParent();
    } else {
      try {
        return new File(ioUtil.getURL().toURI()).getParent();
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Could not find parent directory for " + aLocation);
      }
    }
  }

}
