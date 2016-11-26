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

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLView;
import samples.common.layerControls.actions.AbstractRemoveLayersAction;

/**
 * <p>Action to remove a GL layer from a <code>ILcdTreeLayered</code> instance.</p>
 */
public class RemoveGLLayersAction extends AbstractRemoveLayersAction{

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(RemoveGLLayersAction.class.getName());

  /**
   * <p>Create an action to remove a GL layer from an <code>ILcdTreeLayered</code> instance.</p>
   * @param aLayered the <code>ILcdTreeLayered</code> to create the action for
   */
  public RemoveGLLayersAction(ILcdTreeLayered aLayered) {
    super( aLayered );
  }

  protected void removeLayer( ILcdLayerTreeNode aParentNode, ILcdLayer aLayer ) {
    if ( aLayer instanceof ILcdGLLayer ) {
      final ILcdGLView view = ( ILcdGLView ) getLayered();
      final ILcdGLLayer layer = ( ILcdGLLayer ) aLayer;
      TLcdAWTUtil.invokeAndWait(
          new Runnable() {
            public void run() {
              try {
                view.setAutoUpdate( false );
                view.removeLayer( layer );
              } finally {
                view.setAutoUpdate( true );
              }
              view.invalidate( true, this, "Removing layer from view." );
            }
          } );
      }
    }
  }
