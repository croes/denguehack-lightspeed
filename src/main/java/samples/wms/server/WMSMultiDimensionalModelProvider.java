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
package samples.wms.server;

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.ogc.common.ALcdRequestContext;
import com.luciad.wms.server.ALcdMultiDimensionalModelProvider;
import com.luciad.wms.server.ILcdModelDecoderFactory;
import com.luciad.wms.server.TLcdWMSRequestContext;
import com.luciad.wms.server.model.ALcdWMSDimension;
import com.luciad.wms.server.model.ALcdWMSLayer;
import com.luciad.wms.server.model.TLcdWMSDimensionExtent;

import samples.wms.server.updater.DirectoryMonitorEvent;
import samples.wms.server.updater.DirectoryMonitorListener;
import samples.wms.server.updater.DynamicModelProvider;

/**
 * Implementation of ALcdMultiDimensionalModelProvider that provides support
 * for multi-dimensional layers.
 */
public class WMSMultiDimensionalModelProvider extends ALcdMultiDimensionalModelProvider
    implements DirectoryMonitorListener {

  // The model provider to which is delegated.
  private DynamicModelProvider fModelProvider;

  /**
   * Creates a new instance with the specified model decoder factories.
   *
   * @param aModelDecoderFactories The model decoder factories to be used by this model provider.
   */
  public WMSMultiDimensionalModelProvider(ILcdModelDecoderFactory[] aModelDecoderFactories) {
    fModelProvider = new DynamicModelProvider(aModelDecoderFactories.clone());
  }

  public ILcdModel getModel(String aSourceName, ALcdRequestContext aRequestContext) throws IOException {
    return fModelProvider.getModel(aSourceName, aRequestContext);
  }

  public ILcdModel getModel(ALcdWMSLayer aLayer, ALcdWMSDimension[] aDimensions, TLcdWMSDimensionExtent[] aDimensionExtent, TLcdWMSRequestContext aWMSRequestContext) throws IOException {
    // The sample server contains only one layer, 'Custom', with one dimension. Consequently, this method is only invoked for that layer,
    // and the supplied dimension / dimension extent arrays each contain exactly one element.
    // The dimension defines 3 available values, 1, 2 or 3, with 1 as default value.
    // The layer's source name is used as the base directory containing the multi-dimensional dataset;
    // the dimensional values are used to determine the concrete datasource.
    return getModel(aLayer.getSourceName() + "/mixed_" + aDimensionExtent[0].getValue(0) + ".ctm", aWMSRequestContext);
  }

  public TLcdWMSDimensionExtent getDimensionExtent(ALcdWMSLayer aLayer, ALcdWMSDimension aDimension, TLcdWMSDimensionExtent aSuppliedExtent, TLcdWMSRequestContext aWMSRequestContext) {
    TLcdWMSDimensionExtent resolvedExtent = super.getDimensionExtent(aLayer, aDimension, aSuppliedExtent, aWMSRequestContext);

    // Check if the default implementation of getDimensionExtent() could resolve and validate the supplied extent.
    if (resolvedExtent == null || (resolvedExtent.getValueCount() == 0 && resolvedExtent.getIntervalCount() == 0)) {
      // Not resolved and validated: perform custom validation if desired.
      // If the supplied extent is accepted, return it. Otherwise, return the empty extent 'resolved_extent' or null,
      // and a proper service exception report will be sent to the client, indicating that the supplied dimension parameter
      // of the given dimension is not valid.
      return aSuppliedExtent;
    }
    return resolvedExtent;
  }

  public void event(DirectoryMonitorEvent aEvent) {
    fModelProvider.event(aEvent);
  }
}
