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
package samples.gxy.projections;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.projection.ILcdAzimuthal;
import com.luciad.projection.ILcdConic;
import com.luciad.projection.ILcdCylindrical;
import com.luciad.projection.ILcdObliqueCylindrical;
import com.luciad.projection.ILcdProjection;
import com.luciad.projection.ILcdTransverseCylindrical;
import com.luciad.projection.TLcdPolarStereographic;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.transformation.TLcdGridReferenceUtil;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYController;
import com.luciad.view.map.TLcdMapLocation;

/**
 * When active on a <code>ILcdGXYView</code>, this <code>ILcdController</code>
 * will catch each mouseReleased and try to modify the properties of the
 * <code>ILcdProjection</code> currently on the <code>ILcdGXYView</code> accordingly.
 * <p/>
 * E.g, if the <code>ILcdProjection</code> is an <code>ILcdAzimuthal</code> projection,
 * this <code>ILcdController</code> will transform the mouseReleased screen point into
 * a Lon and Lat coordinate, and use them to set the originLon and originLat properties
 * of the <code>ILcdAzimuthal</code> projection.
 */
public class GXYCenterMapController
    extends ALcdGXYController
    implements MouseListener {

  private TLcdMapLocation fTLcdMapLocation = new TLcdMapLocation();
  private ILcdAction fActionToTrigger;

  public GXYCenterMapController() {
    this(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }

  public GXYCenterMapController(Cursor aCursor) {
    setName("Center map");
    setShortDescription("Click to recenter the map");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.RECENTER_ICON));
    setCursor(aCursor);
  }

  /**
   * Sets an <code>ILcdAction</code> to trigger just after having recentered the <code>ILcdGXYView</code>.
   * This can be used to set another <code>ILcdGXController</code> after each recentre.
   *
   * @see com.luciad.view.gxy.TLcdGXYSetControllerAction
   */
  public void setActionToTrigger(ILcdAction aActionToTrigger) {
    fActionToTrigger = aActionToTrigger;
  }

  public ILcdAction getActionToTrigger() {
    return fActionToTrigger;
  }

  /**
   * This method is called by the <code>ILcdGXYView</code> when it makes this <code>ILcdGXYController</code>
   * active on it.
   */
  public void startInteraction(ILcdGXYView aGXYView) {
    super.startInteraction(aGXYView);
  }

  /**
   * This method is called by the <code>ILcdGXYView</code> when it stops this <code>ILcdGXYController</code>
   */
  public void terminateInteraction(ILcdGXYView aGXYView) {
    super.terminateInteraction(aGXYView);
  }

  /**
   * Invoked when a mouse button has been released on the <code>ILcdGXYView</code> component.
   */
  public void mouseReleased(MouseEvent me) {
    recentre(me);
  }

  public void recentre(MouseEvent me) {
    fTLcdMapLocation.mouseMoved(me);

    // lon, lat are the coordinates where the mouse has been released.
    double lon = fTLcdMapLocation.getLon();
    double lat = fTLcdMapLocation.getLat();

    ILcdXYWorldReference xy_world_ref = getGXYView().getXYWorldReference();

    // We check that the xy_world_ref is an ILcdGridReference to get the ILcdProjection
    // of the ILcdGXYView
    if (xy_world_ref instanceof ILcdGridReference) {
      ILcdGridReference grid_ref = (ILcdGridReference) xy_world_ref;
      ILcdProjection projection = grid_ref.getProjection();
      ILcdGeodeticDatum geodetic_datum = grid_ref.getGeodeticDatum();
      projection = (ILcdProjection) projection.clone();
      geodetic_datum = (ILcdGeodeticDatum) geodetic_datum.clone();
      TLcdGridReference new_grid_ref =
          new TLcdGridReference(geodetic_datum, projection,
                                grid_ref.getFalseEasting(),
                                grid_ref.getFalseNorthing(),
                                grid_ref.getScale(),
                                grid_ref.getUnitOfMeasure(),
                                grid_ref.getRotation());

      // We now look what kind of projection it is and set it up depending on
      // its type
      if (projection instanceof ILcdAzimuthal) {
        ILcdAzimuthal azimuthal = (ILcdAzimuthal) projection;
        azimuthal.setOriginLon(lon);
        azimuthal.setOriginLat(lat);
      } else if (projection instanceof ILcdCylindrical) {
        ILcdCylindrical cylindrical = (ILcdCylindrical) projection;
        cylindrical.setCentralMeridian(lon);
      } else if (projection instanceof ILcdTransverseCylindrical) {
        ILcdTransverseCylindrical transverse_cylindrival = (ILcdTransverseCylindrical) projection;
        transverse_cylindrival.setCentralMeridian(lon);
        transverse_cylindrival.setOriginLat(lat);
      } else if (projection instanceof ILcdObliqueCylindrical) {
        ILcdObliqueCylindrical oblique_cylindrical = (ILcdObliqueCylindrical) projection;
        oblique_cylindrical.setCentralMeridian(lon);
        oblique_cylindrical.setStandardParallel(lat);
      } else if (projection instanceof ILcdConic) {
        ILcdConic conic = (ILcdConic) projection;
        conic.setOriginLon(lon);
        conic.setOriginLat(lat);
      } else if (projection instanceof TLcdPolarStereographic) {
        ((TLcdPolarStereographic) projection).setCentralMeridian(lon);
      }
      // avoid repainting of the ILcdGXYView on every property change
      boolean auto_update_mode_to_restore = getGXYView().isAutoUpdate();
      try {
        getGXYView().setAutoUpdate(false);
        // We put the view origin to the center of the view Component
        Point view_origin = new Point(getGXYView().getWidth() / 2,
                                      getGXYView().getHeight() / 2);
        getGXYView().setViewOrigin(view_origin);
        getGXYView().setXYWorldReference(new_grid_ref);
      } finally {
        getGXYView().setAutoUpdate(auto_update_mode_to_restore);
      }

      // set the world origin to the center of the projection
      TLcdGridReferenceUtil gridrefutil = new TLcdGridReferenceUtil(new_grid_ref);
      TLcdXYPoint new_world_origin = new TLcdXYPoint();
      try {
        gridrefutil.lonlat2gridSFCT(lon, lat, new_world_origin);
        getGXYView().setWorldOrigin(new_world_origin);
      } catch (TLcdOutOfBoundsException ex) {
      }

      if (fActionToTrigger != null && fActionToTrigger.isEnabled()) {
        ActionEvent action_event = new ActionEvent(getGXYView(),
                                                   ActionEvent.ACTION_PERFORMED,
                                                   "recentre",
                                                   0);
        fActionToTrigger.actionPerformed(action_event);
      }
    }
  }

  /**
   * Invoked when a mouse button has been clicked on the <code>ILcdGXYView</code> component.
   */
  public void mouseClicked(MouseEvent me) {
    if (me.getModifiers() == (me.BUTTON3_MASK)) {
      recentre(me);
    }
  }

  /**
   * Invoked when a mouse button has been pressed on the <code>ILcdGXYView</code> component.
   */
  public void mousePressed(MouseEvent me) {
  }

  /**
   * Invoked when the mouse enters the <code>ILcdGXYView</code> component.
   */
  public void mouseEntered(MouseEvent me) {
  }

  /**
   * Invoked when the mouse exits the <code>ILcdGXYView</code> component.
   */
  public void mouseExited(MouseEvent me) {
  }
}

