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
package samples.lucy.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.luciad.util.ALcdWeakStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.view.ALcdPaintExecutorService;

/**
 * <p>
 *   Class which facilitates keeping track of different status events for different tasks.
 *   Each time the {@code ILcdStatusSource} passed in the constructor fires a status event,
 *   a {@link Snapshot} is fired to all listeners attached to this class.
 *   The snapshot contains the info of all ongoing tasks.
 * </p>
 */
public final class StatusEventManager {

  private Snapshot fLatestSnapshot = new Snapshot(null,
                                                  Collections.<OngoingTask>emptyList());

  private final ALcdPaintExecutorService fExecutorService;
  private final CopyOnWriteArrayList<StatusEventManagerListener> fListeners = new CopyOnWriteArrayList<>();

  /**
   * Creates a new manager which will fire the snapshots on the EDT.
   * Note that you need to keep a strong reference to the manager as long as you want to remain informed of events.
   * As soon as the {@code StatusEventManager} gets GC-ed, it will no longer pass any events from {@code aStatusSource} to
   * its {@code StatusEventManagerListener}s.
   *
   * @param aStatusSource The source of the status events
   */
  public StatusEventManager(ILcdStatusSource aStatusSource) {
    this(aStatusSource, ALcdPaintExecutorService.createEDT());
  }

  /**
   * Creates a new manager which will use the {@code aStatusEventExecutorService} to schedule the firing of the snapshots.
   * Note that you need to keep a strong reference to the manager as long as you want to remain informed of events.
   *
   * @param aStatusSource The source of the status events
   * @param aStatusEventExecutorService The executor service which will be used to schedule the firing of the snapshots
   */
  public StatusEventManager(ILcdStatusSource aStatusSource, ALcdPaintExecutorService aStatusEventExecutorService) {
    fExecutorService = aStatusEventExecutorService;
    aStatusSource.addStatusListener(new WeakStatusEventListener(this));
  }

  private void handleStatusEvent(final TLcdStatusEvent aStatusEvent) {
    final Thread thread = Thread.currentThread();
    fExecutorService.invokeLater(new Runnable() {
      @Override
      public void run() {
        List<OngoingTask> ongoingTasks = new ArrayList<>(fLatestSnapshot.getOngoingTasks());

        String task = aStatusEvent.getTaskID();
        OngoingTask currentTask = fLatestSnapshot.findOngoingTask(task);

        if (currentTask == null) {
          //no previous events received for this task
          switch (aStatusEvent.getID()) {
          case TLcdStatusEvent.MESSAGE:
          case TLcdStatusEvent.END_BUSY:
            //a single message, we do not consider the task to be ongoing
            //no need to modify any of the lists in the snapshot, as those lists
            //only contain ongoing tasks
            break;
          case TLcdStatusEvent.START_BUSY:
          case TLcdStatusEvent.PROGRESS:
            ongoingTasks.add(new OngoingTask(task, aStatusEvent.getProgress(), aStatusEvent.getMessage(), aStatusEvent.isBlockingHint(), thread));
            break;
          }
        } else {
          int index = ongoingTasks.indexOf(currentTask);
          ongoingTasks.remove(index);
          switch (aStatusEvent.getID()) {
          case TLcdStatusEvent.MESSAGE:
            ongoingTasks.add(index, new OngoingTask(task, currentTask.getProgress(), aStatusEvent.getMessage(), aStatusEvent.isBlockingHint(), thread));
            break;
          case TLcdStatusEvent.PROGRESS:
          case TLcdStatusEvent.START_BUSY:
            ongoingTasks.add(index, new OngoingTask(task, aStatusEvent.getProgress(), aStatusEvent.getMessage(), aStatusEvent.isBlockingHint(), thread));
            break;
          case TLcdStatusEvent.END_BUSY:
            //task is no longer ongoing
            break;
          }
        }
        fLatestSnapshot = new Snapshot(aStatusEvent,
                                       Collections.unmodifiableList(ongoingTasks));
        fireSnapshot(fLatestSnapshot);
      }
    });
  }

  private void fireSnapshot(Snapshot aSnapshot) {
    for (StatusEventManagerListener listener : fListeners) {
      listener.newSnapShotAvailable(aSnapshot);
    }
  }

  public void addListener(StatusEventManagerListener aListener) {
    fListeners.add(aListener);
  }

  public void removeListener(StatusEventManagerListener aListener) {
    fListeners.remove(aListener);
  }

