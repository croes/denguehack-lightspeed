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
package samples.lightspeed.internal.decoder;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;

import com.luciad.format.dwg.ILcdDWGShape;
import com.luciad.format.dwg.TLcdDWGModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * @author tomn
 * @since 2013.0
 */
public class DWGLayerFactory extends ALspSingleLayerFactory {

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLcdDWGModelDescriptor desc = (TLcdDWGModelDescriptor) aModel.getModelDescriptor();

    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .minimumObjectSizeForPainting(0)
                                .objectViewMargin(10)
                                .bodyStyler(TLspPaintState.REGULAR, new DWGStyler(desc))
                                .build();
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdDWGModelDescriptor;
  }

  private class DWGStyler extends ALspStyler {

    private TLcdDWGModelDescriptor fDesc;
    private HashMap<Color, HashMap<Integer, TLspLineStyle>> fLineStyles = new HashMap<Color, HashMap<Integer, TLspLineStyle>>();
    private HashMap<Color, TLspFillStyle> fFillStyles = new HashMap<Color, TLspFillStyle>();

    public DWGStyler(TLcdDWGModelDescriptor aDesc) {
      fDesc = aDesc;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object o : aObjects) {
        if (o instanceof ILcdDWGShape) {
          ILcdDWGShape shape = (ILcdDWGShape) o;

          Color fillColor = shape.getDisplayInfo().getLayer().getColorValue();
          TLspFillStyle fill = fFillStyles.get(fillColor);
          if (fill == null) {
            fill = TLspFillStyle.newBuilder()
                                .color(fillColor)
                                .opacity(shape.getDisplayInfo().getColorTransparency() / 255f)
                                .build();
            fFillStyles.put(fillColor, fill);
          }

          Color color = fillColor;
          int weight = shape.getDisplayInfo().getLayer().getLineWeight();
          System.out.println("weight = " + weight);
          HashMap<Integer, TLspLineStyle> wmap = fLineStyles.get(color);
          if (wmap == null) {
            wmap = new HashMap<Integer, TLspLineStyle>();
            fLineStyles.put(color, wmap);
          }
          TLspLineStyle line = wmap.get(weight);
          if (line == null) {
            line = TLspLineStyle.newBuilder()
                                .color(color)
                                .opacity(1f)
                                .width(weight / 8f)
                                .build();
            wmap.put(weight, line);
          }

          aStyleCollector.object(shape).styles( /*fill, */line).submit();
        }
      }
    }
  }
}
