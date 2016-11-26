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
package samples.lightspeed.plots;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspLabelingAlgorithm;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.plots.TLspPlotLayerBuilder;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ViewScaleThresholdListener;
import samples.lightspeed.plots.datamodelstyling.DataTypeStyler;
import samples.lightspeed.plots.datamodelstyling.EnumAnnotation;
import samples.lightspeed.plots.datamodelstyling.EnumAttributeProvider;
import samples.lightspeed.plots.datamodelstyling.PlotFilterPanel;
import samples.lightspeed.plots.datamodelstyling.PlotStylePanel;
import samples.lightspeed.plots.datamodelstyling.RangeAnnotation;
import samples.lightspeed.plots.datamodelstyling.RangeAttributeProvider;

/**
 * The sample loads 500.000 aircraft positions around New York.
 * <p>
 *   The plots are loaded in {@link #addData()} and
 *   are painted with a plot layer ({@link TLspPlotLayerBuilder}).
 * </p>
 * <p>
 *   The data model contains several useful properties but it does not indicate which ones are enumerations and which ones are ranges, so we add that information.
 *   This sample extends the decoded data model with annotations that can be used for automatic styling:
 *   <ul>
 *     <li>For <i>ranged</i> properties (such as time, speed), a {@link RangeAnnotation} is added</li>
 *     <li>For properties with <i>limited values</i> (such as class) an {@link EnumAnnotation} is added</li>
 *   </ul>
 * </p>
 * <p>
 *   Based on these {@link TLcdDataProperty} annotations, {@link DataTypeStyler} can automatically
 *   create a {@link com.luciad.view.lightspeed.style.TLspPlotStyle}.  It also uses these annotations to populate the necessary attributes,
 *   see {@link EnumAttributeProvider} and {@link RangeAttributeProvider}.
 * </p>
 * <p>
 *   See {@link DataTypeStyler#updateStyle()} for the actual creation of a {@link com.luciad.view.lightspeed.style.TLspPlotStyle}.
 * </p>
 * <p>
 *   The icons are also scaled automatically based on the view scale, see {@link com.luciad.view.lightspeed.style.TLspPlotStyle.Builder#automaticScaling(double)}.
 * </p>
 * <p>
 *   Also based on these {@link TLcdDataProperty} annotations, a filter GUI ({@link PlotFilterPanel}),
 *   styling GUI ({@link PlotStylePanel}) and legend GUI ({@link LegendPanel}) are created.
 * </p>
 */
public class MainPanel extends LightspeedSample implements ChangeListener {

  private PlotFilterPanel fPlotFilterPanel;
  private PlotStylePanel fPlotStylePanel;
  private LegendPanel fLegendPanel;

  @Override
  protected void addData() throws IOException {
    super.addData();

    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());

    // Decode plots model
    TLcdSHPModelDecoder modelDecoder = new TLcdSHPModelDecoder("plots");
    ILcdModel model = modelDecoder.decode("Data/Shp/Tracks/500k/tracks_500K.shp");

    // Prepare styler and GUI based on data model
    TLcdDataModel dataModel = ((ILcdDataModelDescriptor) model.getModelDescriptor()).getDataModel();
    TLcdDataType dataType = dataModel.getDeclaredType("plotsType");
    annotateDataModel(model, dataType);

    DataTypeStyler styler = new DataTypeStyler(dataType);

    final HeadingStyler headingStyler = new HeadingStyler(styler, dataType.getProperty("Heading"));

    ViewScaleThresholdListener.attach(getView(), 0.04, new ViewScaleThresholdListener.ThresholdListener() {
      @Override
      public void thresholdChanged(boolean aBelowThreshold) {
        headingStyler.setEnabled(aBelowThreshold);
      }
    });

    fLegendPanel.initialize(styler);
    fPlotFilterPanel.initialize(dataType, styler);
    fPlotStylePanel.initialize(dataType, styler);

