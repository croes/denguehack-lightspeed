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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.partition.ILcdPartitionedGraph;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.network.basic.graph.Node;
import samples.network.common.graph.GraphManager;
import samples.network.common.view.gxy.AGraphNodePainterProvider;

/**
 * A painter provider for nodes in a graph.
 */
public class NodePainterProvider extends AGraphNodePainterProvider {

  private static final ILcdIcon ICON_START_NODE = new TLcdImageIcon("samples/images/flag_green.png");
  private static final ILcdIcon ICON_END_NODE = new TLcdImageIcon("samples/images/flag_red.png");

  private TLcdGXYIconPainter fDelegatePainter = new TLcdGXYIconPainter();

  private boolean fHighlightPartitions = true;
  private Map<ILcdGraph, Color> fPartitionColors = new HashMap<ILcdGraph, Color>();
  private Random fRandom = new Random(1);

  private TLcdSymbol fIconNode = new TLcdSymbol(TLcdSymbol.FILLED_RECT, 4, Color.gray);

  public NodePainterProvider(GraphManager aGraphManager,
                             EnumSet<GraphNodeMode> aModes) {
    super(aGraphManager, aModes);
  }

  // Implementations for AGraphNodePainterProvider.

  protected ILcdGXYPainter getGXYPainter(Object aObject, GraphNodeMode aMode) {
    if (aObject instanceof Node) {
      switch (aMode) {
      case START_NODE:
        fDelegatePainter.setIcon(ICON_START_NODE);
        break;
      case END_NODE:
        fDelegatePainter.setIcon(ICON_END_NODE);
        break;
      case NORMAL_NODE:
        // Paints each partition in a different color.
        if (getGraphManager() != null && fHighlightPartitions) {
          ILcdGraph partition = ((ILcdPartitionedGraph) getGraphManager().getGraph()).getPartitionForNode(aObject);
          Color color = getPartitioncolor(partition);
          fIconNode.setFillColor(color);
          fIconNode.setBorderColor(color);
        }
        fDelegatePainter.setIcon(fIconNode);
        break;
      }
      fDelegatePainter.setObject(aObject);
      return fDelegatePainter;
    }
    return null;
  }

  private Color getPartitioncolor(ILcdGraph aPartition) {
    Color color;
    if (fPartitionColors.containsKey(aPartition)) {
      color = fPartitionColors.get(aPartition);
    } else {
      color = new Color(50 + fRandom.nextInt(10) * 20,
                        50 + fRandom.nextInt(10) * 20,
                        50 + fRandom.nextInt(10) * 20);
      fPartitionColors.put(aPartition, color);
    }
    return color;
  }

}
