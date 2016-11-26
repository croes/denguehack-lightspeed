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
package samples.lightspeed.demo.application.data.weather;

import java.util.List;

import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Creates the extruded icing panel for the extended weather theme.
 */
class ExtrudedIcingPanelFactory extends IcingPanelFactory {

  public ExtrudedIcingPanelFactory(IcingModel aIcingModel, List<ILspLayer> aIcingSeverityExtrudedContourLayers, List<ILspLayer> aIcingSLDExtrudedContourLayers) {
    super(aIcingModel, aIcingSeverityExtrudedContourLayers, aIcingSLDExtrudedContourLayers);
  }

  @Override
  protected MultiDimensionalRangeFilter getMultiDimensionalRangeFilterFromSeverityLayer(ILspLayer layer) {
    return IcingSeverityExtrudedContourLayerFactory.getMultiDimensionalRangeFilter(layer);
  }

  protected MultiDimensionalRangeFilter getMultiDimensionalRangeFilterFromSLDLayer(ILspLayer layer) {
    return IcingSLDExtrudedContourLayerFactory.getMultiDimensionalRangeFilter(layer);
  }

  @Override
  protected IcingSeverityFilter getSeverityFilterFromSeverityLayer(ILspLayer layer) {
    return IcingSeverityExtrudedContourLayerFactory.getSeverityFilter(layer);
  }

  @Override
  protected IcingProbabilityFilter getProbabilityFilterFromSeverityLayer(ILspLayer layer) {
    return IcingSeverityExtrudedContourLayerFactory.getProbabilityFilter(layer);
  }

}
