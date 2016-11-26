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
package samples.lightspeed.imaging.multispectral;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.imaging.ALcdBandColorSemantics;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.TLcdBandColorSemanticsBuilder;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.TLcdBandSelectOp;
import com.luciad.imaging.operator.TLcdBinaryOp;
import com.luciad.imaging.operator.TLcdConvolveOp;
import com.luciad.imaging.operator.TLcdCurvesOp;
import com.luciad.imaging.operator.TLcdIndexLookupOp;
import com.luciad.imaging.operator.TLcdPixelRescaleOp;
import com.luciad.imaging.operator.TLcdSemanticsOp;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.util.ELcdInterpolationType;
import com.luciad.view.lightspeed.services.asynchronous.ILspTaskExecutor;
import com.luciad.view.lightspeed.services.asynchronous.TLspTaskExecutorRunnable;

import samples.lightspeed.imaging.multispectral.curves.CatmullRomEditLine;
import samples.lightspeed.imaging.multispectral.general.ImageOperatorUtil;
import samples.lightspeed.imaging.multispectral.histogram.Histogram;
import samples.lightspeed.imaging.multispectral.histogram.HistogramUtil;
import samples.lightspeed.imaging.multispectral.normalize.NormalizationUtil;

/**
 * Data class used to store all the parameters of the operators used in the sample.
 * When a change is made in the GUI, the new values are stored in this class.
 *
 */
public class OperatorModel {

  // Identifiers for property change events
  public static final String BAND_CHANGE_EVENT = "bandselect";
  public static final String CURVE_CHANGE_EVENT = "curvechange";
  public static final String SHARPNESS_CHANGE_EVENT = "sharpnesschange";
  public static final String NORMALIZE_CHANGE_EVENT = "normalizechange";
  public static final String STYLER_PROPERTY_CHANGE_EVENT = "stylerchange";

  private List<PropertyChangeListener> fChangeListeners = new ArrayList<PropertyChangeListener>();

  private ILcdModel fModel;

  // Parameters for operators
  private int fNbBands;
  private int[] fSelectedBands;

  private NormalizationCalculator fNormalizationCalculator;
  private double[] fScales;
  private double[] fOffsets;

  private boolean fIsNormalized = false;

  private ILspTaskExecutor fTaskExecutor;

  private HistogramHolder fHistograms = new HistogramHolder(new ComputeHistogramFunction() {
    @Override
    public Future compute(final HistogramUtil.Callback aCallback) {
      if (!ImageOperatorUtil.isImageModel(fModel)) {
        aCallback.histogramNotAvailable();
        return null;
      }

      return fTaskExecutor.execute(new TLspTaskExecutorRunnable(OperatorModel.class, new Runnable() {
        @Override
        public void run() {
          Histogram[] histogramsPerBand = HistogramUtil.getHistogramsPerBand(fModel);
          if (histogramsPerBand == null) {
            aCallback.histogramNotAvailable();
          } else {
            aCallback.histogramsAvailable(histogramsPerBand, OperatorModel.this);
          }
        }
      }, false));
    }
  });

  private HistogramHolder fLuminanceHistogram = new HistogramHolder(new ComputeHistogramFunction() {
    @Override
    public Future compute(final HistogramUtil.Callback aCallback) {
      if (!ImageOperatorUtil.isImageModel(fModel)) {
        aCallback.histogramNotAvailable();
        return null;
      }

      return fTaskExecutor.execute(new TLspTaskExecutorRunnable(OperatorModel.class, new Runnable() {
        @Override
        public void run() {
          Histogram[] luminanceHistogram = HistogramUtil.getLuminanceHistogram(fModel, fSelectedBands);
          if (luminanceHistogram == null) {
            aCallback.histogramNotAvailable();
          } else {
            aCallback.histogramsAvailable(luminanceHistogram, OperatorModel.this);
          }
        }
      }, false));
    }
  });
  private double fSharpening = 0;
  private float fOpacity = 1;
  private float fContrast = 1;
  private float fBrightness = 1;

