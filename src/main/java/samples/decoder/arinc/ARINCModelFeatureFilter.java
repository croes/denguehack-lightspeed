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

package samples.decoder.arinc;


import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.format.arinc.util.ILcdARINCModelFilter;
import com.luciad.model.ILcdModelDescriptor;

/**
 * This class demonstrates filtering of ARINC data. It defines a filter that
 * uses a specific property and property value to accept objects.
 * <p/>
 * The property to be used to filter on, should be defined by its property and a
 * valid value. Alternatively you could also create a similar feature that uses
 * the name of a property in case the <code>TLcdDataProperty</code> is not
 * available.
 * <p/>
 * Knowing a property and value, this filter will only accept those objects that
 * have the specified value for the specified property.
 * <p/>
 * The filter will let through all objects if one of the parameters ( property
 * or property value) is null.
 * <p/>
 */
class ARINCModelFeatureFilter implements ILcdARINCModelFilter {

  private TLcdDataProperty fProperty;
  private Object fPropertyValue;

  // methods of ILcdARINCModelFilter
  /**
   * This method has no effect. You don't need to call it.
   * 
   * @param aModelDescriptor
   *          not relevant
   */
  public void setModelDescriptor( ILcdModelDescriptor aModelDescriptor ) {

  }

  //method of ILcdFilter

  /**
   * Checks whether an object suffices the filter criterion.
   * <p/>
   * If one of the parameters that determine the filter criterion (property or
   * property value) is not specified, the filter will let all objects through.
   * <p/>
   * If the parameters allow to define a correct criterion, an object will pass
   * through the filter if the object is featured and the value of the feature
   * to be checked equals the given value.
   * <p/>
   * An <code>IllegalArgumentException</code> will be thrown in case the
   * specified <code>TLcdDataProperty</code> is not a property of the object
   * that is passed to the filter.
   * 
   * @param aObject
   *          the object to be checked
   * @return true if the object suffices the filter criterion, false otherwise
   * @throws IllegalArgumentException
   *           if the data type of <code>aObject</code> does not have the
   *           specified <code>TLcdDataProperty</code>
   */
  public boolean accept( Object aObject ) {
    if ( (fProperty == null) || (fPropertyValue == null) ) {
      //one of the parameters has not been set -> all objects will pass through the filter
      return true;
    }

    return (aObject instanceof ILcdDataObject) && (fPropertyValue.equals( ((ILcdDataObject) aObject).getValue( fProperty ) ));
  }

  //public methods

  /**
   * Specifies the property to filter on.
   * 
   * @param aProperty
   *          a property
   */
  public void setProperty( TLcdDataProperty aProperty ) {
    fProperty = aProperty;
  }

  /**
   * Specifies the value of the property to filter on.
   * 
   * @param aPropertyValue
   *          a property value
   */
  public void setPropertyValue( Object aPropertyValue ) {
    fPropertyValue = aPropertyValue;
  }
}
