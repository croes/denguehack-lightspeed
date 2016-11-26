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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.TableModel;

import com.luciad.util.ILcdDisposable;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.TLcdStatusEventSupport;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Search manager responsible for linking the search user interface with the search logic (i.e. {@code ISearchTask} and {@code ISearchService}).
 * This manager makes sure that:
 * <ul>
 *   <li>all found results are reported to the UI;</li>
 *   <li>a search task is created and executed whenever new tasks are defined;</li>
 *   <li>old search tasks are cleaned up when new ones are registered</li>
 * </ul>
 *
 * <p>
 *   All public methods of this class should be called on the Event Dispatch Thread.
 * </p>
 *
 * <p>
 *   Consult the {@link samples.lucy.search package javadoc} for a high-level overview
 *   of how the search process uses the different interfaces in combination with this {@code SearchManager}.
 * </p>
 */
public final class SearchManager implements ILcdStatusSource, ILcdDisposable {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(SearchManager.class);
  static final int GLOBAL_SEARCH_RESULT_LIMIT = 500;

  private final List<ISearchService> fSearchServices = new ArrayList<>();
  private final List<SearchRunnable> fRunningTasks = new ArrayList<>();

  private final SearchResultContainer fResultContainer = new SearchResultContainer();

  private final TLcdStatusEventSupport fStatusEventSupport;
  private Pattern fCurrentPattern = null;

