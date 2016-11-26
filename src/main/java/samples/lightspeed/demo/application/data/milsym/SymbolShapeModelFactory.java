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
package samples.lightspeed.demo.application.data.milsym;

import java.util.Enumeration;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeight3DArcBand;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.ILcdAPP6AShape;

import samples.lightspeed.demo.framework.application.Framework;

/**
 * Creates a model that contains shapes that are associated with a symbol.
 * This allows the shape to be toggled on/off together with its associated symbol.
 * Association is determined by code and location.
 *
 * The current implementation only takes into account the "Free Republic of San Francisco"-model.
 */
class SymbolShapeModelFactory {

  private static final String ASSOCIATED_APP6A_CODE = "SHGPESR-----CA-";

  public ILcdModel createModel() {
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference();
    TLcdVectorModel model = new TLcdVectorModel(modelReference, new TLcdModelDescriptor("Military symbol shapes", "SymbolShape", "Military symbol shapes"));

    TLcdLonLatHeight3DArcBand shape1 = new TLcdLonLatHeight3DArcBand(-122.4724252, 37.8085170, 21, 3, 2400, 80, 25, -30, 40, 0, 0, modelReference.getGeodeticDatum().getEllipsoid());
    SymbolShape symbolShape1 = createSymbolShape(shape1, ASSOCIATED_APP6A_CODE);
    addSymbolShape(model, symbolShape1);

    TLcdLonLatHeight3DArcBand shape2 = new TLcdLonLatHeight3DArcBand(-122.4858655, 37.8280267, 106, 3, 2400, -70, -25, -30, 40, 0, 0, modelReference.getGeodeticDatum().getEllipsoid());
    SymbolShape symbolShape2 = createSymbolShape(shape2, ASSOCIATED_APP6A_CODE);
    addSymbolShape(model, symbolShape2);

    TLcdLonLatHeight3DArcBand shape3 = new TLcdLonLatHeight3DArcBand(-122.4241323, 37.8582771, 111, 3, 6000, -70, -25, -20, 30, 0, 0, modelReference.getGeodeticDatum().getEllipsoid());
    SymbolShape symbolShape3 = createSymbolShape(shape3, ASSOCIATED_APP6A_CODE);
    addSymbolShape(model, symbolShape3);

    return model;
  }

  private SymbolShape createSymbolShape(ILcdShape aShape, String aAssociatedAPP6ACode) {
    ILcdModel model = Framework.getInstance().getModelWithID("model.id.milsym.sf");
    if (model == null) {
      return null;
    }

    Enumeration elements = model.elements();
    while (elements.hasMoreElements()) {
      Object element = elements.nextElement();
      if (element instanceof ILcdAPP6AShape) {
        ILcdAPP6AShape symbol = (ILcdAPP6AShape) element;
        if (isAssociatedSymbol(aShape, symbol, aAssociatedAPP6ACode)) {
          return new SymbolShape(aShape, symbol);
        }

      }
    }
    return null;
  }

  private boolean isAssociatedSymbol(ILcdShape aShape, ILcdAPP6AShape aSymbol, String aAssociatedAPP6ACode) {
    if (aAssociatedAPP6ACode.equals(aSymbol.getAPP6ACode()) &&
        aSymbol.getPointList() != null &&
        aSymbol.getPointList().getPointCount() > 0) {
      ILcdPoint symbolPoint = aSymbol.getPointList().getPoint(0);
      ILcd2DEditablePoint shapePoint = symbolPoint.cloneAs2DEditablePoint();
      shapePoint.move2D(aShape.getFocusPoint());

      return symbolPoint.equals(shapePoint);
    }
    return false;
  }

  private void addSymbolShape(ILcdModel aModel, SymbolShape aSymbolShape) {
    if (aSymbolShape != null) {
      aModel.addElement(aSymbolShape, ILcdModel.NO_EVENT);
    }
  }

  /**
   * Domain object representing a shape with an associated symbol.
   */
  static class SymbolShape {

    private final ILcdShape fShape;
    private final ILcdAPP6ACoded associatedSymbol;

    public SymbolShape(ILcdShape aShape, ILcdAPP6ACoded aAssociatedSymbol) {
      fShape = aShape;
      associatedSymbol = aAssociatedSymbol;
    }

    public ILcdShape getShape() {
      return fShape;
    }

    public ILcdAPP6ACoded getAssociatedSymbol() {
      return associatedSymbol;
    }

  }

}
