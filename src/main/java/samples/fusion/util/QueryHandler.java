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
package samples.fusion.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;

import com.luciad.fusion.tilestore.ILfnQueryHandler;
import com.luciad.fusion.tilestore.metadata.ALfnResourceMetadata;

/**
 * A query handler implementation that waits on a latch until all results are available.
 */
public class QueryHandler implements ILfnQueryHandler {

  private final CountDownLatch fDone = new CountDownLatch(1);
  private final List<ALfnResourceMetadata> fResults = new ArrayList<>();
  private Throwable fThrowable;

  /**
   * This method needs to be synchronized because it may be called multiple times from different threads (in theory).
   */
  public synchronized void got(ALfnResourceMetadata aResourceMetadata) {
    fResults.add(aResourceMetadata);
  }

  /**
   * Unblocks the latch when all results are in.
   */
  public void done() {
    fDone.countDown();
  }

  public void cancelled(CancellationException aException) {
    handle(aException);
  }

  public void interrupted(InterruptedException aException) {
    handle(aException);
  }

  public void threw(IOException aException) {
    handle(aException);
  }

  public void threw(RuntimeException aException) {
    handle(aException);
  }

  public void threw(Error aError) {
    handle(aError);
  }

  /**
   * Unblocks the latch upon a throwable.
   */
  private void handle(Throwable aThrowable) {
    synchronized (this) {
      // Keep the first exception.
      if (fThrowable != null) {
        fThrowable = aThrowable;
      }
    }
    fDone.countDown();
  }

  /**
   * Blocks on the latch and then returns the results or throws the first throwable that occurred.
   */
  public List<ALfnResourceMetadata> awaitResults() throws IOException, InterruptedException {
    fDone.await();
    // No synchronization needed because the latch guarantees a happens-before relationship.
    if (fThrowable != null) {
      IOException ioException = new IOException(fThrowable.getMessage());
      ioException.initCause(fThrowable);
      throw ioException;
    }
    return fResults;
  }
}
