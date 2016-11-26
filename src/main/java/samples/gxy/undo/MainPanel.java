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
package samples.gxy.undo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.IOException;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdRedoAction;
import com.luciad.gui.TLcdUndoAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdInterval;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.common.action.ShowPopupAction;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates the undo support of LuciadLightspeed.
 * <p/>
 * It uses a polyline editor that generates ILcdUndoable instances for each edit,
 * so that these changes can be reverted.
 * The edit controller will take these undoables and inform its ILcdUndoableListener of
 * them. In this case, that listener is the TLcdUndoManager, which simply collects them
 * and keeps them in an ordered list. The TLcdUndoAction and
 * TLcdRedoAction then provide the user interface to tell the
 * TLcdUndoManager to undo the last done action and redo the last undone action.
 */
public class MainPanel extends GXYSample {

  private static final double MIN_SCALE = 0.00001; // scale ( 1:100000 )
  private static final double MAX_SCALE = 1; // scale ( 1:1 )

  private ILcdGXYLayer fPolylineLayer;

  @Override
  protected void createGUI() {
    super.createGUI();
    //create the undo manager
    TLcdUndoManager undoManager = new TLcdUndoManager(10);

    // Set up the actions that interact with the undo manager
    TLcdUndoAction undoAction = new TLcdUndoAction(undoManager);
    TLcdRedoAction redoAction = new TLcdRedoAction(undoManager);

    //insert these actions in the toolbar
    getToolBars()[0].addAction(undoAction);
    getToolBars()[0].addAction(redoAction);

    TLcdGXYEditController2 editController = getToolBars()[0].getGXYControllerEdit();
    configureUndoForEditController(undoManager, editController);
    TLcdGXYNewController2 newController = createNewController();
    configureUndoForNewController(undoManager, newController);
    getToolBars()[0].addSpace();
    getToolBars()[0].addGXYController(newController);
  }

  private void configureUndoForEditController(TLcdUndoManager aUndoManager, TLcdGXYEditController2 aEditController) {
    //Install a delete action that can be undone
    TLcdDeleteSelectionAction undoableDeleteAction = new TLcdDeleteSelectionAction(getView());
    undoableDeleteAction.addUndoableListener(aUndoManager);
    aEditController.setRightClickAction(new ShowPopupAction(
        new ILcdAction[]{undoableDeleteAction}, getView()
    ));

    //add the TLcdUndoManager as an ILcdUndoableListener to the edit controller.
    aEditController.addUndoableListener(aUndoManager);
    aEditController.setPanUndoable(false);
    aEditController.setZoomUndoable(false);
  }

  private void configureUndoForNewController(TLcdUndoManager aUndoManager, TLcdGXYNewController2 aNewController) {
    aNewController.addUndoableListener(aUndoManager);
  }

  private TLcdGXYNewController2 createNewController() {
    TLcdGXYNewController2 newController = new TLcdGXYNewController2(new ALcdGXYNewControllerModel2() {
      @Override
      public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return fPolylineLayer;
      }

      @Override
      public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
        return new Polyline(((TLcdGeodeticReference) fPolylineLayer.getModel().getModelReference()).getGeodeticDatum().getEllipsoid());
      }
    });
    newController.setActionToTriggerAfterCommit(new TLcdGXYSetControllerAction(getView(), getToolBars()[0].getGXYCompositeEditController()));
    newController.setIcon(TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON));
    newController.setShortDescription("Create a new polyline");
    return newController;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    fPolylineLayer = createPolylineLayer();
    // Since this is the layer that will be edited, it should not be regarded as a background layer,
    // as it will change frequently. 
    GXYLayerUtil.addGXYLayer(getView(), fPolylineLayer, false, false);
    GXYLayerUtil.fitGXYLayer(getView(), fPolylineLayer);
  }

  private ILcdGXYLayer createPolylineLayer() {
    TLcdGXYLayer layer = new TLcdGXYLayer(createPolylineModel());
    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setScaleRange(new TLcdInterval(MIN_SCALE, MAX_SCALE));
    layer.setGXYPen(new TLcdGeodeticPen());

    UndoablePointListPainter painter = new UndoablePointListPainter(TLcdGXYPointListPainter.POLYLINE);
    painter.setLineStyle(new TLcdG2DLineStyle(Color.green, Color.orange));

    layer.setGXYPainterProvider(painter);
    layer.setGXYEditorProvider(painter);
    return layer;
  }

  private ILcdModel createPolylineModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing an undoable/redoable polyline.",   // source name (is used as tooltip text)
        "MyPolyline",     // data type
        "Polyline"   // display name (user)
    ));

    model.addElement(createLonLatPolyline(datum.getEllipsoid()), ILcdFireEventMode.NO_EVENT);
    return model;
  }

  private Object createLonLatPolyline(ILcdEllipsoid aEllipsoid) {
    ILcd2DEditablePoint[] points = {new TLcdLonLatPoint(3.0, 50.1),
                                    new TLcdLonLatPoint(3.4, 50.4), new TLcdLonLatPoint(3.7, 50.8),
                                    new TLcdLonLatPoint(4.0, 50.9), new TLcdLonLatPoint(4.6, 51.2),
                                    new TLcdLonLatPoint(5.2, 51.9),};
    return new Polyline(new TLcd2DEditablePointList(points, false), aEllipsoid);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Undo sample");
  }
}
