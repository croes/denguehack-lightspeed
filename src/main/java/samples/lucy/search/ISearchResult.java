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
package samples.lucy.search;

import java.util.regex.Pattern;

import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounded;

/**
 * <p>
 *   Interface defining a search result found in scope of a {@link ISearchTask}.
 * </p>
 *
 * <p>
 *   Consult the {@link samples.lucy.search package javadoc} for a high-level overview
 *   of how the search process uses this interface.
 * </p>
 */
public interface ISearchResult extends ILcdBounded {

  /**
   * Returns the object that is the actual search result.
   *
   * @return the object that is the actual search result
   */
  Object getResult();

  /**
   * Returns the reference of the bounds.
   *
   * @return the reference of the bounds
   */
  ILcdGeoReference getReference();

  /**
   * Returns the pattern that was used to generate this search result.
   *
   * @return The search pattern
   */
  Pattern getSearchPattern();

  /**
   * Returns the service that created this result
   *
   * @return the service that created this result
   */
  ISearchService getSearchService();

  /**
   * <p>
   *   Returns a string representation of this search result.
   *   This representation should be suitable to be presented to the user in the UI.
   * </p>
   *
   * <p>
   *   This representation can be different from the search pattern.
   *   For example when searching for "New York", the user might only type "New Y", and
   *   then selecting the result for "New York" from the list of search results.
   *   In this case the pattern would only contain "New Y", while this method would return "New York".
   * </p>
   *
   * @return a string representation of this search result.
   */
  String getStringRepresentation();
}
