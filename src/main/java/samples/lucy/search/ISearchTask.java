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

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * <p>
 *   Interface defining a search task.
 *   Search tasks are created by a {@link ISearchService}, and will be executed by the {@link SearchManager}.
 * </p>
 *
 * <p>
 *   Consult the {@link samples.lucy.search package javadoc} for a high-level overview
 *   of how the search process uses this interface.
 * </p>
 */
public interface ISearchTask {
  /**
   * <p>
   *   Execute the search operation with the given pattern.
   *   All results needs to be published to the {@link ResultCollector}.
   *   Interaction with the {@code ResultCollector} should only happen on the calling thread of this method.
   * </p>
   *
   * <p>
   *   This method is called on a worker thread, so it is safe to perform long-running operations in the implementation
   *   of this method.
   * </p>
   *
   * @param aSearchPattern the search pattern
   * @param aSearchResultCollector the collector receiving all search results
   */
  void search(Pattern aSearchPattern, ResultCollector aSearchResultCollector);

  /**
   * <p>
   *   Returns if this task is a fast or slow running task.
   * </p>
   *
   * <p>
   *   To have immediate feedback to the user, first all {@link Speed#FAST FAST} running tasks are executed.
   * </p>
   *
   * @return the speed of this task.
   */
  Speed getSpeed();

  /**
   * Enum indicating how fast/slow this search task is.
   */
  enum Speed {
    FAST(100),
    SLOW(1000);

    private final int fRelativeDuration;

    Speed(int aSpeed) {
      fRelativeDuration = aSpeed;
    }

    public static Comparator<Speed> FROM_FAST_TO_SLOW = new Comparator<Speed>() {
      @Override
      public int compare(Speed aSpeed, Speed anotherSpeed) {
        return -1 * (aSpeed.fRelativeDuration - anotherSpeed.fRelativeDuration);
      }
    };
  }

  /**
   * <p>
   *   Collector for the search results.
   * </p>
   *
   * <p>
   *   This interface should not be implemented.
   *   The {@link SearchManager} will pass an instance of this interface to the {@link #search(Pattern, ResultCollector)}
   *   method, and that method should use the provided instance to publish search results.
   * </p>
   */
  interface ResultCollector {
    /**
     * Add a new search result to the collector.
     *
     * @param aResult a new search result
     */
    void addResult(ISearchResult aResult);

    /**
     * Returns {@code true} when the search task is cancelled, {@code false} otherwise.
     *
     * @return {@code true} when the search task is cancelled, {@code false} otherwise.
     */
    boolean isCancelled();
  }
}
