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
package samples.lucy.drawing;

import java.util.HashMap;

import com.luciad.lucy.addons.drawing.format.ALcyShapeCodec;
import com.luciad.lucy.addons.drawing.format.ALcyShapeSupplier;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * This class inherits the adapt methods of DomainObjectConverter, hence it
 * returns only true for canadapt when the domain object is already an instance
 * of aCanAdaptToClass
 * Conversion takes place between 2 ILcdShapes. So domainobject must be an ILcdShape and not
 * an TLcySLDDomainObject (just the base shape). The source and target model may be different.
 * For the conversion the shapecodecs are used
 */
public class ShapeSupplierDomainObjectConverter extends DomainObjectConverter {
  private final ALcyShapeCodec fShapeCodec;
  private final ALcyShapeSupplier fShapeSupplier;
  private final ILcdFilter fShapeFilter;

  public ShapeSupplierDomainObjectConverter(
      ALcyShapeSupplier aShapeSupplier
                                           ) {
    this(aShapeSupplier, aShapeSupplier.createShapeCodec());
  }

  public ShapeSupplierDomainObjectConverter(
      ALcyShapeSupplier aShapeSupplier, ALcyShapeCodec aShapeCodec
                                           ) {
    this(aShapeSupplier, aShapeCodec, new ShapeFilter(aShapeSupplier));
  }

  public ShapeSupplierDomainObjectConverter(
      ALcyShapeSupplier aShapeSupplier, ALcyShapeCodec aShapeCodec, ILcdFilter aShapeFilter
                                           ) {
    fShapeCodec = aShapeCodec;
    fShapeSupplier = aShapeSupplier;
    fShapeFilter = aShapeFilter;
  }

  @Override
  public boolean canConvert(Object aDomainObject, ILcdModel aSourceModel, ILcdModel aTargetModel, Class aCanAdaptToClass) {
    return aDomainObject instanceof ILcdShape &&
           fShapeFilter.accept(aDomainObject) &&
           canAdapt(aDomainObject, aTargetModel, aCanAdaptToClass);
  }

  @Override
  public Object convert(Object aDomainObject, ILcdModel aSourceModel, ILcdModel aTargetModel, Class aCanAdaptToClass) throws IllegalArgumentException, TLcdOutOfBoundsException {
    if (canConvert(aDomainObject, aSourceModel, aTargetModel, aCanAdaptToClass)) {
      return cloneShape((ILcdShape) aDomainObject, aSourceModel, aTargetModel);
    } else {
      throw new IllegalArgumentException("Cannot convert the given domain object [" + aDomainObject + "]. Call canConvert before calling convert");
    }
  }

  protected Object cloneShape(ILcdShape aDomainObject, ILcdModel aSourceModel,
                              ILcdModel aTargetModel) throws TLcdOutOfBoundsException {
    HashMap state = new HashMap();
    fShapeCodec.storeState(aDomainObject, aSourceModel, state);
    ILcdShape shape = fShapeSupplier.createShape(aTargetModel);
    fShapeCodec.restoreState(shape, aTargetModel, state);
    return shape;
  }

  private static class ShapeFilter implements ILcdFilter {
    private ALcyShapeSupplier fShapeSupplier;

    public ShapeFilter(ALcyShapeSupplier aShapeSupplier) {
      this.fShapeSupplier = aShapeSupplier;
    }

    @Override
    public boolean accept(Object aObject) {
      return aObject instanceof ILcdShape && fShapeSupplier.canHandle((ILcdShape) aObject);
    }
  }
}
