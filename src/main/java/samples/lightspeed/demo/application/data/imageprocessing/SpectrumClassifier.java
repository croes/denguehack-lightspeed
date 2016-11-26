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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows classifying pixels in a 7-band LandSat7 image by matching
 * them against a small database.
 */
class SpectrumClassifier {
  private static final double THRESHOLD = 0.2;
  private List<Sample> fSampleSet;

  private static final int[] CLASSIFICATION_BANDS = {0, 1, 2, 3, 4, 5, 6};
  private static final double MAX_DISTANCE = Math.sqrt(CLASSIFICATION_BANDS.length) * 255;

  public enum Classification {
    SNOW("Snow"),
    VEGETATION("Vegetation"),
    URBAN("Man-made"),
    WILD_FIRE("Wildfire"),
    UNKNOWN("Uncertain"),
    WATER("Water"),
    BARE_SOIL("Soil"),
    CLOUDS("Clouds");

    private String fDisplayName;

    Classification(String aDisplayName) {
      fDisplayName = aDisplayName;
    }

    public String getDisplayName() {
      return fDisplayName;
    }
  }

  public SpectrumClassifier() {
    //sample set to be used for classifying spectrum samples
    fSampleSet = new ArrayList<Sample>();
    fSampleSet.add(new Sample(Classification.VEGETATION, new float[]{61.0f, 52.0f, 44.0f, 151.0f, 85.0f, 155.0f, 41.0f}));
    fSampleSet.add(new Sample(Classification.VEGETATION, new float[]{61.0f, 53.0f, 43.0f, 149.0f, 85.0f, 159.0f, 41.0f}));
    fSampleSet.add(new Sample(Classification.VEGETATION, new float[]{56.0f, 49.0f, 50.0f, 75.0f, 71.0f, 163.0f, 50.0f}));

    fSampleSet.add(new Sample(Classification.WATER, new float[]{54.0f, 38.0f, 29.0f, 17.0f, 11.0f, 138.0f, 9.0f}));
    fSampleSet.add(new Sample(Classification.WATER, new float[]{57.0f, 39.0f, 34.0f, 22.0f, 18.0f, 124.0f, 17.0f}));
    fSampleSet.add(new Sample(Classification.WATER, new float[]{56.0f, 37.0f, 24.0f, 13.0f, 9.0f, 138.0f, 8.0f}));

    fSampleSet.add(new Sample(Classification.URBAN, new float[]{205.0f, 191.0f, 212.0f, 154.0f, 164.0f, 177.0f, 111.0f}));
    fSampleSet.add(new Sample(Classification.URBAN, new float[]{162.0f, 151.0f, 168.0f, 110.0f, 182.0f, 135.0f, 197.0f}));
    fSampleSet.add(new Sample(Classification.URBAN, new float[]{110.0f, 100.0f, 114.0f, 82.0f, 88.0f, 196.0f, 80.0f}));
    fSampleSet.add(new Sample(Classification.URBAN, new float[]{156.0f, 149.0f, 171.0f, 117.0f, 124.0f, 173.0f, 126.0f}));
    fSampleSet.add(new Sample(Classification.URBAN, new float[]{178.0f, 163.0f, 192.0f, 130.0f, 122.0f, 173.0f, 106.0f}));
    fSampleSet.add(new Sample(Classification.URBAN, new float[]{176.0f, 164.0f, 183.0f, 121.0f, 210.0f, 153.0f, 219.0f}));

    fSampleSet.add(new Sample(Classification.WILD_FIRE, new float[]{57.0f, 51.0f, 63.0f, 58.0f, 83.0f, 174.0f, 80.0f}));
    fSampleSet.add(new Sample(Classification.WILD_FIRE, new float[]{60.0f, 51.0f, 62.0f, 53.0f, 94.0f, 180.0f, 98.0f}));
    fSampleSet.add(new Sample(Classification.WILD_FIRE, new float[]{55.0f, 45.0f, 52.0f, 47.0f, 75.0f, 186.0f, 76.0f}));

    fSampleSet.add(new Sample(Classification.SNOW, new float[]{255.0f, 255.0f, 255.0f, 207.0f, 14.0f, 107.0f, 12.0f}));
    fSampleSet.add(new Sample(Classification.SNOW, new float[]{156.0f, 147.0f, 169.0f, 127.0f, 59.0f, 120.0f, 49.0f}));
    fSampleSet.add(new Sample(Classification.SNOW, new float[]{226.0f, 211.0f, 236.0f, 161.0f, 21.0f, 102.0f, 15.0f}));

    fSampleSet.add(new Sample(Classification.CLOUDS, new float[]{255.0f, 242.0f, 255.0f, 205.0f, 220.0f, 77.0f, 184.0f}));
    fSampleSet.add(new Sample(Classification.CLOUDS, new float[]{255.0f, 255.0f, 255.0f, 255.0f, 255.0f, 75.0f, 210.0f}));
    fSampleSet.add(new Sample(Classification.CLOUDS, new float[]{254.0f, 226.0f, 253.0f, 190.0f, 197.0f, 78.0f, 170.0f}));

    fSampleSet.add(new Sample(Classification.BARE_SOIL, new float[]{128.0f, 138.0f, 177.0f, 136.0f, 171.0f, 180.0f, 149.0f}));
    fSampleSet.add(new Sample(Classification.BARE_SOIL, new float[]{98.0f, 94.0f, 120.0f, 94.0f, 127.0f, 187.0f, 111.0f}));
    fSampleSet.add(new Sample(Classification.BARE_SOIL, new float[]{96.0f, 95.0f, 126.0f, 102.0f, 138.0f, 191.0f, 119.0f}));
    fSampleSet.add(new Sample(Classification.BARE_SOIL, new float[]{69.0f, 63.0f, 81.0f, 76.0f, 81.0f, 174.0f, 73.0f}));
    fSampleSet.add(new Sample(Classification.BARE_SOIL, new float[]{87.0f, 81.0f, 98.0f, 86.0f, 104.0f, 176.0f, 87.0f}));
    fSampleSet.add(new Sample(Classification.BARE_SOIL, new float[]{70.0f, 64.0f, 74.0f, 77.0f, 90.0f, 165.0f, 72.0f}));
  }

