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

import com.luciad.lucy.model.ALcyDomainObjectConverter;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdShape;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * Basic implementation of {@link ALcyDomainObjectConverter}. For adaptation, it merely performs instanceof tests.
 *
 * This converter can only convert between the same models, and works only with ILcdShapes as domainobjects.
 * For the conversion it simply calls the clone method on the ilcdshape
 * The canAdapt method only returns true when the ilcdshape is an instance of aDesiredClass
 *
 */
public class DomainObjectConverter extends ALcyDomainObjectConverter {

  @Override
  public boolean canAdapt(Object aDomainObject, ILcdModel aSourceModel, Class aDesiredClass) {
    return aDesiredClass.isAssignableFrom(aDomainObject.getClass());
  }

  @Override
  public <T> T adapt(Object aDomainObject, ILcdModel aSourceModel, Class<T> aDesiredClass) throws IllegalArgumentException {
    if (!canAdapt(aDomainObject, aSourceModel, aDesiredClass)) {
      throw new IllegalStateException("This converter cannot adapt [" + aDomainObject + "] to the desired class [" + aDesiredClass.getName() + "]. Call canAdapt before calling adapt");
    }
    return (T) aDomainObject;
  }

  @Override
  public boolean canConvert(Object aDomainObject, ILcdModel aSourceModel, ILcdModel aTargetModel, Class aCanAdaptToClass) {
    return aSourceModel.equals(aTargetModel) &&
           aDomainObject instanceof ILcdShape &&
           canAdapt(aDomainObject, aSourceModel, aCanAdaptToClass);

  }

  @Override
  public Object convert(Object aDomainObject, ILcdModel aSourceModel, ILcdModel aTargetModel, Class aCanAdaptToClass) throws IllegalArgumentException, TLcdOutOfBoundsException {
    if (canConvert(aDomainObject, aSourceModel, aTargetModel, aCanAdaptToClass)) {
      return clone(aDomainObject, aSourceModel, aTargetModel);
    }
    throw new UnsupportedOperationException("This converter cannot convert domain object [" + aDomainObject + "]. Call canConvert before calling convert");
  }

  private Object clone(Object aDomainObject, ILcdModel aSourceModel, ILcdModel aTargetModel) {
    return ((ILcdShape) aDomainObject).clone();
  }
}
