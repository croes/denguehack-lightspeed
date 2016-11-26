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
package samples.lightspeed.shapes;

import java.io.IOException;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdFireEventMode;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample shows how to programmatically create and display a model (ILcdModel)
 * by manually creating domain objects (ILcdShape) and adding them to the model.
 */
public class MainPanel extends LightspeedSample {

  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(createPolygonModel()).layer().addToView(getView()).fit();
    LspDataUtil.instance().model(createPolylineModel()).layer().addToView(getView());
    LspDataUtil.instance().model(createPointModel()).layer().addToView(getView());
  }

  private ILcdModel createPolygonModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPolygon",      // data type
        "PolygonModel"    // display name (user)
    ));

    model.addElement(createLonLatPolygon(datum.getEllipsoid()),
                     ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createPolylineModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPolyline",     // data type
        "PolylineModel"   // display name (user)
    ));

    model.addElement(createLonLatPolyline(datum.getEllipsoid()),
                     ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private ILcdModel createPointModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPoint",        // data type
        "PointModel"      // display name (user)
    ));

    model.addElement(new TLcdLonLatPoint(3.6, 51.2), ILcdFireEventMode.NO_EVENT);

    return model;
  }

  private Object createLonLatPolygon(ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] point_2d_array = {
        new TLcdLonLatPoint(3.4, 51.5),
        new TLcdLonLatPoint(4.3, 51.8),
        new TLcdLonLatPoint(5.2, 51.6),
        new TLcdLonLatPoint(5.2, 50.5),
        new TLcdLonLatPoint(3.7, 50.4),
    };
    ILcd2DEditablePointList point_list_2d =
        new TLcd2DEditablePointList(point_2d_array, false);
    return new TLcdLonLatPolygon(point_list_2d, aEllipsoid);
  }

  private Object createLonLatPolyline(ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] point_2d_array = {
        new TLcdLonLatPoint(3.3, 50.4),
        new TLcdLonLatPoint(3.4, 50.5),
        new TLcdLonLatPoint(3.7, 50.8),
        new TLcdLonLatPoint(4.0, 50.9),
        new TLcdLonLatPoint(4.6, 51.2),
        new TLcdLonLatPoint(5.2, 51.9),
    };
    ILcd2DEditablePointList point_list_2d =
        new TLcd2DEditablePointList(point_2d_array, false);
    return new TLcdLonLatPolyline(point_list_2d, aEllipsoid);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Displaying shapes");
  }

}
