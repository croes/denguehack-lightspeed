package com.luciad.dengue.spectral;

import com.luciad.dengue.util.RasterStyler;
import com.luciad.dengue.weather.WeatherAnalysis;
import com.luciad.imaging.ALcdImage;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;

import java.awt.*;
import java.io.IOException;

/**
 * @author Thomas De Bodt
 */
public class SpectralAnalysis extends WeatherAnalysis {

  private SpectralModel fSpectralModel;

  @Override
  protected void addData() throws IOException {
    super.addData();

    fSpectralModel = new SpectralModel();
    getView().addLayer(
        TLspRasterLayerBuilder
            .newBuilder()
            .model(fSpectralModel)
            .styler(
                TLspPaintRepresentationState.REGULAR_BODY,
                new RasterStyler(new TLcdColorMap(
                    new TLcdInterval(0, 1),
                    new double[]{0.0, 1.0},
                    new Color[]{new Color(0, true), new Color(0xff0000)}
                ))
            )
            .build()
    );

    EventQueue.invokeLater(() -> {
      fTimeSlider.addChangeListener(e -> updateSpectralModel());
    });
  }

  private void updateSpectralModel() {
    EventQueue.invokeLater(() -> {
      fSpectralModel.update(
          ALcdImage.fromDomainObject(fPrecipitationModel.elements().nextElement()),
          ALcdImage.fromDomainObject(fTemperatureModel.elements().nextElement())
      );
    });
  }

  public static void main(String[] args) {
    startSample(SpectralAnalysis.class, "Spectral analysis");
  }
}