    // Add plots layer to the view
    ILspStyledLayer layer = TLspPlotLayerBuilder.newBuilder()
                                                .model(model)
                                                .mandatoryAttributes(styler.getAttributes())
                                                .mandatoryOrientation(true)
                                                .bodyStyler(TLspPaintState.REGULAR, headingStyler)
                                                .labelScaleRange(new TLcdInterval(0.5, Double.MAX_VALUE))
                                                .labelStyler(TLspPaintState.REGULAR, createLabelStyler())
                                                .build();

    getView().addLayer(layer);
    FitUtil.fitOnLayers(this, layer);
  }

  private TLspLabelStyler createLabelStyler() {
    return TLspLabelStyler.newBuilder()
                          .algorithm(new TLspLabelingAlgorithm(new TLspLabelLocationProvider(10)))
                          .group(TLspLabelPlacer.DEFAULT_REALTIME_GROUP)
                          .styles(TLspTextStyle.newBuilder().build(), TLspPinLineStyle.newBuilder().build())
                          .build();
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    fLegendPanel.updateLegend();
  }

  /**
   * Computes {@link samples.lightspeed.plots.datamodelstyling.RangeAnnotation}s and {@link samples.lightspeed.plots.datamodelstyling.EnumAnnotation}s for the different
   * properties of the data model.
   */
  private void annotateDataModel(ILcdModel aModel, TLcdDataType aDataType) {
    List<TLcdDataProperty> properties = aDataType.getProperties();

    EnumAnnotation.Builder<Object>[] enumBuilders = new EnumAnnotation.Builder[properties.size()];
    RangeAnnotation.Builder<Number>[] rangeBuilders = new RangeAnnotation.Builder[properties.size()];

    for (int i = 0; i < properties.size(); i++) {
      enumBuilders[i] = EnumAnnotation.newBuilder();
      rangeBuilders[i] = RangeAnnotation.newBuilder();
    }

    Enumeration elements = aModel.elements();
    while (elements.hasMoreElements()) {
      ILcdPoint point = (ILcdPoint) elements.nextElement();
      ILcdDataObject dataObject = (ILcdDataObject) point;
      for (int i = 0; i < properties.size(); i++) {
        TLcdDataProperty property = properties.get(i);
        Object value = dataObject.getValue(property);
        enumBuilders[i].accumulate(value);
        if (value instanceof Number) {
          rangeBuilders[i].accumulate((Number) value);
        }
      }
    }

    for (int i = 0; i < properties.size(); i++) {
      if (enumBuilders[i].build() != null) {
        properties.get(i).addAnnotation(enumBuilders[i].build());
      }
      if (rangeBuilders[i].build() != null) {
        properties.get(i).addAnnotation(rangeBuilders[i].build());
      }
    }
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    fPlotFilterPanel = new PlotFilterPanel();
    fPlotFilterPanel.addChangeListener(this);
    fPlotStylePanel = new PlotStylePanel();
    fPlotStylePanel.addChangeListener(this);
    fLegendPanel = new LegendPanel();
    TitledPanel filter = TitledPanel.createTitledPanel("Filter", fPlotFilterPanel);
    TitledPanel style = TitledPanel.createTitledPanel("Style", fPlotStylePanel);
    JScrollPane scrollPane = new JScrollPane(fLegendPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
      @Override
      public Dimension getPreferredSize() {
        // Clamp scroll pane height, see also class comment of JScrollPane
        Dimension size = super.getPreferredSize();
        size.height = Math.min(size.height, 80);
        return size;
      }

      @Override
      public boolean isValidateRoot() {
        // Re-layout this scroll pane if the LegendPanel changes its preferred size.
        return false;
      }
    };
    TitledPanel legend = TitledPanel.createTitledPanel("Legend", scrollPane);
    JPanel compositePanel = new JPanel();
    BoxLayout layout = new BoxLayout(compositePanel, BoxLayout.Y_AXIS);
    compositePanel.setLayout(layout);
    compositePanel.add(filter);
    compositePanel.add(style);
    compositePanel.add(legend);
    addComponentToRightPanel(compositePanel);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Plot Painting");
  }

}