  /**
   * Get the most likely classification based on multiple spectrum samples.
   *
   * @param aSpectrumSamples the list of spectrum samples
   * @return the most occurring classification
   */
  public Classification getClassification(List<float[]> aSpectrumSamples) {
    List<Classification> classifications = new ArrayList<Classification>(aSpectrumSamples.size());

    for (int i = 0; i < aSpectrumSamples.size(); i++) {
      float[] spectrum = aSpectrumSamples.get(i);
      classifications.add(getClassification(spectrum));
    }

    return getMostOccurringClassification(classifications);
  }

  /**
   * Get the classification for a spectrum sample
   *
   * @param aSpectrumSample the array containing the spectrum values
   * @return the classification for the sample
   */
  public Classification getClassification(float[] aSpectrumSample) {
    for (Sample s : fSampleSet) {
      s.setSpectrumSample(aSpectrumSample);
    }

    Collections.sort(fSampleSet);
    Sample result = fSampleSet.get(0);

    if (result.calculateEuclideanDistance() > THRESHOLD) {
      return Classification.UNKNOWN;
    } else {
      return result.getClassification();
    }
  }

  /**
   * Find the most occurring classification from a list.
   *
   * @param aClassifications the list of classifications
   * @return the most occurring classification
   */
  private Classification getMostOccurringClassification(List<Classification> aClassifications) {
    int size = aClassifications.size();
    if (size == 0) {
      return Classification.UNKNOWN;
    }

    int maxCount = 0;
    Classification mostOccurringClassification = null;

    for (int i = 0; i < size; i++) {
      Classification classification = aClassifications.get(i);
      int count = 0;
      for (int j = 0; j < size; j++) {
        if (i == j || aClassifications.get(i).equals(aClassifications.get(j))) {
          count++;
        }
      }
      if (count > maxCount) {
        maxCount = count;
        mostOccurringClassification = classification;
      }
    }
    return mostOccurringClassification;
  }

  /**
   * Class to allow quick comparison of samples. Euclidean distance is used for comparison.
   */
  private class Sample implements Comparable<Sample> {
    private Classification fClassification;
    private float[] fSpectrumValues;
    private float[] fSpectrumSample;

    public Sample(Classification aClass, float[] aSpectrumValues) {
      fSpectrumValues = aSpectrumValues;
      fClassification = aClass;
    }

    public Classification getClassification() {
      return fClassification;
    }

    @Override
    public int compareTo(Sample aSample) {
      double d1 = this.calculateEuclideanDistance();
      double d2 = aSample.calculateEuclideanDistance();
      if (d1 == d2) {
        return 0;
      }
      return (this.calculateEuclideanDistance() < aSample.calculateEuclideanDistance()) ? -1 : 1;
    }

    public void setSpectrumSample(float[] aSpectrumSample) {
      fSpectrumSample = aSpectrumSample;
    }

    public double calculateEuclideanDistance() {
      double sumSquared = 0;
      for (int i : CLASSIFICATION_BANDS) {
        sumSquared += Math.pow(fSpectrumSample[i] - fSpectrumValues[i], 2);
      }
      return Math.sqrt(sumSquared) / MAX_DISTANCE;
    }
  }
}
