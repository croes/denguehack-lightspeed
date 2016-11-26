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
package samples.lucy.lightspeed.map.guifactory;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.lightspeed.TLcyLspMapLayerControlFactory;
import com.luciad.lucy.util.TLcyCompositeAndFilter;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Map Layer Control factory that will disable the visibility checkboxes for all layers
 * and disable the delete action for SHP layers.
 */
public class LspMapLayerControlFactory extends TLcyLspMapLayerControlFactory {
  public LspMapLayerControlFactory(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv);
  }

  @Override
  protected ILcdFilter<ILspLayer> createActiveSettableEnabledFilter(int aID, ALcyProperties aProperties) {
    ILcdFilter<ILspLayer> super_filter = super.createActiveSettableEnabledFilter(aID, aProperties);

    //Make sure we use the filter created by the super implementation.
    TLcyCompositeAndFilter<ILspLayer> my_filter = new TLcyCompositeAndFilter<ILspLayer>();
    my_filter.addFilter(super_filter);

    switch (aID) {
    case LAYER_VISIBLE_ACTIVE_SETTABLE_ENABLED_FILTER:
      //None of the visibility checkboxes may be enabled.
      my_filter.addFilter(new AcceptNothingFilter());
      break;
    }
    return my_filter;
  }

  @Override
  protected ILcdFilter<ILspLayer> createActionEnabledFilter(int aID, ALcyProperties aProperties) {
    ILcdFilter<ILspLayer> super_filter = super.createActionEnabledFilter(aID, aProperties);

    //Make sure we use the filter created by the super implementation.
    TLcyCompositeAndFilter<ILspLayer> my_filter = new TLcyCompositeAndFilter<ILspLayer>();
    my_filter.addFilter(super_filter);

    switch (aID) {
    case DELETE_LAYER_ACTION_ENABLED_FILTER:
      //None of the SHP layers may be removed.
      my_filter.addFilter(new NonSHPLayerFilter());
      break;
    }
    return my_filter;
  }

  private static class AcceptNothingFilter implements ILcdFilter<ILspLayer> {
    @Override
    public boolean accept(ILspLayer aObject) {
      return false;
    }
  }

  private static class NonSHPLayerFilter implements ILcdFilter<ILspLayer> {
    @Override
    public boolean accept(ILspLayer aObject) {
      if (aObject != null && aObject.getModel() != null) {
        return !(aObject.getModel().getModelDescriptor() instanceof TLcdSHPModelDescriptor);
      }
      return false;
    }
  }
}
