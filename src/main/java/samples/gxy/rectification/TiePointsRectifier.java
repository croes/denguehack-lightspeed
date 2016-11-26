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

import java.util.Enumeration;
import java.util.Vector;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.reference.ILcdRasterReference;
import com.luciad.format.raster.reference.ILcdRasterReferencer;
import com.luciad.format.raster.reference.TLcdPolynomialRasterReferencer;
import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.ILcdProjection;
import com.luciad.projection.ILcdRectifiedProjection;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.projection.TLcdGeodetic;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.transformation.TLcdGridReferenceUtil;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdOutOfBoundsException;

import samples.gxy.rectification.util.EditableRaster;

/**
 * This is the core class of the sample. It groups all the data structures in one central place and
 * makes them accessible to the other utility classes.
 *
 * There are two views: the "source" and the "target" view, displayed in the left side and
 * respectively in the right side of the sample.
 *
 * Tie points are defined by the user as pairs in the two views, transformed automatically into
 * model coordinates and used to compute the parameters of the orthorectified projection. The tie
 * points are kept in two models (source/target), and displayed in the corresponding view via two
 * layers.
 *
 * When a new raster is loaded, two editable raster models are constructed from it and placed in the
 * source/target views. The source view (left) is given a convenient reference system, mapping the
 * pixel coordinates of the image directly to "world" coordinates. This reference system has no
 * geographical meaning, but allows us to:
 * a) use pixel coordinates when selecting image tie points
 * b) display a non-distorted image in the left-hand view.
 *
 * The second raster model is placed in the target view and initially has the same reference as the
 * loaded raster. However, as soon as tie points are added or modified, the reference will change to
 * a rectified reference (an ILcdGridReference having an ILcdRectifiedProjection).
 */
public class TiePointsRectifier implements Rectifier {

  private int fImageWidth;   // The width  of the input raster, expressed in pixels.
  private int fImageHeight;  // The height of the input raster, expressed in pixels.

  private TLcdVectorModel fSourceTiePointModel;        // Contains tie-points in pixel coordinates.
  private TLcdVectorModel fTargetTiePointModel;        // Contains tie-points in world coordinates.
  private TLcd2DBoundsIndexedModel fSourceRasterModel; // The raster model in pixel coordinates.
  private TLcd2DBoundsIndexedModel fTargetRasterModel; // The rectified raster model.

  // The raster referencer keeps the geographical reference system of a raster model, together with
  // the bounds of the raster in that reference system.  
  private ILcdRasterReferencer fRasterReferencer = new TLcdPolynomialRasterReferencer(2);

  // A vector of TiePointPair elements. This is only needed when the user
  // deletes tie points from one of the models - it helps us synchronize
  // the removal of the points from the other model.
  private Vector<TiePointPair> fTiePointPairs;

  @Override
  public ILcdModel getSourceModel() {
    return fSourceRasterModel;
  }

  @Override
  public ILcdModel getRectifiedModel() {
    return fTargetRasterModel;
  }

  public int getImageWidth() {
    return fImageWidth;
  }

  public int getImageHeight() {
    return fImageHeight;
  }

  public TLcd2DBoundsIndexedModel getSourceRasterModel() {
    return fSourceRasterModel;
  }

  public TLcd2DBoundsIndexedModel getTargetRasterModel() {
    return fTargetRasterModel;
  }

  public TLcdVectorModel getSourceTiePointModel() {
    return fSourceTiePointModel;
  }

  public TLcdVectorModel getTargetTiePointModel() {
    return fTargetTiePointModel;
  }

  public ILcdRasterReferencer getRasterReferencer() {
    return fRasterReferencer;
  }

  public void setRasterReferencer(ILcdRasterReferencer aRasterReferencer) {
    fRasterReferencer = aRasterReferencer;
  }

  public Vector<TiePointPair> getTiePointPairs() {
    return fTiePointPairs;
  }

  /**
   * The "entry" point - this loads a new raster, creates all the models and initializes all the
   * required data structures.
   *
   * @param aRasterModel the input raster.
   */
  public boolean loadRasterModel(ILcdModel aRasterModel) {
    return loadRasterModel(aRasterModel, null);
  }

