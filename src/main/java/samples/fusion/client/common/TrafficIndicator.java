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
package samples.fusion.client.common;

import static java.lang.Math.round;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JComponent;

import com.luciad.gui.TLcdAWTUtil;

/**
 * A concurrent traffic indicator that enables a given component while data is being received, and disables it when
 * traffic stops. It does reference counting to keep track of concurrent traffic.
 */
public class TrafficIndicator {

  private final JComponent fComponent;

  private final Runnable fEnabler = new Runnable() {

    public void run() {
      setEnabled(true);
    }
  };

  private final Runnable fDisabler = new Runnable() {

    public void run() {
      setEnabled(false);
    }
  };

  private final AtomicBoolean fTooltipperScheduled = new AtomicBoolean();

  private final Runnable fTooltipper = new Runnable() {

    public void run() {
      fTooltipperScheduled.set(false);
      setToolTipText();
    }
  };

  private final AtomicLong fReceived = new AtomicLong();

  private final AtomicInteger fNumRequestsInProgress = new AtomicInteger();

  public TrafficIndicator(JComponent aComponent) {
    fComponent = aComponent;
    fComponent.setEnabled(false);
    setToolTipText();
  }

  public RequestProgress newRequest() {
    if (fNumRequestsInProgress.getAndIncrement() == 0) {
      TLcdAWTUtil.invokeLater(fEnabler);
    }
    return new RequestProgress();
  }

  private void received(int aNumBytes) {
    if (aNumBytes > 0) {
      fReceived.addAndGet(aNumBytes);
      if (fTooltipperScheduled.compareAndSet(false, true)) {
        TLcdAWTUtil.invokeLater(fTooltipper);
      }
    }
  }

  private void requestDone() {
    if (fNumRequestsInProgress.decrementAndGet() == 0) {
      TLcdAWTUtil.invokeLater(fDisabler);
    }
  }

  private void setToolTipText() {
    long receivedKB = round(1.e-3 * fReceived.get());
    String text = new StringBuilder(100).append(receivedKB).append(" KB received").toString();
    fComponent.setToolTipText(text);
  }

  private void setEnabled(boolean aEnabled) {
    fComponent.setEnabled(aEnabled);
  }

  /**
   * Tracks the progress of a single request.
   */
  public class RequestProgress {

    private final AtomicBoolean fDone = new AtomicBoolean(false);

    public void received(int aNumBytes) {
      TrafficIndicator.this.received(aNumBytes);
    }

    public void done() {
      if (fDone.compareAndSet(false, true)) {
        TrafficIndicator.this.requestDone();
      }
    }
  }
}
