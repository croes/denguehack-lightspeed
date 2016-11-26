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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Keeps track of the current search results. It manages a delegate table model that contains the search results that
 * are currently shown in the UI. This is a sorted subset of all results.
 */
final class SearchResultContainer {
  static final int MAX_RESULTS_IN_UI = 50;
  static final int TOP_HIT_COUNT_PER_SERVICE = 2;

  // All search results. The search services (=key) are sorted by their priority
  private final Map<ISearchService, SearchResults> fSearchResults = new TreeMap<>(new Comparator<ISearchService>() {
    @Override
    public int compare(ISearchService o1, ISearchService o2) {
      return Integer.compare(o1.getServicePriority(), o2.getServicePriority());
    }
  });

  private final SearchResultTableModel fTableModel = new SearchResultTableModel();

  /**
   * Adds the given list of search results, sorts them, and updates the UI.
   * @param aResults The results to add.
   * @return The new total number of search results.
   */
  public int addSearchResults(List<ISearchResult> aResults) {
    // Add search results, separated per service
    for (ISearchResult result : aResults) {
      ISearchService key = result.getSearchService();
      SearchResults toAddTo = fSearchResults.get(key);
      if (toAddTo == null) {
        toAddTo = new SearchResults(key);
        fSearchResults.put(key, toAddTo);
      }
      toAddTo.addPreserveOriginalOrder(result);
    }


    Collection<SearchResults> allResults = fSearchResults.values();

    // Sort all results, separate per service
    for (SearchResults s : allResults) {
      s.sort();
    }

    // Divide result count across all services
    divideShowCountPerService(allResults, MAX_RESULTS_IN_UI);

    // Mix the results from all services
    List<ISearchResult> shownResults = federateSearchResults(allResults);

    // Update the UI
    fTableModel.changeSearchResults(shownResults);

    return getTotalCount();
  }

  private int getTotalCount() {
    int total = 0;
    for (SearchResults r : fSearchResults.values()) {
      total += r.getSortedResults().size();
    }
    return total;
  }

  /**
   * Evenly spreads the search result count across all services. Takes care to handle the case where certain services
   * return less results than others. If so, other services use up the available search result count.
   * @param aSearchResultsSFCT The search data.
   * @param aCountToSpread The number of search results to spread across the services.
   */
  private void divideShowCountPerService(Collection<SearchResults> aSearchResultsSFCT, int aCountToSpread) {
    for (SearchResults searchResults : aSearchResultsSFCT) {
      searchResults.resetShowCount();
    }
    divideShowCountPerServiceImpl(aSearchResultsSFCT, aCountToSpread);
  }

  private void divideShowCountPerServiceImpl(Collection<SearchResults> aSearchResultsSFCT, int aCountToSpread) {
    // Pick the service with the least amount of search results
    int minResultCount = Integer.MAX_VALUE;
    SearchResults serviceToDeplete = null;
    for (SearchResults s : aSearchResultsSFCT) {
      List<ISearchResult> results = s.getSortedResults();
      int available = results.size() - s.getShowCount();
      if (available < minResultCount) {
        minResultCount = available;
        serviceToDeplete = s;
      }
    }


    // Deplete all services for an amount equal to what the service with the min amount of results has to offer
    int remaining = aCountToSpread;
    int perService = Math.min(minResultCount, remaining / aSearchResultsSFCT.size()); // simply ignoring the potential rounding error
    for (SearchResults s : aSearchResultsSFCT) {
      s.incrementShowCount(perService);
      remaining -= perService;
    }

    // Spread remaining count over all but the service that is now depleted, using recursion
    ArrayList<SearchResults> otherServices = new ArrayList<>(aSearchResultsSFCT);
    otherServices.remove(serviceToDeplete);
    if ( !otherServices.isEmpty() ) {
      divideShowCountPerServiceImpl(otherServices, remaining);
    }
  }

  /**
   * Mix the results of all services in a single list, suitable for the UI. Puts the most important
   * items from every service in the beginning, and then fills up the remaining places.
   * @param aResults The search data.
   * @return The mixed, flat list.
   */
  private List<ISearchResult> federateSearchResults(Collection<SearchResults> aResults) {
    final ArrayList<ISearchResult> shownResults = new ArrayList<>();

    // Add best few results of each search service
    for (SearchResults s : aResults) {
      List<ISearchResult> results = s.getSortedResults();
      int topHits = Math.min(TOP_HIT_COUNT_PER_SERVICE, results.size());
      shownResults.addAll(results.subList(0, topHits));
    }

    // Add remaining results
    for (SearchResults s : aResults) {
      List<ISearchResult> results = s.getSortedResults();
      int topHits = Math.min(TOP_HIT_COUNT_PER_SERVICE, results.size());
      shownResults.addAll(results.subList(topHits, s.getShowCount()));
    }

    return shownResults;
  }

  public void clearResults() {
    fSearchResults.clear();
    fTableModel.clearResults();
  }

  public SearchResultTableModel getTableModel() {
    return fTableModel;
  }

  /**
   * Search results in both their original and sorted order, per service. Also keeps track of the amount of items
   * to show for this service.
   */
  static class SearchResults {
    private final ISearchService fSearchService;
    private final List<ISearchResult> fOriginalOrderedResults = new ArrayList<>();
    private final List<ISearchResult> fSortedResults = new ArrayList<>();
    private int fShowCount = 0;

    public SearchResults(ISearchService aSearchService) {
      fSearchService = aSearchService;
    }

    public void addPreserveOriginalOrder(ISearchResult aResult) {
      fOriginalOrderedResults.add(aResult);
    }

    public void sort() {
      fSortedResults.clear();
      fSortedResults.addAll(fOriginalOrderedResults);
      fSearchService.sort(fSortedResults);
    }

    public ISearchService getSearchService() {
      return fSearchService;
    }

    public List<ISearchResult> getSortedResults() {
      return fSortedResults;
    }

    public int getShowCount() {
      return fShowCount;
    }

    public void incrementShowCount(int aCount) {
      fShowCount += aCount;
    }

    public void resetShowCount() {
      fShowCount = 0;
    }
  }
}
