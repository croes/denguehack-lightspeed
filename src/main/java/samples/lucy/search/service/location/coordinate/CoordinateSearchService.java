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
package samples.lucy.search.service.location.coordinate;

import java.text.Format;
import java.util.Collections;
import java.util.List;

import javax.swing.table.TableCellRenderer;

import samples.lucy.search.ISearchService;
import samples.lucy.search.ISearchTask;
import samples.lucy.search.service.location.AbstractLocationSearchService;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * <p>
 *   {@link ISearchService} implementation which can search for a location based on a coordinate string.
 *   The conversion from a coordinate string to an actual location is delegated to a {@code java.text.Format} instance,
 *   which must be provided in the constructor.
 * </p>
 *
 * <p>
 *   If you want to add search support for your own type of coordinate string, it is sufficient to
 *   {@link samples.lucy.search.SearchAddOn#createSearchServices(ILcyGenericMapComponent) register}
 *   an instance of this class instantiated with a {@code java.text.Format} capable of parsing and formatting
 *   your coordinate string instances.
 * </p>
 */
public final class CoordinateSearchService extends AbstractLocationSearchService {

  private final CoordinateSearchResultRenderer fRenderer;
  private final PointFormatProvider fPointFormatProvider;

  public CoordinateSearchService(ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent, PointFormatProvider aPointFormatProvider) {
    super(aLucyEnv, aMapComponent);
    fPointFormatProvider = aPointFormatProvider;
    fRenderer = new CoordinateSearchResultRenderer(aPointFormatProvider);
  }

  public CoordinateSearchService(ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent, final Format aFormat) {
    this(aLucyEnv, aMapComponent, new PointFormatProvider() {
      @Override
      public Format retrieveFormat() {
        return aFormat;
      }
    });
  }

  @Override
  public final List<? extends ISearchTask> createSearchTasks() {
    return Collections.singletonList(new CoordinateSearchTask(fPointFormatProvider, this));
  }

  @Override
  public final TableCellRenderer getSearchResultRenderer() {
    return fRenderer;
  }

  @Override
  public int getServicePriority() {
    return MEDIUM_SERVICE_PRIORITY;
  }

  /**
   * Provider for a {@code java.text.Format} instance which is capable of converting
   * a coordinate string into an {@code ILcdPoint} instance
   */
  public interface PointFormatProvider {
    /**
     * Return a {@code java.text.Format} instance which will be used to convert a coordinate
     * string into an {@code ILcdPoint} instance
     *
     * @return a {@code java.text.Format} instance
     */
    Format retrieveFormat();
  }
}
