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
package samples.lucy.drawing.customdomainobject;

import com.luciad.lucy.model.ALcyDomainObjectConverter;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * {@link ALcyDomainObjectConverter} for link {@link CustomDomainObject}.
 *
 * This is a very simple converter that can only convert {@link CustomDomainObject}
 * instances from one model to another.
 *
 * It can adapt a {@link CustomDomainObject} to any class to which 
 * the {@link CustomDomainObject} class is assignable. In that case,
 * adapting is basically the same as casting.
 */
public class CustomDomainObjectConverter extends ALcyDomainObjectConverter {

  private TLcdGeoReference2GeoReference fTransformer = new TLcdGeoReference2GeoReference();
  private CustomDomainObjectSupplier fSupplier;

  public CustomDomainObjectConverter(CustomDomainObjectSupplier aSupplier) {
    fSupplier = aSupplier;
  }

  @Override
  public Object adapt(Object aDomainObject, ILcdModel aSourceModel,
                      Class aDesiredClass) throws IllegalArgumentException {
    return aDomainObject;
  }

  @Override
  public boolean canAdapt(Object aDomainObject, ILcdModel aSourceModel,
                          Class aDesiredClass) {
    return aDesiredClass.isAssignableFrom(CustomDomainObject.class);
  }

  @Override
  public boolean canConvert(Object aDomainObject, ILcdModel aSourceModel,
                            ILcdModel aTargetModel,
                            Class aDesiredClass) {
    if (!aDesiredClass.isAssignableFrom(CustomDomainObject.class)) {
      return false;
    }
    return fSupplier.canHandle(aDomainObject);
  }

  @Override
  public Object convert(Object aDomainObject, ILcdModel aSourceModel,
                        ILcdModel aTargetModel, Class aDesiredClass) throws IllegalArgumentException,
                                                                            TLcdOutOfBoundsException {
    ILcd2DEditablePoint result = (ILcd2DEditablePoint) fSupplier.createDomainObject(aTargetModel);
    ILcdPoint target = transform((ILcdPoint) aDomainObject, aSourceModel.getModelReference(), aTargetModel.getModelReference());
    result.move2D(target);
    return result;
  }

  private synchronized ILcdPoint transform(ILcdPoint aPoint, ILcdModelReference aSourceRef, ILcdModelReference aTargetRef) throws TLcdOutOfBoundsException {
    ILcd3DEditablePoint result = aTargetRef.makeModelPoint().cloneAs3DEditablePoint();
    if (aSourceRef.equals(aTargetRef)) {
      //no fancy calculations required
      result.move3D(aPoint);
    } else {
      fTransformer.setSourceReference(aSourceRef);
      fTransformer.setDestinationReference(aTargetRef);
      fTransformer.sourcePoint2destinationSFCT(aPoint, result);
    }
    return result;
  }

}
