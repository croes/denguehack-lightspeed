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
package samples.opengl.common;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewLocation;

import javax.swing.JLabel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * This component displays the lon/lat coordinates of the location under the
 * mouse pointer on a 3D view. A TLcdGLViewLocation is used to compute the
 * coordinates.
 */
public class MouseLocationComponent extends JLabel {

  private ILcdGLView fGLView;
  private TLcdGLViewLocation fGLViewLocation;
  private TLcdGLViewLocation.LocationMode fLocationMode;
  private TLcdLonLatFormatter fLLFormat;
  private TLcdXYZPoint fWorldPoint = new TLcdXYZPoint();
  private TLcdLonLatHeightPoint fLLHPoint = new TLcdLonLatHeightPoint();
  private TLcdGeoReference2GeoReference fWorld2LonLat;

  public MouseLocationComponent( ILcdGLView aGLView ) {
    this( aGLView, TLcdGLViewLocation.LocationMode.SEA_LEVEL );
  }

  public MouseLocationComponent( ILcdGLView aGLView, TLcdGLViewLocation.LocationMode aLocationMode ) {
    fGLView = aGLView;
    fGLViewLocation = new TLcdGLViewLocation();
    fLocationMode = aLocationMode;
    fGLView.addMouseMotionListener( new MyMouseMotionListener() );

    fLLFormat = new TLcdLonLatFormatter(TLcdLonLatFormatter.DEFAULT);

    // Create a transformation from world coordinates (obtained from
    // TLcdGLViewLocation) to lon/lat coordinates.
    fWorld2LonLat = new TLcdGeoReference2GeoReference(
            (ILcdGeoReference) fGLView.getXYZWorldReference(),
            new TLcdGeodeticReference(new TLcdGeodeticDatum())
          );
  }

  class MyMouseMotionListener extends MouseMotionAdapter {
    public void mouseDragged( MouseEvent e ) {
      this.mouseMoved( e );
    }

    public void mouseMoved( MouseEvent e ) {
      try {
        updateMouseCoordinates( e, fLocationMode );
      }
      catch (TLcdOutOfBoundsException e1) {
        if (fLocationMode == TLcdGLViewLocation.LocationMode.CLOSEST_SURFACE) {
          // maybe the CLOSEST_SURFACE approach failed,
          // try once more with the default SEA_LEVEL approach
          try {
            updateMouseCoordinates( e, TLcdGLViewLocation.LocationMode.SEA_LEVEL );
          } catch ( TLcdOutOfBoundsException e2 ) {
            // also failed using SEA_LEVEL => Cursor is not over the surface of the Earth.
            setText("###");
          }
        }
        else {
          // Cursor is not over the surface of the Earth.
          setText("###");
        }
      }
      repaint();
    }

    private void updateMouseCoordinates( MouseEvent e, TLcdGLViewLocation.LocationMode aLocationMode ) throws TLcdOutOfBoundsException {
      // First obtain world coordinates for the point under the cursor.
      fGLViewLocation.viewAWTPoint2worldSFCT(
            fGLView,
            e.getPoint(),
            aLocationMode,
            fWorldPoint
          );
      // Transform the world coordinates to geodetic coordinates.
      fWorld2LonLat.sourcePoint2destinationSFCT(fWorldPoint, fLLHPoint);
      // Format and display the results in fLabel.
      setText(fLLFormat.format(fLLHPoint.getX(), fLLHPoint.getY()));
    }
  }
}

