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
package samples.opengl.terrain.aboveground;

import com.luciad.format.raster.terrain.TLcdTerrainModelDescriptor;
import com.luciad.format.raster.terrain.opengl.TLcdGLAboveGroundPointlistPainter;
import com.luciad.format.raster.terrain.opengl.TLcdGLTerrainPainter;
import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.painter.TLcdGLShapeListPainter;
import com.luciad.view.opengl.style.ILcdGLStyle;
import com.luciad.view.opengl.style.ILcdGLStyleMode;
import com.luciad.view.opengl.style.TLcdGLColorStyle;
import samples.opengl.common.GLViewSupport;

import java.awt.Color;

/**
 * The layer factory for the sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  public static final String ALPS = "Alps";
  
  private static TLcdGLAboveGroundPointlistPainter fAboveGroundPainter = new TLcdGLAboveGroundPointlistPainter();
  private static TerrainPaintableFactory           fPaintableFactory   = new TerrainPaintableFactory( fAboveGroundPainter );

  public ILcdGLLayer createLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    if ( modelDescriptor instanceof TLcdSHPModelDescriptor ) {
      return createPolylineLayer( aModel, aTargetView );
    }
    else if ( modelDescriptor instanceof TLcdTerrainModelDescriptor ) {
      return createTerrainLayer( aModel, aTargetView );
    }
    else {
      return null;
    }
  }

  public boolean isValidModel( ILcdModel aModel, ILcdGLView aTargetView ) {
    return true;
  }

  public static ILcdGLLayer createTerrainLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    // Setup the 'above ground' parts
    TLcdGLLayer terrainLayer = new TLcdGLLayer( aModel );
    terrainLayer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    // Add a polygon offset to the layer. This ensures that the vector data will be drawn on top of the terrain
    terrainLayer.setLayerStyle( new PolygonOffsetStyle( 5f, 5f ) );
    terrainLayer.setPainter( new TLcdGLTerrainPainter( fPaintableFactory ) );
    terrainLayer.setLabel( ALPS );
    return terrainLayer;
  }

  public static ILcdGLLayer createPolylineLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    layer.setLayerStyle( new PolylineStyle( Color.blue, 2f ) );

    TLcdGLShapeListPainter shapeListPainter = new TLcdGLShapeListPainter();
    shapeListPainter.setPainter( fAboveGroundPainter );

    layer.setPainter( shapeListPainter );
    layer.setLabel( "Rivers" );
    return layer;
  }

  public static TLcdGLAboveGroundPointlistPainter getAboveGroundPainter() {
    return fAboveGroundPainter; 
  }

  private static class PolygonOffsetStyle implements ILcdGLStyle {
    private boolean fWasEnabled;
    private float fFactor;
    private float fUnits;

    public PolygonOffsetStyle( float aFactor, float aUnits ) {
      fFactor = aFactor;
      fUnits = aUnits;
    }

    public void setUp( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext ) {
      ILcdGL gl = aGLDrawable.getGL();

      fWasEnabled = gl.glIsEnabled( ILcdGL.GL_POLYGON_OFFSET_FILL );
      if ( !fWasEnabled ) {
        gl.glEnable( ILcdGL.GL_POLYGON_OFFSET_FILL );
      }
      gl.glPolygonOffset( fFactor, fUnits );
    }

    public void cleanUp( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext ) {
      ILcdGL gl = aGLDrawable.getGL();

      if ( !fWasEnabled ) {
        gl.glDisable( ILcdGL.GL_POLYGON_OFFSET_FILL );
      }
    }
  }

  private static class PolylineStyle implements ILcdGLStyle {
    private TLcdGLColorStyle fColorStyle;
    private float fWidth;


    public PolylineStyle( Color aColor, float aWidth ) {
      fColorStyle = new TLcdGLColorStyle( aColor );
      fWidth = aWidth;
    }

    public void setUp( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext ) {
      ILcdGL gl = aGLDrawable.getGL();
      gl.glLineWidth( fWidth );

      fColorStyle.setUp( aGLDrawable, aObject, aMode, aStyleMode, aGLContext );
    }

    public void cleanUp( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLStyleMode aStyleMode, ILcdGLContext aGLContext ) {
      fColorStyle.cleanUp( aGLDrawable, aObject, aMode, aStyleMode, aGLContext );

      ILcdGL gl = aGLDrawable.getGL();
      gl.glLineWidth( 1f );
    }
  }
}