  // Catmull rom curves
  private CatmullRomEditLine[] fBandCurves;
  private CatmullRomEditLine[] fCurrentCurves = new CatmullRomEditLine[3];
  private CatmullRomEditLine fLuminanceCurve;

  private String[] fBandNames;
  private boolean fCurvesOnLuminance = false;
  private TLcdLookupTable fLookupTable;

  public OperatorModel(ILcdModel aModel, ALcdImage aImage, ILspTaskExecutor aTaskExecutor) {
    this(aModel, aImage, aTaskExecutor, true);
  }

  /**
   * Create a new OperatorModel.
   *
   * @param aModel the model that contains the relevant image
   * @param aImage image to which the operator will be applied
   * @param aNormalize apply normalization at construction or not
   */
  public OperatorModel(ILcdModel aModel, ALcdImage aImage, ILspTaskExecutor aTaskExecutor, boolean aNormalize) {
    fModel = aModel;
    fTaskExecutor = aTaskExecutor;
    ALcdBandSemantics firstBandSemantic = aImage.getConfiguration().getSemantics().get(0);
    if (firstBandSemantic instanceof ALcdBandColorSemantics) {
      ALcdBandColorSemantics colorSemantic = (ALcdBandColorSemantics) firstBandSemantic;
      if (colorSemantic.getType() == ALcdBandColorSemantics.Type.PALETTE_INDEX) {
        fLookupTable = TLcdLookupTable.newBuilder().fromIndexColorModel(colorSemantic.getPalette()).interpolation(ELcdInterpolationType.NONE).build();
        aImage = TLcdIndexLookupOp.indexLookup(aImage, fLookupTable);
      }
    }

    fNbBands = aImage.getConfiguration().getSemantics().size();
    int numSelectedBands = fNbBands < 3 ? 1 : Math.min(fNbBands, 3);
    fSelectedBands = new int[numSelectedBands];
    fBandCurves = createDefaultCurves(fNbBands);
    for (int i = 0; i < numSelectedBands; i++) {
      fSelectedBands[i] = Math.min(fNbBands - 1, i);
      fCurrentCurves[i] = fBandCurves[Math.min(fNbBands - 1, i)];
    }

    fNormalizationCalculator = new NormalizationCalculator();
    fScales = new double[fNbBands];
    fOffsets = new double[fNbBands];
    Arrays.fill(fScales, 1.0);
    Arrays.fill(fOffsets, 0.0);

    fLuminanceCurve = new CatmullRomEditLine();
    fBandNames = createBandNames(fNbBands);
    if (aNormalize) {
      normalize();
    }
  }

  /**
   * Creates an array of band names with a length of aNbBands
   * @param aNbBands length of the names array
   * @return a string array of created band names
   */
  protected String[] createBandNames(int aNbBands) {
    String[] bandNames = new String[aNbBands];
    for (int i = 0; i < aNbBands; i++) {
      bandNames[i] = "Band " + Integer.toString(i + 1);
    }

    return bandNames;
  }

  /**
   * @return selected band names as a string array
   */
  public String[] getSelectedBandNames() {
    String[] names = new String[fSelectedBands.length];
    for (int i = 0; i < fSelectedBands.length; i++) {
      names[i] = fBandNames[fSelectedBands[i]];
    }

    return names;
  }

  /**
   * Register listeners that need to be notified about changes.
   *
   * @param aListener a listener
   */
  public void addChangeListener(PropertyChangeListener aListener) {
    fChangeListeners.add(aListener);
  }

  /**
   * Remove a specific listener
   *
   * @param aListener the listener to remove.
   */
  public void removeChangeListener(PropertyChangeListener aListener) {
    fChangeListeners.remove(aListener);
  }

  //notify registered listeners in case a value has changed.
  public void fireChangeEvent(PropertyChangeEvent aChangeEvent) {
    for (PropertyChangeListener listener : fChangeListeners) {
      listener.propertyChange(aChangeEvent);
    }
  }

