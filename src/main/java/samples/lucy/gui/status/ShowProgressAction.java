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
package samples.lucy.gui.status;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import samples.common.gui.PopupPanelButton;
import samples.lucy.util.StatusEventManager;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.TLcdStatusEvent;

/**
 * <p>
 *   Action which monitors {@code ILcyLucyEnv} for status events representing progress,
 *   and shows a progress bar for that progress.
 *   When multiple {@link TLcdStatusEvent#getTaskID() tasks} are running in parallel, the
 *   action allows to show a pop-up displaying the progress of each of the tasks.
 * </p>
 *
 * <p>
 *   Note that this action has been designed to be inserted into a tool bar.
 *   The action also only supports to be inserted into one action bar at the same time.
 *   Use multiple action instances if you want to insert it into multiple action bars.
 * </p>
 */
public class ShowProgressAction extends ALcdAction implements ILcyCustomizableRepresentationAction {

  final ProgressComponent fProgressComponent = new ProgressComponent();
  /**
   * The status event manager is stored in a field to ensure that the lifetime of the manager is the same as this action.
   * The status event manager only forwards events to the listeners as long as it is hard referenced.
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final StatusEventManager fStatusEventManager;
  private final ILcyLucyEnv fLucyEnv;

  public ShowProgressAction(ILcyLucyEnv aLucyEnv) {
    fStatusEventManager = new StatusEventManager(aLucyEnv);
    fLucyEnv = aLucyEnv;
    fStatusEventManager.addListener(new StatusEventManager.StatusEventManagerListener() {
      @Override
      public void newSnapShotAvailable(StatusEventManager.Snapshot aSnapshot) {
        fProgressComponent.handleSnapshot(aSnapshot);
      }
    });
  }

  @Override
  public final Component customizeRepresentation(Component aDefaultComponent, ILcdAction aWrapperAction, ILcyActionBar aActionBar) {
    return fProgressComponent;
  }

  @Override
  public final void actionPerformed(ActionEvent e) {
    throw new UnsupportedOperationException("This action shows progress, but doesn't perform anything.");
  }

  protected JProgressBar createMainProgressBar() {
    JProgressBar progressBar = new JProgressBar();
    progressBar.setStringPainted(false);
    return progressBar;
  }

  protected JComponent createMainProgressBarComponent(JProgressBar aMainProgressBar, TLcyToolBar aInterruptActionToolBar, PopupPanelButton aManyTasksButton) {
    JPanel result = new JPanel();
    result.setBorder(BorderFactory.createEmptyBorder());
    result.getInsets().set(0, 0, 0, 0);
    result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));
    result.setAlignmentY(Component.CENTER_ALIGNMENT);
    result.setOpaque(false);

    result.add(Box.createHorizontalGlue());
    result.add(aMainProgressBar);
    result.add(aInterruptActionToolBar.getComponent());
    result.add(wrapWithToolBar(aManyTasksButton));
    Font font = aManyTasksButton.getFont();
    aManyTasksButton.setFont(font.deriveFont((float) font.getSize() - 3));

    return result;
  }

  protected void customizeInterruptAction(ILcdAction aAction) {
    aAction.putValue(ILcdAction.SMALL_ICON, new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.CANCEL_CHANGES_ICON), 12, 12));
  }

  protected JProgressBar createPopupProgressBar() {
    //Use fixed size, ensuring that the different progress bars in the ui are correctly outlined
    JProgressBar progressBar = new JProgressBar() {
      @Override
      public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        return new Dimension(150, preferredSize.height);
      }

      @Override
      public Dimension getMaximumSize() {
        return getPreferredSize();
      }

      @Override
      public Dimension getMinimumSize() {
        return getPreferredSize();
      }
    };
    progressBar.setStringPainted(false);
    return progressBar;
  }

  class ProgressComponent extends JPanel {

    final JProgressBar fMainProgressBar;
    final TLcyToolBar fMainInterruptActionToolbar;
    final JComponent fMainProgressBarComponent;

    final PopupPanelButton fManyTasksButton;
    private final JPanel fManyTasksContent = new JPanel();

    final Map<String, OngoingTaskComponent> fOngoingTaskComponents = new HashMap<>();

    ProgressComponent() {
      fMainProgressBar = createMainProgressBar();
      fMainInterruptActionToolbar = createToolBar();
      initProgressBarRange(fMainProgressBar);

      fManyTasksContent.setLayout(new BoxLayout(fManyTasksContent, BoxLayout.PAGE_AXIS));

      fManyTasksButton = new PopupPanelButton(fManyTasksContent);
      fManyTasksButton.setVisible(false);

      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setAlignmentY(CENTER_ALIGNMENT);
      fMainProgressBarComponent = createMainProgressBarComponent(fMainProgressBar, fMainInterruptActionToolbar, fManyTasksButton);
      add(fMainProgressBarComponent);
      setOpaque(false);
      updateVisibility(false);
    }

    /**
     * To avoid flickering we keep the action always {@link ILcdAction#VISIBLE visible}.
     * We also keep the actual component of the action always visible.
     * This ensures, in combination with the overridden {@code getXXXSize} method, that the action will always consume
     * exactly the same amount of space when inserted into a toolbar.
     *
     * However, we do not want the progress bar to be constantly visible. Therefore we update the visibility of the inner components.
     *
     * @param aVisible The new visibility for the inner components
     */
    private void updateVisibility(boolean aVisible) {
      fMainProgressBarComponent.setVisible(aVisible);
    }

