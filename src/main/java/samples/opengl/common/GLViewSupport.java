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
package samples.opengl.common;

import com.luciad.gui.TLcdGUIIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.painter.TLcdGLLonLatGridPainter;
import com.luciad.view.opengl.style.*;

import java.awt.*;

public class GLViewSupport {

  private GLViewSupport() {
  }

  /**
   * Creates an ILcdGLPathFactory for the given model reference.
   * @param aModelReference the model reference
   * @return an ILcdGLPathFactory that can be used to draw shapes defined against aModelReference
   */
  public static ILcdGLPathFactory createPathFactory(
          ILcdModelReference aModelReference
        ) {
    if (aModelReference instanceof ILcdGeodeticReference) {
      return new TLcdGLGeodeticPathFactory();
    }
    else {
      return new TLcdGLCartesianPathFactory();
    }
  }

  public static ILcdGLLayer createGridLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    TLcdGLLonLatGridPainter painter = new TLcdGLLonLatGridPainter();
    painter.setPaintFill( true );
    painter.setPaintOutline( true );
    painter.setDrawZeroSeparately( true );

    // Add a texture style to the grid
    TLcdGL2DTextureStyle textureStyle = new TLcdGL2DTextureStyle();
    TLcdGLSingleTextureFactory textureFactory = new TLcdGLSingleTextureFactory();
    textureFactory.setImage(TLcdImageIcon.getImage("background/geocentric/bluemarble2048.png"));
    textureStyle.setTextureFactory(textureFactory);

    if (aTargetView.getXYZWorldReference() instanceof ILcdGeocentricReference) {
      /* Use a polygon offset style to avoid visual interference ("z fighting")
         between the grid and other layers. */
      TLcdGLPolygonOffsetStyle poStyle = new TLcdGLPolygonOffsetStyle(10, 10);

      // Combine the above styles into a composite
      TLcdGLCompositeStyle style = new TLcdGLCompositeStyle();
      style.addStyle(textureStyle);
      style.addStyle(poStyle);

      painter.setFillStyle(style);
    }
    else {
      painter.setFillStyle(textureStyle);
    }
    painter.setOutlineStyle(new TLcdGLColorStyle(Color.blue));
    painter.setZeroStyle(new TLcdGLColorStyle(Color.red));

    if (!(aTargetView.getXYZWorldReference() instanceof ILcdGeocentricReference)) {
      layer.setLayerStyle(new TLcdGLNoDepthWriteStyle());
    }

    layer.setPainter(painter);
    layer.setSelectable( false );
    layer.setIcon( new TLcdGUIIcon( TLcdGUIIcon.MAPCANVAS16) );
    return layer;
  }

  public static ILcdGLLayer createHighPrecisionGridLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    TLcdGLLayer layer = new TLcdGLLayer( aModel );
    layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
    boolean useHighPrecisionRendering = true;
    TLcdGLLonLatGridPainter painter = new TLcdGLLonLatGridPainter( useHighPrecisionRendering );
    painter.setPaintFill( true );
    painter.setPaintOutline( true );
    painter.setDrawZeroSeparately( true );

    // Add a texture style to the grid
    TLcdGL2DTextureStyle textureStyle = new TLcdGL2DTextureStyle();
    TLcdGLSingleTextureFactory textureFactory = new TLcdGLSingleTextureFactory();
    textureFactory.setImage(TLcdImageIcon.getImage("background/geocentric/bluemarble2048.png"));
    textureStyle.setTextureFactory(textureFactory);

    if (aTargetView.getXYZWorldReference() instanceof ILcdGeocentricReference) {
      /* Use a polygon offset style to avoid visual interference ("z fighting")
         between the grid and other layers. */
      TLcdGLPolygonOffsetStyle poStyle = new TLcdGLPolygonOffsetStyle(10, 10);

      // Combine the above styles into a composite
      TLcdGLCompositeStyle style = new TLcdGLCompositeStyle();
      style.addStyle(textureStyle);
      style.addStyle(poStyle);

      painter.setFillStyle(style);
    }
    else {
      painter.setFillStyle(textureStyle);
    }
    painter.setOutlineStyle(new TLcdGLColorStyle(Color.blue));
    painter.setZeroStyle(new TLcdGLColorStyle(Color.red));

    if (!(aTargetView.getXYZWorldReference() instanceof ILcdGeocentricReference)) {
      layer.setLayerStyle(new TLcdGLNoDepthWriteStyle());
    }

    layer.setPainter(painter);
    layer.setSelectable( false );
    layer.setIcon( new TLcdGUIIcon( TLcdGUIIcon.MAPCANVAS16) );
    return layer;
  }
}
