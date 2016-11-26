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

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import com.luciad.format.raster.TLcdWarpRasterPainter;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.ILcdProjection;
import com.luciad.projection.ILcdRectifiedProjection;
import com.luciad.projection.TLcdOrthorectifiedProjection;
import com.luciad.projection.TLcdPerspectiveProjection;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.RasterLayerFactory;
import samples.gxy.rectification.util.EditableRaster;

/**
 * Connects the inputs/outputs of the Orthorectifier and TiePointRectifier classes and creates
 * layers for all the models involved. The entry methods are loadRasterModel and loadTerrainModel.
 * When both the raster and the terrain are loaded the (ortho)rectification is performed.
 */
public class LayerManager {

  private ILcdGXYView fSourceView;  // Left panel.
  private ILcdGXYView fTargetView;  // Right panel.

  private TLcdGXYLayer fTiePointSourceLayer;        // Left panel, TiePointRectifier
  private TLcdGXYLayer fSourceRasterLayer;          // Left panel, TiePointRectifier
  private TLcdGXYLayer fTiePointTargetLayer;        // Right panel, TiePointRectifier
  private TLcdGXYLayer fTargetRasterLayer;          // Right panel, Orthorectifier
  private TLcdGXYLayer fCorrectedTargetLayer;       // Right panel, Orthorectifier
  private TLcdGXYLayer fTargetOrthorectifiedLayer;  // Right panel, Orthorectifier
  private TLcdGXYLayer fTargetTerrainLayer;         // Right panel, Orthorectifier
  private ILcdGXYLayer fCustomDataLayer;            // Right panel, independent

  // A listener that mirrors the selection of tie points from one panel to the other. 
  private MirrorLayerSelectionListener fLayerSelectionListener;

  private TiePointsRectifier fRectifier;    // Handles non-parametric rectification
  private Orthorectifier fOrthorectifier;   // Handles parametric rectification

  private Iterable<ILcdGXYLayerFactory> fLayerFactories;

  // The lower this value, the better the painting accuracy, at the expense of drawing performance.
  private static final int WARP_BLOCK_SIZE = 32;  // in pixels

  public LayerManager(ILcdGXYView aSourceView,
                      ILcdGXYView aTargetView,
                      Orthorectifier aOrthorectifier,
                      TiePointsRectifier aRectifier,
                      Iterable<ILcdGXYLayerFactory> aLayerFactories) {
    fSourceView = aSourceView;
    fTargetView = aTargetView;

    // Make sure that selecting a tie point in one view will select the corresponding tie point
    // in the other view. 
    fLayerSelectionListener = new MirrorLayerSelectionListener();

    fOrthorectifier = aOrthorectifier;
    fRectifier = aRectifier;

    fLayerFactories = aLayerFactories;
  }

