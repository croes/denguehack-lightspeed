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

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdRasterOffsetModelXYZWorldTransformation;
import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.TLcdGUIIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintablefactory.*;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.painter.TLcdGL3DIconPainter;
import com.luciad.view.opengl.painter.TLcdGLIconPainter2;
import com.luciad.view.opengl.painter.TLcdGLLonLatGridPainter;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter2;
import com.luciad.view.opengl.painter.TLcdGLShapeListPainter;
import com.luciad.view.opengl.painter.TLcdGLTextureFontLabelPainter;
import com.luciad.view.opengl.style.TLcdGL2DTextureStyle;
import com.luciad.view.opengl.style.TLcdGLColorStyle;
import com.luciad.view.opengl.style.TLcdGLCompositeStyle;
import com.luciad.view.opengl.style.TLcdGLFillStyle;
import com.luciad.view.opengl.style.TLcdGLNoDepthWriteStyle;
import com.luciad.view.opengl.style.TLcdGLOutlineStyle;
import com.luciad.view.opengl.style.TLcdGLPolygonOffsetStyle;
import com.luciad.view.opengl.style.TLcdGLSingleTextureFactory;
import samples.opengl.common.BasicColorPainterStyle;
import samples.opengl.common.BasicFillStyleProvider;
import samples.opengl.common.GLViewSupport;
import samples.opengl.common.SelectionToggleFillStyleProvider;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

