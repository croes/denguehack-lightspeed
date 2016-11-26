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
package samples.gxy.overview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Enumeration;

import javax.swing.JPanel;

import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.transformation.TLcdMapWorldMapWorldTransformation;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYOverviewController;
import com.luciad.view.map.TLcdMapJPanel;

import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;

/**
 * This container contains an ILcdGXYView which serves as an overview for
 * a slave ILcdGXYView by setting a TLcdGXYOverviewController on the
 * ILcdGXYView.
 * MyLayeredListener makes sure that layer changes of the slave view are propagated to the overview view.
 */
class OverviewPanelSW extends JPanel {

  private final TLcdMapJPanel fMasterGXYView = SampleMapJPanelFactory.createMapJPanel(new TLcdLonLatBounds(-130.0, 25.0, 65.0, 25.0));
  private ILcdGXYView fSlaveGXYView;
  private boolean fSynchronizeLayers = true;
  private final TLcdGXYOverviewController fGXYControllerOverview = new TLcdGXYOverviewController();
  private final MyLayeredListener fMyLayeredListener = new MyLayeredListener();

  private Dimension fPreferredSize = new Dimension(300, 200);
  private Dimension fMinimumSize = new Dimension(300, 200);

  public OverviewPanelSW() {
    TLcdMapWorldMapWorldTransformation transformation = new TLcdMapWorldMapWorldTransformation();
    fGXYControllerOverview.setXYWorldXYWorldTransformation(transformation);
    fGXYControllerOverview.setDragCentered(false);
    fGXYControllerOverview.setForceToSquare(false);
    fGXYControllerOverview.setFilled(true);
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Overview", fMasterGXYView));
  }

  public ILcdGXYView getMasterGXYView() {
    return fMasterGXYView;
  }

  public void setSynchronizeLayers(boolean aSynchronizeLayers) {
    fSynchronizeLayers = aSynchronizeLayers;
    if (fSlaveGXYView != null) {
      if (fSynchronizeLayers) {
        fSlaveGXYView.addLayeredListener(fMyLayeredListener);
      } else {
        fSlaveGXYView.removeLayeredListener(fMyLayeredListener);
      }
    }
  }

  public boolean isSynchronizeLayers() {
    return fSynchronizeLayers;
  }

  public void setSlaveGXYView(ILcdGXYView aSlaveGXYView) {
    if (fSlaveGXYView != aSlaveGXYView) {
      if (fSlaveGXYView != null) {
        fSlaveGXYView.removeLayeredListener(fMyLayeredListener);
      }

      if (isSynchronizeLayers()) {
        fMasterGXYView.removeAllLayers();
      }

      fSlaveGXYView = aSlaveGXYView;
      if (fSlaveGXYView != null) {
        if (isSynchronizeLayers()) {
          ILcdGXYLayer gxy_layer;
          for (Enumeration e = fSlaveGXYView.layers(); e.hasMoreElements(); ) {
            gxy_layer = (ILcdGXYLayer) e.nextElement();
            fMasterGXYView.addGXYLayer(gxy_layer);
          }
          fSlaveGXYView.addLayeredListener(fMyLayeredListener);
        }
        fGXYControllerOverview.setSlaveView(fSlaveGXYView);
        fMasterGXYView.setGXYController(fGXYControllerOverview);
      } else {
        fMasterGXYView.setGXYController(null);
      }
    }
  }

  public ILcdGXYView getSlaveGXYView() {
    return fSlaveGXYView;
  }

  public void setPreferredSize(Dimension aDimension) {
    fPreferredSize = aDimension;
  }

  public Dimension getPreferredSize() {
    return fPreferredSize;
  }

  public Dimension getMinimumSize() {
    return fMinimumSize;
  }

  class MyLayeredListener implements ILcdLayeredListener {

    public void layeredStateChanged(TLcdLayeredEvent e) {
      // We can cast here because we listen to an ILcdGXYView which contains
      // only ILcdGXYLayer.
      ILcdGXYLayer layer = (ILcdGXYLayer) e.getLayer();
      if (e.getID() == TLcdLayeredEvent.LAYER_ADDED) {
        fMasterGXYView.addGXYLayer(layer);
      } else if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        fMasterGXYView.removeLayer(layer);
      } else if (e.getID() == TLcdLayeredEvent.LAYER_MOVED) {
        boolean found = false;
        int index = 0;
        Enumeration enumeration = fSlaveGXYView.layers();
        while (enumeration.hasMoreElements() && !found) {
          found = enumeration.nextElement().equals(layer);
          index++;
        }
        fMasterGXYView.moveLayerAt(index - 1, layer);
      }
    }
  }

}


