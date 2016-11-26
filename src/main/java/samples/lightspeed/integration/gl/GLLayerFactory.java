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
package samples.lightspeed.integration.gl;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdRasterOffsetModelXYZWorldTransformation;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.TLcdGUIIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.TLcdGLGeodeticPathFactory;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintablefactory.*;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.painter.TLcdGL3DIconPainter;
import com.luciad.view.opengl.painter.TLcdGLIconPainter2;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter2;
import com.luciad.view.opengl.painter.TLcdGLShapeListPainter;
import com.luciad.view.opengl.painter.TLcdGLTextureFontLabelPainter;
import com.luciad.view.opengl.style.TLcdGLFillStyle;
import com.luciad.view.opengl.style.TLcdGLLineStyle;
import com.luciad.view.opengl.style.TLcdGLNoDepthWriteStyle;
import com.luciad.view.opengl.style.TLcdGLOutlineStyle;
import samples.opengl.common.BasicFillStyleProvider;
import samples.opengl.common.GLViewSupport;
import samples.opengl.common.RedBluePainterStyle;
import samples.opengl.common.SelectionToggleFillStyleProvider;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

class GLLayerFactory {

  private final BasicFillStyleProvider fShapeFillStyleProvider = new BasicFillStyleProvider();

  private final double fMinZ;
  private final double fMaxZ;

  /**
   * Creates a new <code>GLLayerFactory</code>
   */
  public GLLayerFactory(){
    fMinZ = 0.0;
    fMaxZ = 20000.0;
  }

