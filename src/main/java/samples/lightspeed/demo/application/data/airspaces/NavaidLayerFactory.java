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
package samples.lightspeed.demo.application.data.airspaces;

import java.awt.Color;
import java.util.Collection;

import com.luciad.ais.symbology.icao.TLcdICAODefaultSymbolProvider;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdHaloIcon;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdInterval;
import com.luciad.util.collections.ILcdMultiKeyCache;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Layer factory that creates layers for navaids.
 */
public class NavaidLayerFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    return TLspShapeLayerBuilder.newBuilder(ILspLayer.LayerType.BACKGROUND)
                                .model(aModel)
                                .selectable(false)
                                .bodyStyler(
                                    TLspPaintState.REGULAR,
                                    new NavaidStyler()
                                ).bodyScaleRange(new TLcdInterval(1e-3, Double.MAX_VALUE))
                                .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof NavaidModelDescriptor;
  }

  private static class NavaidStyler extends ALspStyler {
    private TLcdICAODefaultSymbolProvider fProvider;

    private NavaidStyler() {
      fProvider = new TLcdICAODefaultSymbolProvider();
      fProvider.setColor(new Color(255, 192, 0));
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      ILcdMultiKeyCache mkc = aContext.getView().getServices().getMultiKeyCache().getOrCreateBranch(
          new Object[]{this, aContext.getViewXYZWorldTransformation()}
      );
      boolean is3D = aContext.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D;

      for (Object o : aObjects) {
        ALspStyle style = (ALspStyle) mkc.get(o.getClass());

        if (style == null) {
          ILcdIcon icon = new TLcdHaloIcon(fProvider.getIcon(o), Color.black, 1);
          style = TLspIconStyle.newBuilder()
                               .icon(icon)
                               .offset(0, is3D ? -icon.getIconHeight() / 2.0 : 0)
                               .build();
          mkc.put(o.getClass(), style);
        }

        if (is3D) {
          ILcdPoint pIn = (ILcdPoint) o;
          ILcdHeightProvider hp = aContext.getView().getServices()
                                          .getTerrainSupport().getViewDependentHeightProvider(
                  (ILcdGeoReference) aContext.getModelReference(),
                  false
              );
          TLcdLonLatHeightPoint pOut = new TLcdLonLatHeightPoint(
              pIn.getX(),
              pIn.getY(),
              hp.retrieveHeightAt(pIn)
          );

          aStyleCollector
              .object(o)
              .geometry(pOut)
              .styles(style)
              .submit();
        } else {
          aStyleCollector
              .object(o)
              .styles(style)
              .submit();
        }
      }
    }
  }
}


