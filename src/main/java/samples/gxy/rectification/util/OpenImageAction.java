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
package samples.gxy.rectification.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;

/**
 * Displays a file open dialog, tries to decode the chosen file, them calls
 * LayerManager.loadRasterModel().
 */
public abstract class OpenImageAction extends ALcdAction {

  private Component fParentComponent;
  private JFileChooser fFileChooser;
  private ILcdModelDecoder fModelDecoder;

  public OpenImageAction(Component aParentComponent,
                         ILcdModelDecoder aModelDecoder) {
    fParentComponent = aParentComponent;
    fModelDecoder = aModelDecoder;
    putValue(ILcdAction.SMALL_ICON, TLcdIconFactory.create(TLcdIconFactory.OPEN_ICON));
    setShortDescription("Open an image.");
    putValue(ILcdAction.SHOW_ACTION_NAME, true);
    setName("Load image...");
  }

  public void actionPerformed(ActionEvent event) {
    if (fFileChooser == null) {
      fFileChooser = new JFileChooser();
      fFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    }
    if (fFileChooser.showOpenDialog(fParentComponent) == JFileChooser.APPROVE_OPTION) {
      File selected_file = fFileChooser.getSelectedFile();
      try {
        ILcdModel model = fModelDecoder.decode(selected_file.getCanonicalPath());
        if (model.getModelDescriptor() instanceof TLcdRasterModelDescriptor) {
          rasterModelLoaded(model);
        } else {
          System.err.println("The sample supports only single-level rasters.");
        }
      } catch (IOException ex) {
        TLcdUserDialog.message("Can't open image " + selected_file,
                               ILcdDialogManager.ERROR_MESSAGE,
                               this,
                               fParentComponent);
      }
    }
  }

  protected abstract void rasterModelLoaded(ILcdModel aModel);
}
