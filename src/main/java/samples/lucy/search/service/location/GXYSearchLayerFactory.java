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

import java.awt.Font;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdTranslatedIcon;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;

/**
 * Layer factory to display search results in a GXY view.
 */
public final class GXYSearchLayerFactory implements ILcdGXYLayerFactory {
  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!AbstractLocationSearchService.isSearchResultModel(aModel)) {
      return null;
    }

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel) {
      @Override
      public boolean isSelectableSupported() {
        return false;
      }

      @Override
      public boolean isEditableSupported() {
        return false;
      }
    };
    layer.setSelectable(false);
    layer.setEditable(false);
    layer.setIcon(TLcdIconFactory.create(TLcdIconFactory.SEARCH_ICON));
    layer.setInteractMargin(64); //support up to fairly large search icons

    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    ILcdIcon icon = TLcdIconFactory.create(TLcdIconFactory.LOCATION_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32);
    painter.setIcon(new TLcdTranslatedIcon(icon, 0, -16));

    layer.setGXYPainterProvider(painter);

    TLcdGXYDataObjectLabelPainter labelPainter = new TLcdGXYDataObjectLabelPainter();
    labelPainter.setHaloEnabled(true);
    labelPainter.setFont(labelPainter.getFont().deriveFont(Font.BOLD, 12f));
    labelPainter.setPositionList(new int[]{TLcdGXYLabelPainter.SOUTH});
    labelPainter.setExpressions(createExpression(SEARCH_RESULT_TYPE, DESCRIPTION_PROPERTY));
    layer.setGXYLabelPainterProvider(labelPainter);

    layer.setLabeled(true);

    return layer;
  }
}
