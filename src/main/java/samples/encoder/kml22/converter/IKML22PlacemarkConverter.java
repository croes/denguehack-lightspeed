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
package samples.encoder.kml22.converter;

import com.luciad.format.kml22.model.feature.TLcdKML22Placemark;
import com.luciad.model.ILcdModel;

/**
 * Instance of this interface convert a model element into a KML placemark.
 */
public interface IKML22PlacemarkConverter {

  /**
   * Returns whether this converter can convert the model element into a KML placemark.
   *
   * @param aModel the model containing aObject
   * @param aObject candidate to be converted into a KML placemark
   * @return true if this instance can convert aObject.
   */
  boolean canConvertIntoPlacemark(ILcdModel aModel, Object aObject);

  /**
   * Convert an object into a {@link TLcdKML22Placemark}.
   * This converter should set the name, description, extendedData... of the converted placemark.
   * Note, however, that geometry, time and style are converted by resp. a {@link IKML22ShapeConverter}
   * a {@link IKML22TimeConverter} and a {@link IKML22StyleConverter} and may override any value set on the converted placemark
   * by this converter.
   *
   * @param aModel the model containing aObject
   * @param aObject an object to convert into a KML feature
   * @param aKMLBuilder the current KML builder to access existing schema.
   *
   * @return a {@link TLcdKML22Placemark} instance.
   * @throws IllegalArgumentException if it cannot convert into a placemark.
   */
  TLcdKML22Placemark convertIntoPlacemark(ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder) throws IllegalArgumentException;
}
