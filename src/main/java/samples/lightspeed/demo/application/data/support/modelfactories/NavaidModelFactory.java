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

import com.luciad.ais.model.TLcd2DBoundsIndexedModelList;
import com.luciad.ais.model.navaid.ILcdWayPoint;
import com.luciad.format.arinc.decoder.TLcdARINCDMEHandler;
import com.luciad.format.arinc.decoder.TLcdARINCDecoder;
import com.luciad.format.arinc.decoder.TLcdARINCNDBHandler;
import com.luciad.format.arinc.decoder.TLcdARINCTACANHandler;
import com.luciad.format.arinc.decoder.TLcdARINCVORHandler;
import com.luciad.format.arinc.decoder.TLcdARINCWaypointHandler;
import com.luciad.format.arinc.model.navaid.ILcdARINCNavaidFeature;
import com.luciad.format.arinc.model.navaid.ILcdARINCWayPointFeature;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFeatured;
import com.luciad.util.ILcdFeaturedDescriptor;

import samples.lightspeed.demo.application.data.airspaces.NavaidModelDescriptor;
import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Loads an Arinc model.
 */
public class NavaidModelFactory extends AbstractModelFactory {

  private List<String> fAcceptedAerodromes;
  private List<String> fAcceptedRegions;
  private TLcdLonLatBounds fWaypointAOI;

  public NavaidModelFactory(String aType) {
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
//    fAcceptedAerodromes.add( "" );

    o = aProperties.get("arinc.filter.regions");
    if (o != null) {
      fAcceptedRegions = new ArrayList<String>();
      Collections.addAll(fAcceptedRegions, o.toString().split(","));
    }
//    fAcceptedRegions.add( "" );

    String aoi = aProperties.getProperty("arinc.waypoint.aoi");
    if (aoi != null) {
      String[] coords = aoi.split(",");
      fWaypointAOI = new TLcdLonLatBounds(
          Double.parseDouble(coords[0]),
          Double.parseDouble(coords[1]),
          Double.parseDouble(coords[2]),
          Double.parseDouble(coords[3])
      );
    } else {
      fWaypointAOI = new TLcdLonLatBounds(-180, -90, 360, 180);
    }
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    TLcdARINCDecoder decoder = new TLcdARINCDecoder();
    decoder.addHandlerForTypeToBeDecoded(new TLcdARINCVORHandler());
    decoder.addHandlerForTypeToBeDecoded(new TLcdARINCTACANHandler());
    decoder.addHandlerForTypeToBeDecoded(new TLcdARINCDMEHandler());
    decoder.addHandlerForTypeToBeDecoded(new TLcdARINCNDBHandler());
    decoder.addHandlerForTypeToBeDecoded(new TLcdARINCWaypointHandler());

    TLcd2DBoundsIndexedModelList modelList = (TLcd2DBoundsIndexedModelList) decoder.decode(aSource);
    TLcd2DBoundsIndexedModel merge = new TLcd2DBoundsIndexedModel(
        modelList.getModelReference(),
        new NavaidModelDescriptor()
    );

    for (int i = 0; i < modelList.getModelCount(); i++) {
      ILcdModel model = modelList.getModel(i);
      ILcdFeaturedDescriptor descriptor = (ILcdFeaturedDescriptor) model.getModelDescriptor();

      Enumeration navaids = model.elements();
      while (navaids.hasMoreElements()) {
        ILcdFeatured o = (ILcdFeatured) navaids.nextElement();

        int iregion;
        if (o instanceof ILcdWayPoint) {
          iregion = descriptor.getFeatureIndex(ILcdARINCWayPointFeature.ICAO_REGION);

          ILcdPoint p = (ILcdPoint) o;
          if (!fWaypointAOI.contains2D(p)) {
            continue;
          }
        } else {
          iregion = descriptor.getFeatureIndex(ILcdARINCNavaidFeature.ICAO_REGION);
        }
        String region = iregion >= 0 ? "" + o.getFeature(iregion) : "";

        if ((fAcceptedRegions != null) && fAcceptedRegions.contains(region)) {
          merge.addElement(o, ILcdModel.NO_EVENT);
        }
      }
    }

    return merge;
  }

}
