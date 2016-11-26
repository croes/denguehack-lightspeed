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
package samples.network.numeric.view.gxy;

import java.awt.Color;
import java.util.EnumSet;

import com.luciad.network.graph.route.ILcdRoute;
import com.luciad.shape.ILcdShapeList;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphEdgePainterProvider;
import samples.network.numeric.graph.NumericGraphManager;

public class EdgePainterProvider extends AGraphEdgePainterProvider {

  private static final TLcdG2DLineStyle ROAD_STYLE = new TLcdG2DLineStyle();
  private static final TLcdG2DLineStyle START_STYLE = new TLcdG2DLineStyle();
  private static final TLcdG2DLineStyle END_STYLE = new TLcdG2DLineStyle();
  private static final TLcdG2DLineStyle DESTROYED_ROAD_STYLE = new TLcdG2DLineStyle();
  private static final TLcdG2DLineStyle ROUTE_STYLE = new TLcdG2DLineStyle();

  private static final String EDGE_ID_PROPERTY_NAME = "ID";

  static {
    ROAD_STYLE.setColor(Color.DARK_GRAY);
    ROAD_STYLE.setLineWidth(1);
    START_STYLE.setColor(new Color(0, 107, 33));
    START_STYLE.setLineWidth(1);
    END_STYLE.setColor(new Color(206, 0, 33));
    END_STYLE.setLineWidth(1);
    DESTROYED_ROAD_STYLE.setColor(Color.RED);
    DESTROYED_ROAD_STYLE.setLineWidth(2);
    ROUTE_STYLE.setColor(Color.BLUE);
    ROUTE_STYLE.setLineWidth(2);
  }

  private TLcdGXYPointListPainter fPainter = new TLcdGXYPointListPainter();

  public EdgePainterProvider(GraphManager aGraphManager, EnumSet<GraphEdgeMode> aModes) {
    super(aGraphManager, aModes, EDGE_ID_PROPERTY_NAME);
  }

  @Override
  public void addRoute(ILcdRoute aRoute) {
    for (int i = 0; i < aRoute.getEdgeCount(); i++) {
      fRouteEdges.add(((NumericGraphManager) fGraphManager).getId2EdgeIdMap().getId((Long) aRoute.getEdge(i)));
    }
  }

  protected ILcdGXYPainter getGXYPainter(Object aObject, GraphEdgeMode aMode) {
    switch (aMode) {
    case NORMAL_EDGE:
      fPainter.setLineStyle(ROAD_STYLE);
      break;
    case START_EDGE:
      fPainter.setLineStyle(START_STYLE);
      break;
    case END_EDGE:
      fPainter.setLineStyle(END_STYLE);
      break;
    case ROUTE_EDGE:
      fPainter.setLineStyle(ROUTE_STYLE);
      break;
    case DESTROYED_EDGE:
      fPainter.setLineStyle(DESTROYED_ROAD_STYLE);
      break;
    }

    fPainter.setObject(((ILcdShapeList) aObject).getShape(0));
    return fPainter;
  }

}
