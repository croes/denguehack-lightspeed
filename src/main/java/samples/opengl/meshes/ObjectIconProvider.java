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
package samples.opengl.meshes;

import com.luciad.format.object3d.ILcd3DMesh;
import com.luciad.format.object3d.ILcd3DMeshStyle;
import com.luciad.format.object3d.TLcd3DMeshBuilder;
import com.luciad.format.object3d.TLcd3DMeshStyleBuilder;
import com.luciad.view.opengl.ILcdGL3DIcon;
import com.luciad.view.opengl.ILcdGLObjectIconProvider;
import com.luciad.view.opengl.TLcdGL3DMeshIcon;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.style.TLcdGL3DMeshStyleProvider;

import java.awt.Color;

/**
 * An implementation of ILcdGLObjectIconProvider that uses built-in LuciadLightspeed meshes.
 */
class ObjectIconProvider implements ILcdGLObjectIconProvider {

  private TLcd3DMeshBuilder fMeshBuilder;

  private ILcdGL3DIcon fIcon = null;
  private TLcd3DMeshStyleBuilder fStyleBuilder;

  public static enum MeshType {
    ARROW,

    BOX,

    PYRAMID,

    SPHERE
  }

  public ObjectIconProvider(MeshType aMeshType) {
    super();
    fMeshBuilder = new TLcd3DMeshBuilder();
    fStyleBuilder = new TLcd3DMeshStyleBuilder();
    fIcon = createIcon( aMeshType );
  }

  private ILcd3DMeshStyle createColorStyle(Color aColor) {
    return fStyleBuilder
        .ambient( Color.darkGray )
        .diffuse( aColor )
        .specular( Color.darkGray )
        .emissive( Color.black )
        .textureSource( null )
        .shininess( 16.0 )
        .build();
  }

  private ILcd3DMeshStyle createTextureStyle(String aSource) {
    return fStyleBuilder
        .ambient( Color.darkGray )
        .diffuse( Color.white )
        .specular( Color.white )
        .emissive( Color.black )
        .textureSource( aSource )
        .shininess( 16.0 )
        .build();
  }

  private ILcdGL3DIcon createIcon(MeshType aMeshType) {
    switch(aMeshType) {
      case ARROW: return createArrowIcon();
      case BOX: return createBoxIcon();
      case PYRAMID: return createPyramidIcon( TLcd3DMeshBuilder.AxisOrientation.Y_ALIGNED );
      case SPHERE: return createEllipsoidIcon();
    }
    return null;
  }

  public boolean canGet3DIcon( Object aObject ) {
    return fIcon != null;
  }

  public ILcdGL3DIcon get3DIcon( Object aObject ) {
    return fIcon;
  }

  private ILcdGL3DIcon createArrowIcon() {
    ILcd3DMesh mesh = fMeshBuilder
        .arrow( 20, 60, 50, 150)
        .orientation(TLcd3DMeshBuilder.AxisOrientation.Y_ALIGNED)
        .style( createColorStyle( Color.orange ) )
        .build();
    return createStyledIcon( mesh );
  }

  private ILcdGL3DIcon createBoxIcon() {
    ILcd3DMesh mesh = fMeshBuilder
        .box( 200, 200, 200 )
        .style( createTextureStyle( "Data/textures/chessboard.png" ) )
        .build();
    return createStyledIcon( mesh );
  }

  private ILcdGL3DIcon createEllipsoidIcon() {
    ILcd3DMesh mesh = fMeshBuilder
        .ellipsoid( 100, 100, 100 )
        .sliceCount( 36 )
        .stackCount( 18 )
        .style( createColorStyle( Color.green ) )
        .build();
    return createStyledIcon( mesh );
  }

  private ILcdGL3DIcon createPyramidIcon( TLcd3DMeshBuilder.AxisOrientation aOrientation ) {
    ILcd3DMesh mesh = fMeshBuilder
        .pyramid( 75, 0, 150 )
        .orientation( aOrientation )
        .style( createColorStyle( Color.blue ) )
        .build();
    return createStyledIcon( mesh );
  }

  private ILcdGL3DIcon createStyledIcon( ILcd3DMesh aMesh ) {
    TLcdGL3DMeshIcon icon = new TLcdGL3DMeshIcon( aMesh ) {
      @Override
      public void paint( ILcdGLDrawable aGLDrawable ) {
        ILcdGL gl = aGLDrawable.getGL();
        gl.glPushAttrib(ILcdGL.GL_LINE_BIT);
        gl.glLineWidth( 3 );
        super.paint( aGLDrawable );
        gl.glPopAttrib();//restore original line width settings
      }
    };
    icon.setStyleProvider( new TLcdGL3DMeshStyleProvider() );
    return icon;
  }
}
