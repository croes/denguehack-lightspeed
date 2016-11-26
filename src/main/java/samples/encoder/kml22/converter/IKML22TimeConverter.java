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

import com.luciad.format.kml22.model.feature.TLcdKML22AbstractFeature;
import com.luciad.format.kml22.model.time.TLcdKML22AbstractTimePrimitive;
import com.luciad.model.ILcdModel;

/**
 * Interface for classes setting the time primitive of KML feature based on the represented object.
 */
public interface IKML22TimeConverter {

  /**
   *  Returns whether this converter can extract time info from the represented object and set a
   *  {@link TLcdKML22AbstractTimePrimitive time primivite} accordingly to the feature.
   *
   * @param aFeature the KML feature whose time primitive needs to be set
   * @param aModel  the model containing aObject
   * @param aObject aObject the object represented by aFeature.
   *
   * @return true if it can assign a time primitive to the KML feature.
   */
  boolean canConvertTime(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject);

  /**
   * Extract time info from the represented object and set a {@link TLcdKML22AbstractTimePrimitive time primivite}
   * accordingly to the feature.
   *
   * @param aFeature the KML feature whose time primitive needs to be set
   * @param aModel the model containing aObject
   * @param aObject the object represented by aFeature. Used to compute time.
   *
   * @throws IllegalArgumentException if it cannot assign a time primitive to the feature
   */
  void convertTime(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject);
}
