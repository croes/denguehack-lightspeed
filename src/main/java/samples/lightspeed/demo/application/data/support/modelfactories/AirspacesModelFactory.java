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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.luciad.ais.model.airspace.ILcdAirspaceFeature;
import com.luciad.ais.model.airspace.TLcdFeaturedAirspace;
import com.luciad.format.arinc.decoder.TLcdARINCControlledAirspaceHandler;
import com.luciad.format.arinc.decoder.TLcdARINCDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.ILcdFeaturedDescriptor;
import com.luciad.util.ILcdFireEventMode;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Loads a Dafif2 model.
 */
public class AirspacesModelFactory extends AbstractModelFactory {

  private List<String> fAcceptedAerodromes;
  private List<String> fAcceptedRegions;

  public AirspacesModelFactory(String aType) {
    super(aType);
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);

    Object o = aProperties.get("arinc.filter.aerodromes");
    if (o != null) {
      fAcceptedAerodromes = new ArrayList<String>();
      Collections.addAll(fAcceptedAerodromes, o.toString().split(","));
    }

    o = aProperties.get("arinc.filter.regions");
    if (o != null) {
      fAcceptedRegions = new ArrayList<String>();
      Collections.addAll(fAcceptedRegions, o.toString().split(","));
    }
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {

    TLcdARINCDecoder decoder = new TLcdARINCDecoder();
    decoder.addHandlerForTypeToBeDecoded(new TLcdARINCControlledAirspaceHandler());

    ILcdModel result = decoder.decode(aSource);

    TLcdVectorModel model = new TLcdVectorModel(result.getModelReference(), result.getModelDescriptor());

    Enumeration e = result.elements();
    while (e.hasMoreElements()) {
      model.addElement(convertToExtrudedShape(e.nextElement(), result), ILcdFireEventMode.NO_EVENT);
    }
    return model;
  }

  private ILcdShape convertToExtrudedShape(Object aObject, ILcdModel aModel) {

    // First check if it's an airspace that we're converting
    if (aObject instanceof TLcdFeaturedAirspace) {
      // Extruded shape was not cached, build new one and put it in cache
      TLcdFeaturedAirspace airspace = (TLcdFeaturedAirspace) aObject;
      ILcdFeaturedDescriptor descriptor = (ILcdFeaturedDescriptor) aModel.getModelDescriptor();
      int minIndex = descriptor.getFeatureIndex(ILcdAirspaceFeature.LOWER_LIMIT);
      int maxIndex = descriptor.getFeatureIndex(ILcdAirspaceFeature.UPPER_LIMIT);

      // Calculate minimum height of extruded shape
      Float minF = (Float) airspace.getFeature(minIndex);
      if (minF == null) {
        minF = 0.0f;
      }

      // 5 m offset.
      minF = Math.max(minF, 100);

      // Calculate maximum height of extruded shape
      Float maxF = (Float) airspace.getFeature(maxIndex);
      if (maxF == null) {
        maxF = 1e4f;
      }

      // Create new extruded shape and store it in cache for further use
      return new TLcdExtrudedShape((ILcdShape) aObject, minF, maxF);
    } else {
      return null;
    }
  }
}
