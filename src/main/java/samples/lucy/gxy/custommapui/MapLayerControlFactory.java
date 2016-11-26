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
package samples.lucy.gxy.custommapui;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.lucy.map.ILcyMapLayerControl;
import com.luciad.lucy.map.TLcyMapLayerControlFactory;
import com.luciad.lucy.util.TLcyCompositeAndFilter;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYLayer;

/**
 * Map Layer Control factory that will disable the visibility checkboxes for all layers
 * and disable the delete action for SHP layers.
 */
public class MapLayerControlFactory extends TLcyMapLayerControlFactory {
  @Override
  protected ILcdFilter<ILcdGXYLayer> createEnabledFilter(int aID, ILcyMapLayerControl aLayerControl) {
    ILcdFilter<ILcdGXYLayer> super_filter = super.createEnabledFilter(aID, aLayerControl);

    //make sure we use the filter created by the super implementation
    TLcyCompositeAndFilter<ILcdGXYLayer> my_filter = new TLcyCompositeAndFilter<ILcdGXYLayer>();
    my_filter.addFilter(super_filter);

    switch (aID) {
    case LAYER_VISIBLE_ACTIVE_SETTABLE_ENABLED_FILTER:
      //none of the visibility checkboxes may be enabled.
      my_filter.addFilter(new AcceptNothingFilter());
      break;
    case DELETE_LAYER_ACTION_ENABLED_FILTER:
      //none of the SHP layers may be removed.
      my_filter.addFilter(new NonSHPLayerFilter());
      break;
    }
    return my_filter;
  }

  private static class AcceptNothingFilter implements ILcdFilter<ILcdGXYLayer> {
    @Override
    public boolean accept(ILcdGXYLayer aLayer) {
      return false;
    }
  }

  private static class NonSHPLayerFilter implements ILcdFilter<ILcdGXYLayer> {
    @Override
    public boolean accept(ILcdGXYLayer aLayer) {
      return !(aLayer.getModel().getModelDescriptor() instanceof TLcdSHPModelDescriptor);
    }
  }
}
