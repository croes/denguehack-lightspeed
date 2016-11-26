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
package samples.lightspeed.meshencoder;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.Beans;
import java.io.File;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdShape;
import com.luciad.view.ILcdLayer;

import samples.common.MetaKeyUtil;

/**
 * An action that save the selected
 */
public class SaveMeshAction extends ALcdAction {

  private static int fNextNumber = 0;

  private ILcdLayer fLayer;
  private Frame fParentFrame;

  public SaveMeshAction() {
    this(null);
  }

  /**
   * Constructor with a <code>ILcdLayer</code> to consider.
   */
  public SaveMeshAction(ILcdLayer aLayer) {
    setLayer(aLayer);
    if (!Beans.isDesignTime()) {
      this.setName("Save");
    } else {
      setName("actionSave" + fNextNumber++);
    }
    setIcon(TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    setShortDescription("Save shape as mesh");
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, MetaKeyUtil.getCMDMask()));
  }

  /**
   * Sets the <code>ILcdLayer</code>(view) to consider
   */
  public void setLayer(ILcdLayer aLayer) {
    fLayer = aLayer;
  }

  public ILcdLayer getLayer() {
    return fLayer;
  }

  /**
   * Sets the parent Frame to be used by Dialogs (optional).
   */
  public void setParentFrame(Frame aParentFrame) {
    fParentFrame = aParentFrame;
  }

  public Frame getParentFrame() {
    return fParentFrame;
  }

  /**
   * Method called when this <code>ILcdAction</code> is triggered.
   */
  public void actionPerformed(ActionEvent e) {
    if ((getLayer() != null) && (TLcdUserDialog.hasManager())) {
      ILcdLayer layer = getLayer();
      ILcdModel model = layer.getModel();
      ILcdShape shapeToSave = null;

      Enumeration enumeration = layer.selectedObjects();
      //Encode the first selected object
      if (enumeration.hasMoreElements()) {
        Object selectedObject = enumeration.nextElement();
        if (selectedObject instanceof ILcdShape) {
          shapeToSave = (ILcdShape) selectedObject;
        }
      }

      // Try to find a parent Frame for TLcdUserDialog
      Frame parent_frame = fParentFrame;
      if (parent_frame == null) {
        parent_frame = TLcdAWTUtil.findParentFrame(e);
      }

      if (shapeToSave != null) {
        File file = TLcdUserDialog.chooseFile(null, ILcdDialogManager.SAVE, this, parent_frame);
        new JSONShapeMeshEncoder().exportShape(shapeToSave, model.getModelReference(), file.getAbsoluteFile().toString());
      } else {
        TLcdUserDialog.message("No shape selected. Please select a shape in the view to save it as a mesh.", ILcdDialogManager.INFORMATION_MESSAGE, this, null);
      }

    }
  }

}
