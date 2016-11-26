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
package samples.decoder.asterix;

import java.awt.Component;
import java.lang.ref.WeakReference;

import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdUserDialog;

/**
 * Shows a message when the lie decoded model has finished decoding, or if an error occurred.
 */
public abstract class LiveDecoderResultCallback implements LiveDecodedModel.ResultCallback {

  private final WeakReference<Component> fComponent;
  private final String fFinishedMessage;
  private final String fErrorMessage;

  public LiveDecoderResultCallback(Component aComponent, String aFile) {
    fComponent = new WeakReference<>(aComponent);
    fFinishedMessage = "Successfully finished replaying [" + aFile + "]";
    fErrorMessage = "An error occurred during the decoding of [" + aFile + "]";
  }

  @Override
  public void update(LiveDecodedModel aModel) {
  }

  @Override
  public void finished(LiveDecodedModel aModel) {
    showMessage(fFinishedMessage, ILcdDialogManager.PLAIN_MESSAGE);
  }

  @Override
  public void error(LiveDecodedModel aModel) {
    showMessage(fErrorMessage, ILcdDialogManager.ERROR_MESSAGE);
  }

  private void showMessage(final String aMessage, final int aMessageType) {
    Runnable showMessageRunnable = new Runnable() {
      @Override
      public void run() {
        Component component = fComponent.get();
        if (component != null) {
          TLcdUserDialog.message(
              aMessage,
              aMessageType,
              component,
              component
          );
        }
      }
    };
    TLcdAWTUtil.invokeNowOrLater(showMessageRunnable);
  }
}
