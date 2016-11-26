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
package samples.lightspeed.style.strokedline;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Simple layer factory.
 */
public class LayerFactory extends ALspSingleLayerFactory {

  public LayerFactory() {
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName().equals("ComplexStroke");
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor().getTypeName().equals("ComplexStroke")) {
      return createStrokedShapesLayer(aModel);
    }
    return null;
  }

  private ILspLayer createStrokedShapesLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    ALspStyler regularStyler = new MyStyler();

    layerBuilder.model(aModel)
                .selectable(true)
                .bodyEditable(true)
                .bodyStyler(TLspPaintState.REGULAR, regularStyler);
    return layerBuilder.build();
  }

  private static class MyStyler extends ALspStyler {

    private List<ALspStyle> getStyles(Object aObject) {
      if (aObject instanceof StyledShapeFactory.StyledShape) {
        StyledShapeFactory.StyledShape strokedShape = (StyledShapeFactory.StyledShape) aObject;
        return strokedShape.getStyles();
      } else {
        // Fallback
        return Collections.<ALspStyle>singletonList(TLspLineStyle.newBuilder().build());
      }
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        List<ALspStyle> styles = getStyles(object);
        aStyleCollector.object(object).styles(styles).submit();
      }
    }
  }
}
