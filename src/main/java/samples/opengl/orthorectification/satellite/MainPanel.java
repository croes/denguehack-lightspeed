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
package samples.opengl.orthorectification.satellite;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.reference.*;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.view.opengl.*;

import samples.common.layerControls.swing.LayerControlPanel;
import samples.opengl.common.*;
import samples.opengl.common.layerControls.swing.LayerControlPanelFactory3D;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * The main panel of the 3D sample application.
 */
class MainPanel extends JPanel {

  /**
   * Creates a new MainPanel.
   */
  public MainPanel() {

    // Create a geocentric world reference.
    TLcdGeocentricReference worldReference = new TLcdGeocentricReference(new TLcdGeodeticDatum());

    // Create the 3D view.
    TLcdGLViewCanvas canvas = new TLcdGLViewCanvas(800, 800);
    canvas.setXYZWorldReference(worldReference);
    canvas.setLayerFactory(new LayerFactory());
    canvas.setBackground(Color.black);

    Abstract3DPanel.setupSkybox(canvas);

    createGUI(canvas);
    setUpCamera(canvas);
    addData(canvas);
  }


  private void createGUI(TLcdGLViewCanvas aCanvas) {
    // Create the toolbar.
    Toolbar toolBar = new Toolbar(aCanvas, this);

    // Create the vertical exaggeration slider and the layer controls.
    LayerControlPanel layerControl = LayerControlPanelFactory3D.createDefaultGLLayerControlPanel( aCanvas );

    JPanel controlPanel = new JPanel(new BorderLayout());
    controlPanel.add(BorderLayout.CENTER, layerControl);

    JButton button = new JButton("Snapshot");
    button.addActionListener(new SnapshotListener(aCanvas));

    JPanel panel = new JPanel();
    panel.add(button);
    controlPanel.add(BorderLayout.NORTH, panel);

    // Populate the frame.
    setLayout(new BorderLayout());
    add(toolBar, BorderLayout.NORTH);
    add(aCanvas, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.EAST);

    updateUI();
  }


  private void setUpCamera(TLcdGLViewCanvas aCanvas) {
    ILcdGLCamera camera = aCanvas.getCamera();
    camera.setFov(0.7);

    // Set the initial camera position above Italy.
    TLcdGLGeocentricFixedEyeCameraAdapter adapter = new TLcdGLGeocentricFixedEyeCameraAdapter((ILcdGeocentricReference)aCanvas.getXYZWorldReference());
    adapter.setCamera(camera);

    adapter.setCollectChanges(true);
    // Position the camera somewhere west of Ithaca, NY. 
    adapter.setLocation(new TLcdLonLatHeightPoint(-79.47, 42.48, 681e3));
    adapter.setPitch(-70);
    adapter.setYaw(90);
    adapter.setDistance(1e6);
    adapter.applyCollectedChanges();
    adapter.setCollectChanges(false);
  }


  private void addData(final TLcdGLViewCanvas canvas) {

    // Create the globe and add it to the view.
    canvas.addModel(ModelFactory.createGridModel());

    // Create the countries model and add it to the view.
    try {
      canvas.addModel(ModelFactory.createSHPModel("Data/Shp/World/world.shp"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // 3D terrain requires a vertex shader capable graphics card when working
    // with a geocentric world reference. Therefore, TerrainFactory checks the
    // necessary hardware requirements before attempting to create a terrain
    // layer. However, the requirements cannot be checked unless the view has
    // been fully initialized, so we use a view listener to postpone the adding
    // of the terrain data until the view's first repaint.
    canvas.addViewListener(new ALcdGLViewAdapter() {
      public void postRender(TLcdGLViewEvent aViewEvent) {
        aViewEvent.getView().removeViewListener(this);
        try {

          if (canvas.getXYZWorldReference() instanceof ILcdGeocentricReference) {
            String extensions = canvas.getGLInformation().getProperty("gl.extensions", "");
            if (extensions.indexOf("GL_ARB_vertex_shader") < 0) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  Frame parentFrame = TLcdAWTUtil.findParentFrame(canvas);
                  JOptionPane.showMessageDialog(
                    parentFrame,
                    "Cannot create terrain. To display 3D terrain in a geocentric world reference, a vertex shader capable graphics card is required."
                  );
                }
              });
              return;
            }
          }

          // Create the terrain model and add it to the view.
          canvas.addModel(ModelFactory.createTerrainModel("Data/Ithaca/Terrain/IthacaEast.trn"));

          // Create the rivers model and add it to the view.
          canvas.addModel(ModelFactory.createSHPModel("Data/Shp/Ithaca/streets.shp"));
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