/**
 * The layer factory for the geocentric sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  private final BasicColorPainterStyle fShapeColorStyle = new BasicColorPainterStyle();

  private final BasicFillStyleProvider fShapeFillStyleProvider = new BasicFillStyleProvider();


  public boolean isValidModel(ILcdModel aModel, ILcdGLView aTargetView) {
    return true;
  }

  public ILcdGLLayer createLayer(ILcdModel aModel, ILcdGLView aTargetView) {

    double minz = 0;
    double maxz = 20000;

    String name = aModel.getModelDescriptor().getTypeName();
    if (name.equals(ModelFactory.GRID_MODEL_TYPE_NAME)) {
      return createGridLayer(aModel, aTargetView);
    }
    else if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      /* For SHP files, check the shape type to decide which painter to use. We
         assume, for convenience, that there is only one type of geometry in
         the file: either polygons or polylines. */
      TLcdSHPModelDescriptor shp_desc = (TLcdSHPModelDescriptor) aModel.getModelDescriptor();
      int[] shapeTypes = shp_desc.getShapeTypes();
      for (int i = 0; i < shapeTypes.length; i++) {
        if ((shapeTypes[i] == TLcdSHPModelDescriptor.POLYGON) ||
            (shapeTypes[i] == TLcdSHPModelDescriptor.POLYGON_M) ||
            (shapeTypes[i] == TLcdSHPModelDescriptor.POLYGON_Z)) {
          return createShpPolygonLayer(aModel, aTargetView);
        }
      }
      // If there are no polylines or polygons present in the file, no layer is created.
      return null;
    }
    else if (name.equals(ModelFactory.POINT3D_MODEL_TYPE_NAME)) {
      return createPoint3DLayer(aModel, aTargetView);
    }
    else if (name.equals(ModelFactory.POINT2D_MODEL_TYPE_NAME)) {
      return createPoint2DLayer(aModel, aTargetView);
    }
    else if (name.equals(ModelFactory.POLYGON_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLExtrudedPolygonPaintableFactory(minz, maxz));
    }
    else if (name.equals(ModelFactory.ELLIPSE_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLExtrudedEllipsePaintableFactory(minz, maxz));
    }
    else if (name.equals(ModelFactory.ARCBAND_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLExtrudedArcBandPaintableFactory(minz, maxz));
    }
    else if (name.equals(ModelFactory.CIRCLE_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLExtrudedCirclePaintableFactory(minz, maxz));
    }
    else if (name.equals(ModelFactory.COMPLEXPOLY_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLExtrudedComplexPolygonPaintableFactory(minz, maxz));
    }
    else if (name.equals(ModelFactory.DOME_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLDomePaintableFactory());
    }
    else if (name.equals(ModelFactory.SPHERE_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLSpherePaintableFactory());
    }
    else if (name.equals(ModelFactory.LL_BUFFER_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLExtrudedLonLatBufferPaintableFactory(minz, maxz));
    }
    else if (name.equals(ModelFactory.LLH_BUFFER_MODEL_TYPE_NAME)) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLLonLatHeightBufferPaintableFactory());
    }
    else if (name.equals(ModelFactory.VARIABLE_GEO_BUFFER_MODEL_TYPE_NAME )) {
      return createShapeLayer(aModel, aTargetView, new TLcdGLLonLatHeightVariableGeoBufferPaintableFactory());
    }
    else {
      return null;
    }
  }

  /**
   * Creates a grid layer. In a geocentric reference, the grid can be used to
   * display the globe. It is best drawn opaquely, as being able to look
   * through it can be confusing to the user. In this case, we add a
   * TLcdGL2DTextureStyle to make the grid look like a convincing
   * representation of the Earth.
   */
  private ILcdGLLayer createGridLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    TLcdGLLonLatGridPainter painter = new TLcdGLLonLatGridPainter();
    painter.setPaintFill(true);
    painter.setPaintOutline(true);
    painter.setDrawZeroSeparately(true);

    // Add a texture style to the grid
    TLcdGL2DTextureStyle textureStyle = new TLcdGL2DTextureStyle();
    TLcdGLSingleTextureFactory textureFactory = new TLcdGLSingleTextureFactory();
    textureFactory.setImage(TLcdImageIcon.getImage("background/geocentric/bluemarble2048.png"));
    textureStyle.setTextureFactory(textureFactory);

    /* Use a polygon offset style to avoid visual interference ("z fighting")
       between the grid and other layers. */
    TLcdGLPolygonOffsetStyle poStyle = new TLcdGLPolygonOffsetStyle(10, 10);

    // Combine the above styles into a composite
    TLcdGLCompositeStyle style = new TLcdGLCompositeStyle();
    style.addStyle(textureStyle);
    style.addStyle(poStyle);

    painter.setFillStyle(style);
    painter.setOutlineStyle(new TLcdGLColorStyle(Color.blue));
    painter.setZeroStyle(new TLcdGLColorStyle(Color.red));

    layer.setPainter(painter);
    layer.setSelectable(false);

    return layer;
  }

  /**
   * Creates a layer that can be used with any type of shape. The layer uses a
   * TLcdGLPaintablePainter, whose paintable factory is supplied as an argument
   * to this method.
   */
  private ILcdGLLayer createShapeLayer(ILcdModel aModel, ILcdGLView aTargetView, ILcdGLPaintableFactory aPaintableFactory) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    TLcdGLPaintablePainter2 painter = new TLcdGLPaintablePainter2(aPaintableFactory, true);
    painter.setDecalOutlines(true);
    painter.setPaintFill( true );
    painter.setPaintOutline( true );
    painter.setOutlineStyleProvider(new TLcdGLOutlineStyle(Color.black));
    painter.setFillStyleProvider(new SelectionToggleFillStyleProvider(
        fShapeFillStyleProvider,
        new TLcdGLFillStyle(Color.red)
    ));

    layer.setPainter( painter );
    layer.setSelectable( true );

    return layer;
  }

  /**
   * Creates a layer for complex polygon shapes. Because the data is 2D, we use
   * a TLcdGLNoDepthWriteStyle to avoid visual interference between the
   * polygons and the grid layer. We also set a label painter on the layer.
   */
  private ILcdGLLayer createShpPolygonLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    TLcdGLPaintablePainter2 p = new TLcdGLPaintablePainter2(new TLcdGLComplexPolygonPaintableFactory(), true);
    p.setPaintFill(true);
    p.setPaintOutline(true);
    p.setFillStyleProvider(new SelectionToggleFillStyleProvider(
        new TLcdGLFillStyle(Color.cyan, 0.3f),
        new TLcdGLFillStyle(Color.red, 0.6f)
    ));
    p.setOutlineStyleProvider(new TLcdGLOutlineStyle(Color.black));
    layer.setTransparencyEnabled(true);
    // Wrap the painter in a shape list painter.
    layer.setPainter( new TLcdGLShapeListPainter(p) );

    layer.setSelectable( true );
    layer.setLayerStyle(new TLcdGLNoDepthWriteStyle());

    // Create a label painter.
    TLcdGLTextureFontLabelPainter label_painter = new TLcdGLTextureFontLabelPainter(
        new Font("Default", Font.BOLD, 12), Color.white, Color.black
    ) {
      protected String[] retrieveLabels(ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext) {
        return new String[] { aObject.toString() };
      }

      protected void anchorPointSFCT(ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext, ILcd3DEditablePoint aAnchorPointSFCT) throws TLcdNoBoundsException {
        /* Compute the anchor point in model coordinates rather than world
           coordinates, as the default implementation does. In world
           coordinates, the center of a shape's bounds will often be inside the
           globe, which is usually not very suitable for an anchor point. */
        ILcdBounds bounds = ((ILcdBounded) aObject).getBounds();
        ILcd2DEditablePoint p = bounds.getLocation().cloneAs2DEditablePoint();
        p.translate2D(bounds.getWidth() / 2.0, bounds.getHeight() / 2.0);
        try {
          aContext.getModelXYZWorldTransformation().modelPoint2worldSFCT(p, aAnchorPointSFCT);
        }
        catch (TLcdOutOfBoundsException e) {
          throw new TLcdNoBoundsException(e.getMessage());
        }
      }
    };
    label_painter.setLabelLocation(TLcdGLTextureFontLabelPainter.TOP_CENTER);
    label_painter.setFarFadeOutRange(new TLcdInterval(2500000, 3500000));
    label_painter.setOverlay(true);
    layer.setLabelPainter(label_painter);
    layer.setLabeledSupported(true);
    layer.setLabeled(true);

    return layer;
  }

  /**
   * Create a layer for points that are drawn with a 3D icon.
   */
  private ILcdGLLayer createPoint3DLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    TLcdGL3DIconPainter painter = new TLcdGL3DIconPainter();
    painter.setIconScale( 1000 );
    painter.setIconProvider( new ObjectIconProvider() );
    painter.setDrawVerticalLine( true );
    painter.setVerticalLineColor( Color.green );
    painter.setVerticalOffsetFactor(1);
    layer.setPainter( painter );

    layer.setSelectable(false);

    return layer;
  }

  /**
   * Create a layer for points that are drawn with a 2D icon. We use a
   * TLcdRasterOffsetModelXYZWorldTransformation with the layer, so that icons
   * are drawn on top of the terrain rather than at elevation = 0. 
   */
  private ILcdGLLayer createPoint2DLayer( ILcdModel aModel, ILcdGLView aTargetView ) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    TLcdDefaultModelXYZWorldTransformation xyz_transformation = new TLcdDefaultModelXYZWorldTransformation();
    TLcdRasterOffsetModelXYZWorldTransformation offset_trans = new TLcdRasterOffsetModelXYZWorldTransformation(xyz_transformation);
    layer.setModelXYZWorldTransformation(offset_trans);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    // Load elevation to use with the raster offset transformation.
    try {
      TLcdDMEDModelDecoder d = new TLcdDMEDModelDecoder(TLcdSharedBuffer.getBufferInstance());
      ILcdModel m = d.decode("Data/Dted/Alps/dmed");
      ILcdMultilevelRaster mlr = (ILcdMultilevelRaster) m.elements().nextElement();
      offset_trans.addRaster(mlr.getRaster(mlr.getRasterCount() - 1), m.getModelReference());
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    TLcdGLIconPainter2 painter = new TLcdGLIconPainter2();
    painter.setIconProvider(new ILcdObjectIconProvider() {
      private ILcdIcon fIcon = new TLcdGUIIcon(TLcdGUIIcon.GLOBE32);
      public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
        return fIcon;
      }
      public boolean canGetIcon(Object aObject) {
        return true;
      }
    });
    painter.setVerticalIconOffset(0.5f);
    painter.setSizeFactor(5000);
    painter.setIconScalingMode(TLcdGLIconPainter2.IconScalingMode.WORLD);
    layer.setPainter( painter );

    layer.setSelectable(false);

    return layer;
  }
}
