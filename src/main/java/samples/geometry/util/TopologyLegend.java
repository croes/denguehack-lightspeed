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
package samples.geometry.util;

import java.awt.Color;

import javax.swing.JPanel;

import samples.gxy.common.TitledPanel;
import samples.common.gui.ColorLegend;

public class TopologyLegend {

  public static JPanel createTopologyLegend() {
    Color[] colors = new Color[] {TopologyPainterProvider.COLOR_DEFAULT, TopologyPainterProvider.COLOR_SELECTED, TopologyPainterProvider.COLOR_CROSSES, TopologyPainterProvider.COLOR_TOUCHES, TopologyPainterProvider.COLOR_CONTAIN_WITHIN, TopologyPainterProvider.COLOR_INTERSECTS };
    for(int i = 0; i < colors.length; i++) colors[i] = TopologyPainterProvider.multiplyColor( colors[i], 0.75 );
    String[] labels = new String[] { "Default", "Selected", "Crosses", "Touches", "Contains or within", "Intersects" };
    return TitledPanel.createTitledPanel("Legend", new ColorLegend(labels, colors, false));
  }
}
