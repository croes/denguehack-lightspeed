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
package samples.network.basic.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Enumeration;

import com.luciad.gui.ILcdIcon;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.network.graph.route.TLcdRoute;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ALcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.network.basic.graph.Edge;
import samples.network.basic.graph.Node;
import samples.network.common.graph.GraphManager;

/**
 * A painter for an edge in a graph.
 */
public class EdgePainter extends ALcdGXYPainter {

  private TLcdGXYPointListPainter fLinePainter = new TLcdGXYPointListPainter();
  private TLcdG2DLineStyle fLineStyle = new TLcdG2DLineStyle();

  private TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter();
  private ArrowIcon fArrowIcon = new ArrowIcon(Color.black);
  private ArrowIcon fArrowIconHighlighted = new ArrowIcon(Color.magenta);

  private GraphManager fGraphManager;

  private Object fObject;

  public EdgePainter() {
    fLineStyle.setColor(Color.black);
    fLineStyle.setSelectionColor(Color.magenta);
    fLineStyle.setLineWidth(1);
    fLineStyle.setSelectionLineWidth(1);
    fLinePainter.setLineStyle(fLineStyle);

    fIconPainter.setIcon(fArrowIcon);
    fIconPainter.setSelectionIcon(fArrowIconHighlighted);
  }

  public void setGraphManager(GraphManager aGraphManager) {
    fGraphManager = aGraphManager;
  }

  public void setColor(Color aColor) {
    fLineStyle.setColor(aColor);
  }

  // Implementation of ILcdGXYPainter

  public Object getObject() {
    return fObject;
  }

  public void setObject(Object aObject) {
    fObject = aObject;
  }

  public void boundsSFCT(Graphics graphics,
                         int i,
                         ILcdGXYContext iLcdGXYContext,
                         ILcd2DEditableBounds iLcd2DEditableBounds) throws TLcdNoBoundsException {
    fLinePainter.setObject(getObject());
    fLinePainter.boundsSFCT(graphics, i, iLcdGXYContext, iLcd2DEditableBounds);
  }