  public void loadRasterModel(ILcdModel aRasterModel) {

    ILcdRectifiedProjection rectified_projection = null;
    ILcdProjection camera_projection = null;
    ILcdModel editable_model = aRasterModel;

    ILcdModelReference reference = aRasterModel.getModelReference();
    if (reference instanceof ILcdGridReference) {

      // It is possible that the input raster has already been rectified ("corrected") and/or orthorectified.
      // This means that the projection system of this model may be one of the following
      // 1. rectified projection, contains orthorectified projection, which contains camera and initial projection
      // 2. rectified projection, contains orthorectified projection, which contains camera projection but no initial projection)
      // 3. rectified projection, contains initial projection
      // 4. orthorectified projection, contains camera and initial projection
      // 5. orthorectified projection, contains camera projection but no initial projection
      // 6. initial projection
      //
      // In the context of orthorectification, "initial" projection is a synonym for the wrapped
      // projection (see the developer guide).

      ILcdGridReference grid_ref = (ILcdGridReference) reference;
      ILcdProjection initial_projection = grid_ref.getProjection();
      if (initial_projection instanceof ILcdRectifiedProjection) {
        rectified_projection = (ILcdRectifiedProjection) initial_projection;
        initial_projection = rectified_projection.getBaseProjection();
      }
      if (initial_projection instanceof TLcdPerspectiveProjection) {
        camera_projection = initial_projection;
      } else if (initial_projection instanceof TLcdOrthorectifiedProjection) {
        TLcdOrthorectifiedProjection orthorectified_projection =
            (TLcdOrthorectifiedProjection) initial_projection;
        camera_projection = orthorectified_projection.getCameraProjection();
        initial_projection = orthorectified_projection.getWrappedProjection();
        if (initial_projection == null) {
          initial_projection = camera_projection;
        }
      }

      if (initial_projection != grid_ref.getProjection()) {
        // We need to compute the bounds of the initial raster.
        TLcdGridReference new_reference = new TLcdGridReference(grid_ref);
        new_reference.setProjection(initial_projection);
        TLcd2DBoundsIndexedModel new_model =
            TiePointsRectifier.createEditableRasterModel(aRasterModel);
        new_model.setModelReference(new_reference);

        editable_model = computeInitialBounds(grid_ref, new_reference, new_model);
      }
    }

    fOrthorectifier.loadRasterModel(editable_model, camera_projection);
    // Feed the orthorectified raster to the tie-point rectifier.
    ILcdModel orthorectified_model = fOrthorectifier.getOrthorectifiedRasterModel();
    fRectifier.loadRasterModel(orthorectified_model, rectified_projection);

    // Create/update all the affected layers.
    setTargetOriginalRasterModel(editable_model);
    setOrthorectifiedModel(orthorectified_model);
    setSourceRasterModel(fRectifier.getSourceRasterModel());
    setCorrectedTargetModel(fRectifier.getTargetRasterModel());
    setSourceTiePointsModel(fRectifier.getSourceTiePointModel());
    setTargetTiePointsModel(fRectifier.getTargetTiePointModel());

    // Rearrange the layers, so that they are always in the expected order.
    setLayerOrder();
  }

  /**
   * Takes as input a raster in a given model reference. Creates another raster containing the same
   * tiles and covering the same area, but having a new model reference.
   *
   * @param aOldReference the reference of the input raster model
   * @param aNewReference the desired reference of the output raster model
   * @param aModel        the input raster model
   *
   * @return the output raster model
   */
  private TLcd2DBoundsIndexedModel computeInitialBounds(ILcdGeoReference aOldReference,
                                                        ILcdModelReference aNewReference,
                                                        TLcd2DBoundsIndexedModel aModel) {
    TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference();
    g2g.setSourceReference(aOldReference);
    g2g.setDestinationReference(aNewReference);

    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference(aNewReference);
    model.setModelDescriptor(aModel.getModelDescriptor());

    Enumeration en = aModel.elements();
    while (en.hasMoreElements()) {
      EditableRaster rst = (EditableRaster) en.nextElement();

      ILcdBounds old_bounds = rst.getBounds();
      ILcdPoint old_origin = old_bounds.getLocation().cloneAs2DEditablePoint();

      TLcdXYZPoint new_origin = new TLcdXYZPoint();

      try {
        g2g.sourcePoint2destinationSFCT(old_origin, new_origin);
        rst.move2D(new_origin);

        TLcdXYZPoint pct1 = new TLcdXYZPoint(old_origin.getX() + old_bounds.getWidth(),
                                             old_origin.getY() + old_bounds.getHeight(),
                                             old_origin.getZ() + old_bounds.getDepth());
        TLcdXYZPoint pct2 = new TLcdXYZPoint();

        g2g.sourcePoint2destinationSFCT(pct1, pct2);
        rst.setWidth(pct2.getX() - new_origin.getX());
        rst.setHeight(pct2.getY() - new_origin.getY());
      } catch (TLcdOutOfBoundsException ex) {
        ex.printStackTrace();
      }
      model.addElement(rst, ILcdFireEventMode.NO_EVENT);
    }

    return model;
  }

