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
package samples.lightspeed.demo.application.data.imageprocessing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.services.asynchronous.ILspTaskExecutor;

import samples.lightspeed.imaging.multispectral.OperatorModel;
import samples.lightspeed.imaging.multispectral.curves.CatmullRomEditLine;
import samples.lightspeed.imaging.multispectral.histogram.HistogramUtil;

/**
 * Composite Operator Model Extended
 */
public class CompositeExtendedOperatorModel extends OperatorModelExtended {

  private final List<OperatorModelExtended> fOperatorModels;
  private OperatorModelExtended fActiveOperatorModel;

  public CompositeExtendedOperatorModel(ALcdImage aImage, ILspTaskExecutor aTaskExecutor, OperatorModelExtended... aOperatorModels) {
    super(aOperatorModels[0].getModel(), aImage, aTaskExecutor, false);
    fOperatorModels = Arrays.asList(aOperatorModels);
    fActiveOperatorModel = aOperatorModels[0];
  }

  public void setActiveOperatorModel(OperatorModelExtended aActiveOperatorModel) {
    if (fOperatorModels.contains(aActiveOperatorModel)) {
      fActiveOperatorModel = aActiveOperatorModel;
    } else {
      throw new IllegalArgumentException("Not found in defined operator models");
    }
  }

  @Override
  public String[] createBandNames(int aNbBands) {
    String[] bandNames = new String[aNbBands];
    for (int i = 0; i < aNbBands; i++) {
      bandNames[i] = "Band " + Integer.toString(i + 1);
    }

    return bandNames;
  }

  @Override
  public String[] getSelectedBandNames() {
    return fActiveOperatorModel.getSelectedBandNames();
  }

  @Override
  public void addChangeListener(PropertyChangeListener aListener) {
    fActiveOperatorModel.addChangeListener(aListener);
  }

  @Override
  public void removeChangeListener(PropertyChangeListener aListener) {
    fActiveOperatorModel.removeChangeListener(aListener);
  }

  @Override
  public void fireChangeEvent(PropertyChangeEvent aChangeEvent) {
    fActiveOperatorModel.fireChangeEvent(aChangeEvent);
  }

  @Override
  public int getNbBands() {
    return fActiveOperatorModel.getNbBands();
  }

  @Override
  public int[] getSelectedBands() {
    return fActiveOperatorModel.getSelectedBands();
  }

  @Override
  public CatmullRomEditLine[] getCurrentCurves() {
    return fActiveOperatorModel.getCurrentCurves();
  }

  @Override
  public CatmullRomEditLine getLuminanceCurve() {
    return fActiveOperatorModel.getLuminanceCurve();
  }

  @Override
  public void curveChange() {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.curveChange();
    }
  }

  @Override
  public void getHistogram(int aIndex, HistogramUtil.Callback aCallback) {
    fActiveOperatorModel.getHistogram(aIndex, aCallback);
  }

  @Override
  public void getLuminanceHistogram(HistogramUtil.Callback aCallback) {
    fActiveOperatorModel.getLuminanceHistogram(aCallback);
  }

  @Override
  public void equalizeLuminance(HistogramUtil.Callback aCallback) {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.equalizeLuminance(aCallback);
    }
  }

  @Override
  public void equalize() {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.equalize();
    }
  }

  @Override
  public void normalize() {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.normalize();
    }
  }

  @Override
  public void resetCurves() {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.resetCurves();
    }
  }

  @Override
  public void resetNormalization() {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.resetNormalization();
    }
  }

  @Override
  public boolean isNormalized() {
    return fActiveOperatorModel.isNormalized();
  }

  @Override
  public void setNormalizedDifference(boolean aNormalizedDifference) {
    for (OperatorModelExtended operatorModel : fOperatorModels) {
      operatorModel.setNormalizedDifference(aNormalizedDifference);
    }
  }

  @Override
  public boolean isNormalizedDifference() {
    return fActiveOperatorModel.isNormalizedDifference();
  }

  @Override
  public void resetGeneralParameters() {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.resetGeneralParameters();
    }
  }

  @Override
  public List<ALcdImageOperatorChain> getImageOperators() {
    return fActiveOperatorModel.getImageOperators();
  }

  @Override
  public void setSelectedBands(int[] aSelectedBands) {
    //ensure that active model sets first to fire correct events
    fActiveOperatorModel.setSelectedBands(aSelectedBands);
    for (OperatorModel operatorModel : fOperatorModels) {
      if (operatorModel != fActiveOperatorModel) {
        operatorModel.setSelectedBands(aSelectedBands);
      }
    }
  }

  @Override
  public double getSharpening() {
    return fActiveOperatorModel.getSharpening();
  }

  @Override
  public void setSharpening(double aSharpening) {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.setSharpening(aSharpening);
    }
  }

  @Override
  public float getOpacity() {
    return fActiveOperatorModel.getOpacity();
  }

  @Override
  public void setOpacity(float aOpacity) {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.setOpacity(aOpacity);
    }
  }

  @Override
  public float getContrast() {
    return fActiveOperatorModel.getContrast();
  }

  @Override
  public void setContrast(float aContrast) {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.setContrast(aContrast);
    }
  }

  @Override
  public float getBrightness() {
    return fActiveOperatorModel.getBrightness();
  }

  @Override
  public void setBrightness(float aBrightness) {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.setBrightness(aBrightness);
    }
  }

  @Override
  public boolean isCurvesOnLuminance() {
    return fActiveOperatorModel.isCurvesOnLuminance();
  }

  @Override
  public void setCurvesOnLuminance(boolean aCurvesOnLuminance) {
    for (OperatorModel operatorModel : fOperatorModels) {
      operatorModel.setCurvesOnLuminance(aCurvesOnLuminance);
    }
  }

  @Override
  public ILcdModel getModel() {
    return fActiveOperatorModel.getModel();
  }
}
