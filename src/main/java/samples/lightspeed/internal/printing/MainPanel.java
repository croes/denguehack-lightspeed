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
package samples.lightspeed.internal.printing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.integration.gxy.TLspGXYLayerAdapter;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.density.TLspDensityPainter;
import com.luciad.view.lightspeed.painter.grid.TLspMGRSGridLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.painter.shape.TLspShapePaintingHints;
import com.luciad.view.lightspeed.style.*;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.labels.interactive.InteractiveLabelLayerFactory;
import samples.lightspeed.labels.interactive.InteractiveLabelProvider;
import samples.lightspeed.labels.interactive.InteractiveSwingLabelComponent;
import samples.lightspeed.labels.interactive.RegularSwingLabelComponent;
import samples.lightspeed.labels.placement.RiversLayerFactory;
import samples.lightspeed.printing.PrintAction;

/**
 * Sample for testing all printing features.
 */
@SuppressWarnings("deprecation")
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  private CreateAndEditToolBar fCreateAndEditToolBar;
  private ILspInteractivePaintableLayer fShapesLayer;
  private HashSet<String> fMainLayers;

  public MainPanel(String[] aArgs) {
    super(aArgs);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    for (ILspLayer layer : Collections.list((Enumeration<ILspLayer>) getView().layers())) {
      registerAsMainLayer(layer);
    }

    TLcdCompositeModelDecoder modelDecoder = new TLcdCompositeModelDecoder(ServiceRegistry.getInstance().query(ILcdModelDecoder.class));

    getView().addModel(modelDecoder.decode("Data/Dted/Alps/dmed"));

    ILcdModel animatedModel = ModelFactory.createAnimatedModel();
    ILspInteractivePaintableLayer animatedLayer = TLspShapeLayerBuilder.newBuilder().selectable(false).bodyEditable(false).bodyStyles(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 32, Color.black, Color.red)).build()).model(animatedModel).build();
    getView().addLayer(animatedLayer);

    ILcdModel linesModel = modelDecoder.decode("Data/Shp/Europe/europe.shp");
    ILspInteractivePaintableLayer pixelLinesLayer = TLspShapeLayerBuilder.newBuilder().
        model(linesModel).
                                                                             label("Pixel lines").
                                                                             labelStyler(
                                                                                 TLspPaintState.REGULAR,
                                                                                 TLspLabelStyler.newBuilder().
                                                                                     styles(
                                                                                         TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("COUNTRY").build(),
                                                                                         TLspTextStyle.newBuilder().build(),
                                                                                         TLspPinLineStyle.newBuilder().dashPattern(new TLspLineStyle.DashPattern(TLspLineStyle.DashPattern.DOT, 1)).build(),
                                                                                         TLspLabelBoxStyle.newBuilder().filled(true).fillColor(new Color(0x88858585, true)).frameThickness(1).frameColor(Color.RED).build()
                                                                                     ).
                                                                                                    locations(10, TLspLabelLocationProvider.Location.values()).
                                                                                                    build()
                                                                             ).
                                                                             build();
    getView().addLayer(pixelLinesLayer);

    TLspGXYLayerAdapter gxyPixelLinesLayer = LayerFactory.createGXYLayerAdapter(pixelLinesLayer);
    gxyPixelLinesLayer.setVisible(false);
    getView().addLayer(gxyPixelLinesLayer);

    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            model(ModelFactory.createLinesModel(0)).
                                 label("Offset lines").
                                 bodyStyles(TLspPaintState.REGULAR, TLspLineStyle.newBuilder().elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).width(2f).pixelOffset(2).color(Color.RED).opacity(.6f).build()).
                                 build()
    );

    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            model(ModelFactory.createLinesModel(1)).
                                 label("World lines").
                                 bodyStyles(TLspPaintState.REGULAR, TLspWorldSizedLineStyle.newBuilder().elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN).width(50e3).color(Color.RED).opacity(.6f).build()).
                                 build()
    );

    ILcdModel riversModel = modelDecoder.decode("Data/Shp/Usa/rivers.shp");
    ILspLayer riversLayer = new RiversLayerFactory().createLayer(riversModel);
    riversLayer.setLabel("Labels");
    getView().addLayer(riversLayer);

    ILcdModel cityModel = modelDecoder.decode("Data/Shp/Usa/city_125.shp");
    Map<Object, String> cityComments = new TLcdWeakIdentityHashMap<>();
    RegularSwingLabelComponent regularComponent = new RegularSwingLabelComponent(cityComments);
    InteractiveLabelProvider labelProvider = new InteractiveLabelProvider(new InteractiveSwingLabelComponent(cityComments));
    InteractiveLabelLayerFactory layerFactory = new InteractiveLabelLayerFactory(labelProvider, regularComponent, new InteractiveSwingLabelComponent(cityComments), cityComments);
    ILspLayer layer = layerFactory.createLayers(cityModel).iterator().next();
    layer.setLabel("Swing labels");
    getView().addLayer(layer);

    TLspDensityPainter painter = new TLspDensityPainter(ILspWorldElevationStyle.ElevationMode.OBJECT_DEPENDENT);
    painter.setStyler(TLspPaintState.REGULAR, TLspDensityPointStyle.newBuilder().pixelSize(32).hardness(0.5).build());
    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            label("Density").
                                 model(cityModel).
                                 bodyPainter(painter).
                                 build()
    );

    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            label("Cities").
                                 model(cityModel).
                                 bodyStyles(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 16, Color.BLACK, Color.WHITE)).opacity(.5f).build()).
                                 build()
    );

    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            model(ModelFactory.createVerticalLinesModel()).
                                 bodyStyler(TLspPaintState.REGULAR, new PointDomainObject.PointDomainObjectStyler()).
                                 build()
    );

    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            model(ModelFactory.create3dIconsModel(getView())).
                                 bodyStyler(TLspPaintState.REGULAR, new PointDomainObject.PointDomainObjectStyler()).
                                 build()
    );

    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            model(ModelFactory.createVariousIconsModel()).
                                 bodyStyler(TLspPaintState.REGULAR, new PointDomainObject.PointDomainObjectStyler()).build()
    );
    TLcdSymbol symbol = new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 32, Color.black, Color.orange);
    TLcdSymbol symbol2 = new TLcdSymbol(TLcdSymbol.FILLED_RECT, 8, Color.gray, Color.orange);
    TLcdSymbol symbol3 = new TLcdSymbol(TLcdSymbol.OUTLINED_AREA, 16, Color.white, Color.gray);
    TLcdSymbol symbol4 = new TLcdSymbol(TLcdSymbol.PLUS_RECT, 24, Color.white, Color.red);
    BufferedImage bufferedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    symbol.paintIcon(null, graphics, 0, 0);
    graphics.dispose();
    getView().addLayer(
        TLspShapeLayerBuilder.newBuilder().
            model(ModelFactory.createPolygonFillLayer()).
                                 bodyStyles(TLspPaintState.REGULAR, TLspFillStyle.newBuilder()
                                                                                 .textureCoordinatesMode(ILspTexturedStyle.TextureCoordinatesMode.OBJECT_RELATIVE)
                                                                                 .texture(bufferedImage)
                                                                                 .repeatTexture(true)
                                                                                 .stipplePattern(TLspFillStyle.StipplePattern.HATCHED)
                                                                                 .build()).build()
    );

    InputStream inputStream = new TLcdInputStreamFactory().createInputStream("samples/lightspeed/demo/icons/globe.png");
    getView().addLayer(TLspShapeLayerBuilder.newBuilder()
                                            .model(ModelFactory.createStrokedLinesModel(0, 0))
                                            .label("Stroked lines")
                                            .bodyStyles(TLspPaintState.REGULAR, TLspStrokedLineStyle.newBuilder()
                                                                                                    .text(" w ", Color.white)
                                                                                                    .text(" - ", Color.white)
                                                                                                    .text(" + ", Color.orange)
                                                                                                    .text(" o ", Color.white)
                                                                                                    .text(" + ", Color.orange)
                                                                                                    .icon(symbol)
                                                                                                    .space(5)
                                                                                                    .icon(symbol2)
                                                                                                    .space(5)
                                                                                                    .icon(symbol3)
                                                                                                    .space(5)
                                                                                                    .icon(symbol4)
                                                                                                    .space(5)
                                                                                                    .icon(symbol)
                                                                                                    .shape(new Ellipse2D.Double(0, 0, 32, 32), Color.red, Color.white, 10)
                                                                                                    .shape(new Ellipse2D.Double(0, 0, 32, 32), Color.red)
                                                                                                    .shape(new Ellipse2D.Double(0, 0, 32, 32), Color.orange, null, 0)
                                                                                                    .image(ImageIO.read(inputStream))
                                                                                                    .build()
                                            ).build());
    inputStream.close();

    getView().addLayer(TLspShapeLayerBuilder.newBuilder()
                                            .model(ModelFactory.createStrokedLinesModel(5, 0))
                                            .label("Dash patterns")
                                            .bodyStyles(TLspPaintState.REGULAR, TLspLineStyle.newBuilder()
                                                                                             .dashPattern(new TLspLineStyle.DashPattern(TLspLineStyle.DashPattern.LONG_SHORT_DASH, 5))
                                                                                             .color(Color.red)
                                                                                             .width(2.0)
                                                                                             .build()
                                            ).build());

    ILcdModel dc = modelDecoder.decode("Data/Shp/Dc/streets.shp");
    getView().addLayer(TLspShapeLayerBuilder.newBuilder()
                                            .model(dc)
                                            .minimumObjectSizeForPainting(50)
                                            .label("MinObjectSize")
                                            .build());

    getView().addLayer(fShapesLayer);
    registerAsMainLayer(fShapesLayer);

    getView().addLayer(TLspMGRSGridLayerBuilder.newBuilder().build());

    getView().addLayer(TLspShapeLayerBuilder.newBuilder()
                                            .model(ModelFactory.createPaintInViewModel())
                                            .build());
  }

  private void registerAsMainLayer(ILspLayer aLayer) {
    if (fMainLayers == null) {
      fMainLayers = new HashSet<String>();
    }
    fMainLayers.add(aLayer.getLabel());
  }

  private boolean isMainLayer(ILspLayer aLayer) {
    return fMainLayers != null && fMainLayers.contains(aLayer.getLabel());
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Disable layers
    ALcdAction disableLayersAction = new ALcdAction("Disable layers", new TLcdImageIcon("images/gui/i16_filter.gif")) {
      @Override
      public void actionPerformed(ActionEvent e) {
        for (ILspLayer layer : Collections.list((Enumeration<ILspLayer>) getView().layers())) {
          if (!isMainLayer(layer)) {
            layer.setVisible(false);
          }
        }
      }
    };

    // Nice print and print preview
    PrintAction nicePrintAction = new PrintAction(this, getView());
    samples.lightspeed.printing.PrintPreviewAction nicePrintPreviewAction = new samples.lightspeed.printing.PrintPreviewAction(this, getView());

    // Print preview
    final PrintPreviewAction action = new PrintPreviewAction((ALspAWTView) getView());
    final A4PrintPreviewAction actionA4 = new A4PrintPreviewAction((ALspAWTView) getView());
    final JSpinner previewScaleSpinner = createSpinner(1, 1, 8, 1, "Rasterization scale");
    final JSpinner previewFeatureScaleSpinner = createSpinner(1, 0.25, 2.0, 0.25, "Feature scale");
    previewScaleSpinner.getModel().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        action.setScaleFactor((Double) previewScaleSpinner.getValue());
        actionA4.setScaleFactor((Double) previewScaleSpinner.getValue());
      }
    });
    previewFeatureScaleSpinner.getModel().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        action.setFeatureScaleFactor((Double) previewFeatureScaleSpinner.getValue());
        actionA4.setFeatureScaleFactor((Double) previewFeatureScaleSpinner.getValue());
      }
    });

    ToolBar toolBar = getToolBars()[0];
    toolBar.addSpace();
    toolBar.addAction(disableLayersAction);
    toolBar.addSpace();
    toolBar.addAction(nicePrintAction);
    toolBar.addAction(nicePrintPreviewAction);
    toolBar.addSpace();
    toolBar.addComponent(previewScaleSpinner);
    toolBar.addComponent(previewFeatureScaleSpinner);
    toolBar.addAction(action);
    toolBar.addAction(actionA4);
  }

  private JSpinner createSpinner(double aValue, double aMinimum, double aMaximum, double aStep, String aToolTipText) {
    JSpinner viewScaleSpinner = new JSpinner(new SpinnerNumberModel(aValue, aMinimum, aMaximum, aStep));
    viewScaleSpinner.setMaximumSize(new Dimension(viewScaleSpinner.getPreferredSize().width * 2, viewScaleSpinner.getMaximumSize().height));
    viewScaleSpinner.setToolTipText(aToolTipText);
    return viewScaleSpinner;
  }

  @Override
  protected ToolBar[] createToolBars(ILspAWTView aView) {
    final ToolBar regularToolBar = new ToolBar(aView, this, true, true);

    ILspController defaultController = regularToolBar.getDefaultController();

    if (fCreateAndEditToolBar == null) {
      fShapesLayer = createShapesLayer();
      fCreateAndEditToolBar = new CreateAndEditToolBar(aView, this,
                                                       regularToolBar.getButtonGroup(),
                                                       true,
                                                       false,
                                                       false,
                                                       fShapesLayer) {
        @Override
        protected ILspController createDefaultController() {
          return regularToolBar.getDefaultController();
        }
      };
    }

    getView().setController(defaultController);

    return new ToolBar[]{regularToolBar, fCreateAndEditToolBar};
  }

  private ILspInteractivePaintableLayer createShapesLayer() {
    ILcdModel shapesModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("Shapes", "Shapes", "Shapes"));
    return TLspShapeLayerBuilder.newBuilder().model(shapesModel).bodyEditable(true).paintingHints(TLspShapePaintingHints.MAX_QUALITY).build();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, aArgs, "Printing");
  }

}
