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
package samples.tea;

import java.awt.Component;
import java.util.Enumeration;

import javax.swing.JDialog;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdMatrixView;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.tea.*;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdLineType;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.ILcdLayer;

import samples.gxy.common.ProgressUtil;

public abstract class AbstractVisibilityAction {

  public enum Dialog {
    ENABLED,
    DISABLED
  }

  private static final TLcdLineType LINE_TYPE = TLcdLineType.GEODETIC;

  private static final TLcdAltitudeMatrixViewFactory ALTITUDE_MATRIX_VIEW_FACTORY = new TLcdAltitudeMatrixViewFactory();
  private static final ILcdAltitudeProvider ALTITUDE_PROVIDER = new TLcdFixedHeightAltitudeProvider(2, TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL);
  private static final ILcdAltitudeProvider ALTITUDE_PROVIDER_2 = new TLcdFixedHeightAltitudeProvider(2, TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL);
  private static final TLcdAltitudeDescriptor ALTITUDE_DESCRIPTOR = TLcdAltitudeDescriptor.getDefaultInstance();

  private static final TLcdVisibilityMatrixViewFactory VISIBILITY_MATRIX_VIEW_FACTORY = new TLcdVisibilityMatrixViewFactory();
  public static final TLcdVisibilityDescriptor VISIBILITY_DESCRIPTOR = TLcdVisibilityDescriptor.getDefaultInstance();

  private static final TLcdMatrixViewRasterFactory MATRIX_VIEW_RASTER_FACTORY = new TLcdMatrixViewRasterFactory();
  private static final ILcdMatrixRasterValueMapper MATRIX_RASTER_VALUE_MAPPER = new VisibilityMatrixRasterValueMapper(VISIBILITY_DESCRIPTOR);

  private final Component fComponent;
  private final ILcdLayer fToPolylineLayer;
  private final ILcdLayer fToPolygonLayer;
  private final TLcdP2PRadarPropagationFunction fP2PPropagationFunction;
  private final ILcdPoint fPoint;
  private final ILcdGeoReference fPointReference;
  private final ILcdPolyline fPolyline1;
  private final ILcdPolyline fPolyline2;
  private final ILcdGeoReference fPolyline1Reference;
  private final ILcdGeoReference fPolyline2Reference;
  private final ILcdPolygon fPolygon1;
  private final ILcdPolygon fPolygon2;
  private final ILcdGeoReference fPolygon1Reference;
  private final ILcdGeoReference fPolygon2Reference;

  public AbstractVisibilityAction(Component aComponent,
                                  ALcdTerrainElevationProvider aTerrainElevationProvider,
                                  ILcdLayer aPointLayer, ILcdLayer aPolylineLayer, ILcdLayer aPolygonLayer,
                                  ILcdLayer aToPolylineLayer, ILcdLayer aToPolygonLayer) {
    fComponent = aComponent;
    fToPolylineLayer = aToPolylineLayer;
    fToPolygonLayer = aToPolygonLayer;

    fPoint = (ILcdPoint) aPointLayer.getModel().elements().nextElement();
    fPointReference = (ILcdGeoReference) aPointLayer.getModel().getModelReference();

    Enumeration enumeration = aPolylineLayer.getModel().elements();
    fPolyline1 = (ILcdPolyline) enumeration.nextElement();
    fPolyline2 = (ILcdPolyline) enumeration.nextElement();

    fPolyline1Reference = (ILcdGeoReference) aPolylineLayer.getModel().getModelReference();
    fPolyline2Reference = (ILcdGeoReference) aPolylineLayer.getModel().getModelReference();

    enumeration = aPolygonLayer.getModel().elements();
    fPolygon1 = (ILcdPolygon) enumeration.nextElement();
    fPolygon2 = (ILcdPolygon) enumeration.nextElement();

    fPolygon1Reference = (ILcdGeoReference) aPolygonLayer.getModel().getModelReference();
    fPolygon2Reference = (ILcdGeoReference) aPolygonLayer.getModel().getModelReference();

    fP2PPropagationFunction = new TLcdP2PRadarPropagationFunction(TLcdEarthRepresentationMode.SPHERICAL_EULER_RADIUS, aTerrainElevationProvider, 1.0);
  }

