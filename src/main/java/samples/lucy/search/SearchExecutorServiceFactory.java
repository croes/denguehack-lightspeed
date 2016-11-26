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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Factory for creating executor services for the search add-on.
 */
final class SearchExecutorServiceFactory {
  private static int sNextThread = 0;

  private SearchExecutorServiceFactory() {
  }

  /**
   * Creates the default executor service for the search add-on.
   * @return the executor service
   */
  public static ExecutorService createSearchExecutorService() {
    ThreadFactory threadFactory = new ThreadFactory() {
      @SuppressWarnings("NullableProblems")
      @Override
      public Thread newThread(Runnable aRunnable) {
        Thread background = new Thread(aRunnable, "Background Search Executor " + sNextThread++);
        //Use daemon threads, so that they don't interfere with application shutdown
        background.setDaemon(true);
        background.setPriority(Thread.MIN_PRIORITY);
        return background;
      }
    };

    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }
}
