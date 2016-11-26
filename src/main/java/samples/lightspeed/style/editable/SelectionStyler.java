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
package samples.lightspeed.style.editable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyleChangeListener;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyleChangeEvent;

/**
 * Custom styler that is used to submit a style when the object is selected,
 * based on the unselected style of that object. The unselected style of the object is
 * assumed to be editable.
 */
public class SelectionStyler extends ALspStyler {

  private TLspEditableStyler fEditableProvider;
  private TLspFillStyle.Builder<?> fFillBuilder;
  private TLspLineStyle.Builder<?> fLineBuilder;

  public SelectionStyler(TLspEditableStyler aEditableProvider) {
    fEditableProvider = aEditableProvider;
    fFillBuilder = TLspFillStyle.newBuilder();
    fLineBuilder = TLspLineStyle.newBuilder();
    aEditableProvider.addStyleChangeListener(new ILspStyleChangeListener() {
      // If the wrapped styler generates new styles, we should also fire an event.
      @Override
      public void styleChanged(TLspStyleChangeEvent aEvent) {
        fireStyleChangeEvent(aEvent.getAffectedModel(), aEvent.getAffectedObjects(), aEvent.getAffectedStyles());
      }
    });
  }

  @Override
  public void style(final Collection<?> aObjects, final ALspStyleCollector aStyleCollector, TLspContext aContext) {
    ALspStyleCollector collector = new ALspStyleCollector(aObjects) {
      @Override
      protected void submitImpl() {
        TLspLineStyle lineStyle = getFirstLineStyle(getStyles());
        TLspFillStyle fillStyle = getFirstFillStyle(getStyles());

        if (lineStyle != null && fillStyle != null) {
          double lw = lineStyle.getWidth() + 2.;
          fLineBuilder.all(lineStyle).width(lw);
          fFillBuilder.all(fillStyle);
          if (getStyleTargetProvider() != null) {
            aStyleCollector.geometry(getStyleTargetProvider());
          }
          aStyleCollector.objects(getObjects())
                         .styles(Arrays.<ALspStyle>asList(fFillBuilder.build(), fLineBuilder.build())).submit();
        } else if (lineStyle != null) {
          double lw = lineStyle.getWidth() + 2;
          fLineBuilder.all(lineStyle).width(lw);
          if (getStyleTargetProvider() != null) {
            aStyleCollector.geometry(getStyleTargetProvider());
          }
          aStyleCollector.objects(getObjects())
                         .styles(Arrays.<ALspStyle>asList(fLineBuilder.build())).submit();
        } else {
          if (getStyleTargetProvider() != null) {
            aStyleCollector.geometry(getStyleTargetProvider());
          }
          aStyleCollector.objects(getObjects()).styles(getStyles()).submit();
        }

      }
    };
    fEditableProvider.style(aObjects, collector, aContext);
  }

  private TLspFillStyle getFirstFillStyle(List<ALspStyle> aStyles) {
    if (aStyles == null) {
      return null;
    }
    for (ALspStyle style : aStyles) {
      if (style instanceof TLspFillStyle) {
        return (TLspFillStyle) style;
      }
    }
    return null;
  }

  private TLspLineStyle getFirstLineStyle(List<ALspStyle> aStyles) {
    if (aStyles == null) {
      return null;
    }
    for (ALspStyle style : aStyles) {
      if (style instanceof TLspLineStyle) {
        return (TLspLineStyle) style;
      }
    }
    return null;
  }

}
