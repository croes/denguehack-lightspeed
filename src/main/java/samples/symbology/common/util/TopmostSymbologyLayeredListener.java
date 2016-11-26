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
package samples.symbology.common.util;

import java.lang.ref.WeakReference;

import samples.symbology.common.EMilitarySymbology;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;

/**
 * A weak listener that tracks the topmost symbology layer.
 *
 * @param <T> the object that should be notified if the topmost layer changes
 */
public abstract class TopmostSymbologyLayeredListener<T> implements ILcdLayeredListener {

  private final ILcdLayered fView;
  private final WeakReference<T> fField;

  public TopmostSymbologyLayeredListener(ILcdLayered aView, T aField) {
    fView = aView;
    fField = new WeakReference<T>(aField);
  }

  @Override
  public void layeredStateChanged(TLcdLayeredEvent e) {
    // For views with many layers, this functionality should be throttled to
    // avoid a performance impact.
    T target = fField.get();
    if (target == null) {
      fView.removeLayeredListener(this);
    } else {
      setSymbology(target, MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(fView));
    }
  }

  protected abstract void setSymbology(T aField, EMilitarySymbology aSymbology);

}
