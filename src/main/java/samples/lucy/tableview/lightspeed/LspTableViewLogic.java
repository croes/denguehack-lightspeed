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
package samples.lucy.tableview.lightspeed;

import samples.lucy.tableview.IExtendedTableModel;
import samples.lucy.tableview.ITableViewLogic;
import samples.lucy.tableview.TableViewFilterUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdDynamicFilter;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ALspLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Lightspeed implementation of <code>ITableViewLogic</code>
 *
 */
public class LspTableViewLogic implements ITableViewLogic {
  private final ILcyLucyEnv fLucyEnv;

  public LspTableViewLogic(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public boolean acceptModelContext(TLcyModelContext aModelContext) {
    return aModelContext != null &&
           aModelContext.getLayer() instanceof ILspLayer &&
           aModelContext.getView() instanceof ILspView;
  }

  @Override
  public ILcdFilter<?> retrieveLayerFilter(ILcdLayer aLayer) {
    if (aLayer instanceof ALspLayer) {
      return ((ALspLayer) aLayer).getFilter();
    }
    return null;
  }

  @Override
  public TLcyModelObjectFilter findOrAddFilter(ILcdLayer aLayer, IExtendedTableModel aExtendedTableModel) {
    if (aLayer instanceof ALspLayer) {
      ILcdIntegerIndexedModel modelForTableView = aExtendedTableModel.getOriginalModel();
      TLcyModelObjectFilter installedFilter = findInstalledFilter((ALspLayer) aLayer, modelForTableView);
      if (installedFilter == null) {
        TLcyModelObjectFilter newFilter = new TLcyModelObjectFilter(modelForTableView);
        if (addFilterToLayer(newFilter, aLayer)) {
          return newFilter;
        }
      } else {
        return installedFilter;
      }
    }
    return null;
  }

  private TLcyModelObjectFilter findInstalledFilter(ALspLayer aLayer, ILcdModel aModel) {
    ILcdDynamicFilter<?> filter = aLayer.getFilter();
    return TableViewFilterUtil.findInstalledFilter(filter, aModel);
  }

  @Override
  public boolean addFilterToLayer(ILcdFilter aFilter, ILcdLayer aLayerSFCT) {
    if (!(aFilter instanceof ILcdDynamicFilter) || !(aLayerSFCT instanceof ALspLayer)) {
      return false;
    }
    ALspLayer lspLayer = (ALspLayer) aLayerSFCT;

    ILcdDynamicFilter currentLayerFilter = lspLayer.getFilter();
    ILcdFilter newFilter = TableViewFilterUtil.combineFilters(currentLayerFilter, aFilter);
    if (newFilter != currentLayerFilter) {
      lspLayer.setFilter((ILcdDynamicFilter) newFilter);
    }
    return true;
  }
}
