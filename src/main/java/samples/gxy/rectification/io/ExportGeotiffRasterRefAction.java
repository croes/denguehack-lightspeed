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

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.TLcdGeoTIFFModelEncoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdModelReferenceEncoder;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.common.ProgressUtil;
import samples.gxy.rectification.Rectifier;

/**
 * An action that saves the rectified raster model as a GeoTIFF file. Only the base projection and
 * the tie points are saved, the rectifying projection is not. This means that to get exactly the
 * same results when you load back the model, you should use an ILcdRasterReferencer configured with
 * the same parameters as the current rectifying projection.
 */
public class ExportGeotiffRasterRefAction extends ALcdAction {

  private ILcdGXYView fParentComponent;
  private Rectifier fRectifier;
  private JFileChooser fFileChooser;

  private static final int LEVEL_COUNT = 1;
  private static final String TFW_EXTENSION = "tfw";

  public ExportGeotiffRasterRefAction(ILcdGXYView aParentComponent,
                                      Rectifier aRectifier) {
    fParentComponent = aParentComponent;
    fRectifier = aRectifier;
    putValue(ILcdAction.SMALL_ICON, TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    putValue(ILcdAction.SHOW_ACTION_NAME, true);
    setName("Save rectified...");
    setShortDescription("Save the raster as a GeoTIFF.");

    fFileChooser = new JFileChooser();
    fFileChooser.setFileFilter(new MyFileFilter());
  }

  public void actionPerformed(ActionEvent event) {
    if (fFileChooser.showSaveDialog((Component) fParentComponent) == JFileChooser.APPROVE_OPTION) {
      String file = fFileChooser.getSelectedFile().getAbsolutePath();
      final String selected_file = file.endsWith(".tif") || file.endsWith(".tiff") ?
                                   file : file + ".tif";

      final TLcdGeoTIFFModelEncoder model_encoder = new TLcdGeoTIFFModelEncoder();
      model_encoder.setModelReferenceEncoder(new TLcdModelReferenceEncoder());
      final ILcdModel model = fRectifier.getRectifiedModel();

      // Get the size of the raster in pixels
      Object elem = model.elements().nextElement();
      ILcdRaster raster = elem instanceof ILcdRaster ? (ILcdRaster) elem :
                          elem instanceof ILcdMultilevelRaster ?
                          ((ILcdMultilevelRaster) elem).getRaster(0) : null;

      ILcdTile tile = raster.retrieveTile(0, 0);
      final int raster_width = raster.getTileColumnCount() * tile.getWidth();
      final int raster_height = raster.getTileRowCount() * tile.getHeight();

      model_encoder.setLevelCount(LEVEL_COUNT);

      final Frame frame = TLcdAWTUtil.findParentFrame(fParentComponent);
      final JDialog dialog = new JDialog(frame, "Saving raster", true);

      JPanel panel = new JPanel();
      panel.add(new JLabel("Saving the raster in rectified coordinates system."));
      dialog.add(panel);
      dialog.pack();
      TLcdAWTUtil.centerWindow(dialog);
      ProgressUtil.showDialog(dialog);

      new Thread() {
        public void run() {
          super.run();
          // Guard the analysis of the model by acquiring a read lock.
          try (TLcdLockUtil.Lock autoUnlocker = TLcdLockUtil.readLock(model)) {
            model_encoder.export(model, selected_file);

            // Encode the raster reference in a .ref file
            int index = selected_file.lastIndexOf(".");
            String baseFilename = index == -1 ?
                                  selected_file + "." :
                                  selected_file.substring(0, index + 1);

            // Encode the raster position in a .tfw file.
            ILcdBounds modelBounds = ((ILcdBounded) model).getBounds();

            double scale_x = modelBounds.getWidth() / raster_width;
            double scale_y = -modelBounds.getHeight() / raster_height;

            double translate_x = modelBounds.getLocation().getX() + 0.5 * scale_x;
            double translate_y = modelBounds.getLocation().getY() + modelBounds.getHeight() - 0.5 * scale_y;

            try {
              PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(baseFilename + TFW_EXTENSION)));
              out.println(scale_x);
              out.println(0.0);
              out.println(0.0);
              out.println(scale_y);
              out.println(translate_x);
              out.println(translate_y);
              out.close();
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          } catch (IOException ex) {
            System.err.println("Can't save image file [" + selected_file + "]");
          } finally {
            ProgressUtil.hideDialog(dialog);
          }
        }
      }.start();
    }
  }

  private static class MyFileFilter extends javax.swing.filechooser.FileFilter {

    public String getDescription() {
      return "GeoTIFF Images";
    }

    public boolean accept(File aPathName) {
      return aPathName.isDirectory() ||
             aPathName.getName().endsWith("tif") ||
             aPathName.getName().endsWith("tiff");
    }
  }
}
