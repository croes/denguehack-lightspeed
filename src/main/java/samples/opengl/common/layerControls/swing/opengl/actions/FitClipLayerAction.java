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

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.luciad.gui.TLcdGUIIcon;
import samples.common.layerControls.actions.AbstractLayerTreeAction;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.TLcdLayerTreeNodeUtil;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.action.TLcdGLClippingPlanesFitAction;

/**
 * <p>Action which allows to fit the <code>ILcdGLView</code> to the selected layers and adjusts the
 * cutoff planes when triggered. This requires that the layers are of type <code>ILcdGLLayer</code>.</p>
 */
public class FitClipLayerAction extends AbstractLayerTreeAction {
  private static String STRING_FIT_CLIP = "Fit clipping planes to layer";

  private ILcdGLView fGLView;
  private TLcdGLClippingPlanesFitAction fTLcdGLClippingPlanesFitAction = new TLcdGLClippingPlanesFitAction();

  /**
   * <p>Create a new fit-clip action for the view <code>aGLView</code>.</p>
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   * @param aGLView the view which must be fit onto the layers, set on this action
   */
  public FitClipLayerAction( ILcdTreeLayered aLayered, ILcdGLView aGLView ) {
    super( aLayered );
    fGLView = aGLView;
    setIcon( new TLcdGUIIcon( TLcdGUIIcon.FITCLIP_3D_16 ) );
    setShortDescription( STRING_FIT_CLIP );
  }


  public void actionPerformed( ActionEvent e ) {
    if ( fGLView != null ) {
      ILcd3DEditableBounds bounds = null;
      ArrayList<ILcdLayer> selectedLayers = getFilteredLayers();
      for ( ILcdLayer layer : selectedLayers ) {
        if ( !( TLcdLayerTreeNodeUtil.isEmptyNode( layer ) ) && layer instanceof ILcdGLLayer ) {
          ILcdBounds layerBounds = ( ( ILcdGLLayer ) layer ).getBounds( fGLView );
          if ( bounds == null ) {
            bounds = layerBounds.cloneAs3DEditableBounds();
          }
          else
            bounds.setTo3DUnion( layerBounds );
        }
      }
      if ( bounds != null ) {
        fTLcdGLClippingPlanesFitAction.setView( fGLView );
        fTLcdGLClippingPlanesFitAction.setBounds( bounds );
        fTLcdGLClippingPlanesFitAction.actionPerformed( null );
      }
    }
  }

  protected boolean shouldBeEnabled() {
    //should be enabled when at least one GLLayer is selected
    ArrayList<ILcdLayer> layers = getFilteredLayers();
    for ( ILcdLayer layer : layers ) {
      if ( !( TLcdLayerTreeNodeUtil.isEmptyNode( layer ) ) && layer instanceof ILcdGLLayer ) {
        return true;
      }
    }
    return false;
  }
}
