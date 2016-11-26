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
package samples.lucy.frontend.mapcentric.modelcustomizer;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.readLock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdStatusEvent;

/**
 * <p>
 *   Showing a table view might trigger lazy-loading of the model data.
 *   If we would just update the table view on the EDT, the UI will be blocked during the data loading.
 *    This class ensures the data loading happens on a background thread,
 *    and the table view code will only be triggered after the lazy loading has taken place.
 * </p>
 */
public final class LazyLoader {

  public static LazyLoader getInstance(ILcyLucyEnv aLucyEnv) {
    LazyLoader lazyLoader = aLucyEnv.getService(LazyLoader.class);
    if (lazyLoader == null) {
      lazyLoader = new LazyLoader(aLucyEnv);
      aLucyEnv.addService(lazyLoader);
    }
    return lazyLoader;
  }

  private final ILcyLucyEnv fLucyEnv;
  private final Executor fExecutor;

  private LazyLoader(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    fExecutor = createBackgroundExecutor();
  }

  /**
   * <p>
   *   Triggers any possible lazy loading of the model in the given context on a background thread.
   *   Once the lazy loading is done, {@code aUIUpdatingRunnable} will be triggered on the EDT.
   * </p>
   *
   * <p>
   *   The implementation is not using {@code ILcdModel.elements()} as that might trigger too much loading, e.g. a
   *   database model could possibly return the entire database content.
   *   Calling {@code ILcdIntegerIndexedModel.size()} is a nice compromise.
   * </p>
   *
   * @param aModelContext The model context for which lazy loading must be triggered.
   *                      When {@code null}, this method will only run the {@code Runnable} on the EDT.
   * @param aUIUpdatingRunnable a {@code Runnable} which will be triggered on the EDT once the lazy-loading of the data is done.
   *                            This runnable could for example be used to update the UI.
   *                            May be {@code null}.
   */
  public void triggerLazyLoadingOfModel(TLcyModelContext aModelContext, Runnable aUIUpdatingRunnable) {
    triggerLazyLoadingOfModel(aModelContext != null ? new TLcyModelContext[]{aModelContext} : null, aUIUpdatingRunnable);
  }

  /**
   * <p>
   *   Triggers any possible lazy loading of the models in the given contexts on a background thread.
   *   Once the lazy loading is done, {@code aUIUpdatingRunnable} will be triggered on the EDT.
   * </p>
   *
   * <p>
   *   The implementation is not using {@code ILcdModel.elements()} as that might trigger too much loading, e.g. a
   *   database model could possibly return the entire database content.
   *   Calling {@code ILcdIntegerIndexedModel.size()} is a nice compromise.
   * </p>
   *
   * @param aModelContexts The model contexts for which lazy loading must be triggered.
   *                       When {@code null} or empty, this method will only run the {@code Runnable} on the EDT.
   * @param aUIUpdatingRunnable a {@code Runnable} which will be triggered on the EDT once the lazy-loading of the data is done.
   *                            This runnable could for example be used to update the UI.
   *                            May be {@code null}.
   */
  public void triggerLazyLoadingOfModel(final TLcyModelContext[] aModelContexts, final Runnable aUIUpdatingRunnable) {
    final TLcdStatusEvent.Progress[] progress = new TLcdStatusEvent.Progress[]{null};
    final Timer timer = new Timer(100, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        progress[0] = TLcdStatusEvent.startIndeterminateProgress(fLucyEnv, LazyLoader.this, TLcyLang.getString("Reading table view data"));
      }
    });
    timer.setRepeats(false);

    Runnable command = new Runnable() {
      @Override
      public void run() {
        if (aModelContexts != null && aModelContexts.length > 0) {
          timer.start();
          for (TLcyModelContext modelContext : aModelContexts) {
            ILcdModel model = modelContext.getModel();
            if (model instanceof ILcdIntegerIndexedModel) {
              try (Lock autoUnlock = readLock(model)) {
                ((ILcdIntegerIndexedModel) model).size();
              }
            }
          }
          timer.stop();
        }
        TLcdAWTUtil.invokeNowOrLater(new Runnable() {
          @Override
          public void run() {
            if (progress[0] != null) {
              progress[0].end(TLcyLang.getString("Finished reading table view data"));
            }
            aUIUpdatingRunnable.run();
          }
        });
      }
    };
    if (fLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
      command.run();
    } else {
      fExecutor.execute(command);
    }
  }

  /**
   * Returns an Executor that performs its tasks one after the other, so not concurrent.
   *
   * @return The executor.
   */
  private Executor createBackgroundExecutor() {
    // Let the threads die quickly, so that they don't interfere with application shutdown
    ThreadFactory threadFactory = new BackgroundThreadFactory();
    return new ThreadPoolExecutor(0, 1, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
  }

  private static class BackgroundThreadFactory implements ThreadFactory {
    private int fNextThread = 0;

    @Override
    public Thread newThread(Runnable r) {
      Thread background = new Thread(r, "Model customizer background Executor " + fNextThread++);
      //Use daemon threads, so that they don't interfere with application shutdown
      background.setDaemon(true);
      background.setPriority(Thread.MIN_PRIORITY);
      return background;
    }
  }
}
