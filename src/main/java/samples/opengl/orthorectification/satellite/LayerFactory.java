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
package samples.opengl.orthorectification.satellite;

import com.luciad.format.raster.terrain.TLcdTerrainModelDescriptor;
import com.luciad.format.raster.terrain.opengl.*;
import com.luciad.format.raster.terrain.opengl.paintable.*;
import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.binding.*;
import com.luciad.view.opengl.paintable.ILcdGLPaintable;
import com.luciad.view.opengl.paintablefactory.*;
import com.luciad.view.opengl.painter.*;
import com.luciad.view.opengl.style.*;
import samples.opengl.common.*;

import java.awt.*;

/**
 * The layer factory for the 3D sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  private TerrainPaintableFactory fPaintableFactory = new TerrainPaintableFactory();


  // Implementations for ILcdGLLayerFactory.

  public boolean isValidModel(ILcdModel aModel, ILcdGLView aTargetView) {
    return true;
  }

  public ILcdGLLayer createLayer(ILcdModel aModel, ILcdGLView aTargetView) {

    String name = aModel.getModelDescriptor().getTypeName();
    if (name.equals("Grid")) {
      return createGridLayer(aModel);
    } else if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      /* For SHP files, check the shape type to decide which painter to use. We
         assume, for convenience, that there is only one type of geometry in
         the file: either polygons or polylines. */
      TLcdSHPModelDescriptor shp_desc = (TLcdSHPModelDescriptor)aModel.getModelDescriptor();
      int[] shapeTypes = shp_desc.getShapeTypes();
      for (int i = 0; i < shapeTypes.length; i++) {
        if ((shapeTypes[i] == TLcdSHPModelDescriptor.POLYGON) ||
            (shapeTypes[i] == TLcdSHPModelDescriptor.POLYGON_M) ||
            (shapeTypes[i] == TLcdSHPModelDescriptor.POLYGON_Z)) {
          return createPolygonLayer(aModel);
        }
        if ((shapeTypes[i] == TLcdSHPModelDescriptor.POLYLINE) ||
            (shapeTypes[i] == TLcdSHPModelDescriptor.POLYLINE_M) ||
            (shapeTypes[i] == TLcdSHPModelDescriptor.POLYLINE_Z)) {
          return createPolylineLayer(aModel);
        }
      }
      // If there are no polylines or polygons present in the file, no layer is created.
      return null;
    } else if (aModel.getModelDescriptor() instanceof TLcdTerrainModelDescriptor) {
      return createTerrainLayer(aModel);
    } else {
      return null;
    }
  }

  
  /**
   * Creates a grid layer. In a geocentric reference, the grid can be used to
   * display the globe. It is best drawn opaquely, as being able to look through
   * it can be confusing to the user. In this case, we add a
   * TLcdGL2DTextureStyle to make the grid look like a convincing representation
   * of the Earth.
   */
  private ILcdGLLayer createGridLayer(ILcdModel aModel) {

    // Add a texture to the grid.
    TLcdGL2DTextureStyle textureStyle = new TLcdGL2DTextureStyle();
    TLcdGLSingleTextureFactory textureFactory = new TLcdGLSingleTextureFactory();
    textureFactory.setImage(TLcdImageIcon.getImage("background/geocentric/bluemarble2048.png"));
    textureStyle.setTextureFactory(textureFactory);

    // Use a polygon offset style to avoid visual interference ("z fighting")
    // between the grid and other layers.
    TLcdGLPolygonOffsetStyle polygonStyle = new TLcdGLPolygonOffsetStyle(10, 10);

    // Combine the above styles into a composite
    TLcdGLCompositeStyle style = new TLcdGLCompositeStyle();
    style.addStyle(textureStyle);
    style.addStyle(polygonStyle);

    // Create a painter.
    TLcdGLLonLatGridPainter painter = new TLcdGLLonLatGridPainter();
    painter.setPaintFill(true);
    painter.setPaintOutline(false);
    painter.setDrawZeroSeparately(true);
    painter.setFillStyle(style);
    painter.setOutlineStyle(new TLcdGLColorStyle(Color.blue));
    painter.setZeroStyle(new TLcdGLColorStyle(Color.red));

    // Create a layer.
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    layer.setPainter(painter);
    layer.setSelectable(false);
    layer.setLabel("Earth");

    return layer;
  }


  /**
   * Creates a layer for complex polygon shapes. Because the data is 2D, we use
   * a TLcdGLNoDepthWriteStyle to avoid visual interference between the polygons
   * and the grid layer.
   */
  private ILcdGLLayer createPolygonLayer(ILcdModel aModel) {

    // Create a painter.
    TLcdGLPaintablePainter painter = new TLcdGLPaintablePainter(new TLcdGLComplexPolygonPaintableFactory());
    painter.setPaintFill(true);
    painter.setPaintOutline(true);
    painter.setFillStyle(new TLcdGLColorStyle(Color.cyan, 0.3f));
    painter.setOutlineStyle(new TLcdGLColorStyle(Color.black));

    // Create a layer.
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    layer.setLayerStyle(new TLcdGLNoDepthWriteStyle());
    layer.setTransparencyEnabled(true);
    layer.setPainter(new TLcdGLShapeListPainter(painter));
    layer.setSelectable(false);
    layer.setLabel("Countries");

    return layer;
  }


  /**
   * Creates a layer for terrain.
   */
  private ILcdGLLayer createTerrainLayer(ILcdModel aModel) {

    // Create a painter.
    TLcdGLTerrainPainter painter = new TLcdGLTerrainPainter(fPaintableFactory);
    painter.setPaintFill(true);
    painter.setPaintOutline(false);

    // Create a layer.
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    layer.setPainter(painter);
    layer.setSelectable(false);
    layer.setLabel("Terrain");

    return layer;
  }


  /**
   * Creates a layer for polylines on top of the terrain.
   */
  private ILcdGLLayer createPolylineLayer(ILcdModel aModel) {

    // Create a painter.
    TLcdGLShapeListPainter shapeListPainter = new TLcdGLShapeListPainter();
    shapeListPainter.setPainter(fPaintableFactory.fPainter);

    // Create a layer.
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    layer.setLayerStyle(new TLcdGLCompositeStyle(new ILcdGLStyle[]{
      new TLcdGLNoDepthTestStyle(),
      new TLcdGLColorStyle(new Color(0x6080a0)),
      new TLcdGLLineStyle(1.5f, true),
    }));
    layer.setPainter(shapeListPainter);
    layer.setSelectable(false);
    layer.setLabel("Rivers");

    return layer;
  }


  /**
   * This paintable factory remembers the terrain paintable that it creates,
   * attaching it to its point list painter. The point list painter can then be
   * used for painting polylines on top of the terrain.
   */
  private static class TerrainPaintableFactory
    implements ILcdGLPaintableFactory {

    private TLcdGLTerrainPaintableFactory fPaintableFactory = new TLcdGLTerrainPaintableFactory();
    private TLcdGLAboveGroundPointlistPainter fPainter = new TLcdGLAboveGroundPointlistPainter();


    // Implementations for ILcdGLPaintableFactory.

    public ILcdGLPaintable createPaintable(ILcdGLDrawable aGLDrawable, Object aObject, TLcdGLPaintableFactoryMode aMode, ILcdGLContext aContext) {

      TLcdGLTerrainPaintable paintable =
        (TLcdGLTerrainPaintable)fPaintableFactory.createPaintable(aGLDrawable, aObject, aMode, aContext);

      // Remember the terrain paintable for painting the polylines later on.
      fPainter.setTerrainPaintable(paintable);

      return paintable;
    }

    public boolean isModeSupported(TLcdGLPaintableFactoryMode aMode) {
      return fPaintableFactory.isModeSupported(aMode);
    }
  }
}
