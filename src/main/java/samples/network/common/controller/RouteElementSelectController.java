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
package samples.network.common.controller;

import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShapeList;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYSelectController2;
import com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2;

import samples.network.common.graph.GraphManager;

/**
 * A controller for selecting route elements.
 */
public class RouteElementSelectController extends TLcdGXYSelectController2 implements MouseListener {

  public static ILcdIcon ICON_START_NODE = new TLcdImageIcon("samples/images/flag_green.png");
  public static ILcdIcon ICON_START_EDGE = new TLcdImageIcon("samples/images/flag_green_line.png");
  public static ILcdIcon ICON_END_NODE = new TLcdImageIcon("samples/images/flag_red.png");
  public static ILcdIcon ICON_END_EDGE = new TLcdImageIcon("samples/images/flag_red_line.png");

  public enum Mode {
    START_NODE, START_EDGE, END_NODE, END_EDGE
  }

  private GraphManager fGraphManager;
  private Mode fMode;

  public RouteElementSelectController(GraphManager aGraphManager,
                                      Mode aMode) {
    switch (aMode) {
    case START_NODE:
      setName("Select start node.");
      setShortDescription("Select start node.");
      setIcon(ICON_START_NODE);
      break;
    case START_EDGE:
      setName("Select start edge.");
      setShortDescription("Select start edge.");
      setIcon(ICON_START_EDGE);
      break;
    case END_NODE:
      setName("Select end node.");
      setShortDescription("Select end node.");
      setIcon(ICON_END_NODE);
      break;
    case END_EDGE:
      setName("Select end edge.");
      setShortDescription("Select end edge.");
      setIcon(ICON_END_EDGE);
      break;
    }

    fGraphManager = aGraphManager;
    fMode = aMode;

    setSelectControllerModel(new TLcdGXYSelectControllerModel2() {
      @Override
      public void applySelection(ILcdGXYView aGXYView, Rectangle aSelectionBounds, int aMouseMode, int aX, int aY, int aSelectByWhatMode, int aSelectHowMode, ILcdGXYLayerSubsetList aSelectionCandidates) {
        Enumeration elementsEnum = aSelectionCandidates.elements();
        if ((aSelectByWhatMode & SELECT_BY_WHAT_BODIES_ON_CLICK) != 0) {
          while (elementsEnum.hasMoreElements()) {
            Object element = elementsEnum.nextElement();
            switch (fMode) {
            case START_NODE:
              if (element instanceof ILcdPoint) {
                fGraphManager.setStartNode(element);
                Object startEdge = fGraphManager.getStartEdge();
                if (startEdge != null && !fGraphManager.canSetStartEdge(startEdge)) {
                  fGraphManager.setStartEdge(null);
                }
                return;
              }
              break;
            case START_EDGE:
              if ((element instanceof ILcdShapeList || element instanceof ILcdPolyline) && fGraphManager.canSetStartEdge(element)) {
                fGraphManager.setStartEdge(element);
                return;
              }
              break;
            case END_NODE:
              if (element instanceof ILcdPoint) {
                fGraphManager.setEndNode(element);
                Object endEdge = fGraphManager.getEndEdge();
                if (endEdge != null && !fGraphManager.canSetEndEdge(endEdge)) {
                  fGraphManager.setEndEdge(null);
                }

                return;
              }
              break;
            case END_EDGE:
              if ((element instanceof ILcdShapeList || element instanceof ILcdPolyline) && fGraphManager.canSetEndEdge(element)) {
                fGraphManager.setEndEdge(element);
                return;
              }
              break;
            }
          }
        }
      }
    });
  }

}
