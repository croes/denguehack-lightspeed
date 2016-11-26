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
import java.awt.event.KeyEvent;
import java.beans.Beans;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFormatter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

import samples.common.MetaKeyUtil;

/**
 * When triggered, this ILcdAction will allow the user to choose, among
 * the ILcdModel objects displayed on an ILcdLayered view, one ILcdModel
 * which has an ILcdModelEncoder, and save the ILcdModel using its
 * ILcdModelEncoder.
 * @see com.luciad.model.ILcdModelEncoder
 */
public class SaveAction
    extends ALcdAction {

  private static int fNextNumber = 0;

  ILcdLayered fLayered;
  Frame fParentFrame;

  public SaveAction() {
    this(null);
  }

  /**
   * Constructor with a <code>ILcdLayered</code> (view) to consider.
   * Note that <code>ILcdGXYView</code> implements <code>ILcdLayered</code>
   */
  public SaveAction(ILcdLayered aLayered) {
    setLayered(aLayered);
    if (!Beans.isDesignTime()) {
      this.setName("Save");
    } else {
      setName("actionSave" + fNextNumber++);
    }
    setIcon(TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    setShortDescription("Save layer");
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, MetaKeyUtil.getCMDMask()));
  }

  /**
   * Sets the <code>ILcdLayered</code>(view) to consider
   * Note that <code>ILcdGXYView</code> implements <code>ILcdLayered</code>
   */
  public void setLayered(ILcdLayered aLayered) {
    fLayered = aLayered;
  }

  public ILcdLayered getLayered() {
    return fLayered;
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
    if ((getLayered() != null) && (TLcdUserDialog.hasManager())) {
      ILcdLayered layered = getLayered();
      Vector layer_vector = new Vector();
      ILcdLayer layer;
      ILcdModel model;
      ILcdModel model_to_save = null;

      // Get all ILcdLayer objects which ILcdModel can be saved
      for (Enumeration enumeration = layered.layers(); enumeration.hasMoreElements(); ) {
        layer = (ILcdLayer) enumeration.nextElement();
        model = layer.getModel();
        model = untransform(model);
        if ((model != null) &&
            (model.getModelEncoder() != null) &&
            (model.getModelEncoder().canSave(model))) {
          layer_vector.addElement(layer);
          // ...
        }
      }

      // Try to find a parent Frame for TLcdUserDialog
      Frame parent_frame = fParentFrame;
      if (parent_frame == null) {
        parent_frame = TLcdAWTUtil.findParentFrame(e);
      }

      if (layer_vector.size() == 1) {
        // Only one ILcdModel can be saved: ask for confirmation
        layer = (ILcdLayer) layer_vector.elementAt(0);
        int choice = TLcdUserDialog.confirm("Save [" + layer.getLabel() + "] ?",
                                            ILcdDialogManager.YES_NO_OPTION,
                                            ILcdDialogManager.QUESTION_MESSAGE,
                                            this,
                                            parent_frame);
        if (choice == ILcdDialogManager.OK_OPTION) {
          model_to_save = layer.getModel();
          model_to_save = untransform(model_to_save);
        }
      }
      else if (layer_vector.size() > 1) {
        // More than one ILcdModel can be saved: ask the user to select one
        ILcdFormatter formatter = new ILcdFormatter() {
          public String format(Object aObject) {
            ILcdLayer layer2 = (ILcdLayer) aObject;
            return layer2.getLabel();
          }
        };
        layer = (ILcdLayer) TLcdUserDialog.choose(layer_vector,
                                                  formatter,
                                                  "Choose one to save:",
                                                  this,
                                                  parent_frame);
        if (layer != null) {
          model_to_save = layer.getModel();
          model_to_save = untransform(model_to_save);
        }
      }
      else {
        TLcdUserDialog.message("No layer can be saved.", ILcdDialogManager.INFORMATION_MESSAGE, this, null);
      }

      if (model_to_save != null) {
        // model_to_save is the chosen ILcdModel to be saved with a valid
        // ILcdModelEncoder
        try {
          model_to_save.getModelEncoder().save(model_to_save);
        } catch (IOException ioex) {
          ioex.printStackTrace();
          TLcdUserDialog.message("Cannot save [" + model_to_save.getModelDescriptor().getDisplayName() + "] due to " + ioex.getMessage(),
                                 ILcdDialogManager.ERROR_MESSAGE,
                                 this,
                                 parent_frame);
        }
      }
    }
  }

  /**
   * Untransform the model if necessary.
   */
  protected ILcdModel untransform(ILcdModel aModel) {
    return aModel;
  }

}

