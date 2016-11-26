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
package samples.common.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFormatter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * Extension of {@link SaveAction} to export a model instead of saving it
 * @see com.luciad.model.ILcdModelEncoder
 */
public class ExportAction
    extends SaveAction {

  public ExportAction() {
    super();
  }

  /**
   * Sets the <code>ILcdLayered</code>(view) to consider
   * Note that <code>ILcdGXYView</code> implements <code>ILcdLayered</code>
   */
  public ExportAction(ILcdLayered aLayered) {
    super(aLayered);
  }

  /**
   * Method called when this <code>ILcdAction</code> is triggered.
   */
  public void actionPerformed(ActionEvent e) {
    if ((getLayered() != null) && (TLcdUserDialog.hasManager())) {
      ILcdLayered layered = getLayered();
      Vector layerVector = new Vector();
      ILcdLayer layer;
      ILcdModel model;
      ILcdModel modelToExport = null;
      String destination = null;
      TLcdIOUtil ioUtil = new TLcdIOUtil();
      // Get all ILcdLayer objects which ILcdModel can be saved
      for (Enumeration enumeration = layered.layers(); enumeration.hasMoreElements(); ) {
        layer = (ILcdLayer) enumeration.nextElement();
        model = layer.getModel();
        if ((model != null) &&
            (model.getModelEncoder() != null)) {
          ioUtil.setSourceName(layer.getModel().getModelDescriptor().getSourceName());
          String originalSourceName = ioUtil.getURL() != null ? ioUtil.getURL().getPath() : ioUtil.getFileName();
          // We don't known up front the destination file of the model so we use the original source file for canExport check
          if(model.getModelEncoder().canExport(model, originalSourceName)) {
            layerVector.addElement(layer);
          }
        }
      }
      // Try to find a parent Frame for TLcdUserDialog
      Frame parentFrame = getParentFrame();
      if (parentFrame == null) {
        parentFrame = TLcdAWTUtil.findParentFrame(e);
      }

      if (layerVector.size() == 1) {
        // Only one ILcdModel can be saved: ask for confirmation
        layer = (ILcdLayer) layerVector.elementAt(0);
        ioUtil.setSourceName(layer.getModel().getModelDescriptor().getSourceName());
        String originalSourceName = ioUtil.getURL() != null ? ioUtil.getURL().getPath() : ioUtil.getFileName();
        File answer = TLcdUserDialog.chooseFile(originalSourceName,
                                                ILcdDialogManager.SAVE,
                                                this,
                                                parentFrame);
        if (answer != null) {
          modelToExport = layer.getModel();
          destination = answer.getAbsolutePath();
        }
      }
      else if (layerVector.size() > 1) {
        // More than one ILcdModel can be saved: ask the user to select one
        ILcdFormatter formatter = new ILcdFormatter() {
          public String format(Object aObject) {
            ILcdLayer layer2 = (ILcdLayer) aObject;
            return layer2.getLabel();
          }
        };
        layer = (ILcdLayer) TLcdUserDialog.choose(layerVector,
                                                  formatter,
                                                  "Choose one to export:",
                                                  this,
                                                  parentFrame);
        if (layer != null) {
          ioUtil.setSourceName(layer.getModel().getModelDescriptor().getSourceName());
          String originalSourceName = ioUtil.getURL() != null ? ioUtil.getURL().getPath() : ioUtil.getFileName();
          File answer = TLcdUserDialog.chooseFile(originalSourceName,
                                                  ILcdDialogManager.SAVE,
                                                  this,
                                                  parentFrame);
          if (answer != null) {
            modelToExport = layer.getModel();
            destination = answer.getAbsolutePath();
          }
        }
      }
      else {
        TLcdUserDialog.message("No layer can be exported.", ILcdDialogManager.INFORMATION_MESSAGE, this, null);
      }

      if (modelToExport != null) {
        // modelToExport is the chosen ILcdModel to be saved with a valid
        // ILcdModelEncoder
        try {
          modelToExport.getModelEncoder().export(modelToExport, destination);
        } catch (IOException ioex) {
          ioex.printStackTrace();
          TLcdUserDialog.message("Cannot export [" + modelToExport.getModelDescriptor().getDisplayName() + "] due to " + ioex.getMessage(),
                                 ILcdDialogManager.ERROR_MESSAGE,
                                 this,
                                 parentFrame);
        }
      }
    }
  }
}