  /**
   * Loads a new raster, creates all the models and initializes all the required data structures.
   * This function also receives a (possibly null) rectified projection.
   *
   * @param aRasterModel         the raster model.
   * @param aRectifiedProjection a rectified projection.
   */
  public boolean loadRasterModel(ILcdModel aRasterModel, ILcdRectifiedProjection aRectifiedProjection) {
    if (aRasterModel instanceof ILcd2DBoundsIndexedModel) {

      ILcd2DBoundsIndexedModel model = (ILcd2DBoundsIndexedModel) aRasterModel;

      // Create two new (editable) models from the input raster model.
      fSourceRasterModel = createEditableRasterModel(aRasterModel);
      fTargetRasterModel = createEditableRasterModel(aRasterModel);

      fTargetTiePointModel = new TLcdVectorModel();
      fSourceTiePointModel = new TLcdVectorModel();

      // Create or decode tie points and put them in the corresponding models
      if (aRectifiedProjection != null) {
        // The model's reference already has tie-points, we need to re-use them for initialization.
        setupModelWithTiePoints(model, aRectifiedProjection);
      } else {
        // Create new image and model tie points from scratch
        setupModelWithoutTiePoints(model);
      }

      TiePointRemovalModelListener removal_listener = new TiePointRemovalModelListener();
      fSourceTiePointModel.addModelListener(removal_listener);
      fTargetTiePointModel.addModelListener(removal_listener);

      MyModelListener model_listener = new MyModelListener();
      fSourceTiePointModel.addModelListener(model_listener);
      fTargetTiePointModel.addModelListener(model_listener);

      // Create a collection that relates each image point to its model peer.
      fTiePointPairs = new Vector<TiePointPair>();
      for (int i = 0; i < fSourceTiePointModel.size(); i++) {
        fTiePointPairs.add(new TiePointPair((ILcdPoint) fSourceTiePointModel.elementAt(i),
                                            (ILcdPoint) fTargetTiePointModel.elementAt(i)));
      }

      // Compute a reference that maps the image tie points to the world tie points as good as
      // possible.
      updateRasterReference();
      return true;
    }
    return false;
  }

  /**
   * Given an image width/height and a set of lon/lat bounds, creates a grid reference which places
   * the image inside those geodetic bounds.
   *
   * @param aImageWidth  the image width
   * @param aImageHeight the image height
   *
   * @return a grid reference placing the image inside the given bounds
   */
  private TLcdGridReference createImageReference(double aImageWidth,
                                                 double aImageHeight) {
    return new TLcdGridReference(new TLcdGeodeticDatum(),
                                 new TLcdGeodetic(aImageWidth, aImageHeight));
  }

  /**
   * Given a model that contains ILcdRaster elements, it creates a new model containing
   * EditableRaster elements wrapped around the original rasters.
   *
   * "Normal" rasters do not usually allow us to modify their bounds, so we need to create
   * EditableRasters, for which the bounds can be adjusted as we wish.
   *
   * @param aModel the input model
   *
   * @return a model containing EditableRaster elements
   */
  static TLcd2DBoundsIndexedModel createEditableRasterModel(ILcdModel aModel) {
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    Enumeration elements = aModel.elements();
    while (elements.hasMoreElements()) {
      Object obj = elements.nextElement();
      if (obj instanceof ILcdRaster) {
        ILcdRaster old_raster = (ILcdRaster) obj;
        ILcdTile[][] tiles = new ILcdTile[old_raster.getTileRowCount()][];
        for (int i = 0; i < tiles.length; i++) {
          tiles[i] = new ILcdTile[old_raster.getTileColumnCount()];
          for (int j = 0; j < tiles[i].length; j++) {
            tiles[i][j] = old_raster.retrieveTile(i, j);
          }
        }

        TLcdXYBounds bounds = new TLcdXYBounds(old_raster.getBounds());
        EditableRaster new_raster = new EditableRaster(
            bounds,
            tiles,
            old_raster.getPixelDensity(),
            old_raster.getDefaultValue(),
            old_raster.getColorModel()
        );
        model.addElement(new_raster, ILcdFireEventMode.NO_EVENT);
      } else {
        throw new IllegalArgumentException("Only ILcdRaster elements are supported.");
      }
    }
    model.setModelReference(aModel.getModelReference());
    model.setModelDescriptor(aModel.getModelDescriptor());
    return model;
  }

  /**
   * Try to determine the size of a raster in pixels.
   *
   * @param aRaster the raster to be analyzed.
   */
  private void computeImageSizeSFCT(ILcdRaster aRaster) {
    // Assume that all tiles have the same size in pixels
    ILcdTile tile = aRaster.retrieveTile(0, 0);
    double tile_width = tile.getWidth();
    double tile_height = tile.getHeight();
    int tile_rows = aRaster.getTileRowCount();
    int tile_cols = aRaster.getTileColumnCount();

    fImageWidth = (int) (tile_width * tile_cols);
    fImageHeight = (int) (tile_height * tile_rows);
  }

