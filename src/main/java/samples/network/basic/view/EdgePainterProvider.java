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

import java.awt.Color;
import java.util.EnumSet;

import com.luciad.view.gxy.ILcdGXYPainter;

import samples.network.basic.graph.Edge;
import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphEdgePainterProvider;

/**
 * A painter provider for edges in a graph.
 */
public class EdgePainterProvider extends AGraphEdgePainterProvider {

  private EdgePainter fDelegatePainter = new EdgePainter();

  public EdgePainterProvider(GraphManager aGraphManager,
                             EnumSet<AGraphEdgePainterProvider.GraphEdgeMode> aModes) {
    super(aGraphManager, aModes, null);
    fDelegatePainter.setGraphManager(aGraphManager);
  }

  // Implementations for AGraphEdgePainterProvider.

  protected ILcdGXYPainter getGXYPainter(Object aObject, GraphEdgeMode aMode) {
    if (aObject instanceof Edge) {
      switch (aMode) {
      case START_EDGE:
        fDelegatePainter.setColor(new Color(0, 107, 33));
        break;
      case END_EDGE:
        fDelegatePainter.setColor(new Color(206, 0, 33));
        break;
      case ROUTE_EDGE:
        fDelegatePainter.setColor(Color.BLUE);
        break;
      case NORMAL_EDGE:
        fDelegatePainter.setColor(Color.GRAY);
        break;
      }
      fDelegatePainter.setObject(aObject);
      return fDelegatePainter;
    }
    return null;
  }

}
