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
package samples.lightspeed.internal.lvnl.buildings.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import com.luciad.format.object3d.openflight.TLcdOpenFlightFileDecoder;
import com.luciad.format.object3d.openflight.lightspeed.TLspOpenFlight3DIcon;
import com.luciad.format.object3d.openflight.model.TLcdOpenFlightHeaderNode;
import com.luciad.view.lightspeed.painter.mesh.ILsp3DIcon;

/**
 * Date: Jan 25, 2007
 * Time: 8:52:34 AM
 *
 * @author Tom Nuydens
 */
class OpenFlightObjectFormatSupport implements ObjectFormatSupport {

  private Set<String> fTexturePaths;
  private boolean fPreloadTextures;

  public OpenFlightObjectFormatSupport(boolean aPreloadTextures) {
    fTexturePaths = new HashSet<String>();
    fPreloadTextures = aPreloadTextures;
  }

  public FileFilter getFileFilter() {
    return new ExtensionBasedFileFilter("OpenFlight (*.flt)", new String[]{"flt"});
  }

  public boolean canDecodeObject(File f) {
    return getFileFilter().accept(f);
  }

  public ILsp3DIcon decodeObject(File f) throws IOException {
    TLcdOpenFlightFileDecoder decoder = new TLcdOpenFlightFileDecoder();
    TLcdOpenFlightHeaderNode node = decoder.decode(f.getAbsolutePath());

    String texturePath = f.getParentFile().getAbsolutePath();
    TLspOpenFlight3DIcon icon = new TLspOpenFlight3DIcon(node);

    if (fTexturePaths.add(texturePath)) {
      icon.addTextureSearchPath(texturePath);
    }

    return icon;
  }

/*
  private static void preloadStyles(
      ILcdOpenFlightNode aNode, 
      TLspOpenFlightMeshGLStateProvider aStyleProvider,
      TLspContext aContext
    ) {
    if (aNode instanceof ILcdStyled3DMesh ) {
      ILcdStyled3DMesh mesh = ((ILcdStyled3DMesh ) aNode);
      for (int k = 0; k < mesh.getPrimitiveCount(); k++) {
        ILcd3DMeshStyle style = mesh.getStyleForPrimitive(k);
        aStyleProvider.getGLState( style, aContext );
      }
    }

    int childCount = aNode.getChildCount();
    for (int i = 0; i < childCount; i++) {
        preloadStyles( aNode.getChild(i), aStyleProvider, aContext );
    }
  }
*/
}