  /**
   * The input raster model is expressed in a regular, non-rectified reference system. We create 4
   * points, one at each corner of the raster. The 4 points transformed in pixel coordinates will be
   * placed in the source tie point model. The same 4 points transformed in world coordinates will
   * be placed in the target tie point model. By doing this, the computed rectified reference will
   * initially be identical to the original reference of the input raster.
   *
   * @param aRasterModel the input raster model.
   */
  private void setupModelWithoutTiePoints(ILcd2DBoundsIndexedModel aRasterModel) {

    // Create the reference model and a very simple grid reference shared by
    // all the elements in the left panel:
    // - reference view
    // - reference tie point model
    // - reference raster model
    // The grid reference maps the pixel coordinates to the initial lon/lat bounds.
    EditableRaster raster = (EditableRaster) fSourceRasterModel.elementAt(0);
    computeImageSizeSFCT(raster);

    // Create a suitable reference for the left-side raster (fRasterSourceModel):
    // - must be a grid
    // - model coordinates are the same as pixel coordinates
    // - it doesn't need to be a geographically valid reference
    ILcdModelReference image_reference = createImageReference(fImageWidth, fImageHeight);

    // Update the raster's bounds.
    Enumeration my_enum = fSourceRasterModel.elements();
    while (my_enum.hasMoreElements()) {
      raster = (EditableRaster) my_enum.nextElement();
      raster.move2D(0, 0);
      raster.setWidth(fImageWidth);
      raster.setHeight(fImageHeight);
    }

    TLcdXYZBounds old_bounds = (TLcdXYZBounds) fSourceRasterModel.getBounds();
    old_bounds.move2D(0, 0);
    old_bounds.setWidth(fImageWidth);
    old_bounds.setHeight(fImageHeight);

    fSourceRasterModel.setModelReference(image_reference);
    fSourceTiePointModel = new TLcdVectorModel(image_reference);

    TLcdXYPoint[] image_points = new TLcdXYPoint[4];
    image_points[0] = new TLcdXYPoint(0, 0);
    image_points[1] = new TLcdXYPoint(fImageWidth, 0);
    image_points[2] = new TLcdXYPoint(fImageWidth, fImageHeight);
    image_points[3] = new TLcdXYPoint(0, fImageHeight);

    for (TLcdXYPoint image_point : image_points) {
      fSourceTiePointModel.addElement(image_point, ILcdFireEventMode.NO_EVENT);
    }

    ILcdPoint[] model_points = new TLcdXYPoint[4];
    ILcdBounds raster_bounds = aRasterModel.getBounds();
    double scale_x = raster_bounds.getWidth() / fImageWidth;
    double scale_y = raster_bounds.getHeight() / fImageHeight;
    double x = raster_bounds.getLocation().getX();
    double y = raster_bounds.getLocation().getY();
    model_points[0] = new TLcdXYPoint(x, y);
    model_points[1] = new TLcdXYPoint(fImageWidth * scale_x + x, y);
    model_points[2] = new TLcdXYPoint(fImageWidth * scale_x + x, fImageHeight * scale_y + y);
    model_points[3] = new TLcdXYPoint(x, fImageHeight * scale_y + y);

    // Create the target tie point model.  
    for (ILcdPoint model_point : model_points) {
      fTargetTiePointModel.addElement(model_point, ILcdFireEventMode.NO_EVENT);
    }

    if (aRasterModel.getModelReference() instanceof ILcdGridReference) {
      fTargetTiePointModel.setModelReference(aRasterModel.getModelReference());
    } else {
      ILcdModelReference raster_reference = aRasterModel.getModelReference();
      ILcdGeodeticDatum datum = ((ILcdGeodeticReference) raster_reference).getGeodeticDatum();
      TLcdGridReference grid_reference = new TLcdGridReference(datum, new TLcdEquidistantCylindrical());

      TLcdGeodetic2Grid g2g = new TLcdGeodetic2Grid(raster_reference, grid_reference);
      fTargetTiePointModel.removeAllElements(ILcdFireEventMode.NO_EVENT);
      for (ILcdPoint model_point : model_points) {
        TLcdXYZPoint pct = new TLcdXYZPoint();
        try {
          g2g.modelPoint2worldSFCT(model_point, pct);
          fTargetTiePointModel.addElement(pct, ILcdFireEventMode.NO_EVENT);
        } catch (TLcdOutOfBoundsException ex) {
          ex.printStackTrace();
        }
      }
      fTargetTiePointModel.setModelReference(grid_reference);
    }
  }

