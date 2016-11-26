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
package samples.gxy.editing;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolygon;
import com.luciad.util.ILcdFireEventMode;

public class GridModelFactory {

  public ILcdModel createModel() {

    // We create a grid model, based on WGS-84 (the default TLcdGeodeticDatum),
    // and add some default shapes.

    TLcdGeodeticDatum geodeticDatum = new TLcdGeodeticDatum();
    TLcdGridReference modelReference = new TLcdGridReference(geodeticDatum, new TLcdEquidistantCylindrical());
    TLcdVectorModel model = new TLcdVectorModel(modelReference);

    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing the newly created shapes.", // source name (is used as tooltip text)
        "Shapes", // type name
        "Grid shapes"  // display name
    ));

    model.addElement(createXYPolygon(), ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private Object createXYPolygon() {
    // create an array of ILcd2DEditablePoint objects
    ILcd2DEditablePoint[] point_2d_array = {
        new TLcdXYPoint(-8237642.318702244, 3396182.177764575),
        new TLcdXYPoint(-8259906.2168609, 3730140.650144395),
        new TLcdXYPoint(-7937079.693560405, 3841460.140937669),
        new TLcdXYPoint(-7681044.864735876, 3752404.548303051),
        new TLcdXYPoint(-7825760.202767132, 3407314.1268439027)
    };
    // wrap this array into a TLcd2DEditablePointList object
    TLcd2DEditablePointList point_list_2d =
        new TLcd2DEditablePointList(point_2d_array, false);
    // create a geodetic polygon object from the previously created ILcd2DEditablePointList
    // on the given ellipsoid aEllipsoid
    return new TLcdXYPolygon(point_list_2d);
  }

}
