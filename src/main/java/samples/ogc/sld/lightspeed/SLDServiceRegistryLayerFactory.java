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
package samples.ogc.sld.lightspeed;

import samples.ogc.sld.lightspeed.SLDFileLayerFactory;
import com.luciad.ogc.sld.xml.TLcdSLDFeatureTypeStyleDecoder;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

import samples.common.serviceregistry.ServiceLoaderRegistry;

/**
 * Layer factory that creates SLD styled layers if the data source file is accompanied by an SLD (.sld) file.
 * <p/>
 * For {@code ILcdModelTreeNode} models, an {@code ILcdLayerTreeNode} is created, with a sub-layer
 * for every child model, created using the layer factories found in the service registry.
 *
 * @since 2013.0
 * @see TLcdSLDFeatureTypeStyleDecoder
 * @see ServiceLoaderRegistry
 */
@LcdService(service = ILspLayerFactory.class, priority = LcdService.FALLBACK_PRIORITY - 1)
public class SLDServiceRegistryLayerFactory extends SLDFileLayerFactory {

  /**
   * Creates a new factory using the layer factories in the default service registry for model tree nodes.
   */
  public SLDServiceRegistryLayerFactory() {
    super(ServiceLoaderRegistry.getInstance().query(ILspLayerFactory.class));
  }
}
