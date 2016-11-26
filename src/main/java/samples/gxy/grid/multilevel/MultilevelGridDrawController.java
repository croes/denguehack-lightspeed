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
package samples.gxy.grid.multilevel;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Vector;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.controller.ALcdGXYController;
import com.luciad.view.map.multilevelgrid.ILcdEditableMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGrid;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridCoordinateModel;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridLayer;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridUtil;

/**
 * A controller that adds multilevel elements to a multilevel model based on the mouse position of the view.
 * The controller adds elements in a smart way, taking into account elements at higher and lower levels
 * and siblings at the same level.
 */
public class MultilevelGridDrawController
    extends ALcdGXYController
    implements MouseListener, MouseMotionListener {

  private TLcdGXYContext fGXYContext = new TLcdGXYContext();
  private TLcdLonLatHeightPoint fTempModelPoint = new TLcdLonLatHeightPoint();
  private TLcdXYPoint fTempWorldPoint = new TLcdXYPoint();
  private Point fTempViewPoint = new Point();
  private TLcdLonLatBounds fTempModelBounds = new TLcdLonLatBounds();
  private ILcdEditableMultilevelGridCoordinate fTempEditableCoordinate = new TLcdMultilevelGridCoordinate();
  private Vector fTempVector = new Vector();

  private String fStatus = StatusedEditableMultilevelGridCoordinate.HOSTILE;

  private RemoveElementFunction fRemoveElementFunction = new RemoveElementFunction();

  public MultilevelGridDrawController() {
  }

  public ILcdIcon getIcon() {
    return TLcdIconFactory.create(TLcdIconFactory.EDIT_ICON);
  }

  public String getShortDescription() {
    return "Add elements to a CGRS or GARS grid layer";
  }

  public String getName() {
    return "Multilevel draw";
  }

  public void mouseClicked(MouseEvent e) {
    handleMouse(e.getPoint().getX(), e.getPoint().getY());
  }

  public void mouseDragged(MouseEvent e) {
    handleMouse(e.getPoint().getX(), e.getPoint().getY());
  }

  public void setStatus(String aStatus) {
    fStatus = aStatus;
  }

  public String getStatus() {
    return fStatus;
  }

  private void handleMouse(double aMouseX, double aMouseY) {
    // Walk over all the layers and retrieve the multilevel grid layer that has been touched.
    fTempViewPoint.setLocation(aMouseX, aMouseY);
    TLcdGXYContext context = new TLcdGXYContext();

    Enumeration layers = getGXYView().layers();
    while (layers.hasMoreElements()) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();

      if (layer instanceof StatusedCoordinateLayer) {
        StatusedCoordinateLayer coordinateLayer = (StatusedCoordinateLayer) layer;

        context.resetFor(layer, getGXYView());
        TLcdMultilevelGridCoordinateModel model = (TLcdMultilevelGridCoordinateModel) layer.getModel();
        ILcdMultilevelGrid multilevelGrid = model.getMultilevelGrid();

        // convert the mouse location.
        fGXYContext.resetFor(layer, getGXYView());
        fGXYContext.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(fTempViewPoint, fTempWorldPoint);
        try {
          fGXYContext.getModelXYWorldTransformation().worldPoint2modelSFCT(fTempWorldPoint, fTempModelPoint);

          StatusedEditableMultilevelGridCoordinate editable_coordinate =
              coordinateLayer.createStatusedEditableMultilevelGridCoordinate();
          editable_coordinate.setStatus(fStatus);

          TLcdMultilevelGridLayer gridLayer = coordinateLayer.getGridLayer();
          int level = determineLevel(gridLayer, multilevelGrid, context);
          if (level != -1) {
            TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(fTempModelPoint, level + 1, multilevelGrid, (ILcdGeoReference) gridLayer.getModel().getModelReference(), editable_coordinate);
            addElement(model, editable_coordinate);
          }
        } catch (TLcdOutOfBoundsException e1) {
          // the mouse was not in model bounds. we do nothing.
        }
      }
    }
  }

  /**
   * Take care of merging and splitting elements when a new element is added to the model.
   * When an element is added at a different level, it is possible that an element at a higher level
   * needs to be split or that elements at the same level should be merged. Elements at lower levels
   * at the same location should be removed anyhow.
   * @param aModel          the model to add the element to.
   * @param aStatusedEditableMultilevelCoordinate the element to add to the model.
   */
  private void addElement(TLcdMultilevelGridCoordinateModel aModel,
                          StatusedEditableMultilevelGridCoordinate aStatusedEditableMultilevelCoordinate) {
    boolean should_add_object = true;
    // check whether there is a parent with the same status, in that case we do not have to add it at all.
    // if there is a parent with a different status, we will have to split that parent.
    for (int level_index = 0; level_index < aStatusedEditableMultilevelCoordinate.getCoordinateLevelCount(); level_index++) {
      fTempVector.clear();
      fTempEditableCoordinate.setCoordinate(level_index, ILcdMultilevelGrid.X_AXIS, aStatusedEditableMultilevelCoordinate.getCoordinate(level_index, ILcdMultilevelGrid.X_AXIS));
      fTempEditableCoordinate.setCoordinate(level_index, ILcdMultilevelGrid.Y_AXIS, aStatusedEditableMultilevelCoordinate.getCoordinate(level_index, ILcdMultilevelGrid.Y_AXIS));
      fTempEditableCoordinate.setCoordinateLevelCount(level_index + 1);
      aModel.elementsAtSFCT(fTempEditableCoordinate, fTempVector);
      for (int element_index = 0; element_index < fTempVector.size(); element_index++) {
        StatusedEditableMultilevelGridCoordinate element_at_coordinate = (StatusedEditableMultilevelGridCoordinate) fTempVector.elementAt(element_index);
        if (!aStatusedEditableMultilevelCoordinate.getStatus().equals(element_at_coordinate.getStatus())) {
          aModel.removeElement(element_at_coordinate, ILcdFireEventMode.FIRE_LATER);
          if (element_at_coordinate.getCoordinateLevelCount() != aStatusedEditableMultilevelCoordinate.getCoordinateLevelCount()) {
            // we have to split up this element, because it is a higher element with a different status.
            // we have to add its children again.
            int split_level = element_at_coordinate.getCoordinateLevelCount();
            for (int x_index = 0; x_index < aModel.getMultilevelGrid().getDivisions(split_level, ILcdMultilevelGrid.X_AXIS); x_index++) {
              for (int y_index = 0; y_index < aModel.getMultilevelGrid().getDivisions(split_level, ILcdMultilevelGrid.Y_AXIS); y_index++) {
                ILcdEditableMultilevelGridCoordinate split_coordinate = element_at_coordinate.cloneAsEditableMultilevelCoordinate();
                split_coordinate.setCoordinateLevelCount(split_level + 1);
                split_coordinate.setCoordinate(split_level, ILcdMultilevelGrid.X_AXIS, x_index);
                split_coordinate.setCoordinate(split_level, ILcdMultilevelGrid.Y_AXIS, y_index);
                aModel.addElement(split_coordinate, ILcdFireEventMode.FIRE_LATER);
              }
            }
          }
        } else {
          should_add_object = false;
        }
      }
    }

    if (should_add_object) {
      // check whether there are any elements at lower levels when adding this element. If so they should be removed.
      try {
        TLcdMultilevelGridUtil.multilevelCoordinateBoundsSFCT(aStatusedEditableMultilevelCoordinate, aModel.getMultilevelGrid(), fTempModelBounds);
        fRemoveElementFunction.setModel(aModel);
        fRemoveElementFunction.setMinLevel(aStatusedEditableMultilevelCoordinate.getCoordinateLevelCount());
        // we have to make the bounds slightly smaller, to avoid collisions with siblings.
        fTempModelBounds.translate2D(1E-4, 1E-4);
        fTempModelBounds.setWidth(fTempModelBounds.getWidth() - 2E-4);
        fTempModelBounds.setHeight(fTempModelBounds.getHeight() - 2E-4);
        aModel.applyOnInteract2DBounds(fTempModelBounds, true, fRemoveElementFunction, 0, 0);
        fRemoveElementFunction.setModel(null);
      } catch (TLcdNoBoundsException e1) {
        // this should not happen. If it does we do nothing and just return.
        return;
      }
      // check whether the siblings have the same status, if so we have to merge them.
      mergeAndAdd(aStatusedEditableMultilevelCoordinate, aModel);
    }
  }

  /**
   * Determines whether an coordinate should be merged with its siblings in the model.
   * This is only the case if all of its siblings exist and they all have the same status.
   * If the object does not need to be merged it is added to the model. Otherwise its parent is added to the model.
   * @param aStatusedEditableMultilevelCoordinate the coordinate to check.
   * @param aModel the model that contains the siblings.
   */
  private void mergeAndAdd(StatusedEditableMultilevelGridCoordinate aStatusedEditableMultilevelCoordinate,
                           TLcdMultilevelGridCoordinateModel aModel) {
    boolean should_merge = true;
    Vector elements_to_remove = new Vector();
    if (aStatusedEditableMultilevelCoordinate.getCoordinateLevelCount() == 1) {
      // we are at the highest level, we do not have to mergeAndAdd.
      should_merge = false;
    } else {
      int level = aStatusedEditableMultilevelCoordinate.getCoordinateLevelCount() - 1;
      // move the temp editable coordinate to the same location as the coordinate passed.
      fTempEditableCoordinate.setCoordinateLevelCount(level + 1);
      for (int level_index = 0; level_index < aStatusedEditableMultilevelCoordinate.getCoordinateLevelCount(); level_index++) {
        // we assume two axes.
        for (int axe_index = 0; axe_index < 2; axe_index++) {
          fTempEditableCoordinate.setCoordinate(level_index, axe_index, aStatusedEditableMultilevelCoordinate.getCoordinate(level_index, axe_index));
        }
      }
      int x_divisions = aModel.getMultilevelGrid().getDivisions(level, 0);
      int y_divisions = aModel.getMultilevelGrid().getDivisions(level, 0);
      for (int x_index = 0; x_index < x_divisions && should_merge; x_index++) {
        for (int y_index = 0; y_index < y_divisions && should_merge; y_index++) {
          if (
              (x_index != aStatusedEditableMultilevelCoordinate.getCoordinate(level, ILcdMultilevelGrid.X_AXIS)) ||
              (y_index != aStatusedEditableMultilevelCoordinate.getCoordinate(level, ILcdMultilevelGrid.Y_AXIS))
              ) {
            fTempEditableCoordinate.setCoordinate(level, 0, x_index);
            fTempEditableCoordinate.setCoordinate(level, 1, y_index);
            fTempVector.clear();
            aModel.elementsAtSFCT(fTempEditableCoordinate, fTempVector);
            // if there is no element, we do not have to mergeAndAdd.
            if (fTempVector.size() == 0) {
              should_merge = false;
            }
            for (int elements_at_coordinate_index = 0; elements_at_coordinate_index < fTempVector.size() && should_merge; elements_at_coordinate_index++) {
              StatusedEditableMultilevelGridCoordinate coordinate_in_model = (StatusedEditableMultilevelGridCoordinate) fTempVector.elementAt(elements_at_coordinate_index);
              should_merge = coordinate_in_model.getStatus().equals(aStatusedEditableMultilevelCoordinate.getStatus());
              elements_to_remove.addElement(coordinate_in_model);
            }
          }
        }
      }
    }

    if (!should_merge) {
      aModel.addElement(aStatusedEditableMultilevelCoordinate, ILcdFireEventMode.FIRE_NOW);
    } else {
      // remove all elements at the siblings ...
      for (int element_in_model_index = 0; element_in_model_index < elements_to_remove.size(); element_in_model_index++) {
        aModel.removeElement(elements_to_remove.elementAt(element_in_model_index), ILcdFireEventMode.FIRE_LATER);
      }
      // and add an element one level higher.
      aStatusedEditableMultilevelCoordinate.setCoordinateLevelCount(aStatusedEditableMultilevelCoordinate.getCoordinateLevelCount() - 1);
      addElement(aModel, aStatusedEditableMultilevelCoordinate);
    }
  }

  /**
   * Determines the level at which an element should be added. This implementation returns the highest level
   * painted in the given context.
   * @param aGridLayer the grid layer based on which the level is returned.
   * @param aGrid the grid with regard to which the coordinate is expressed.
   * @param aGXYContext the context in which the grid is painted.
   * @return the highest level rendered in the grid layer in the given context.
   */
  private int determineLevel(TLcdMultilevelGridLayer aGridLayer,
                             ILcdMultilevelGrid aGrid,
                             TLcdGXYContext aGXYContext) {
    int level = aGrid.getLevelCount() - 1;
    boolean level_ok = false;
    while (!level_ok && level >= 0) {
      level_ok = aGridLayer.isPaintLevel(level, aGXYContext);
      level--;
    }
    if (level_ok) {
      return ++level;
    } else {
      return -1;
    }
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
  }

  /**
   * A function that removes all objects that are passed from the model.
   */
  private class RemoveElementFunction implements ILcdFunction {

    private TLcdMultilevelGridCoordinateModel fModel;
    private int fMinLevel;

    public void setModel(TLcdMultilevelGridCoordinateModel aModel) {
      fModel = aModel;
    }

    public void setMinLevel(int aMinLevel) {
      fMinLevel = aMinLevel;
    }

    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      ILcdMultilevelGridCoordinate multilevelGridCoordinate = (ILcdMultilevelGridCoordinate) aObject;
      if (multilevelGridCoordinate.getCoordinateLevelCount() > fMinLevel) {
        fModel.removeElement(aObject, ILcdFireEventMode.FIRE_LATER);
      }
      return true;
    }
  }
}
