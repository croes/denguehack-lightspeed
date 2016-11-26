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
package samples.gxy.rectification;

import java.awt.Component;
import java.util.Enumeration;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.model.ALcdModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.projection.ILcdProjection;
import com.luciad.projection.TLcdGeodetic;
import com.luciad.projection.TLcdOrthorectifiedProjection;
import com.luciad.projection.TLcdPerspectiveProjection;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdCache;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.util.height.TLcdFixedHeightProvider;
import com.luciad.util.height.TLcdRasterHeightProvider;
import com.luciad.util.height.TLcdTransformedHeightProvider;

import samples.gxy.rectification.util.PerspectiveParamsDialog;

/**
 * This class takes as input a raster model and a digital terrain elevation model (DEM). The raster
 * model is orthorectified using the information from the DEM model and the result is placed in a
 * new model.
 */
public class Orthorectifier implements Rectifier {

  private Component fParentComponent;

  private ILcdModel fTerrainModel;
  private ILcdModel fRasterModel;
  private ALcdModel fOrthorectifiedRasterModel;

  private ILcdHeightProvider fHeightProvider;
  private ILcdHeightProvider fTransformedHeightProvider;
  private ILcdProjection f3DCameraProjection;

  public Orthorectifier(Component aParentComponent) {
    fParentComponent = aParentComponent;
    fHeightProvider = new TLcdFixedHeightProvider(0, new TLcdXYBounds());
    fTransformedHeightProvider = fHeightProvider;
  }

  @Override
  public ILcdModel getSourceModel() {
    return fRasterModel;
  }

  @Override
  public ILcdModel getRectifiedModel() {
    return fOrthorectifiedRasterModel;
  }

  public ILcdModel getRasterModel() {
    return fRasterModel;
  }

  public ALcdModel getOrthorectifiedRasterModel() {
    return fOrthorectifiedRasterModel;
  }

  public void loadRasterModel(ILcdModel aRasterModel) {
    loadRasterModel(aRasterModel, null);
  }

  public void loadRasterModel(ILcdModel aRasterModel, ILcdProjection a3DCameraProjection) {
    fRasterModel = aRasterModel;
    f3DCameraProjection = a3DCameraProjection;

    if (f3DCameraProjection == null) {
      // Verify if the raster's reference uses a perspective projection.
      // If it does not, popup a dialog box and ask the camera parameters from the user.
      ILcdModelReference model_reference = aRasterModel.getModelReference();
      if (model_reference instanceof ILcdGridReference) {
        ILcdGridReference gridReference = (ILcdGridReference) model_reference;
        ILcdProjection projection = gridReference.getProjection();
        if (projection instanceof TLcdPerspectiveProjection) {
          // The raster uses a perspective projection, We can use it directly.
          f3DCameraProjection = projection;
        } else if (projection instanceof TLcdOrthorectifiedProjection) {
          TLcdOrthorectifiedProjection ortho = (TLcdOrthorectifiedProjection) projection;
          f3DCameraProjection = ortho.getCameraProjection();

          // Avoid wrapping of the reference's TLcdOrthorectifiedProjection in a second
          // TLcdOrthorectifiedProjection when rectifyModel is called.
          TLcdGridReference grid = new TLcdGridReference((ILcdGridReference) model_reference);
          grid.setProjection(ortho.getWrappedProjection() == null ? ortho.getCameraProjection() :
                             ortho.getWrappedProjection());
          ((ALcdModel) fRasterModel).setModelReference(grid);
        }
      }
    }

    if (f3DCameraProjection == null) {
      // Popup the dialog box
      PerspectiveParamsDialog panel = new PerspectiveParamsDialog(fParentComponent);
      panel.setVisible(true);
      f3DCameraProjection = panel.getProjection();
    }

    if (fTerrainModel != null) {
      // TLcdOrthorectifiedProjection requires a height provider that works in geodetic coordinates
      // (see the TLcdOrthorectifiedProjection API documentation). Since the terrain model may be
      // expressed in a different reference system, we create a transformed height provider.
      // The new geodetic reference must use the same geodetic datum as the raster model.
      ILcdGeoReference raster_ref = (ILcdGeoReference) fRasterModel.getModelReference();
      ILcdGeoReference geodetic_ref = new TLcdGeodeticReference(raster_ref.getGeodeticDatum());

      ILcdGeoReference terrain_ref = (ILcdGeoReference) fTerrainModel.getModelReference();
      TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference(terrain_ref,
                                                                            geodetic_ref);
      try {
        fTransformedHeightProvider = new TLcdTransformedHeightProvider(fHeightProvider, g2g);
      } catch (TLcdNoBoundsException ex) {
        throw new RuntimeException("The raster model and terrain model have incompatible reference systems.");
      }

      fOrthorectifiedRasterModel = rectifyModel(aRasterModel,
                                                f3DCameraProjection,
                                                fTransformedHeightProvider);
    }
  }

