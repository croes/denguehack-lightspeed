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
package samples.lightspeed.demo.application.data.milsym;

import java.awt.Color;
import java.util.Collection;

import com.luciad.model.ILcdModel;
import com.luciad.util.ALcdDynamicFilter;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.application.data.milsym.SymbolShapeModelFactory.SymbolShape;

class SymbolShapeLayerFactory {

  public ILspLayer createLayer(ILcdModel aModel, MilSymFilter aMilSymFilter) {
    return TLspShapeLayerBuilder.newBuilder()
                                .bodyStyler(TLspPaintState.REGULAR, new SymbolShapeStyler())
                                .model(aModel)
                                .bodyEditable(true)
                                .filter(new SymbolShapeFilter(aMilSymFilter))
                                .build();
  }


  private static class SymbolShapeStyler extends ALspStyler {

    private static final TLspLineStyle LINE_STYLE = TLspLineStyle.newBuilder()
                                                                 .color(new Color(1.0f, 0.78039217f, 0.7921569f, 1f))
                                                                 .build();
    private static final TLspFillStyle FILL_STYLE = TLspFillStyle.newBuilder()
                                                                 .color(new Color(0.973f, 0.514f, 0.525f, 0.31f))
                                                                 .build();

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        if (object instanceof SymbolShape) {
          SymbolShape symbolShape = (SymbolShape) object;
          aStyleCollector.object(symbolShape)
                         .geometry(symbolShape.getShape())
                         .styles(LINE_STYLE,
                                 FILL_STYLE)
                         .submit();
        }
      }
    }

  }

  private static class SymbolShapeFilter extends ALcdDynamicFilter<SymbolShape> implements ILcdChangeListener {

    private final MilSymFilter fMilSymFilter;

    public SymbolShapeFilter(MilSymFilter aMilSymFilter) {
      fMilSymFilter = aMilSymFilter;
      fMilSymFilter.addChangeListener(this);
    }

    @Override
    public boolean accept(SymbolShape aSymbolShape) {
      return fMilSymFilter.accept(aSymbolShape.getAssociatedSymbol());
    }

    @Override
    public void stateChanged(TLcdChangeEvent aChangeEvent) {
      fireChangeEvent();
    }

  }

}
