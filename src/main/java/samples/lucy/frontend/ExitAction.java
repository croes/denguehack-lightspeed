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
package samples.lucy.frontend;

import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.JFrame;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyXMLAddOnLoader;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Action that properly exits Lucy.  The exit might be veto'ed by some listener,
 * in which case nothing happens.
 */
public class ExitAction extends ALcdAction {
  private final ILcyLucyEnv fLucyEnv;
  private final TLcyXMLAddOnLoader fXMLAddOnLoader;
  private final JFrame fMainFrame;
  private final String fPreferencesPrefix;

  public ExitAction(ILcyLucyEnv aLucyEnv,
                    TLcyXMLAddOnLoader aXMLAddOnLoader,
                    JFrame aMainFrame,
                    String aPreferencesPrefix) {
    super(TLcyLang.getString("Exit"));
    Objects.requireNonNull(aLucyEnv, "The Lucy env should not be null");
    Objects.requireNonNull(aMainFrame, "The main frame should not be null");
    Objects.requireNonNull(aPreferencesPrefix, "The prefix should not be null");
    fLucyEnv = aLucyEnv;
    fXMLAddOnLoader = aXMLAddOnLoader;
    fMainFrame = aMainFrame;
    fPreferencesPrefix = aPreferencesPrefix;
  }

  @Override
  public void actionPerformed(ActionEvent aEvent) {
    try {
      fLucyEnv.setLucyEnvState(ILcyLucyEnv.STATE_CLOSING);
      FrontEndUtil.storeFrameState(fMainFrame, fPreferencesPrefix, fLucyEnv);
      fMainFrame.dispose();

      //disposing the frame might schedule other runnables, which should finish before we start unplugging add-ons
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            if (fXMLAddOnLoader != null) {
              fXMLAddOnLoader.unLoadAddOns();
            }
            fLucyEnv.setLucyEnvState(ILcyLucyEnv.STATE_CLOSED);
            fLucyEnv.setLucyEnvState(ILcyLucyEnv.STATE_DISPOSING);
            //using invokeLater allows to dispose the main frame completely before
            //we further dispose Lucy and the JVM
            TLcdAWTUtil.invokeLater(new Runnable() {
              @Override
              public void run() {
                try {
                  fLucyEnv.setLucyEnvState(ILcyLucyEnv.STATE_DISPOSED);
                  //As of Java 1.5, we could do without calling System.exit ...
                  System.exit(0);
                } catch (TLcyVetoException e) {
                  throw new RuntimeException("Throwing TLcyVetoException's when Lucy switches state is only supported when switching from STATE_INITIALIZED to STATE_CLOSING. "
                                             + "Throwing an exception on other state transitions will leave Lucy in an undefined state.", e);
                }
              }
            });
          } catch (TLcyVetoException error) {
            throw new RuntimeException("Throwing TLcyVetoException's when Lucy switches state is only supported when switching from STATE_INITIALIZED to STATE_CLOSING. "
                                       + "Throwing an exception on other state transitions will leave Lucy in an undefined state.");
          }
        }
      });
    } catch (TLcyVetoException ignore) {
      //somebody veto'ed closing the application.  This means we don't have to
      //do anything.  Perhaps the next time the user closes the window it succeeds
    }
  }
}