  /**
   * @return number of bands
   */
  public int getNbBands() {
    return fNbBands;
  }

  /**
   * @return the selected bands
   */
  public int[] getSelectedBands() {
    return fSelectedBands;
  }

  /**
   * @return all catmull rom lines
   */
  public CatmullRomEditLine[] getCurrentCurves() {
    return fCurrentCurves;
  }

  /**
   * @return luminance curve
   */
  public CatmullRomEditLine getLuminanceCurve() {
    return fLuminanceCurve;
  }

  /**
   * Called when the curve is updated
   */
  public void curveChange() {
    fireChangeEvent(new PropertyChangeEvent(this, CURVE_CHANGE_EVENT, null, fCurrentCurves));
  }

  /**
   * Creates a histogram of the given band in given view
   *
   * @param aIndex band index
   * @param aCallback histogram callback
   */
  public void getHistogram(final int aIndex, final HistogramUtil.Callback aCallback) {
    fHistograms.get(new HistogramUtil.Callback() {
      @Override
      public void histogramsAvailable(Histogram[] aHistograms, Object aSource) {
        aCallback.histogramsAvailable(new Histogram[]{aHistograms[fSelectedBands[aIndex]]}, OperatorModel.this);
      }

      @Override
      public void histogramNotAvailable() {
      }
    });
  }

  /**
   * Creates a luminance histogram
   *
   * @param aCallback histogram callback
   */
  public void getLuminanceHistogram(final HistogramUtil.Callback aCallback) {
    if (fSelectedBands.length != 1) {
      fLuminanceHistogram.get(new HistogramUtil.Callback() {
        @Override
        public void histogramsAvailable(final Histogram[] aHistograms, Object aSource) {
          aCallback.histogramsAvailable(aHistograms, OperatorModel.this);
        }

        @Override
        public void histogramNotAvailable() {
        }
      });
    }
  }

  /**
   * Creates an equalization curve for the given histogram
   *
   * @param aHistogramIndex  histogram index
   * @param aLuminanceEqualization if true, curve will be created for fLuminanceHistogram
   */
  private void createEqualizationCurve(int aHistogramIndex, boolean aLuminanceEqualization) {
    Histogram histogram;
    if (aLuminanceEqualization) {
      histogram = fLuminanceHistogram.get(0);
    } else {
      histogram = fHistograms.get(aHistogramIndex);
    }

    if (histogram == null) {
      return;
    }

    if (aLuminanceEqualization) {
      fLuminanceCurve = new CatmullRomEditLine(histogram.getEqualizationCurve());
    } else {
      int channel = fBandCurves[aHistogramIndex].getChannel();
      fBandCurves[aHistogramIndex] = new CatmullRomEditLine(histogram.getEqualizationCurve());
      fBandCurves[aHistogramIndex].setChannel(channel);
    }
  }

  /**
   * Creates an equalization curve for luminance
   *
   * @param aCallback histogram callback
   */
  public void equalizeLuminance(final HistogramUtil.Callback aCallback) {
    getLuminanceHistogram(new HistogramUtil.Callback() {
      @Override
      public void histogramsAvailable(Histogram[] aHistograms, Object aSource) {
        createEqualizationCurve(0, true);
        aCallback.histogramsAvailable(null, OperatorModel.this);
      }

      @Override
      public void histogramNotAvailable() {
      }
    });
  }

  /**
   * Equalizes all bands
   *
   */
  public void equalize() {
    // Equalize all bands
    for (int i = 0; i < fNbBands; i++) {
      createEqualizationCurve(i, false);
    }

    // Update the current curves with the new curves
    for (int i = 0; i < fSelectedBands.length; i++) {
      fCurrentCurves[i] = fBandCurves[fSelectedBands[i]];
    }
  }

