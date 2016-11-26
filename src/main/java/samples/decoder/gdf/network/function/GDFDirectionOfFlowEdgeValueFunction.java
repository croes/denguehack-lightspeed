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
package samples.decoder.gdf.network.function;

import com.luciad.format.gdf.ILcdGDFLineFeature;
import com.luciad.network.function.ILcdEdgeValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.network.graph.route.ILcdRoute;

/**
 * This sample function checks whether traffic is allowed or not on a given road
 * (GDF Line Feature). If allowed, 0 is returned, otherwise,
 * Double.POSITIVE_INFINITY is returned. It uses the value of the 'Direction of
 * Traffic Flow' attribute (attribute type code 'DF'), which is specified in the
 * GDF specification as follows:
 * <p/>
 * <p/>
 * <ul>
 * <li>1 = Allowed in both directions</li>
 * <li>2 = Closed in positive direction, and open in negative direction</li>
 * <li>3 = Closed in negative direction, and open in positive direction</li>
 * <li>4 = Closed in both directions</li>
 * </ul>
 * <p/>
 * In this sample function, only the DF attributes with a restrictive
 * sub-attribute 'Vehicle Type' (attribute type code 'VT') with one of the
 * following values:
 * <p/>
 * <ul>
 * <li>0  = All Vehicles</li>
 * <li>11 = Passenger Cars</li>
 * </ul>
 * <p/>
 * are taken into account.
 */
public class GDFDirectionOfFlowEdgeValueFunction implements ILcdEdgeValueFunction {

  public double computeEdgeValue(ILcdGraph aGraph, ILcdRoute aRoute, Object aNextEdge, TLcdTraversalDirection aDirection)
      throws IllegalArgumentException {
    ILcdGDFLineFeature feature = (ILcdGDFLineFeature) aNextEdge;
    if (feature.getFromPoint() == null || feature.getToPoint() == null) {
      return 0;
    }

    for (int i = 0; i < feature.getAttributeCount(); i++) {
      for (int j = 0; j < feature.getAttribute(i).getAttributeCount(); j++) {
        if (feature.getAttribute(i).getAttributeType(j).getAttributeTypeCode() == "DF") {
          long DF_value;
          long VT_value;
          if (feature.getAttribute(i).getAttributeType(j).getRepresentationClass() == Long.class) {
            DF_value = ((Long) feature.getAttribute(i).getAttributeValue(j)).longValue();
            VT_value = ((Long) feature.getAttribute(i).getAttributeValue(j + 1)).longValue();
          } else {
            DF_value = Long.parseLong((String) feature.getAttribute(i).getAttributeValue(j));
            VT_value = Long.parseLong((String) feature.getAttribute(i).getAttributeValue(j + 1));
          }

          if (VT_value == 0 || VT_value == 11) {
            boolean forward = !(aDirection == TLcdTraversalDirection.FORWARD ^ feature.getFromPoint().equals(aRoute.getEndNode()));

            /*
               Some data providers (e.g. TeleAtlas) encode DF values using the following mapping:

                 1 = open in both directions
                 2 = closed in positive direction
                 3 = closed in negative direction
                 4 = closed in both directions

                 booleanh allowed = DF_value == 1 || (DF_value == 2 && !forward) || (DF_value == 3 && forward)


               Other data providers (e.g. Navteq) encode DF values using another mapping:

                 1 = open in both directions
                 2 = open in positive direction
                 3 = open in negative direction
                 4 = closed in both directions

                 boolean allowed = DF_value == 1 || (DF_value == 3 && !forward) || (DF_value == 2 && forward)

               The next line might need to be adjusted depending on the data provider.
            */
            boolean allowed = DF_value == 1 || (DF_value == 2 && !forward) || (DF_value == 3 && forward);
            return allowed ? 0 : Double.POSITIVE_INFINITY;
          }

        }
      }
    }
    return 0;
  }

  public int getOrder() {
    return 0;
  }

}
