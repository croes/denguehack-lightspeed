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
package samples.lucy.drawing.hippodrome;

import com.luciad.format.xml.bind.schema.ILcdXMLSchemaBasedDecoderLibrary;
import com.luciad.format.xml.bind.schema.ILcdXMLSchemaBasedEncoderLibrary;
import samples.lucy.drawing.ShapeCustomizerPanelFactory;
import samples.lucy.drawing.ShapeSupplierDomainObjectConverter;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.drawing.format.ALcyShapeCodec;
import com.luciad.lucy.addons.drawing.format.ALcyShapeSupplier;
import com.luciad.lucy.addons.drawing.format.TLcyDrawingSymbolizerType;
import com.luciad.lucy.addons.drawing.format.TLcyShapePainterProviderContainer;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.model.ALcyDomainObjectConverter;
import com.luciad.lucy.util.TLcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdShape;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;

import samples.gxy.hippodromePainter.GXYHippodromePainter;
import samples.gxy.hippodromePainter.IHippodrome;
import samples.gxy.hippodromePainter.LonLatHippodrome;
import samples.gxy.hippodromePainter.XYHippodrome;

/**
 * {@link ALcyShapeSupplier} for hippodromes.
 */
public class HippodromeShapeSupplier extends ALcyShapeSupplier {

  public static final String ID = "hippodrome";
  static final String STATE_PREFIX = "Samples.";

  private ILcyLucyEnv fLucyEnv;

  public HippodromeShapeSupplier(ILcyLucyEnv aLucyEnv) {
    // the symbolizer type is polygon because we have an outline and a fill
    super(ID, "Hippodrome", TLcyDrawingSymbolizerType.POLYGON, new TLcyProperties());
    fLucyEnv = aLucyEnv;
  }

  public ILcyCustomizerPanelFactory createGXYPainterEditorCustomizerPanelFactory() {
    return null;
  }

  @Override
  public ILcdShape createShape(ILcdModel aForModel) {
    if (isGeodetic(aForModel)) {
      return new LonLatHippodrome(((ILcdGeodeticReference) aForModel.getModelReference()).getGeodeticDatum().getEllipsoid());
    } else {
      return new XYHippodrome();
    }
  }

  private boolean isGeodetic(ILcdModel aForModel) {
    ILcdModelReference modelReference = aForModel.getModelReference();
    if (modelReference instanceof ILcdGeoReference) {
      int mode = ((ILcdGeoReference) modelReference).getCoordinateType();
      if (mode == ILcdGeoReference.GEODETIC) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a single factory that creates {@link HippodromeShapeCustomizer} instances.
   */
  @Override
  public ILcyCustomizerPanelFactory[] createShapeCustomizerPanelFactories() {
    ShapeCustomizerPanelFactory factory = new ShapeCustomizerPanelFactory(this) {
      @Override
      public ILcyCustomizerPanel createCustomizerPanel(Object aObject) {
        return new HippodromeShapeCustomizer(getShapeContextFilter(), fLucyEnv);
      }
    };
    return new ILcyCustomizerPanelFactory[]{factory};
  }

  @Override
  public TLcyShapePainterProviderContainer createShapePainterProviderContainer() {
    ILcdGXYPainterStyle selectionStyle =
        TLcyShapePainterProviderContainer.createSelectionLineStyle(getProperties());

    // we need to use an empty fill and line style because styles are handled by SLD
    GXYHippodromePainter fillPainter = new GXYHippodromePainter();
    fillPainter.setMode(ALcdGXYAreaPainter.FILLED);
    fillPainter.setLineStyle(selectionStyle);
    fillPainter.setFillStyle(TLcyShapePainterProviderContainer.EMPTY_STYLE);
    GXYHippodromePainter strokePainter = new GXYHippodromePainter();
    strokePainter.setMode(ALcdGXYAreaPainter.OUTLINED);
    strokePainter.setLineStyle(selectionStyle);
    strokePainter.setFillStyle(TLcyShapePainterProviderContainer.EMPTY_STYLE);
    // we use the same instance both for painter and editor
    return new TLcyShapePainterProviderContainer(strokePainter, fillPainter, null, null, null,
                                                 strokePainter, fillPainter, null, null, null);
  }

  @Override
  public ALcyShapeCodec createShapeCodec() {
    return new HippodromeCodec(STATE_PREFIX + ID);
  }

  @Override
  public boolean canHandle(ILcdShape aShape) {
    return aShape instanceof IHippodrome;
  }

  @Override
  public ALcyDomainObjectConverter createDomainObjectConverter() {
    return new ShapeSupplierDomainObjectConverter(this);
  }

  @Override
  public ILcdXMLSchemaBasedEncoderLibrary createXMLSchemaBasedEncoderLibrary() {
    return HippodromeXMLSupport.createXMLSchemaBasedEncoderLibrary();
  }

  @Override
  public ILcdXMLSchemaBasedDecoderLibrary createXMLSchemaBasedDecoderLibrary() {
    return HippodromeXMLSupport.createXMLSchemaBasedDecoderLibrary(this);
  }

}