  public void loadTerrainModel(ILcdModel aTerrainModel) {

    if (!(aTerrainModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor)) {
      throw new IllegalArgumentException("The terrain model must contain single-level rasters.");
    }

    fTerrainModel = aTerrainModel;
    ILcdRaster raster = (ILcdRaster) aTerrainModel.elements().nextElement();
    fHeightProvider = new TLcdRasterHeightProvider(raster);

    if (fRasterModel != null) {
      // TLcdOrthorectifiedProjection requires a height provider that works in geodetic coordinates
      // (see the TLcdOrthorectifiedProjection API documentation). Since the terrain model may be
      // expressed in a different reference system, we create a transformed height provider.
      // The new geodetic reference must use the same geodetic datum as the raster model.
      ILcdGeoReference raster_ref = (ILcdGeoReference) fRasterModel.getModelReference();
      ILcdGeoReference geodetic_ref = new TLcdGeodeticReference(raster_ref.getGeodeticDatum());

      ILcdGeoReference terrain_ref = (ILcdGeoReference) fTerrainModel.getModelReference();
      TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference(terrain_ref,
                                                                            geodetic_ref);
      try {
        fTransformedHeightProvider = new TLcdTransformedHeightProvider(fHeightProvider, g2g);
      } catch (TLcdNoBoundsException ex) {
        throw new RuntimeException("The raster model and terrain model have incompatible reference systems.");
      }

      fOrthorectifiedRasterModel = rectifyModel(fRasterModel,
                                                f3DCameraProjection,
                                                fTransformedHeightProvider);
    }
  }

  /**
   * Creates an orthorectified version of a given reference system, using the position of the
   * imaging sensor and information about the terrain elevation.
   *
   * @param a3DCameraProjection    normally a TLcdPerspectiveProjection corresponding to the
   *                               camera.
   * @param aHeightProvider        the terrain elevation.
   * @param aNonRectifiedReference the input geographical reference system to be orthorectified.
   *
   * @return an orthorectified reference.
   */
  private TLcdGridReference rectifyReference(ILcdProjection a3DCameraProjection,
                                             ILcdHeightProvider aHeightProvider,
                                             ILcdGeoReference aNonRectifiedReference) {
    TLcdGridReference reference;
    if (aNonRectifiedReference instanceof ILcdGeodeticReference) {
      // Create a fake grid reference, because we need to have a projection.
      reference = createGeodeticGridReference(aNonRectifiedReference.getGeodeticDatum());
    } else if (aNonRectifiedReference instanceof ILcdGridReference) {
      reference = new TLcdGridReference((ILcdGridReference) aNonRectifiedReference);
    } else {
      throw new IllegalArgumentException("The input reference must be either geodetic or grid.");
    }

    TLcdOrthorectifiedProjection new_proj;
    if (a3DCameraProjection == reference.getProjection()) {
      new_proj = new TLcdOrthorectifiedProjection(a3DCameraProjection, aHeightProvider);
    } else {
      new_proj = new TLcdOrthorectifiedProjection(a3DCameraProjection,
                                                  reference.getProjection(),
                                                  aHeightProvider);
    }
    reference.setProjection(new_proj);
    return reference;
  }

  private static TLcdGridReference createGeodeticGridReference(ILcdGeodeticDatum aDatum) {
    ILcdEllipsoid ellipsoid = aDatum.getEllipsoid();
    double uom = Math.toRadians(ellipsoid.getA());
    TLcdGeodetic projection = new TLcdGeodetic(uom, uom);
    return new TLcdGridReference(aDatum,
                                 projection,
                                 0.0, 0.0, 1.0,
                                 uom,
                                 0.0);
  }

  /**
   * Creates an orthorectified version of a given raster model, using the position of the imaging
   * sensor and information about the terrain elevation.
   *
   * @param aRasterModel        the input raster model to be orthorectified.
   * @param a3DCameraProjection normally a TLcdPerspectiveProjection corresponding to the camera.
   * @param aAltitudeProvider   the terrain elevation.
   *
   * @return an orthorectified raster model.
   */
  private ALcdModel rectifyModel(ILcdModel aRasterModel,
                                 ILcdProjection a3DCameraProjection,
                                 ILcdHeightProvider aAltitudeProvider) {

    ILcdModelReference old_Ref = aRasterModel.getModelReference();
    TLcdGridReference new_ref = rectifyReference(a3DCameraProjection,
                                                 aAltitudeProvider,
                                                 (ILcdGeoReference) old_Ref);
    ALcdModel new_model = new TLcd2DBoundsIndexedModel();
    Enumeration en = aRasterModel.elements();
    while (en.hasMoreElements()) {
      Object element = en.nextElement();
      resetElementCache(element);
      new_model.addElement(element, ILcdFireEventMode.FIRE_LATER);
    }
    new_model.setModelDescriptor(aRasterModel.getModelDescriptor());
    new_model.setModelReference(new_ref);
    return new_model;
  }

  /**
   * When the elements are copied from one model to another, any existing cache information must be
   * invalidated. This is because a stale cache may affect the painting.
   *
   * @param aObject the object whose cache must be invalidated.
   */
  private void resetElementCache(Object aObject) {
    if (aObject instanceof ILcdCache) {
      ((ILcdCache) aObject).clearCache();
    }
    if ((aObject instanceof ILcdShapeList)) {
      ILcdShapeList shp_list = (ILcdShapeList) aObject;
      for (int i = 0; i < shp_list.getShapeCount(); i++) {
        resetElementCache(shp_list.getShape(i));
      }
    }
    if ((aObject instanceof ILcdMultilevelRaster)) {
      ILcdMultilevelRaster rst_list = (ILcdMultilevelRaster) aObject;
      for (int i = 0; i < rst_list.getRasterCount(); i++) {
        resetElementCache(rst_list.getRaster(i));
      }
    }
    if ((aObject instanceof ILcdRaster)) {
      ILcdRaster rst_list = (ILcdRaster) aObject;
      for (int i = 0; i < rst_list.getTileRowCount(); i++) {
        for (int j = 0; j < rst_list.getTileColumnCount(); j++) {
          resetElementCache(rst_list.retrieveTile(i, j));
        }
      }
    }
  }
}
