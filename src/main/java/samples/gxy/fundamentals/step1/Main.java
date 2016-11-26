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
package samples.gxy.fundamentals.step1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintQueueManager;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.controller.TLcdGXYPanController;
import com.luciad.view.gxy.controller.TLcdGXYZoomWheelController;
import com.luciad.view.map.TLcdMapJPanel;
import com.luciad.view.swing.TLcdLayerTree;


/**
 * Fundamentals sample 1: a basic application.
 * Shows how to create a 2D view and set it up with some background data.
 */
public class Main {

  // The application frame
  private JFrame fFrame;

  private TLcdMapJPanel createView() {
    // Creates the 2D view.
    TLcdMapJPanel map = new TLcdMapJPanel();
    TLcdGXYAsynchronousPaintQueueManager manager = new TLcdGXYAsynchronousPaintQueueManager();
    manager.setGXYView(map);
    return map;
  }

  protected void initLayers(TLcdMapJPanel aView) throws IOException {
    addBackgroundLayer(aView);
  }

  protected void addBackgroundLayer(TLcdMapJPanel aView) throws IOException {
    // Creates the model.
    TLcdGeoTIFFModelDecoder modelDecoder = new TLcdGeoTIFFModelDecoder();
    ILcdModel model = modelDecoder.decode("Data/GeoTIFF/BlueMarble/bluemarble.tif");

    // Creates the background layer.
    ILcdGXYLayer layer = new TLcdGXYAsynchronousLayerWrapper(new ImageLayerFactory().createGXYLayer(model));

    // Adds the background layer to the view and moves the grid layer to the top.
    aView.addGXYLayer(layer);
    aView.moveLayerAt(aView.layerCount() - 1, aView.getGridLayer());
  }

  /**
   * Creates a controller that pans using the left mouse button, and zooms using the mouse wheel.
   */
  protected void initController(TLcdMapJPanel aView) {
    TLcdGXYCompositeController compositeController = new TLcdGXYCompositeController();
    TLcdGXYPanController controller = new TLcdGXYPanController();
    controller.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().leftMouseButton().build());
    controller.setDragViewOnPan(true);
    compositeController.addGXYController(controller);
    compositeController.addGXYController(new TLcdGXYZoomWheelController());
    aView.setGXYController(compositeController);
  }

  /**
   * Opens a JFrame containing our view, tool bar and layer control.
   * @param aView The view.
   */
  private void initGUI(TLcdMapJPanel aView) {
    fFrame = new JFrame("LuciadLightspeed GXY Fundamentals");
    fFrame.getContentPane().setLayout(new BorderLayout());
    fFrame.getContentPane().add(aView, BorderLayout.CENTER);
    fFrame.add(new JScrollPane(createLayerControl(aView)), BorderLayout.EAST);
    fFrame.setSize(800, 600);
    fFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fFrame.setVisible(true);
  }

  private Component createLayerControl(TLcdMapJPanel aView) {
    return new TLcdLayerTree(aView);
  }

  /**
   * Entry point for starting the application.
   */
  protected void start() {
    try {
      TLcdMapJPanel view = createView();
      initGUI(view);
      initLayers(view);
      initController(view);
    } catch (IOException e) {
      // In a real application, exception handling should of course be more graceful.
      e.printStackTrace();
      JOptionPane.showMessageDialog(fFrame, "Starting the sample failed:\n\t" + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void main(final String[] aArgs) {
    // Switch to the Event Dispatch Thread, this is required by any Swing based application.
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new Main().start();
      }
    });
  }
}

