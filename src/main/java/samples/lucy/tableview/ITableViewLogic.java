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

import com.luciad.lucy.util.TLcyModelObjectFilter;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayer;

/**
 * <p>
 *   Interface which forms a facade for the table view for the view-specific logic.
 *   This interface allows to have only one {@link TableViewCustomizerPanelFactory} for all types of views (GXY, Lsp and GL).
 *   The only view-dependent part in that factory is injected by this interface when creating the factory.
 * </p>
 */
public interface ITableViewLogic {

  /**
   * Returns <code>true</code> when the model context can be handled by this
   * <code>ITableViewLogic</code>, <code>false</code> otherwise
   *
   * @param aModelContext The model context
   *
   * @return <code>true</code> when the model context can be handled by this
   *         <code>ITableViewLogic</code>, <code>false</code> otherwise
   */
  boolean acceptModelContext(TLcyModelContext aModelContext);

  /**
   * Finds the <code>TLcyModelObjectFilter</code> placed on {@code aLayer}
   * or creates a new filter, in case there was no TLcyModelObjectFilter present.
   *
   * @param aLayer The layer
   * @param aExtendedTableModel The extended table model
   *
   * @return The <code>TLcyModelObjectFilter</code> already present on {@code aLayer}
   * or a newly added filter in case no such filter was already installed.<br />
   * In case no filter could be installed, this method returns <code>null</code>.
   */
  TLcyModelObjectFilter findOrAddFilter(ILcdLayer aLayer, IExtendedTableModel aExtendedTableModel);

  /**
   * Add the given <code>ILcdFilter</code> to the given layer, assuming the filter and the layer are of the correct type.
   * If the filter can not be added to the layer, do nothing.
   *
   * @param aFilter The <code>ILcdFilter</code> that will be added
   *
   * @param aLayerSFCT The layer to which the <code>ILcdFilter</code> will be added
   * @return <code>true</code> in case the filter was successfully installed. <code>false</code> otherwise.
   */
  boolean addFilterToLayer(ILcdFilter<?> aFilter, ILcdLayer aLayerSFCT);

  /**
   * If available, retrieve the <code>ILcdFilter</code> installed on this layer.
   *
   * @param aLayer The layer from which the <code>ILcdFilter</code> will be retrieved
   *
   * @return The <code>ILcdFilter</code> installed on the provided layer. <code>null</code> if no filter was yet installed.
   */
  ILcdFilter<?> retrieveLayerFilter(ILcdLayer aLayer);

}
