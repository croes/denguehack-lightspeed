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
package samples.decoder.gdf.view.gxy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.luciad.format.gdf.ILcdGDFLineFeature;
import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphEdgePainterProvider;
import samples.network.numeric.graph.NumericGraphManager;

/**
 * Painter provider for GDF line features.
 */
public class GDFLinePainterProvider extends AGraphEdgePainterProvider {

  public static enum Mode {
    ALL_ROAD_BORDER,
    ROAD_INNER,
    ROAD_SIGNS
  }

  private static final Color START_EDGE_COLOR = new Color(0, 107, 33);
  private static final Color END_EDGE_COLOR = new Color(206, 0, 33);
  private static final Color DESTROYED_EDGE_COLOR = new Color(206, 0, 33);

  private static final OutlineStyle RAILWAY_OUTER_STROKE = new OutlineStyle(new Color(150, 150, 150),
                                                                            new BasicStroke(3));
  private static final OutlineStyle RAILWAY_INNER_STROKE = new OutlineStyle(Color.WHITE,
                                                                            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10, 10}, 10));

  private Mode fMode;

  private Map<Integer, ILcdGXYPainterStyle> fLineStyles = new HashMap<Integer, ILcdGXYPainterStyle>();

  private TLcdGXYPointListPainter fPointListPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
  private GDFRoadPainter fRoadPainter;

  public GDFLinePainterProvider(GraphManager aGraphManager,
                                EnumSet<GraphEdgeMode> aGraphModes,
                                Mode aPaintMode,
                                GDFRenderingSettings aRenderingSettings) {
    super(aGraphManager, aGraphModes, null);
    fMode = aPaintMode;
    fRoadPainter = new GDFRoadPainter(aPaintMode, aRenderingSettings);
  }

  public GDFLinePainterProvider(Mode aMode,
                                GDFRenderingSettings aRenderingSettings) {
    fMode = aMode;
    fRoadPainter = new GDFRoadPainter(aMode, aRenderingSettings);
  }

  @Override
  public void addRoute(ILcdRoute aRoute) {
    if (fGraphManager instanceof NumericGraphManager) {
      for (int i = 0; i < aRoute.getEdgeCount(); i++) {
        fRouteEdges.add(((NumericGraphManager) fGraphManager).getId2EdgeIdMap().getId((Long) aRoute.getEdge(i)));
      }
    } else {
      super.addRoute(aRoute);
    }
  }

  @Override
  protected boolean isRouteEdge(Object aEdge) {
    if (!(aEdge instanceof ILcdGDFLineFeature)) {
      return false;
    }
    if (fGraphManager instanceof NumericGraphManager) {
      return super.isRouteEdge(((ILcdGDFLineFeature) aEdge).getFeatureKey().getFeatureIdentifier());
    } else {
      return super.isRouteEdge(aEdge);
    }
  }

  /**
   * Registers the specified line style for the given GDF feature class code.
   *
   * @param aFeatureClassCode the feature class code for which to register the style.
   * @param aLineStyle        the style to be registered.
   */
  public void registerStyle(int aFeatureClassCode, ILcdGXYPainterStyle aLineStyle) {
    fLineStyles.put(aFeatureClassCode, aLineStyle);
  }

  // Implementations for AGraphEdgePainterProvider.

  protected ILcdGXYPainter getGXYPainter(Object aObject, GraphEdgeMode aMode) {
    if (aObject instanceof ILcdGDFLineFeature) {
      ILcdGDFLineFeature feature = (ILcdGDFLineFeature) aObject;
      int fcc = feature.getFeatureClass().getFeatureClassCode();

      if (fcc == 4110) {
        // Roads.
        if (fMode == Mode.ROAD_INNER && aMode == GraphEdgeMode.ROUTE_EDGE) {
          fRoadPainter.setHighlightColor(Color.RED);
          fRoadPainter.setHighlight(true);
        } else if (fMode == Mode.ROAD_INNER && aMode == GraphEdgeMode.START_EDGE) {
          fRoadPainter.setHighlightColor(START_EDGE_COLOR);
          fRoadPainter.setHighlight(true);
        } else if (fMode == Mode.ROAD_INNER && aMode == GraphEdgeMode.END_EDGE) {
          fRoadPainter.setHighlightColor(END_EDGE_COLOR);
          fRoadPainter.setHighlight(true);
        } else if (fMode == Mode.ROAD_INNER && aMode == GraphEdgeMode.DESTROYED_EDGE) {
          fRoadPainter.setHighlightColor(DESTROYED_EDGE_COLOR);
          fRoadPainter.setHighlight(true);
        } else {
          fRoadPainter.setHighlight(false);
        }
        fRoadPainter.setObject(aObject);
        return fRoadPainter;
      } else if (fcc == 4210) {
        // Railroads.
        if (fMode == Mode.ALL_ROAD_BORDER || fMode == Mode.ROAD_INNER) {
          fPointListPainter.setObject(aObject);
          fPointListPainter.setLineStyle(fMode == Mode.ALL_ROAD_BORDER ? RAILWAY_OUTER_STROKE : RAILWAY_INNER_STROKE);
          return fPointListPainter;
        }
      } else {
        // Other line features.
        ILcdGXYPainterStyle style = fLineStyles.get(fcc);
        if (style != null) {
          fPointListPainter.setObject(aObject);
          fPointListPainter.setLineStyle(style);
          return fPointListPainter;
        }
      }
    }

    return null;
  }

  private static class OutlineStyle implements ILcdGXYPainterStyle {

    private Color fColor;
    private Stroke fStroke;

    private OutlineStyle(Color aColor, Stroke aStroke) {
      fColor = aColor;
      fStroke = aStroke;
    }

    public void setupGraphics(Graphics aGraphics, Object aObject, int aMode, ILcdGXYContext aGXYContext) {
      aGraphics.setColor(fColor);
      ((Graphics2D) aGraphics).setStroke(fStroke);
    }
  }

}
