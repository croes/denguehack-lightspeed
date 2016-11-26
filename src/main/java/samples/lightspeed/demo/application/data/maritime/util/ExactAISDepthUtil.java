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
package samples.lightspeed.demo.application.data.maritime.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.format.s57.ILcdS57Object;
import com.luciad.format.s57.TLcdS57SoundingPoint;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFeatured;
import com.luciad.util.ILcdFunction;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.data.maritime.AISPlot;
import samples.lightspeed.demo.framework.data.IOUtil;

/**
 * Utility code for calculating, saving and loading ocean depth for specific points in ECDIS
 * datasets,
 * based on soundings and dredged areas. Used in exactAIS theme for styling purposes.
 */
public class ExactAISDepthUtil {

  public static void insertDepthsInAISPlots(ILspLayer aNOAALayer, ILcdModel aAISPlotModel, String aDepthMapLocation, boolean aCreateNewFile) {
    if (aCreateNewFile) {
      Map<Integer, Map<Long, Double>> depthMapping = new HashMap<Integer, Map<Long, Double>>();
      Enumeration elements = aAISPlotModel.elements();
      while (elements.hasMoreElements()) {
        Object element = elements.nextElement();
        if (element instanceof AISPlot) {
          double depth = retrieveDepthForBoat((AISPlot) element, aNOAALayer);
          int id = ((AISPlot) element).getID();
          long timeStamp = ((AISPlot) element).getTimeStamp();
          addDepthMapEntry(depthMapping, depth, id, timeStamp);
        }
      }
      writeDepthMap(depthMapping, aDepthMapLocation);
      fillPlotsWithDepthInfo(aAISPlotModel, depthMapping);
    } else {
      Map<Integer, Map<Long, Double>> depthMapping = readDepthMap(aDepthMapLocation);
      fillPlotsWithDepthInfo(aAISPlotModel, depthMapping);
    }
  }

  private static void fillPlotsWithDepthInfo(ILcdModel aAISPlotModel, Map<Integer, Map<Long, Double>> aDepthMapping) {
    Enumeration elements = aAISPlotModel.elements();
    while (elements.hasMoreElements()) {
      Object element = elements.nextElement();
      if (element instanceof AISPlot) {
        AISPlot plot = (AISPlot) element;
        Map<Long, Double> plotMap = aDepthMapping.get(plot.getID());
        if (plotMap != null) {
          plot.setOceanFloorDepth(plotMap.get(plot.getTimeStamp()));
        } else {
          plot.setOceanFloorDepth(Double.NaN);
        }
      }
    }
  }

