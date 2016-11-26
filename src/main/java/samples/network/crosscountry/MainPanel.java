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
package samples.network.crosscountry;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import samples.common.LuciadFrame;
import com.luciad.model.ILcdModel;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.network.crosscountry.data.RoutingLayerFactory;
import samples.network.crosscountry.data.ModelFactory;
import samples.network.crosscountry.graph.CrossCountryRasterGraphManager;
import samples.network.crosscountry.graph.EmptyResultRoute;
import samples.network.crosscountry.gui.ComputeCrossCountryRouteController;
import samples.network.crosscountry.gui.CrossCountryDistanceFunctionControlPanel;
import samples.network.crosscountry.gui.CrossCountryHeuristicFunctionControlPanel;
import samples.network.crosscountry.gui.RouteInfoPanel;

/**
 * This sample demonstrates the cross country movement.
 */
public class MainPanel extends SamplePanel {

  private TLcdMapJPanel fMapJPanel;
  private RouteInfoPanel fRouteInfoPanel;
  private CrossCountryRasterGraphManager fRasterGraphManager;
  private ModelFactory fModelFactory = new ModelFactory();

  private ILcdModel fRouteModel;
  private ExecutorService fExecutorService = Executors.newSingleThreadExecutor();
  private Future fCurrentExecution;

  protected void addData() {
    // Add the background data
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);

    // Add the elevation data
    ILcdModel elevationModel = GXYDataUtil.instance().model("Data/Dted/Alps/dmed").layer().addToView(fMapJPanel).fit().getModel();
    ILcdRaster elevationRaster = null;
    Enumeration elems = elevationModel.elements();
    while (elems.hasMoreElements() && elevationRaster == null) {
      Object elem = elems.nextElement();
      if (elem instanceof ILcdRaster) {
        elevationRaster = (ILcdRaster) elem;
      } else if (elem instanceof ILcdMultilevelRaster) {
        ILcdMultilevelRaster multilevelRaster = (ILcdMultilevelRaster) elem;
        elevationRaster = multilevelRaster.getRaster(multilevelRaster.getRasterCount() - 1);
      }
    }
    fRasterGraphManager.setRaster(elevationModel, elevationRaster);

    // Add the route model
    fRouteModel = fModelFactory.createRouteModel(elevationModel.getModelReference());
    GXYDataUtil.instance().model(fRouteModel).layer(new RoutingLayerFactory()).addToView(fMapJPanel);
  }

  protected void createGUI() {
    fMapJPanel = SampleMapJPanelFactory.createMapJPanel();
    fMapJPanel.setGXYLayerFactory(new RoutingLayerFactory());
    fRasterGraphManager = new CrossCountryRasterGraphManager();

    // Create the default toolbar
    ToolBar toolBar = new ToolBar(fMapJPanel, true, this);
    toolBar.addGXYController(new ComputeCrossCountryRouteController(fRasterGraphManager));
    LayerControlPanel layerControlSW = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    // Create the map
    TitledPanel mapPanel = TitledPanel.createTitledPanel(
        "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
                                                        );

    // Create the layer control and routing settings
    JPanel eastPanel = new JPanel(new BorderLayout());
    eastPanel.add(layerControlSW, BorderLayout.CENTER);
    JPanel southEastPanel = new JPanel();
    southEastPanel.setLayout(new BoxLayout(southEastPanel, BoxLayout.Y_AXIS));
    southEastPanel.add(new CrossCountryDistanceFunctionControlPanel(fRasterGraphManager));
    southEastPanel.add(new CrossCountryHeuristicFunctionControlPanel(fRasterGraphManager));
    eastPanel.add(southEastPanel, BorderLayout.SOUTH);

    // Create the route information
    fRouteInfoPanel = new RouteInfoPanel();

    // Add the components to the sample
    setLayout(new BorderLayout());
    add(toolBar, BorderLayout.NORTH);
    add(mapPanel, BorderLayout.CENTER);
    add(eastPanel, BorderLayout.EAST);
    add(fRouteInfoPanel, BorderLayout.SOUTH);

    // Automatically update the route
    fRasterGraphManager.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateRoute();
      }
    });
  }

  private void updateRoute() {
    // Update the start/end point
    if (fRasterGraphManager.getStartNode() != null) {
      EmptyResultRoute emptyResultRoute = new EmptyResultRoute();
      emptyResultRoute.addNode(fRasterGraphManager.getStartNode());
      if (fRasterGraphManager.getEndNode() != null) {
        emptyResultRoute.addNode(fRasterGraphManager.getEndNode());
      }

      fRouteModel.removeAllElements(ILcdModel.FIRE_LATER);
      fRouteModel.addElement(emptyResultRoute, ILcdModel.FIRE_LATER);
      fRouteModel.fireCollectedModelChanges();
    }
    // Compute a route if possible
    if (fRasterGraphManager.getStartNode() != null &&
        fRasterGraphManager.getEndNode() != null &&
        fRasterGraphManager.getEdgeValueFunctionFactory() != null &&
        fRasterGraphManager.getModel() != null &&
        fRasterGraphManager.getRaster() != null) {
      // Wait for the previous route computation
      try {
        if (fCurrentExecution != null) {
          fCurrentExecution.get();
        }
      } catch (InterruptedException e) {
        // ignore
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      // Start computing a new route
      fCurrentExecution = fExecutorService.submit(new ComputeRouteRunnable());
    }
  }

  private class ComputeRouteRunnable implements Runnable {

    public void run() {
      // Update the status panel
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          fRouteInfoPanel.setStatusComputingRoute();
        }
      });

      // Compute the route
      try {
        final double[] routeDistance = new double[1];
        long start = System.currentTimeMillis();
        final ILcdRoute newRoute = fRasterGraphManager.getShortestRoute(routeDistance);
        long end = System.currentTimeMillis();
        final long time = end - start;

        // Update the status panel
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            fRouteModel.removeAllElements(ILcdModel.FIRE_LATER);
            if (newRoute != null) {
              fRouteModel.addElement(newRoute, ILcdModel.FIRE_LATER);
            }
            fRouteModel.fireCollectedModelChanges();
            if (newRoute != null) {
              fRouteInfoPanel.setStatusComputedRoute(newRoute.getEdgeCount(), routeDistance[0], time);
            } else {
              fRouteInfoPanel.setStatusComputedNoRoute();
            }
          }
        });
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }

  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Cross Country Movement");
      }
    });
  }
}
