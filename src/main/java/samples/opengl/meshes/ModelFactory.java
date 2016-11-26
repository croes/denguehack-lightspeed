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
package samples.opengl.meshes;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

/**
 * A utility class that can create some hard coded models. These models are used
 * as sample data in the sample application.
 */
class ModelFactory {

  public static final String ARROW_MODEL_DISPLAY_NAME = "Arrows";
  public static final String ARROW_MODEL_TYPE_NAME = ARROW_MODEL_DISPLAY_NAME;
  public static final String WAYPOINT_MODEL_DISPLAY_NAME = "Waypoints";
  public static final String WAYPOINT_MODEL_TYPE_NAME = WAYPOINT_MODEL_DISPLAY_NAME;
  public static final String TARGET_MODEL_DISPLAY_NAME = "Target";
  public static final String TARGET_MODEL_TYPE_NAME = TARGET_MODEL_DISPLAY_NAME;
  public static final String TRACK_MODEL_DISPLAY_NAME = "Track";
  public static final String TRACK_MODEL_TYPE_NAME = TRACK_MODEL_DISPLAY_NAME;
  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = GRID_MODEL_DISPLAY_NAME;

  
  public static ILcdModel createTrackModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            TRACK_MODEL_DISPLAY_NAME,
            TRACK_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor( descriptor );
    model.setModelReference( modelReference );

    OrientedLonLatHeightPoint p = new OrientedLonLatHeightPoint( 10, 45, 20000, 90, 0, 0 );
    model.addElement( p, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createWaypointModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            WAYPOINT_MODEL_DISPLAY_NAME,
            WAYPOINT_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor( descriptor );
    model.setModelReference( modelReference );

    OrientedLonLatHeightPoint p1 = new OrientedLonLatHeightPoint( 10, 46, 20000, -90, 0, 0 );
    model.addElement( p1, ILcdFireEventMode.NO_EVENT );

    OrientedLonLatHeightPoint p2 = new OrientedLonLatHeightPoint( 12, 46, 20000, -90, 0, 0 );
    model.addElement( p2, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createTargetModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            TARGET_MODEL_DISPLAY_NAME,
            TARGET_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor( descriptor );
    model.setModelReference( modelReference );

    OrientedLonLatHeightPoint p1 = new OrientedLonLatHeightPoint( 10, 47, 20000, 0, 0, 0 );
    model.addElement( p1, ILcdFireEventMode.NO_EVENT );

    return model;
  }

  public static ILcdModel createArrowModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            ARROW_MODEL_DISPLAY_NAME,
            ARROW_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelDescriptor( descriptor );
    model.setModelReference( modelReference );

    int steps = 8;
    double da = 180./steps;
    for (int i = 1; i < steps; i++) {
      double angle = -90 + i*da;
      double x = 10 + Math.cos(Math.toRadians( angle ));
      double y = 46 + Math.sin(Math.toRadians( angle ));
      OrientedLonLatHeightPoint p = new OrientedLonLatHeightPoint( x, y, 20000, -angle, 0, 0 );
      model.addElement( p, ILcdFireEventMode.NO_EVENT );
    }
    return model;
  }


  public static ILcdModel createGridModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", GRID_MODEL_DISPLAY_NAME, GRID_MODEL_TYPE_NAME );
    TLcdGeodeticReference reference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );
    model.addElement( new TLcdLonLatGrid( 10, 10 ), ILcdFireEventMode.NO_EVENT );
    return model;
  }
}
