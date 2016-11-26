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
package samples.lightspeed.demo.application.data.support.layerfactories;

import java.util.Collection;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridLayerBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridStyle;

import samples.lightspeed.demo.application.data.support.modelfactories.DummyModelFactory;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.grid.MGRSGridLayerFactory;

/**
 * Layer factory that creates MGRS grids.
 */
public class MGRSLayerFactory extends AbstractLayerFactory {

  private final MGRSGridLayerFactory fDelegateGridLayerFactory = new MGRSGridLayerFactory(false, true, TLspMGRSGridStyle.Orientation.ALONG_LINE, 6.0, 3.0, null);

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return DummyModelFactory.isDummyModel(aModel);
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    ILcdModel mgrsModel = TLspMGRSGridLayerBuilder.createModel("MGRS Grid", TLcdEllipsoid.DEFAULT);
    return fDelegateGridLayerFactory.createLayers(mgrsModel);
  }
}
