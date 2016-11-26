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
package samples.metadata.util;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import java.util.Collection;

/**
 * Utility class for hierarchical, multivalued data objects.
 */
public class HierarchicalDataObjectUtil {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(HierarchicalDataObjectUtil.class.getName());

  /**
   * Retrieves all objects which are the result of retrieving all properties with the names in
   * <code>aPropertyNameList</code>, starting from the object aStartObject and the String at the index
   * aStartIndex.
   *
   * @param aPropertyNameList the names of the properties to follow.
   * @param aStartIndex      the index of the property to start with.
   * @param aStartObject     the data object to look in
   * @param aObjects         the collection in which the resulting values will be put.
   */
  public void retrieveDataObjectValueSFCT(
          String[] aPropertyNameList,
          int aStartIndex,
          ILcdDataObject aStartObject,
          Collection<Object> aObjects ) {
    String propertyName = aPropertyNameList[ aStartIndex ];
    TLcdDataProperty property = aStartObject.getDataType().getProperty( propertyName );
    int start_index = aStartIndex;
    if ( property != null ) {
      Object value = aStartObject.getValue( property );
      if ( value != null ) {
        start_index++;
        // is this the property we are interested in.
        if ( start_index == aPropertyNameList.length ) {
          if ( property.isCollection() ) {
            aObjects.addAll( ( Collection<?> ) value );
          } else {
            aObjects.add( value );
          }
        } else {
          if ( property.isCollection() ) {
            // we add all objects as labels
            int count = 0;
            for ( Object o : ( Collection<?> ) value ) {
              count++;
              retrieveDataObjectValueSFCT( aPropertyNameList, start_index, (ILcdDataObject) o, aObjects );
            }
            if ( count == 0 ) {
              sLogger.warn("No elements were defined for multivalued property \"" + propertyName + "\" in " + aStartObject.getClass() );
            }
          } else {
            retrieveDataObjectValueSFCT( aPropertyNameList, start_index, (ILcdDataObject) value, aObjects );
          }
        }
      } else {
        sLogger.warn("Property \"" + propertyName + "\" in class " + aStartObject.getClass() + " is null." );
      }
    } else {
      sLogger.warn("No property with name \"" + propertyName + "\" can be found in " + aStartObject.getDataType() );
    }
  }
}
