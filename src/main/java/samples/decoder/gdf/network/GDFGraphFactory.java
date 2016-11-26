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
package samples.decoder.gdf.network;

import java.util.Enumeration;

import com.luciad.format.gdf.ILcdGDFLineFeature;
import com.luciad.format.gdf.ILcdGDFPointFeature;
import com.luciad.model.ILcdModel;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdGraph;
import com.luciad.util.ILcdFireEventMode;

import samples.network.numeric.preprocessor.IGraphFactory;

/**
 * This factory creates graphs from GDF models that can be used within the networking API.
 * A feature class filter can be used to create only nodes and edges from point and line
 * features with a predefined feature class code.
 */
public class GDFGraphFactory implements IGraphFactory {

  private boolean fUseFeatureClassFilter = true;

  // Feature class codes for junctions and road elements (see GDF specification).
  private int fNodeFeatureClassCode = 4120;
  private int fEdgeFeatureClassCode = 4110;

  /**
   * Creates a graph of the given model, taking the filtering settings into account.
   *
   * @param aModel
   *
   * @return
   *
   * @throws NullPointerException if the given model is <code>null</code>.
   */
  public ILcdGraph createGraph(ILcdModel aModel) {

    TLcdGraph graph = new TLcdGraph();

    // Adding nodes
    int count = 0;
    for (Enumeration elements = aModel.elements(); elements.hasMoreElements(); ) {
      Object element = elements.nextElement();
      if (element instanceof ILcdGDFPointFeature) {
        if (!fUseFeatureClassFilter ||
            ((ILcdGDFPointFeature) element).getFeatureClass().getFeatureClassCode() == fNodeFeatureClassCode) {
          graph.addNode(element, ILcdFireEventMode.NO_EVENT);
          count++;
        }
      }

    }

    // Adding edges
    count = 0;
    for (Enumeration elements = aModel.elements(); elements.hasMoreElements(); ) {
      Object element = elements.nextElement();
      if (element instanceof ILcdGDFLineFeature) {
        if (!fUseFeatureClassFilter ||
            ((ILcdGDFLineFeature) element).getFeatureClass().getFeatureClassCode() == fEdgeFeatureClassCode) {
          try {
            if (((ILcdGDFLineFeature) element).getFromPoint() != null &&
                ((ILcdGDFLineFeature) element).getToPoint() != null) {
              graph.addEdge(element,
                            ((ILcdGDFLineFeature) element).getFromPoint(),
                            ((ILcdGDFLineFeature) element).getToPoint(), ILcdFireEventMode.NO_EVENT);
            }
          } catch (IllegalArgumentException e) {
            // border edge, nothing to do
          }
          count++;
        }
      }
    }

    return graph;
  }

  /**
   * Sets the feature class code for nodes. Is only used if the UseFeatureClassFilter
   * flag is set to <code>true</code>. By default, the feature class is initialized
   * to 4120, the feature class code that corresponds to road junctions.
   *
   * @param aFeatureClassCode
   */
  public void setNodeFeatureClassCode(int aFeatureClassCode) {
    fNodeFeatureClassCode = aFeatureClassCode;
  }

  /**
   * Sets the feature class code for edges. Is only used if the UseFeatureClassFilter
   * flag is set to <code>true</code>. By default, the feature class is initialized
   * to 4110, the feature class code that corresponds to road elements.
   *
   * @param aFeatureClassCode
   */
  public void setEdgeFeatureClassCode(int aFeatureClassCode) {
    fEdgeFeatureClassCode = aFeatureClassCode;
  }

  /**
   * Sets whether a feature class filter should be used when creating graphs.
   * If <code>true</code>, only features with a given feature class code will
   * be added to the graph as nodes or edges.
   *
   * @param aUseFeatureClassFilter
   */
  public void setUseFeatureClassFilter(boolean aUseFeatureClassFilter) {
    fUseFeatureClassFilter = aUseFeatureClassFilter;
  }
}
