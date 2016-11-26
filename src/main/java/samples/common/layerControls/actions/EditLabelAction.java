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
package samples.common.layerControls.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;

/**
 * <p>Action allowing to change the label of a layer.</p>
 *
 * <p>It will display a dialog in which you can enter the new label.</p>
 */
public class EditLabelAction extends AbstractLayerTreeAction {
  /**
   * Create a new action to change the label of a layer contained in <code>aLayered</code>
   * @param aLayered the ILcdTreeLayered instance to create the action for.
   */
  public EditLabelAction(ILcdTreeLayered aLayered) {
    super(aLayered);
    setIcon(TLcdIconFactory.create(TLcdIconFactory.LAYER_PROPERTIES_ICON));
    setShortDescription("Changes the layer's label");
  }

  protected boolean shouldBeEnabled() {
    return getFilteredLayers().size() == 1;
  }

  public void actionPerformed(ActionEvent e) {
    //the action is only enabled when exactly one layer is selected, so we can do this call without
    //any other checks
    ILcdLayer layer = getFilteredLayers().get(0);
    String newLabel = JOptionPane.showInputDialog(TLcdAWTUtil.findParentFrame(e),
                                                  "The new label for the layer " + layer.getLabel() + ":",
                                                  "Adjust layer label",
                                                  JOptionPane.INFORMATION_MESSAGE);
    //user has cancelled the dialog -> newLabel would be null
    if (newLabel != null) {
      layer.setLabel(newLabel);
    }
  }
}
