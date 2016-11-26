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
package samples.gxy.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;

/**
 * This utility class can be used to show or hide a progress dialog.
 */
public class ProgressUtil {

  /**
   * Creates a progress dialog. The dialog implements the interface ILcdStatusListener
   * and can be for example used in combination with an ILcdInputStreamFactory to show
   * a progress bar while decoding a model.
   *
   * @param aComponent  the parent component.
   * @param aTitle      the dialog title.
   * @return a progress dialog.
   */
  public static ProgressDialog createProgressDialog(final Component aComponent, final String aTitle) {
    return createProgressDialog(aComponent, aTitle, true);
  }

  /**
   * Creates a progress dialog. The dialog implements the interface ILcdStatusListener
   * and can be for example used in combination with an ILcdInputStreamFactory to show
   * a progress bar while decoding a model.
   *
   * @param aComponent    the parent component.
   * @param aTitle        the dialog title.
   * @param aCancelButton {@code true} to add a cancel button.
   * @return a progress dialog.
   */
  public static ProgressDialog createProgressDialog(final Component aComponent, final String aTitle, final boolean aCancelButton) {
    final ProgressDialog dialog[] = {null};
    final Thread threadToInterrupt = !EventQueue.isDispatchThread() ? Thread.currentThread() : null;
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      public void run() {
        dialog[0] = new ProgressDialog(TLcdAWTUtil.findParentFrame(aComponent), aTitle, 425, 0, aCancelButton);
        if (threadToInterrupt != null) {
          dialog[0].setThreadToInterrupt(threadToInterrupt);
        }
        dialog[0].setVisible(false);
      }
    });
    return dialog[0];
  }

  public static void showDialog(final JDialog aDialog) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        aDialog.setVisible(true);
      }
    });
  }

  public static void hideDialog(final JDialog aDialog) {
    hideDialog(aDialog, true);
  }

  public static void hideDialog(final JDialog aDialog, final boolean aDispose) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        aDialog.setVisible(false);
        if (aDispose) {
          aDialog.dispose();
        }
      }
    });
  }

  public static class ProgressDialog extends JDialog implements ILcdStatusListener {

    private static final int DELAY = 20;

    private JProgressBar fProgressBar = new JProgressBar();
    private JLabel fProgressLabel = new JLabel();
    private JButton fCancelButton = new JButton("Cancel");
    private long fProgressTime = 0;
    private boolean fVisible = true;

    private MyCancelActionListener fCancelActionListener;

    private ProgressDialog(Frame aOwner, String aTitle, int aPreferredWidth, int aPreferredHeight, boolean aCancelButton) {
      super(aOwner, aTitle, true);
      createGUI(aOwner, aPreferredWidth, aPreferredHeight, aCancelButton);
      scheduleSetVisible(DELAY); // automatically shows the dialog after a while
    }

    private void createGUI(Frame aOwner, int aPreferredWidth, int aPreferredHeight, boolean aCancelButton) {
      fProgressLabel.setText(getTitle());

      JPanel panel = new JPanel(new BorderLayout(5, 0));
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      JPanel progressPanel = new JPanel(new BorderLayout(0, 2));
      progressPanel.add(fProgressBar, BorderLayout.NORTH);
      progressPanel.add(fProgressLabel, BorderLayout.SOUTH);
      panel.add(progressPanel, BorderLayout.CENTER);

      if (aCancelButton) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        fCancelButton.setEnabled(false);
        buttonPanel.add(fCancelButton);
        panel.add(buttonPanel, BorderLayout.EAST);
      }

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      pack();             // adjusts the initial preferred size of the panel

      Dimension preferredSize = panel.getPreferredSize();
      if (aPreferredWidth != 0) {
        preferredSize.width = aPreferredWidth;
      }
      if (aPreferredHeight != 0) {
        preferredSize.height = aPreferredHeight;
      }
      panel.setPreferredSize(preferredSize);
      pack();             // set the user preferred size to the panel

      setModalityType(ModalityType.MODELESS); // don't block input dialogs

      if (aOwner != null) {
        setLocation(
            (int) aOwner.getLocation().getX() + (aOwner.getWidth() - getSize().width) / 2,
            (int) aOwner.getLocation().getY() + (aOwner.getHeight() - getSize().height) / 2
                   );
      } else {
        TLcdAWTUtil.centerWindow(this);
      }
    }

    private void scheduleSetVisible(int aDelay) {
      Timer timer = new Timer(aDelay, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          ProgressDialog.super.setVisible(fVisible);
        }
      });
      timer.setRepeats(false);
      timer.start();
    }

    @Override
    public void setVisible(boolean aVisible) {
      fVisible = aVisible;

      // Workaround for Oracle Java Bug 5109571:
      // calling JDialog.setVisible(false)
      // immediately after calling JDialog.setVisible(true)
      // will not hide the dialog in some cases.
      // A workaround is to delay the hiding.
      if (!aVisible) {
        scheduleSetVisible(50);
      } else {
        super.setVisible(aVisible);
      }
    }

    public void setThreadToInterrupt(Thread aThreadToInterrupt) {
      removeWindowListener(fCancelActionListener);
      fCancelButton.removeActionListener(fCancelActionListener);

      fCancelActionListener = new MyCancelActionListener(aThreadToInterrupt);

      addWindowListener(fCancelActionListener);
      fCancelButton.addActionListener(fCancelActionListener);
      fCancelButton.setEnabled(true);
    }

    public void cancel() {
      fCancelButton.doClick();
    }

    public void statusChanged(TLcdStatusEvent aStatusEvent) {
      switch (aStatusEvent.getID()) {
      case TLcdStatusEvent.PROGRESS:
        long time = System.currentTimeMillis() - fProgressTime;
        int value = Math.max(
            (int) (aStatusEvent.getValue() * 100),
            fProgressBar.getValue()
                            );
        if (time > DELAY && value < 90) {
          showDialog(this);                         // show dialog if it takes a long time
        }
        update(value, aStatusEvent.getMessage());
        break;
      case TLcdStatusEvent.START_BUSY:
        fProgressTime = System.currentTimeMillis();   // start a timer
        update((int) (aStatusEvent.getValue() * 100), aStatusEvent.getMessage());
        break;
      case TLcdStatusEvent.END_BUSY:
        update((int) (aStatusEvent.getValue() * 100), aStatusEvent.getMessage());
        hideDialog(this, false);                    // hide dialog
        break;
      }
    }

    private void update(final int aValue, final String aText) {
      TLcdAWTUtil.invokeNowOrLater(new Runnable() {
        public void run() {
          fProgressBar.setValue(aValue);
          fProgressLabel.setText(aText);
        }
      });
    }

    private static class MyCancelActionListener extends WindowAdapter implements ActionListener {
      private Thread fThreadToInterrupt;

      public MyCancelActionListener(Thread aThreadToInterrupt) {
        fThreadToInterrupt = aThreadToInterrupt;
      }

      public void actionPerformed(ActionEvent aEvent) {
        if (fThreadToInterrupt != null) {
          fThreadToInterrupt.interrupt();
          ((Component) aEvent.getSource()).setEnabled(false);
        }
      }

      public void windowClosing(WindowEvent aEvent) {
        if (fThreadToInterrupt != null) {
          fThreadToInterrupt.interrupt();
        }
      }
    }
  }

}