  /**
   * The input raster model is expressed in an already rectified reference system. The rectified
   * reference system contains a rectified projection that provides us with an initial set of
   * tie-points. We extract those points and use them to initialize the source and the target tie
   * point models.
   *
   * @param aRasterModel the input raster model.
   * @param aRectifiedProjection the rectified projection
   */
  private void setupModelWithTiePoints(ILcd2DBoundsIndexedModel aRasterModel,
                                       ILcdRectifiedProjection aRectifiedProjection) {

    ILcdGridReference model_reference = (ILcdGridReference) aRasterModel.getModelReference();
    computeImageSizeSFCT((ILcdRaster) fSourceRasterModel.elementAt(0));

    double scale_x = aRasterModel.getBounds().getWidth() / fImageWidth;
    double scale_y = aRasterModel.getBounds().getHeight() / fImageHeight;
    TLcdGridReferenceUtil ref_util = new TLcdGridReferenceUtil(model_reference);

    for (int i = 0; i < aRectifiedProjection.getTiePointCount(); i++) {
      TLcdXYPoint pct = new TLcdXYPoint();
      ref_util.world2gridSFCT(aRectifiedProjection.getSourcePoint(i), pct);
      double x = pct.getX() / scale_x;
      double y = pct.getY() / scale_y;
      pct.move2D(x, y);
      fSourceTiePointModel.addElement(pct, ILcdFireEventMode.FIRE_LATER);

      pct = new TLcdXYPoint();
      ref_util.world2gridSFCT(aRectifiedProjection.getTargetPoint(i), pct);
      fTargetTiePointModel.addElement(pct, ILcdFireEventMode.FIRE_LATER);
    }

    ILcdModelReference image_reference = createImageReference(fImageWidth, fImageHeight);
    fSourceTiePointModel.setModelReference(image_reference);
    fSourceRasterModel.setModelReference(image_reference);
    Enumeration my_enum = fSourceRasterModel.elements();
    while (my_enum.hasMoreElements()) {
      EditableRaster raster = (EditableRaster) my_enum.nextElement();
      raster.move2D(0, 0);
      raster.setWidth(fImageWidth);
      raster.setHeight(fImageHeight);
    }
    TLcdXYZBounds old_bounds = (TLcdXYZBounds) fSourceRasterModel.getBounds();
    old_bounds.move2D(0, 0);
    old_bounds.setWidth(fImageWidth);
    old_bounds.setHeight(fImageHeight);

    ILcdModelReference non_rectified_ref = getNonRectifiedReference(aRasterModel.getModelReference());
    fTargetTiePointModel.setModelReference(non_rectified_ref);

    updateRasterReference();
  }

  /**
   * Verifies if the given reference makes use of rectification. If it does, it creates a new
   * similar reference, minus the rectified projection. If it doesn't, it returns the same reference
   * unmodified.
   *
   * @param aModelReference a possibly rectified reference
   *
   * @return a non-rectified reference
   */
  private ILcdModelReference getNonRectifiedReference(ILcdModelReference aModelReference) {
    if (aModelReference instanceof ILcdGridReference) {
      ILcdGridReference grid_ref = (ILcdGridReference) aModelReference;
      ILcdProjection projection = grid_ref.getProjection();

      if (projection instanceof ILcdRectifiedProjection) {
        // We need to create a similar reference, minus the orthorectified projection.
        ILcdProjection helper = ((ILcdRectifiedProjection) projection).getBaseProjection();
        if (helper == null) {
          // Fall-back to a geodetic reference
          return new TLcdGeodeticReference(grid_ref.getGeodeticDatum());
        } else {
          TLcdGridReference ref = new TLcdGridReference(grid_ref);
          ref.setProjection(helper);
          return ref;
        }
      }
    }
    return aModelReference;
  }