  /**
   * Normalize all bands of the image.
   *
   */
  public void normalize() {
    if (!fIsNormalized) {
      fNormalizationCalculator.get(fTaskExecutor, fModel, new NormalizationUtil.Callback() {
        @Override
        public void parametersAvailable(double[] aScales, double[] aOffsets) {
          fScales = aScales;
          fOffsets = aOffsets;
          TLcdAWTUtil.invokeAndWait(new Runnable() {
            @Override
            public void run() {
              fireChangeEvent(new PropertyChangeEvent(OperatorModel.this, NORMALIZE_CHANGE_EVENT, null, null));
            }
          });
        }
      });

      fIsNormalized = true;
    }
  }

  /**
   * Resets curves and normalization
   */
  public void resetCurves() {
    // reset all bands
    for (int i = 0; i < fNbBands; i++) {
      fBandCurves[i].reset();
    }
    fLuminanceCurve.reset();
  }

  public void resetNormalization() {
    fNormalizationCalculator.cancel();
    Arrays.fill(fScales, 1);
    Arrays.fill(fOffsets, 0);
    fIsNormalized = false;
    fireChangeEvent(new PropertyChangeEvent(OperatorModel.this, NORMALIZE_CHANGE_EVENT, null, null));
  }

  public boolean isNormalized() {
    return fIsNormalized;
  }

  /**
   * Resets opacity, brightness, contrast and sharpening to default values.
   */
  public void resetGeneralParameters() {
    fSharpening = 0;
    fOpacity = 1;
    fContrast = 1;
    fBrightness = 1;
  }

  /**
   * @return a list of image operator chains with the parameters currently set
   */
  public List<ALcdImageOperatorChain> getImageOperators() {
    List<ALcdImageOperatorChain> operators = new ArrayList<>();
    if (fLookupTable != null) {
      operators.add(createIndexLookup(fLookupTable));
    }

    operators.add(BandSelectOperatorChain.create(fSelectedBands, fScales, fOffsets));

    if (fSelectedBands.length != 1) {
      operators.add(createLuminanceEqualization());
    }

    operators.add(createCurvesOperator(false));

    ALcdImageOperatorChain sharpeningOperator = createSharpeningOperator();
    if (sharpeningOperator != null) {
      operators.add(sharpeningOperator);
    }

    return operators;
  }

  /**
   * @param aLookupTable lookup table
   *
   * @return new operator chain with a {@code TLcdIndexLookupOp} operator with given lookup table
   */
  private ALcdImageOperatorChain createIndexLookup(final TLcdLookupTable aLookupTable) {
    return new IndexLookUpImageOperatorChain(aLookupTable);
  }

  /**
   * @return a new operator chain which applies a luminance equalization
   */
  private ALcdImageOperatorChain createLuminanceEqualization() {
    return new LuminanceEqualizationOperatorChain(createCurvesOperator(true));
  }

  /**
   * @return a sharpening operator.
   */
  private ALcdImageOperatorChain createSharpeningOperator() {
    if (fSharpening > 0) {
      return new SharpeningImageOperatorChain(fSharpening);
    }
    return null;
  }

  /**
   * Set the currently selected bands.
   *
   * @param aSelectedBands  the selected bands.
   */
  public void setSelectedBands(int[] aSelectedBands) {
    fSelectedBands = new int[aSelectedBands.length];

    for (int i = 0; i < fSelectedBands.length; i++) {
      fSelectedBands[i] = Math.min(fNbBands - 1, aSelectedBands[Math.min(aSelectedBands.length - 1, i)]);
    }

    for (int i = 0; i < fCurrentCurves.length; i++) {
      int indexCurves = Math.min(fSelectedBands[Math.min(fSelectedBands.length - 1, i)], fBandCurves.length - 1);
      fCurrentCurves[i] = fBandCurves[indexCurves];
    }

    fLuminanceHistogram.invalidate();
    fireChangeEvent(new PropertyChangeEvent(this, BAND_CHANGE_EVENT, null, fSelectedBands));
  }