  public boolean isTouched(Graphics aGraphics, int i, ILcdGXYContext aILcdGXYContext) {
    fLinePainter.setObject(getObject());
    return fLinePainter.isTouched(aGraphics, i, aILcdGXYContext);
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {

    Edge edge = (Edge) getObject();

    paintLine(aGraphics, aMode, aGXYContext, edge, null);
    paintArrow(aGraphics, aMode, aGXYContext, edge, fLineStyle.getColor());
    paintTurns(aGraphics, aMode, aGXYContext, edge);
  }

  // Private helper methods.

  private void paintLine(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Edge edge, Color aColor) {

    fLineStyle.setSelectionColor(Color.magenta);
    fLineStyle.setLineWidth(edge.getMaxSpeed() / 3);
    fLineStyle.setSelectionLineWidth(edge.getMaxSpeed() / 3);
    fLinePainter.setObject(getObject());
    fLinePainter.paint(aGraphics, aMode, aGXYContext);
  }

  private void paintArrow(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Edge edge, Color aColor) {

    fArrowIcon.setColor(aColor);

    if (edge.isDirected()) {
      if (edge.getPoint(0).getX() < edge.getPoint(1).getX()) {
        fArrowIcon.setRotation(0);
        fArrowIconHighlighted.setRotation(0);
      }
      if (edge.getPoint(0).getX() > edge.getPoint(1).getX()) {
        fArrowIcon.setRotation(Math.PI);
        fArrowIconHighlighted.setRotation(Math.PI);
      }
      if (edge.getPoint(0).getY() < edge.getPoint(1).getY()) {
        fArrowIcon.setRotation(Math.PI * 3 / 2);
        fArrowIconHighlighted.setRotation(Math.PI * 3 / 2);
      }
      if (edge.getPoint(0).getY() > edge.getPoint(1).getY()) {
        fArrowIcon.setRotation(Math.PI / 2);
        fArrowIconHighlighted.setRotation(Math.PI / 2);
      }

      TLcdXYPoint point = new TLcdXYPoint(
          (edge.getPoint(0).getX() + edge.getPoint(1).getX()) / 2,
          (edge.getPoint(0).getY() + edge.getPoint(1).getY()) / 2
      );
      fIconPainter.setObject(point);
      fIconPainter.paint(aGraphics, aMode, aGXYContext);
    }
  }

  private void paintTurns(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Edge edge) {

    if (fGraphManager != null) {
      ILcdGraph graph = fGraphManager.getGraph();

      Object start_node = graph.getStartNode(edge);
      Object end_node = graph.getEndNode(edge);

      Enumeration start_edges = graph.getEdges(start_node);
      Enumeration end_edges = graph.getEdges(end_node);

      while (start_edges.hasMoreElements()) {
        Edge start_edge = (Edge) start_edges.nextElement();

        TLcdRoute route = new TLcdRoute(graph, start_node);
        route.addEdgeAtStart(edge);

        TLcdRoute empty_route = new TLcdRoute(graph, start_node);

        if ((fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, route, start_edge, TLcdTraversalDirection.FORWARD) !=
             fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, empty_route, start_edge, TLcdTraversalDirection.FORWARD)
             ||
             fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, route, start_edge, TLcdTraversalDirection.BACKWARD) !=
             fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, empty_route, start_edge, TLcdTraversalDirection.BACKWARD)
            ) && start_edge != edge) {
          paintTurnElement(aGraphics, aMode, aGXYContext, edge, (Node) graph.getStartNode(edge), start_edge);
        }
      }

      while (end_edges.hasMoreElements()) {
        Edge end_edge = (Edge) end_edges.nextElement();

        TLcdRoute route = new TLcdRoute(graph, end_node);
        route.addEdgeAtStart(edge);

        TLcdRoute empty_route = new TLcdRoute(graph, end_node);

        if ((fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, route, end_edge, TLcdTraversalDirection.FORWARD) !=
             fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, empty_route, end_edge, TLcdTraversalDirection.FORWARD)
             ||
             fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, route, end_edge, TLcdTraversalDirection.BACKWARD) !=
             fGraphManager.getEdgeValueFunction().computeEdgeValue(graph, empty_route, end_edge, TLcdTraversalDirection.BACKWARD)
            ) && end_edge != edge) {
          paintTurnElement(aGraphics, aMode, aGXYContext, edge, (Node) graph.getEndNode(edge), end_edge);
        }
      }
    }
  }

  private void paintTurnElement(Graphics aGraphics,
                                int aMode,
                                ILcdGXYContext aGXYContext,
                                Edge edge,
                                Node node,
                                Edge start_edge) {
    int x1, x2, y1, y2;

    if (edge.getPoint(0).getX() != edge.getPoint(1).getX()) {
      if (node.getX() == Math.min(edge.getPoint(0).getX(), edge.getPoint(1).getX())) {
        x1 = (int) node.getX() + 1;
        x2 = (int) node.getX() + 2;
      } else {
        x1 = (int) node.getX() - 1;
        x2 = (int) node.getX() - 2;
      }

      if (start_edge.getPoint(0).getY() < node.getY() ||
          start_edge.getPoint(1).getY() < node.getY()) {
        y1 = (int) node.getY() - 1;
        y2 = (int) node.getY() - 1;
      } else {
        y1 = (int) node.getY() + 1;
        y2 = (int) node.getY() + 1;
      }
    } else {
      if (node.getY() == Math.min(edge.getPoint(0).getY(), edge.getPoint(1).getY())) {
        y1 = (int) node.getY() + 1;
        y2 = (int) node.getY() + 2;
      } else {
        y1 = (int) node.getY() - 1;
        y2 = (int) node.getY() - 2;
      }

      if (start_edge.getPoint(0).getX() < node.getX() ||
          start_edge.getPoint(1).getX() < node.getX()) {
        x1 = (int) node.getX() - 1;
        x2 = (int) node.getX() - 1;
      } else {
        x1 = (int) node.getX() + 1;
        x2 = (int) node.getX() + 1;
      }
    }
    ILcd2DEditablePointList pointlist = new TLcd2DEditablePointList();
    pointlist.insert2DPoint(0, x1, y1);
    pointlist.insert2DPoint(1, x2, y2);
    TLcdXYPolyline line = new TLcdXYPolyline(pointlist);
    fLineStyle.setColor(Color.red);
    fLineStyle.setSelectionColor(Color.red);
    fLinePainter.setObject(line);
    fLinePainter.paint(aGraphics, aMode, aGXYContext);
  }

  /**
   * Paints an arrow icon.
   */
  private class ArrowIcon implements ILcdIcon {

    private double fRadius = 5;
    private double fRotation;
    private Color fColor;

    public ArrowIcon(Color aColor) {
      setColor(aColor);
    }

    public void setColor(Color aColor) {
      fColor = aColor;
    }

    public void setRotation(double aRotation) {
      fRotation = aRotation;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      x += 2;
      y += 2;
      graphics.setColor(fColor);
      ((Graphics2D) graphics).setStroke(new BasicStroke(1));
      int[] x_points = new int[]{(int) (x + fRadius * Math.cos(fRotation)),
                                 (int) (x + fRadius * Math.cos(fRotation + Math.PI * 3 / 4)),
                                 (int) (x + fRadius * Math.cos(fRotation + Math.PI * 5 / 4))};
      int[] y_points = new int[]{(int) (y + fRadius * Math.sin(fRotation)),
                                 (int) (y + fRadius * Math.sin(fRotation + Math.PI * 3 / 4)),
                                 (int) (y + fRadius * Math.sin(fRotation + Math.PI * 5 / 4))};
      graphics.fillPolygon(x_points, y_points, 3);
    }

    public int getIconWidth() {
      return 5;
    }

    public int getIconHeight() {
      return 5;
    }

    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }
  }

}
