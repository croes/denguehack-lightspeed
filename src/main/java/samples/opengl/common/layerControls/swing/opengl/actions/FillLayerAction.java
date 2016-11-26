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

package samples.opengl.common.layerControls.swing.opengl.actions;

import com.luciad.gui.TLcdGUIIcon;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.painter.ILcdGLFillCapable;
import samples.common.layerControls.actions.AbstractPropertyBasedLayerTreeToggleAction;

/**
 * <p>Action which toggles fill painting for the selected layers on or off. This requires that the
 * selected layers be of type <code>ILcdGLFillCapable</code>, or contain a painter of that type.</p>
 */
public class FillLayerAction extends AbstractPropertyBasedLayerTreeToggleAction{
  private static String STRING_TOGGLE_FILL   = "Toggle fill";

  /**
   * <p>Create an action that toggles the paintFill property of a layer.</p>
   * @param aLayered the <code>ILcdTreeLayered</code> instance for which this action is created
   */
  public FillLayerAction( ILcdTreeLayered aLayered ) {
    super( aLayered, "paintFill", new TLcdGUIIcon( TLcdGUIIcon.PAINTFILL_3D_16) );
    setShortDescription( STRING_TOGGLE_FILL );
  }

  protected boolean layerSupportsProperty( ILcdLayer aLayer ) {
    if (!(aLayer instanceof ILcdGLLayer))
      return false;
    ILcdGLLayer glLayer = (ILcdGLLayer) aLayer;
    return ( glLayer.getPainter() instanceof ILcdGLFillCapable && ( ( ILcdGLFillCapable ) glLayer.getPainter() ).isPaintFillSupported() );
  }

  protected void setLayerProperty( boolean aNewValue, ILcdLayer aLayer ) {
    ( ( ILcdGLFillCapable ) ( ( ILcdGLLayer ) aLayer ).getPainter() ).setPaintFill( aNewValue );
  }

  protected boolean getLayerProperty( ILcdLayer aLayer ) {
    return ( ( ILcdGLFillCapable ) ( ( ILcdGLLayer ) aLayer ).getPainter() ).isPaintFill();
  }
}
