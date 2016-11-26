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
import com.luciad.model.ILcdModel;

/**
 * Interface for classes setting the style of KML feature based on the represented object.
 */
public interface IKML22StyleConverter {

  /**
   *  Returns whether this converter can Extract the style info from the represented object and configure to the KML feature.
   *
   * @param aFeature the KML feature whose style needs to be set
   * @param aModel the model containing aObject
   * @param aObject the object represented by aFeature. Used to compute style.
   *
   * @return true if this instance can assign a style to aFeature.
   */
  boolean canConvertStyle(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject);

  /**
   * Extract the style info from the represented object and configure the KML feature style
   * accordingly to the feature.
   *
   * @param aFeature the KML feature whose style needs to be set
   * @param aModel the model containing aObject
   * @param aObject the object represented by aFeature. Used to compute style.
   * @param aKMLBuilder the current KML builder to access existing styles.
   *
   * @throws IllegalArgumentException if it cannot assign a style to the feature
   */
  void convertStyle(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder);
}