  private static Map<Integer, Map<Long, Double>> readDepthMap(String aFileName) {
    Map<Integer, Map<Long, Double>> depthMapping = new HashMap<Integer, Map<Long, Double>>();
    try {
      BufferedReader reader = IOUtil.createReader(aFileName);
      String nextLine = reader.readLine();
      while (nextLine != null) {
        String[] splitString = nextLine.split(",");
        int identifier = Integer.parseInt(splitString[0]);
        long timestamp = Long.parseLong(splitString[1]);
        double depth = Double.parseDouble(splitString[2]);
        addDepthMapEntry(depthMapping, depth, identifier, timestamp);
        nextLine = reader.readLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return depthMapping;
  }

  private static void writeDepthMap(Map<Integer, Map<Long, Double>> aDepthMapping, String aName) {
    try {
      File file = new File("resources", aName);
      if (!file.exists()) {
        file.createNewFile();
      }
      FileOutputStream fos = new FileOutputStream(file);
      PrintWriter printWriter = new PrintWriter(fos);
      for (Map.Entry<Integer, Map<Long, Double>> entry : aDepthMapping.entrySet()) {
        for (Map.Entry<Long, Double> valueEntry : entry.getValue().entrySet()) {
          printWriter.println(entry.getKey() + "," + valueEntry.getKey() + "," + valueEntry.getValue());
        }
      }
      printWriter.flush();
      printWriter.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void addDepthMapEntry(Map<Integer, Map<Long, Double>> aDepthMapping, double aDepth, int aId, long aTimeStamp) {
    Map<Long, Double> depthMap = aDepthMapping.get(aId);
    if (depthMap == null) {
      depthMap = new HashMap<Long, Double>();
      aDepthMapping.put(aId, depthMap);
    }
    depthMap.put(aTimeStamp, aDepth);
  }

  /**
   * Interpolates a depth, based on the NOAA layer and its soundings/dredged areas.
   *
   * @param aPlot      a plot to check
   * @param aNOAALayer the ECDIS layer to use
   *
   * @return depth of ocean for plot according to given ECDIS data
   */
  private static double retrieveDepthForBoat(AISPlot aPlot, ILspLayer aNOAALayer) {
    ILcdPoint location = aPlot.getLocation();
    double width = 0.05;
    double height = width / 2;
    TLcdLonLatBounds queryBounds = new TLcdLonLatBounds(location.getX() - width / 2, location.getY() - height / 2, width, height);
    CollectSoundingFunction functionToApply = new CollectSoundingFunction();
    ((ILcd2DBoundsIndexedModel) aNOAALayer.getModel()).applyOnInteract2DBounds(queryBounds, false, functionToApply, 0, 0);
    ILcdEllipsoid ellipsoid = ((ILcdGeoReference) aNOAALayer.getModel().getModelReference()).getGeodeticDatum().getEllipsoid();
    for (ILcdS57Object collectedDredgedArea : functionToApply.fCollectedDredgedAreas) {
      if (collectedDredgedArea.contains2D(aPlot.getX(), aPlot.getY())) {
        Float result = (Float) ((ILcdFeatured) collectedDredgedArea).getFeature(0);
        return result == null ? Double.NaN : (double) result;
      }
    }
    int size = functionToApply.fCollectedSoundingElements.size();
    if (size == 0) {
      return Double.NaN;
    } else {
      Collections.sort(functionToApply.fCollectedSoundingElements, new DistanceComparator(aPlot, ellipsoid));
      double closestDistance = ellipsoid.geodesicDistance(functionToApply.fCollectedSoundingElements.get(0), aPlot);
      if ( /*closestDistance > 10000*/false) {
        return Double.NaN;
      } else {
        if (functionToApply.fCollectedSoundingElements.size() < 1) {
          return functionToApply.fCollectedSoundingElements.get(0).getSoundingValue();
        } else {
          int maxSamplePoints = 10;
          double w[] = new double[maxSamplePoints];
          double d[] = new double[maxSamplePoints];
          double v[] = new double[maxSamplePoints];
          Arrays.fill(w, Double.NaN);
          Arrays.fill(d, Double.NaN);
          Arrays.fill(v, Double.NaN);
          double dMax = Double.NEGATIVE_INFINITY;
          for (int i = 0; i < maxSamplePoints && i < functionToApply.fCollectedSoundingElements.size(); i++) {
            TLcdS57SoundingPoint soundingPoint = functionToApply.fCollectedSoundingElements.get(i);
            d[i] = ellipsoid.geodesicDistance(soundingPoint, aPlot);
            v[i] = soundingPoint.getSoundingValue();
            if (d[i] > dMax) {
              dMax = d[i];
            }
          }
          double weightSum = 0;
          double topSum = 0;
          for (int i = 0; i < maxSamplePoints && i < functionToApply.fCollectedSoundingElements.size(); i++) {
            w[i] = Math.pow(Math.E, -d[i] / dMax);
            weightSum += w[i];
            topSum += w[i] * v[i];
          }
          return topSum / weightSum;
        }
      }
    }
  }

  private static class DistanceComparator implements Comparator<ILcdPoint> {

    private final ILcdPoint fCenterPoint;
    private final ILcdEllipsoid fEllipsoid;

    private DistanceComparator(ILcdPoint aCenterPoint, ILcdEllipsoid aEllipsoid) {
      fCenterPoint = aCenterPoint;
      fEllipsoid = aEllipsoid;
    }

    @Override
    public int compare(ILcdPoint o1, ILcdPoint o2) {
      double distance1 = fEllipsoid.geodesicDistance(o1.getFocusPoint(), fCenterPoint);
      double distance2 = fEllipsoid.geodesicDistance(o2.getFocusPoint(), fCenterPoint);
      if (distance1 < distance2) {
        return -1;
      } else if (distance1 > distance2) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  private static class CollectSoundingFunction implements ILcdFunction {

    private List<TLcdS57SoundingPoint> fCollectedSoundingElements;
    private List<ILcdS57Object> fCollectedDredgedAreas;

    public CollectSoundingFunction() {
      fCollectedSoundingElements = new ArrayList<TLcdS57SoundingPoint>();
      fCollectedDredgedAreas = new ArrayList<ILcdS57Object>();
    }

    // Implementations for ILcdFunction.

    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      if (aObject instanceof TLcdS57SoundingPoint &&
          ((ILcdS57Object) aObject).getIdentifier().getS57ObjectClass() == 129) {
        fCollectedSoundingElements.add((TLcdS57SoundingPoint) aObject);
      }
      if (aObject instanceof ILcdS57Object &&
          ((ILcdS57Object) aObject).getIdentifier().getS57ObjectClass() == 46) {
        fCollectedDredgedAreas.add((ILcdS57Object) aObject);
      }
      return true;
    }

  }
}