  /**
   * Creates a curves operator
   *
   * @return the curves operator.
   */
  private ALcdImageOperatorChain createCurvesOperator(boolean isCurvesOnLuminance) {
    final ILcdPoint[][] controlPoints = new ILcdPoint[4][];
    if (isCurvesOnLuminance) {
      for (int i = 0; i < 3; i++) {
        ILcdPoint points[] = new ILcdPoint[fLuminanceCurve.getPointCount()];
        for (int k = 0; k < points.length; k++) {
          points[k] = fLuminanceCurve.getPoint(k);
        }
        controlPoints[i] = points;
      }
    } else {
      for (int i = 0; i < 3; i++) {
        int index = fSelectedBands.length == 3 ? i : 0;
        ILcdPoint points[] = new ILcdPoint[fCurrentCurves[index].getPointCount()];
        for (int k = 0; k < points.length; k++) {
          points[k] = fCurrentCurves[index].getPoint(k);
        }
        controlPoints[i] = points;
      }
    }

    ILcdPoint[] alphaCurveControlPoints = new ILcdPoint[2];
    alphaCurveControlPoints[0] = new TLcdXYPoint(0, 0);
    alphaCurveControlPoints[1] = new TLcdXYPoint(1, 1);
    controlPoints[3] = alphaCurveControlPoints;
    return new CurvesOperatorChain(controlPoints);
  }

  /**
   * @param aNbBands number of bands
   * @return Creates default curves for the given band number
   */
  private CatmullRomEditLine[] createDefaultCurves(int aNbBands) {
    CatmullRomEditLine[] bandCurves = new CatmullRomEditLine[aNbBands];
    for (int i = 0; i < bandCurves.length; i++) {
      bandCurves[i] = new CatmullRomEditLine();
    }
    return bandCurves;
  }

  public double getSharpening() {
    return fSharpening;
  }

  public void setSharpening(double aSharpening) {
    fSharpening = aSharpening;
    fireChangeEvent(new PropertyChangeEvent(this, SHARPNESS_CHANGE_EVENT, null, fSharpening));
  }

  public float getOpacity() {
    return fOpacity;
  }

  public void setOpacity(float aOpacity) {
    fOpacity = aOpacity;
    fireChangeEvent(new PropertyChangeEvent(this, STYLER_PROPERTY_CHANGE_EVENT, null, fOpacity));
  }

  public float getContrast() {
    return fContrast;
  }

  public void setContrast(float aContrast) {
    fContrast = aContrast;
    fireChangeEvent(new PropertyChangeEvent(this, STYLER_PROPERTY_CHANGE_EVENT, null, fContrast));
  }

  public float getBrightness() {
    return fBrightness;
  }

  public void setBrightness(float aBrightness) {
    fBrightness = aBrightness;
    fireChangeEvent(new PropertyChangeEvent(this, STYLER_PROPERTY_CHANGE_EVENT, null, fBrightness));
  }

  public boolean isCurvesOnLuminance() {
    return fCurvesOnLuminance;
  }

  public void setCurvesOnLuminance(boolean aCurvesOnLuminance) {
    fCurvesOnLuminance = aCurvesOnLuminance;
  }

  public ILcdModel getModel() {
    return fModel;
  }

  /**
   * Converts an image with index data (also known as a image with a color palette) to a regular color image.
   */
  private static class IndexLookUpImageOperatorChain extends ALcdImageOperatorChain {
    private final TLcdLookupTable fLookupTable;

    public IndexLookUpImageOperatorChain(TLcdLookupTable aLookupTable) {
      fLookupTable = aLookupTable;
    }

    @Override
    public ALcdImage apply(ALcdImage aInput) {
      return TLcdIndexLookupOp.indexLookup(aInput, fLookupTable);
    }
  }

  /**
   * Selects the correct bands and interprets these as grayscale or RGB data.
   */
  public static class BandSelectOperatorChain extends ALcdImageOperatorChain {
    private int[] fBands;
    private double[] fScales;
    private double[] fOffsets;
    private List<ALcdBandSemantics> fTargetSemantics;

