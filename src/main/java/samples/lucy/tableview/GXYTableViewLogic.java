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
package samples.lucy.tableview;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.TLcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerRunnable;

/**
 * GXY implementation of <code>ITableViewLogic</code>
 */
public class GXYTableViewLogic implements ITableViewLogic {

  private final ILcyLucyEnv fLucyEnv;

  public GXYTableViewLogic(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public boolean acceptModelContext(TLcyModelContext aModelContext) {
    return aModelContext != null &&
           aModelContext.getLayer() instanceof ILcdGXYLayer &&
           aModelContext.getView() instanceof ILcdGXYView;
  }

  @Override
  public ILcdFilter<?> retrieveLayerFilter(ILcdLayer aLayer) {
    final ILcdFilter<?>[] layerFilter = new ILcdFilter[]{null};
    if (aLayer instanceof ILcdGXYLayer) {
      fLucyEnv.getMapManager().getAsynchronousPaintFacade().invokeNowOnGXYLayer((ILcdGXYLayer) aLayer, new ILcdGXYAsynchronousLayerRunnable() {
        @Override
        public void run(ILcdGXYLayer aSafeGXYLayer) {
          if (aSafeGXYLayer instanceof TLcdLayer) {
            layerFilter[0] = ((TLcdLayer) aSafeGXYLayer).getFilter();
          }
        }
      });
    }
    return layerFilter[0];
  }

  @Override
  public TLcyModelObjectFilter findOrAddFilter(ILcdLayer aLayer, final IExtendedTableModel aExtendedTableModel) {
    final TLcyModelObjectFilter[] filter = new TLcyModelObjectFilter[]{null};
    //gxy layers might be asynchronous, so pass it through the facade
    if (aLayer instanceof ILcdGXYLayer) {
      try {
        fLucyEnv.getMapManager().getAsynchronousPaintFacade().invokeAndWaitOnGXYLayer((ILcdGXYLayer) aLayer, new ILcdGXYAsynchronousLayerRunnable() {
          @Override
          public void run(ILcdGXYLayer aSafeGXYLayer) {
            //We can perform the cast, due to the filter of model contexts
            if (aSafeGXYLayer instanceof TLcdLayer) {
              ILcdIntegerIndexedModel modelForTableView = aExtendedTableModel.getOriginalModel();
              TLcyModelObjectFilter installedFilter = findInstalledFilter((TLcdLayer) aSafeGXYLayer,
                                                                          modelForTableView);
              if (installedFilter != null) {
                filter[0] = installedFilter;
              } else {
                TLcyModelObjectFilter modelObjectFilter = new TLcyModelObjectFilter(modelForTableView);
                if (addFilterToLayer(modelObjectFilter, aSafeGXYLayer)) {
                  filter[0] = modelObjectFilter;
                }
              }
            }
          }
        });
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    return filter[0];
  }

  private TLcyModelObjectFilter findInstalledFilter(TLcdLayer aLayer, ILcdModel aModel) {
    ILcdFilter<?> filter = aLayer.getFilter();
    return TableViewFilterUtil.findInstalledFilter(filter, aModel);
  }

  @Override
  public boolean addFilterToLayer(ILcdFilter aFilter, ILcdLayer aLayerSFCT) {
    if (!(aLayerSFCT instanceof TLcdLayer)) {
      return false;
    }
    ILcdFilter currentLayerFilter = ((TLcdLayer) aLayerSFCT).getFilter();
    ILcdFilter newFilter = TableViewFilterUtil.combineFilters(currentLayerFilter, aFilter);
    if (newFilter != currentLayerFilter) {
      ((TLcdLayer) aLayerSFCT).setFilter(newFilter);
    }
    return true;
  }
}
