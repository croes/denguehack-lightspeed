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
package samples.gxy.common.layerControls.actions;

import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;

import samples.common.layerControls.actions.AbstractPropertyBasedLayerTreeToggleAction;

/**
 * <p>Action to toggle the editable label property of a GXYLayer.</p>
 */
public class EditableLabelGXYLayerAction extends AbstractPropertyBasedLayerTreeToggleAction {
  private static String STRING_EDITABLE_LABEL = "Toggle editable label";

  /**
   * <p>Create an action to toggle the editable labels property of a layer.</p>
   * @param aLayered the <code>ILcdTreeLayered</code> instance to create the action for
   */
  public EditableLabelGXYLayerAction(ILcdTreeLayered aLayered) {
    super(aLayered, "labelsEditable", new TLcdResizeableIcon(null, 16, 16));//empty icon
    setShortDescription(STRING_EDITABLE_LABEL);
  }

  protected boolean layerSupportsProperty(ILcdLayer aLayer) {
    return (aLayer instanceof ILcdGXYEditableLabelsLayer) &&
           (((ILcdGXYEditableLabelsLayer) aLayer).isLabelsEditableSupported());
  }

  protected void setLayerProperty(boolean aNewValue, ILcdLayer aLayer) {
    ((ILcdGXYEditableLabelsLayer) aLayer).setLabelsEditable(aNewValue);
  }

  protected boolean getLayerProperty(ILcdLayer aLayer) {
    return ((ILcdGXYEditableLabelsLayer) aLayer).isLabelsEditable();
  }
}
