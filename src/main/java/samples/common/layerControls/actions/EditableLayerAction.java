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

import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;

/**
 * <p>Action to toggle the editable property of a layer.</p>
 */
public class EditableLayerAction extends AbstractPropertyBasedLayerTreeToggleAction {
  private static String STRING_EDITABLE = "Toggle layer editable";

  /**
   * <p>Create an action to toggle the editable property of a layer.</p>
   * @param aLayered the <code>ILcdTreeLayered</code> instance to create the action for.
   */
  public EditableLayerAction(ILcdTreeLayered aLayered) {
    super(aLayered, "editable", TLcdIconFactory.create(TLcdIconFactory.EDIT_ICON));
    setShortDescription(STRING_EDITABLE);

  }

  protected boolean layerSupportsProperty(ILcdLayer aLayer) {
    return aLayer.isEditableSupported();
  }

  protected void setLayerProperty(boolean aNewValue, ILcdLayer aLayer) {
    aLayer.setEditable(aNewValue);
  }

  protected boolean getLayerProperty(ILcdLayer aLayer) {
    return aLayer.isEditable();
  }
}
