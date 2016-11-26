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
package samples.lightspeed.fundamentals.step1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;

import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;
import com.luciad.view.swing.TLcdLayerTree;

/**
 * The sample demonstrates how to create and set up a 2D/3D view with some background data.
 */
public class Main {

  static {
    // Set-up Swing to support heavy weight components, such as the TLspAWTView
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
  }

  // The application frame
  private JFrame fFrame;

  /**
   * Instantiates an TLspAWTView that can be added to our GUI.
   * @return The created view.
   */
  private ILspAWTView createView() {
    ILspAWTView view = TLspViewBuilder.newBuilder().buildAWTView();

    // Set layer factory of the view. When adding models to the view, this factory 
    // is used to create layers for those models.
    view.setLayerFactory(createLayerFactory());

    return view;
  }

  /**
   * Creates the layer factory which is set to the view.
   * @return The layer factory.
   */
  protected ILspLayerFactory createLayerFactory() {
    return new BasicLayerFactory();
  }

  /**
   * Creates and adds the layers that will be visible in the view.
   * @param aView The view.
   * @throws IOException In case of I/O failure.
   */
  protected void initLayers(ILspView aView) throws IOException {
    // Create a TLcdEarthRepositoryModelDecoder to decode Luciad Earth repositories
    ILcdModelDecoder earthDecoder = new TLcdEarthRepositoryModelDecoder();

    // Decode a sample data set (imagery data)
    ILcdModel earthModel = earthDecoder.decode("Data/Earth/SanFrancisco/tilerepository.cfg");

    // Calling addLayersFor() will cause the view to invoke its layer factory with
    // the given model and then add the resulting layers to itself
    aView.addLayersFor(earthModel);

    // TLcdSHPModelDecoder can read ESRI SHP files
    ILcdModelDecoder decoder = new TLcdSHPModelDecoder();

    // Decode world.shp to create an ILcdModel
    ILcdModel shpModel = decoder.decode("Data/Shp/World/world.shp");

    // Calling addLayers() will cause the view to invoke its layer factory with
    // the given model and then add the resulting layers to itself
    Collection<ILspLayer> shpLayer = aView.addLayersFor(shpModel);

    fitViewExtents(aView, shpLayer);

    // Create and add the grid layer
    aView.addLayer(TLspLonLatGridLayerBuilder.newBuilder().build());
  }

  protected void fitViewExtents(ILspView aView, Collection<ILspLayer> aLayers) {
    try {
      // Fit the view to the relevant layers.
      new TLspViewNavigationUtil(aView).fit(aLayers);
    } catch (TLcdOutOfBoundsException e) {
      JOptionPane.showMessageDialog(fFrame,
                                    "Could not fit on layer, layer is outside the valid bounds");
    } catch (TLcdNoBoundsException e) {
      JOptionPane.showMessageDialog(fFrame,
                                    "Could not fit on destination, no valid bounds found");
    }
  }

  /**
   * Initializes the controller of the view, responsible for the interaction with the
   * end-user. Nothing is done here, meaning the views's default controller is used.
   * Sub-classes can override this method.
   * @param aView The view.
   */
  protected void initController(ILspView aView) {
  }

  /**
   * Opens a JFrame containing our view, tool bar and layer control.
   * @param aView The view.
   */
  private void initGUI(ILspAWTView aView) {
    fFrame = new JFrame("Luciad Lightspeed Fundamentals");
    fFrame.getContentPane().setLayout(new BorderLayout());
    fFrame.getContentPane().add(aView.getHostComponent(), BorderLayout.CENTER);
    fFrame.add(createToolBar(aView), BorderLayout.NORTH);
    fFrame.add(new JScrollPane(createLayerControl(aView)), BorderLayout.EAST);
    fFrame.setSize(800, 600);
    fFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fFrame.setVisible(true);
  }

  private Component createLayerControl(ILspView aView) {
    return new TLcdLayerTree(aView);
  }

  protected JToolBar createToolBar(final ILspView aView) {
    // Create and add toolbar to frame
    JToolBar toolBar = new JToolBar();

    // Create a button group for the radio buttons
    ButtonGroup group = new ButtonGroup();

    // Create a button to switch to 2D
    JRadioButton b2d = new JRadioButton("2D", true);
    b2d.setAction(new AbstractAction("2D") {
      @Override
      public void actionPerformed(ActionEvent e) {
        TLspViewTransformationUtil.setup2DView(
            aView,
            new TLcdGridReference(new TLcdGeodeticDatum(),
                                  new TLcdEquidistantCylindrical()),
            true
        );
      }
    });
    b2d.setToolTipText("Switch the view to 2D");
    group.add(b2d);

    // Create a button to switch to 3D
    JRadioButton b3d = new JRadioButton("3D", false);
    b3d.setAction(new AbstractAction("3D") {
      @Override
      public void actionPerformed(ActionEvent e) {
        TLspViewTransformationUtil.setup3DView(aView, true);
      }
    });
    b3d.setToolTipText("Switch the view to 3D");
    group.add(b3d);

    // Add the two buttons to the toolbar
    toolBar.add(b2d);
    toolBar.add(b3d);

    return toolBar;
  }

  /**
   * Entry point for starting the application.
   */
  protected void start() {
    try {
      ILspAWTView view = createView();
      initGUI(view);
      initLayers(view);
      initController(view);
    } catch (IOException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(fFrame, "Starting the sample failed:\n\t" + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void main(String[] args) {
    // Switch to the Event Dispatch Thread, this is required by any Swing based application.
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        new Main().start();
      }
    });
  }
}
