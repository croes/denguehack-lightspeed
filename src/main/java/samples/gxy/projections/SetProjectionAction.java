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

import java.awt.event.ActionEvent;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.ALcdAction;
import com.luciad.projection.ILcdAzimuthal;
import com.luciad.projection.ILcdCylindrical;
import com.luciad.projection.ILcdObliqueCylindrical;
import com.luciad.projection.ILcdProjection;
import com.luciad.projection.ILcdTransverseCylindrical;
import com.luciad.projection.TLcdGeodetic;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYViewFitAction;

/**
 * When triggered (actionPerformed method), this ILcdAction will
 * set a given ILcdProjection to a given ILcdGXYView
 * (both are given in the constructor).
 */
public class SetProjectionAction extends ALcdAction {

  private ILcdProjection fProjection;
  private ILcdGXYView fGXYView;
  private String fName;

  public SetProjectionAction(ILcdProjection aProjection, String aActionName, ILcdGXYView aGXYView) {
    fProjection = aProjection;
    fGXYView = aGXYView;
    fName = aActionName;
  }


  public void actionPerformed(ActionEvent e) {
    if (fProjection != null) {
      ILcdGridReference old_gridref = (ILcdGridReference) (fGXYView.getXYWorldReference());
      TLcdGridReference new_gridref = new TLcdGridReference(old_gridref);
      if (fProjection instanceof TLcdGeodetic) {
        ILcdEllipsoid ellipsoid = old_gridref.getGeodeticDatum().getEllipsoid();
        double uom = Math.toRadians(ellipsoid.getA());
        TLcdGeodetic projection = new TLcdGeodetic(uom, uom);
        new_gridref = new TLcdGridReference(old_gridref.getGeodeticDatum(),
                                            projection,
                                            0.0, 0.0, 1.0,
                                            uom,
                                            0.0);
      } else {

        adjustMyProjection();

        new_gridref.setProjection((ILcdProjection) fProjection.clone());
        new_gridref.setGeodeticDatum(old_gridref.getGeodeticDatum());
      }
      fGXYView.setXYWorldReference(new_gridref);
      if ( fGXYView.layerCount() > 0) {
        new TLcdGXYViewFitAction(fGXYView).fitGXYLayer((ILcdGXYLayer) fGXYView.getLayer(0), fGXYView);
      }
    }
  }

  public String toString() {
    return fName;
  }

  public ILcdProjection getProjection() {
    return fProjection;
  }

  /**
   * This method gets the ILcdProjection of the ILcdGXYView
   * passed in the constructor method, and tries to adjust the ILcdProjection
   * of this action to fit as much as possible to the ILcdProjection
   * currently active in the ILcdGXYView.
   * <p/>
   * For example, consider that the ILcdProjection of the ILcdGXYView
   * is currently an ILcdCylindrical which central meridian is lon = 50degrees,
   * and consider the ILcdProjection of this action is an ILcdAzimuthal.
   * It means that this action will replace the current ILcdCylindrical
   * projection of the ILcdGXYView by an ILcdAzimuthal projection.
   * We would like that the lon attribute of the ILcdCylindrical projection
   * to be 50degrees also.
   * <p/>
   * This method will be called in the actionPerformed method just before
   * setting this action ILcdProjection to its ILcdGXYView.
   */
  protected void adjustMyProjection() {

    ILcdGridReference old_view_projection_ref = (ILcdGridReference) fGXYView.getXYWorldReference();
    ILcdProjection old_view_projection = old_view_projection_ref.getProjection();

    if (fProjection instanceof ILcdAzimuthal) {
      ILcdAzimuthal new_azimuthal = (ILcdAzimuthal) fProjection;
      if (old_view_projection instanceof ILcdAzimuthal) {
        ILcdAzimuthal old_azimuthal = (ILcdAzimuthal) old_view_projection;
        new_azimuthal.setOriginLon(old_azimuthal.getOriginLon());
        new_azimuthal.setOriginLat(old_azimuthal.getOriginLat());
      } else if (old_view_projection instanceof ILcdCylindrical) {
        new_azimuthal.setOriginLon(((ILcdCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdObliqueCylindrical) {
        new_azimuthal.setOriginLon(((ILcdObliqueCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdTransverseCylindrical) {
        new_azimuthal.setOriginLon(((ILcdTransverseCylindrical) old_view_projection).getCentralMeridian());
      }
    } else if (fProjection instanceof ILcdCylindrical) {
      ILcdCylindrical new_cylindrical = (ILcdCylindrical) fProjection;
      if (old_view_projection instanceof ILcdAzimuthal) {
        new_cylindrical.setCentralMeridian(((ILcdAzimuthal) old_view_projection).getOriginLon());
      } else if (old_view_projection instanceof ILcdCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdObliqueCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdObliqueCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdTransverseCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdTransverseCylindrical) old_view_projection).getCentralMeridian());
      }
    } else if (fProjection instanceof ILcdObliqueCylindrical) {
      ILcdObliqueCylindrical new_cylindrical = (ILcdObliqueCylindrical) fProjection;
      if (old_view_projection instanceof ILcdAzimuthal) {
        new_cylindrical.setCentralMeridian(((ILcdAzimuthal) old_view_projection).getOriginLon());
      } else if (old_view_projection instanceof ILcdCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdObliqueCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdObliqueCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdTransverseCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdTransverseCylindrical) old_view_projection).getCentralMeridian());
      }
    } else if (fProjection instanceof ILcdTransverseCylindrical) {
      ILcdTransverseCylindrical new_cylindrical = (ILcdTransverseCylindrical) fProjection;
      if (old_view_projection instanceof ILcdAzimuthal) {
        new_cylindrical.setCentralMeridian(((ILcdAzimuthal) old_view_projection).getOriginLon());
      } else if (old_view_projection instanceof ILcdCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdObliqueCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdObliqueCylindrical) old_view_projection).getCentralMeridian());
      } else if (old_view_projection instanceof ILcdTransverseCylindrical) {
        new_cylindrical.setCentralMeridian(((ILcdTransverseCylindrical) old_view_projection).getCentralMeridian());
      }
    }
  }
}
