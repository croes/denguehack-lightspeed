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

import java.awt.Component;
import java.awt.Container;
import java.lang.ref.WeakReference;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;

/**
 * Destroys Lightspeed views when Lucy is terminated, if they aren't destroyed already.
 */
public final class LspViewDestroyer implements ILcyLucyEnvListener {
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(LspViewDestroyer.class);
  private final WeakReference<ILspView> fView;

  public LspViewDestroyer(ILspView aView) {
    fView = new WeakReference<ILspView>(aView);
  }

  @Override
  public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
    final ILspView view = fView.get();
    if (view != null && // if still relevant
        aEvent.getID() == ILcyLucyEnv.STATE_DISPOSED) { // if Lucy disposes
      TLcdAWTUtil.invokeNowOrLater(new Runnable() {
        @Override
        public void run() {
          String viewAsString = "no toString"; // Avoid calling toString on a view that is already destroyed
          try {
            // if view is not yet destroyed
            if (view.getServices() != null) {
              viewAsString = view.toString();
              if (view instanceof ILspAWTView) {
                Component hostComponent = ((ILspAWTView) view).getHostComponent();
                if (hostComponent != null) {
                  Container parent = hostComponent.getParent();
                  if (parent != null) {
                    parent.remove(hostComponent);
                  }
                }
              }
              view.destroy();
            }
          } catch (Exception e) {
            LOGGER.warn("Destroying the ILspView [" + viewAsString + "] threw an exception", e);
          }
        }
      });
    }
  }
}