  /*
   * Changes to the contents of the search box pop-up, called SearchResultContainer, are always batched together and
   * performed by a (Swing) timer. This significantly reduces the 'flickering' in the search box pop-up. The flickering
   * is caused during typing by clearing the search box, and afterwards adding results.
   * We therefore keep a list of search results that are pending to be added, and whether a clear is pending. When the
   * timer fires, all pending operations are performed at once.
   *
   * Lock on fPendingSearchResults when manipulating it, accessed from both EDT and search worker threads.
   */
  private final List<ISearchResult> fPendingSearchResults = new ArrayList<>();
  private boolean fClearResultContainerPending = false; // accessed on EDT only
  private final Timer fUpdateResultContainerTimer = new Timer(200, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      updateResultContainer();
    }
  });

  private final ExecutorService fExecutorService;
  private TLcdStatusEvent.Progress fProgress;

  /**
   * Default constructor.
   *
   * @param aService the executor service needed for this search manager.
   * @param aSearchServices The search services which will be used
   */
  SearchManager(ExecutorService aService, Collection<ISearchService> aSearchServices) {
    assertOnEDT();
    fExecutorService = aService;
    fSearchServices.addAll(aSearchServices);
    fStatusEventSupport = new TLcdStatusEventSupport();

    // Coalesce events so that the UI updates are deferred until we've had a 'quite' interval.
    fUpdateResultContainerTimer.setCoalesce(true);
    fUpdateResultContainerTimer.setRepeats(false);
  }

  /**
   * <p>
   *   Returns a {@code TableModel} instance which contains all the published {@link ISearchResult} instances for the
   *   current search.
   *   This model is updated when new search results become available, and cleared when a search is cancelled.
   *   Updates on this model will always happen on the Event Dispatch Thread.
   * </p>
   *
   * @return a {@code TableModel} containing all the published {@link ISearchResult} instances.
   */
  public TableModel getSearchResultsUIModel() {
    assertOnEDT();
    return fResultContainer.getTableModel();
  }

  /**
   * <p>
   *   This method starts a new search operation for the specified keyword.
   * </p>
   * <p>
   *   This will perform the following operations:
   * </p>
   * <ol>
   *   <li>
   *     If a previous search is still ongoing, it will be cancelled.
   *   </li>
   *   <li>
   *     A status event will be fired ({@link TLcdStatusEvent#START_BUSY}) to
   *     indicate that a new search has started.
   *   </li>
   *   <li>
   *     Each of the {@link ISearchService} instances will be asked to {@link ISearchService#createSearchTasks() create search tasks}.
   *   </li>
   *   <li>
   *     The {@link ISearchTask#search(Pattern, ISearchTask.ResultCollector) search} method of each of those {@link ISearchTask} instances is
   *     executed on a background thread.
   *   </li>
   *   <li>
   *     When a task publishes results on the {@link ISearchTask.ResultCollector}, the result will
   *     be exposed in the {@link #getSearchResultsUIModel()}.
   *     This model can be used to present the results in a UI to the user.
   *   </li>
   *   <li>
   *     When all tasks are finished, a status event will be fired ({@link TLcdStatusEvent#END_BUSY}).
   *   </li>
   * </ol>
   *
   * @param aSearchKeyword the new search keyword.
   *                       No search will be started when empty or {@code null}.
   *
   * @see #cancelAndClearCurrentSearchResults()
   * @see #handleResult(ISearchResult)
   */
  public void search(String aSearchKeyword) {
    assertOnEDT();

    cancelRunningTasks();
    clearResultContainerLater();

    if (aSearchKeyword != null && !aSearchKeyword.isEmpty()) {
      Pattern pattern = Pattern.compile(aSearchKeyword, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
      startNewSearch(pattern);
    }
  }


  private void onResultsReceived(List<ISearchResult> aSearchResults) {
    assertOnEDT();
    if (LOGGER.isDebugEnabled()) {
      for (ISearchResult searchResult : aSearchResults) {
        LOGGER.debug("Result received: [" + searchResult.getResult() + "]");
      }
    }

    int total = fResultContainer.addSearchResults(aSearchResults);
    if (total >= GLOBAL_SEARCH_RESULT_LIMIT) {
      cancelRunningTasks();
    }
  }

  /**
   * <p>
   *   This method should be invoked when the result is chosen and must be handled.
   *   The search manager will pass the result back to the search service who provided the result (see {@link ISearchService#onResultChosen(ISearchResult)}).
   *   The service decides how to deal with the result.
   * </p>
   *
   * <p>
   *   If this method is called while a search is ongoing, the search will be cancelled.
   *   All results from the current search will also be removed from the UI model because a result has been selected.
   * </p>
   *
   * <p>
   *   Typically, this method is called from the UI when the user selected a search result.
   *   The UI can then pass the selected result back to this method, which will ensure that the result
   *   is passed along to the correct {@code ISearchService} which knows what actions to perform with the result.
   * </p>
   *
   * @param aSearchResult the search result
   */
  public void handleResult(ISearchResult aSearchResult) {
    assertOnEDT();
    aSearchResult.getSearchService().onResultChosen(aSearchResult);

    cancelRunningTasks();
    clearResultContainerLater();
  }

  /**
   * <p>
   *   Cancel the currently ongoing search (if any), and clear all the results.
   *   A typical use-case for this method is when the user presses the cancel button in the UI.
   * </p>
   */
  public void cancelAndClearCurrentSearchResults() {
    assertOnEDT();
    cancelRunningTasks();
    clearResultContainerLater();

    for (ISearchService service : fSearchServices) {
      service.onClearSearchBox();
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   *   All status events will be fired on the Event Dispatch Thread.
   * </p>
   */
  @Override
  public void addStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.addStatusListener(aListener);
  }

  @Override
  public void removeStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.removeStatusListener(aListener);
  }

  private void fireStatusEvent(TLcdStatusEvent<SearchManager> aStatusEvent) {
    assertOnEDT();
    fStatusEventSupport.fireStatusEvent(aStatusEvent);
  }

  /**
   * Disposes the search manager and releases all its resources.
   */
  @Override
  public void dispose() {
    assertOnEDT();
    cancelRunningTasks();
    clearResultContainerLater();

    // Avoid that the above call could keep a pending Timer hanging around, so do what the timer would do, but do it
    // immediately, and then stop the timer.
    updateResultContainer();
    fUpdateResultContainerTimer.stop();
  }

  private void cancelRunningTasks() {
    assertOnEDT();

    //we cannot loop directly over the fRunningTasks list. Cancelling a task will remove it from fRunningTasks, leading to
    //ConcurrentModificationExceptions
    List<SearchRunnable> tasksToCancel = new ArrayList<>(fRunningTasks);
    for (SearchRunnable runningTask : tasksToCancel) {
      runningTask.cancel();
    }
    fRunningTasks.clear();

    // Clear any pending results, important to do this after the running tasks were canceled
    synchronized (fPendingSearchResults) {
      fPendingSearchResults.clear();
    }

    fireEndBusyEvent();
    fCurrentPattern = null;
  }

  private void fireEndBusyEvent() {
    assertOnEDT();
    if (fProgress != null) {
      fProgress.end("Search finished");
      fProgress = null;
    }
  }

  private void updateResultContainer() {
    assertOnEDT();

    if (fClearResultContainerPending) {
      fClearResultContainerPending = false;
      fResultContainer.clearResults();
    }

    ArrayList<ISearchResult> toHandle;
    synchronized (fPendingSearchResults) {
      toHandle = new ArrayList<>(fPendingSearchResults);
      fPendingSearchResults.clear();
    }
    if (!toHandle.isEmpty()) {
      onResultsReceived(toHandle);
    }
  }

  private void clearResultContainerLater() {
    fClearResultContainerPending = true;
    fUpdateResultContainerTimer.restart();
  }

  private void startNewSearch(Pattern aSearchPattern) {
    fCurrentPattern = aSearchPattern;
    fProgress = TLcdStatusEvent.startIndeterminateProgress(fStatusEventSupport.asListener(), this, "Searching " + fCurrentPattern);

    // First determine the search tasks
    Map<ISearchTask, ISearchService> task2ServiceMapping = new HashMap<>();
    List<ISearchTask> searchTasks = new ArrayList<>();
    for (ISearchService searchService : fSearchServices) {
      List<? extends ISearchTask> tasks = searchService.createSearchTasks();
      searchTasks.addAll(tasks);

      for (ISearchTask task : tasks) {
        task2ServiceMapping.put(task, searchService);
      }
    }

    Collections.sort(searchTasks, new SearchTaskComparator());

    for (ISearchTask task : searchTasks) {
      SearchResultCollector collector = new SearchResultCollector();

      SearchRunnable runnable = new SearchRunnable(task, aSearchPattern, collector);
      fRunningTasks.add(runnable);
      fExecutorService.submit(runnable);
    }
  }

  private static void assertOnEDT() {
    if (!EventQueue.isDispatchThread()) {
      throw new UnsupportedOperationException("This method should be called on the Event Dispatch Thread. "
                                              + "It was called on [" + Thread.currentThread().getName() + "]");
    }
  }

  private class SearchRunnable extends SwingWorker<Void, Void> {
    private final ISearchTask fTask;
    private final Pattern fSearchPattern;
    private final SearchResultCollector fCollector;

    public SearchRunnable(ISearchTask aTask, Pattern aSearchPattern, SearchResultCollector aCollector) {
      fTask = aTask;
      fSearchPattern = aSearchPattern;
      fCollector = aCollector;
    }

    @Override
    protected Void doInBackground() throws Exception {
      fTask.search(fSearchPattern, fCollector);
      return null;
    }

    public void cancel() {
      cancel(true);
      fCollector.setCancelled(true);
    }

    @Override
    protected void done() {
      fRunningTasks.remove(SearchRunnable.this);
      if (fRunningTasks.isEmpty()) {
        fireEndBusyEvent();
      }
    }
  }

  /**
   * Collects all search results in a list with 'pending' results. The pending results are flushed once in a while,
   * using a timer.
   */
  private class SearchResultCollector implements ISearchTask.ResultCollector {
    /**
     * Accessed on the EDT through the {@link #setCancelled(boolean)} method,
     * and on a worker thread through the {@link #isCancelled()} method which is called by the {@link ISearchTask}.
     */
    private boolean fCancelled = false;

    @Override
    public void addResult(final ISearchResult aResult) {
      synchronized (fPendingSearchResults) {
        if (fCancelled) {
          return;
        }
        fPendingSearchResults.add(aResult);
      }

      // Not sure if restart can be called from a worker thread, use an invokeLater to prevent possible issues.
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          fUpdateResultContainerTimer.restart();
        }
      });
    }

    @Override
    public boolean isCancelled() {
      synchronized (fPendingSearchResults) {
        return fCancelled;
      }
    }

    public void setCancelled(boolean aCancelled) {
      // Sync on fPendingSearchResults so that addResult is either not yet started, or already done.
      synchronized (fPendingSearchResults) {
        fCancelled = aCancelled;
      }
    }
  }

  private static class SearchTaskComparator implements Comparator<ISearchTask> {
    private final Comparator<ISearchTask.Speed> fSpeedComparator = ISearchTask.Speed.FROM_FAST_TO_SLOW;
    @Override
    public int compare(ISearchTask aSearchTask1, ISearchTask aSearchTask2) {
      ISearchTask.Speed speed1 = aSearchTask1.getSpeed();
      ISearchTask.Speed speed2 = aSearchTask2.getSpeed();

      return fSpeedComparator.compare(speed1, speed2);
    }
  }
}
