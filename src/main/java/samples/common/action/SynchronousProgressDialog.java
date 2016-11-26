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
package samples.common.action;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;

/**
 * Progress dialog for synchronous tasks.
 */
class SynchronousProgressDialog extends JDialog {

  private final JProgressBar fBar = new JProgressBar(0, 100);
  private final ILcdStatusListener fListener;
  private final ILcdStatusSource fStatusSource;

  public SynchronousProgressDialog(String aTitle, Frame aParent, ILcdStatusSource aStatusSource) {
    super(aParent, aTitle);
    setResizable(false);
    setLayout(new BorderLayout());
    fBar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    add(fBar);
    pack();

    fListener = new ILcdStatusListener() {
      @Override
      public void statusChanged(TLcdStatusEvent aStatusEvent) {
        setProgress(aStatusEvent.getValue());
      }
    };
    fStatusSource = aStatusSource;
    fStatusSource.addStatusListener(fListener);

    TLcdAWTUtil.centerWindow(this);
    setVisible(true);
  }

  @Override
  public void setVisible(boolean b) {
    if (!b) {
      fStatusSource.removeStatusListener(fListener);
    }
    super.setVisible(b);
  }

  private void setProgress(double aValue) {
    fBar.setValue((int) (aValue * 100));
    fBar.paintImmediately(0, 0, fBar.getWidth(), fBar.getHeight());
  }
}
