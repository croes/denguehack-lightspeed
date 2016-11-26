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
package samples.gxy.painterstyles;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdSelection;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;

import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates several techniques available in LuciadLightspeed to render
 * shape outlines and fill patterns.
 * <p/>
 * The functionality to paint an ILcdPointList, like a polyline or polygon, with
 * rounded corners is provided by TLcdGXYRoundedPointListPainter.
 * The roundness of the corners can be adjusted in the sample, by selecting
 * a shape and dragging the slider on top of the configuration panel.
 * <p/>
 * The sample uses a variety of painter styles that can be configured individually for each object.
 * The styles include TLcdGXYColorPainterStyle,
 * TLcdStrokeLineStyle, TLcdGXYHatchedFillStyle, and a custom style based on a pattern-based stroke.
 * To create the pattern-based stroke, TLcdGXYComplexStroke is used.
 * This java.awt.Stroke implementation allows to use a Shape array as pattern to
 * render a shape. The sample illustrates how two different patterns can be combined
 * in one TLcdGXYComplexStroke and how its behavior can be configured through several settings.
 * <p/>
 * The sample also illustrates halo effects.
 * A halo is an outline of constant width and color around shapes or text, which is typically drawn in
 * a contrasting color to ensure that the shapes or text are clearly visible on any background.
 * For shapes, the halo effect is obtained by wrapping an ILcdGXYPainter (in this case,
 * the TLcdGXYRoundedPointListPainter) into a TLcdGXYHaloPainter. This painter allows
 * to control the halo color and thickness, as illustrated by the sample in the
 * configuration panel.
 * <p/>
 * The sample shows a polyline and polygon that are rendered with rounded corners,
 * a pattern-based stroke, a hatched fill pattern and a halo.
 * New polylines can be added to the view via a button in the toolbar.
 * This button sets an instance of TLcdGXYNewController on the view as active controller,
 * to draw a new polyline. The newly created polyline is painted with the current settings
 * of the style configuration panel.
 */
public class MainPanel extends GXYSample {

  private ILcdGXYLayer fStyledLayer = new StyledShapeLayer();

  private StyledShapeCustomizer fStyleCustomizer = new StyledShapeCustomizer();

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(3.00, 50.60, 1.00, 1.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    // Increase the selection sensitivity to allow easier selection of the thick lines.
    getToolBars()[0].getGXYControllerEdit().getSelectControllerModel().setSensitivity(7);
    // Increase the editing sensitivity to allow easier modification of the thick lines.
    getToolBars()[0].getGXYControllerEdit().getEditControllerModel().setSensitivity(7);
    // Create a polyline new controller and add it to the toolbar
    TLcdGXYNewController2 newPolylineController = new TLcdGXYNewController2(
        new MyNewStyledPolylineControllerModel());
    newPolylineController.setIcon(TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON)); // Set the icon
    newPolylineController.setShortDescription("Create a polyline");               // Set the short descriptions
    newPolylineController.setActionToTriggerAfterCommit(                            // Switch to edit mode after creating the ILcdShape
                                                                                    new TLcdGXYSetControllerAction(getView(), getToolBars()[0].getGXYCompositeEditController())
                                                       );
    getToolBars()[0].addGXYController(newPolylineController);
  }

  @Override
  protected JPanel createSettingsPanel() {
    return fStyleCustomizer;
  }

  @Override
  protected LayerControlPanel createLayerPanel() {
    return null; // more room for the settings panel
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Add the styled layer to the map.
    GXYLayerUtil.addGXYLayer(getView(), fStyledLayer, true, false);

    // Register a selection listener to update the customizer panel.
    fStyledLayer.addSelectionListener(new ILcdSelectionListener() {
      public void selectionChanged(TLcdSelectionChangedEvent aSelectionChangedEvent) {
        ILcdSelection selection = aSelectionChangedEvent.getSelection();
        if (selection.getSelectionCount() == 1) {
          Object object = selection.selectedObjects().nextElement();
          if (object instanceof StyledShape) {
            fStyleCustomizer.setObject(new TLcdDomainObjectContext(
                object, fStyledLayer.getModel(),
                fStyledLayer, getView()));
          } else {
            fStyleCustomizer.setObject(null);
          }
        } else {
          fStyleCustomizer.setObject(null);
        }
      }
    });
  }

  private class MyNewStyledPolylineControllerModel extends ALcdGXYNewControllerModel2 {

    @Override
    public ILcdGXYLayer getGXYLayer(Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
      return fStyledLayer;
    }

    @Override
    public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
      // fStyledLayer's model is geodetic, so its ILcdModelReference implements ILcdGeoReference.
      ILcdGeoReference geoReference = (ILcdGeoReference) aContext.getGXYLayer().getModel().getModelReference();
      ILcdEllipsoid ellipsoid = geoReference.getGeodeticDatum().getEllipsoid();

      // The polyline is initially empty.
      // The new controller will call the controller model's edit method for every point that needs to be inserted.
      StyledPolyline styledPolyline = new StyledPolyline(new TLcd2DEditablePointList(), ellipsoid);
      fStyleCustomizer.initializeObject(styledPolyline);
      return styledPolyline;
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Painter styles");
  }
}
