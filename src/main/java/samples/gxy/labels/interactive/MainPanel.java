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
package samples.gxy.labels.interactive;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYStampLabelPainter;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYInteractiveLabelsController;
import com.luciad.view.gxy.labeling.TLcdGXYAsynchronousLabelPlacer;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.controller.EditPanController;
import samples.gxy.common.labels.LastPlacedPriorityLabelingAlgorithm;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample demonstrates the editable labels of LuciadLightspeed. The labels are editable in the sense
 * that they can be moved to a new location, as well as in the sense that they offer the user an
 * interactive component that can be used to edit information related to the domain objects.
 *
 * This sample also demonstrates the possibility to use Swing/AWT components to paint the labels.
 */
public class MainPanel extends GXYSample {

  private TLcdGXYLayer fCitiesLayer;
  private InteractiveSwingLabelComponent fInteractiveLabelComponent;
  private RegularSwingLabelComponent fRegularComponent;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // This Map instance is shared between
    // - the InteractiveSwingLabelComponent instance
    // - the RegularSwingLabelComponent instance
    // This is done to make it possible for the interactive label to also edit the comment of the regular label.
    Map<Object, String> cities_comment_map = new HashMap<Object, String>();
    fInteractiveLabelComponent = new InteractiveSwingLabelComponent(cities_comment_map);
    fRegularComponent = new RegularSwingLabelComponent(cities_comment_map);

    ILcdGXYController interactive_labels_controller = createInteractiveLabelsController();
    getToolBars()[0].addGXYController(interactive_labels_controller);
    getView().setGXYController(getToolBars()[0].getGXYController(interactive_labels_controller));

    // We use LastPlacedPriorityLabelingAlgorithm to avoid minor problems like labels being removed
    // by the label placer, right after editing them. Note: using this algorithm is not necessary
    // to make interactive labels work.
    LastPlacedPriorityLabelingAlgorithm algorithm = new LastPlacedPriorityLabelingAlgorithm();
    TLcdGXYAsynchronousLabelPlacer label_placer = new TLcdGXYAsynchronousLabelPlacer(algorithm);
    getView().setGXYViewLabelPlacer(label_placer);
  }

  private ILcdGXYController createInteractiveLabelsController() {
    SwingInteractiveLabelProvider interactive_label_provider = new SwingInteractiveLabelProvider(fInteractiveLabelComponent) {
      public boolean canProvideInteractiveLabel(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
        // Only enable interactive labels for labels of fCitiesLayer
        return super.canProvideInteractiveLabel(aObject, aLabelIndex, aSubLabelIndex, aGXYContext) &&
               aGXYContext.getGXYLayer() == fCitiesLayer;
      }
    };

    TLcdGXYInteractiveLabelsController interactive_labels_controller = new TLcdGXYInteractiveLabelsController(interactive_label_provider);

    TLcdGXYCompositeController composite_controller = new TLcdGXYCompositeController();
    composite_controller.addGXYController(interactive_labels_controller);
    composite_controller.addGXYController(new EditPanController(createEditController()));

    return composite_controller;
  }

  protected void addData() throws IOException {
    super.addData();
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView());
    ILcdGXYLayer cities = GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).getLayer();
    fCitiesLayer = (TLcdGXYLayer) cities;
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        setupInteractiveLabels(fCitiesLayer);
      }
    });
  }

  /**
   * Creates an edit controller that makes sure the label over which the mouse hovers
   * (the interactive label) is kept sticky.
   *
   * This is especially needed if there is a difference in size between the interactive
   * label and the regular labels, or if the labeled objects are moving.
   *
   * If there is no difference in size nor moving objects are involved (the sample default),
   * a regular TLcdGXYEditController2 could be used as well.
   *
   * @return The edit controller
   */
  private TLcdGXYEditController2 createEditController() {
    TLcdGXYEditController2 edit_controller = new TLcdGXYEditController2();
    // instant editing ensures that we don't see the old label's pin while moving the interactive label
    edit_controller.setInstantEditing(true);
    // prevent the decluttering algorithm from moving the interactive label
    edit_controller.setStickyLabelsLayerFilter(new ILcdFilter() {
      public boolean accept(Object aObject) {
        return aObject == fCitiesLayer;
      }
    });
    return edit_controller;
  }

  /**
   * Sets up interactive labeling on the given layer.
   * @param aCitiesLayer The layer to setup.
   */
  private void setupInteractiveLabels(final TLcdGXYLayer aCitiesLayer) {
    // Create a label painter for the regular labels
    TLcdGXYStampLabelPainter regular_label_painter = new TLcdGXYStampLabelPainter(new SwingGXYLabelStamp() {
      protected Component retrieveLabelComponent(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aContext) {
        fRegularComponent.setObject(aObject);
        return fRegularComponent;
      }
    });
    regular_label_painter.setWithPin(true);

    // Create a label painter for the interactive labels
    TLcdGXYStampLabelPainter interactive_label_painter = new TLcdGXYStampLabelPainter(new SwingGXYLabelStamp() {
      protected Component retrieveLabelComponent(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aContext) {
        return fInteractiveLabelComponent;
      }
    });
    interactive_label_painter.setWithPin(true);

    // Create the interactive label painter that delegates to the regular label painter for regular
    // labels and to the interactive label painter for interactive labels.
    CompositeLabelPainter composite = new CompositeLabelPainter(regular_label_painter, interactive_label_painter) {
      protected ILcdGXYLabelPainter2 getLabelPainter() {
        boolean is_interactive = getLabelLocation() != null && getLabelLocation().isInteractiveLabel();
        return is_interactive ? getLabelPainter2() : getLabelPainter1();
      }
    };

    //Set the label painter/editor to the layer
    aCitiesLayer.setGXYLabelPainterProvider(composite);
    aCitiesLayer.setGXYLabelEditorProvider(composite);

    //Fine tune the layer
    aCitiesLayer.setLabeled(true);
    aCitiesLayer.setSelectable(true);
    aCitiesLayer.setLabelsEditable(true);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Interactive labels");
  }
}
