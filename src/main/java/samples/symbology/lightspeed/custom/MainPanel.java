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
package samples.symbology.lightspeed.custom;

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bLayerBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;

import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;

/**
 * Sample demonstrating how to customize the visualization of military symbols and implement new
 * military symbols in a Lightspeed view.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addData() throws IOException {
    super.addData();

    ILcdModel symbolModel = createSymbolModel();
    ILspLayer layer = TLspMS2525bLayerBuilder.newBuilder()
                                             .model(symbolModel)
                                             .bodyStyler(TLspPaintState.REGULAR, new SymbolBodyStyler(false))
                                             .labelStyler(TLspPaintState.REGULAR, new SymbolLabelStyler(false))
                                             .bodyStyler(TLspPaintState.SELECTED, new SymbolBodyStyler(true))
                                             .labelStyler(TLspPaintState.SELECTED, new SymbolLabelStyler(true))
                                             .build();
    getView().addLayer(layer);
    FitUtil.fitOnLayers(this, layer);
  }

  private ILcdModel createSymbolModel() {
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel(
        new TLcdGeodeticReference(),
        new TLcdModelDescriptor("", "", "Military Symbols")
    );

    // Add some tactical symbols
    String[] tacticalCodes = new String[]{
        "GFTPAS------BEX",
        "G*M*OAR-----GMX",
        "WO-DMPA----L---",
        "GFGPOLKA----UKX",
        "G*MASL------SPX",
        /**
         * This is a custom symbol code format, which is detected by SymbolStyler
         * and handled as a special case.
         */
        "C|UAV|F|10e3|NL",
    };
    for (int i = 0; i < tacticalCodes.length; i++) {
      TLcdLonLatPolyline geometry = new TLcdLonLatPolyline();
      geometry.insert2DPoint(0, 4, 50 - i);
      geometry.insert2DPoint(1, 5, 51 - i);
      geometry.insert2DPoint(2, 6, 50.5 - i);
      geometry.insert2DPoint(3, 7, 51 - i);
      Symbol symbol = new Symbol(tacticalCodes[i], geometry);
      model.addElement(symbol, ILcdModel.NO_EVENT);
    }

    // Add some point symbols
    addPointSymbol(model, "SAFAG-----MTFRX", 9, 50, Double.NaN);
    addPointSymbol(model, "SFAPMF------SWX", 9, 49, 120);
    addPointSymbol(model, "SFGPUC----M*USX", 9, 48, Double.NaN);
    addPointSymbol(model, "SFGPUSXHT---TRX", 9, 47, Double.NaN);
    addPointSymbol(model, "IFGPSRD-----BRX", 9, 46, Double.NaN);

    return model;
  }

  private void addPointSymbol(ILcdModel aModel, String aCode, double aX, double aY, double aMovementDirection) {
    TLcdLonLatPoint geometry = new TLcdLonLatPoint(aX, aY);
    Symbol symbol = new Symbol(aCode, geometry);
    if (!Double.isNaN(aMovementDirection)) {
      symbol.getModifiers().put(ILcdMS2525bCoded.sMovementDirection, Double.toString(aMovementDirection));
    }
    aModel.addElement(symbol, ILcdModel.NO_EVENT);
  }

  public static void main(String[] args) {
    startSample(MainPanel.class, "Customized military symbology");
  }
}
