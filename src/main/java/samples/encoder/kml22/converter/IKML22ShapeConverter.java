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

import com.luciad.format.kml22.model.geometry.TLcdKML22AbstractGeometry;
import com.luciad.model.ILcdModel;

/**
 * Instance of this interface convert a model element into a KML abstract geometry.
 */
public interface IKML22ShapeConverter {

  /**
   * Returns whether this converter can convert the model element into a KML abstract geometry.
   *
   * @param aModel the model containing aObject
   * @param aObject an object that could be converted into a KML feature
   * @return true if this instance can convert aObject.
   */
  boolean canConvertIntoShape(ILcdModel aModel, Object aObject);

  /**
   * Convert an object into a {@link TLcdKML22AbstractGeometry}.
   *
   * @param aModel the model containing aObject
   * @param aObject an object to convert into a KML feature
   *
   * @return a {@link TLcdKML22AbstractGeometry} instance or null if it fails to convert into a shape
   * @throws IllegalArgumentException if canConvertIntoShape return false for the argument.
   */
  TLcdKML22AbstractGeometry convertIntoShape(ILcdModel aModel, Object aObject);
}
