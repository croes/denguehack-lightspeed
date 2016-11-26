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
package samples.lightspeed.nongeoreferenced;

import static samples.common.SwingUtil.createButtonForAction;

import java.awt.Component;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerDistanceFormatStyle;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerLabelStyler;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridLayerBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspXYGridStyle;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.painter.shape.TLspShapePaintingHints;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.common.formatsupport.OpenAction;
import samples.common.model.CartesianReference;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.LspOpenSupport;
import samples.lightspeed.common.LuciadLogoIcon;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.grid.XYGridStyleFactory;

/**
 * Demonstrates visualizing data that is not geographically referenced.
 * <p/>
 * You can create a non-geographically referenced view by creating a {@link CartesianReference
 * custom reference} and setting it as both the model's reference and the view's world reference.
 */
public class MainPanel extends LightspeedSample {

  private CartesianCreateAndEditToolBar fCreateAndEditToolBar;

  @Override
  protected ILspAWTView createView() {
    return TLspViewBuilder.newBuilder().
        viewType(ILspView.ViewType.VIEW_2D).
                              worldReference(CartesianReference.getInstance()).
                              buildAWTView();
  }

  @Override
  protected void addData() throws IOException {
    ILspLayer gridLayer = createXYGridLayer();
    getView().addLayer(gridLayer);

    LspDataUtil.instance().model("Data/Rst/wash_spot_small.gif", new SimpleImageModelDecoder()).layer().label("Raster").addToView(getView()).fit();
    getView().addLayer(fCreateAndEditToolBar.getCreationLayer());
  }

  private ILspLayer createXYGridLayer() {
    ILcdModel gridModel = TLspXYGridLayerBuilder.createModel("XY Grid", CartesianReference.getInstance());
    TLspXYGridStyle gridStyle = XYGridStyleFactory.createGridStyle(false);
    gridStyle = gridStyle.asBuilder().clampToBounds(new TLcdXYBounds(0, 0, 1000, 1000)).build();
    return TLspXYGridLayerBuilder.newBuilder().model(gridModel).style(gridStyle).build();
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    LspOpenSupport openSupport = new LspOpenSupport(getView(), Collections.singleton(new SimpleImageModelDecoder()));
    openSupport.addStatusListener(getStatusBar());
    ToolBar toolBar = getToolBars()[0];
    toolBar.addAction(new OpenAction(openSupport), ToolBar.FILE_GROUP);
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    ToolBar regularToolBar = new ToolBar(aView, this, false, false) {
      @Override
      protected TLspRulerController createRulerController() {
        return createCartesianRulerController();
      }
    };

    ILspController defaultController = regularToolBar.getDefaultController();

    if (fCreateAndEditToolBar == null) {
      ILspInteractivePaintableLayer shapesLayer = createAnnotationsLayer();
      fCreateAndEditToolBar = new CartesianCreateAndEditToolBar(aView, this, regularToolBar, shapesLayer);
    }

    getView().setController(defaultController);

    return new ToolBar[]{regularToolBar, fCreateAndEditToolBar};
  }

  private TLspRulerController createCartesianRulerController() {
    TLspRulerController rulerController = new TLspRulerController();
    rulerController.setMeasureMode(TLspRulerController.MeasureMode.MEASURE_CARTESIAN);
    ILspStyler labelStyler = rulerController.getLabelStyler();
    if (labelStyler instanceof TLspRulerLabelStyler) {
      for ( TLspCustomizableStyle style : ((TLspRulerLabelStyler) labelStyler).getStyles() ) {
        // replace the distance formats; we just show pixel values
        if (style.getStyle() instanceof TLspRulerDistanceFormatStyle) {
          NumberFormat instance = DecimalFormat.getInstance();
          instance.setMaximumFractionDigits(0);
          style.setStyle(((TLspRulerDistanceFormatStyle) style.getStyle()).asBuilder().distanceFormat(instance).build());
        }
      }
    }
    return rulerController;
  }

  @Override
  protected void addOverlayComponents(JComponent aOverlayPanel) {
    TLcdOverlayLayout layout = (TLcdOverlayLayout) aOverlayPanel.getLayout();

    addNavigationControls(aOverlayPanel);

    JLabel luciadLogo = new JLabel(new LuciadLogoIcon());
    aOverlayPanel.add(luciadLogo);
    layout.putConstraint(luciadLogo, TLcdOverlayLayout.Location.SOUTH_WEST, TLcdOverlayLayout.ResolveClash.VERTICAL);

    if (getView() instanceof ALspAWTView) {
      aOverlayPanel.add(new CartesianMouseLocationComponent((ALspAWTView) getView()), TLcdOverlayLayout.Location.SOUTH);
    }

    AbstractButton fullScreenRestoreButton = createButtonForAction(this, getFullScreenAction().getRestoreAction(), false);
    aOverlayPanel.add(fullScreenRestoreButton);
    layout.putConstraint(fullScreenRestoreButton, TLcdOverlayLayout.Location.NORTH_WEST, TLcdOverlayLayout.ResolveClash.VERTICAL);
  }

  private ILspInteractivePaintableLayer createAnnotationsLayer() {
    return TLspShapeLayerBuilder.newBuilder().
        model(ModelFactory.createAnnotationsModel()).
                                    bodyEditable(true).
                                    labelStyler(TLspPaintState.REGULAR, TLspLabelStyler.newBuilder().styles(new ALspLabelTextProviderStyle() {
                                      @Override
                                      public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
                                        ILcdPoint point = ((ILcdShape) aDomainObject).getFocusPoint();
                                        return new String[]{String.format("(%d,%d)", (int) point.getX(), (int) point.getY())};
                                      }
                                    }).build()).
                                    paintingHints(TLspShapePaintingHints.MAX_QUALITY).
                                    build();
  }

  public static void main(String[] aArgs) {
    startSample(MainPanel.class, "Non geographically referenced data");
  }

}
