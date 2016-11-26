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
package samples.lightspeed.common;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyleChangeListener;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyleChangeEvent;

/**
 * Class that make it easier to wrap stylers.
 */
public class StylerWrapper implements ILspStyler {

  private final ILspStyler fDelegate;
  private final CopyOnWriteArrayList<ILspStyleChangeListener> fListeners = new CopyOnWriteArrayList<>();

  public StylerWrapper(ILspStyler aDelegate) {
    fDelegate = aDelegate;
    fDelegate.addStyleChangeListener(new ILspStyleChangeListener() {
      @Override
      public void styleChanged(TLspStyleChangeEvent aEvent) {
        TLspStyleChangeEvent event = new TLspStyleChangeEvent(StylerWrapper.this,
                                                              aEvent.getAffectedModel(),
                                                              aEvent.getAffectedObjects(),
                                                              aEvent.getAffectedStyles());
        fireStyleChangeEvent(event);
      }
    });
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    fDelegate.style(aObjects, aStyleCollector, aContext);
  }

  @Override
  public void addStyleChangeListener(ILspStyleChangeListener aListener) {
    fListeners.add(aListener);
  }

  @Override
  public void removeStyleChangeListener(ILspStyleChangeListener aListener) {
    fListeners.remove(aListener);
  }

  public void fireStyleChangeEvent() {
    fireStyleChangeEvent(new TLspStyleChangeEvent(this, null, null, null));
  }

  private void fireStyleChangeEvent(TLspStyleChangeEvent aStyleChangeEvent) {
    for (ILspStyleChangeListener listener : fListeners) {
      listener.styleChanged(aStyleChangeEvent);
    }
  }

}
