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
package samples.lightspeed.internal.symbology.clusterLabeling;

import java.io.IOException;
import java.util.Random;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.util.ILcdFilter;

import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.FitUtil;
import samples.symbology.common.app6.APP6ModelFactory;

/**
 * Extension of the realtime.lightspeed.clusterLabeling sample that shows how it can be integrated with
 * military symbology.
 */
public class SymbologyMainPanel extends samples.realtime.lightspeed.clusterLabeling.MainPanel {

  public static final String SYMBOLOGY_MODEL_TYPE_NAME = "ClusteredSymbols";

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Register a layer factory that is capable of painting military symbols as labels, in order to
    // make it possible to declutter them
    ServiceRegistry.getInstance().register(new SymbologyLayerFactory(getLabelingAlgorithmProvider()));

    for (int i = 0; i < samples.realtime.gxy.clusterLabeling.MainPanel.CLUSTERS.length; i++) {
      TLcdVectorModel trackModel = createSymbologyModel(i);
      getView().addModel(trackModel);
    }

    FitUtil.fitOnBounds(this, new TLcdLonLatBounds(48.00, 48.00, 14.00, 14.00), new TLcdGeodeticReference());
  }

  private TLcdVectorModel createSymbologyModel(int aClusterIndex) {
    Random rand = new Random(271828 * (aClusterIndex + 1));
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new SymbologyModelDescriptor());
    double[] coordinates = samples.realtime.gxy.clusterLabeling.MainPanel.CLUSTERS[aClusterIndex];
    APP6ModelFactory app6ModelFactory = new APP6ModelFactory();
    for (int i = 0; i < 10; i++) {
      double lon = coordinates[0] + rand.nextDouble() / 2.0;
      double lat = coordinates[1] + rand.nextDouble() / 2.0;
      addSymbolToModel(model, createAPP6Symbol(app6ModelFactory, i, lon, lat));
    }
    // Also create a military graphic
    double lon = coordinates[0] + rand.nextDouble() / 2.0;
    double lat = coordinates[1] + rand.nextDouble() / 2.0;
    addSymbolToModel(model, createHelicopterAttack(app6ModelFactory, lon, lat));
    return model;
  }

  protected void addSymbolToModel(TLcdVectorModel aModel, Object aObject) {
    aModel.addElement(aObject, ILcdModel.NO_EVENT);
  }

  private static TLcdEditableAPP6AObject createAPP6Symbol(APP6ModelFactory aApp6ModelFactory, int aI, double aLon, double aLat) {
    TLcdEditableAPP6AObject symbol;
    if (aI % 3 == 0) {
      symbol = aApp6ModelFactory.createAirport(ELcdAPP6Standard.APP_6A);
    } else if (aI % 3 == 1) {
      symbol = aApp6ModelFactory.createAttackRotaryWing(ELcdAPP6Standard.APP_6A);
    } else {
      symbol = aApp6ModelFactory.createLightInfantry(ELcdAPP6Standard.APP_6A);
    }
    symbol.move2D(aLon, aLat);
    symbol.putTextModifier(ILcdAPP6ACoded.sUniqueDesignation, "Ud");
    symbol.putTextModifier(ILcdAPP6ACoded.sAdditionalInformation, "Add");
    return symbol;
  }

  private static TLcdEditableAPP6AObject createHelicopterAttack(APP6ModelFactory aApp6ModelFactory, double aLon, double aLat) {
    TLcdEditableAPP6AObject symbol = aApp6ModelFactory.createHelicopterAttack(ELcdAPP6Standard.APP_6A);
    symbol.translate2D(aLon - symbol.getStartPoint().getX(), aLat - symbol.getStartPoint().getY());
    symbol.putTextModifier(ILcdAPP6ACoded.sUniqueDesignation, "Ud");
    return symbol;
  }

  @Override
  protected ILcdFilter<ILcdModel> createModelFilter() {
    // Make sure the DeclutterController accepts our symbology models
    return new ILcdFilter<ILcdModel>() {
      @Override
      public boolean accept(ILcdModel aModel) {
        return SYMBOLOGY_MODEL_TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName());
      }
    };
  }

  private static class SymbologyModelDescriptor extends TLcdModelDescriptor {
    public SymbologyModelDescriptor() {
      super("Generated", SYMBOLOGY_MODEL_TYPE_NAME, "Symbology");
    }
  }

}
