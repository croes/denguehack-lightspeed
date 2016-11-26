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
package samples.symbology.lightspeed;

import java.beans.PropertyChangeListener;

import com.luciad.model.ILcdModel;
import com.luciad.symbology.app6a.model.TLcdAPP6AModelDescriptor;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ALayerBuilder;
import com.luciad.util.ILcdInterval;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * A layer factory for models containing APP-6 objects.
 * Clusters by default.
 */
public class APP6LayerFactory extends MilitarySymbologyLayerFactory {

  public APP6LayerFactory(PropertyChangeListener aPropertyChangeListener) {
    this(null, aPropertyChangeListener, Clustering.ENABLED);
  }

  public APP6LayerFactory(ILcdInterval aLabelScaleRange, PropertyChangeListener aPropertyChangeListener, Clustering aClustering) {
    super(aLabelScaleRange, aPropertyChangeListener, aClustering);
  }

  @Override
  protected ILspLayer createLayer(ILcdModel aModel, ILcdInterval aLabelScaleRange) {
    ILspStyler regularBodyStyler = new ClusterAwareAPP6SymbolStyler(TLspPaintState.REGULAR);
    ILspStyler selectedBodyStyler = new ClusterAwareAPP6SymbolStyler(TLspPaintState.SELECTED);
    ILspStyler regularLabelStyler = new ClusterAwareMilitarySymbologyLabelStylerWrapper(regularBodyStyler, TLspPaintState.REGULAR);
    ILspStyler selectedLabelStyler = new ClusterAwareMilitarySymbologyLabelStylerWrapper(regularBodyStyler, TLspPaintState.SELECTED);
    ILspLayer layer;
    layer = TLspAPP6ALayerBuilder.newBuilder()
                                 .objectViewMargin(50)
                                 .model(aModel)
                                 .bodyStyler(TLspPaintState.REGULAR, regularBodyStyler)
                                 .bodyStyler(TLspPaintState.SELECTED, selectedBodyStyler)
                                 .labelStyler(TLspPaintState.REGULAR, regularLabelStyler)
                                 .labelStyler(TLspPaintState.SELECTED, selectedLabelStyler)
                                 .labelScaleRange(aLabelScaleRange)
                                 .build();
    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel != null && aModel.getModelDescriptor() instanceof TLcdAPP6AModelDescriptor;
  }

}
