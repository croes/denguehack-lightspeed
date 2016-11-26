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
package samples.lightspeed.vertical;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;

import samples.gxy.vertical.Flight;

public class FlightModelFactory {

  public static final String FLIGHTS = "Flights";

  public static ILcdModel createFlightModel() {

    TLcdLonLatHeightPoint[] flightPoints1 = {
        new TLcdLonLatHeightPoint(11.2040, 48.4419, 650),
        new TLcdLonLatHeightPoint(12.2080, 48.5618, 950),
        new TLcdLonLatHeightPoint(13.2080, 48.6818, 1050),
        new TLcdLonLatHeightPoint(14.3280, 48.5818, 1050),
        new TLcdLonLatHeightPoint(16.3280, 48.5818, 1000)
    };
    TLcd3DEditablePointList flightPointList1 = new TLcd3DEditablePointList(flightPoints1, false);

    int[] routeMinAltitude1 = {600, 900, 1000, 1000};
    int[] routeMaxAltitude1 = {1250, 1100, 1100, 1400};

    TLcdLonLatHeightPoint[] flightPoints2 = {
        new TLcdLonLatHeightPoint(5.4131, 48.0007, 450),
        new TLcdLonLatHeightPoint(6.1744, 47.4118, 800),
        new TLcdLonLatHeightPoint(6.3951, 47.3750, 900),
        new TLcdLonLatHeightPoint(7.2753, 47.2919, 1000),
        new TLcdLonLatHeightPoint(7.3956, 47.2800, 900),
        new TLcdLonLatHeightPoint(8.1143, 47.2880, 1000)
    };
    TLcd3DEditablePointList flightPointList2 = new TLcd3DEditablePointList(flightPoints2, false);

    int[] routeMinAltitude2 = {400, 700, 850, 850, 700};
    int[] routeMaxAltitude2 = {900, 1000, 1250, 1150, 1100};

    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor("FlightModel", FLIGHTS, "Flights"));
    model.addElement(new Flight(flightPointList1, datum.getEllipsoid(), routeMinAltitude1, routeMaxAltitude1), ILcdFireEventMode.NO_EVENT);
    model.addElement(new Flight(flightPointList2, datum.getEllipsoid(), routeMinAltitude2, routeMaxAltitude2), ILcdFireEventMode.NO_EVENT);
    return model;
  }
}
