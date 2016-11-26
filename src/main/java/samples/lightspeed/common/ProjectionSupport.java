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
package samples.lightspeed.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.projection.*;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.reference.TLcdUTMGrid;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.TLcdInterval;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;

/**
 * Support class to easily change projections.
 */
public class ProjectionSupport {

  static enum WorldReference {
    Geocentric3D("Geocentric", new TLcdGeocentricReference(sDefaultGeodeticDatum), true),
    Grid3D("Grid", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdEquidistantCylindrical()), true),
    EquidistantCylindrical("Equidistant Cylindrical", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdEquidistantCylindrical()), false),
    Orthographic("Orthographic", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdOrthographic()), false),
    Stereographic("Stereographic", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdStereographic()), false),
    Mercator("Mercator", new TLcdGridReference(sDefaultGeodeticDatum, createMercator()), false),
    PseudoMercator("Pseudo Mercator", new TLcdGridReference(sDefaultGeodeticDatum, createPseudoMercator()), false),
    TransverseMercator("Transverse Mercator", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdTransverseMercator()), false),
    UTM("Universal Transverse Mercator (UTM)", new TLcdUTMGrid(30), false),
    LambertConformal("Lambert Conformal", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdLambertConformal()), false),
    LambertAzimuthalEqualArea("Lambert Azimuthal Equal Area", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdLambertAzimuthalEqualArea()), false),
    PolarStereographicNorth("Polar Stereographic (North)", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdPolarStereographic(TLcdPolarStereographic.NORTH_POLE)), false),
    PolarStereographicSouth("Polar Stereographic (South)", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdPolarStereographic(TLcdPolarStereographic.SOUTH_POLE)), false),
    Gnomonic("Gnomonic", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdGnomonic()), false),
    ObliqueMercator("Oblique Mercator", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdObliqueMercator()), false),
    Cassini("Cassini", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdCassini()), false),
    Mollweide("Mollweide", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdMollweide()), false),
    DutchStereographic("Dutch Stereographic", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdDutchStereographic()), false),
    EckertIV("Eckert IV", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdEckertIV()), false),
    EckertVI("Eckert VI", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdEckertVI()), false),
    MillerCylindrical("Miller Cylindrical", new TLcdGridReference(sDefaultGeodeticDatum, new TLcdMillerCylindrical()), false);

    private static ILcdProjection createMercator() {
      TLcdMercator mercator = new TLcdMercator();
      mercator.setLatitudeLimits(new TLcdInterval(-85.0, 85.0));
      return mercator;
    }

    private static ILcdProjection createPseudoMercator() {
      TLcdPseudoMercator pseudoMercator = new TLcdPseudoMercator();
      pseudoMercator.setLatitudeLimits(new TLcdInterval(-85.0, 85.0));
      return pseudoMercator;
    }

    private final String fName;
    private final ILcdXYZWorldReference fWorldReference;
    private final boolean f3D;

    private WorldReference(String aName, ILcdXYZWorldReference aWorldReference, boolean a3D) {
      fName = aName;
      fWorldReference = aWorldReference;
      f3D = a3D;
    }

    public String getName() {
      return fName;
    }

    public ILcdXYZWorldReference getWorldReference() {
      return (ILcdXYZWorldReference) fWorldReference.clone();
    }

    public boolean is3D() {
      return f3D;
    }

    @Override
    public String toString() {
      return fName;
    }
  }

  private static final TLcdGeodeticDatum sDefaultGeodeticDatum;
  private final static LinkedHashMap<String, WorldReference> fName2WorldReference = new LinkedHashMap<String, WorldReference>();

  static {
    sDefaultGeodeticDatum = new TLcdGeodeticDatum();
    WorldReference[] references = WorldReference.values();
    for (WorldReference w : references) {
      fName2WorldReference.put(w.toString(), w);
    }
  }

  public static String[] get2DWorldRefNames() {
    List<String> result = new ArrayList<String>();
    for (WorldReference w : WorldReference.values()) {
      if (!w.is3D()) {
        result.add(w.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  public static String[] get3DWorldRefNames() {
    List<String> result = new ArrayList<String>();
    for (WorldReference w : WorldReference.values()) {
      if (w.is3D()) {
        result.add(w.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  private final ILspView fView;
  private String fProjection;
  private PropertyChangeSupport fPropertyChangeSupport;

  /**
   * Creates a new projection support instance.
   *
   * @param aView the view for which the projection support is created
   */
  public ProjectionSupport(ILspView aView) {
    fView = aView;
    fPropertyChangeSupport = new PropertyChangeSupport(this);
    fProjection = toString(aView.getXYZWorldReference(),
                           aView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D);
    fView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() != this) {
          if ("XYZWorldReference".equalsIgnoreCase(evt.getPropertyName()) ||
              "viewXYZWorldTransformation".equals(evt.getPropertyName())) {
            String newProjection = ProjectionSupport.this.toString(fView.getXYZWorldReference(),
                                                                   fView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D);
            if (newProjection != null && !fProjection.equals(newProjection)) {
              if (fView.getXYZWorldReference() instanceof ILcdGridReference) {
                ILcdProjection projection = ((ILcdGridReference) fView.getXYZWorldReference()).getProjection();
                ILcdPoint origin = getCenter(projection);
                setProjection(newProjection, origin.getX(), origin.getY());
              } else {
                setProjection(newProjection);
              }
            }
          }
        }
      }
    });
  }

  /**
   * Returns the name of the current projection.
   *
   * @return the name of the current projection
   */
  public String getProjection() {
    return fProjection;
  }

  /**
   * Sets the current projection of the associated view to the given projection.
   *
   * @param aProjection the new projection
   */
  public void setProjection(String aProjection) {
    setProjection(aProjection, 0, 0);
  }

  /**
   * Sets up a UTM projection for the given UTM zone.
   * @param aZone the new UTM zone (between 1-60)
   */
  public void setUTMZone(int aZone) {
    if (!(fView.getXYZWorldReference() instanceof TLcdUTMGrid)) {
      setProjection(WorldReference.UTM.getName());
    }
    TLcdUTMGrid reference = (TLcdUTMGrid) fView.getXYZWorldReference();
    if (reference.getZoneID() == aZone) {
      return;
    }
    TLcdUTMGrid worldReference = new TLcdUTMGrid(aZone);
    TLspViewTransformationUtil.setup2DView(fView, worldReference, true);
  }

  /**
   * @return the zone ID of the current UTM projection, or 0 if no UTM projection is set
   */
  public int getUTMZone() {
    ILcdXYZWorldReference reference = fView.getXYZWorldReference();
    return reference instanceof TLcdUTMGrid ? ((TLcdUTMGrid) reference).getZoneID() : 0;
  }

  /**
   * Sets the current projection of the associated view to the given projection.
   *
   * @param aProjection the new projection
   * @param aCenterLon the projection is centered on this longitude, if possible
   * @param aCenterLat the projection is centered on this latitude, if possible
   */
  public void setProjection(String aProjection, double aCenterLon, double aCenterLat) {
    if (aProjection == null || aProjection.isEmpty() || aProjection.equals(fProjection)) {
      return;
    }
    String oldProjection = fProjection;
    fProjection = aProjection;

    WorldReference worldReference = fName2WorldReference.get(aProjection);
    if (worldReference == null) {
      return;
    }

    ILcdXYZWorldReference ref = worldReference.getWorldReference();

    if (aCenterLon != 0 || aCenterLat != 0) {
      if (ref instanceof ILcdGridReference) {
        ILcdProjection projection = ((ILcdGridReference) ref).getProjection();
        center(projection, aCenterLon, aCenterLat);
      }
    }

    if (worldReference.is3D()) {
      TLspViewTransformationUtil
          .setup3DView(fView, ref, true, true);
    } else {
      TLspViewTransformationUtil.setup2DView(fView, ref, true);
    }

    fPropertyChangeSupport.firePropertyChange("projection", oldProjection, fProjection);
  }

  private void center(ILcdProjection projection, double lon, double lat) {
    if (projection instanceof ILcdAzimuthal) {
      ILcdAzimuthal azimuthal = (ILcdAzimuthal) projection;
      azimuthal.setOriginLon(lon);
      azimuthal.setOriginLat(lat);
    } else if (projection instanceof ILcdCylindrical) {
      ILcdCylindrical cylindrical = (ILcdCylindrical) projection;
      cylindrical.setCentralMeridian(lon);
    } else if (projection instanceof ILcdTransverseCylindrical) {
      ILcdTransverseCylindrical transverseCylindrical = (ILcdTransverseCylindrical) projection;
      transverseCylindrical.setCentralMeridian(lon);
      transverseCylindrical.setOriginLat(lat);
    } else if (projection instanceof ILcdObliqueCylindrical) {
      ILcdObliqueCylindrical obliqueCylindrical = (ILcdObliqueCylindrical) projection;
      obliqueCylindrical.setCentralMeridian(lon);
      obliqueCylindrical.setStandardParallel(lat);
    } else if (projection instanceof ILcdConic) {
      ILcdConic conic = (ILcdConic) projection;
      conic.setOriginLon(lon);
      conic.setOriginLat(lat);
    } else if (projection instanceof TLcdPolyconic) {
      TLcdPolyconic polyconic = (TLcdPolyconic) projection;
      polyconic.setOrigin(new TLcdLonLatPoint(lon, lat));
    }
  }

  private ILcdPoint getCenter(ILcdProjection projection) {
    TLcdLonLatPoint result = new TLcdLonLatPoint();
    if (projection instanceof ILcdAzimuthal) {
      ILcdAzimuthal azimuthal = (ILcdAzimuthal) projection;
      result.move2D(azimuthal.getOriginLon(), azimuthal.getOriginLat());
    } else if (projection instanceof ILcdCylindrical) {
      ILcdCylindrical cylindrical = (ILcdCylindrical) projection;
      result.move2D(cylindrical.getCentralMeridian(), 0);
    } else if (projection instanceof ILcdTransverseCylindrical) {
      ILcdTransverseCylindrical transverseCylindrical = (ILcdTransverseCylindrical) projection;
      result.move2D(transverseCylindrical.getCentralMeridian(), transverseCylindrical.getOriginLat());
    } else if (projection instanceof ILcdObliqueCylindrical) {
      ILcdObliqueCylindrical obliqueCylindrical = (ILcdObliqueCylindrical) projection;
      result.move2D(obliqueCylindrical.getCentralMeridian(), obliqueCylindrical.getStandardParallel());
    } else if (projection instanceof ILcdConic) {
      ILcdConic conic = (ILcdConic) projection;
      result.move2D(conic.getOriginLon(), conic.getOriginLat());
    } else if (projection instanceof TLcdPolyconic) {
      TLcdPolyconic polyconic = (TLcdPolyconic) projection;
      result.move2D(polyconic.getOrigin());
    }
    return result;
  }

  public String toString(ILcdXYZWorldReference aReference, boolean a3D) {
    for (WorldReference w : fName2WorldReference.values()) {
      if (w.is3D() != a3D) {
        continue;
      }
      ILcdXYZWorldReference ref = w.getWorldReference();
      if (aReference.getClass().equals(ref.getClass())) {
        if (aReference instanceof TLcdGridReference && ref instanceof TLcdGridReference) {
          ILcdProjection proj1 = ((TLcdGridReference) ref).getProjection();
          ILcdProjection proj2 = ((TLcdGridReference) aReference).getProjection();
          if ((proj1 == null && proj2 == null) ||
              (proj1 != null && proj2 != null && proj1.getClass().equals(proj2.getClass()))) {
            // Special case for polar stereographic (north and south are possible)
            if (proj1 instanceof TLcdPolarStereographic) {
              TLcdPolarStereographic polar1 = (TLcdPolarStereographic) proj1;
              TLcdPolarStereographic polar2 = (TLcdPolarStereographic) proj2;
              if (polar1.getPole() != polar2.getPole()) {
                continue;
              }
            }
            return w.toString();
          }
        } else if (aReference instanceof TLcdGeocentricReference && ref instanceof TLcdGeocentricReference) {
          return w.toString();
        }
      }
    }
    return null;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }
}