  private void pointToPolyline(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, Dialog aDialog) {
    JDialog dialog = null;
    if (aDialog == Dialog.ENABLED) {
      dialog = ProgressUtil.createProgressDialog(fComponent, "Computing point to polyline visibility.");
      ProgressUtil.showDialog(dialog);
      VISIBILITY_MATRIX_VIEW_FACTORY.addStatusListener((ILcdStatusListener) dialog);
    }

    try {
      double stepSize = computeStepSize(aPolyline, aPolylineReference);

      ILcdAltitudeMatrixView matrixViewPoint = ALTITUDE_MATRIX_VIEW_FACTORY.createPointAltitudeMatrixView(
          fPoint,
          fPointReference,
          2,                        // fixed height above ground
          TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL,
          ALTITUDE_DESCRIPTOR,      // default descriptor
          fPointReference           // same as source reference
      );

      ILcdAltitudeMatrixView matrixViewPolyline = ALTITUDE_MATRIX_VIEW_FACTORY.createPathAltitudeMatrixView(
          aPolyline,
          aPolylineReference,
          ALTITUDE_PROVIDER,        // fixed height above ground
          stepSize,
          LINE_TYPE,                // geodetic line type
          ALTITUDE_DESCRIPTOR,      // default descriptor
          aPolylineReference        // same as source reference
      );
      final
      ILcdMatrixView result = VISIBILITY_MATRIX_VIEW_FACTORY.createVisibilityMatrixView(
          matrixViewPoint,
          matrixViewPolyline,
          stepSize,
          fP2PPropagationFunction,
          VISIBILITY_DESCRIPTOR     // default descriptor
      );
      if (result != null) {
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            ILcdModel model = fToPolygonLayer.getModel();
            try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
              model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
            }
            model.fireCollectedModelChanges();

            model = fToPolylineLayer.getModel();
            try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
              model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
              model.addElement(result, ILcdFireEventMode.FIRE_LATER);
            }
            model.fireCollectedModelChanges();
          }
        });
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not compute visibility: " + e.getMessage());
    } finally {
      if (aDialog == Dialog.ENABLED && dialog != null) {
        ProgressUtil.hideDialog(dialog);
        VISIBILITY_MATRIX_VIEW_FACTORY.removeStatusListener((ILcdStatusListener) dialog);
      }
    }
  }

  private void polylineToPolyline(ILcdPolyline aPolyline1, ILcdGeoReference aPolyline1Reference, ILcdPolyline aPolyline2, ILcdGeoReference aPolyline2Reference, Dialog aDialog) {
    JDialog dialog = null;
    if (aDialog == Dialog.ENABLED) {
      dialog = ProgressUtil.createProgressDialog(fComponent, "Computing polyline to polyline visibility.");
      ProgressUtil.showDialog(dialog);
      VISIBILITY_MATRIX_VIEW_FACTORY.addStatusListener((ILcdStatusListener) dialog);
    }

    try {
      double stepSize = computeStepSize(aPolyline1, aPolyline1Reference);

      ILcdAltitudeMatrixView matrixViewPolyline1 = ALTITUDE_MATRIX_VIEW_FACTORY.createPathAltitudeMatrixView(
          aPolyline1,
          aPolyline1Reference,
          ALTITUDE_PROVIDER,        // fixed height above ground
          stepSize,
          LINE_TYPE,                // geodetic line type
          ALTITUDE_DESCRIPTOR,      // default descriptor
          aPolyline1Reference       // same as source reference
      );

      ILcdAltitudeMatrixView matrixViewPolyline2 = ALTITUDE_MATRIX_VIEW_FACTORY.createPathAltitudeMatrixView(
          aPolyline2,
          aPolyline2Reference,
          ALTITUDE_PROVIDER_2,       // fixed height above ground
          stepSize,
          LINE_TYPE,                // geodetic line type
          ALTITUDE_DESCRIPTOR,      // default descriptor
          aPolyline2Reference       // same as source reference
      );

      final ILcdVisibilityMatrixView result = VISIBILITY_MATRIX_VIEW_FACTORY.createVisibilityMatrixView(
          matrixViewPolyline2,
          matrixViewPolyline1,
          stepSize,
          fP2PPropagationFunction,
          VISIBILITY_DESCRIPTOR     // default descriptor
      );
      if (result != null) {
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            ILcdModel model = fToPolygonLayer.getModel();
            try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
              model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
            }
            model.fireCollectedModelChanges();

            model = fToPolylineLayer.getModel();
            try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
              model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
              model.addElement(result, ILcdFireEventMode.FIRE_LATER);
            }
            model.fireCollectedModelChanges();
          }
        });
      }
    }
    catch (Exception e) {
      throw new RuntimeException( "Could not compute visibility: " + e.getMessage());
    }
    finally {
      if (aDialog == Dialog.ENABLED && dialog != null) {
        ProgressUtil.hideDialog(dialog);
        VISIBILITY_MATRIX_VIEW_FACTORY.removeStatusListener((ILcdStatusListener) dialog);
      }
    }
  }

  private void polygonToPolyline(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, Dialog aDialog) {
    JDialog dialog = null;
    if (aDialog == Dialog.ENABLED) {
      dialog = ProgressUtil.createProgressDialog(fComponent, "Computing polygon to polyline visibility.");
      ProgressUtil.showDialog(dialog);
      VISIBILITY_MATRIX_VIEW_FACTORY.addStatusListener((ILcdStatusListener) dialog);
    }

    try {
      double stepSize = computeStepSize(aPolyline, aPolylineReference);

      ILcdAltitudeMatrixView matrixViewShape = createAreaAltitudeMatrixView(ALTITUDE_MATRIX_VIEW_FACTORY, aPolygon, aPolygonReference, ALTITUDE_PROVIDER, ALTITUDE_DESCRIPTOR);

      ILcdAltitudeMatrixView matrixViewPolyline = ALTITUDE_MATRIX_VIEW_FACTORY.createPathAltitudeMatrixView(
          aPolyline,
          aPolylineReference,
          ALTITUDE_PROVIDER,        // fixed height above ground
          stepSize,
          LINE_TYPE,                // geodetic line type
          ALTITUDE_DESCRIPTOR,      // default descriptor
          aPolylineReference        // same as source reference
      );

      final ILcdVisibilityMatrixView result = VISIBILITY_MATRIX_VIEW_FACTORY.createVisibilityMatrixView(
          matrixViewShape,
          matrixViewPolyline,
          stepSize,
          fP2PPropagationFunction,
          VISIBILITY_DESCRIPTOR     // default descriptor
      );
      if (result != null) {
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            ILcdModel model = fToPolygonLayer.getModel();
            try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
              model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
            }
            model.fireCollectedModelChanges();

            model = fToPolylineLayer.getModel();
            try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
              model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
              model.addElement(result, ILcdFireEventMode.FIRE_LATER);
            }
            model.fireCollectedModelChanges();
          }
        });
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not compute visibility: " + e.getMessage());
    } finally {
      if (aDialog == Dialog.ENABLED && dialog != null) {
        ProgressUtil.hideDialog(dialog);
        VISIBILITY_MATRIX_VIEW_FACTORY.removeStatusListener((ILcdStatusListener) dialog);
      }
    }
  }

  private void pointToPolygon(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, Dialog aDialog) {
    JDialog dialog = null;
    if (aDialog == Dialog.ENABLED) {
      dialog = ProgressUtil.createProgressDialog(fComponent, "Computing point to polygon visibility.");
      ProgressUtil.showDialog(dialog);
      VISIBILITY_MATRIX_VIEW_FACTORY.addStatusListener((ILcdStatusListener) dialog);
    }

    try {
      double stepSize = computeStepSize(aPolygon, aPolygonReference);

      ILcdAltitudeMatrixView matrixViewPoint = ALTITUDE_MATRIX_VIEW_FACTORY.createPointAltitudeMatrixView(
          fPoint,
          fPointReference,
          2,                        // fixed height above ground
          TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL,
          ALTITUDE_DESCRIPTOR,      // default descriptor
          fPointReference           // same as source reference
      );

      ILcdAltitudeMatrixView matrixViewShape = createAreaAltitudeMatrixView(ALTITUDE_MATRIX_VIEW_FACTORY, aPolygon, aPolygonReference, ALTITUDE_PROVIDER, ALTITUDE_DESCRIPTOR);

      ILcdVisibilityMatrixView result = VISIBILITY_MATRIX_VIEW_FACTORY.createVisibilityMatrixView(
          matrixViewPoint,
          matrixViewShape,
          stepSize,
          fP2PPropagationFunction,
          VISIBILITY_DESCRIPTOR     // default descriptor
      );

      if (result != null) {
        final
        ILcdRaster raster = MATRIX_VIEW_RASTER_FACTORY.createEquivalentRaster(
            result,
            (ILcdGeoReference) fToPolygonLayer.getModel().getModelReference(),
            MATRIX_RASTER_VALUE_MAPPER
        );
        if (raster != null) {
          TLcdAWTUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
              ILcdModel model = fToPolylineLayer.getModel();
              try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
                model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
              }
              model.fireCollectedModelChanges();

              model = fToPolygonLayer.getModel();
              try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
                model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
                model.addElement(raster, ILcdFireEventMode.FIRE_LATER);
              }
              model.fireCollectedModelChanges();
            }
          });
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Could not compute visibility: " + e.getMessage());
    }
    finally {
      if (aDialog == Dialog.ENABLED && dialog != null) {
        ProgressUtil.hideDialog(dialog);
        VISIBILITY_MATRIX_VIEW_FACTORY.removeStatusListener((ILcdStatusListener) dialog);
      }
    }
  }

  private void polylineToPolygon(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, Dialog aDialog) {
    JDialog dialog = null;
    if (aDialog == Dialog.ENABLED) {
      dialog = ProgressUtil.createProgressDialog(fComponent, "Computing polyline to polygon visibility.");
      ProgressUtil.showDialog(dialog);
      VISIBILITY_MATRIX_VIEW_FACTORY.addStatusListener((ILcdStatusListener) dialog);
    }

    try {
      double stepSize = computeStepSize(aPolygon, aPolygonReference);

      ILcdAltitudeMatrixView matrixViewPolyline = ALTITUDE_MATRIX_VIEW_FACTORY.createPathAltitudeMatrixView(
          aPolyline,
          aPolylineReference,
          ALTITUDE_PROVIDER,        // fixed height above ground
          stepSize,
          LINE_TYPE,                // geodetic line type
          ALTITUDE_DESCRIPTOR,      // default descriptor
          aPolylineReference        // same as source reference
      );

      ILcdAltitudeMatrixView matrixViewShape = createAreaAltitudeMatrixView(ALTITUDE_MATRIX_VIEW_FACTORY, aPolygon, aPolygonReference, ALTITUDE_PROVIDER, ALTITUDE_DESCRIPTOR);

      ILcdVisibilityMatrixView result = VISIBILITY_MATRIX_VIEW_FACTORY.createVisibilityMatrixView(
          matrixViewPolyline,
          matrixViewShape,
          stepSize,
          fP2PPropagationFunction,
          VISIBILITY_DESCRIPTOR     // default descriptor
      );
      if (result != null) {
        final ILcdRaster raster = MATRIX_VIEW_RASTER_FACTORY.createEquivalentRaster(
            result,
            (ILcdGeoReference) fToPolygonLayer.getModel().getModelReference(),
            MATRIX_RASTER_VALUE_MAPPER
        );
        if (raster != null) {
          TLcdAWTUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
              ILcdModel model = fToPolylineLayer.getModel();
              try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
                model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
              }
              model.fireCollectedModelChanges();

              model = fToPolygonLayer.getModel();
              try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
                model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
                model.addElement(raster, ILcdFireEventMode.FIRE_LATER);
              }
              model.fireCollectedModelChanges();
            }
          });
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Could not compute visibility: " + e.getMessage());
    }
    finally {
      if (aDialog == Dialog.ENABLED && dialog != null) {
        ProgressUtil.hideDialog(dialog);
        VISIBILITY_MATRIX_VIEW_FACTORY.removeStatusListener((ILcdStatusListener) dialog);
      }
    }
  }

  private void polygonToPolygon(ILcdPolygon aPolygon1, ILcdGeoReference aPolygon1Reference, ILcdPolygon aPolygon2, ILcdGeoReference aPolygon2Reference, Dialog aDialog) {
    JDialog dialog = null;
    if (aDialog == Dialog.ENABLED) {
      dialog = ProgressUtil.createProgressDialog(fComponent, "Computing polygon to polygon visibility." );
      ProgressUtil.showDialog(dialog);
      VISIBILITY_MATRIX_VIEW_FACTORY.addStatusListener((ILcdStatusListener) dialog);
    }

    try {
      double stepSize = computeStepSize(aPolygon1, aPolygon1Reference);

      ILcdAltitudeMatrixView matrixViewShape1 = createAreaAltitudeMatrixView(ALTITUDE_MATRIX_VIEW_FACTORY, aPolygon1, aPolygon1Reference, ALTITUDE_PROVIDER, ALTITUDE_DESCRIPTOR);

      ILcdAltitudeMatrixView matrixViewShape2 = createAreaAltitudeMatrixView(ALTITUDE_MATRIX_VIEW_FACTORY, aPolygon2, aPolygon2Reference, ALTITUDE_PROVIDER_2, ALTITUDE_DESCRIPTOR);

      ILcdVisibilityMatrixView result = VISIBILITY_MATRIX_VIEW_FACTORY.createVisibilityMatrixView(
          matrixViewShape2,
          matrixViewShape1,
          stepSize,
          fP2PPropagationFunction,
          VISIBILITY_DESCRIPTOR     // default descriptor
      );
      if (result != null) {
        final ILcdRaster raster = MATRIX_VIEW_RASTER_FACTORY.createEquivalentRaster(
            result,
            (ILcdGeoReference) fToPolygonLayer.getModel().getModelReference(),
            MATRIX_RASTER_VALUE_MAPPER
        );
        if (raster != null) {
          TLcdAWTUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
              ILcdModel model = fToPolylineLayer.getModel();
              try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
                model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
              }
              model.fireCollectedModelChanges();

              model = fToPolygonLayer.getModel();
              try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
                model.removeAllElements(ILcdFireEventMode.FIRE_LATER);
                model.addElement(raster, ILcdFireEventMode.FIRE_LATER);
              }
              model.fireCollectedModelChanges();
            }
          });
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Could not compute visibility: " + e.getMessage());
    }
    finally {
      if (aDialog == Dialog.ENABLED && dialog != null) {
        ProgressUtil.hideDialog(dialog);
        VISIBILITY_MATRIX_VIEW_FACTORY.removeStatusListener((ILcdStatusListener) dialog);
      }
    }
  }

  protected abstract ILcdAltitudeMatrixView createAreaAltitudeMatrixView(TLcdAltitudeMatrixViewFactory aAltitudeMatrixViewFactory,
                                                                         ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, ILcdAltitudeProvider aAltitudeProvider, TLcdAltitudeDescriptor aAltitudeDescriptor) throws TLcdOutOfBoundsException, TLcdNoBoundsException;

  private static double computeStepSize(ILcdShape aShape, ILcdGeoReference aShapeReference) {
    int steps = 100;
    ILcdBounds boundsInMeters = computeBoundsInMeters(aShape, aShapeReference);
    return Math.max(boundsInMeters.getWidth() / steps, boundsInMeters.getHeight() / steps);
  }

  /**
   * Returns the converted shape bounds defined in meters.
   *
   * @param aShape          The shape whose bounds need to be converted.
   * @param aShapeReference The reference in which the shape is defined.
   * @return the converted shape bounds defined in meters.
   */
  private static ILcdBounds computeBoundsInMeters(ILcdShape aShape, ILcdGeoReference aShapeReference) {
    double boundsWidthInMeters = 0;
    double boundsHeightInMeters = 0;
    ILcdBounds bounds = aShape.getBounds();
    ILcdPoint location = bounds.getLocation();
    if (aShapeReference instanceof ILcdGeodeticReference) {
      ILcdEllipsoid ellipsoid = aShapeReference.getGeodeticDatum().getEllipsoid();
      boundsHeightInMeters = ellipsoid.geodesicDistance(
          location.getX(),
          location.getY(),
          location.getX(),
          location.getY() + bounds.getHeight()
      );
      // Find Y coordinate closest to equator -> this gives the maximum width.
      double y;
      if (location.getY() <= 0 && location.getY() + bounds.getHeight() >= 0) {
        y = 0;      // equator is inside bounds.
      }
      else {
        // find minimum Y coordinate between lower and upper bounds line.
        y = Math.min(Math.abs( location.getY() + bounds.getHeight() ), Math.abs(location.getY()));
      }
      // Width of shape depends on latitude
      boundsWidthInMeters = ellipsoid.geodesicDistance(location.getX(), y, location.getX() + bounds.getWidth(), y);
    }
    else if (aShapeReference instanceof ILcdGridReference) {
      ILcdGridReference gridReference = (ILcdGridReference ) aShapeReference;
      double uom = gridReference.getUnitOfMeasure();
      boundsWidthInMeters = aShape.getBounds().getWidth() / uom;
      boundsHeightInMeters = aShape.getBounds().getHeight() / uom;
    }

    return new TLcdXYBounds(0, 0, boundsWidthInMeters, boundsHeightInMeters);
  }

  public enum Shape {

    POINT("Point") {
      @Override
      public void to(Shape aShape, AbstractVisibilityAction aAction, Dialog aDialog) {
        aShape.fromPoint(aAction, aDialog);
      }

      @Override
      void fromPoint(AbstractVisibilityAction aAction, Dialog aDialog) {
        throw new UnsupportedOperationException("Point to point visibility is not supported");
      }

      @Override
      void fromPolyline(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        throw new UnsupportedOperationException("Polyline to point visibility is not supported");
      }

      @Override
      void fromPolygon(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        throw new UnsupportedOperationException("Polygon to point visibility is not supported");

      }

      @Override
      public Object getObject(AbstractVisibilityAction aAction) {
        return aAction.fPoint;
      }
    },
    POLYLINE1("Polyline 1") {
      @Override
      public void to(Shape aShape, AbstractVisibilityAction aAction, Dialog aDialog) {
        aShape.fromPolyline(aAction.fPolyline1, aAction.fPolyline1Reference, aAction, aDialog);
      }

      @Override
      void fromPoint(AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.pointToPolyline(aAction.fPolyline1, aAction.fPolyline1Reference, aDialog);
      }

      @Override
      void fromPolyline(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polylineToPolyline(aAction.fPolyline1, aAction.fPolyline1Reference, aPolyline, aPolylineReference, aDialog);
      }

      @Override
      void fromPolygon(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polygonToPolyline(aPolygon, aPolygonReference, aAction.fPolyline1, aAction.fPolyline1Reference, aDialog);
      }

      @Override
      public Object getObject(AbstractVisibilityAction aAction) {
        return aAction.fPolyline1;
      }
    },
    POLYLINE2("Polyline 2") {
      @Override
      public void to(Shape aShape, AbstractVisibilityAction aAction, Dialog aDialog) {
        aShape.fromPolyline(aAction.fPolyline2, aAction.fPolyline2Reference, aAction, aDialog);
      }

      @Override
      void fromPoint(AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.pointToPolyline(aAction.fPolyline2, aAction.fPolyline2Reference, aDialog);
      }

      @Override
      void fromPolyline(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polylineToPolyline(aAction.fPolyline2, aAction.fPolyline2Reference, aPolyline, aPolylineReference, aDialog);
      }

      @Override
      void fromPolygon(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polygonToPolyline(aPolygon, aPolygonReference, aAction.fPolyline2, aAction.fPolyline2Reference, aDialog);
      }

      @Override
      public Object getObject(AbstractVisibilityAction aAction) {
        return aAction.fPolyline2;
      }
    },
    POLYGON1("Polygon 1") {
      @Override
      public void to(Shape aShape, AbstractVisibilityAction aAction, Dialog aDialog) {
        aShape.fromPolygon(aAction.fPolygon1, aAction.fPolygon1Reference, aAction, aDialog);
      }

      @Override
      void fromPoint(AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.pointToPolygon(aAction.fPolygon1, aAction.fPolygon1Reference, aDialog);
      }

      @Override
      void fromPolyline(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polylineToPolygon(aPolyline, aPolylineReference, aAction.fPolygon1, aAction.fPolygon1Reference, aDialog);
      }

      @Override
      void fromPolygon(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polygonToPolygon(aAction.fPolygon1, aAction.fPolygon1Reference, aPolygon, aPolygonReference, aDialog);
      }

      @Override
      public Object getObject(AbstractVisibilityAction aAction) {
        return aAction.fPolygon1;
      }
    },
    POLYGON2("Polygon 2") {
      @Override
      public void to(Shape aShape, AbstractVisibilityAction aAction, Dialog aDialog) {
        aShape.fromPolygon(aAction.fPolygon2, aAction.fPolygon2Reference, aAction, aDialog);
      }

      @Override
      void fromPoint(AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.pointToPolygon(aAction.fPolygon2, aAction.fPolygon2Reference, aDialog);
      }

      @Override
      void fromPolyline(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polylineToPolygon(aPolyline, aPolylineReference, aAction.fPolygon2, aAction.fPolygon2Reference, aDialog);
      }

      @Override
      void fromPolygon(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, AbstractVisibilityAction aAction, Dialog aDialog) {
        aAction.polygonToPolygon(aAction.fPolygon2, aAction.fPolygon2Reference, aPolygon, aPolygonReference, aDialog);
      }

      @Override
      public Object getObject(AbstractVisibilityAction aAction) {
        return aAction.fPolygon2;
      }
    };

    private final String fName;

    Shape(String aName) {
      this.fName = aName;
    }

    @Override
    public String toString() {
      return fName;
    }

    public abstract void to(Shape aShape, AbstractVisibilityAction aAction, Dialog aDialog);
    public abstract Object getObject(AbstractVisibilityAction aAction);

    abstract void fromPoint(AbstractVisibilityAction aAction, Dialog aDialog);
    abstract void fromPolyline(ILcdPolyline aPolyline, ILcdGeoReference aPolylineReference, AbstractVisibilityAction aAction, Dialog aDialog);
    abstract void fromPolygon(ILcdPolygon aPolygon, ILcdGeoReference aPolygonReference, AbstractVisibilityAction aAction, Dialog aDialog);

  }


}