  /**
   * This method is called when a new terrain model is loaded. If a raster model has been previously
   * loaded, it will be rectified.
   *
   * @param aTerrainModel the terrain model to be used in orthorectification.
   */
  public void loadTerrainModel(ILcdModel aTerrainModel) {

    // Send the terrain model to the orthorectifier. This may trigger the orthorectification of any
    // raster model that might have been already sent to the orthorectifier.
    fOrthorectifier.loadTerrainModel(aTerrainModel);

    // Was there a raster model that has been orthorectified ?
    if (fOrthorectifier.getRasterModel() != null) {

      // Feed the orthorectified raster to the tie-point rectifier.
      fRectifier.loadRasterModel(fOrthorectifier.getOrthorectifiedRasterModel());

      // Create/update all the affected layers.
      setOrthorectifiedModel(fOrthorectifier.getOrthorectifiedRasterModel());
      setSourceRasterModel(fRectifier.getSourceRasterModel());
      setCorrectedTargetModel(fRectifier.getTargetRasterModel());
      setSourceTiePointsModel(fRectifier.getSourceTiePointModel());
      setTargetTiePointsModel(fRectifier.getTargetTiePointModel());
    }

    // Create/update the terrain layer.
    setTerrainModel(aTerrainModel);

    // Rearrange the layers, so that they are always in the expected order.
    setLayerOrder();
  }

  /**
   * Creates the layer for the "source" raster model - the unmodified image displayed in the left
   * panel.
   *
   * @param aRasterModel the raster to be displayed.
   */
  private void setSourceRasterModel(ILcdModel aRasterModel) {
    GXYLayerUtil.removeGXYLayer(fSourceView, fSourceRasterLayer, false);
    fSourceView.setXYWorldReference((ILcdXYWorldReference) aRasterModel.getModelReference());

    RasterLayerFactory factory = new RasterLayerFactory();
    fSourceRasterLayer = (TLcdGXYLayer) factory.createGXYLayer(aRasterModel);

    // When painting large rasters, zoomed out and with forced painting enabled, the performance
    // of a warp raster painter is better than the default painter's.
    TLcdWarpRasterPainter raster_painter = new TLcdWarpRasterPainter();
    fSourceRasterLayer.setGXYPainterProvider(raster_painter);
    raster_painter.setForcePainting(true);
    GXYLayerUtil.addGXYLayer(fSourceView, fSourceRasterLayer);
    GXYLayerUtil.fitGXYLayer(fSourceView, fSourceRasterLayer);
  }

  /**
   * Creates the layer for the "target: raster model - the unmodified raster displayed in the right
   * panel.
   *
   * @param aRasterModel the raster to be displayed.
   */
  private void setTargetOriginalRasterModel(ILcdModel aRasterModel) {
    GXYLayerUtil.removeGXYLayer(fTargetView, fTargetRasterLayer, false);

    RasterLayerFactory factory = new RasterLayerFactory();
    factory.setForcePainting(true);
    factory.setWarpBlockSize(WARP_BLOCK_SIZE);
    fTargetRasterLayer = (TLcdGXYLayer) factory.createGXYLayer(aRasterModel);
    fTargetRasterLayer.setLabel("Original " + aRasterModel.getModelDescriptor().getDisplayName());
    GXYLayerUtil.addGXYLayer(fTargetView, fTargetRasterLayer);
    GXYLayerUtil.fitGXYLayer(fTargetView, fTargetRasterLayer);
  }

  /**
   * Creates the layer for the orthorectified model.
   *
   * @param aRasterModel the orthorectified raster model.
   */
  private void setOrthorectifiedModel(ILcdModel aRasterModel) {
    GXYLayerUtil.removeGXYLayer(fTargetView, fTargetOrthorectifiedLayer, false);
    RasterLayerFactory factory = new RasterLayerFactory();
    factory.setForcePainting(true);
    factory.setWarpBlockSize(WARP_BLOCK_SIZE);
    fTargetOrthorectifiedLayer = (TLcdGXYLayer) factory.createGXYLayer(aRasterModel);
    fTargetOrthorectifiedLayer.setLabel("Orthorectified " + aRasterModel.getModelDescriptor().getDisplayName());
    GXYLayerUtil.addGXYLayer(fTargetView, fTargetOrthorectifiedLayer);
    GXYLayerUtil.fitGXYLayer(fTargetView, fTargetOrthorectifiedLayer);

    aRasterModel.addModelListener(new MyModelListener());
  }

