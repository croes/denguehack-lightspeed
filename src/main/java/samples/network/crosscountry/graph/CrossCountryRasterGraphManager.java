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
package samples.network.crosscountry.graph;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.model.ILcdModel;
import com.luciad.network.algorithm.routing.TLcdCrossCountryShortestRouteAlgorithm;
import com.luciad.network.function.ILcdCrossCountryDistanceFunction;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;

/**
 * This class stores all relevant graph-related information for computing cross country routes
 * based on an <code>ILcdRaster</code>: the raster itself, the start and/or end nodes that are
 * currently selected and the functions to be used by the algorithms.
 */
public class CrossCountryRasterGraphManager {

  public static final String RASTER_PROPERTY = "Raster";
  public static final String RASTER_MODEL_PROPERTY = "RasterModel";
  public static final String START_NODE_PROPERTY = "StartNode";
  public static final String END_NODE_PROPERTY = "EndNode";
  public static final String DISTANCE_FUNCTION_FACTORY_PROPERTY = "DistanceFunctionFactory";
  public static final String HEURISTIC_DISTANCE_FUNCTION_FACTORY_PROPERTY = "HeuristicDistanceFunctionFactory";

  // Raster
  private ILcdModel fRasterModel;
  private ILcdRaster fRaster;

  // Algorithm
  private TLcdCrossCountryShortestRouteAlgorithm fShortestRouteAlgorithm;

  // Functions
  private RasterDistanceFunctionFactory fDistanceFunctionFactory;
  private RasterDistanceFunctionFactory fHeuristicDistanceFunctionFactory;

  // Search parameters
  private ILcdPoint fStartNode;
  private ILcdPoint fEndNode;

  private double fMaximumDistance = Double.POSITIVE_INFINITY;

  // Listeners
  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  // get/set raster and its model

  public ILcdModel getModel() {
    return fRasterModel;
  }

  public ILcdRaster getRaster() {
    return fRaster;
  }

  public void setRaster(ILcdModel aModel, ILcdRaster aRaster) {
    ILcdRaster oldRaster = fRaster;
    ILcdModel oldRasterModel = fRasterModel;
    fRasterModel = aModel;
    fRaster = aRaster;
    // Use a discretization step that matches that of the ILcdRaster
    int graphWidth = aRaster.getTileColumnCount() * aRaster.retrieveTile(0, 0).getWidth();
    int graphHeight = aRaster.getTileRowCount() * aRaster.retrieveTile(0, 0).getHeight();
    fShortestRouteAlgorithm = new TLcdCrossCountryShortestRouteAlgorithm(
        aRaster.getBounds().getWidth() / graphWidth,
        aRaster.getBounds().getHeight() / graphHeight
    );
    firePropertyChange(RASTER_MODEL_PROPERTY, oldRasterModel, fRasterModel);
    firePropertyChange(RASTER_PROPERTY, oldRaster, fRaster);
  }

  // get/set algorithm parameters

  public RasterDistanceFunctionFactory getEdgeValueFunctionFactory() {
    return fDistanceFunctionFactory;
  }

  public void setEdgeValueFunctionFactory(RasterDistanceFunctionFactory aEdgeValueEdgeValueFunctionFactory) {
    RasterDistanceFunctionFactory oldRasterEdgeValueFunctionFactory = fDistanceFunctionFactory;
    fDistanceFunctionFactory = aEdgeValueEdgeValueFunctionFactory;
    firePropertyChange(DISTANCE_FUNCTION_FACTORY_PROPERTY, oldRasterEdgeValueFunctionFactory, fDistanceFunctionFactory);
  }

  public RasterDistanceFunctionFactory getHeuristicEstimateFunction() {
    return fHeuristicDistanceFunctionFactory;
  }

  public void setHeuristicEstimateFunction(RasterDistanceFunctionFactory aDistanceFunction) {
    RasterDistanceFunctionFactory oldDistanceFunctionFactory = fHeuristicDistanceFunctionFactory;
    fHeuristicDistanceFunctionFactory = aDistanceFunction;
    firePropertyChange(HEURISTIC_DISTANCE_FUNCTION_FACTORY_PROPERTY, oldDistanceFunctionFactory, fHeuristicDistanceFunctionFactory);
  }

  public ILcdPoint getStartNode() {
    return fStartNode;
  }

  public void setStartNode(ILcdPoint aStartNode) {
    ILcdPoint oldStartNode = fStartNode;
    fStartNode = aStartNode;
    firePropertyChange(START_NODE_PROPERTY, oldStartNode, fStartNode);
  }

  public ILcdPoint getEndNode() {
    return fEndNode;
  }

  public void setEndNode(ILcdPoint aEndNode) {
    ILcdPoint oldEndNode = fStartNode;
    fEndNode = aEndNode;
    firePropertyChange(END_NODE_PROPERTY, oldEndNode, fEndNode);
  }

  public double getMaximumDistance() {
    return fMaximumDistance;
  }

  public void setMaximumDistance(double aMaximumDistance) {
    fMaximumDistance = aMaximumDistance;
  }

  // Property changes

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    fPropertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  // Executing the algorithm

  public ILcdRoute<ILcdPoint, ILcdPolyline> getShortestRoute(double[] aRouteDistance) {
    ILcdPoint startPoint = getStartNode();
    ILcdPoint endPoint = getEndNode();
    ILcdCrossCountryDistanceFunction distanceFunction = fDistanceFunctionFactory.createDistanceFunction(fRaster);
    ILcdCrossCountryDistanceFunction heuristicDistanceFunction;
    if (fHeuristicDistanceFunctionFactory != null) {
      heuristicDistanceFunction = fHeuristicDistanceFunctionFactory.createDistanceFunction(fRaster);
    } else {
      heuristicDistanceFunction = null;
    }
    double maxDist = getMaximumDistance();
    ILcdRoute<ILcdPoint, ILcdPolyline> route = fShortestRouteAlgorithm.getShortestRoute(
        startPoint, endPoint,
        distanceFunction,
        heuristicDistanceFunction,
        maxDist
                                                                                       );
    if (route != null) {
      if (aRouteDistance != null) {
        aRouteDistance[0] = 0.0;
        for (int i = 0; i < route.getNodeCount() - 1; i++) {
          ILcdPoint p1 = route.getNode(i);
          ILcdPoint p2 = route.getNode(i + 1);
          aRouteDistance[0] += distanceFunction.computeDistance(p1, p2);
        }
      }
      return route;
    } else {
      return null;
    }
  }

}