  /**
   * Takes the two sets of tie points (reference/image points and target points) and computes a new
   * orthorectified reference.
   */
  public void updateRasterReference() {

    // Safety check - do nothing with less than 3 pairs of points.
    if (fSourceTiePointModel.size() < 3) {
      return;
    }

    // Create an initial rotated reference system in which the raster is aligned
    // with the axes.
    TLcdXYPoint[] img_points = new TLcdXYPoint[fSourceTiePointModel.size()];
    TLcdXYPoint[] map_points = new TLcdXYPoint[fTargetTiePointModel.size()];

    for (int i = 0; i < img_points.length; i++) {
      img_points[i] = new TLcdXYPoint((ILcdPoint) fSourceTiePointModel.elementAt(i));
      map_points[i] = new TLcdXYPoint((ILcdPoint) fTargetTiePointModel.elementAt(i));

      // The raster referencer expects image coordinates with inverted y.
      img_points[i].move2D(img_points[i].getX(), fImageHeight - img_points[i].getY());
    }

    ILcdModelReference model_reference;

    ILcdRasterReference raster_reference =
        fRasterReferencer.createRasterReference(fImageWidth, fImageHeight,
                                                img_points,
                                                fTargetTiePointModel.getModelReference(),
                                                map_points,
                                                null);

    model_reference = raster_reference.getModelReference();
    ILcdBounds new_bounds = raster_reference.getBounds();

    // Update the raster's bounds.
    Enumeration my_enum = fTargetRasterModel.elements();
    while (my_enum.hasMoreElements()) {
      EditableRaster raster = (EditableRaster) my_enum.nextElement();
      raster.move2D(new_bounds.getLocation());
      raster.setWidth(new_bounds.getWidth());
      raster.setHeight(new_bounds.getHeight());

    }
    TLcdXYZBounds old_bounds = (TLcdXYZBounds) fTargetRasterModel.getBounds();
    old_bounds.move2D(new_bounds.getLocation());
    old_bounds.setWidth(new_bounds.getWidth());
    old_bounds.setHeight(new_bounds.getHeight());

    fTargetRasterModel.setModelReference(model_reference);
    fTargetRasterModel.allElementsChanged(ILcdFireEventMode.FIRE_NOW);
  }

  private class TiePointRemovalModelListener implements ILcdModelListener {

    @Override
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      ILcdModel sourceModel = aEvent.getModel();

      if ((aEvent.getCode() & TLcdModelChangedEvent.ALL_OBJECTS_REMOVED) != 0 ||
          (aEvent.getCode() & TLcdModelChangedEvent.SOME_OBJECTS_REMOVED) != 0) {
        ILcdModel mirrorModel = retrieveMirrorModel(sourceModel);

        Enumeration elements = aEvent.elements();
        while (elements.hasMoreElements()) {
          Object element = elements.nextElement();
          int change = aEvent.retrieveChange(element);
          if (change == TLcdModelChangedEvent.OBJECT_REMOVED) {
            Object mirrorObject = retrieveMirrorObject(element, sourceModel);
            if (mirrorObject != null) {
              mirrorModel.removeElement(mirrorObject, ILcdModel.FIRE_LATER);
              removeTiePointPair(element, sourceModel);
            }
          }
        }

        mirrorModel.fireCollectedModelChanges();
      }
    }

    private ILcdModel retrieveMirrorModel(ILcdModel aModel) {
      if (aModel == fSourceTiePointModel) {
        return fTargetTiePointModel;
      } else {
        return fSourceTiePointModel;
      }
    }

    private Object retrieveMirrorObject(Object aObject, ILcdModel aModel) {
      for (TiePointPair tiePointPair : fTiePointPairs) {
        if (aModel == fSourceTiePointModel) {
          if (aObject == tiePointPair.fSourcePoint) {
            return tiePointPair.fTargetPoint;
          }
        } else {
          if (aObject == tiePointPair.fTargetPoint) {
            return tiePointPair.fSourcePoint;
          }
        }
      }
      return null;
    }

    private void removeTiePointPair(Object aObject, ILcdModel aModel) {
      for (int i = 0; i < fTiePointPairs.size(); i++) {
        TiePointPair tiePointPair = fTiePointPairs.get(i);
        if (aModel == fSourceTiePointModel) {
          if (aObject == tiePointPair.fSourcePoint) {
            fTiePointPairs.remove(tiePointPair);
            return;
          }
        } else {
          if (aObject == tiePointPair.fTargetPoint) {
            fTiePointPairs.remove(tiePointPair);
            return;
          }
        }
      }
    }
  }

  /**
   * Container for grouping a pair of source/target tie points.
   */
  public static class TiePointPair {

    ILcdPoint fSourcePoint;
    ILcdPoint fTargetPoint;

    public TiePointPair(ILcdPoint aSourcePoint, ILcdPoint aTargetPoint) {
      fSourcePoint = aSourcePoint;
      fTargetPoint = aTargetPoint;
    }
  }

  /**
   * Whenever one tie-point has been modified, we need to recompute the rectified raster reference.
   */
  private class MyModelListener implements ILcdModelListener {

    private boolean fInternalChange = false;

    public void modelChanged(TLcdModelChangedEvent e) {

      if (fInternalChange ||
          fSourceTiePointModel.size() != fTargetTiePointModel.size()) {
        return;
      }

      fInternalChange = true;
      updateRasterReference();
      fInternalChange = false;
    }
  }

}
