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
package samples.network.numeric;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.SwingUtilities;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdFitGXYLayerInViewClipAction;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintQueueManager;

import samples.common.LuciadFrame;
import samples.network.numeric.action.LoadGraphConfigurationAction;
import samples.network.numeric.graph.Edge2IdMap;
import samples.network.numeric.graph.IDataObject2GraphIdMap;
import samples.network.numeric.graph.IGraphId2DataIdMap;
import samples.network.numeric.graph.Id2EdgeIdMap;
import samples.network.numeric.graph.Node2IdMap;
import samples.network.numeric.indexedmap.IntegerLongIndexedMap;
import samples.network.numeric.indexedmap.LongIntegerIndexedMap;
import samples.network.numeric.view.gxy.SHPLayerFactory;

/**
 * This sample shows how to work with large, numeric graphs.
 */
public class MainPanel extends ANumericNetworkSample {

  private ILcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();

  public MainPanel() {
    super();
    setLoadGraphConfigurationAction(new SHPLoadGraphConfigurationAction());
  }

  @Override
  protected void addData() {
    super.addData();
    final ILcdGXYLayer layer = loadGraph("Data/Numeric/Usa/roads/roads.properties");
    if (layer != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          ILcdBounds bounds = new TLcdLonLatBounds(-72.78, 41.62, 0.25, 0.25);
          TLcdFitGXYLayerInViewClipAction.doFit(layer, fMapJPanel, bounds);
        }
      });
    }
  }

  @Override
  public void createGUI() {
    super.createGUI();
    TLcdGXYAsynchronousPaintQueueManager paintQueueManager = new TLcdGXYAsynchronousPaintQueueManager();
    paintQueueManager.setGXYView(fMapJPanel);
  }

  private class SHPLoadGraphConfigurationAction extends LoadGraphConfigurationAction {

    private SHPLoadGraphConfigurationAction() {
      super(fMapJPanel,
            new SHPDirectoryModelDecoder(),
            new SHPLayerFactory(getGraphManager()),
            getGraphManager(),
            MainPanel.this,
            fInputStreamFactory);
    }

    @Override
    protected IDataObject2GraphIdMap createNode2GraphIdMap(String aMapFile) throws IOException {
      return new Node2IdMap(new IntegerLongIndexedMap(aMapFile, fInputStreamFactory));
    }

    @Override
    protected IDataObject2GraphIdMap createEdge2GraphIdMap(String aMapFile) throws IOException {
      return new Edge2IdMap(new IntegerLongIndexedMap(aMapFile, fInputStreamFactory));
    }

    @Override
    protected IGraphId2DataIdMap createGraphId2EdgeIdMap(String aMapFile) throws IOException {
      return new Id2EdgeIdMap(new LongIntegerIndexedMap(aMapFile, fInputStreamFactory));
    }
  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Numeric network viewer", 900, 700);
      }
    });
  }

}