  /**
   * Listener which will be called each time the {@code StatusEventManager} receives a new event from the status source.
   */
  public interface StatusEventManagerListener {
    /**
     * Callback method each time a new snapshot becomes available
     * @param aSnapshot The new snapshot
     */
    void newSnapShotAvailable(Snapshot aSnapshot);
  }

  /**
   * A snapshot of the current progress of all tasks.
   * A snapshot will be created each time an incoming event is received.
   */
  public static class Snapshot {

    private final List<OngoingTask> fOngoingTasks;
    private final TLcdStatusEvent fCurrentStatusEvent;

    private Snapshot(TLcdStatusEvent aCurrentStatusEvent,
                     List<OngoingTask> aOngoingTasks) {
      fCurrentStatusEvent = aCurrentStatusEvent;
      fOngoingTasks = aOngoingTasks;
    }

    /**
     * <p>
     *   Returns a list with all tasks which are still ongoing.
     * </p>
     * <p>
     *   This list will only include the task of {@link #getCurrentStatusEvent()} if
     *   we expect more incoming events for that task.
     *   For example when {@link #getCurrentStatusEvent()} is a {@link TLcdStatusEvent#END_BUSY} event,
     *   the task will not be included in this list.
     * </p>
     *
     * @return The list of tasks which are still ongoing (=tasks for which we expect more incoming events)
     */
    public List<OngoingTask> getOngoingTasks() {
      return fOngoingTasks;
    }

    /**
     * Returns the {@code OngoingTask} fronm {@link #getOngoingTasks()} for the specified task identifier,
     * or {@code null} when no such task is available.
     *
     * @param aTask The task
     *
     * @return the {@code OngoingTask} fronm {@link #getOngoingTasks()} for the specified task identifier,
     * or {@code null} when no such task is available.
     */
    public OngoingTask findOngoingTask(String aTask) {
      for (OngoingTask ongoingTask : fOngoingTasks) {
        if (ongoingTask.getTask().equals(aTask)) {
          return ongoingTask;
        }
      }
      return null;
    }
    /**
     * Returns the status event for which this snapshot has been generated
     *
     * @return the status event for which this snapshot has been generated
     */
    public TLcdStatusEvent getCurrentStatusEvent() {
      return fCurrentStatusEvent;
    }
  }

  private static class WeakStatusEventListener<T extends ILcdStatusSource> extends
                                                                           ALcdWeakStatusListener<StatusEventManager, T> {

    WeakStatusEventListener(StatusEventManager aObjectToModify) {
      super(aObjectToModify);
    }

    @Override
    protected void statusChangedImpl(StatusEventManager aStatusEventManager, TLcdStatusEvent<T> aStatusEvent) {
      aStatusEventManager.handleStatusEvent(aStatusEvent);
    }
  }

  /**
   * This class represents an ongoing class and allows to query the task for all its information.
   */
  public static class OngoingTask {
    private final String fTask;
    private final double fProgress;
    private final boolean fBlocking;
    private final String fMessage;
    private final Thread fThread;

    private OngoingTask(String aTask, double aProgress, String aMessage, boolean aBlocking, Thread aThread) {
      fTask = aTask;
      fProgress = aProgress;
      fMessage = aMessage;
      fBlocking = aBlocking;
      fThread = aThread;
    }

    /**
     * Returns the task
     * @return the task
     *
     * @see TLcdStatusEvent#getTaskID()
     */
    public String getTask() {
      return fTask;
    }

    /**
     * Returns the last received {@link TLcdStatusEvent#getProgress()} for this task,
     * {@link Double#NaN} when no progress has ever been received for this task.
     *
     * @return the last received {@link TLcdStatusEvent#getProgress()} for this task
     */
    public double getProgress() {
      return fProgress;
    }

    /**
     * Returns the last received {@link TLcdStatusEvent#getMessage()} for this task.
     * @return the last received {@link TLcdStatusEvent#getMessage()} for this task.
     */
    public String getMessage() {
      return fMessage;
    }

    /**
     * Returns the last received {@link TLcdStatusEvent#isBlockingHint()} for this task
     * @return the last received {@link TLcdStatusEvent#isBlockingHint()} for this task
     */
    public boolean isBlocking() {
      return fBlocking;
    }

    /**
     * Returns the thread on which the last received status event of this task was received.
     *
     * @return the thread on which the last received status event of this task was received.
     */
    public Thread getThread() {
      return fThread;
    }
  }
}
