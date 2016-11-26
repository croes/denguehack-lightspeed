package com.luciad.dengue.spectral;

import com.luciad.dengue.util.HasAImage;
import com.luciad.imaging.*;
import com.luciad.imaging.operator.TLcdBandSelectOp;
import com.luciad.imaging.operator.TLcdBinaryOp;
import com.luciad.imaging.operator.TLcdIndexLookupOp;
import com.luciad.imaging.operator.TLcdSemanticsOp;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.internal.imaging.TLinSharedImagingEngine;
import com.luciad.internal.util.TLinRatioUnit;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.util.collections.TLcdDoubleArrayList;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.measure.ALcdMeasureProvider;
import com.luciad.util.measure.TLcdImageModelMeasureProviderFactory;

import java.awt.*;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas De Bodt
 */
public class SpectralModel extends TLcdVectorModel {

  private final TLcdInterval fPrecipitationInterval = new TLcdInterval(400, 1000);
  private final TLcdInterval fTemperatureInterval = new TLcdInterval(100, 1000);

  private final HasAImage fHasAImage;

  public SpectralModel() {
    super(new TLcdGeodeticReference(),
          new TLcdImageModelDescriptor("Spectral", "Spectral", "Spectral"));
    fHasAImage = new HasAImage();
    addElement(fHasAImage, NO_EVENT);
  }

  public void update(ALcdImage aPrecipitation,
                     ALcdImage aTemperature) {
    ALcdBasicImage.Configuration spectralCfg = getSpectralConfig(aPrecipitation, aTemperature);
    ALcdBasicImage precipitation = debug(getBasicImage(aPrecipitation, spectralCfg), "prec");
    ALcdBasicImage temperature = debug(getBasicImage(aTemperature, spectralCfg), "temp");

    ALcdBasicImage filteredPrecipitation = debug(filter(precipitation, fPrecipitationInterval), "prec_filt");
    ALcdBasicImage filteredTemperature = debug(filter(temperature, fTemperatureInterval), "temp_filt");

    ALcdBasicImage mixed = debug(multiply(filteredPrecipitation, filteredTemperature), "mixed");
    ALcdBasicImage mixedSingleBand = debug((ALcdBasicImage)TLcdBandSelectOp.bandSelect(mixed, new int[]{0}), "mixed1");

    ALcdBasicImage spectralImage = debug(
        (ALcdBasicImage)TLcdSemanticsOp.semantics(
            mixedSingleBand,
            Collections.singletonList(
                TLcdBandMeasurementSemanticsBuilder
                    .newBuilder()
                    .unitOfMeasure(TLinRatioUnit.RATIO)
                    .dataType(ALcdBandSemantics.DataType.FLOAT)
                    .build()
            ),
            new double[]{1.0},
            new double[]{0}
        ),
        "spectral"
    );

    try(TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(this)) {
      fHasAImage.setImage(spectralImage);
      elementChanged(fHasAImage, FIRE_LATER);
    }
    fireCollectedModelChanges();
  }

  private static ALcdBasicImage debug(ALcdBasicImage aImage, String aName) {
    /*final int lon = -100;
    final int lat = 37;
    Rectangle r = new Rectangle(
        (int)Math.round((lon + 180) * aImage.getConfiguration().getWidth() / 360.0),
        (int)Math.round((lat + 90) * aImage.getConfiguration().getHeight() / 180.0),
        1, 1
    );
    WritableRaster data;
    ALcdImagingEngine engine = TLinSharedImagingEngine.getImagingEngine();
    try {
      data = engine.getImageData(aImage, r);
    } finally {
      TLinSharedImagingEngine.releaseImagingEngine(engine);
    }
    double[] pixel = data.getPixel(data.getMinX(), data.getMinY(), (double[])null);
    System.out.printf("  %s[%d,%d]a: %s%n", aName, r.x, r.y, Arrays.toString(pixel));

    TLcdVectorModel m = new TLcdVectorModel(
        aImage.getConfiguration().getImageReference(),
        new TLcdImageModelDescriptor()
    );
    m.addElement(aImage, NO_EVENT);
    TLcdImageModelMeasureProviderFactory measureProviderFactory = new TLcdImageModelMeasureProviderFactory();
    ALcdMeasureProvider measureProvider = measureProviderFactory.createMeasureProvider(m);
    if(measureProvider != null) {
      TLcdISO19103Measure[] measures = measureProvider.retrieveMeasuresAt(new TLcdLonLatPoint(lon, lat), new TLcdGeodeticReference(), ALcdMeasureProvider.Parameters.newBuilder().build());
      System.out.printf("  %s[%d,%d]b: %s%n", aName, r.x, r.y, measures[0].doubleValue());
    }*/
    return aImage;
  }