  /**
   * Creates the layer for the corrected (= rectified using tie-points) raster model.
   *
   * @param aRasterModel the corrected raster model.
   */
  private void setCorrectedTargetModel(ILcdModel aRasterModel) {
    GXYLayerUtil.removeGXYLayer(fTargetView, fCorrectedTargetLayer, false);
    RasterLayerFactory factory = new RasterLayerFactory();
    factory.setForcePainting(true);
    factory.setWarpBlockSize(WARP_BLOCK_SIZE);
    factory.setOpacity(0.5f);
    fCorrectedTargetLayer = (TLcdGXYLayer) factory.createGXYLayer(aRasterModel);
    fCorrectedTargetLayer.setLabel("Corrected " + aRasterModel.getModelDescriptor().getDisplayName());
    GXYLayerUtil.addGXYLayer(fTargetView, fCorrectedTargetLayer);
    GXYLayerUtil.fitGXYLayer(fTargetView, fCorrectedTargetLayer);

    aRasterModel.addModelListener(new MyModelListener());
  }

  /**
   * Creates the layer for the terrain raster model.
   *
   * @param aRasterModel the terrain raster model.
   */
  private void setTerrainModel(ILcdModel aRasterModel) {
    GXYLayerUtil.removeGXYLayer(fTargetView, fTargetTerrainLayer, false);
    TLcdCompositeGXYLayerFactory factory = new TLcdCompositeGXYLayerFactory(fLayerFactories);
    fTargetTerrainLayer = (TLcdGXYLayer) factory.createGXYLayer(aRasterModel);
    fTargetTerrainLayer.setLabel("Terrain");
    GXYLayerUtil.addGXYLayer(fTargetView, fTargetTerrainLayer);
    GXYLayerUtil.fitGXYLayer(fTargetView, fTargetTerrainLayer);
  }