    /**
     * To avoid jumps in size when toggling the visibility of the containing components, we base
     * the size on the main component.
     */
    @Override
    public Dimension getPreferredSize() {
      return fMainProgressBarComponent.getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    void handleSnapshot(StatusEventManager.Snapshot aSnapshot) {
      List<StatusEventManager.OngoingTask> ongoingTasks = aSnapshot.getOngoingTasks();

      updateVisibility(!ongoingTasks.isEmpty());

      Set<String> tasksToRemoveFromTheUI = new HashSet<>(fOngoingTaskComponents.keySet());

      for (StatusEventManager.OngoingTask ongoingTask : ongoingTasks) {
        String task = ongoingTask.getTask();
        //task is still ongoing, does not need to be removed
        tasksToRemoveFromTheUI.remove(task);

        OngoingTaskComponent ongoingTaskComponent = fOngoingTaskComponents.get(task);
        if (ongoingTaskComponent != null) {
          ongoingTaskComponent.updateFor(ongoingTask);
        } else {
          InterruptTaskAction interruptAction = new InterruptTaskAction(task, fLucyEnv);
          customizeInterruptAction(interruptAction);
          ongoingTaskComponent = new OngoingTaskComponent(createPopupProgressBar(), interruptAction);
          ongoingTaskComponent.updateFor(ongoingTask);
          fOngoingTaskComponents.put(task, ongoingTaskComponent);
          fManyTasksContent.add(ongoingTaskComponent);
        }
      }

      for (String taskToRemove : tasksToRemoveFromTheUI) {
        OngoingTaskComponent componentToRemove = fOngoingTaskComponents.remove(taskToRemove);
        fManyTasksContent.remove(componentToRemove);
      }

      if (ongoingTasks.size() > 1) {
        fMainProgressBar.setIndeterminate(true);
        fManyTasksButton.setVisible(true);
        fManyTasksButton.setText(Integer.toString(ongoingTasks.size()));
      } else {
        fManyTasksButton.setVisible(false);
      }

      if (ongoingTasks.size() == 1) {
        StatusEventManager.OngoingTask ongoingTask = ongoingTasks.get(0);
        updateProgressBarForProgress(ongoingTask.getProgress(), fMainProgressBar);
        if (fMainInterruptActionToolbar.getActionBarItemCount() == 0) {
          InterruptTaskAction action = fOngoingTaskComponents.get(ongoingTask.getTask()).getInterruptAction();
          fMainInterruptActionToolbar.insertAction(action, null);
        }
      } else {
        while (fMainInterruptActionToolbar.getActionBarItemCount() > 0) {
          fMainInterruptActionToolbar.removeAction((ILcdAction) fMainInterruptActionToolbar.getActionBarItem(0));
        }
      }

      fManyTasksContent.revalidate();
      fManyTasksContent.repaint();

      revalidate();
      repaint();

      Window popup = TLcdAWTUtil.findParentWindow(fManyTasksContent);
      if (popup != null && popup.isShowing()) {
        popup.revalidate();
        popup.repaint();
        popup.pack();
      }
    }
  }

