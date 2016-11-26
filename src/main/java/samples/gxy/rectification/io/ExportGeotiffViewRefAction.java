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
package samples.gxy.rectification.io;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.TLcdGeoTIFFModelEncoder;
import com.luciad.format.raster.TLcdRaster;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.raster.TLcdRenderedImageTile;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYViewFitAction;
import com.luciad.view.gxy.TLcdGXYViewPlanarImage;

import samples.gxy.common.ProgressUtil;
import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.gxy.rectification.Rectifier;

/**
 * This action renders the rectified raster to an offscreen view, then saves the view as a GeoTIFF
 * file. In general, the offscreen view can have any reference, but in this sample it has the same
 * reference as the right-hand ("mapped") visible view.
 */
public class ExportGeotiffViewRefAction extends ALcdAction {

  private ILcdGXYView fGXYView;
  private Rectifier fRectifier;
  private JFileChooser fFileChooser;

  // Constants that affect the quality of the output
  private static final int IMAGE_WIDTH = 1024;
  private static final int IMAGE_HEIGHT = 1024;
  private static final int TILE_WIDTH = 512;
  private static final int TILE_HEIGHT = 512;
  private static final int LEVEL_COUNT = 5;
  private static final int WARP_BLOCK_SIZE = 32;

  public ExportGeotiffViewRefAction(ILcdGXYView aGXYView,
                                    Rectifier aRectifier) {
    fGXYView = aGXYView;
    fRectifier = aRectifier;
    putValue(ILcdAction.SMALL_ICON, new TLcdImageIcon("images/gui/i16_reshape.gif"));
    setShortDescription("Save the raster in the view's reference.");
    putValue(ILcdAction.SHOW_ACTION_NAME, true);
    setName("Save in view reference...");

    fFileChooser = new JFileChooser();
    fFileChooser.setFileFilter(new MyFileFilter());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (fRectifier.getRectifiedModel() == null || !fRectifier.getRectifiedModel().elements().hasMoreElements()) {
      System.out.println("No image is loaded.");
      return;
    }

    if (fFileChooser.showSaveDialog((Component) fGXYView) == JFileChooser.APPROVE_OPTION) {
      String file = fFileChooser.getSelectedFile().getAbsolutePath();
      final String selected_file = file.endsWith(".tif") || file.endsWith(".tiff") ?
                                   file : file + ".tif";

      // The following two lines approximate the ideal image size using the pixel density.
      // This may result in a large image with a very long saving time, so for the purpose of this
      // sample we will use a fixed size. 
//      int image_width  = (int)(raster.getBounds().getWidth()  * Math.sqrt(raster.getPixelDensity()));
//      int image_height = (int)(raster.getBounds().getHeight() * Math.sqrt(raster.getPixelDensity()));
      int image_width = IMAGE_WIDTH;
      int image_height = IMAGE_HEIGHT;

      //-------------------------------------------------------
      // Create an offscreen view to paint the raster
      TLcdGXYViewPlanarImage view_image =
          new TLcdGXYViewPlanarImage(image_width,
                                     image_height,
                                     TILE_WIDTH,
                                     TILE_HEIGHT,
                                     BufferedImage.TYPE_INT_ARGB);

      // Use a transparent background.
      view_image.setBackground(new Color(0, 0, 0, 0));

      // Avoid internal updates to the image that we wouldn't see anyway.
      view_image.setAutoUpdate(false);

      // Set the view's world reference.
      ILcdXYWorldReference world_reference = fGXYView.getXYWorldReference();

      view_image.setXYWorldReference(world_reference);

      // Create a model layer and add it to the view.
      RasterLayerFactory factory = new RasterLayerFactory();
      factory.setForcePainting(true);
      factory.setWarpBlockSize(WARP_BLOCK_SIZE);
      TLcdGXYLayer model_layer = (TLcdGXYLayer) factory.createGXYLayer(fRectifier.getRectifiedModel());
      view_image.addGXYLayer(model_layer);

      // Fit the view to the model layer.
      TLcdGXYViewFitAction fit_action = new TLcdGXYViewFitAction(view_image);
      fit_action.fitGXYLayer(model_layer, view_image);

      ILcdModelReference model_reference = (ILcdModelReference) view_image.getXYWorldReference();
      TLcdRasterModelDescriptor model_descriptor = new TLcdRasterModelDescriptor(selected_file,
                                                                                 selected_file,
                                                                                 "TIF");

      final ILcdModel model = new TLcdVectorModel(model_reference, model_descriptor);

      ILcd2DEditableBounds view_world_bounds = new TLcdXYBounds();
      view_image.getGXYViewXYWorldTransformation().viewAWTBounds2worldSFCT(view_image.getBounds(),
                                                                           view_world_bounds);

      ILcdTile tile = new TLcdRenderedImageTile(view_image);
      double density = tile.getWidth() / view_world_bounds.getWidth() *
                       tile.getHeight() / view_world_bounds.getHeight();

      model.addElement(new TLcdRaster(view_world_bounds,
                                      new ILcdTile[][]{{tile}},
                                      density,
                                      0,
                                      null),
                       ILcdFireEventMode.NO_EVENT);

      final TLcdGeoTIFFModelEncoder model_encoder = new TLcdGeoTIFFModelEncoder();

      model_encoder.setCompression(TLcdGeoTIFFModelEncoder.COMPRESSION_DEFLATE);
      model_encoder.setLevelCount(LEVEL_COUNT);
      model_encoder.setTileWidth(TILE_WIDTH);
      model_encoder.setTileHeight(TILE_HEIGHT);

      final Frame frame = TLcdAWTUtil.findParentFrame(fGXYView);
      final JDialog dialog = new JDialog(frame, "Saving raster", true);

      JPanel panel = new JPanel();
      panel.add(new JLabel("Saving the corrected raster."));
      panel.add(new JLabel("This operation may take a few minutes."));
      dialog.add(panel);
      dialog.pack();
      TLcdAWTUtil.centerWindow(dialog);
      ProgressUtil.showDialog(dialog);

      new Thread() {
        @Override
        public void run() {
          super.run();
          try {
            model_encoder.export(model, selected_file);
          } catch (IOException ex) {
            System.err.println("Can't save image file [" + selected_file + "]");
          } finally {
            ProgressUtil.hideDialog(dialog);
          }
        }
      }.start();
    }
  }

  void displayGeodeticBounds(ILcdBounds aBounds, ILcdXYWorldReference aReference) {
    TLcdGeodetic2Grid g2g = new TLcdGeodetic2Grid(new TLcdGeodeticReference(),
                                                  aReference);
    try {
      TLcdXYZBounds bounds = new TLcdXYZBounds();
      g2g.worldBounds2modelSFCT(aBounds, bounds);
      System.out.println(bounds.toString());
    } catch (TLcdOutOfBoundsException ex) {
      ex.printStackTrace();
    }
  }

  private static class MyFileFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public String getDescription() {
      return "GeoTIFF Images";
    }

    @Override
    public boolean accept(File aPathName) {
      return aPathName.isDirectory() ||
             aPathName.getName().endsWith("tif") ||
             aPathName.getName().endsWith("tiff");
    }
  }
}
