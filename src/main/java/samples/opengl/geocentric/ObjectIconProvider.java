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
package samples.opengl.geocentric;

import com.luciad.format.object3d.ILcd3DMesh;
import com.luciad.format.object3d.obj.TLcdOBJMeshDecoder;
import com.luciad.view.opengl.ILcdGL3DIcon;
import com.luciad.view.opengl.ILcdGLObjectIconProvider;
import com.luciad.view.opengl.TLcdGL3DMeshIcon;
import com.luciad.view.opengl.style.TLcdGL3DMeshStyleProvider;

import java.io.IOException;

/**
 * An ILcdGLObjectIconProvider used by the geocentric sample.
 */
class ObjectIconProvider implements ILcdGLObjectIconProvider {

  private ILcdGL3DIcon fIcon = null;

  public ObjectIconProvider() {
    super();
    try {
      // Load an airplane icon.
      fIcon = loadOBJ( "Data/3d_icons/plane.obj" );
    } catch ( IOException e ) {
      fIcon = null;
    }
  }

  public boolean canGet3DIcon( Object aObject ) {
    return fIcon != null;
  }

  public ILcdGL3DIcon get3DIcon( Object aObject ) {
    return fIcon;
  }

  private ILcdGL3DIcon loadOBJ( String aSourceName ) throws IOException {

    /* Load an icon in WaveFront OBJ format. TLcdOBJMeshDecoder will return an
       ILcd3DMesh, which we can then encapsulate in a TLcdGL3DMeshIcon. */
    TLcdOBJMeshDecoder decoder = new TLcdOBJMeshDecoder();
    ILcd3DMesh mesh = decoder.decodeMesh( aSourceName );

    TLcdGL3DMeshIcon icon = new TLcdGL3DMeshIcon( mesh );

    /* The style provider will provide materials and textures for the mesh. */
    TLcdGL3DMeshStyleProvider styleProvider = new TLcdGL3DMeshStyleProvider();
    icon.setStyleProvider( styleProvider );

    return icon;
  }
}