  static class OngoingTaskComponent extends JPanel {

    private final JLabel fMessageLabel = new JLabel();
    private final JProgressBar fProgressBar;
    private final InterruptTaskAction fInterruptAction;

    OngoingTaskComponent(JProgressBar aProgressBar, InterruptTaskAction aInterruptAction) {
      fProgressBar = aProgressBar;
      fInterruptAction = aInterruptAction;

      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

      aProgressBar.setStringPainted(false);
      initProgressBarRange(aProgressBar);

      TLcyToolBar toolBar = createToolBar();
      toolBar.insertAction(aInterruptAction, null);

      add(aProgressBar);
      add(Box.createHorizontalStrut(10));
      add(fMessageLabel);
      add(Box.createHorizontalStrut(10));
      add(Box.createHorizontalGlue());
      add(toolBar.getComponent());
    }

    void updateFor(StatusEventManager.OngoingTask aOngoingTask) {
      updateProgressBarForProgress(aOngoingTask.getProgress(), fProgressBar);
      fMessageLabel.setText(aOngoingTask.getMessage());
      fInterruptAction.update(aOngoingTask.getThread(), aOngoingTask.isBlocking());
    }

    InterruptTaskAction getInterruptAction() {
      return fInterruptAction;
    }

    JLabel getMessageLabel() {
      return fMessageLabel;
    }

    JProgressBar getProgressBar() {
      return fProgressBar;
    }
  }

  private static TLcyToolBar createToolBar() {
    TLcyToolBar toolBar = new TLcyToolBar(createUnobtrusiveToolBar());
    toolBar.setAutoHide(true);
    return toolBar;
  }

  private static JToolBar createUnobtrusiveToolBar() {
    JToolBar tb = new JToolBar();
    tb.setFloatable(false);
    tb.setOpaque(false);
    tb.setBorder(BorderFactory.createEmptyBorder());
    return tb;
  }

  /**
   * Wrapping a button in a tool bar makes it appear good in most look and feels: no borders, good hover behavior.
   * @param aButton The button.
   * @return The button wrapped in a tool bar.
   */
  protected static JComponent wrapWithToolBar(AbstractButton aButton) {
    JToolBar tb = createUnobtrusiveToolBar();
    tb.add(aButton);
    return tb;
  }

  private static void initProgressBarRange(JProgressBar aProgressBar) {
    aProgressBar.setMinimum(0);
    aProgressBar.setMaximum(100);
  }

  private static void updateProgressBarForProgress(double aProgress, JProgressBar aProgressBar) {
    if (Double.isNaN(aProgress)) {
      aProgressBar.setIndeterminate(true);
      aProgressBar.setValue(0);
    } else {
      aProgressBar.setIndeterminate(false);
      aProgressBar.setValue((int) (aProgress * 100));
    }
  }

  static class InterruptTaskAction extends ALcdAction {
    private final Object fLock = new Object();
    private WeakReference<Thread> fThread = new WeakReference<>(null);
    private final String fTask;
    private final ILcyLucyEnv fLucyEnv;

    InterruptTaskAction(String aTask, ILcyLucyEnv aLucyEnv) {
      super(TLcyLang.getString("Cancel"));
      putValue(ILcdAction.SHOW_ACTION_NAME, false);
      fTask = aTask;
      fLucyEnv = aLucyEnv;
      setEnabled(false);
      setVisible(false);
    }

    private void setVisible(boolean aVisible) {
      putValue(ILcdAction.VISIBLE, aVisible);
    }

    void update(Thread aThread, boolean aBlocking) {
      if (aBlocking) {
        return;
      }
      fThread = new WeakReference<>(aThread);
      setEnabled(aThread != null && !fLucyEnv.getWorkspaceManager().isDecodingWorkspace());
      setVisible(!fLucyEnv.getWorkspaceManager().isDecodingWorkspace());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Thread thread;
      synchronized (fLock) {
        thread = fThread.get();
      }

      if (thread != null) {
        try {
          thread.interrupt();
          setEnabled(false);
        } catch (SecurityException ex) {
          //ignore
        }
      }
    }
  }
}
