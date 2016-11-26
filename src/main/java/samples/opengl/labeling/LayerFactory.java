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

package samples.opengl.labeling;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.TLcdInterval;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintable.ILcdGLPaintable;
import com.luciad.view.opengl.paintable.TLcdGLCompositePaintable;
import com.luciad.view.opengl.paintablefactory.ILcdGLPaintableFactory;
import com.luciad.view.opengl.paintablefactory.TLcdGLComplexPolygonPaintableFactory;
import com.luciad.view.opengl.paintablefactory.TLcdGLExtrudedEllipsePaintableFactory;
import com.luciad.view.opengl.paintablefactory.TLcdGLPaintableFactoryMode;
import com.luciad.view.opengl.paintablefactory.TLcdGLPointListPaintableFactory;
import com.luciad.view.opengl.paintablefactory.TLcdGLPolylinePaintableFactory;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.painter.TLcdGLFont;
import com.luciad.view.opengl.painter.TLcdGLIconPainter2;
import com.luciad.view.opengl.painter.TLcdGLLabelPainter;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter2;
import com.luciad.view.opengl.painter.TLcdGLShapeListPainter;
import com.luciad.view.opengl.painter.TLcdGLTextureFontLabelPainter;
import com.luciad.view.opengl.style.TLcdGLFillStyle;
import com.luciad.view.opengl.style.TLcdGLNoDepthTestStyle;
import com.luciad.view.opengl.style.TLcdGLOutlineStyle;
import samples.opengl.common.GLViewSupport;
import samples.opengl.common.RedBlueFillStyleProvider;
import samples.opengl.common.SelectionToggleFillStyleProvider;
import samples.opengl.common.SelectionToggleOutlineStyleProvider;

import java.awt.Color;
import java.awt.Font;

/**
 * The layer factory for the sample application.
 */
class LayerFactory implements ILcdGLLayerFactory {

  private Font fAWTFont = new Font("Dialog", Font.BOLD, 12);
  private TLcdGLFont fFont = new TLcdGLFont(new Font("Dialog", Font.BOLD, 12));
  private double fUnitOfMeasure;

  public LayerFactory(double aUnitOfMeasure) {
    super();
    fUnitOfMeasure = aUnitOfMeasure;
  }

