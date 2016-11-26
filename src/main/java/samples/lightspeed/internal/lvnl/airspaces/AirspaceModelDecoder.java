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
package samples.lightspeed.internal.lvnl.airspaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import com.luciad.ais.model.TLcdAISDataObjectFactory;
import com.luciad.ais.model.airspace.ILcdAirspaceFeature;
import com.luciad.ais.model.airspace.TLcdAirspaceModelDescriptor;
import com.luciad.ais.model.airspace.TLcdFeaturedAirspace;
import com.luciad.format.arinc.decoder.TLcdARINCControlledAirspaceHandler;
import com.luciad.format.arinc.decoder.TLcdARINCDecoder;
import com.luciad.format.arinc.model.airspace.ILcdARINCAirspaceFeature;
import com.luciad.format.dafif.decoder.TLcdDAFIFAirspaceDecoder;
import com.luciad.format.dafif.util.ILcdDAFIFModelFilter;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdFeatureIndexedAnd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFeaturedDescriptor;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdConstant;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * @author tomn
 * @since 2012.0
 */
public class AirspaceModelDecoder implements ILcdModelDecoder {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(AirspaceModelDecoder.class);

  @Override
  public String getDisplayName() {
    return null;
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return false;
  }

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    TLcdAISDataObjectFactory fDomainFactory = new TLcdAISDataObjectFactory();
    TLcdDAFIFAirspaceDecoder fDAFIFAirspaceDecoder = new TLcdDAFIFAirspaceDecoder(fDomainFactory);

    TLcd2DBoundsIndexedModel extruded_airspacemodel = null;

    String dafif_source = "Data/internal.data/lvnl/dafif/EH";
    final String[] fDafifAirspaceNames = new String[]{
        "HEBRIDES UTA",
        "REYKJAVIK OCA SCTR B",
        "REYKJAVIK CTA DOMESTIC SCTR",
        "NORWAY CTA",
        "CANADIAN (RVSM) AIRSPACE",
    };

    try {
      TLcdARINCDecoder decoder = new TLcdARINCDecoder();
      decoder.addHandlerForTypeToBeDecoded(new TLcdARINCControlledAirspaceHandler());

      // Read ARINC airspace data.
      ILcdFeatureIndexedAnd2DBoundsIndexedModel airspace_model = (ILcdFeatureIndexedAnd2DBoundsIndexedModel) decoder.decode(
          "Data/internal.data/lvnl/arinc/arinc_testdata.txt"
      );

      // Create extruded model.
      AirspaceFilter airspace_filter = new AirspaceFilter();
      TLcdGeodeticReference reference = new TLcdGeodeticReference(new TLcdGeodeticDatum());
      TLcdAirspaceModelDescriptor model_descriptor = (TLcdAirspaceModelDescriptor) airspace_model.getModelDescriptor();

      extruded_airspacemodel = new TLcd2DBoundsIndexedModel();
      extruded_airspacemodel.setModelDescriptor(model_descriptor);
      extruded_airspacemodel.setModelReference(reference);

      // Filter airspaces.
      List unique_index = Arrays
          .asList(ILcdARINCAirspaceFeature.AIRSPACE_CENTER, ILcdARINCAirspaceFeature.NAME);
      airspace_model.addIndex(unique_index, true);
      String[] list = new String[]{
//          "EHAM,SCHIPOL CTR",
          "EHSB,SOESTERBERG CTR",
          "EHRD,ROTTERDAM CTR",
          "EHVB,VALKENBERG CTR",
          "EHAM,AMSTERDAM CTA WEST",
          "EHAM,AMSTERDAM CTA EAST",
          "EHAM,AMSTERDAM CTA SOUTH",
          "EHSB,SOESTERBERG TMA",
          "EHDL,NIEUW MILLIGEN TMA AREA B",
      };
      for (int i = 0; i < list.length; i++) {
        StringTokenizer tokenizer = new StringTokenizer(list[i], ",");
        ArrayList unique_values = new ArrayList();
        for (int j = 0; j < unique_index.size(); j++) {
          unique_values.add(tokenizer.nextToken());
        }

        double lower_limit = 0;
        double upper_limit = 0;

        TLcdFeaturedAirspace featured_airspace = (TLcdFeaturedAirspace) airspace_model.retrieveByUniqueIndex(unique_index, unique_values);

        Object lower_limit_feature = featured_airspace.getFeature(model_descriptor.getFeatureIndex(ILcdAirspaceFeature.LOWER_LIMIT));
        Object upper_limit_feature = featured_airspace.getFeature(model_descriptor.getFeatureIndex(ILcdAirspaceFeature.UPPER_LIMIT));
        if (lower_limit_feature != null) {
          lower_limit = Double.parseDouble(lower_limit_feature.toString());
        }
        if (lower_limit_feature != null) {
          upper_limit = Double.parseDouble(upper_limit_feature.toString());
        }
        ExtrudedAirspace extruded_airspace = new ExtrudedAirspace(featured_airspace, lower_limit / TLcdConstant.FT2MTR_STD, upper_limit / TLcdConstant.FT2MTR_STD, ExtrudedAirspace.ARINC);
        extruded_airspacemodel.addElement(extruded_airspace, ILcdFireEventMode.NO_EVENT);
      }
      //Now add also DAFIF airspaces
      fDAFIFAirspaceDecoder.setDAFIFModelFilter(new ILcdDAFIFModelFilter() {
        public void setModelDescriptor(ILcdModelDescriptor iLcdModelDescriptor) {
          //do nothing here
        }

        public boolean accept(Object o) {
          //check on the names from the config file
          if (o instanceof TLcdFeaturedAirspace) {
            for (int i = 0; i < fDafifAirspaceNames.length; i++) {
              if (fDafifAirspaceNames[i].equalsIgnoreCase((String) ((TLcdFeaturedAirspace) o).getFeature(2))) {
                return true;
              }
            }
            return false;
          } else {
            return false;
          }
        }
      });
      ILcdModel dafif_model = fDAFIFAirspaceDecoder.decode(dafif_source);
      ILcdFeaturedDescriptor fDafifModelDescriptor = (ILcdFeaturedDescriptor) dafif_model.getModelDescriptor();
      //Add as extra airspaces
      Enumeration elements = dafif_model.elements();
      while (elements.hasMoreElements()) {
        TLcdFeaturedAirspace featured_airspace = (TLcdFeaturedAirspace) elements.nextElement();
        Object lower_limit_feature = featured_airspace.getFeature(fDafifModelDescriptor.getFeatureIndex(ILcdAirspaceFeature.LOWER_LIMIT));
        Object upper_limit_feature = featured_airspace.getFeature(fDafifModelDescriptor.getFeatureIndex(ILcdAirspaceFeature.UPPER_LIMIT));
        double lower_limit = 0.0;
        double upper_limit = 0.0;
        if (lower_limit_feature != null) {
          lower_limit = Double.parseDouble(lower_limit_feature.toString());
        }
        if (lower_limit_feature != null) {
          upper_limit = Double.parseDouble(upper_limit_feature.toString());
        }
        ExtrudedAirspace extruded_airspace = new ExtrudedAirspace(featured_airspace, lower_limit * 100, upper_limit * 100, ExtrudedAirspace.DAFIF);
        extruded_airspacemodel.addElement(extruded_airspace, ILcdFireEventMode.NO_EVENT);
      }

      sLogger.debug("Loaded airspace data.");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return extruded_airspacemodel;
  }
}
