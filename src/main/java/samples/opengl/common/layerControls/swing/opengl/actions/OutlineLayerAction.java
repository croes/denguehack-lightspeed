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
import com.luciad.view.opengl.painter.ILcdGLOutlineCapable;
import samples.common.layerControls.actions.AbstractPropertyBasedLayerTreeToggleAction;

/**
 * <p>Action that turns wire frame painting for the selected layers on or off. This requires
 * that the selected layer be of type <code>ILcdGLLayer</code>.</p>
 */
public class OutlineLayerAction extends AbstractPropertyBasedLayerTreeToggleAction{
  private static String STRING_OUTLINED  = "Toggle wireframe";

  /**
   * <p>Create an action to toggle the paintOutline property of a layer.</p>
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   */
  public OutlineLayerAction( ILcdTreeLayered aLayered ) {
    super( aLayered, "paintOutline", new TLcdGUIIcon( TLcdGUIIcon.PAINTWIREFRAME_3D_16) );
    setShortDescription( STRING_OUTLINED );
  }

  protected boolean layerSupportsProperty( ILcdLayer aLayer ) {
    if (!(aLayer instanceof ILcdGLLayer))
      return false;
    ILcdGLLayer glLayer = (ILcdGLLayer) aLayer;
    return ( glLayer.getPainter() instanceof ILcdGLOutlineCapable && ( ( ILcdGLOutlineCapable ) glLayer.getPainter() ).isPaintOutlineSupported() );
  }

  protected void setLayerProperty( boolean aNewValue, ILcdLayer aLayer ) {
    ( ( ILcdGLOutlineCapable ) ( ( ILcdGLLayer ) aLayer).getPainter() ).setPaintOutline( aNewValue );
  }

  protected boolean getLayerProperty( ILcdLayer aLayer ) {
    return ( ( ILcdGLOutlineCapable ) ( ( ILcdGLLayer ) aLayer).getPainter() ).isPaintOutline();
  }
}