    private BandSelectOperatorChain(int[] aBands, double[] aScales, double[] aOffsets) {
      fBands = aBands;
      fScales = new double[fBands.length];
      fOffsets = new double[fBands.length];
      for (int i = 0; i < fBands.length; i++) {
        fScales[i] = aScales[fBands[i]];
        fOffsets[i] = aOffsets[fBands[i]];
      }

      if (aBands.length == 1) {
        // Set the semantics to resemble a gray image.
        fTargetSemantics = TLcdBandColorSemanticsBuilder.newBuilder()
                                                        .colorModel(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE))
                                                        .buildSemantics();
      } else if (aBands.length == 3) {
        // Set the semantics to resemble a RGB image.
        fTargetSemantics = TLcdBandColorSemanticsBuilder.newBuilder()
                                                        .colorModel(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE))
                                                        .buildSemantics();
      } else {
        throw new IllegalArgumentException("Unsupported number of bands: " + aBands.length);
      }
    }

    @Override
    public ALcdImage apply(ALcdImage aInput) {
      ALcdImage bandSelected = TLcdBandSelectOp.bandSelect(aInput, fBands);
      ALcdImage normalized = TLcdPixelRescaleOp.pixelRescale(bandSelected, fScales, fOffsets);
      return TLcdSemanticsOp.semantics(normalized, fTargetSemantics.toArray(new ALcdBandSemantics[fTargetSemantics.size()]));
    }

    public static ALcdImageOperatorChain create(int[] aBands, double[] aScales, double[] aOffsets) {
      return new BandSelectOperatorChain(aBands, aScales, aOffsets);
    }
  }

  /**
   * Performs the sharpening operation.
   */
  private static class SharpeningImageOperatorChain extends ALcdImageOperatorChain {
    private final double fSharpening;

    public SharpeningImageOperatorChain(double aSharpening) {
      fSharpening = aSharpening;
    }

    @Override
    public ALcdImage apply(ALcdImage aInput) {
      float n = (float) (-1f * fSharpening);
      float c = 1f - 8f * n;
      return TLcdConvolveOp.convolve(aInput, new double[]{n, n, n, n, c, n, n, n, n}, 3, 3);
    }
  }

  /**
   * Applies re-scaling based on curves.
   */
  private static class CurvesOperatorChain extends ALcdImageOperatorChain {
    private final ILcdPoint[][] fControlPoints;

    public CurvesOperatorChain(ILcdPoint[][] aControlPoints) {
      fControlPoints = aControlPoints.clone();
      for (int i = 0; i < fControlPoints.length; i++) {
        fControlPoints[i] = fControlPoints[i].clone();
        for (int j = 0; j < fControlPoints[i].length; j++) {
          fControlPoints[i][j] = fControlPoints[i][j].cloneAs2DEditablePoint();
        }
      }
    }

    @Override
    public ALcdImage apply(ALcdImage aInput) {
      return TLcdCurvesOp.curves(aInput, fControlPoints, TLcdCurvesOp.CurveType.CATMULL_ROM);
    }
  }

  /**
   * Applies equalization based on the luminance of the image.
   */
  private static class LuminanceEqualizationOperatorChain extends ALcdImageOperatorChain {
    private final ALcdImageOperatorChain fLuminanceCurvesOperator;

    public LuminanceEqualizationOperatorChain(ALcdImageOperatorChain aLuminanceCurvesOperator) {
      fLuminanceCurvesOperator = aLuminanceCurvesOperator;
    }

    @Override
    public ALcdImage apply(ALcdImage aInput) {
      if (aInput instanceof ALcdBasicImage) {
        ALcdBasicImage aInputImage = (ALcdBasicImage) aInput;
        ALcdBasicImage luminanceImage = HistogramUtil.createLuminanceImage(aInputImage);
        ALcdBasicImage equalizedLuminance = (ALcdBasicImage) fLuminanceCurvesOperator.apply(luminanceImage);
        ALcdBasicImage imageWithScales = TLcdBinaryOp.binaryOp(equalizedLuminance, luminanceImage, TLcdBinaryOp.Operation.DIVIDE);
        return TLcdBinaryOp.binaryOp(aInputImage, imageWithScales, TLcdBinaryOp.Operation.MULTIPLY);
      } else {
        return aInput;
      }
    }
  }

  private static class HistogramHolder {
    private final ComputeHistogramFunction fComputeFunction;
    private ArrayList<HistogramUtil.Callback> fCallbacks;
    private Histogram[] fHistograms;
    private Callback fCurrentCallback;

    public HistogramHolder(ComputeHistogramFunction aComputeFunction) {
      fComputeFunction = aComputeFunction;
    }

    public void get(final HistogramUtil.Callback aCallback) {
      final Histogram[] histograms;
      synchronized (this) {
        histograms = fHistograms;
        if (histograms == null) {
          if (fCurrentCallback == null) {
            fCurrentCallback = new Callback();
            fCallbacks = new ArrayList<>();
            Future future = fComputeFunction.compute(fCurrentCallback);
            fCurrentCallback.setFuture(future);
          }
          fCallbacks.add(aCallback);
        }
      }
      if (histograms != null) {
        TLcdAWTUtil.invokeNowOrLater(new Runnable() {
          public void run() {
            invokeCallback(histograms, aCallback);
          }
        });
      }
    }

    private static void invokeCallback(Histogram[] aHistograms, HistogramUtil.Callback aCallback) {
      if (aHistograms.length == 0) {
        aCallback.histogramNotAvailable();
      } else {
        aCallback.histogramsAvailable(aHistograms, null);
      }
    }

    public synchronized Histogram get(int aHistogramIndex) {
      return fHistograms != null && aHistogramIndex < fHistograms.length ? fHistograms[aHistogramIndex] : null;
    }

    private void updateHistograms(Histogram[] aHistograms, Callback aCallback) {
      ArrayList<HistogramUtil.Callback> callbacks;
      synchronized (HistogramHolder.this) {
        if (fCurrentCallback == aCallback) {
          fHistograms = aHistograms;
        }
        callbacks = fCallbacks;
        fCallbacks = null;
      }
      for (HistogramUtil.Callback callback : callbacks) {
        invokeCallback(aHistograms, callback);
      }
    }

    public synchronized void invalidate() {
      if (fCurrentCallback != null) {
        fCurrentCallback.cancel();
        fCurrentCallback = null;
      }
      fHistograms = null;
    }

    private class Callback implements HistogramUtil.Callback {
      private Future fFuture;

      @Override
      public void histogramsAvailable(Histogram[] aHistograms, Object aSource) {
        updateHistograms(aHistograms, this);
      }

      @Override
      public void histogramNotAvailable() {
        updateHistograms(new Histogram[0], this);
      }

      public synchronized void cancel() {
        if (fFuture != null) {
          fFuture.cancel(false);
        }
      }

      public synchronized void setFuture(Future aFuture) {
        fFuture = aFuture;
      }
    }
  }

  private interface ComputeHistogramFunction {
    Future compute(HistogramUtil.Callback aCallback);
  }

  private static class NormalizationCalculator {

    private Future fFuture;

    public synchronized void get(final ILspTaskExecutor aTaskExecutor, final ILcdModel aModel, final NormalizationUtil.Callback aCallback) {
      if (fFuture == null || fFuture.isDone()) {
        fFuture = aTaskExecutor.execute(new TLspTaskExecutorRunnable(OperatorModel.class, new Runnable() {
          @Override
          public void run() {
            double[][] scaleOffset = NormalizationUtil.calculateNormalizingPixelRescale(aModel);
            aCallback.parametersAvailable(scaleOffset[0], scaleOffset[1]);
          }
        }, false));
      }
    }

    public synchronized void cancel() {
      if (fFuture != null) {
        fFuture.cancel(false);
      }
    }
  }
}
