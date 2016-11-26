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
package samples.lucy.search.service.location;

import static com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage.createExpression;
import static samples.lucy.search.service.location.SearchResultModelDescriptor.DESCRIPTION_PROPERTY;
import static samples.lucy.search.service.location.SearchResultModelDescriptor.SEARCH_RESULT_TYPE;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

/**
 * Layer factory to display search results in a Lightspeed view.
 */
public final class LSPSearchLayerFactory extends ALspSingleLayerFactory {
  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return AbstractLocationSearchService.isSearchResultModel(aModel);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspLabelStyler southOnlyLabelStyler =
        TLspLabelStyler.newBuilder()
                       .locations(TLspLabelLocationProvider.Location.SOUTH)
                       .styles(TLspTextStyle.newBuilder().build(),
                               TLspDataObjectLabelTextProviderStyle
                                   .newBuilder()
                                   .expressions(createExpression(SEARCH_RESULT_TYPE, DESCRIPTION_PROPERTY))
                                   .build())
                       .build();

    ILspInteractivePaintableLayer layer =
        TLspShapeLayerBuilder.newBuilder()
                             .model(aModel)
                             .bodyStyles(TLspPaintState.REGULAR,
                                         TLspIconStyle.newBuilder()
                                                      .icon(TLcdIconFactory.create(TLcdIconFactory.LOCATION_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32))
                                                      .offset(0, -16)
                                                      .build(),
                                         TLspViewDisplacementStyle.newBuilder().build())
                             .labelStyler(TLspPaintState.REGULAR, southOnlyLabelStyler)
                             .bodyEditable(false)
                             .editableSupported(false)
                             .selectableSupported(false)
                             .selectable(false)
                             .icon(TLcdIconFactory.create(TLcdIconFactory.SEARCH_ICON))
                             .build();
    layer.setVisible(TLspPaintRepresentation.LABEL, true);
    return layer;
  }
}
