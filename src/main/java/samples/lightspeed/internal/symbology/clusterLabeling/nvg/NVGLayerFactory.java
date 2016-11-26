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

import com.luciad.format.nvg.lightspeed.TLspNVGLayerBuilder;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Point;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.internal.symbology.clusterLabeling.SymbologyLayerFactory;
import samples.realtime.lightspeed.clusterLabeling.AnimatedDeclutterLabelingAlgorithmProvider;

/**
 * Layer factory that makes sure that the bodies of NVG objects are painted as a label.
 */
public class NVGLayerFactory extends SymbologyLayerFactory {

  private static int sLayerCount = 0;

  public NVGLayerFactory(AnimatedDeclutterLabelingAlgorithmProvider aLabelingAlgorithmProvider) {
    super(aLabelingAlgorithmProvider);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    String label = aModel.getModelDescriptor().getDisplayName() + " " + sLayerCount++;
    ILspInteractivePaintableLayer layer = TLspNVGLayerBuilder.newBuilder()
                                                             .label(label)
                                                             .model(aModel)
                                                             .build();

    wrapStylers(layer, new PointSymbologyFilter() {
      @Override
      public boolean accept(Object aObject) {
        if (aObject instanceof TLcdNVG20Point) {
          return true;
        }
        return super.accept(aObject);
      }
    });
    return layer;
  }
}
