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

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.table.TableCellRenderer;

import com.luciad.lucy.map.ILcyGenericMapComponent;

/**
 * <p>
 *   Instances of this interface provide all the functionality required by the search add-on to perform a search operation.
 *   The interfaces defines:
 * </p>
 *
 * <ul>
 *   <li>what search tasks should be executed</li>
 *   <li>how a search result should be presented to a user by providing a renderer</li>
 *   <li>how a search result should be handled when selected by the user (f.i. executing a fit action on the map)</li>
 * </ul>
 *
 * <p>
 *   If you want to implement your own {@code ISearchService} instance, you might be able to start
 *   from one of the existing abstract base classes of this interface.
 *   If you create your own instance, you have to register it to the {@link SearchManager}.
 *   This can be done by returning your instance from the {@link SearchAddOn#createSearchServices(ILcyGenericMapComponent)}
 *   method.
 * </p>
 *
 * <p>
 *   Consult the {@link samples.lucy.search package javadoc} for a high-level overview
 *   of how the search process uses this interface.
 * </p>
 */
public interface ISearchService {
  int HIGH_SERVICE_PRIORITY = 10;
  int MEDIUM_SERVICE_PRIORITY = 20;
  int LOW_SERVICE_PRIORITY = 30;

  /**
   * <p>
   *   Creates a new set of search tasks that will be executed.
   * </p>
   *
   * <p>
   *   This method is called on the Event Dispatch Thread, so it should finish quickly.
   *   Note that the returned search task instances will be executed on a background thread,
   *   so they can be long running.
   *   See {@link ISearchTask#search(Pattern, ISearchTask.ResultCollector)} for more information.
   * </p>
   *
   * @return the list of created {@link ISearchTask}s.
   *         Never {@code null}, but can be empty.
   */
  List<? extends ISearchTask> createSearchTasks();

  /**
   * <p>
   *   Method invoked by the search add-on when we select a search result in the UI. This method can be used to
   *   execute some action, e.g. fitting on the search result.
   * </p>
   *
   * <p>
   *   This method will be called on the Event Dispatch Thread.
   *   Only {@code ISearchResult} instances which were created by an {@code ISearchTask} created
   *   in the {@link #createSearchTasks()} method of this specific search service will be passed to this method.
   * </p>
   *
   * @param aSearchResult the search result
   */
  void onResultChosen(ISearchResult aSearchResult);

  /**
   * <p>
   *   Retrieves a renderer for the search results.
   *   It is recommended that this method always return the same renderer instance for a specific search service.
   *   The values passed to the renderer will be the {@code ISearchResult} instances which were created by the
   *   {@link #createSearchTasks() search tasks} of this service.
   * </p>
   *
   * <p>
   *   This method will be called on the Event Dispatch Thread.
   * </p>
   *
   * @return A renderer for the search results
   */
  TableCellRenderer getSearchResultRenderer();

  /**
   * <p>
   *   Method invoked by the search add-on when we clear the search box in the the UI.
   *   This method can be used to execute some action, e.g. removing layers that where created/added by the search add-on to visualize search results.
   * </p>
   *
   * <p>
   *   This method will be called on the Event Dispatch Thread.
   * </p>
   */
  void onClearSearchBox();

  /**
   * <p>
   *   Returns the priority of this service, lower means higher priority. Usually one of these constants is used:
   *   {@link #HIGH_SERVICE_PRIORITY}, {@link #MEDIUM_SERVICE_PRIORITY} or {@link #LOW_SERVICE_PRIORITY}.
   * </p>
   *
   * <p>
   *   The priority of the service is one element to define which search results are rated highest, the
   *   {@link #sort(List)} is important as well.
   * </p>
   *
   * @return the priority of this service.
   */
  int getServicePriority();

  /**
   * <p>
   *   Sorts the given list of search results. Most important search results should be at the beginning.
   * </p>
   *
   * <p>
   *   The input order of the list is identical to the order in which these elements were returned by the service. This
   *   means that services that naturally provide ordering, can leave this method body empty.
   * </p>
   *
   * <p>
   *   This method will be called on the Event Dispatch Thread.
   *   Only {@code ISearchResult} instances which were created by an {@code ISearchTask} created
   *   in the {@link #createSearchTasks()} method of this specific search service will be passed to this method.
   * </p>
   *
   * @param aResultsSFCT The list to sort, in place, compatible with the Java Collections sort methods.
   */
  void sort(List<ISearchResult> aResultsSFCT);
}
