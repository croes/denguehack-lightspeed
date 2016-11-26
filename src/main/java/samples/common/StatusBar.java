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
package samples.common;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;

/**
 * A status bar that listens to TLcdStatusEvents.
 */
public class StatusBar extends JPanel implements ILcdStatusListener {

  private final JProgressBar fProgressBar = new JProgressBar();
  private final JLabel fLabel = new JLabel();
  private final Timer fTimer = new Timer(10000, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      fLabel.setVisible(false);
    }
  });

  public StatusBar() {
    super();
    this.setLayout(new BorderLayout(5, 0));
    this.add(fProgressBar, BorderLayout.WEST);
    this.add(fLabel, BorderLayout.CENTER);
    fProgressBar.setVisible(false);
    fLabel.setVisible(false);
    fTimer.setRepeats(false);
  }

  public void statusChanged(final TLcdStatusEvent aStatusEvent) {
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      public void run() {
        fProgressBar.setIndeterminate(aStatusEvent.isProgressIndeterminate());
        fProgressBar.setVisible(aStatusEvent.getID() != TLcdStatusEvent.END_BUSY);
        if ((aStatusEvent.getID() == TLcdStatusEvent.PROGRESS)
            || (aStatusEvent.getID() == TLcdStatusEvent.START_BUSY)
            || (aStatusEvent.getID() == TLcdStatusEvent.END_BUSY)) {
          fProgressBar.setValue((int) (aStatusEvent.getValue() * 100));
          fLabel.setText(aStatusEvent.getMessage());
        }
        if (aStatusEvent.getID() != TLcdStatusEvent.END_BUSY) {
          fTimer.stop();
          fLabel.setVisible(true);
        } else {
          fTimer.restart();
        }
      }
    });
  }

  public void setText(String aText) {
    fLabel.setText(aText);
  }
}