  private ALcdBasicImage filter(ALcdBasicImage aImage, ILcdInterval aInterval) {
    ALcdBandSemantics semantic = getSingleBandSemantic(aImage);
    double dataMin = semantic.getNormalizedRangeMinValue().doubleValue();
    double dataMax = semantic.getNormalizedRangeMaxValue().doubleValue();

    double min = aInterval.getMin();
    double max = aInterval.getMax();

    double d = (max - min) * 0.1; // 10% fade-out on each end

    TLcdDoubleArrayList levels = new TLcdDoubleArrayList();
    ArrayList<Color> colors = new ArrayList<>();
    levels.addDouble(dataMin);
    colors.add(Color.BLACK);
    if(min - d > dataMin) {
      levels.addDouble(min - d);
      colors.add(Color.BLACK);
    }
    levels.addDouble(min);
    colors.add(Color.WHITE);
    levels.addDouble(max);
    colors.add(Color.WHITE);
    if(max + d < dataMax) {
      levels.addDouble(max + d);
      colors.add(Color.BLACK);
    }
    levels.addDouble(dataMax);
    colors.add(Color.BLACK);

    TLcdColorMap colorFilterOnRange = new TLcdColorMap(
        new TLcdInterval(dataMin, dataMax),
        levels.toDoubleArray(),
        colors.toArray(new Color[colors.size()])
    );

    return (ALcdBasicImage)TLcdIndexLookupOp.indexLookup(
        aImage,
        TLcdLookupTable
            .newBuilder()
            .fromColorMap(colorFilterOnRange)
            .build()
    );
  }

  private ALcdBandSemantics getSingleBandSemantic(ALcdImage aImage) {
    List<ALcdBandSemantics> semantics = aImage.getConfiguration().getSemantics();
    if(semantics.size() != 1) {
      throw new IllegalArgumentException("Expected 1-band image but got " + semantics);
    }
    return semantics.get(0);
  }

  private ALcdBasicImage multiply(ALcdBasicImage... aImages) {
    ALcdBasicImage mul = aImages[0];
    for(int i = 1; i < aImages.length; i++) {
      return TLcdBinaryOp.multiply(mul, aImages[i]);
    }
    return mul;
  }

  private ALcdBasicImage.Configuration getSpectralConfig(ALcdImage... aImages) {
    ALcdBasicImage.Configuration refCfg = ((ALcdBasicImage)aImages[0]).getConfiguration();
    List<ALcdBandSemantics> spectralSemantics = Stream.of(aImages).map(this::getSingleBandSemantic).collect(Collectors.toList());
    return refCfg.asBuilder().semantics(spectralSemantics).build();
  }

  private ALcdBasicImage getBasicImage(ALcdImage aImage, ALcdBasicImage.Configuration aSpectralCfg) {
    if(!(aImage instanceof ALcdBasicImage)) {
      throw new IllegalArgumentException("Not a basic image: " + aImage);
    }
    ALcdBasicImage basicImage = (ALcdBasicImage)aImage;
    ALcdBasicImage.Configuration cfg = basicImage.getConfiguration();
    if(cfg.getWidth() != aSpectralCfg.getWidth() ||
       cfg.getHeight() != aSpectralCfg.getHeight()) {
      throw new IllegalArgumentException("Resolution does not match: got " + cfg + " but need " + aSpectralCfg);
    }
    if(!cfg.getBounds().equals(aSpectralCfg.getBounds())) {
      throw new IllegalArgumentException("Bounds do not match: got " + cfg + " but need " + aSpectralCfg);
    }
    return basicImage;
  }
}
