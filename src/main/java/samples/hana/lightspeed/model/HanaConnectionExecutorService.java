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
package samples.hana.lightspeed.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.TLcdStatusEventSupport;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.hana.lightspeed.common.HanaConnectionParameters;

/**
 * Wrapper for an {@link ExecutorService} and connection pool.
 *
 * It has the following properties:
 * <ul>
 *   <li>Immediately opens max connections when created.</li>
 *   <li>Has a thread pool executors: one thread for each connection.</li>
 *   <li>Uses a LIFO queue.</li>
 * </ul>
 */
public class HanaConnectionExecutorService {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(HanaConnectionExecutorService.class);

  private final AtomicInteger fQueryCount = new AtomicInteger(0);
  private final BlockingDeque<Connection> fConnections;
  private final LifoBlockingDeque<Runnable> fQueue = new LifoBlockingDeque<Runnable>();
  private final AtomicInteger fRunning = new AtomicInteger(0);
  private final ExecutorService fExecutorService;
  private final String fUrl;
  private final String fType;
  private final TLcdStatusEventSupport fBusyListeners = new TLcdStatusEventSupport();

  private TLcdStatusEvent.Progress fProgress;

  public static abstract class ResultHandler {

    public abstract void handleRow(ResultSet aResultSet) throws IOException, SQLException;

    public void handleEnd() throws IOException, SQLException {
    }
  }

  public HanaConnectionExecutorService(String aType, HanaConnectionParameters aParameters, int aNumConnections) {
    fType = aType;
    try {
      DriverManager.registerDriver(new com.sap.db.jdbc.Driver());
      BlockingDeque<Connection> connections = new LinkedBlockingDeque<Connection>();
      for (int i = 0; i < aNumConnections; i++) {
        connections.add(DriverManager.getConnection(aParameters.getUrl(), aParameters.getUser(), aParameters.getPassword()));
      }
      fConnections = connections;
      fExecutorService = new ThreadPoolExecutor(aNumConnections, aNumConnections, 60, TimeUnit.SECONDS, fQueue);
      fUrl = aParameters.getUrl();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }

  public String getURL() {
    return fUrl;
  }

  public void submitAndWait(String query, ResultHandler handler, String... aQueryArguments) {
    try {
      Future f = submit(query, handler, aQueryArguments);
      f.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    } catch (ExecutionException e) {
      throw new IllegalStateException(e);
    }
  }

  public void submitQueryAndWait(String query, ResultHandler handler, String... aQueryArguments) {
    try {
      Future f = submitQuery(query, handler, aQueryArguments);
      f.get();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    } catch (ExecutionException e) {
      throw new IllegalStateException(e);
    }
  }

  public Future submit(String query, ResultHandler handler, String... aQueryArguments) {
    return fExecutorService.submit(new MyRunnable(query, aQueryArguments, true, handler));
  }

  public Future submitQuery(String query, ResultHandler handler, String... aQueryArguments) {
    return fExecutorService.submit(new MyRunnable(query, aQueryArguments, false, handler));
  }

  public Connection getConnection() {
    Connection connection;
    try {
      connection = fConnections.pollFirst(1, TimeUnit.HOURS);
    } catch (InterruptedException e) {
      sLogger.error("No free connection after 1 hour.", e);
      throw new IllegalStateException(e);
    }
    int running = fRunning.incrementAndGet();
    if (running == 1) {
      fProgress = TLcdStatusEvent.startProgress(fBusyListeners.asListener(), this, "Connected");
    }
    return connection;
  }

  public void releaseConnection(Connection aConnection) {
    int running = fRunning.decrementAndGet();
    if (running == 0 && fProgress != null) {
      fProgress.end();
      fProgress = null;
    }
    fConnections.add(aConnection);
  }

  @Override
  public String toString() {
    return "HanaConnectionExecutorService[ " + fType + ": " + fRunning.get() + " running, " + fQueue.size() + " queued]";
  }

  public void addBusyListener(ILcdStatusListener aBusyListener) {
    fBusyListeners.addStatusListener(aBusyListener);
  }

  private class MyRunnable implements Runnable {
    private final String fQuery;
    private final String[] fQueryArguments;
    private final ResultHandler fHandler;
    private final boolean fOnlyExecute;

    public MyRunnable(String aQuery, String[] aQueryArguments, boolean aOnlyExecute, ResultHandler aHandler) {
      fQuery = aQuery;
      fHandler = aHandler;
      fQueryArguments = aQueryArguments;
      fOnlyExecute = aOnlyExecute;
    }

    @Override
    public void run() {
      Connection connection = getConnection();
      try {
        long startTime = System.currentTimeMillis();
        long elapsedTime = startTime;
        int queryId = fQueryCount.incrementAndGet();
        sLogger.debug("Query " + fType + "-" + queryId + ": " + fQuery);

        PreparedStatement statement = connection.prepareStatement(fQuery);
        for (int i = 0; i < fQueryArguments.length; i++) {
          statement.setString(i + 1, fQueryArguments[i]);
        }
        if (fOnlyExecute) {
          statement.execute();
          fHandler.handleEnd();
        } else {
          ResultSet resultSet = statement.executeQuery();
          int count = 0;
          while (resultSet.next()) {
            fHandler.handleRow(resultSet);
            count++;
            if (System.currentTimeMillis() - elapsedTime > 10000) {
              elapsedTime = System.currentTimeMillis();
              long duration = Math.max(1, System.currentTimeMillis() - startTime);
              sLogger.debug("Query " + fType + "-" + queryId + ": loading, " + count + " records in " + duration + " ms (" + ((count * 1000) / duration) + "/s)");
            }
          }
          resultSet.close();

          fHandler.handleEnd();

          long duration = Math.max(1, System.currentTimeMillis() - startTime);
          sLogger.debug("Query " + fType + "-" + queryId + ": loaded " + count + " records in " + duration + " ms (" + ((count * 1000) / duration) + "/s)");
        }

      } catch (Exception e) {
        sLogger.error("Error executing query " + fQuery, e);
        throw new IllegalStateException(e);
      } finally {
        releaseConnection(connection);
      }
    }
  }

  public static class LifoBlockingDeque<E> extends LinkedBlockingDeque<E> {
    @Override
    public boolean offer(E e) {
      return super.offerFirst(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return super.offerFirst(e, timeout, unit);
    }

    @Override
    public boolean add(E e) {
      return super.offerFirst(e);
    }

    @Override
    public void put(E e) throws InterruptedException {
      super.putFirst(e);
    }
  }
}
