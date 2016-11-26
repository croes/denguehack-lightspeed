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
package samples.lucy.lightspeed.style.customizablestyle;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.lightspeed.ALcyLspStyleFormat;
import com.luciad.lucy.map.lightspeed.ALcyLspStyleRepository;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.lucy.lightspeed.common.MapComponentUtil;

/**
 * <p><code>ALcyLspStyleFormat</code> extension for SHP models which only contain points.</p>
 *
 * <p>The layers created by this format use a different style for 'big' and 'small' countries.</p>
 *
 */
class SHPFormat extends ALcyLspStyleFormat {

  SHPFormat(String aLongPrefix, String aShortPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv, aLongPrefix, aShortPrefix, aProperties, new ILcdFilter<ILcdModel>() {
      @Override
      public boolean accept(ILcdModel aObject) {
        return isSHPWorldModel(aObject);
      }
    });
  }

  @Override
  protected ILspLayerFactory createLayerFactoryImpl() {
    return new ALspSingleLayerFactory() {
      @Override
      public ILspLayer createLayer(ILcdModel aModel) {
        //create the styler
        CountryCustomizableStyler styler = new CountryCustomizableStyler();

        //use the TLspShapeLayerBuilder to construct the layer
        TLspShapeLayerBuilder builder = TLspShapeLayerBuilder.newBuilder();
        builder.model(aModel).bodyStyler(TLspPaintState.REGULAR, styler);

        //create and set a styler for the selection style
        ALcyLspStyleRepository styleRepository = ALcyLspStyleRepository.getInstance(getLucyEnv());
        builder.bodyStyler(TLspPaintState.SELECTED, styleRepository.createSelectionStyler(styler));

        //make the layer selectable by default and adjust the label
        builder.selectable(true).label("Countries");

        return builder.build();
      }

      @Override
      public boolean canCreateLayers(ILcdModel aModel) {
        //the format is wrapped with a TLcyLspSafeGuardFormatWrapper so we can just return true
        return true;
      }
    };
  }

  /**
   * Returns <code>true</code> when <code>aModel</code> is the SHP model which contains countries.
   *
   * @param aModel The model
   *
   * @return <code>true</code> when <code>aModel</code> is the SHP model which only contains
   *         countries, <code>false</code> otherwise
   */
  private static boolean isSHPWorldModel(ILcdModel aModel) {
    if (aModel != null && aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      return MapComponentUtil.WORLD_MODEL_SOURCE_NAME.equals(aModel.getModelDescriptor().getSourceName());
    }
    return false;
  }
}
