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
package samples.lightspeed.grid;

import java.util.Collection;
import java.util.Collections;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridStyler;

class LonLatGridLayerFactory implements ILspLayerFactory {

  enum Spacing {
    FINE("Fine", 1.0 / 3.0),
    DEFAULT("Default", 1.0),
    COARSE("Coarse", 3.0);

    private final double fModifier;
    private final String fName;

    Spacing(String aName, double aModifier) {
      fModifier = aModifier;
      fName = aName;
    }

    private double getModifier() {
      return fModifier;
    }

    @Override
    public String toString() {
      return fName;
    }
  }

  private final double fSpacingMultiplier;
  private final TLspLonLatGridStyler fStyler;

  LonLatGridLayerFactory(Spacing aSpacing, TLspLonLatGridStyler aStyler) {
    fSpacingMultiplier = aSpacing.getModifier();
    fStyler = aStyler;
  }

  public static ILcdModel createLonLatGridModel() {
    return TLspLonLatGridLayerBuilder.createModel("Lon Lat Grid");
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    try {
      // Check if the grid layer builder accepts the given model
      TLspLonLatGridLayerBuilder.newBuilder().model(aModel);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    ILspLayer layer = TLspLonLatGridLayerBuilder.newBuilder()
                                                .bodyStyler(TLspPaintState.REGULAR, fStyler)
                                                .labelStyler(TLspPaintState.REGULAR, fStyler)
                                                .spacingRange(0.048, fSpacingMultiplier * 1 / 60.0)   // zoomed in
                                                .spacingRange(0.024, fSpacingMultiplier * 1 / 12.0)
                                                .spacingRange(0.012, fSpacingMultiplier * 1 / 6.0)
                                                .spacingRange(0.006, fSpacingMultiplier * 1 / 2.0)
                                                .spacingRange(0.0012, fSpacingMultiplier * 1)
                                                .spacingRange(0.000336, fSpacingMultiplier * 5)
                                                .spacingRange(0.0000672, fSpacingMultiplier * 10)
                                                .spacingRange(0, fSpacingMultiplier * 30)        // zoomed out
                                                .label("Test Test")
                                                .build();
    return Collections.singletonList(layer);
  }
}