  /*
  * Initializes an ellipse layer
  */
  private void initEllipseLayer( ILcdModel aModel, TLcdGLLayer aNewLayer ) {
    aNewLayer.setModel( aModel );
    aNewLayer.setLabel( aModel.getModelDescriptor().getTypeName() );
    ILcdModelXYZWorldTransformation transformation = new TLcdDefaultModelXYZWorldTransformation();

    aNewLayer.setModelXYZWorldTransformation( transformation );
    aNewLayer.setPathFactory( new TLcdGLGeodeticPathFactory() );
    TLcdGLExtrudedEllipsePaintableFactory paintableFactory =
            new TLcdGLExtrudedEllipsePaintableFactory();
    paintableFactory.setDefaultMinimumZ( 0 );
    paintableFactory.setDefaultMaximumZ( 50000 );
    TLcdGLPaintablePainter painter = new TLcdGLPaintablePainter( paintableFactory );
    painter.setPaintFill( true );
    painter.setPaintOutline( true );
    painter.setFillStyle( new RedBluePainterStyle() );
    painter.setOutlineStyle(new TLcdGLLineStyle() );

    aNewLayer.setPainter( painter );
  }
  /**
   * Initializes a layer for points that are drawn with a 3D icon.
   */
  private void initPoint3DLayer( ILcdModel aModel, TLcdGLLayer aNewLayer ) {
    aNewLayer.setModel( aModel );
    aNewLayer.setLabel( aModel.getModelDescriptor().getTypeName() );
    aNewLayer.setPathFactory( GLViewSupport.createPathFactory( aModel.getModelReference() ));

    TLcdGL3DIconPainter painter = new TLcdGL3DIconPainter();
    painter.setIconScale( 1000 );
    painter.setIconProvider( new ObjectIconProvider() );
    painter.setDrawVerticalLine( true );
    painter.setVerticalLineColor( Color.green );
    painter.setVerticalOffsetFactor(1);
    aNewLayer.setPainter( painter );

    aNewLayer.setSelectable(false);

  }
  /*
  *  Initializes a layer for 2D points
  */
  private void initPoint2DLayer( ILcdModel aModel, TLcdGLLayer aNewLayer ) {
    aNewLayer.setModel( aModel );
    aNewLayer.setLabel( aModel.getModelDescriptor().getTypeName() );
    TLcdDefaultModelXYZWorldTransformation xyz_transformation = new TLcdDefaultModelXYZWorldTransformation();
    TLcdRasterOffsetModelXYZWorldTransformation offset_trans = new TLcdRasterOffsetModelXYZWorldTransformation(xyz_transformation);
    aNewLayer.setModelXYZWorldTransformation(offset_trans);
    aNewLayer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    // Load elevation to use with the raster offset transformation.
    try {
      TLcdDMEDModelDecoder d = new TLcdDMEDModelDecoder( TLcdSharedBuffer.getBufferInstance());
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
    aNewLayer.setPainter( painter );
    aNewLayer.setSelectable(false);
  }
  /*
  * Initializes a layer for a SHP model.
  */
  private void initShpPolygonLayer( ILcdModel aModel, TLcdGLLayer aNewLayer ) {
    aNewLayer.setModel( aModel );
    aNewLayer.setLabel( aModel.getModelDescriptor().getTypeName() );
    aNewLayer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    TLcdGLPaintablePainter2 p = new TLcdGLPaintablePainter2(new TLcdGLComplexPolygonPaintableFactory(), true);
    p.setPaintFill( true );
    p.setPaintOutline( true );
    p.setFillStyleProvider( new SelectionToggleFillStyleProvider(
        new TLcdGLFillStyle( Color.cyan, 0.3f ),
        new TLcdGLFillStyle( Color.red, 0.6f )
    ) );
    p.setOutlineStyleProvider(new TLcdGLOutlineStyle(Color.black));
    aNewLayer.setTransparencyEnabled( true );
    // Wrap the painter in a shape list painter.
    aNewLayer.setPainter( new TLcdGLShapeListPainter(p) );

    aNewLayer.setSelectable( true );
    aNewLayer.setLayerStyle(new TLcdGLNoDepthWriteStyle());

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
        ILcdBounds bounds = ((ILcdBounded ) aObject).getBounds();
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
    aNewLayer.setLabelPainter(label_painter);
    aNewLayer.setLabeledSupported( true );
    aNewLayer.setLabeled( true );

    aNewLayer.setDrapingEnabled( true );
  }

  /**
   * Initializes a layer that can be used with any type of shape. The layer uses a
   * TLcdGLPaintablePainter, whose paintable factory is supplied as an argument
   * to this method.
   */
  private void initShapeLayer( ILcdModel aModel, ILcdGLPaintableFactory aPaintableFactory, TLcdGLLayer aNewLayer ) {
        aNewLayer.setModel( aModel );
    aNewLayer.setLabel( aModel.getModelDescriptor().getTypeName() );
    aNewLayer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    TLcdGLPaintablePainter2 painter = new TLcdGLPaintablePainter2(aPaintableFactory, true);
    painter.setDecalOutlines(true);
    painter.setPaintFill( true );
    painter.setPaintOutline( true );
    painter.setOutlineStyleProvider(new TLcdGLOutlineStyle(Color.black));
    painter.setFillStyleProvider(new SelectionToggleFillStyleProvider(
            fShapeFillStyleProvider,
            new TLcdGLFillStyle(Color.red)
          ));

    aNewLayer.setPainter( painter );
    aNewLayer.setSelectable( true );

  }

  /**
   * Initializes a <code>ILcdGLLayer</code> for models created by {@link ModelFactory}.
   *
   * @param aModel a model created by {@link ModelFactory}.
   * @param aLayerSFCT The layer to initialize.
   */
  public void initLayerSFCT( ILcdModel aModel, TLcdGLLayer aLayerSFCT ) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    if ( typeName.equals( ModelFactory.ELLIPSE_MODEL_TYPE_NAME ) ) {
      initEllipseLayer( aModel, aLayerSFCT );
    }
    else if ( typeName.equals( ModelFactory.POINT3D_MODEL_TYPE_NAME ) ) {
      initPoint3DLayer( aModel, aLayerSFCT );
    }
    else if ( typeName.equals( ModelFactory.POINT2D_MODEL_TYPE_NAME ) ) {
      initPoint2DLayer( aModel, aLayerSFCT );
    }
    else if ( typeName.equals( ModelFactory.SHP_MODEL_TYPE_NAME ) ) {
      initShpPolygonLayer( aModel, aLayerSFCT );
    }
    else if(typeName.equals( ModelFactory.ARCBAND_MODEL_TYPE_NAME )){
      initShapeLayer( aModel, new TLcdGLExtrudedArcBandPaintableFactory( fMinZ, fMaxZ ), aLayerSFCT );
    }
    else if (typeName.equals(ModelFactory.COMPLEXPOLY_MODEL_TYPE_NAME)) {
      initShapeLayer( aModel, new TLcdGLExtrudedComplexPolygonPaintableFactory( fMinZ, fMaxZ ), aLayerSFCT );
    }
    else if (typeName.equals(ModelFactory.LLH_BUFFER_MODEL_TYPE_NAME)) {
      initShapeLayer( aModel, new TLcdGLLonLatHeightBufferPaintableFactory(), aLayerSFCT );
    }
    else if (typeName.equals(ModelFactory.LL_BUFFER_MODEL_TYPE_NAME)) {
      initShapeLayer( aModel, new TLcdGLExtrudedLonLatBufferPaintableFactory( fMinZ, fMaxZ ), aLayerSFCT );
    }
    else if (typeName.equals(ModelFactory.SPHERE_MODEL_TYPE_NAME)) {
      initShapeLayer( aModel, new TLcdGLSpherePaintableFactory(), aLayerSFCT );
    }
    else if (typeName.equals(ModelFactory.DOME_MODEL_TYPE_NAME)) {
      initShapeLayer( aModel, new TLcdGLDomePaintableFactory(), aLayerSFCT );
    }
    else if (typeName.equals(ModelFactory.CIRCLE_MODEL_TYPE_NAME)) {
      initShapeLayer( aModel, new TLcdGLExtrudedCirclePaintableFactory( fMinZ, fMaxZ ), aLayerSFCT );
    }
    else if (typeName.equals(ModelFactory.POLYGON_MODEL_TYPE_NAME)) {
      initShapeLayer( aModel, new TLcdGLExtrudedPolygonPaintableFactory( fMinZ, fMaxZ ), aLayerSFCT );
    }
  }















}
