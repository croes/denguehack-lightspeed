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

package samples.gxy.decoder.custom1;

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableCircle;
import com.luciad.shape.shape2D.ILcd2DEditablePolygon;
import com.luciad.shape.shape2D.ILcd2DEditablePolyline;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYEditorProvider;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYCirclePainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

/**
 * This implementation of ILcdGXYLayerFactory creates ILcdGXYLayer objects for
 * models decoded with a Custom1ModelDecoder. The MyPainterEditorProvider
 * provides ILcdPainter and ILcdEditor objects for ILcdPoint,
 * ILcd2DEditablePolygon, ILcd2DEditablePolylline and ILcd2DEditableCircle
 * objects. These are the only ILcdShape objects the layer will be showing.
 */
@LcdService
public class Custom1LayerFactory
    implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    ILcdModelDescriptor data_descriptor = aModel.getModelDescriptor();

    if (!Custom1ModelDecoder.TYPE_NAME.equalsIgnoreCase(data_descriptor.getTypeName())) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setLabeled(false);
    layer.setVisible(true);

    // Set the pen on the layer.
    layer.setGXYPen(new TLcdGeodeticPen());

    // Set our own painter/editor provider on the layer.
    MyPainterEditorProvider gxy_painter_editor_provider = new MyPainterEditorProvider();

    layer.setGXYPainterProvider(gxy_painter_editor_provider);
    layer.setGXYEditorProvider(gxy_painter_editor_provider);

    return layer;
  }

  private static class MyPainterEditorProvider implements ILcdGXYPainterProvider, ILcdGXYEditorProvider {

    private TLcdGXYPointListPainter fPolylinePainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
    private TLcdGXYPointListPainter fPolygonPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.FILLED);
    private TLcdGXYIconPainter fPointPainter = new TLcdGXYIconPainter();
    private TLcdGXYCirclePainter fCirclePainter = new TLcdGXYCirclePainter();

    public MyPainterEditorProvider() {
      fPolygonPainter.setFillStyle(new TLcdGXYPainterColorStyle(Color.blue, Color.white));
    }

    @Override
    public ILcdGXYPainter getGXYPainter(Object aObject) {
      ILcdGXYPainter painter = null;
      if (aObject instanceof ILcd2DEditablePolyline) {
        painter = fPolylinePainter;
      } else if (aObject instanceof ILcd2DEditablePolygon) {
        painter = fPolygonPainter;
      } else if (aObject instanceof ILcdPoint) {
        painter = fPointPainter;
      } else if (aObject instanceof ILcd2DEditableCircle) {
        painter = fCirclePainter;
      }
      if (painter != null) {
        painter.setObject(aObject);
      }
      return painter;
    }

  @Override
    public ILcdGXYEditor getGXYEditor(Object aObject) {
      // We can make this cast as all used painters also implement ILcdGXYEditor
      return (ILcdGXYEditor) getGXYPainter(aObject);
    }

  @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        // Class extends Object and implement Cloneable -> exception is never thrown
        throw new RuntimeException(e);
      }
    }
  }
}

