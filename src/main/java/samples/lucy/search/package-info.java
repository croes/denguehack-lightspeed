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
/**
 * <p>
 *   This package provides the {@link samples.lucy.search.SearchAddOn}:
 *   an add-on which allows the user to search through the available data.
 *   This package also offers the main interfaces to extend this search functionality with your
 *   own search capabilities.
 *   Consult the javadoc of those interfaces for more information.
 * </p>
 *
 * <h4>Performing a search:</h4>
 *
 * <p>
 *   The following sequence diagram shows a simplified version of the interaction between the main interfaces when a search is performed.
 *   Note that for brevity threading related information is skipped.
 *   The threading information can be found in the javadoc of the different interface methods.
 * </p>
 *
 * <p>
 *   <img alt="Sequence diagram illustrating the interaction between the main interfaces when performing a search" src="doc-files/searching.png"/>
 * </p>
 *
 * <p>
 *   This diagram does not show how the results are handled.
 *   This is discussed in the next section.
 * </p>
 *
 * <h4>Handling the search results:</h4>
 *
 * <p>
 *   When a search is performed by calling the {@link samples.lucy.search.SearchManager#search(String)} method
 *   (as illustrated in the section above),
 *   the results will be made available through a {@code TableModel}: see {@link samples.lucy.search.SearchManager#getSearchResultsUIModel()}.
 * </p>
 *
 * <p>
 *   The default UI allows for different operations:
 * </p>
 *
 * <ul>
 *   <li>
 *     <b>Input a search query: </b> when a query is entered in the UI, it will be passed to the {@link samples.lucy.search.SearchManager#search(String)} method.
 *   </li>
 *   <li>
 *     <b>Show the results in a pop-up: </b> when a search is performed, a {@link samples.lucy.search.SearchManager#getSearchResultsUIModel() TableModel} instance
 *     is populated with the results ({@code ISearchResult} instances).
 *     This allows to show the results in the pop-up using a {@code JTable}.
 *   </li>
 *   <li>
 *     <b>Visualization of the results: </b> the results are visualized in two ways: they are rendered in the pop-up menu, and once a result is selected it is shown in the query input field.
 *     The pop-up menu entries are rendered using the renderer provided by the {@link samples.lucy.search.ISearchService#getSearchResultRenderer()} method.
 *     The text shown in the input field once a result is chosen is determined by the {@link samples.lucy.search.ISearchResult#getStringRepresentation()} method.
 *   </li>
 *   <li>
 *     <b>Selecting a search result: </b> once search results are coming in, the UI allows to select a search result.
 *     Depending on the selected search result, an action is performed.
 *     For example when searching on a location, the map will be fitted on that location once the result is selected.
 *     To do this, the UI calls the {@link samples.lucy.search.SearchManager#handleResult(samples.lucy.search.ISearchResult)} method
 *     with the selected search result.
 *     It is up to the service to determine what kind of action must be taken with that result.
 *   </li>
 *   <li>
 *     <b>Cancelling a search: </b> when a search is ongoing or done, the UI allows to clear the search results (from the map) and the UI by clicking the cross icon.
 *     This will call the {@link samples.lucy.search.SearchManager#cancelAndClearCurrentSearchResults()} method.
 *   </li>
 * </ul>
 *
 * <h4>Typical customizations:</h4>
 *
 * <ul>
 *   <li>
 *     <b>Adding an extra search capability: </b> if you want to search through other data (e.g. geocoding an address using the Bing Maps REST API),
 *     you need to create your own {@link samples.lucy.search.ISearchService} and
 *     register it to the {@link samples.lucy.search.SearchManager} by passing it in in the constructor.
 *     The default search services are created in the {@link samples.lucy.search.SearchAddOn#createSearchServices(com.luciad.lucy.map.ILcyGenericMapComponent)} method.
 *   </li>
 *   <li>
 *     <b>Adjusting the rendering of a search result: </b> the search results are displayed in a pop-up menu.
 *     How those results are visualized is determined by the {@link samples.lucy.search.ISearchService#getSearchResultRenderer() TableCellRenderer}
 *     created in the {@code ISearchService}.
 *     Replace the renderer if you want to customize the visualization.
 *   </li>
 *   <li>
 *     <b>Adjusting the action which is performed when a result is chosen: </b> when a search result is selected in the UI,
 *     an action can be performed for that result (for example when searching for a location, the map can fit on that location).
 *     This action is defined in the {@link samples.lucy.search.ISearchService#onResultChosen(samples.lucy.search.ISearchResult)} method,
 *     and can be replaced there.
 *   </li>
 * </ul>
 *
 * @since 2016.0
 */
package samples.lucy.search;
