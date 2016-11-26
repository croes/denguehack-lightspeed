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
package samples.lightspeed.internal.symbology.clusterLabeling.nvg;

import java.io.IOException;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20MultiPoint;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Point;
import com.luciad.model.TLcdVectorModel;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;

import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.internal.symbology.clusterLabeling.SymbologyMainPanel;

/**
 * Extension of the realtime.lightspeed.clusterLabeling sample that shows how it can be integrated with NVG models.
 */
public class MainPanel extends SymbologyMainPanel {

  @Override
  protected void addData() throws IOException {
    // Register a layer factory that is capable of painting NVG symbols as labels, in order to
    // make it possible to declutter them
    ServiceRegistry.getInstance().register(new NVGLayerFactory(getLabelingAlgorithmProvider()), ServiceRegistry.HIGH_PRIORITY);
    super.addData();
  }

  @Override
  protected void addSymbolToModel(TLcdVectorModel aModel, Object aObject) {
    // Convert the app6 object to a NVG symbol and add it to the model
    if (aObject instanceof TLcdEditableAPP6AObject) {
      TLcdEditableAPP6AObject object = (TLcdEditableAPP6AObject) aObject;
      aObject = toNVGSymbol(object);
    }
    super.addSymbolToModel(aModel, aObject);
  }

  private static TLcdNVG20Content toNVGSymbol(TLcdEditableAPP6AObject aSymbol) {
    if (aSymbol.isLine()) {
      TLcdNVG20MultiPoint nvgSymbol = new TLcdNVG20MultiPoint();
      nvgSymbol.setSymbolFromAPP6(aSymbol);
      nvgSymbol.setPoints(aSymbol.get2DEditablePointList());
      return nvgSymbol;
    } else {
      TLcdNVG20Point nvgSymbol = new TLcdNVG20Point();
      nvgSymbol.setSymbolFromAPP6(aSymbol);
      nvgSymbol.setX(aSymbol.getX(0));
      nvgSymbol.setY(aSymbol.getY(0));
      nvgSymbol.setZ(aSymbol.getZ(0));
      return nvgSymbol;
    }
  }

}