  public ILcdGLLayer createLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    if (modelDescriptor instanceof TLcdSHPModelDescriptor) {
      return createSHPLayer(aModel, aTargetView);
    }
    if (typeName.equals(ModelFactory.ELLIPSE_MODEL_TYPE_NAME)) {
      return createEllipseLayer(aModel, aTargetView);
    }
    else if (typeName.equals(ModelFactory.POINT_MODEL_TYPE_NAME)) {
      return createPointLayer(aModel, aTargetView);
    }
    else if (typeName.equals(ModelFactory.POLYLINE_MODEL_TYPE_NAME)) {
      return createPolylineLayer(aModel, aTargetView);
    }
    else if (typeName.equals(ModelFactory.GRID_MODEL_TYPE_NAME)) {
      return GLViewSupport.createGridLayer(aModel, aTargetView);
    }
    else {
      return null;
    }
  }

  public boolean isValidModel(ILcdModel aModel, ILcdGLView aTargetView) {
    ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
    String typeName = modelDescriptor.getTypeName();
    return ((modelDescriptor instanceof TLcdSHPModelDescriptor) ||
            typeName.equals(ModelFactory.ELLIPSE_MODEL_TYPE_NAME) ||
            typeName.equals(ModelFactory.POINT_MODEL_TYPE_NAME) ||
            typeName.equals(ModelFactory.POLYLINE_MODEL_TYPE_NAME) ||
            typeName.equals(ModelFactory.GRID_MODEL_TYPE_NAME
            )
    );
  }

  private ILcdGLLayer createSHPLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    TLcdGLComplexPolygonPaintableFactory paintableFactory = new TLcdGLComplexPolygonPaintableFactory();
    TLcdGLPaintablePainter2 painter = new TLcdGLPaintablePainter2(paintableFactory);
    painter.setPaintFill(true);
    painter.setPaintOutline(true);
    painter.setFillStyleProvider(
            new SelectionToggleFillStyleProvider(
                    new TLcdGLFillStyle(Color.gray),
                    new TLcdGLFillStyle(Color.red)
            )
    );
    painter.setOutlineStyleProvider(new TLcdGLOutlineStyle(Color.black));
    layer.setPainter(new TLcdGLShapeListPainter(painter));

    // Use a texture font label painter for this layer.
    TLcdGLTextureFontLabelPainter label_painter = new TLcdGLTextureFontLabelPainter(
            fAWTFont, Color.black, Color.white
    );
    // This interval specifies the distance range at which distant labels fade out.
    label_painter.setFarFadeOutRange(
            new TLcdInterval(5000000 / fUnitOfMeasure, 7500000 / fUnitOfMeasure)
    );

    layer.setLabelPainter(label_painter);
    layer.setLabeledSupported(true);
    layer.setLabeled(true);

    layer.setSelectable(true);

    return layer;
  }

  private ILcdGLLayer createEllipseLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    TLcdGLExtrudedEllipsePaintableFactory paintableFactory =
            new TLcdGLExtrudedEllipsePaintableFactory();
    paintableFactory.setDefaultMinimumZ(0);
    paintableFactory.setDefaultMaximumZ(50000);
    TLcdGLPaintablePainter2 painter = new TLcdGLPaintablePainter2(paintableFactory);
    painter.setPaintFill(true);
    painter.setPaintOutline(true);
    painter.setFillStyleProvider(new RedBlueFillStyleProvider());
    painter.setOutlineStyleProvider(new TLcdGLOutlineStyle(Color.white));
    layer.setPainter(painter);

    // Use an ALcdGLImageLabelPainter for this layer. The sample implementation
    // used here draws labels which contain a string with an oval frame around
    // it.
    ImageLabelPainter label_painter = new ImageLabelPainter(fAWTFont) {
      protected String retrieveLabel(Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext) {
        return aContext.getLayer().getLabel();
      }
    };
    // Add a one pixel white halo around the labels.
    label_painter.setHaloColor(Color.white);
    label_painter.setHaloThickness(1);
    label_painter.setHaloEnabled(true);
    // This interval specifies the distance range at which distant labels fade out.
    label_painter.setFarFadeOutRange(
            new TLcdInterval(1000000 / fUnitOfMeasure, 1500000 / fUnitOfMeasure)
    );
    // This interval specifies the distance range at which nearby labels fade out.
    label_painter.setNearFadeOutRange(
            new TLcdInterval(100000 / fUnitOfMeasure, 150000 / fUnitOfMeasure)
    );
    label_painter.setVerticalOffset(30);
    label_painter.setWithPin(true);
    label_painter.setPinColor(Color.black);
    label_painter.setLabelLocation(ImageLabelPainter.TOP_CENTER);

    layer.setLabelPainter(label_painter);
    layer.setLabeledSupported(true);
    layer.setLabeled(true);

    return layer;
  }

  private ILcdGLLayer createPointLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    // Paint all points with a default icon.
    TLcdGLIconPainter2 painter = new TLcdGLIconPainter2();
    painter.setSizeFactor(1);
    painter.setIconScalingMode(TLcdGLIconPainter2.IconScalingMode.VIEW);
    painter.setIconProvider(new ILcdObjectIconProvider() {
      private ILcdIcon fIcon = new TLcdImageIcon("images/icons/globe_32.png");

      public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
        return fIcon;
      }

      public boolean canGetIcon(Object aObject) {
        return true;
      }
    });
    painter.setSelectionIconProvider(new ILcdObjectIconProvider() {
      private ILcdIcon fIcon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 32, Color.red);

      public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
        return fIcon;
      }

      public boolean canGetIcon(Object aObject) {
        return true;
      }
    });
    layer.setPainter(painter);

    // Use a TLcdGLLabelPainter for this layer.
    TLcdGLLabelPainter label_painter = new TLcdGLLabelPainter(fFont) {
      protected String[] retrieveLabel(
              ILcdGLDrawable aGLDrawable,
              Object aObject,
              ILcdGLPaintMode aMode,
              ILcdGLContext aContext
            ) {
        return new String[]{aContext.getLayer().getLabel()};
      }
    };
    // Add a filled background frame to the labels.
    label_painter.setFilled(true);
    label_painter.setFrame(true);
    label_painter.setBackground(Color.orange);
    // Set the spacing between the frame and the label text to 5 pixels on all sides.
    label_painter.setBorderWidth(5);
    // Draw labels in overlay mode, so they cannot be obscured by other objects in the view.
    label_painter.setOverlayLabels(true);
    // Set the location of the label relative to the object's anchor point.
    label_painter.setLabelLocation(TLcdGLLabelPainter.CENTER_LEFT);

    layer.setLabelPainter(label_painter);
    layer.setLabeledSupported(true);
    layer.setLabeled(true);

    return layer;
  }

  private ILcdGLLayer createPolylineLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer(aModel);
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));

    MyPolylinePaintableFactory paintableFactory = new MyPolylinePaintableFactory();
    TLcdGLPaintablePainter2 painter = new TLcdGLPaintablePainter2(paintableFactory);
    painter.setPaintFill(false);
    painter.setPaintOutline(true);

    TLcdGLOutlineStyle cyanStyle = new TLcdGLOutlineStyle( Color.cyan );
    cyanStyle.getLineStyle().setWidth( 2 );
    cyanStyle.getLineStyle().setSmooth( true );
    cyanStyle.getPointStyle().setSize( 6 );
    cyanStyle.getPointStyle().setSmooth( true );

    TLcdGLOutlineStyle redStyle = new TLcdGLOutlineStyle( Color.red );
    redStyle.getLineStyle().setWidth( 2 );
    redStyle.getLineStyle().setSmooth( true );
    redStyle.getPointStyle().setSize( 6 );
    redStyle.getPointStyle().setSmooth( true );

    painter.setOutlineStyleProvider(
        new SelectionToggleOutlineStyleProvider(
            cyanStyle,
            redStyle
        )
    );

    layer.setPainter(painter);

    // Use an extension of TLcdGLLabelPainter which decreases the font size as
    // the distance between the label and the viewer grows larger.
    TLcdGLLabelPainter label_painter = new ScalingLabelPainter(fAWTFont);
    label_painter.setDefaultColor(Color.white);
    label_painter.setSelectedColor(Color.red);
    label_painter.setLabelLocation(TLcdGLLabelPainter.TOP_RIGHT);

    layer.setLabelPainter(label_painter);
    layer.setLabeledSupported(true);
    layer.setLabeled(true);

    return layer;
  }

  /**
   * A paintable factory that creates both a polyline and the corresponding vertical lines.
   */
  private static class MyPolylinePaintableFactory implements ILcdGLPaintableFactory {
    VerticalLinesPaintableFactory fPaintableFactory =
            new VerticalLinesPaintableFactory();
    TLcdGLPointListPaintableFactory fPaintableFactory2 =
            new TLcdGLPointListPaintableFactory();
    TLcdGLPolylinePaintableFactory fPaintableFactory3 =
            new TLcdGLPolylinePaintableFactory();

    public ILcdGLPaintable createPaintable(ILcdGLDrawable aGLDrawable, Object aObject, TLcdGLPaintableFactoryMode aMode, ILcdGLContext aContext) {
      TLcdGLCompositePaintable paintable = new TLcdGLCompositePaintable();
      paintable.addPaintable(fPaintableFactory.createPaintable(aGLDrawable, aObject, aMode, aContext));
      paintable.addPaintable(fPaintableFactory2.createPaintable(aGLDrawable, aObject, aMode, aContext));
      paintable.addPaintable(fPaintableFactory3.createPaintable(aGLDrawable, aObject, aMode, aContext));
      return paintable;
    }

    public boolean isModeSupported(TLcdGLPaintableFactoryMode aMode) {
      return fPaintableFactory.isModeSupported(aMode) && fPaintableFactory2.isModeSupported(aMode) && fPaintableFactory3.isModeSupported(aMode);
    }
  }
}
