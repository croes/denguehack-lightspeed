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
package samples.lucy.drawing.styles;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.drawing.TLcyDrawingAddOn;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyMapComponent;

/**
 * GXY version of {@link ApplyStyleAddOn}
 *
 */
public class GXYApplyStyleAddOn extends ApplyStyleAddOn {

  private TLcyDrawingAddOn fDrawingAddOn;

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    fDrawingAddOn = aLucyEnv.retrieveAddOnByClass(TLcyDrawingAddOn.class);
    if (fDrawingAddOn == null) {
      throw new RuntimeException("Can not find the TLcyDrawingAddOn. Please load the drawing add-on before loading the GXYApplyStyleAddOn");
    }
    super.plugInto(aLucyEnv);
  }

  @Override
  protected boolean canHandleMapComponent(ILcyGenericMapComponent<?, ?> aMapComponent) {
    return aMapComponent instanceof ILcyMapComponent;
  }

  @Override
  protected ApplyStyleActionFactory createApplyStyleActionFactory(ILcyGenericMapComponent<?, ?> aMapComponent) {
    return new ApplyStyleActionFactory(fDrawingAddOn.getLucyEnv(), aMapComponent, fDrawingAddOn.getDrawingSettingsForMap(aMapComponent.getMainView()));
  }
}