  /**
   * Creates the layer for displaying the "source" tie points - the tie points in pixel coordinates
   * displayed in the left panel.
   *
   * @param aModel the source tie-points model.
   */
  private void setSourceTiePointsModel(ILcdModel aModel) {
    GXYLayerUtil.removeGXYLayer(fSourceView, fTiePointSourceLayer, false);

    TLcdGXYIconPainter painter = new TLcdGXYIconPainter();
    painter.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 8, Color.blue));
    painter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 8, Color.red));

    fTiePointSourceLayer = new TLcdGXYLayer();
    fTiePointSourceLayer.setEditable(true);
    fTiePointSourceLayer.setSelectable(true);
    fTiePointSourceLayer.setModel(aModel);
    fTiePointSourceLayer.setGXYEditorProvider(painter);
    fTiePointSourceLayer.setGXYPainterProvider(painter);

    GXYLayerUtil.addGXYLayer(fSourceView, fTiePointSourceLayer);
    fTiePointSourceLayer.addSelectionListener(fLayerSelectionListener);
  }

  /**
   * Creates the layer for displaying the "target" tie points - the tie points in world coordinates
   * displayed in the right panel.
   *
   * @param aModel the target tie-points model.
   */
  private void setTargetTiePointsModel(ILcdModel aModel) {
    GXYLayerUtil.removeGXYLayer(fTargetView, fTiePointTargetLayer, false);

    TLcdGXYIconPainter painter = new TLcdGXYIconPainter();
    painter.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 8, Color.blue));
    painter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 8, Color.red));

    // Create target tie point layer
    fTiePointTargetLayer = new TLcdGXYLayer();
    fTiePointTargetLayer.setEditable(true);
    fTiePointTargetLayer.setSelectable(true);
    fTiePointTargetLayer.setModel(aModel);
    fTiePointTargetLayer.setLabel("Tie Points");
    fTiePointTargetLayer.setGXYEditorProvider(painter);
    fTiePointTargetLayer.setGXYPainterProvider(painter);

    GXYLayerUtil.addGXYLayer(fTargetView, fTiePointTargetLayer);
    fTiePointTargetLayer.addSelectionListener(fLayerSelectionListener);
  }

  /**
   * Adds the custom model layer to the target (right-hand side) view.
   *
   * @param aLayer the layer to be added.
   */
  public void setCustomDataLayer(ILcdGXYLayer aLayer) {
    GXYLayerUtil.removeGXYLayer(fTargetView, fCustomDataLayer, false);
    aLayer.setSelectable(false);
    fCustomDataLayer = aLayer;
    GXYLayerUtil.addGXYLayer(fTargetView, fCustomDataLayer);
    setLayerOrder();
  }

  /**
   * Re-arranges the layers of the target view in a predefined order. Layers that are not yet
   * initialized are silently skipped.
   */
  private void setLayerOrder() {
    int idx = 1;

    ILcdGXYLayer layers[] = new ILcdGXYLayer[]{
        fTargetTerrainLayer,
        fTargetRasterLayer,
        fTargetOrthorectifiedLayer,
        fCorrectedTargetLayer,
        fTiePointTargetLayer,
        fCustomDataLayer
    };
    for (ILcdGXYLayer layer : layers) {
      if (layer != null) {
        GXYLayerUtil.moveGXYLayer(fTargetView, idx++, layer);
      }
    }
  }

  /**
   * This class listens to the changes in the selection of the two tie-points models. When a
   * tie-point is selected or deselected in one view, the corresponding tie point from the other
   * view is also selected/deselected.
   */
  private class MirrorLayerSelectionListener implements ILcdSelectionListener {
    private boolean fInternalChange = false;

    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      TLcdVectorModel tie_point_source_model = (TLcdVectorModel) fTiePointSourceLayer.getModel();
      TLcdVectorModel tie_point_target_model = (TLcdVectorModel) fTiePointTargetLayer.getModel();
      if (fInternalChange || tie_point_source_model.size() != tie_point_target_model.size()) {
        // Prevent recursive calls or calls when new tie-points are being added to a model.
        return;
      }
      fInternalChange = true;
      TLcdGXYLayer layer2;
      TLcdVectorModel model1, model2;
      if (aSelectionEvent.getSelection() == fTiePointSourceLayer) {
        layer2 = fTiePointTargetLayer;
        model1 = tie_point_source_model;
        model2 = tie_point_target_model;
      } else if (aSelectionEvent.getSelection() == fTiePointTargetLayer) {
        layer2 = fTiePointSourceLayer;
        model1 = tie_point_target_model;
        model2 = tie_point_source_model;
      } else {
        return;
      }

      // Deselect all elements from the other layer
      Enumeration enum2 = layer2.selectedObjects();
      Vector<Object> selected = new Vector<Object>();
      while (enum2.hasMoreElements()) {
        selected.add(enum2.nextElement());
      }
      layer2.selectObjects(selected, false, ILcdFireEventMode.FIRE_LATER);
      selected.clear();

      // Select the corresponding element(s) from the other layer.
      Enumeration my_enum = aSelectionEvent.selectedElements();
      Vector<Object> sel = new Vector<Object>();
      while (my_enum.hasMoreElements()) {
        sel.add(my_enum.nextElement());
      }
      for (int i = 0; i < model1.size(); i++) {
        if (sel.contains(model1.elementAt(i))) {
          selected.add(model2.elementAt(i));
        }
      }
      layer2.selectObjects(selected, true, ILcdFireEventMode.FIRE_NOW);
      fInternalChange = false;
    }
  }

  private class MyModelListener implements ILcdModelListener {

    /**
     * Called when either the orthorectified model or the non-parametrically (tie-point) rectified
     * model has changed. This happens when the user loads a new terrain model, or when the user
     * changes some tie-points.
     *
     * @param aEvent the {@code TLcdModelChangedEvent} that contains relevant information on the
     *               change(s).
     */
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      if (aEvent.getModel() == fTargetOrthorectifiedLayer.getModel()) {
        fTargetView.invalidateGXYLayer(fTargetOrthorectifiedLayer, true, this, null);
      } else if (aEvent.getModel() == fCorrectedTargetLayer.getModel()) {
        fTargetView.invalidateGXYLayer(fCorrectedTargetLayer, true, this, null);
      }
    }
  }
}
