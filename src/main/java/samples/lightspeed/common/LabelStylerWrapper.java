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

import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyleChangeListener;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyleChangeEvent;

/**
 * Class that make it easier to wrap label stylers.
 */
public class LabelStylerWrapper extends ALspLabelStyler {

  private final ILspStyler fDelegate;

  public LabelStylerWrapper(ILspStyler aDelegate) {
    fDelegate = aDelegate;
    fDelegate.addStyleChangeListener(new ILspStyleChangeListener() {
      @Override
      public void styleChanged(TLspStyleChangeEvent aEvent) {
        fireStyleChangeEvent(aEvent.getAffectedModel(),
                             aEvent.getAffectedObjects(),
                             aEvent.getAffectedStyles());
      }
    });
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    if (aStyleCollector instanceof ALspLabelStyleCollector) {
      ALspLabelStyleCollector labelStyleCollector = (ALspLabelStyleCollector) aStyleCollector;
      style(aObjects, labelStyleCollector, aContext);
    } else {
      super.style(aObjects, aStyleCollector, aContext);
    }
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    fDelegate.style(aObjects, aStyleCollector, aContext);
  }

}
